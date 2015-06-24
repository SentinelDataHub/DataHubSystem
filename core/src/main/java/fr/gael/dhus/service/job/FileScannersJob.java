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

import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.scanner.ScannerFactory;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Autowired by {@link AutowiringJobFactory}
 */
@Component
public class FileScannersJob extends AbstractJob
{
   private static Logger logger=Logger.getLogger(FileScannersJob.class);

   @Autowired
   private UserDao userDao;

   @Autowired
   private ScannerFactory scannerFactory;

   @Autowired
   private ConfigurationManager configurationManager;
   
   @Autowired
   TaskExecutor taskExecutor;
   
   private static int thread_counter = 0;
   
   @Override
   public String getCronExpression ()
   {
      return configurationManager.getFileScannersCronConfiguration ().
         getSchedule ();
   }

   @Override
   protected void executeInternal (JobExecutionContext arg0)
      throws JobExecutionException
   {
      long start = System.currentTimeMillis ();
      
      if (!configurationManager.getFileScannersCronConfiguration ().isActive ())
         return;
      logger.info ("SCHEDULER : Products scanners.");
      if (!DHuS.isStarted ())
      {
         logger.warn("SCHEDULER : Not run while system not fully initialized.");
         return;
      }
 
      logger.info ("Running FileScanners Executions.");
      for (final User user:userDao.readAll ())
      {
         Set<FileScanner>fscanners = userDao.getFileScanners (user);
         if ((fscanners == null) || (fscanners.size ()==0))
         {
            logger.debug ("No scanner for user \"" + 
               user.getUsername () + "\".");
            continue;
         }

         for (final FileScanner fs:fscanners)
         {
            if (!fs.isActive ())
            {
               logger.info (user.getUsername () + "'s fileScanner \"" + 
                  fs.getUrl () + "\" is disabled.");
               continue;
            }
            
            Runnable runnable = new Runnable()
            {
               
               @Override
               public void run ()
               {
                  try
                  {
                     logger.info (user.getUsername () + "'s fileScanner \"" + 
                              fs.getUrl () + "\" started.");
                     scannerFactory.processScan (fs.getId (), user);
                  }
                  catch (Exception e)
                  {
                     logger.info ("Scanner \"" + user.getUsername () + "@" + 
                        fs.getUrl () + "\" not started: " + e.getMessage ());
                  }
               }
            };
            // Asynchronously run all scanners.
            Thread thread = new Thread (runnable, "scanner-job-"+ 
               (++thread_counter));
            if (thread_counter>100) thread_counter=0;
            thread.start ();
         }
      }
      logger.info ("SCHEDULER : Products scanners done - " + 
               (System.currentTimeMillis ()-start) + "ms");
      
   }
}
