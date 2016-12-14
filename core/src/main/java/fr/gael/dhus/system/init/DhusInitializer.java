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
package fr.gael.dhus.system.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.dao.interfaces.DaoListener;
import fr.gael.dhus.database.object.User;

/**
 * Gather all the DHuS initialization required by the web services.
 *
 */
@Component
public class DhusInitializer implements InitializingBean
{
   private static final Logger LOGGER = LogManager.getLogger(DhusInitializer.class);
   
   @Autowired
   private UserDao userDao;
   
   @Autowired
   @Qualifier (value="emailToUserListener")
   private DaoListener<User> listener;
   
   @Override
   public void afterPropertiesSet () throws Exception
   {
      LOGGER.debug("Adding USER listeners.");
      userDao.addListener (listener);
   }

}
