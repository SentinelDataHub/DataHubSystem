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
package fr.gael.dhus.database;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.EvictionDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Eviction;

@Component
public class DBInitializer implements InitializingBean
{
   @Autowired
   private UserDao userDao;

   @Autowired
   private EvictionDao evictionDao;

   @Override
   public void afterPropertiesSet () throws Exception
   {
      // create public data user
      userDao.getPublicData ();
            
      /*
       * Eviction "archive.eviction.maximum.product.number" can be edited
       * only via configuration file (not GUI) if user modify this configuration
       * entry it shall systematically impacts the database.
       * Other settings such as "archive.eviction.maximum.disk.usage" and
       * "archive.eviction.maximum.keep.period" can be configured via GUI.
       */
      Eviction eviction = evictionDao.getFactoryDefault ();
      Eviction database_eviction = evictionDao.getEviction ();
      if (database_eviction.getMaxProductNumber () != 
          eviction.getMaxProductNumber ())
      {
         database_eviction.setMaxProductNumber(eviction.getMaxProductNumber ());
         evictionDao.update (database_eviction);
      }
      
   }
}
