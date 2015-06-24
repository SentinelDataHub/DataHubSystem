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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.exceptions.StatisticsServiceException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

@RPCService ("statisticsService")
public class StatisticsServiceImpl extends RemoteServiceServlet implements
   StatisticsService
{
   private static final long serialVersionUID = 3176551543265675517L;

   public Date getNextScheduleFileScanner () throws StatisticsServiceException
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      try
      {
      return statisticsService.getNextScheduleFileScanner ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new StatisticsServiceException (e.getMessage ());
      }
   }

   public Date getNextScheduleSearch () throws StatisticsServiceException
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      try
      {
      return statisticsService.getNextScheduleSearch ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new StatisticsServiceException (e.getMessage ());
      }
   }

   public Date getNextScheduleDumpDB () throws StatisticsServiceException
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      try
      {
      return statisticsService.getNextScheduleDumpDB ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new StatisticsServiceException (e.getMessage ());
      }
   }

   public Date getNextScheduleCleanupDumpDB () throws StatisticsServiceException
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      try
      {
      return statisticsService.getNextScheduleCleanupDumpDB ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new StatisticsServiceException (e.getMessage ());
      }
   }

   public Date getNextScheduleCleanupDB () throws StatisticsServiceException
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      try
      {
      return statisticsService.getNextScheduleCleanupDB ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new StatisticsServiceException (e.getMessage ());
      }
   }

   public Date getNextScheduleMailLogs () throws StatisticsServiceException
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      try
      {
      return statisticsService.getNextScheduleMailLogs ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new StatisticsServiceException (e.getMessage ());
      }
   }
   
   public Date getNextScheduleEviction () throws StatisticsServiceException
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.StatisticsService.class);

      try
      {
      return statisticsService.getNextScheduleEviction ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new StatisticsServiceException (e.getMessage ());
      }
   }
   
   public Date getNextScheduleArchiveSynchronization () throws StatisticsServiceException
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.StatisticsService.class);

      try
      {
      return statisticsService.getNextScheduleArchiveSynchronization ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new StatisticsServiceException (e.getMessage ());
      }
   }
   
   public Date getNextScheduleSystemCheck () throws StatisticsServiceException
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
                  .getBean (fr.gael.dhus.service.StatisticsService.class);

      try
      {
      return statisticsService.getNextScheduleSystemCheck ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new StatisticsServiceException (e.getMessage ());
      }
   }

   public int getTotalUsers ()
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      return statisticsService.getTotalUsers ();
   }

   public int getTotalDeletedUsers ()
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      return statisticsService.getTotalDeletedUsers ();
   }

   public int getTotalRestrictedUsers ()
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);
      
      return statisticsService.getTotalRestrictedUsers ();
   }
   
   public String[][] getConnectionsPerUser (Date start, Date end, List<String> users, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getConnectionsPerUser (start, end, users, perHour);
   }
   
   public String[][] getConnectionsPerDomain (Date start, Date end, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getConnectionsPerDomain (start, end, perHour);
   }
   
   public String[][] getConnectionsPerUsage (Date start, Date end, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getConnectionsPerUsage (start, end, perHour);
   }
   
   public String[][] getActiveUsersPerDomain (Date start, Date end, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getActiveUsersPerDomain (start, end, perHour);
   }
   
   public String[][] getActiveUsersPerUsage (Date start, Date end, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getActiveUsersPerUsage (start, end, perHour);
   }
   
   public String[][] getRestrictedUsers()
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      return statisticsService.getRestrictedUsers ();
   }
   
   public String[][] getUsersPerUsage()
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      return statisticsService.getUsersPerUsage ();      
   }
   
   public String[][] getUsersPerDomain()
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      return statisticsService.getUsersPerDomain ();      
   }

   public int getTotalSearches ()
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      return statisticsService.getTotalSearches ();
   }
   
   public String[][] getSearchesPerUser (Date start, Date end, List<String> users, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getSearchesPerUser (start, end, users, perHour);
   }
   
   public String[][] getSearchesPerDomain (Date start, Date end, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getSearchesPerDomain (start, end, perHour);
   }
   
   public String[][] getSearchesPerUsage (Date start, Date end, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getSearchesPerUsage (start, end, perHour);
   }

   public int getTotalDownloads ()
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);

      return statisticsService.getTotalDownloads ();
   }
   
   public String[][] getDownloadsPerUser (Date start, Date end, List<String> users, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getDownloadsPerUser (start, end, users, perHour);
   }
   
   public String[][] getDownloadsPerDomain (Date start, Date end, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getDownloadsPerDomain (start, end, perHour);
   }
   
   public String[][] getDownloadsPerUsage (Date start, Date end, boolean perHour)
   {
      fr.gael.dhus.service.StatisticsService statisticsService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.StatisticsService.class);      
      
      return statisticsService.getDownloadsPerUsage (start, end, perHour);
   }
}
