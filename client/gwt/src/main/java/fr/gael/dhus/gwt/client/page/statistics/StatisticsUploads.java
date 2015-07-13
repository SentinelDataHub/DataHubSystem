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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleRadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.services.StatisticsServiceAsync;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.UserData;

public class StatisticsUploads extends AbstractPage
{
   private static StatisticsServiceAsync statisticsService = StatisticsServiceAsync.Util.getInstance ();
   private static UserServiceAsync userService = UserServiceAsync.Util.getInstance ();

   private static RootPanel totalUploads;

   private static List<String> selectedUsers = new ArrayList<String> ();

   private enum GRAPHES
   {
      UPLOADS_PER_USER, UPLOADS_PER_DOMAIN, UPLOADS_PER_USAGE;
   }

   private static RootPanel uploadsPerUserButton;
   private static RootPanel uploadsPerDomainButton;
   private static RootPanel uploadsPerUsageButton;

   private static SimpleRadioButton dayOption;
   private static SimpleRadioButton hourOption;
   private static RootPanel dayLabel;
   private static RootPanel hourLabel;

   private static GRAPHES activeGraph;

   private static TextBox startDate;
   private static TextBox endDate;
   private static RootPanel refreshButton;

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
      $wnd.showStatisticsUploads(function ( sSource, aoData, fnCallback, oSettings ) {
            @fr.gael.dhus.gwt.client.page.statistics.StatisticsUploads::getUsers(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength,
                oSettings.oPreviousSearch.sSearch, fnCallback)},
                function (user) {
            @fr.gael.dhus.gwt.client.page.statistics.StatisticsUploads::checkUser(*)(user)
            },
            function () {
            @fr.gael.dhus.gwt.client.page.statistics.StatisticsUploads::refreshScale(*)()
            });
   }-*/;

   private static native void setUploadsPerUserDataset(String[][] array, String start, String end, boolean perHour)
   /*-{
      $wnd.setUploadsPerUserDataset(array, start, end, perHour);
   }-*/;

   private static native void setUploadsPerDomainDataset(String[][] array, String start, String end, boolean perHour)
   /*-{
      $wnd.setUploadsPerDomainDataset(array, start, end, perHour);
   }-*/;

   private static native void setUploadsPerUsageDataset(String[][] array, String start, String end, boolean perHour)
   /*-{
      $wnd.setUploadsPerUsageDataset(array, start, end, perHour);
   }-*/;

   private static native void setUploadsPerProductDataset(String[][] array, String start, String end, boolean perHour)
   /*-{
      $wnd.setUploadsPerProductDataset(array, start, end, perHour);
   }-*/;

   private static native void refreshUsersTable()
   /*-{
      $wnd.statisticsUploads_refreshUsers();
   }-*/;

   @Override
   public void refreshMe()
   {
      refresh();
   }

   private static void refresh()
   {
      refreshUsersTable ();
      totalUploads.getElement ().setInnerText ("unknown");

      statisticsService.getTotalUploads (new AsyncCallback<Integer>()
      {
         @Override
         public void onSuccess (Integer result)
         {
            totalUploads.getElement ().setInnerText (""+result);
         }

         @Override
         public void onFailure (Throwable caught)
         {
         }
      });

      // basic graph
      refreshGraph ();
   }

   @Override
   public void unload ()
   {
      if (loaded)
      {
         reset();
      }
      super.unload ();
   }

   private static void refreshGraph()
   {
      switch(activeGraph)
      {
         case UPLOADS_PER_DOMAIN:
            uploadsPerDomainRefresh ();
            break;
         case UPLOADS_PER_USAGE:
            uploadsPerUsageRefresh ();
            break;
         case UPLOADS_PER_USER:
            uploadsPerUserRefresh ();
            break;
         default:
            break;
      }
   }

   private static void uploadsPerUserRefresh()
   {
      DateTimeFormat dtf = DateTimeFormat.getFormat ("yyyy-MM-dd hh:mm:ss");
      final Date start = dtf.parse (startDate.getValue()+" 00:00:00"); // for db request, to select all start day
      Date end = dtf.parse (endDate.getValue()+" 23:59:59"); // for db request, to select all end day

      statisticsService.getUploadsPerUser(start, end, selectedUsers, hourOption.getValue(), new AsyncCallback<String[][]>() {
         @Override
         public void onSuccess(String[][] result) {
            setUploadsPerUserDataset(result, startDate.getValue(), endDate.getValue(), hourOption.getValue());
         }

         @Override
         public void onFailure(Throwable caught) {
         }
      });
   }

   private static void uploadsPerDomainRefresh()
   {
      DateTimeFormat dtf = DateTimeFormat.getFormat ("yyyy-MM-dd hh:mm:ss");
      final Date start = dtf.parse (startDate.getValue()+" 00:00:00"); // for db request, to select all start day
      Date end = dtf.parse (endDate.getValue()+" 23:59:59"); // for db request, to select all end day

      statisticsService.getUploadsPerDomain(start, end, hourOption.getValue(), new AsyncCallback<String[][]>() {
         @Override
         public void onSuccess(String[][] result) {
            setUploadsPerDomainDataset(result, startDate.getValue(), endDate.getValue(), hourOption.getValue());
         }

         @Override
         public void onFailure(Throwable caught) {
         }
      });
   }

   private static void uploadsPerUsageRefresh()
   {
      DateTimeFormat dtf = DateTimeFormat.getFormat ("yyyy-MM-dd hh:mm:ss");
      final Date start = dtf.parse (startDate.getValue()+" 00:00:00"); // for db request, to select all start day
      Date end = dtf.parse (endDate.getValue()+" 23:59:59"); // for db request, to select all end day

      statisticsService.getUploadsPerUsage(start, end, hourOption.getValue(), new AsyncCallback<String[][]>() {
         @Override
         public void onSuccess(String[][] result) {
            setUploadsPerUsageDataset(result, startDate.getValue(), endDate.getValue(), hourOption.getValue());
         }

         @Override
         public void onFailure(Throwable caught) {
         }
      });
   }

   private static void deselectPreviousGraph()
   {
      switch(activeGraph)
      {
         case UPLOADS_PER_DOMAIN:
            uploadsPerDomainButton.getElement ().removeClassName ("statisticsUploads_selected");
            break;
         case UPLOADS_PER_USAGE:
            uploadsPerUsageButton.getElement ().removeClassName ("statisticsUploads_selected");
            break;
         case UPLOADS_PER_USER:
            uploadsPerUserButton.getElement ().removeClassName ("statisticsUploads_selected");
            break;
         default:
            break;
      }
   }

   private static void init()
   {
      showStatisticsUploads();
      totalUploads = RootPanel.get ("statisticsUploads_totalUploads");

      uploadsPerUserButton = RootPanel.get ("statisticsUploads_uploadsPerUserButton");
      uploadsPerDomainButton = RootPanel.get ("statisticsUploads_uploadsPerDomainButton");
      uploadsPerUsageButton = RootPanel.get ("statisticsUploads_uploadsPerUsageButton");

      dayOption = SimpleRadioButton.wrap ( RootPanel.get ("statisticsUploads_scaleDay").getElement ());
      hourOption = SimpleRadioButton.wrap ( RootPanel.get ("statisticsUploads_scaleHour").getElement ());

      dayLabel = RootPanel.get("statisticsUploads_scaleDayLabel");
      hourLabel = RootPanel.get("statisticsUploads_scaleHourLabel");

      RootPanel dayOptionBloc = RootPanel.get ("statisticsUploads_scaleDayOption");
      dayOptionBloc.addDomHandler (new ClickHandler()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            dayOption.setValue (true);
            refreshGraph ();
         }
      }, ClickEvent.getType ());
      RootPanel hourOptionBloc = RootPanel.get ("statisticsUploads_scaleHourOption");
      hourOptionBloc.addDomHandler (new ClickHandler()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            hourOption.setValue (true);
            refreshGraph ();
         }
      }, ClickEvent.getType ());

      uploadsPerUserButton.addDomHandler (new ClickHandler()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            deselectPreviousGraph();
            activeGraph = GRAPHES.UPLOADS_PER_USER;
            uploadsPerUserButton.getElement ().addClassName ("statisticsUploads_selected");
            uploadsPerUserRefresh();
         }
      }, ClickEvent.getType ());
      uploadsPerDomainButton.addDomHandler (new ClickHandler()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            deselectPreviousGraph();
            activeGraph = GRAPHES.UPLOADS_PER_DOMAIN;
            uploadsPerDomainButton.getElement().addClassName ("statisticsUploads_selected");
            uploadsPerDomainRefresh();
         }
      }, ClickEvent.getType ());
      uploadsPerUsageButton.addDomHandler (new ClickHandler()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            deselectPreviousGraph();
            activeGraph = GRAPHES.UPLOADS_PER_USAGE;
            uploadsPerUsageButton.getElement().addClassName ("statisticsUploads_selected");
            uploadsPerUsageRefresh ();
         }
      }, ClickEvent.getType ());

      startDate = TextBox.wrap (RootPanel.get ("statisticsUploads_dateFieldDate").getElement ());
      endDate = TextBox.wrap (RootPanel.get ("statisticsUploads_dateFieldDateEnd").getElement ());

      refreshButton = RootPanel.get("statisticsUploads_refresh");

      refreshButton.addDomHandler (new ClickHandler()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            refreshGraph();
         }
      }, ClickEvent.getType ());

      reset();
      refresh();
   }


   private static void reset()
   {
      Date today = new Date();
      DateTimeFormat dtf = DateTimeFormat.getFormat ("yyyy-MM-dd");
      endDate.setValue(dtf.format (today));
      CalendarUtil.addDaysToDate (today, -30);
      startDate.setValue(dtf.format (today));
      refreshScale ();
      dayOption.setValue (true);
      activeGraph = GRAPHES.UPLOADS_PER_USER;
      deselectPreviousGraph();
      uploadsPerUserButton.getElement ().addClassName ("statisticsUploads_selected");
   }

   private static void refreshScale()
   {
      DateTimeFormat dtf = DateTimeFormat.getFormat ("yyyy-MM-dd");
      Date start = dtf.parse(startDate.getValue ());
      Date end = dtf.parse (endDate.getValue ());
      int nbDays = CalendarUtil.getDaysBetween (start, end)+1;
      dayLabel.getElement ().setInnerText ("Per day ("+nbDays+" results)");
      hourLabel.getElement().setInnerText ("Per hour ("+nbDays*24+" results)");
   }

   private static void checkUser(String user)
   {
      if (selectedUsers != null &&
              selectedUsers.contains (user))
      {
         selectedUsers.remove (user);
      }
      else
      {
         if (selectedUsers.size () >= 10)
         {
            Window.alert("Cannot select more than 10 users for statistics.");
            refreshUsersTable ();
            return;
         }
         selectedUsers.add (user);
      }
      refreshGraph();
      refreshUsersTable ();
   }

   private static void getUsers (final int start, final int length, final String search,
                                 final JavaScriptObject function)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");

      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));

      userService.countAll (search, new AsyncCallback<Integer> ()
      {

         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                    "default");
            Window.alert ("There was an error while counting users");
         }

         @Override
         public void onSuccess (final Integer total)
         {
            userService.getAllUsers (start, length, search,
                    new AsyncCallback<List<UserData>> ()
                    {
                       @Override
                       public void onFailure (Throwable caught)
                       {
                          DOM.setStyleAttribute (RootPanel.getBodyElement (),
                                  "cursor", "default");
                          Window.alert ("There was an error while searching for '" +
                                  search + "'");
                       }

                       @Override
                       public void onSuccess (List<UserData> users)
                       {
                          String json = "{\"aaData\": [";

                          for (UserData user : users)
                          {
                             boolean checked =  (selectedUsers != null && selectedUsers.contains (user.getUsername()));;
                             String name = user.getUsername();
                             if (user.isDeleted ())
                             {
                                name += " (deleted)";
                             }
                             json +=
                                     "[{\"checked\":"+checked+", \"name\":\""+user.getUsername ()+"\" }, {\"name\":\""+name+"\", \"deleted\":"+user.isDeleted ()+"}],";
                          }
                          if (users.size () >= 1)
                          {
                             json = json.substring (0, json.length () - 1);
                          }
                          json +=
                                  "],\"iTotalRecords\" : " + total +
                                          ", \"iTotalDisplayRecords\" : " + total + "}";

                          GWTClient.callback (function, JsonUtils.safeEval (json));
                          DOM.setStyleAttribute (RootPanel.getBodyElement (),
                                  "cursor", "default");
                       }
                    });
         }
      });
   }
}