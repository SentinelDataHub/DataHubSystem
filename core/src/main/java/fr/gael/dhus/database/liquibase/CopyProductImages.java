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

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CopyProductImages implements CustomTaskChange
{
   private static final Logger LOGGER = LogManager.getLogger(CopyProductImages.class);
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
      PreparedStatement products=null;
      ResultSet products_res=null;
      JdbcConnection db_connection = (JdbcConnection) database.getConnection ();
      try
      {
         products = db_connection.prepareStatement (
            "SELECT ID,QUICKLOOK,THUMBNAIL FROM PRODUCTS");
         products_res = products.executeQuery ();
         while (products_res.next ())
         {
            PreparedStatement copy_blob_stmt=null;
            ResultSet generated_key_res=null;
            try
            {
               Blob ql = (Blob) products_res.getObject ("QUICKLOOK");
               Blob th = (Blob) products_res.getObject ("THUMBNAIL");
               Long pid = products_res.getLong ("ID");

               // No images: add false flags
               if ( (ql == null) && (th == null))
               {
                  PreparedStatement product_flags_stmt = null;
                  // Add related flags
                  try
                  {
                     product_flags_stmt = db_connection.
                        prepareStatement (
                        "UPDATE PRODUCTS SET THUMBNAIL_FLAG=?,QUICKLOOK_FLAG=? "
                              + "WHERE ID=?");
                     product_flags_stmt.setBoolean (1,false);
                     product_flags_stmt.setBoolean (2,false);
                     product_flags_stmt.setLong (3, pid);
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
                  continue;
               }

               copy_blob_stmt = db_connection.prepareStatement (
                  "INSERT INTO PRODUCT_IMAGES (QUICKLOOK,THUMBNAIL) " +
                        "VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
               
               copy_blob_stmt.setBlob (1, ql);
               copy_blob_stmt.setBlob (2, th);
               copy_blob_stmt.execute ();

               generated_key_res = copy_blob_stmt.getGeneratedKeys ();
               if (generated_key_res.next ())
               {
                  PreparedStatement set_product_image_id_stmt = null;
                  Long iid =  generated_key_res.getLong (1);

                  // Add ProductImages "IMAGES" entry in product
                  try
                  {
                     set_product_image_id_stmt = db_connection.
                        prepareStatement (
                        "UPDATE PRODUCTS SET IMAGES_ID=?, THUMBNAIL_FLAG=?, " +
                              "QUICKLOOK_FLAG=?  WHERE ID=?");
                     set_product_image_id_stmt.setLong (1,iid);
                     set_product_image_id_stmt.setBoolean (2,th!=null);
                     set_product_image_id_stmt.setBoolean (3,ql!=null);
                     set_product_image_id_stmt.setLong (4, pid);
                     set_product_image_id_stmt.execute ();
                  }
                  finally
                  {
                     if (set_product_image_id_stmt!=null) 
                        try
                        {
                           set_product_image_id_stmt.close ();
                        }
                     catch (Exception e)
                     {
                        LOGGER.warn("Cannot close Statement !");
                     }
                  }
               }
               else
               {
                  LOGGER.error("Cannot retrieve Image primary key for " +
                        "product ID #" + products_res.getLong ("ID"));
               }
            }
            finally
            {
               if (generated_key_res != null)
                  try
                  {
                     generated_key_res.close ();
                  }
                  catch (Exception e)
                  {
                     LOGGER.warn("Cannot close ResultSet !");
                  }
               if (copy_blob_stmt != null)
                  try
                  {
                     copy_blob_stmt.close ();
                  }
                  catch (Exception e)
                  {
                     LOGGER.warn("Cannot close Statement !");
                  }
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
}
