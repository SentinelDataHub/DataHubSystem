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
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Corrects invalid ingestion dates in database.
 */
public class CorrectsIngestionDate implements CustomTaskChange
{

   /**
    * Logger of this class.
    */
   private static final Logger LOGGER = LogManager.getLogger(CorrectsIngestionDate.class);

   @Override
   public void execute (Database database) throws CustomChangeException
   {
      SimpleDateFormat metaSdf =
            new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      SimpleDateFormat productSdf =
            new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
      String query =
            "SELECT p.ID, m.VALUE, p.INGESTIONDATE " +
                  "FROM PRODUCTS p LEFT OUTER JOIN " +
                  "METADATA_INDEXES m ON p.ID = m.PRODUCT_ID " +
                  "WHERE m.NAME = 'Ingestion Date'";
      try
      {
         JdbcConnection connection = (JdbcConnection) database.getConnection ();
         PreparedStatement statement = connection.prepareStatement (query);
         ResultSet result = statement.executeQuery ();
         while (result.next ())
         {
            Date validIngestionDate = metaSdf.parse (result.getString (2));
            Date ingestionDate = productSdf.parse (result.getString (3));
            long diffMilli =
                  validIngestionDate.getTime () - ingestionDate.getTime ();
            long diffHour = diffMilli / (1000 * 60 * 60);
            if (diffHour >= 11.0)
            {
               StringBuilder sb = new StringBuilder ();
               sb.append ("UPDATE PRODUCTS ");
               sb.append ("SET INGESTIONDATE = '").append (
                     productSdf.format (validIngestionDate)).append ("' ");
               sb.append ("WHERE ID = ").append (result.getLong (1));
               PreparedStatement update =
                     connection.prepareStatement (sb.toString ());
               if (update.executeUpdate () != 1)
               {
                  LOGGER.warn("Cannot change ingestion date for product#" +
                        result.getLong (1));
               }
            }
         }
         result.close ();
         statement.close ();
      }
      catch (DatabaseException | SQLException | ParseException e)
      {
         throw new CustomChangeException (
               "An error occurred during liquibase execution: ", e);
      }
   }

   @Override
   public String getConfirmationMessage ()
   {
      return null;
   }

   @Override
   public void setUp () throws SetupException
   {

   }

   @Override
   public void setFileOpener (ResourceAccessor resource_accessor)
   {

   }

   @Override
   public ValidationErrors validate (Database database)
   {
      return null;
   }
}
