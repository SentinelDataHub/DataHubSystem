/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 GAEL Systems
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gael.dhus.database.liquibase;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtractProductDatesAndDownloadSize implements CustomTaskChange
{
   private static final Logger LOGGER = LogManager.getLogger(ExtractProductDatesAndDownloadSize.class);

   @Override
   public String getConfirmationMessage ()
   {
      return null;
   }

   @Override
   public void setFileOpener (ResourceAccessor resource_accessor)
   {
   }   

   @Override
   public void setUp () throws SetupException
   {
   }

   @Override
   public ValidationErrors validate (Database arg0)
   {
      return null;
   }

   @Override
   public void execute (Database database) throws CustomChangeException
   {
      // contentStart contentEnd ingestionDate download.size
      JdbcConnection databaseConnection =
         (JdbcConnection) database.getConnection ();
      try
      {
         // SELECT PRODUCT_ID, QUERYABLE, VALUE FROM METADA_INDEXES
         PreparedStatement getQueryables =
               databaseConnection
                     .prepareStatement ("SELECT PRODUCT_ID, QUERYABLE, VALUE " +
                  "FROM METADATA_INDEXES");
         ResultSet res = getQueryables.executeQuery ();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
         while (res.next ())
         {
            Long productIdentifier = (Long) res.getObject ("PRODUCT_ID");   
            String queryable = (String) res.getObject ("QUERYABLE");  
            String value = (String) res.getObject ("VALUE");  
            String query = "";
            if ("beginPosition".equals (queryable))
            {
               Date d = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS")
                     .parse(value);
               query = "UPDATE PRODUCTS SET CONTENTSTART = '"+sdf.format (d)+
                     "' WHERE ID = "+productIdentifier;
            }
            else if ("endPosition".equals (queryable))
            {
               Date d = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS")
                     .parse(value);
               query = "UPDATE PRODUCTS SET CONTENTEND = '"+sdf.format (d)+
                     "' WHERE ID = "+productIdentifier;
            }
            else if ("ingestionDate".equals (queryable))
            {
               Date d = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS")
                     .parse(value);
               query = "UPDATE PRODUCTS SET INGESTIONDATE = '"+sdf.format (d)+
                     "' WHERE ID = "+productIdentifier;
            }
            else
            {
               continue;
            }
            PreparedStatement updateProduct = databaseConnection
                  .prepareStatement (query);
            updateProduct.execute ();
            updateProduct.close();
         }
         getQueryables.close ();
      }
      catch (Exception e)
      {
         LOGGER.error ("Error during liquibase update " +
               "'ExtractProductDatesAndDownloadSize'", e);
      }
      
      try
      {
         PreparedStatement getQueryables =
               databaseConnection.prepareStatement (
                           "SELECT p.ID, p.DOWNLOAD_PATH FROM PRODUCTS p ");
         ResultSet res = getQueryables.executeQuery ();

         while (res.next ())
         {
            Long productIdentifier = (Long) res.getObject ("ID");   
            String path = (String) res.getObject ("DOWNLOAD_PATH");
            File dl = new File(path);
            if (dl.exists())
            {
               PreparedStatement updateProduct = databaseConnection
                     .prepareStatement ("UPDATE PRODUCTS SET DOWNLOAD_SIZE = " +
                           dl.length ()+" WHERE ID = "+productIdentifier);
               updateProduct.execute ();
               updateProduct.close();
            }
         }
         getQueryables.close ();
      }
      catch (Exception e)
      {
         LOGGER.error ("Error during liquibase update " +
               "'ExtractProductDatesAndDownloadSize'", e);
      }
   }
}
