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
package fr.gael.dhus.gwt.client.page.statistics;

import java.util.Arrays;
import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.services.StatisticsServiceAsync;

public class StatisticsMonitoring extends AbstractPage
{  
   private static StatisticsServiceAsync statisticsService = StatisticsServiceAsync.Util.getInstance ();

   private static RootPanel dbDump;
   private static RootPanel dbDumpCleanup;
   private static RootPanel dbCleanup;
   private static RootPanel emailingLogs;
   private static RootPanel usersSavedSearches;
   private static RootPanel usersFileScanners;
   private static RootPanel evictionSchedule;
   private static RootPanel archiveSync;
   private static RootPanel systemCheck;
   
   
   public StatisticsMonitoring()
   {
      // name is automatically prefixed in JS by "statistics_"
      super.name = "Monitoring";
      super.roles = Arrays.asList (RoleData.STATISTICS);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.statistics.StatisticsMonitoring::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.statistics.StatisticsMonitoring::refresh()();
      }
   }-*/;
   
   @Override
   public void load()
   {
      // This page can only be loaded from Statistics Page
   }
   
   private static native void showStatisticsMonitoring()
   /*-{
      $wnd.showStatisticsMonitoring();
   }-*/;
   
   @Override
   public void refreshMe() 
   {
      refresh();
   }

   private static void refresh()
   {      
      dbCleanup.getElement ().setInnerText ("unknown");
      dbDump.getElement ().setInnerText ("unknown");
      dbDumpCleanup.getElement ().setInnerText ("unknown");
      emailingLogs.getElement ().setInnerText ("unknown");
      usersFileScanners.getElement ().setInnerText ("unknown");
      usersSavedSearches.getElement ().setInnerText ("unknown");
      evictionSchedule.getElement ().setInnerText ("unknown");
      archiveSync.getElement ().setInnerText ("unknown");
      systemCheck.getElement ().setInnerText ("unknown");
      
      final DateTimeFormat sdf = DateTimeFormat.getFormat("EEEE dd MMMM yyyy - HH:mm:ss");
      
      statisticsService.getNextScheduleCleanupDB (new AsyncCallback<Date>()
      {         
         @Override
         public void onSuccess (Date result)
         {
            dbCleanup.getElement ().setInnerText (result == null ? "disabled" : sdf.format(result));
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
      statisticsService.getNextScheduleCleanupDumpDB (new AsyncCallback<Date>()
      {         
         @Override
         public void onSuccess (Date result)
         {
            dbDumpCleanup.getElement ().setInnerText (result == null ? "disabled" : sdf.format(result));
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
      statisticsService.getNextScheduleDumpDB (new AsyncCallback<Date>()
      {         
         @Override
         public void onSuccess (Date result)
         {
            dbDump.getElement ().setInnerText (result == null ? "disabled" : sdf.format(result));
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
      statisticsService.getNextScheduleFileScanner (new AsyncCallback<Date>()
      {         
         @Override
         public void onSuccess (Date result)
         {
            usersFileScanners.getElement ().setInnerText (result == null ? "disabled" : sdf.format(result));
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
      statisticsService.getNextScheduleMailLogs (new AsyncCallback<Date>()
      {         
         @Override
         public void onSuccess (Date result)
         {
            emailingLogs.getElement ().setInnerText (result == null ? "disabled" : sdf.format(result));
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
      statisticsService.getNextScheduleSearch (new AsyncCallback<Date>()
      {         
         @Override
         public void onSuccess (Date result)
         {
            usersSavedSearches.getElement ().setInnerText (result == null ? "disabled" : sdf.format(result));
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
      statisticsService.getNextScheduleEviction (new AsyncCallback<Date>()
      {         
         @Override
         public void onSuccess (Date result)
         {
            evictionSchedule.getElement ().setInnerText (result == null ? "disabled" : sdf.format(result));
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
      statisticsService.getNextScheduleArchiveSynchronization (new AsyncCallback<Date>()
      {         
         @Override
         public void onSuccess (Date result)
         {
            archiveSync.getElement ().setInnerText (result == null ? "disabled" : sdf.format(result));
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
      statisticsService.getNextScheduleSystemCheck (new AsyncCallback<Date>()
      {         
         @Override
         public void onSuccess (Date result)
         {
            systemCheck.getElement ().setInnerText (result == null ? "disabled" : sdf.format(result));
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });

   }
   
   private static void init()
   {
      showStatisticsMonitoring();

      dbCleanup = RootPanel.get ("statisticsMonitoring_dbCleanup");
      dbDump = RootPanel.get ("statisticsMonitoring_dbDump");
      dbDumpCleanup = RootPanel.get ("statisticsMonitoring_dbDumpCleanup");
      emailingLogs = RootPanel.get ("statisticsMonitoring_emailingLogs");
      usersFileScanners = RootPanel.get ("statisticsMonitoring_usersFileScanners");
      usersSavedSearches = RootPanel.get ("statisticsMonitoring_usersSavedSearches");
      evictionSchedule = RootPanel.get ("statisticsMonitoring_evictionSchedule");
      archiveSync = RootPanel.get ("statisticsMonitoring_archiveSynchronization");
      systemCheck = RootPanel.get ("statisticsMonitoring_systemCheck");
            
      refresh();      
   }
}
