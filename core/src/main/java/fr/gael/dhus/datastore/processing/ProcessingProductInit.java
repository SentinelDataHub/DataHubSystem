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
package fr.gael.dhus.datastore.processing;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.interfaces.DaoEvent;
import fr.gael.dhus.database.dao.interfaces.DaoListener;
import fr.gael.dhus.database.object.Product;

/**
 * Initializes processes within DHuS system
 */
@Component
public class ProcessingProductInit implements InitializingBean
{
   private static Log logger = LogFactory.getLog (ProcessingProductInit.class);

   @Autowired
   private ProductDao productDao;

   @Autowired
   private ProcessingFactory processingFactory;

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
            List<Processing<Product>> processings =
               processingFactory.getProcessings ();
            Product p = element.getElement ();
            logger.debug ("Remove product \"" + p.getIdentifier () + "\"");

            for (Processing<Product> proc : processings)
            {
               try
               {
                  logger.debug ("   -> Remove processing \"" +
                     proc.getLabel () + "\"");
                  proc.removeProcessing (p);
               }
               catch (Exception e)
               {
                  logger.error (
                     "Cannot remove processing \"" + proc.getLabel () +
                        "\" for removed product \"" +
                        ProductDao.getPathFromProduct (p) + "\n", e);
               }
            }
         }

      });
      // Case of user archive reset
   }
}
