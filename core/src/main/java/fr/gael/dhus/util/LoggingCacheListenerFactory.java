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

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggingCacheListenerFactory extends CacheEventListenerFactory
{
   private static final Logger LOGGER = LogManager.getLogger(LoggingCacheListenerFactory.class);

   @Override
   public CacheEventListener createCacheEventListener(Properties properties)
   {

      return new CacheEventListener()
      {

         @Override
         public Object clone() throws CloneNotSupportedException
         {
            LOGGER.debug("Clone obtained");
            return super.clone();
         }

         @Override
         public void notifyElementRemoved(Ehcache cache, Element element)
            throws CacheException
         {
            LOGGER.debug("Element removed from the cache : " +
               element.getObjectValue());
         }

         @Override
         public void notifyElementPut(Ehcache cache, Element element)
            throws CacheException
         {
            LOGGER.debug("Element put into the cache : " + 
               element.getObjectValue());
         }

         @Override
         public void notifyElementUpdated(Ehcache cache, Element element)
            throws CacheException
         {
            LOGGER.debug("Element updated in the cache : " + 
               element.getObjectValue());
         }

         @Override
         public void notifyElementExpired(Ehcache cache, Element element)
         {
            LOGGER.debug("Element expired in the cache : " +
               element.getObjectValue());
         }

         @Override
         public void notifyElementEvicted(Ehcache cache, Element element)
         {
            LOGGER.debug("Element evicted from the cache : " +
               element.getObjectValue());
         }

         @Override
         public void notifyRemoveAll(Ehcache cache)
         {
            LOGGER.debug("Remove all elements from the cache");
         }

         @Override
         public void dispose()
         {
            LOGGER.debug("Dispose the listener");
         }
      };
   }
}
