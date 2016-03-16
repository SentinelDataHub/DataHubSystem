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

import fr.gael.dhus.gwt.client.AccessDeniedRedirectionCallback;
import fr.gael.dhus.gwt.services.VersionServiceAsync;

public class FooterModule
{
   public static native void setFooter (String footer)
   /*-{
      $wnd.setFooter(footer);
   }-*/;
   
   public static void init()
   {
      AccessDeniedRedirectionCallback<String> callback = new AccessDeniedRedirectionCallback<String> ()
      {         
         @Override
         public void _onFailure (Throwable caught)
         {
            setFooter("&nbsp;&nbsp;&nbsp;" +
               " Created by Gael Systems - 2013 " + "&nbsp;&nbsp;&nbsp;");
         }

         @Override
         public void onSuccess (String result)
         {
            setFooter("&nbsp;&nbsp;&nbsp;" +
                  " Created by Gael Systems - 2013 " + result + 
                  "&nbsp;&nbsp;&nbsp;");
         }
      };

      VersionServiceAsync version = VersionServiceAsync.Util.getInstance ();
      version.getVersion (callback);
   }
}
