/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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

package fr.gael.dhus.datastore;

import java.util.List;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.processing.ProcessingManager;
import fr.gael.dhus.datastore.scanner.FileScannerWrapper;
import fr.gael.dhus.datastore.scanner.Scanner;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.SearchService;
import fr.gael.dhus.spring.cache.IncrementCache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Allows to perform a product ingestion.
 */
@Component
public class Ingester
{
   private static final Logger LOGGER = LogManager.getLogger(Ingester.class);

   @Autowired
   private ProductService productService;

   @Autowired
   private ProcessingManager processingManager;

   @Autowired
   private CollectionService collectionService;

   @Autowired
   private SearchService searchService;

   @IncrementCache(name = "product_count", key = "all", value = 1)
   public void ingest (Product product, User user,
         List<Collection> collections, Scanner scanner,
         FileScannerWrapper wrapper)
   {
      if (scanner != null && scanner.isStopped ())
      {
         if (wrapper != null)
         {
            wrapper.error (product,
                  new InterruptedException ("Processing stopped by the user"));
         }
         productService.systemDeleteProduct (product, Destination.NONE);
         return;
      }
      LOGGER.debug ("Add product \"" +
            ProductDao.getPathFromProduct (product) + "\".");

      try
      {
         long processing_start = System.currentTimeMillis ();

         if (wrapper != null)
         {
            wrapper.startIngestion ();
         }

         processingManager.process (product);
         productService.update (product);
         searchService.index (product);

         if (collections != null)
         {
            for (Collection c : collections)
            {
               collectionService.systemAddProduct (
                     c.getUUID (), product.getId (), true);
            }
         }

         if (wrapper != null)
         {
            wrapper.endIngestion ();
         }

         long processing_end = System.currentTimeMillis ();
         LOGGER.info ("Ingestion processing complete for product " +
               product.getPath ().toExternalForm () + " (" +
               product.getSize () + " bytes, " +
               product.getDownloadableSize () + " bytes compressed)" + " in " +
               (processing_end - processing_start) + "ms.");
      }
      catch (Throwable excp)
      {
         LOGGER.warn ("Unrecoverable error happen during ingestion of " +
               product.getPath () + " (removing from database)", excp);
         try
         {
            productService.systemDeleteProduct (product, Destination.ERROR);
         }
         catch (Exception e)
         {
            LOGGER.error (
                  "Unable to remove product after ingestion failure", e);
         }
         if (wrapper != null)
         {
            wrapper.error (product, excp);
         }
         return;
      }
   }

}
