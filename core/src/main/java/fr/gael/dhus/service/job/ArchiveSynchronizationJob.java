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
import fr.gael.dhus.datastore.DefaultDataStore;
import fr.gael.dhus.datastore.exception.DataStoreLocalArchiveNotExistingException;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Autowired by {@link AutowiringJobFactory}
 */
@Component
public class ArchiveSynchronizationJob extends AbstractJob
{
   private static Logger logger=Logger.getLogger (
      ArchiveSynchronizationJob.class);
   private static boolean running = false;

   @Autowired
   private ConfigurationManager configurationManager;

   @Autowired
   private DefaultDataStore dataStore;
   
   @Override
   public String getCronExpression ()
   {
      return configurationManager.getArchiveSynchronizationCronConfiguration ().
         getSchedule ();
   }

   @Override
   protected void executeInternal (JobExecutionContext arg0) 
      throws JobExecutionException
   {
      if (!configurationManager.getArchiveSynchronizationCronConfiguration ().
         isActive ()) return;
      long start = System.currentTimeMillis ();
      logger.info ("SCHEDULER : Local archive synchronization.");
      if (!DHuS.isStarted ())
      {
         logger.warn("SCHEDULER : Not run while system not fully initialized.");
         return;
      }
      if (!running)
      {
         try
         {
         running=true;
         try
         {
            dataStore.processArchiveSync (false);
         }
         catch (InterruptedException e)
         {
            logger.warn ("Process stopped by the user.");
         }
         catch (DataStoreLocalArchiveNotExistingException e)
         {
            logger.warn (e.getMessage ());
         }
         logger.info ("SCHEDULER : Local archive synchronization done - " + 
                  (System.currentTimeMillis ()-start) + "ms");
         }
         finally
         {
            running=false;
         }
      }
      else
      {
         logger.warn ("SCHEDULER : Previous local archive synchronisation is still running (aborted).");
      }
   }
}
