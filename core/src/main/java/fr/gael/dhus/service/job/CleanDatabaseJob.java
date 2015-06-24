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

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.dao.interfaces.DaoUtils;
import fr.gael.dhus.database.object.config.cron.CleanDatabaseCronConfiguration;
import fr.gael.dhus.search.SolrDao;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Autowired by {@link AutowiringJobFactory}
 */
@Component
public class CleanDatabaseJob extends AbstractJob
{
   public CleanDatabaseJob ()
   {
   }
   private static Logger logger = Logger.getLogger (CleanDatabaseJob.class);

   @Autowired
   private UserDao userDao;
   
   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;
   
   @Autowired
   private SolrDao solrDao;

   @Autowired
   private ConfigurationManager configurationManager;
   
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
      logger.info ("SCHEDULER : Cleanup database.");

      if (!DHuS.isStarted ())
      {
         logger.warn("SCHEDULER : Not run while system not fully initialized.");
         return;
      }

      userDao.cleanupTmpUser(cleanDatabaseConf.getTempUsersConfiguration ().
         getKeepPeriod ());
      actionRecordWritterDao.cleanupOlderActionRecords (
         cleanDatabaseConf.getLogStatConfiguration ().getKeepPeriod ()); 
      
      // optimize database
      DaoUtils.optimize ();
      // Optimize search index. 
      solrDao.optimize ();
      logger.info ("SCHEDULER : Cleanup database done - " + 
         (System.currentTimeMillis ()-start) + "ms");
   }
}
