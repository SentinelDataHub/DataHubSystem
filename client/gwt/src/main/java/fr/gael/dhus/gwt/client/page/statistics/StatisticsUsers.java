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

import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.services.StatisticsServiceAsync;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.share.UserData;

public class StatisticsUsers extends AbstractPage
{  
   private static StatisticsServiceAsync statisticsService = StatisticsServiceAsync.Util.getInstance ();
   private static UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
   
   private static RootPanel totalUsers;
   private static RootPanel restrictedUsersLabel;
   private static RootPanel deletedUsers;
   
   private static List<String> selectedUsers = new ArrayList<String> ();
   
   private enum GRAPHES 
   {
      ACTIVE_USERS_PER_DOMAIN, ACTIVE_USERS_PER_USAGE, 
      RESTRICTED_USERS, USERS_PER_DOMAIN, USERS_PER_USAGE;
   }
   
   private static RootPanel restrictedUsersButton;
   private static RootPanel activeUsersPerDomainButton;
   private static RootPanel usersPerDomainButton;
   private static RootPanel activeUsersPerUsageButton;
   private static RootPanel usersPerUsageButton;

   private static SimpleRadioButton dayOption;
   private static SimpleRadioButton hourOption;
   private static RootPanel dayLabel;
   private static RootPanel hourLabel;
   
   private static GRAPHES activeGraph; 
   
   private static TextBox startDate;
   private static TextBox endDate;
   private static RootPanel refreshButton;
      
