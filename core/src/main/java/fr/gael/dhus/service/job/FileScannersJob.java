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
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.scanner.ScannerException;
import fr.gael.dhus.datastore.scanner.ScannerFactory;
import fr.gael.dhus.service.FileScannerService;
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
public class FileScannersJob extends AbstractJob
{
   private static final Logger LOGGER = LogManager.getLogger(FileScannersJob.class);

   private static int thread_counter = 0;

   @Autowired
   private FileScannerService fs_service;

   @Autowired
   private ScannerFactory scannerFactory;

   @Autowired
   private ConfigurationManager configurationManager;

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
      LOGGER.info("SCHEDULER : Products scanners.");
      if (!DHuS.isStarted ())
      {
         LOGGER.warn("SCHEDULER : Not run while system not fully initialized.");
         return;
      }
 
      LOGGER.info("Running FileScanners Executions.");
      for (final FileScanner fs : fs_service.getActiveScanner ())
      {
         final User user = fs_service.getFileScannerOwner (fs);

         if (!fs.isActive ())
         {
            LOGGER.info(user.getUsername () + "'s fileScanner \"" +
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
                  LOGGER.info(user.getUsername () + "'s fileScanner \"" +
                           fs.getUrl () + "\" started.");
                  scannerFactory.processScan (fs.getId (), user);
               }
               catch (ScannerException e)
               {
                  LOGGER.info("Scanner \"" + user.getUsername () + "@" +
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
      LOGGER.info("SCHEDULER : Products scanners done - " +
               (System.currentTimeMillis ()-start) + "ms");
   }
}
