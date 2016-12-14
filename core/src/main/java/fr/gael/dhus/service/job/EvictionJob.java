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
import fr.gael.dhus.datastore.eviction.EvictionManager;
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
public class EvictionJob extends AbstractJob
{
   private static final Logger LOGGER = LogManager.getLogger(EvictionJob.class);
   private static boolean running = false;
   
   @Autowired
   private EvictionManager evictionManager;

   @Autowired
   private ConfigurationManager configurationManager;
   
   @Override
   public String getCronExpression ()
   {
      return configurationManager.getEvictionCronConfiguration ()
            .getSchedule ();
   }

   @Override
   protected void executeInternal (JobExecutionContext context)
      throws JobExecutionException
   {
      if (!configurationManager.getEvictionCronConfiguration ().isActive ())
         return;
      LOGGER.info("SCHEDULER : Products eviction.");
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
            long start = System.currentTimeMillis ();            
            evictionManager.computeNextProducts ();
            evictionManager.doEvict ();
            
            LOGGER.info("SCHEDULER : Products eviction done - " + 
                     (System.currentTimeMillis ()-start) + "ms");
         }
         finally
         {
            running=false;
         }
      }
      else
      {
         LOGGER.warn("SCHEDULER : Previous products eviction " +
               "is still running (aborted).");
      }
   }
}
