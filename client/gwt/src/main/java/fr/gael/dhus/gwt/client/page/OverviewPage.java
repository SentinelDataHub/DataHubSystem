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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import fr.gael.dhus.gwt.client.GWTClient;

public class OverviewPage extends AbstractPage
{
   public OverviewPage()
   {
      super.name = "Overview";
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.OverviewPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.OverviewPage::refresh()();
      }
   }-*/;
   
   private static native void showOverview()
   /*-{
      $wnd.showOverview();
   }-*/;
   
   private static void refresh()
   {
      Label registerLink = Label.wrap (RootPanel.get ("overview_registerLink").getElement ());
      registerLink.setVisible (GWTClient.getCurrentUser () == null);
      registerLink.addClickHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            Page.REGISTER.load ();
         }
      });
   }
   
   private static void init()
   {
      showOverview();
      refresh();
   }
}
