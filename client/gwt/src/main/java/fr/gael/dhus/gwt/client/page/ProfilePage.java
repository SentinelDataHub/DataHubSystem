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

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.profile.ProfileInformations;
import fr.gael.dhus.gwt.client.page.profile.ProfileSearches;
import fr.gael.dhus.gwt.client.page.profile.ProfileUploaded;
import fr.gael.dhus.gwt.share.UserData;

public class ProfilePage extends AbstractPage
{
   private static ProfileTab currentTab;

   private static enum ProfileTab
   {
      PROFILE_INFORMATIONS (new ProfileInformations ()), PROFILE_SEARCHES (
         new ProfileSearches ()), PROFILE_UPLOADED (new ProfileUploaded ());

      private ProfileTab (AbstractPage page)
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
      for (ProfileTab p : ProfileTab.values ())
      {
         p.getPage ().unload ();
      }
   }

   public ProfilePage ()
   {
      super.name = "Profile";
   }

   @Override
   public native JavaScriptObject getJSInitFunction ()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.ProfilePage::init()();
      }
   }-*/;

   @Override
   public native JavaScriptObject getJSRefreshFunction ()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.ProfilePage::refresh()();
      }
   }-*/;

   private static native void showProfile ()
   /*-{
      $wnd.showProfile();
   }-*/;

   private static void addProfileTab (AbstractPage page, boolean active)
   {
      addProfileTab (page.getName (), page.getJSInitFunction (),
         page.getJSRefreshFunction (), active);
   }

   private static void addProfileTab (AbstractPage page, boolean active,
      String title)
   {
      addProfileTab (page.getName (), page.getJSInitFunction (),
         page.getJSRefreshFunction (), active, title);
   }

   private static native void addProfileTab (String name,
      JavaScriptObject func, JavaScriptObject refresh, boolean active)
   /*-{
      $wnd.addProfileTab(name, func, refresh,
      function(page) {
         @fr.gael.dhus.gwt.client.page.ProfilePage::select(*)(page);
      },active);
   }-*/;

   private static native void addProfileTab (String name,
      JavaScriptObject func, JavaScriptObject refresh, boolean active,
      String title)
   /*-{
      $wnd.addProfileTab(name, func, refresh, 
      function(page) {
         @fr.gael.dhus.gwt.client.page.ProfilePage::select(*)(page);
      },active, title);
   }-*/;

   private static void select (String page)
   {
      currentTab = ProfileTab.valueOf (page);
   }

   private static void refresh ()
   {
      currentTab.getPage ().refreshMe ();
   }

   private static void init ()
   {
      showProfile ();

      UserData user = GWTClient.getCurrentUser ();
      boolean firstAdded = false;
      if (user != null && user.getRoles () != null)
      {
         if (user.getUsername () != "root")
         {
            addProfileTab (ProfileTab.PROFILE_INFORMATIONS.getPage (),
               !firstAdded, "My information");
            if ( !firstAdded)
            {
               currentTab = ProfileTab.PROFILE_INFORMATIONS;
            }
            firstAdded = true;
         }
         if (displayPageForUser (ProfileTab.PROFILE_SEARCHES.getPage ()
            .getRoles (), user.getRoles ()))
         {
            addProfileTab (ProfileTab.PROFILE_SEARCHES.getPage (), !firstAdded,
               "My saved searches");
            if ( !firstAdded)
            {
               currentTab = ProfileTab.PROFILE_SEARCHES;
            }
            firstAdded = true;
         }
         if (displayPageForUser (ProfileTab.PROFILE_UPLOADED.getPage ()
            .getRoles (), user.getRoles ()))
         {
            addProfileTab (ProfileTab.PROFILE_UPLOADED.getPage (), !firstAdded,
               "My uploaded products");
            if ( !firstAdded)
            {
               currentTab = ProfileTab.PROFILE_UPLOADED;
            }
            firstAdded = true;
         }
      }
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
