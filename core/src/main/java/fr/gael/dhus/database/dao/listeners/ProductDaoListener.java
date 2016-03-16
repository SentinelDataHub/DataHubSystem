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
package fr.gael.dhus.database.dao.listeners;

import java.io.File;
import java.io.IOException;

import fr.gael.dhus.system.config.ConfigurationManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.interfaces.DaoEvent;
import fr.gael.dhus.database.dao.interfaces.DaoListener;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.HierarchicalDirectoryBuilder;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.search.SolrDao;

/**
 * Initializes processes within DHuS system
 */
@Component
public class ProductDaoListener implements InitializingBean
{
   private static Log logger = LogFactory.getLog (ProductDaoListener.class);

   @Autowired
   private ConfigurationManager cfgManager;

   @Autowired
   private IncomingManager incomingManager;

   @Autowired
   private ProductDao productDao;
   
   @Autowired
   private SolrDao solrDao;

   @Override
   public void afterPropertiesSet () throws Exception
   {
      // When a product is removed from db, processing must also be removed
      productDao.addListener (new DaoListener<Product> ()
      {
         @Override
         public void created (DaoEvent<Product> element)
         {

         }

         @Override
         public void updated (DaoEvent<Product> element)
         {
         }

         @Override
         public void deleted (DaoEvent<Product> element)
         {            
            Product p = element.getElement ();
            logger.debug ("Removing product \"" + p.getIdentifier () + "\"");

            // TODO: later, tbd inside a service with the call of productDao.delete
            try
            {
               if (isRemovable (p))
               {
                  removeFiles (p);
               }
               solrDao.remove(p.getId());
            }
            catch (Exception e)
            {
               logger.error ("Error while removing product from path \"" +
                  p.getPath () + "\n", e);
            }
         }

      });
      // Case of user archive reset
   }

   private void removeFiles (Product product)
   {
      try
      {
         // Delete product files, only if transfered
         String prodPath = product.getPath ().toString ();
         if (!prodPath.equals (product.getOrigin ()))
         {
            prodPath = prodPath.replaceAll ("file://?", "/");
            deleteIncomingFolder (prodPath);
         }

         // Delete images
         if (product.getThumbnailFlag ())
         {
            deleteIncomingFolder (product.getThumbnailPath ());
         }

         if (product.getQuicklookFlag ())
         {
            deleteIncomingFolder (product.getQuicklookPath ());
         }

         // Delete downloadable archive
         deleteIncomingFolder (product.getDownloadablePath ());
      }
      catch (Exception e)
      {
         logger.error ("There was an error while removing processed files for"
            +" product '"+product.getIdentifier ()+"'", e);
      }
   }

   private void deleteIncomingFolder (String path) throws IOException
   {
      if (path == null)
      {
         return;
      }
      File container = new File (path);
      if (!container.exists () || !incomingManager.isInIncoming (container))
      {
         return;
      }
      if (IncomingManager.INCOMING_PRODUCT_DIR.equals (container
         .getParentFile ().getName ()))
      {
         container = container.getParentFile ();
      }
      if (HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME.equals (container
         .getParentFile ().getName ()))
      {
         container = container.getParentFile ();
      }
      if (container != null)
      {
         FileUtils.forceDelete (container);
      }
   }

   /**
    * Checks if the passed product can be removed:
    *  - if it is located in local filesystem and passed to the system by
    *  upload process. (manually adds shall be manually removed).
    *  - if the product has not been locked.
    * @param product the product to check.
    * @return
    */
   private boolean isRemovable (Product product)
   {
      String a_path = cfgManager.getArchiveConfiguration ()
            .getIncomingConfiguration ().getPath ();
      if (a_path.startsWith ("file:/")) a_path = a_path.substring (6);

      File a_file = new File (a_path);
      if (a_file.exists () && a_file.isDirectory ())
      {
         a_path = a_file.getAbsolutePath ();
         String p_file = product.getPath ().getPath ();
         return p_file.startsWith (a_path) && !product.getLocked ();
      }
      return false;
   }
}
