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

package fr.gael.dhus.util;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class LoggingCacheListenerFactory extends CacheEventListenerFactory
{
   private static final Logger log =
      LoggerFactory.getLogger(LoggingCacheListenerFactory.class);

   @Override
   public CacheEventListener createCacheEventListener(Properties properties)
   {

      return new CacheEventListener()
      {

         @Override
         public Object clone() throws CloneNotSupportedException
         {
            log.debug("Clone obtained");
            return super.clone();
         }

         @Override
         public void notifyElementRemoved(Ehcache cache, Element element)
            throws CacheException
         {
            log.debug("Element removed from the cache : {}",
                      element.getObjectKey());
         }

         @Override
         public void notifyElementPut(Ehcache cache, Element element)
            throws CacheException
         {
            log.debug("Element put into the cache : {}",
                      element.getObjectKey());
         }

         @Override
         public void notifyElementUpdated(Ehcache cache, Element element)
            throws CacheException
         {
            log.debug("Element updated in the cache : {}",
                      element.getObjectKey());
         }

         @Override
         public void notifyElementExpired(Ehcache cache, Element element)
         {
            log.debug("Element expired in the cache : {}",
                      element.getObjectKey());
         }

         @Override
         public void notifyElementEvicted(Ehcache cache, Element element)
         {
            log.debug("Element evicted from the cache : {}",
                      element.getObjectKey());
         }

         @Override
         public void notifyRemoveAll(Ehcache cache)
         {
            log.debug("Remove all elements from the cache");
         }

         @Override
         public void dispose()
         {
            log.debug("Dispose the listener");
         }
      };
   }
}
