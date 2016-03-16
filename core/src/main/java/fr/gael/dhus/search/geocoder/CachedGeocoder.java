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

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Objects;

/**
 * A decorator for Geocoders that adds a cache.
 */
public final class CachedGeocoder implements Geocoder
{
   /** Cache. */
   private final LoadingCache<String, String> cache;

   /** Decorated geocoder. */
   private final Geocoder decorated;

   /**
    * Adds a cache to the given geocoder.
    * @param geocoder to decorate.
    */
   public CachedGeocoder(Geocoder geocoder)
   {
      Objects.requireNonNull(geocoder);
      decorated = geocoder;

      cache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(
                  new CacheLoader<String, String>()
                  {
                     @Override
                     public String load(String key)
                     {
                        return decorated.getBoundariesWKT(key);
                     }
                  }
            );
   }

   @Override
   public String getBoundariesWKT(final String address)
   {
      try
      {
         return cache.get(address);
      }
      catch (Exception e)
      {
         // Avoid: com.google.common.cache.CacheLoader$InvalidCacheLoadException
         //    : CacheLoader returned null for key gfldsgjldfk.
         return null;
      }
   }

   @Override
   public String getName()
   {
      return decorated.getName();
   }

   @Override
   public void setUrl(String url)
   {
      decorated.setUrl(url);
   }

}