   public StatisticsUsers()
   {
      // name is automatically prefixed in JS by "statistics_"
      super.name = "Users";
      super.roles = Arrays.asList (RoleData.STATISTICS);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.statistics.StatisticsUsers::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.statistics.StatisticsUsers::refresh()();
      }
   }-*/;
   
   @Override
   public void load()
   {
      // This page can only be loaded from Statistics Page
   }
   
   private static native void showStatisticsUsers()
   /*-{
      $wnd.showStatisticsUsers(function ( sSource, aoData, fnCallback, oSettings ) {   
            @fr.gael.dhus.gwt.client.page.statistics.StatisticsUsers::getUsers(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, 
                oSettings.oPreviousSearch.sSearch, fnCallback)},
                function (user) {
            @fr.gael.dhus.gwt.client.page.statistics.StatisticsUsers::checkUser(*)(user)
            },
            function () {
            @fr.gael.dhus.gwt.client.page.statistics.StatisticsUsers::refreshScale(*)()
            });
   }-*/;
   
   private static native void setRestrictedUsersDataset(String[][] array)
   /*-{
      $wnd.setRestrictedUsersDataset(array);
   }-*/;
   
   private static native void setUsersPerDomainDataset(String[][] array)
   /*-{
      $wnd.setUsersPerDomainDataset(array);
   }-*/;
   
   private static native void setUsersPerUsageDataset(String[][] array)
   /*-{
      $wnd.setUsersPerUsageDataset(array);
   }-*/;
   
   private static native void setActiveUsersPerDomainDataset(String[][] array, String start, String end, boolean perHour)
   /*-{
      $wnd.setActiveUsersPerDomainDataset(array, start, end, perHour);
   }-*/;
   
   private static native void setActiveUsersPerUsageDataset(String[][] array, String start, String end, boolean perHour)
   /*-{
      $wnd.setActiveUsersPerUsageDataset(array, start, end, perHour);
   }-*/;
   
   private static native void refreshUsersTable()
   /*-{
      $wnd.statisticsUsers_refreshUsers();
   }-*/;
   
   @Override
   public void refreshMe() 
   {
      refresh();
   }

   private static void refresh()
   {
      refreshUsersTable ();     
      totalUsers.getElement ().setInnerText ("unknown");
      restrictedUsersLabel.getElement ().setInnerText ("unknown");
      deletedUsers.getElement ().setInnerText ("unknown");
      
      statisticsService.getTotalUsers (new AsyncCallback<Integer>()
      {         
         @Override
         public void onSuccess (Integer result)
         {
            totalUsers.getElement ().setInnerText (""+result);
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });      
      
      statisticsService.getTotalDeletedUsers (new AsyncCallback<Integer>()
      {         
         @Override
         public void onSuccess (Integer result)
         {
            deletedUsers.getElement ().setInnerText (""+result);
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });    
      
      statisticsService.getTotalRestrictedUsers (new AsyncCallback<Integer>()
      {         
         @Override
         public void onSuccess (Integer result)
         {
            restrictedUsersLabel.getElement ().setInnerText (""+result);
            restrictedUsersButton.setVisible (result>0);
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
         case ACTIVE_USERS_PER_DOMAIN:
            activeUsersPerDomainRefresh ();
         break;
         case ACTIVE_USERS_PER_USAGE:
            activeUsersPerUsageRefresh ();
         break;
         case RESTRICTED_USERS:
            restrictedUsersRefresh ();
         break;
         case USERS_PER_DOMAIN:
            usersPerDomainRefresh ();
         break;
         case USERS_PER_USAGE:
            usersPerUsageRefresh ();
         break;
         default:
         break;
      }
   }
      
   private static void activeUsersPerDomainRefresh()
   {
      DateTimeFormat dtf = DateTimeFormat.getFormat ("yyyy-MM-dd hh:mm:ss"); 
      final Date start = dtf.parse (startDate.getValue()+" 00:00:00"); // for db request, to select all start day
      Date end = dtf.parse (endDate.getValue()+" 23:59:59"); // for db request, to select all end day
      
      statisticsService.getActiveUsersPerDomain (start, end, hourOption.getValue (), new AsyncCallback<String[][]>()
      {         
         @Override
         public void onSuccess (String[][] result)
         {
            setActiveUsersPerDomainDataset (result, startDate.getValue(), endDate.getValue(), hourOption.getValue ());
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
   }
   
   private static void activeUsersPerUsageRefresh()
   {
      DateTimeFormat dtf = DateTimeFormat.getFormat ("yyyy-MM-dd hh:mm:ss"); 
      final Date start = dtf.parse (startDate.getValue()+" 00:00:00"); // for db request, to select all start day
      Date end = dtf.parse (endDate.getValue()+" 23:59:59"); // for db request, to select all end day
      
      statisticsService.getActiveUsersPerUsage (start, end, hourOption.getValue (), new AsyncCallback<String[][]>()
      {         
         @Override
         public void onSuccess (String[][] result)
         {
            setActiveUsersPerUsageDataset (result, startDate.getValue(), endDate.getValue(), hourOption.getValue ());
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
   }
      
   private static void restrictedUsersRefresh()
   {
      statisticsService.getRestrictedUsers (new AsyncCallback<String[][]>()
      {         
         @Override
         public void onSuccess (String[][] result)
         {
            setRestrictedUsersDataset (result);
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
   }
   
   private static void usersPerDomainRefresh()
   {
      statisticsService.getUsersPerDomain (new AsyncCallback<String[][]>()
      {         
         @Override
         public void onSuccess (String[][] result)
         {
            setUsersPerDomainDataset (result);
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
   }
      
   private static void usersPerUsageRefresh()
   {
      statisticsService.getUsersPerUsage (new AsyncCallback<String[][]>()
      {         
         @Override
         public void onSuccess (String[][] result)
         {
            setUsersPerUsageDataset (result);
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
         }
      });
   }
   
   private static void deselectPreviousGraph()
   {
      switch(activeGraph)
      {
         case ACTIVE_USERS_PER_DOMAIN:
            activeUsersPerDomainButton.getElement ().removeClassName ("statisticsUsers_selected");
         break;
         case ACTIVE_USERS_PER_USAGE:
            activeUsersPerUsageButton.getElement ().removeClassName ("statisticsUsers_selected");
         break;
         case RESTRICTED_USERS:
            restrictedUsersButton.getElement ().removeClassName ("statisticsUsers_selected");
         break;
         case USERS_PER_DOMAIN:
            usersPerDomainButton.getElement ().removeClassName ("statisticsUsers_selected");
         break;
         case USERS_PER_USAGE:
            usersPerUsageButton.getElement ().removeClassName ("statisticsUsers_selected");
         break;
         default:
         break;
      }
   }
   
   private static void init()
   {
      showStatisticsUsers();
      
      totalUsers = RootPanel.get ("statisticsUsers_totalUsers");
      restrictedUsersLabel = RootPanel.get ("statisticsUsers_restrictedUsers");
      deletedUsers = RootPanel.get ("statisticsUsers_deletedUsers");
     
      activeUsersPerDomainButton = RootPanel.get ("statisticsUsers_activeUsersPerDomainButton");
      activeUsersPerUsageButton = RootPanel.get ("statisticsUsers_activeUsersPerUsageButton");
      restrictedUsersButton = RootPanel.get ("statisticsUsers_restrictedUsersButton");
      usersPerDomainButton = RootPanel.get ("statisticsUsers_usersPerDomainButton");
      usersPerUsageButton = RootPanel.get ("statisticsUsers_usersPerUsageButton");
      
      dayOption = SimpleRadioButton.wrap ( RootPanel.get ("statisticsUsers_scaleDay").getElement ());
      hourOption = SimpleRadioButton.wrap ( RootPanel.get ("statisticsUsers_scaleHour").getElement ());
      
      dayLabel = RootPanel.get("statisticsUsers_scaleDayLabel");
      hourLabel = RootPanel.get("statisticsUsers_scaleHourLabel");
      
      RootPanel dayOptionBloc = RootPanel.get ("statisticsUsers_scaleDayOption"); 
      dayOptionBloc.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {         
            dayOption.setValue (true);
            refreshGraph ();
         }
      }, ClickEvent.getType ());
      RootPanel hourOptionBloc = RootPanel.get ("statisticsUsers_scaleHourOption"); 
      hourOptionBloc.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {         
            hourOption.setValue (true);
            refreshGraph ();
         }
      }, ClickEvent.getType ());
      
      activeUsersPerDomainButton.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {         
            deselectPreviousGraph();
            activeGraph = GRAPHES.ACTIVE_USERS_PER_DOMAIN;
            activeUsersPerDomainButton.getElement().addClassName ("statisticsUsers_selected");
            activeUsersPerDomainRefresh ();
         }
      }, ClickEvent.getType ());
      activeUsersPerUsageButton.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {         
            deselectPreviousGraph();
            activeGraph = GRAPHES.ACTIVE_USERS_PER_USAGE;
            activeUsersPerUsageButton.getElement().addClassName ("statisticsUsers_selected");
            activeUsersPerUsageRefresh ();
         }
      }, ClickEvent.getType ());
      restrictedUsersButton.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {         
            deselectPreviousGraph();
            activeGraph = GRAPHES.RESTRICTED_USERS;
            restrictedUsersButton.getElement().addClassName ("statisticsUsers_selected");
            restrictedUsersRefresh ();
         }
      }, ClickEvent.getType ());
      usersPerDomainButton.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {         
            deselectPreviousGraph();
            activeGraph = GRAPHES.USERS_PER_DOMAIN;
            usersPerDomainButton.getElement().addClassName ("statisticsUsers_selected");
            usersPerDomainRefresh ();
         }
      }, ClickEvent.getType ());
      usersPerUsageButton.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {         
            deselectPreviousGraph();
            activeGraph = GRAPHES.USERS_PER_USAGE;
            usersPerUsageButton.getElement().addClassName ("statisticsUsers_selected");
            usersPerUsageRefresh ();
         }
      }, ClickEvent.getType ());
      
      startDate = TextBox.wrap (RootPanel.get ("statisticsUsers_dateFieldDate").getElement ());
      endDate = TextBox.wrap (RootPanel.get ("statisticsUsers_dateFieldDateEnd").getElement ());
      
      refreshButton = RootPanel.get("statisticsUsers_refresh");
      
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
      activeGraph = GRAPHES.ACTIVE_USERS_PER_DOMAIN;
      deselectPreviousGraph();
      activeUsersPerDomainButton.getElement ().addClassName ("statisticsUsers_selected");      
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
                        json +=
                           "[{\"checked\":"+checked+", \"name\":\""+user.getUsername()+"\" }, \""+user.getUsername ()+"\"],";
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
