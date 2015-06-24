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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.UserData;
import fr.gael.dhus.gwt.share.exceptions.UserServiceMailingException;

public class RegisterPage extends AbstractPage
{
   private static boolean bot = true;
   
   public RegisterPage()
   {
      super.name = "Register";
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.RegisterPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.RegisterPage::refresh()();
      }
   }-*/;
   
   private static native void showRegister()
   /*-{
      $wnd.showRegister(function() {      
         @fr.gael.dhus.gwt.client.page.RegisterPage::notBot()();
      });
   }-*/;
   
   private static void notBot()
   {
      bot = false;
   }
   
   private static void refresh()
   {      
      Page.REGISTER.unload ();
      Page.REGISTER.load ();
   }
   
   private static void init ()
   {      
      showRegister();
      final UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
      
      final RootPanel register_button = RootPanel.get ("register_button");
      
      register_button.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            TextBox botField = TextBox.wrap (RootPanel.get ("username").getElement ());
            
            if (register_button.getElement ().getClassName ().contains ("disabled") || bot || 
                     (botField.getValue () != null && !botField.getValue ().trim ().isEmpty ()))
            {
               return;
            }

            TextBox mail = TextBox.wrap (RootPanel.get ("register_mail").getElement ());
            TextBox confirmMail = TextBox.wrap (RootPanel.get ("register_mail_confirm").getElement ());
            TextBox firstname = TextBox.wrap (RootPanel.get ("register_firstname").getElement ());
            TextBox lastname = TextBox.wrap (RootPanel.get ("register_lastname").getElement ());
            TextBox username = TextBox.wrap (RootPanel.get ("register_username").getElement ());
            PasswordTextBox password = PasswordTextBox.wrap (RootPanel.get ("register_password").getElement ());
            PasswordTextBox confirmPassword = PasswordTextBox.wrap (RootPanel.get ("register_password_confirm").getElement ());
            TextBox country = TextBox.wrap (RootPanel.get ("register_country").getElement ());
            ListBox domain = ListBox.wrap (RootPanel.get ("register_domain").getElement ());
            TextBox subDomain = TextBox.wrap (RootPanel.get ("register_subDomain").getElement ());
            ListBox usage = ListBox.wrap (RootPanel.get ("register_usage").getElement ());
            TextBox subUsage = TextBox.wrap (RootPanel.get ("register_subUsage").getElement ());
                        
            UserData toSave = new UserData ();
            toSave.setEmail (mail.getValue ());
            toSave.setFirstname (firstname.getValue ());
            toSave.setLastname (lastname.getValue ());
            toSave.setUsername (username.getValue ());
            toSave.setPassword (password.getValue ());
            toSave.setCountry (country.getValue ());
            String domainStr = domain.getItemText (domain.getSelectedIndex ());
            toSave.setDomain (domainStr);
            toSave.setSubDomain ("other".equals (domainStr.toLowerCase ()) ? subDomain.getValue () : "unknown" );
            String usageStr = usage.getItemText (usage.getSelectedIndex ());
            toSave.setUsage (usageStr);
            toSave.setSubUsage ("other".equals (usageStr.toLowerCase ()) ? subUsage.getValue () : "unknown" );

            if (toSave.getUsername () == null ||
               toSave.getUsername ().trim ().isEmpty () ||
               toSave.getEmail () == null ||
               toSave.getEmail ().trim ().isEmpty () ||
               toSave.getPassword () == null ||
               toSave.getPassword ().trim ().isEmpty () ||
               toSave.getPassword () != confirmPassword.getValue () || 
               toSave.getEmail () != confirmMail.getValue ()||
               toSave.getCountry () == null ||
               toSave.getCountry ().trim ().isEmpty () ||
               toSave.getFirstname () == null ||
               toSave.getFirstname ().trim ().isEmpty () ||
               toSave.getLastname () == null ||
               toSave.getLastname ().trim ().isEmpty ())
            {
               Window
                  .alert ("At least one field is missing, "
                     + "mail is not formatted correctly or passwords are not equals.");
               return;
            }

            enableAll (false, false);

            AsyncCallback<Void> callback = new AsyncCallback<Void> ()
            {
               @Override
               public void onFailure (Throwable caught)
               {
                  if (caught instanceof UserServiceMailingException)
                  {
                     Window
                        .alert ("Your account was created, but there was an error while sending your email.\n " +
                           "Please contact an administrator to unlock your account.\n" +
                           caught.getMessage ());
                  }
                  else
                  {
                     Window
                        .alert ("There was an error while creating your account.\n" +
                           caught.getMessage ());
                  }
                  enableAll (true, false);
               }

               @Override
               public void onSuccess (Void result)
               {
                  Window
                     .alert ("An email was sent to let you validate your registration.");
                  enableAll (true, true);

                  Page.OVERVIEW.load ();
               }
            };

            userService.createTmpUser (toSave, callback);
         }
      }, ClickEvent.getType ());
   }
   
   private static void enableAll (boolean enabled, boolean reset)
   {
      TextBox mail = TextBox.wrap (RootPanel.get ("register_mail").getElement ());
      TextBox confirmMail = TextBox.wrap (RootPanel.get ("register_mail_confirm").getElement ());
      TextBox firstname = TextBox.wrap (RootPanel.get ("register_firstname").getElement ());
      TextBox lastname = TextBox.wrap (RootPanel.get ("register_lastname").getElement ());
      TextBox username = TextBox.wrap (RootPanel.get ("register_username").getElement ());
      PasswordTextBox password = PasswordTextBox.wrap (RootPanel.get ("register_password").getElement ());
      PasswordTextBox confirmPassword = PasswordTextBox.wrap (RootPanel.get ("register_password_confirm").getElement ());
      TextBox country = TextBox.wrap (RootPanel.get ("register_country").getElement ());
      ListBox domain = ListBox.wrap (RootPanel.get ("register_domain").getElement ());
      TextBox subDomain = TextBox.wrap (RootPanel.get ("register_subDomain").getElement ());
      ListBox usage = ListBox.wrap (RootPanel.get ("register_usage").getElement ());
      TextBox subUsage = TextBox.wrap (RootPanel.get ("register_subUsage").getElement ());
      
      mail.setEnabled (enabled);
      confirmMail.setEnabled (enabled);
      firstname.setEnabled (enabled);
      lastname.setEnabled (enabled);
      username.setEnabled (enabled);
      password.setEnabled (enabled);
      confirmPassword.setEnabled (enabled);  
      country.setEnabled (enabled);
      domain.setEnabled (enabled);
      subDomain.setEnabled (enabled);
      usage.setEnabled (enabled);
      subUsage.setEnabled (enabled);
   }
}
