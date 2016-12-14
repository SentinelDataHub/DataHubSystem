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
package fr.gael.dhus.service;

import fr.gael.dhus.service.job.JobScheduler;
import fr.gael.dhus.system.config.ConfigurationManager;

import java.util.Date;

import org.quartz.SchedulerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService extends WebService
{
   @Autowired
   private ConfigurationManager cfgManager;
   
   @Autowired
   private JobScheduler jobScheduler;
   
   public Date getNextScheduleFileScanner () throws SchedulerException
   {
      if (!cfgManager.getFileScannersCronConfiguration ().isActive ())
         return null;
      return jobScheduler.getNextFileScannerJobSchedule ();
   }
   
   public Date getNextScheduleSearch () throws SchedulerException
   {
      if (!cfgManager.getSearchesCronConfiguration ().isActive ())
         return null;
      return jobScheduler.getNextSearchesJobSchedule ();
   }
   
   public Date getNextScheduleDumpDB () throws SchedulerException
   {
      if (!cfgManager.getDumpDatabaseCronConfiguration ().isActive ())
         return null;
      return jobScheduler.getNextDumpDatabaseJobSchedule ();
   }
   
   public Date getNextScheduleCleanupDumpDB () throws SchedulerException
   {
      if (!cfgManager.getCleanDatabaseDumpCronConfiguration ().isActive ())
         return null;
      return jobScheduler.getNextCleanDatabaseDumpJobSchedule ();
   }
   
   public Date getNextScheduleCleanupDB () throws SchedulerException
   {
      if (!cfgManager.getCleanDatabaseCronConfiguration ().isActive ())
         return null;
      return jobScheduler.getNextCleanDatabaseJobSchedule ();
   }
   
   public Date getNextScheduleMailLogs () throws SchedulerException
   {
      if (!cfgManager.getSendLogsCronConfiguration ().isActive ())
         return null;
      return jobScheduler.getNextSendLogsJobSchedule ();
   }
   
   public Date getNextScheduleEviction () throws SchedulerException
   {
      if (!cfgManager.getEvictionCronConfiguration ().isActive ())
         return null;
      return jobScheduler.getNextEvictionJobSchedule ();
   }
   
   public Date getNextScheduleArchiveSynchronization () throws
         SchedulerException
   {
      if (!cfgManager.getArchiveSynchronizationCronConfiguration ().isActive ())
         return null;
      return jobScheduler.getNextScheduleArchiveSynchronization ();
   }
   
   public Date getNextScheduleSystemCheck () throws SchedulerException
   {
      if (!cfgManager.getSystemCheckCronConfiguration ().isActive ())
         return null;
      return jobScheduler.getNextScheduleSystemCheck ();
   }
}
