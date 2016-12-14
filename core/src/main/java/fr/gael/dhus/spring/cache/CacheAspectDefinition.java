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
package fr.gael.dhus.spring.cache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@Aspect
public class CacheAspectDefinition
{
   @Autowired
   private CacheManager cacheManager;

   public CacheManager getCacheManager ()
   {
      return cacheManager;
   }

   public void setCacheManager (CacheManager cacheManager)
   {
      this.cacheManager = cacheManager;
   }

   @AfterReturning ("@annotation(fr.gael.dhus.spring.cache.IncrementCache)")
   public void updateCache (JoinPoint joinPoint)
   {
      IncrementCache annotation = ((MethodSignature) joinPoint.getSignature ())
            .getMethod ().getAnnotation (IncrementCache.class);

      Cache cache = cacheManager.getCache (annotation.name ());
      if (cache != null)
      {
         synchronized (cache)
         {
            Integer old_value = cache.get (annotation.key (), Integer.class);
            cache.clear ();
            if (old_value == null)
            {
               return;
            }
            cache.put (annotation.key (), (old_value + annotation.value ()));
         }
      }

   }
}
