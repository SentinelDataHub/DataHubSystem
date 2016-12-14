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
package fr.gael.dhus.service.job;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.dao.interfaces.DaoUtils;
import fr.gael.dhus.database.object.config.cron.CleanDatabaseCronConfiguration;
import fr.gael.dhus.system.config.ConfigurationManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Autowired by {@link AutowiringJobFactory}
 */
@Component
public class CleanDatabaseJob extends AbstractJob
{
   private static final Logger LOGGER = LogManager.getLogger(CleanDatabaseJob.class);

   @Autowired
   private UserDao userDao;

   @Autowired
   private ConfigurationManager configurationManager;

   public CleanDatabaseJob ()
   {
   }

   @Override
   public String getCronExpression ()
   {
      return configurationManager.getCleanDatabaseCronConfiguration ().
         getSchedule ();
   }

   @Override
   protected void executeInternal (JobExecutionContext arg0)
      throws JobExecutionException
   {
      CleanDatabaseCronConfiguration cleanDatabaseConf = 
               configurationManager.getCleanDatabaseCronConfiguration ();

      if (!cleanDatabaseConf.isActive ()) return;

      long start = System.currentTimeMillis ();
      LOGGER.info("SCHEDULER : Cleanup database.");

      if (!DHuS.isStarted ())
      {
         LOGGER.warn("SCHEDULER : Not run while system not fully initialized.");
         return;
      }

      userDao.cleanupTmpUser(cleanDatabaseConf.getTempUsersConfiguration ().
         getKeepPeriod ());

      // optimize database
      DaoUtils.optimize ();

      LOGGER.info("SCHEDULER : Cleanup database done - " +
         (System.currentTimeMillis ()-start) + "ms");
   }
}
