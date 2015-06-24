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
package fr.gael.dhus.gwt.client.module;

import java.util.List;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.client.page.Page;
import fr.gael.dhus.gwt.share.UserData;

public class MenuModule
{
   private static native void clearMenu ()
   /*-{
      $wnd.clearMenu();
   }-*/;

   private static native void addPageToMenu (String page)
   /*-{
      $wnd.addPageToMenu(page, function()
         {
            @fr.gael.dhus.gwt.client.module.MenuModule::loadPage(*)(page);
         });
   }-*/;
   
   private static void addPageToMenu (AbstractPage page)
   {
      addPageToMenu(page.getName ());
   }
   
   private static void loadPage (String pageName)
   {
      Page p = Page.valueOf(pageName.toUpperCase ());
      p.load ();
   }
   
   public static void refresh()
   {
      clearMenu();
      addPageToMenu(Page.OVERVIEW.getPage ());  
      UserData user = GWTClient.getCurrentUser ();
      if (user != null && user.getRoles () != null)
      {         
         if (displayPageForUser(Page.SEARCH.getPage ().getRoles(), user.getRoles ()))
         {
            addPageToMenu(Page.SEARCH.getPage ());
         }
         if (displayPageForUser(Page.UPLOAD.getPage ().getRoles(), user.getRoles ()))
         {
            addPageToMenu(Page.UPLOAD.getPage ());
         }
         addPageToMenu(Page.PROFILE.getPage ());
         if (displayPageForUser(Page.CART.getPage ().getRoles(), user.getRoles ()))
         {
            addPageToMenu(Page.CART.getPage ());
         }
         if (displayPageForUser(Page.MANAGEMENT.getPage ().getRoles(), user.getRoles ()))
         {
            addPageToMenu(Page.MANAGEMENT.getPage ());
         }
         if (displayPageForUser(Page.STATISTICS.getPage ().getRoles(), user.getRoles ()))
         {
            addPageToMenu(Page.STATISTICS.getPage ());
         }
      }   
      addPageToMenu(Page.ABOUT.getPage ());  
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
