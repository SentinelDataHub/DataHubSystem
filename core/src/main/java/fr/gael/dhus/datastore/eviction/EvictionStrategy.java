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

import fr.gael.dhus.database.object.Eviction;
import fr.gael.dhus.database.object.Product;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            return Collections.emptySet();
         }         
      },
   LRU  ("LRU",  "Least Recent Use Algorithm") 
   {
      @Override
      public Set<Product> getProductsToEvict(Eviction eviction)
      {
         Set<Product> result;
         if (evictionManager.canEvictFromArchive (eviction))
         {
            int max = eviction.getMaxProductNumber ();
            Iterator<Product> it = evictionManager.getProductsByIngestionDate (
                  eviction.getKeepPeriod ());
            result = new LinkedHashSet<> ();
            while (result.size () < max && it.hasNext ())
            {
               Product product = it.next ();
               LOGGER.info("Product to evict \"" + product.getIdentifier () +
                     "\" - date " + product.getCreated ());
               result.add (product);
            }
         }
         else
         {
            result = Collections.emptySet ();
         }
         LOGGER.info("Found " + result.size () + " product(s) to evict.");
         return result;
      }
   },
   FIFO ("FIFO", "First In First Out Algorithm") 
   {      
      @Override
      public Set<Product> getProductsToEvict(Eviction eviction)
      {
         Set<Product> result;
         if (evictionManager.canEvictFromArchive (eviction))
         {
            int max = eviction.getMaxProductNumber ();
            Iterator<Product> it = evictionManager.getProductsByIngestionDate (
                  eviction.getKeepPeriod ());
            result = new LinkedHashSet<> ();
            while (result.size () < max && it.hasNext ())
            {
               Product product = it.next ();
               LOGGER.info("Product to evict \"" + product.getIdentifier () +
                     "\" - date " + product.getCreated ());
               result.add (product);
            }
         }
         else
         {
            result = Collections.emptySet ();
         }
         LOGGER.info("Found " + result.size () + " product(s) to evict.");
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

   
   private static final Logger LOGGER = LogManager.getLogger(EvictionStrategy.class);
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
   
   private void setEvictionManager (EvictionManager eviction_manager)
   {
      this.evictionManager = eviction_manager;
   }
   
   public abstract Set<Product> getProductsToEvict(Eviction eviction);
}
