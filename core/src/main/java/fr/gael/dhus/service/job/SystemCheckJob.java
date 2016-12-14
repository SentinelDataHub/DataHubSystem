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

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.database.dao.interfaces.DaoUtils;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.SearchService;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Autowired by {@link AutowiringJobFactory}
 */
@Component
public class SystemCheckJob extends AbstractJob
{
   private static final Logger LOGGER = LogManager.getLogger(SystemCheckJob.class);
   private static boolean running = false; 
   
   @Autowired
   private ProductService productService;
   
   @Autowired
   private SearchService searchService;

   @Autowired
   private ConfigurationManager configurationManager;
   
   @Autowired
   private IncomingManager incomingManager;
   
   @Override
   public String getCronExpression ()
   {
      return configurationManager.getSystemCheckCronConfiguration ().
         getSchedule ();
   }

   @Override
   protected void executeInternal (JobExecutionContext arg0)
      throws JobExecutionException
   {
      if (!configurationManager.getSystemCheckCronConfiguration ().isActive ())
         return;
      LOGGER.info("SCHEDULER : Check system consistency.");
      if (!DHuS.isStarted ())
      {
         LOGGER.warn("SCHEDULER : Not run while system not fully initialized.");
         return;
      }
      if (!running)
      {
         running=true;
         try
         {   
            long time_start = System.currentTimeMillis ();
            LOGGER.info("Control of Database coherence...");
            long start = new Date ().getTime ();
            productService.checkDBProducts ();
            LOGGER.info("Control of Database coherence spent " +
                     (new Date ().getTime ()-start) + " ms");

            LOGGER.info("Control of Indexes coherence...");
            start = new Date ().getTime ();
            searchService.checkIndex();
            LOGGER.info("Control of Indexes coherence spent " +
                     (new Date ().getTime ()-start) + " ms");

            LOGGER.info("Control of incoming folder coherence...");
            start = new Date ().getTime ();
            incomingManager.checkIncomming ();
            LOGGER.info("Control of incoming folder coherence spent " +
                     (new Date ().getTime ()-start) + " ms");

            LOGGER.info("Optimizing database...");
            DaoUtils.optimize ();
            
            LOGGER.info("SCHEDULER : Check system consistency done - " +
               (System.currentTimeMillis ()-time_start) + "ms");

         }
         finally
         {
            running=false;
         }
      }
   }
}
