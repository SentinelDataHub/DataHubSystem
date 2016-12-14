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
package fr.gael.dhus.datastore.scanner;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * This scanner listener aims to record retrieved products during scan only
 * if products are not already present in the database. Otherwise the product
 * is not retained.
 */
public class ScannerListener implements AsynchronousLinkedList.Listener<URLExt>
{
   private static final Logger LOGGER = LogManager.getLogger();

   private static final String LOG_PATTERN = "Product at '{}' is {}";
   private static final String WAITING_INGESTION = "waiting for ingestion.";
   private static final String INGESTED = "already ingested";
   private static final String SCHEDULED = "already scheduled";

   private final List<URL> notIngestedProductList = new LinkedList<>();

   /**
    * Checks product existence in the system by his origin, and raise the
    * appropriate message.
    *
    * @param url element containing the origin of product.
    * @return true if already present, false otherwise.
    */
   private boolean checkProductExistence(final URL url)
   {
      ProductService productService = ApplicationContextProvider.getBean(ProductService.class);

      Product p = productService.getProductByOrigin(url.toString());
      if (p == null)
      {
         LOGGER.info(LOG_PATTERN, url, WAITING_INGESTION);
         return false;
      }
      else
      {
         LOGGER.info(LOG_PATTERN, url, p.getProcessed() ? INGESTED : SCHEDULED);
         return true;
      }
   }

   /**
    * Returns the list containing URL of not ingested product by the system.
    *
    * @return a URL list.
    */
   public List<URL> newlyProducts()
   {
      return notIngestedProductList;
   }

   @Override
   public void addedElement(AsynchronousLinkedList.Event<URLExt> e)
   {
      URL url = e.getElement().getUrl();
      if (!checkProductExistence(url))
      {
         notIngestedProductList.add(url);
      }
   }

   @Override
   public void removedElement(AsynchronousLinkedList.Event<URLExt> e) {}
}
