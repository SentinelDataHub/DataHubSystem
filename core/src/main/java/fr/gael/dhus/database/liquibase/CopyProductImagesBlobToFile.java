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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.compress.utils.IOUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

public class CopyProductImagesBlobToFile implements CustomTaskChange
{
   private static final Logger LOGGER = LogManager.getLogger(CopyProductImagesBlobToFile.class);

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

   /**
    * This method executes:
    *  - extraction of quicklooks and thumbnails from the database to files,
    *  - references these files into the data base.
    *  
    *  remove/update processes are let to liquibase scripts.
    */
   @Override
   public void execute (Database database) throws CustomChangeException
   {
      PreparedStatement products=null;
      ResultSet products_res=null;
      JdbcConnection db_connection = (JdbcConnection) database.getConnection ();
      try
      {
         products = db_connection.prepareStatement (
            "SELECT PRODUCT.ID ID," +
            "       PRODUCT.DOWNLOAD_PATH DWN_PATH, " +
            "       PRODUCT.PATH PRODUCT_PATH," +
            "       IMAGE.QUICKLOOK QUICKLOOK," +
            "       IMAGE.THUMBNAIL THUMBNAIL "+
            "FROM PRODUCTS PRODUCT, PRODUCT_IMAGES IMAGE " +
            "WHERE PRODUCT.IMAGES_ID=IMAGE.ID");
         products_res = products.executeQuery ();
         while (products_res.next ())
         {
            Blob ql = (Blob) products_res.getObject ("QUICKLOOK");
            Blob th = (Blob) products_res.getObject ("THUMBNAIL");
            long id = products_res.getLong ("ID");
            String  download_path = products_res.getString ("DWN_PATH");
            String  product_path = products_res.getString ("PRODUCT_PATH");
               
            if (download_path == null)
            {
               LOGGER.error("No download path for product '" + 
                  product_path + "': product images not managed");
               continue;
            }
               
            // copy blobs into files and update products table
            if (ql != null)
            {
               // Copy file
               String ql_path = download_path.replaceAll ("(?i)(.*).zip",
                     "$1-ql.gif");
               blobToFile (ql, ql_path);
               
               // Update products table
               PreparedStatement product_flags_stmt = null;
               // Add related flags
               try
               {
                  product_flags_stmt = db_connection.prepareStatement (
                     "UPDATE PRODUCTS SET QUICKLOOK_PATH=? WHERE ID=?");
                  product_flags_stmt.setString (1, ql_path);
                  product_flags_stmt.setLong (2, id);
                  product_flags_stmt.execute ();
               }
               finally
               {
                  if (product_flags_stmt!=null) 
                     try
                     {
                        product_flags_stmt.close ();
                     }
                     catch (Exception e)
                     {
                        LOGGER.warn("Cannot close Statement !");
                     }
               }
            }
               
            if (th != null)
            {
               String th_path = download_path.replaceAll ("(?i)(.*).zip",
                     "$1-th.gif");
               blobToFile (th, th_path);
               // Update products table
               PreparedStatement product_flags_stmt = null;
               // Add related flags
               try
               {
                  product_flags_stmt = db_connection.prepareStatement (
                     "UPDATE PRODUCTS SET THUMBNAIL_PATH=? WHERE ID=?");
                  product_flags_stmt.setString (1, th_path);
                  product_flags_stmt.setLong   (2, id);
                  product_flags_stmt.execute ();
               }
               finally
               {
                  if (product_flags_stmt!=null) 
                     try
                     {
                        product_flags_stmt.close ();
                     }
                     catch (Exception e)
                     {
                        LOGGER.warn("Cannot close Statement !");
                     }
               }
            }
         }
         // RUN CHECKPOINT TO clean lob data
         PreparedStatement product_flags_stmt = null;
         try
         {
            product_flags_stmt = db_connection.prepareStatement (
               "CHECKPOINT DEFRAG");
            product_flags_stmt.execute ();
         }
         catch (Exception e)
         {
            LOGGER.error("Cannot perform database checkpoint defrag command",
                  e);
         }
         finally
         {
            if (product_flags_stmt!=null) 
               try
               {
                  product_flags_stmt.close ();
               }
               catch (Exception e)
               {
                  LOGGER.warn("Cannot close Statement !", e);
               }
         }
      }
      catch (Exception e)
      {
         throw new CustomChangeException ("Cannot move Blobs from product", e);
      }
      finally
      {
         if (products_res!=null)
         {
            try
            {
               products_res.close ();
            }
            catch (Exception e)
            {
               LOGGER.warn("Cannot close ResultSet !");
            }
         }
         if (products!=null)
         {
            try
            {
               products.close ();
            }
            catch (Exception e)
            {
               LOGGER.warn("Cannot close Statement !");
            }
         }
         //if (db_connection!=null) try { db_connection.close (); }
         // catch (Exception e) {}
      }
   }
   
   private void blobToFile (Blob blob, String out)
   {
      InputStream is = null;
      OutputStream os = null;
      BufferedOutputStream bos = null;
      
      try
      {
         is = blob.getBinaryStream ();
         os = new FileOutputStream (out);
         bos = new BufferedOutputStream (os);
         IOUtils.copy (is, bos);
         bos.flush ();
      }
      catch (Exception e) 
      {
         LOGGER.error("Cannot copy blob into '" + out + "'.", e);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close ();
            }
            catch (IOException e)
            {
               LOGGER.warn("Cannot close InputStream !");
            }
         }
         if (bos != null)
         {
            try
            {
               bos.close ();
            }
            catch (IOException e)
            {
               LOGGER.warn("Cannot close BufferedOutputStream !");
            }
         }
         if (os != null)
         {
            try
            {
               os.close ();
            }
            catch (IOException e)
            {
               LOGGER.warn("Cannot close OutputStream !");
            }
         }
      }
   }
}
