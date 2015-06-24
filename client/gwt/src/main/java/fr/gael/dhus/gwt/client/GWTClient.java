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
package fr.gael.dhus.gwt.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import fr.gael.dhus.gwt.client.module.FooterModule;
import fr.gael.dhus.gwt.client.module.LoginModule;
import fr.gael.dhus.gwt.client.module.MenuModule;
import fr.gael.dhus.gwt.client.page.Page;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.UserData;

public class GWTClient implements EntryPoint
{
   private static UserData currentUser = null;
  
   public static native void callback (JavaScriptObject func, JavaScriptObject json)
   /*-{
      func(json);
   }-*/;

   @Override
   public void onModuleLoad ()
   {
      GWT.setUncaughtExceptionHandler (new GWT.UncaughtExceptionHandler ()
      {
         @Override
         public void onUncaughtException (Throwable e)
         {
            Throwable unwrapped = unwrap (e);
            Logger logger = Logger.getLogger ("GWTClientLogger");
            logger.log (Level.SEVERE, unwrapped.getMessage (), e);
         }

         public Throwable unwrap (Throwable e)
         {
            if (e instanceof UmbrellaException)
            {
               UmbrellaException ue = (UmbrellaException) e;
               if (ue.getCauses ().size () == 1)
               {
                  return unwrap (ue.getCauses ().iterator ().next ());
               }
            }
            return e;
         }
      });

      Scheduler.get ().scheduleDeferred (new ScheduledCommand ()
      {
         @Override
         public void execute ()
         {
            if (Window.Location.getParameterMap ().containsKey ("terms"))
            {
               startTerms ();
               return;
            }
            String code = Window.Location.getParameter("r");
            if (code == null || code.trim ().isEmpty ())
            {
               startApplication ();
            }
            else
            {
            final UserServiceAsync userService = UserServiceAsync.Util.getInstance ();           

            userService.checkUserCodeForPasswordReset (code, new AsyncCallback<Boolean>()
            {
               
               @Override
               public void onSuccess (Boolean result)
               {
                  if (!result)
                  {
                     startApplication ();
                  }
                  else
                  {
                     startResetPassword();
                  }
               }
               
               @Override
               public void onFailure (Throwable caught)
               {
                  startApplication ();
               }
            });   
            }
         }
      });
   }
   
   private static void startApplication ()
   {      
      LoginModule.load ();
      FooterModule.init ();
   }
   
   private static void startResetPassword ()
   {      
      FooterModule.init ();
      Page.RESETPASSWORD.load ();
   }
   
   private static void startTerms ()
   {      
      FooterModule.init ();
      Page.TERMS.load ();
   }
   
   public static UserData getCurrentUser()
   {
      return currentUser;
   }

   public static void loggedIn (UserData user)
   {     
      currentUser = user;
      MenuModule.refresh ();
      Page.OVERVIEW.load ();
   }

   public static void loggedOut ()
   {
      // looged out is logged in with null user.
      for (Page p : Page.values ())
      {
         p.unload();
      }
      loggedIn(null);
   }
}
