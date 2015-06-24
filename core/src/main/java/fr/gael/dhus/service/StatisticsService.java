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

import java.util.Date;
import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.ActionRecordReaderDao;
import fr.gael.dhus.service.job.JobScheduler;
import fr.gael.dhus.system.config.ConfigurationManager;

@Service
public class StatisticsService extends WebService
{
   @Autowired
   private ActionRecordReaderDao actionRecordReaderDao;
   
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
   
   public Date getNextScheduleArchiveSynchronization () throws SchedulerException
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
   
   public int getTotalUsers ()
   {
      return actionRecordReaderDao.getTotalUsers ();
   }
   
   public int getTotalDeletedUsers ()
   {
      return actionRecordReaderDao.getTotalDeletedUsers ();
   }
   
   public int getTotalRestrictedUsers ()
   {
      return actionRecordReaderDao.getTotalRestrictedUsers ();
   }
   
   public String[][] getConnectionsPerUser (Date start, Date end, List<String> users, boolean perHour)
   {      
      return actionRecordReaderDao.getConnectionsPerUser (start, end, users, perHour);
   }
   
   public String[][] getConnectionsPerUsage (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getConnectionsPerUsage (start, end, perHour);
   }
   
   public String[][] getConnectionsPerDomain (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getConnectionsPerDomain (start, end, perHour);
   }
   
   public String[][] getActiveUsersPerUsage (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getActiveUsersPerUsage (start, end, perHour);
   }
   
   public String[][] getActiveUsersPerDomain (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getActiveUsersPerDomain (start, end, perHour);
   }
   
   public String[][] getRestrictedUsers()
   {
      return actionRecordReaderDao.getRestrictedUsers ();
   }
   
   public String[][] getUsersPerUsage()
   {
      return actionRecordReaderDao.getUsersPerUsage ();
   }   
   
   public String[][] getUsersPerDomain()
   {
      return actionRecordReaderDao.getUsersPerDomain ();
   }   
   
   public int getTotalSearches ()
   {
      return actionRecordReaderDao.getTotalSearches ();
   }
   
   public String[][] getSearchesPerUser (Date start, Date end, List<String> users, boolean perHour)
   {      
      return actionRecordReaderDao.getSearchesPerUser (start, end, users, perHour);
   }
   
   public String[][] getSearchesPerUsage (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getSearchesPerUsage (start, end, perHour);
   }
   
   public String[][] getSearchesPerDomain (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getSearchesPerDomain (start, end, perHour);
   }
   
   public int getTotalDownloads ()
   {
      return actionRecordReaderDao.getTotalDownloads ();
   }
   
   public String[][] getDownloadsPerUser (Date start, Date end, List<String> users, boolean perHour)
   {      
      return actionRecordReaderDao.getDownloadsPerUser (start, end, users, perHour);
   }
   
   public String[][] getDownloadsSizePerUser (Date start, Date end, List<String> users, boolean perHour)
   {      
      return actionRecordReaderDao.getDownloadsSizePerUser (start, end, users, perHour);
   }
   
   public String[][] getDownloadsPerUsage (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getDownloadsPerUsage (start, end, perHour);
   }
   
   public String[][] getDownloadsSizePerUsage (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getDownloadsSizePerUsage (start, end, perHour);
   }
   
   public String[][] getDownloadsPerDomain (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getDownloadsPerDomain (start, end, perHour);
   }
   
   public String[][] getDownloadsSizePerDomain (Date start, Date end, boolean perHour)
   {      
      return actionRecordReaderDao.getDownloadsSizePerDomain (start, end, perHour);
   }
}
