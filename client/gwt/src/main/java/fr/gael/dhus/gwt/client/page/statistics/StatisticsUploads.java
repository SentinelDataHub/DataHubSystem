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

import com.google.gwt.core.client.JavaScriptObject;

import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.services.StatisticsServiceAsync;
import fr.gael.dhus.gwt.share.RoleData;

public class StatisticsUploads extends AbstractPage
{  
   private static StatisticsServiceAsync statisticsService = StatisticsServiceAsync.Util.getInstance ();
   
   public StatisticsUploads()
   {
      // name is automatically prefixed in JS by "statistics_"
      super.name = "Uploads";
      super.roles = Arrays.asList (RoleData.STATISTICS);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.statistics.StatisticsUploads::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.statistics.StatisticsUploads::refresh()();
      }
   }-*/;
   
   @Override
   public void load()
   {
      // This page can only be loaded from Statistics Page
   }
   
   private static native void showStatisticsUploads()
   /*-{
      $wnd.showStatisticsUploads();
   }-*/;
   
   @Override
   public void refreshMe() 
   {
      refresh();
   }

   private static void refresh()
   {
   }
   
   private static void init()
   {
      showStatisticsUploads();
      
      refresh();      
   }
}
