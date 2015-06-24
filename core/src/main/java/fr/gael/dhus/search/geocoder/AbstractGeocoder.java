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
package fr.gael.dhus.search.geocoder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class AbstractGeocoder implements Geocoder
{
   private static LoadingCache<String, String> cache=null;

   private LoadingCache<String, String> getResultsCache ()
   {
      if (AbstractGeocoder.cache == null)
      {
         AbstractGeocoder.cache = CacheBuilder.newBuilder()
           .concurrencyLevel(4)
           .maximumSize(1000)
           .expireAfterWrite(10, TimeUnit.MINUTES)
           .expireAfterAccess(10, TimeUnit.MINUTES)
           .build
           (
               new CacheLoader<String, String>() 
               {
                  public String load(String key) 
                  {
                     return getBoundariesWKT (key);
                  }
               }
            );
         }
      return AbstractGeocoder.cache;
   }
   
   /**
    * Computes boundaries.
    */
   public abstract String getBoundariesWKT(final String address);
   
   /**
    * Returns cached JTS boundaries.
    * @return
    * @throws ExecutionException 
    */
   public String getCachedBoundariesWKT (final String address) throws ExecutionException
   {
      try
      {
         return getResultsCache ().get(address);
      }
      catch (Exception e)
      {
         // Avoid: com.google.common.cache.CacheLoader$InvalidCacheLoadException
         //    : CacheLoader returned null for key gfldsgjldfk.
         return null;
      }
   }
}
