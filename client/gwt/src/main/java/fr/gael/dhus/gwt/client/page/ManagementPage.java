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
import com.google.gwt.user.client.rpc.AsyncCallback;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage;
import fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage;
import fr.gael.dhus.gwt.client.page.management.ManagementEvictionPage;
import fr.gael.dhus.gwt.client.page.management.ManagementSystemPage;
import fr.gael.dhus.gwt.client.page.management.ManagementUsersPage;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.UserData;

public class ManagementPage extends AbstractPage
{
   private static ManagementTab currentTab;

   private static enum ManagementTab
   {
      MANAGEMENT_USERS(new ManagementUsersPage()),
      MANAGEMENT_COLLECTIONS(new ManagementCollectionsPage()),
      MANAGEMENT_DATARIGHT(new ManagementDataRightPage()),
      MANAGEMENT_SYSTEM(new ManagementSystemPage()),
      MANAGEMENT_EVICTION(new ManagementEvictionPage());
      
      private ManagementTab(AbstractPage page)
      {
         this.page = page;
      }
      
      private AbstractPage page;
      
      public AbstractPage getPage()
      {
         return page;
      }
   };
   
   @Override
   public void unload()
   {
      super.unload ();
      for (ManagementTab p : ManagementTab.values ())
      {
         p.getPage().unload();
      }
   }
   
   public ManagementPage()
   {
      super.name = "Management";
      super.roles = Arrays.asList (RoleData.USER_MANAGER, RoleData.DATA_MANAGER, RoleData.SYSTEM_MANAGER);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.ManagementPage::init()();
      }
   }-*/;

   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.ManagementPage::refresh()();
      }
   }-*/;
   
   private static native void showManagement()
   /*-{
      $wnd.showManagement();
   }-*/;
   
   private static void addManagementTab(AbstractPage page, boolean active)   
   {
      addManagementTab(page.getName (), page.getJSInitFunction (), page.getJSRefreshFunction (), active);
   }
   
   private static native void addManagementTab(String name, JavaScriptObject func, JavaScriptObject refresh, boolean active)
   /*-{
      $wnd.addManagementTab(name, func, refresh,
      function(page) {
         @fr.gael.dhus.gwt.client.page.ManagementPage::select(*)(page);
      },active);
   }-*/;
   
   private static native void addManagementTab(String name, JavaScriptObject func, JavaScriptObject refresh, boolean active, String title)
   /*-{
      $wnd.addManagementTab(name, func, refresh, 
      function(page) {
         @fr.gael.dhus.gwt.client.page.ManagementPage::select(*)(page);
      },active, title);
   }-*/;

   private static void select(String page)
   {
      currentTab = ManagementTab.valueOf (page);
   }
   
   private static void refresh()
   {
      currentTab.getPage().refreshMe();
   }
   
   private static void init()
   {
      UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
      userService.isDataPublic (new AsyncCallback<Boolean>()
      {
         
         @Override
         public void onSuccess (Boolean result)
         {
            displayMenu(result);
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
            displayMenu(false);
         }
      });      
   }
   
   private static void displayMenu(boolean dataPublic)
   {
      showManagement();
      UserData user = GWTClient.getCurrentUser ();
      boolean firstAdded = false;
      if (user != null && user.getRoles () != null)
      {         
         if (displayPageForUser(ManagementTab.MANAGEMENT_USERS.getPage().getRoles(), user.getRoles ()))
         {
            addManagementTab(ManagementTab.MANAGEMENT_USERS.getPage (), !firstAdded);
            if (!firstAdded)
            {
               currentTab = ManagementTab.MANAGEMENT_USERS;
            }
            firstAdded = true;            
         } 
         if (displayPageForUser(ManagementTab.MANAGEMENT_COLLECTIONS.getPage().getRoles(), user.getRoles ()))
         {
            addManagementTab(ManagementTab.MANAGEMENT_COLLECTIONS.getPage (), !firstAdded);
            if (!firstAdded)
            {
               currentTab = ManagementTab.MANAGEMENT_COLLECTIONS;
            }
            firstAdded = true;
         } 
         if (!dataPublic && displayPageForUser(ManagementTab.MANAGEMENT_DATARIGHT.getPage().getRoles(), user.getRoles ()))
         {
            AbstractPage page = ManagementTab.MANAGEMENT_DATARIGHT.getPage ();
            addManagementTab(page.getName (), page.getJSInitFunction (), page.getJSRefreshFunction (), !firstAdded, "Data Right Access");
            if (!firstAdded)
            {
               currentTab = ManagementTab.MANAGEMENT_DATARIGHT;
            }
            firstAdded = true;
         } 
         if (displayPageForUser(ManagementTab.MANAGEMENT_SYSTEM.getPage().getRoles(), user.getRoles ()))
         {
            addManagementTab(ManagementTab.MANAGEMENT_SYSTEM.getPage (), !firstAdded);
            if (!firstAdded)
            {
               currentTab = ManagementTab.MANAGEMENT_SYSTEM;
            }
            firstAdded = true;
         }
         if (displayPageForUser(ManagementTab.MANAGEMENT_EVICTION.getPage().getRoles(), user.getRoles ()))
         {
            addManagementTab(ManagementTab.MANAGEMENT_EVICTION.getPage (), !firstAdded);
            if (!firstAdded)
            {
               currentTab = ManagementTab.MANAGEMENT_EVICTION;
            }
            firstAdded = true;
         }
      }
   }
         
   private static boolean displayPageForUser(List<RoleData> pageRoles, List<RoleData> userRoles)
   {
      for(RoleData role : pageRoles){
          if(userRoles.contains (role)){
              return true;
          }
      }
      return false;
   }
}
