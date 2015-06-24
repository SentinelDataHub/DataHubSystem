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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.UserData;
import fr.gael.dhus.gwt.share.exceptions.UserServiceMailingException;
import fr.gael.dhus.gwt.share.exceptions.UserServiceNotExistingException;

public class ForgotPage extends AbstractPage
{
   public ForgotPage()
   {
      super.name = "Forgot";
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.ForgotPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.ForgotPage::refresh()();
      }
   }-*/;
   
   private static native void showForgot()
   /*-{
      $wnd.showForgot();
   }-*/;
      
   private static void refresh()
   {      
      Page.FORGOT.unload ();
      Page.FORGOT.load ();
   }
   
   private static void init ()
   {      
      showForgot();
      final UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
      
      final RootPanel forgot_button = RootPanel.get ("forgot_button");
      
      forgot_button.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            if (forgot_button.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            
            TextBox mail = TextBox.wrap (RootPanel.get ("forgot_mail").getElement ());
            TextBox confirmMail = TextBox.wrap (RootPanel.get ("forgot_mail_confirm").getElement ());
            TextBox username = TextBox.wrap (RootPanel.get ("forgot_username").getElement ());
                        
            UserData user = new UserData ();
            user.setEmail (mail.getValue ());
            user.setUsername (username.getValue ());

            if (user.getUsername () == null ||
               user.getUsername ().trim ().isEmpty () ||
               user.getEmail () == null ||
               user.getEmail ().trim ().isEmpty () ||
               user.getEmail () != confirmMail.getValue ())
            {
               Window
                  .alert ("At least one required field (*) is missing or"
                     + "mail is not formatted correctly.");
               return;
            }

            enableAll (false);

            AsyncCallback<Void> callback = new AsyncCallback<Void> ()
            {
               @Override
               public void onFailure (Throwable caught)
               {
                  if (caught instanceof UserServiceMailingException)
                  {
                     Window
                        .alert ("Your account was found, but there was an error while sending your email.\n " +
                           "Please contact an administrator.\n" +
                           caught.getMessage ());
                  }
                  else if (caught instanceof UserServiceNotExistingException)
                  {
                     Window
                        .alert ("No user can be found for this " +
                        "username/mail combination");
                  }
                  else
                  {
                     Window
                        .alert ("There was an error while retrieving your account.\n" +
                           caught.getMessage ());
                  }
                  enableAll (true);
               }

               @Override
               public void onSuccess (Void result)
               {
                  Window
                     .alert ("An email was sent to you with your password.");
                  enableAll (true);

                  Page.OVERVIEW.load ();
               }
            };

            userService.forgotPassword (user, callback);
         }
      }, ClickEvent.getType ());
   }
   
   private static void enableAll (boolean enabled)
   {
      TextBox mail = TextBox.wrap (RootPanel.get ("forgot_mail").getElement ());
      TextBox confirmMail = TextBox.wrap (RootPanel.get ("forgot_mail_confirm").getElement ());
      TextBox username = TextBox.wrap (RootPanel.get ("forgot_username").getElement ());
      
      mail.setEnabled (enabled);
      confirmMail.setEnabled (enabled);
      username.setEnabled (enabled);
   }
}
