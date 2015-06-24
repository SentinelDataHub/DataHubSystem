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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface StatisticsServiceAsync
{
   public void getNextScheduleFileScanner (AsyncCallback<Date> callback);
   public void getNextScheduleSearch (AsyncCallback<Date> callback);
   public void getNextScheduleDumpDB (AsyncCallback<Date> callback);
   public void getNextScheduleCleanupDumpDB (AsyncCallback<Date> callback);
   public void getNextScheduleCleanupDB (AsyncCallback<Date> callback);
   public void getNextScheduleMailLogs (AsyncCallback<Date> callback);
   public void getNextScheduleEviction (AsyncCallback<Date> callback);
   public void getNextScheduleArchiveSynchronization (AsyncCallback<Date> callback);
   public void getNextScheduleSystemCheck (AsyncCallback<Date> callback);
   
   public void getTotalUsers (AsyncCallback<Integer> callback);
   public void getTotalDeletedUsers(AsyncCallback<Integer> callback);
   public void getTotalRestrictedUsers (AsyncCallback<Integer> callback);
   public void getConnectionsPerUser(Date start, Date end, List<String> users, boolean perHour, AsyncCallback<String[][]> callback);
   public void getConnectionsPerDomain(Date start, Date end, boolean perHour, AsyncCallback<String[][]> callback);
   public void getConnectionsPerUsage(Date start, Date end, boolean perHour, AsyncCallback<String[][]> callback);
   public void getActiveUsersPerDomain(Date start, Date end, boolean perHour,AsyncCallback<String[][]> callback);
   public void getActiveUsersPerUsage(Date start, Date end, boolean perHour,AsyncCallback<String[][]> callback);
   public void getRestrictedUsers(AsyncCallback<String[][]> callback);
   public void getUsersPerDomain(AsyncCallback<String[][]> callback);
   public void getUsersPerUsage(AsyncCallback<String[][]> callback);
   
   public void getTotalSearches (AsyncCallback<Integer> callback);
   public void getSearchesPerUser(Date start, Date end, List<String> users, boolean perHour, AsyncCallback<String[][]> callback);
   public void getSearchesPerDomain(Date start, Date end, boolean perHour, AsyncCallback<String[][]> callback);
   public void getSearchesPerUsage(Date start, Date end, boolean perHour, AsyncCallback<String[][]> callback);
   
   public void getTotalDownloads (AsyncCallback<Integer> callback);
   public void getDownloadsPerUser(Date start, Date end, List<String> users, boolean perHour, AsyncCallback<String[][]> callback);
   public void getDownloadsPerDomain(Date start, Date end, boolean perHour, AsyncCallback<String[][]> callback);
   public void getDownloadsPerUsage(Date start, Date end, boolean perHour, AsyncCallback<String[][]> callback);
   
   /**
    * Utility class to get the RPC Async interface from client-side code
    */
   public static final class Util
   {
      private static StatisticsServiceAsync instance;

      public static final StatisticsServiceAsync getInstance ()
      {
         if (instance == null)
         {
            instance = (StatisticsServiceAsync) GWT.create (StatisticsService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint (GWT.getHostPageBaseURL () +
               "/statisticsService");
         }
         return instance;
      }

      private Util ()
      {
         // Utility class should not be instanciated
      }
   }

}
