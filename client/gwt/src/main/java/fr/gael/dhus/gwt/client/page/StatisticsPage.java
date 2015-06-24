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
package fr.gael.dhus.gwt.client.page;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.page.statistics.StatisticsConnections;
import fr.gael.dhus.gwt.client.page.statistics.StatisticsDownloads;
import fr.gael.dhus.gwt.client.page.statistics.StatisticsMonitoring;
import fr.gael.dhus.gwt.client.page.statistics.StatisticsSearches;
import fr.gael.dhus.gwt.client.page.statistics.StatisticsUploads;
import fr.gael.dhus.gwt.client.page.statistics.StatisticsUsers;

public class StatisticsPage extends AbstractPage
{
   private static StatisticsTab currentTab;

   private static enum StatisticsTab
   {
      STATISTICS_USERS(new StatisticsUsers ()),
      STATISTICS_CONNECTIONS(new StatisticsConnections ()),
      STATISTICS_SEARCHES(new StatisticsSearches ()),
      STATISTICS_DOWNLOADS(new StatisticsDownloads ()),
      STATISTICS_UPLOADS(new StatisticsUploads ()),
      STATISTICS_MONITORING (new StatisticsMonitoring ());

      private StatisticsTab (AbstractPage page)
      {
         this.page = page;
      }

      private AbstractPage page;

      public AbstractPage getPage ()
      {
         return page;
      }
   };

   @Override
   public void unload ()
   {
      super.unload ();
      for (StatisticsTab p : StatisticsTab.values ())
      {
         p.getPage ().unload ();
      }
   }

   public StatisticsPage ()
   {
      super.name = "Statistics";
      super.roles = Arrays.asList (RoleData.STATISTICS);
   }

   @Override
   public native JavaScriptObject getJSInitFunction ()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.StatisticsPage::init()();
      }
   }-*/;

   @Override
   public native JavaScriptObject getJSRefreshFunction ()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.StatisticsPage::refresh()();
      }
   }-*/;

   private static native void showStatistics ()
   /*-{
      $wnd.showStatistics();
   }-*/;

   private static void addStatisticsTab (AbstractPage page, boolean active)
   {
      addStatisticsTab (page.getName (), page.getJSInitFunction (),
         page.getJSRefreshFunction (), active);
   }

   private static void addStatisticsTab (AbstractPage page, boolean active,
      String title)
   {
      addStatisticsTab (page.getName (), page.getJSInitFunction (),
         page.getJSRefreshFunction (), active, title);
   }

   private static native void addStatisticsTab (String name,
      JavaScriptObject func, JavaScriptObject refresh, boolean active)
   /*-{
      $wnd.addStatisticsTab(name, func, refresh,
      function(page) {
         @fr.gael.dhus.gwt.client.page.StatisticsPage::select(*)(page);
      },active);
   }-*/;

   private static native void addStatisticsTab (String name,
      JavaScriptObject func, JavaScriptObject refresh, boolean active,
      String title)
   /*-{
      $wnd.addStatisticsTab(name, func, refresh, 
      function(page) {
         @fr.gael.dhus.gwt.client.page.StatisticsPage::select(*)(page);
      },active, title);
   }-*/;

   private static void select (String page)
   {
      currentTab = StatisticsTab.valueOf (page);
   }

   private static void refresh ()
   {
      currentTab.getPage ().refreshMe ();
   }

   private static void init ()
   {
      showStatistics ();
      
      addStatisticsTab (StatisticsTab.STATISTICS_USERS.getPage (),
         true, "Users");
      addStatisticsTab (StatisticsTab.STATISTICS_CONNECTIONS.getPage (),
         false, "Connections");
      addStatisticsTab (StatisticsTab.STATISTICS_SEARCHES.getPage (),
         false, "Searches");
      addStatisticsTab (StatisticsTab.STATISTICS_DOWNLOADS.getPage (),
         false, "Downloads");
      addStatisticsTab (StatisticsTab.STATISTICS_UPLOADS.getPage (),
         false, "Uploads");
      addStatisticsTab (StatisticsTab.STATISTICS_MONITORING.getPage (),
         false, "Monitoring");
      currentTab = StatisticsTab.STATISTICS_USERS;
   }

   private static boolean displayPageForUser (List<RoleData> pageRoles,
      List<RoleData> userRoles)
   {
      if (pageRoles == null) return true;
      for (RoleData role : pageRoles)
      {
         if (userRoles.contains (role))
         {
            return true;
         }
      }
      return false;
   }
}
