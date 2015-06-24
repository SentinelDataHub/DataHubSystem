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
package fr.gael.dhus.gwt.client.page.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.client.page.SearchPage;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.SearchData;
import fr.gael.dhus.gwt.share.UserData;

public class ProfileSearches extends AbstractPage
{   
   private static UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
   private static RootPanel clearSearches;
   private static RootPanel searchInfos;
   
   private static HashMap<Long, SearchData> displayedSearches = new HashMap<Long, SearchData> ();
   
   public ProfileSearches ()
   {
      // name is automatically prefixed in JS by "profileSearches_"
      super.name = "Searches";
      super.roles = Arrays.asList (RoleData.SEARCH);
   }

   @Override
   public native JavaScriptObject getJSInitFunction ()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.profile.ProfileSearches::init()();
      }
   }-*/;

   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.profile.ProfileSearches::refresh()();
      }
   }-*/;
   
   private static native void showProfileSearches()
   /*-{
      $wnd.showProfileSearches(
         function ( sSource, aoData, fnCallback, oSettings ) {
            @fr.gael.dhus.gwt.client.page.profile.ProfileSearches::getSearches(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, fnCallback)},
         function ( id ) {
            @fr.gael.dhus.gwt.client.page.profile.ProfileSearches::searchIt(*)
               (id)},
         function ( id ) {
            @fr.gael.dhus.gwt.client.page.profile.ProfileSearches::deleteSearch(*)
               (id)},
         function (event, id) {
             if (event.stopPropagation) {
                 event.stopPropagation();   // W3C model
             } else {
                 event.cancelBubble = true; // IE model
             }
               @fr.gael.dhus.gwt.client.page.profile.ProfileSearches::switchSearchActivation(*)(id)
            });
   }-*/;
   
   private static native void refreshSearches()
   /*-{
       $wnd.profileSearches_refreshSearches();
   }-*/;

   @Override
   public void refreshMe() 
   {
      refresh();
   }
   
   private static void refresh()
   {
      refreshSearches ();
      setSearchInfos ();
   }

   private static void init ()
   {
      showProfileSearches ();
      
      searchInfos = RootPanel.get ("profileSearches_searchInfos");
      clearSearches = RootPanel.get ("profileSearches_clearSearches");
      clearSearches.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
            
            if (clearSearches.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            userService.clearSavedSearches (GWTClient.getCurrentUser ().getId (), new AsyncCallback<Void>()
            {               
               @Override
               public void onFailure (Throwable caught)
               {
                  DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                  "default");
                  Window.alert("There was an error while clearing your cart.");
               }
               
               @Override
               public void onSuccess (Void result)
               {            
                  DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                  "default");   
                  refreshSearches ();
               }
            });
         }
      }, ClickEvent.getType ());
      setSearchInfos ();
   }

   private static void getSearches (final int start, final int length,
      final JavaScriptObject function)
   {
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      displayedSearches.clear ();
      final UserData user = GWTClient.getCurrentUser ();
      userService.countUserSearches (user.getId (), new AsyncCallback<Integer>()
      {         
         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
            "default");
            Window.alert("There was an error while counting your saved searches.");
         }

         @Override
         public void onSuccess (final Integer total)
         {
            userService.scrollSearchesOfUser (start, length, user.getId (), new AsyncCallback<List<SearchData>>()
            {
               @Override
               public void onFailure (Throwable caught)
               {
                  Window.alert("There was an error while getting your saved searches.");
               }
      
               @Override
               public void onSuccess (List<SearchData> result)
               {
                  String json = "{\"aaData\": [";
                  for (SearchData search : result)
                  {
                     String advancedText = "";
                     Map<String, String> advanceds = search.getAdvanced ();     
                     if (advanceds != null && !advanceds.isEmpty ())
                     {
                        advancedText += "<i> (";
                        boolean first = true;
                        List<String> keys = new ArrayList<String> (advanceds.keySet ());
                        Collections.sort (keys);        
                        String lastKey = "";
                        for (String key : keys)
                        {
                           if ((lastKey+"End").equals(key))
                           {
                              advancedText += " to "+advanceds.get (key);
                           }  
                           else
                           {
                              if (key.endsWith ("End"))
                              {
                                 advancedText += (first?"":", ") + key.substring (0, key.length ()-3)+": * to "+advanceds.get (key);
                              }
                              else
                              {
                                 advancedText += (first?"":", ") + key+": "+advanceds.get (key);
                              }
                           }
                           first = false;
                           lastKey = key;
                        }
                        advancedText += ")</i>";
                     }
                     displayedSearches.put(search.getId (), search);
                     json += "[\"" + search.getValue ().replace ("\"", "\\\"") + advancedText.replace ("\"", "\\\"") + "\"," + search.getId () + "," +
                     		"{\"id\":"+search.getId ()+", \"notify\":"+search.isNotify ()+"},\"" + search.getId () + "\"],";
                  }
                  if (total >= 1)
                  {
                     json = json.substring (0, json.length () - 1);
                  }
                  json +=
                     "],\"iTotalRecords\" : " + total + ", \"iTotalDisplayRecords\" : " +
                        total + "}";       
                  clearSearches.getElement ().setClassName (total >= 1 ? "button_black":"button_disabled");                  
                  GWTClient.callback (function, JsonUtils.safeEval (json));
               }
            });
         }
      });
   }
   
   private static void deleteSearch (int id)
   {
      UserData user = GWTClient.getCurrentUser ();
      userService.removeUserSearch (user.getId (), new Long(id), new AsyncCallback<Void>()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert("There was an error while removing your saved searches.");
         }

         @Override
         public void onSuccess (Void result)
         {
            refreshSearches ();
         }
      });
   }
   
   private static void searchIt (int id)
   {
      SearchData search = displayedSearches.get (new Long(id));
      if (search == null)
      {
         return;
      }
      SearchPage.search (search);
   }
   
   private static void setSearchInfos ()
   {
      userService.getNextScheduleSearch (new AsyncCallback<Date>()
         {
            @Override
            public void onFailure (Throwable caught)
            {
               searchInfos.getElement ().setInnerText ("An active saved search means that it will be run every day.");            
            }

            @Override
            public void onSuccess (Date result)
            {
               DateTimeFormat sdf = DateTimeFormat.getFormat("EEEE dd MMMM yyyy - HH:mm:ss");
               searchInfos.getElement ().setInnerText ("An active saved search means that it will be run" +
                   " on "+sdf.format(result));    
            }         
         });
   }
   
   private static void switchSearchActivation(int id)
   {
      SearchData search = displayedSearches.get (new Long(id));
      if (search == null)
      {
         return;
      }
      userService.activateUserSearchNotification (new Long(id), !search.isNotify (), new AsyncCallback<Void>()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert("There was an error while removing your saved searches.");
         }

         @Override
         public void onSuccess (Void result)
         {
            refreshSearches ();
         }
      });
   }
}
