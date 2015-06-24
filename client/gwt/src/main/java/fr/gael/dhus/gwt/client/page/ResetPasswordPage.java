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
import fr.gael.dhus.gwt.share.exceptions.UserServiceMailingException;

public class ResetPasswordPage extends AbstractPage
{
   public ResetPasswordPage()
   {
      super.name = "ResetPassword";
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.ResetPasswordPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.ResetPasswordPage::refresh()();
      }
   }-*/;
   
   private static void refresh()
   {      
      Page.RESETPASSWORD.unload ();
      Page.RESETPASSWORD.load ();
   }
   
   private static native void showReset()
   /*-{
      $wnd.showReset();
   }-*/;
   
   private static void init ()
   {      
      final String code = com.google.gwt.user.client.Window.Location.getParameter("r");
      final UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
      
      showReset();
      
      final RootPanel resetPwd_button = RootPanel.get ("resetPwd_button");
      
      resetPwd_button.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            TextBox password = TextBox.wrap (RootPanel.get ("resetPwd_password").getElement ());
            TextBox confirmPassword = TextBox.wrap (RootPanel.get ("resetPwd_password_confirm").getElement ());
            
            if (password.getValue () == null || password.getValue ().trim ().isEmpty () ||
                     password.getValue () != confirmPassword.getValue ())
            {
               Window
                  .alert ("Password fields must be filled and equals");
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
                        .alert ("Your password was changed, but there was an error while sending your email.\n " +
                           "Please contact an administrator.\n" +
                           caught.getMessage ());
                     Window.Location.replace ("/");
                  }
                  else 
                  {
                     Window
                        .alert ("There was an error while changing your password.\n" +
                           caught.getMessage ());
                  }
                  enableAll (true);
               }

               @Override
               public void onSuccess (Void result)
               {
                  Window
                     .alert ("Your password was successfully changed.");

                  Window.Location.replace ("/");
               }
            };

            userService.resetPassword (code, password.getValue (), callback);
         }
      }, ClickEvent.getType ());
   }
   
   
   
   private static void enableAll (boolean enabled)
   {
      TextBox password = TextBox.wrap (RootPanel.get ("resetPwd_password").getElement ());
      TextBox confirmPassword = TextBox.wrap (RootPanel.get ("resetPwd_password_confirm").getElement ());
      
      password.setEnabled (enabled);
      confirmPassword.setEnabled (enabled);
   }
}
