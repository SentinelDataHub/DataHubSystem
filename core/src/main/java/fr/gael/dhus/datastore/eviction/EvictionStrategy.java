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
package fr.gael.dhus.datastore.eviction;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

import fr.gael.dhus.database.dao.interfaces.DaoUtils;
import fr.gael.dhus.database.object.Eviction;
import fr.gael.dhus.database.object.Product;

/**
 * @author pidancier
 *
 */
public enum EvictionStrategy
{
   NONE ("NONE", "No Eviction Applied") 
   {
         @Override
         public Set<Product> getProductsToEvict(Eviction eviction)
         {
            return ImmutableSet.of();
         }         
      },
   LRU  ("LRU",  "Least Recent Use Algorithm") 
   {
      @Override
      public Set<Product> getProductsToEvict(Eviction eviction)
      {
         int skip = 0;
         int top = DaoUtils.DEFAULT_ELEMENTS_PER_PAGE;
         int max = eviction.getMaxProductNumber ();
         List<Product> products;
         Set<Product> result = new LinkedHashSet<Product> ();
         do
         {
            products = evictionManager.getProductsByIngestionDate (
               eviction.getKeepPeriod (), skip, top);
            for (Product product : products)
            {
               logger.info ("Product to evict \"" + product.getIdentifier () +
                  "\" - date " + product.getCreated ());
               if (evictionManager.canEvictFromArchive (product, eviction) &&
                        result.size () < max)
               {
                  result.add(product);
               }
            }
            skip = skip + top;
         }
         while (products.size () == top && result.size () < max);
         logger.info ("Found " + result.size () + " product(s) to evict.");
         return result;
      }
   },
   FIFO ("FIFO", "First In First Out Algorithm") 
   {      
      @Override
      public Set<Product> getProductsToEvict(Eviction eviction)
      {
         int skip = 0;
         int top = DaoUtils.DEFAULT_ELEMENTS_PER_PAGE;
         int max = eviction.getMaxProductNumber ();
         List<Product> products;
         Set<Product> result = new LinkedHashSet<Product> ();
         do
         {
            products = evictionManager.getProductsByIngestionDate (
               eviction.getKeepPeriod (), skip, top);
            for (Product product : products)
            {
               logger.info ("Product to evict \"" + product.getIdentifier () +
                  "\" - date " + product.getCreated ());
               if (evictionManager.canEvictFromArchive (product, eviction) &&
                        result.size () < max)
               {
                  result.add(product);
               }
            }
            skip = skip + top;
         }
         while (products.size () == top && result.size () < max);
         logger.info ("Found " + result.size () + " product(s) to evict.");
         return result;
      }         
   }
   
   ;
   
   /**
    * Manually propagates the EvictionManager 
    */
   @Component
   public static class EvictionManagerServiceInjector
   {
       @Autowired
       private EvictionManager evictionManager;

       @PostConstruct
       public void postConstruct()
       {
          for (EvictionStrategy em : EnumSet.allOf(EvictionStrategy.class))
              em.setEvictionManager (evictionManager);
       }
   }

   
   private static Log logger = LogFactory.getLog (EvictionStrategy.class);
   String mode;
   String description;
   
   EvictionManager evictionManager;   
   
   EvictionStrategy (String mode, String description)
   {
      this.mode = mode;
      this.description = description;
   }
   
   public String getDescription()
   {
      return this.description;
   }
   
   private void setEvictionManager (EvictionManager evictionManager)
   {
      this.evictionManager = evictionManager;
   }
   
   public abstract Set<Product> getProductsToEvict(Eviction eviction);
}
