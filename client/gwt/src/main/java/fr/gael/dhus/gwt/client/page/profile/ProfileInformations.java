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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.UserData;
import fr.gael.dhus.gwt.share.exceptions.UserServiceMailingException;

public class ProfileInformations extends AbstractPage
{   
   private static UserServiceAsync userService = UserServiceAsync.Util.getInstance ();

   private static TextBox address;
   private static TextBox mail;
   private static TextBox firstname;
   private static TextBox lastname;
   private static TextBox phone;
   private static TextBox username;
   private static PasswordTextBox password;
   private static PasswordTextBox oldPassword;
   private static PasswordTextBox confirmPassword;
   private static TextBox country;
   private static ListBox domain;
   private static ListBox usage;
   private static TextBox subUsage;
   private static TextBox subDomain;
   
   private static RootPanel saveChange;
   private static RootPanel savePassword;
   
   public ProfileInformations ()
   {
      // name is automatically prefixed in JS by "profileInfos_"
      super.name = "Informations";
//      super.roles = Arrays.asList (Role.);
   }

   @Override
   public native JavaScriptObject getJSInitFunction ()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.profile.ProfileInformations::init()();
      }
   }-*/;

   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.profile.ProfileInformations::refresh()();
      }
   }-*/;
   
   private static native void showProfileInfos()
   /*-{
      $wnd.showProfileInfos(
         function ( sSource, aoData, fnCallback, oSettings ) {
            @fr.gael.dhus.gwt.client.page.profile.ProfileInformations::getRoles(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, fnCallback)});
   }-*/;

   private static native void refreshRoles()
   /*-{
       $wnd.profileInfos_refreshRoles();
   }-*/;
   
   private static native void refreshDone()
   /*-{
       $wnd.profileInfos_refreshDone();
   }-*/;

   private static native void selectDomain(String domain)
   /*-{
       $wnd.profileInfos_selectDomain(domain);
   }-*/;

   private static native void selectUsage(String usage)
   /*-{
       $wnd.profileInfos_selectUsage(usage);
   }-*/;
   
   @Override
   public void refreshMe() 
   {
      refresh();
   }
   
   private static void refresh()
   {
      refreshRoles ();
      UserData user = GWTClient.getCurrentUser ();      
      username.setEnabled (false);
      username.setValue (user.getUsername ());
      mail.setValue (user.getEmail ());
      firstname.setValue (user.getFirstname ());
      lastname.setValue (user.getLastname ());
      address.setValue (user.getAddress ());
      phone.setValue (user.getPhone ());
      country.setValue (user.getCountry ());
      
      selectDomain (user.getDomain ());
      selectUsage (user.getUsage ());
      
      if ("other".equals (user.getDomain ().toLowerCase ()))
      {
         subDomain.setValue (user.getSubDomain ());
      }
      
      if ("other".equals (user.getUsage ().toLowerCase ()))
      {
         subUsage.setValue (user.getSubUsage ());
      }
      

      refreshDone();
   }

   private static void init ()
   {
      showProfileInfos ();

      address = TextBox.wrap (RootPanel.get ("profileInfos_address").getElement ());
      mail = TextBox.wrap (RootPanel.get ("profileInfos_mail").getElement ());
      firstname = TextBox.wrap (RootPanel.get ("profileInfos_firstname").getElement ());
      lastname = TextBox.wrap (RootPanel.get ("profileInfos_lastname").getElement ());
      phone = TextBox.wrap (RootPanel.get ("profileInfos_phone").getElement ());
      username = TextBox.wrap (RootPanel.get ("profileInfos_username").getElement ());
      oldPassword = PasswordTextBox.wrap (RootPanel.get ("profileInfos_oldPassword").getElement ());
      password = PasswordTextBox.wrap (RootPanel.get ("profileInfos_password").getElement ());
      confirmPassword = PasswordTextBox.wrap (RootPanel.get ("profileInfos_password_confirm").getElement ());
      country = TextBox.wrap (RootPanel.get ("profileInfos_country").getElement ());
      domain = ListBox.wrap (RootPanel.get ("profileInfos_domain").getElement ());
      subDomain = TextBox.wrap (RootPanel.get ("profileInfos_subDomain").getElement ());
      usage = ListBox.wrap (RootPanel.get ("profileInfos_usage").getElement ());
      subUsage = TextBox.wrap (RootPanel.get ("profileInfos_subUsage").getElement ());
      
      saveChange = RootPanel.get ("profileInfos_save");
      savePassword = RootPanel.get ("profileInfos_changePassword");
      
      saveChange.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            if (saveChange.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            
            UserData toSave = GWTClient.getCurrentUser ();
            toSave.setAddress (address.getValue ());
            toSave.setPhone (phone.getValue ());
            toSave.setEmail (mail.getValue ());
            toSave.setFirstname (firstname.getValue ());
            toSave.setLastname (lastname.getValue ());
            toSave.setCountry (country.getValue ());
            String domainStr = domain.getItemText (domain.getSelectedIndex ());
            toSave.setDomain (domainStr);
            toSave.setSubDomain ("other".equals (domainStr.toLowerCase ()) ? subDomain.getValue () : "unknown" );
            String usageStr = usage.getItemText (usage.getSelectedIndex ());
            toSave.setUsage (usageStr);
            toSave.setSubUsage ("other".equals (usageStr.toLowerCase ()) ? subUsage.getValue () : "unknown" );

            if (toSave.getEmail () == null ||
               toSave.getEmail ().trim ().isEmpty ()||
               toSave.getCountry () == null ||
               toSave.getCountry ().trim ().isEmpty () ||
               toSave.getFirstname () == null ||
               toSave.getFirstname ().trim ().isEmpty () ||
               toSave.getLastname () == null ||
               toSave.getLastname ().trim ().isEmpty ())
            {
               Window
                  .alert ("At least one required field (*) is missing.");
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
                        .alert ("Your account was updated, but there was an error while sending your email.\n " +
                           "Please contact an administrator.\n" +
                           caught.getMessage ());
                  }
                  else
                  {
                     Window
                        .alert ("There was an error while updating your account.\n" +
                           caught.getMessage ());
                  }
                  enableAll (true, false);
               }

               @Override
               public void onSuccess (Void result)
               {
                  Window
                     .alert ("Your account was updated.");
                  enableAll (true, true);
                  refresh();
               }
            };

            userService.selfUpdateUser (toSave, callback);
         }
      }, ClickEvent.getType ());
      
      savePassword.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            if (savePassword.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }           
            
            if (password.getValue () == null ||
                     password.getValue ().trim ().isEmpty () || 
                     password.getValue () != confirmPassword.getValue ())
            {
               Window
                  .alert ("New password is not correctly confirmed or is empty.");
               return;
            }        
            
            if (oldPassword.getValue () == null ||
                     oldPassword.getValue ().trim ().isEmpty ())
            {
               Window
                  .alert ("Old password is empty.");
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
                        .alert ("Your password was changed, but there was an error while sending your email.\n " +
                           "Please contact an administrator.\n" +
                           caught.getMessage ());
                  }
                  else
                  {
                     Window
                        .alert ("There was an error while changing your password.\n" +
                           caught.getMessage ());
                  }
                  enableAll (true, false);
               }

               @Override
               public void onSuccess (Void result)
               {
                  Window
                     .alert ("Your password was changed.");
                  enableAll (true, true);
                  refresh();
               }
            };

            userService.selfChangePassword (GWTClient.getCurrentUser ().getId (), 
               oldPassword.getValue (), password.getValue (), callback);
         }
      }, ClickEvent.getType ());
      refresh();
   }
   
   private static void getRoles (final int start, final int length,
      final JavaScriptObject function)
   {
      UserData user = GWTClient.getCurrentUser ();
      String json = "{\"aaData\": [";
      int total = user.getRoles ().size ();
      for (RoleData r : RoleData.getEffectiveRoles ())
      {         
         if (user.containsRole (r))
         {
            json += "[\"" + r.toString () + "\"],";
         }
      }
      if (total >= 1)
      {
         json = json.substring (0, json.length () - 1);
      }
      json +=
         "],\"iTotalRecords\" : " + total + ", \"iTotalDisplayRecords\" : " +
            total + "}";

      GWTClient.callback (function, JsonUtils.safeEval (json));
   }
   
   private static void enableAll (boolean enabled, boolean reset)
   {
      firstname.setEnabled (enabled);
      lastname.setEnabled (enabled);
      mail.setEnabled (enabled);
      address.setEnabled (enabled);
      phone.setEnabled (enabled); 
      country.setEnabled (enabled);
      usage.setEnabled (enabled);
      domain.setEnabled (enabled);
      subUsage.setEnabled (enabled);
      subDomain.setEnabled (enabled);
      
      password.setEnabled (enabled); 
      if (reset) password.setValue ("");
      confirmPassword.setEnabled (enabled);
      if (reset) confirmPassword.setValue ("");
      oldPassword.setEnabled (enabled); 
      if (reset) oldPassword.setValue ("");
      
      saveChange.getElement ().setClassName (enabled?"button_black":"button_disabled");
      savePassword.getElement ().setClassName (enabled?"button_black":"button_disabled");
   }
}
