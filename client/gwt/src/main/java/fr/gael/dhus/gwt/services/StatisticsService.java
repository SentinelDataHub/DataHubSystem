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
package fr.gael.dhus.gwt.services;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

import fr.gael.dhus.gwt.share.exceptions.AccessDeniedException;
import fr.gael.dhus.gwt.share.exceptions.StatisticsServiceException;

public interface StatisticsService extends RemoteService
{
   public Date getNextScheduleFileScanner () throws StatisticsServiceException, AccessDeniedException;
   public Date getNextScheduleSearch () throws StatisticsServiceException, AccessDeniedException;
   public Date getNextScheduleDumpDB () throws StatisticsServiceException, AccessDeniedException;
   public Date getNextScheduleCleanupDumpDB () throws StatisticsServiceException, AccessDeniedException;
   public Date getNextScheduleCleanupDB () throws StatisticsServiceException, AccessDeniedException;
   public Date getNextScheduleMailLogs () throws StatisticsServiceException, AccessDeniedException;
   public Date getNextScheduleEviction () throws StatisticsServiceException, AccessDeniedException;
   public Date getNextScheduleArchiveSynchronization () throws StatisticsServiceException, AccessDeniedException;
   public Date getNextScheduleSystemCheck () throws StatisticsServiceException, AccessDeniedException;
   
   public int getTotalUsers ();   
   public int getTotalDeletedUsers ();   
   public int getTotalRestrictedUsers ();
   public String[][] getConnectionsPerUser (Date start, Date end, List<String> users, boolean perHour);
   public String[][] getConnectionsPerDomain (Date start, Date end, boolean perHour);
   public String[][] getConnectionsPerUsage (Date start, Date end, boolean perHour);
   public String[][] getActiveUsersPerDomain (Date start, Date end, boolean perHour);
   public String[][] getActiveUsersPerUsage (Date start, Date end, boolean perHour);
   public String[][] getRestrictedUsers();   
   public String[][] getUsersPerDomain(); 
   public String[][] getUsersPerUsage();
   
   public int getTotalSearches (); 
   public String[][] getSearchesPerUser (Date start, Date end, List<String> users, boolean perHour);
   public String[][] getSearchesPerUsage (Date start, Date end, boolean perHour);  
   public String[][] getSearchesPerDomain (Date start, Date end, boolean perHour);  
   
   public int getTotalDownloads (); 
   public String[][] getDownloadsPerUser (Date start, Date end, List<String> users, boolean perHour);
   public String[][] getDownloadsPerDomain (Date start, Date end, boolean perHour);  
   public String[][] getDownloadsPerUsage (Date start, Date end, boolean perHour);  
}
