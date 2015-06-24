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
package fr.gael.dhus.gwt.client.page.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.UserData;
import fr.gael.dhus.gwt.share.exceptions.UserServiceMailingException;

public class ManagementUsersPage extends AbstractPage
{
   private static UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
   private static String oldLockedValue;
   private static UserData selectedUser;

   private static TextBox username;
   private static TextBox mail;
   private static TextBox firstname;
   private static TextBox lastname;
   private static TextBox phone;
   private static TextBox address;
   private static TextBox country;
   private static ListBox domain;
   private static ListBox usage;
   private static TextBox subUsage;
   private static TextBox subDomain;
   private static SimpleCheckBox locked;
   private static TextBox lockedReason;  
   private static RootPanel createButton;
   private static RootPanel resetButton;
   private static RootPanel saveButton;
   private static RootPanel updateButton;
   private static RootPanel deleteButton;
   private static RootPanel cancelButton;
   private static SimpleCheckBox rolesCheckAll;
   
   private static State state;
   
   public ManagementUsersPage ()
   {
      // name is automatically prefixed in JS by "management_"
      super.name = "Users";
      super.roles = Arrays.asList (RoleData.USER_MANAGER);
   }

   @Override
   public native JavaScriptObject getJSInitFunction ()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.management.ManagementUsersPage::init()();
      }
   }-*/;

   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.management.ManagementUsersPage::refresh()();
      }
   }-*/;
   
   @Override
   public void load ()
   {
      // This page can only be loaded from Management Page
   }
   
   private static native void refreshRoles()
   /*-{
       $wnd.user_refreshRoles();
   }-*/;
   
   private static native void refreshUsers()
   /*-{
       $wnd.user_refreshUsers();
   }-*/;

   private static native void deselectUser()
   /*-{
       $wnd.user_deselectUser();
   }-*/;
   
   private static native void hideUsersCustomValidity()
   /*-{
       $wnd.user_hideUsersCustomValidity();
   }-*/;
   
   private static native void setUsersTableEnabled(boolean enabled)
   /*-{
       $wnd.user_setUsersTableEnabled(enabled);
   }-*/;
   
   private static native void setRolesTableEnabled(boolean enabled)
   /*-{
       $wnd.user_setRolesTableEnabled(enabled);
   }-*/;
   
   private static native void showUserManagement ()
   /*-{
      $wnd.showUserManagement(
         function ( sSource, aoData, fnCallback, oSettings ) {
            @fr.gael.dhus.gwt.client.page.management.ManagementUsersPage::getRoles(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, fnCallback)},
         function ( sSource, aoData, fnCallback, oSettings ) {   
            @fr.gael.dhus.gwt.client.page.management.ManagementUsersPage::getUsers(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, 
                oSettings.oPreviousSearch.sSearch, fnCallback)},
         function (data) {
            if (data == null) {
               @fr.gael.dhus.gwt.client.page.management.ManagementUsersPage::setNothingState(*)()
            } else {
               @fr.gael.dhus.gwt.client.page.management.ManagementUsersPage::edit(*)(data[1])
            }},
         function (authority) {
             @fr.gael.dhus.gwt.client.page.management.ManagementUsersPage::checkRole(*)(authority)
            },
         function () {
             @fr.gael.dhus.gwt.client.page.management.ManagementUsersPage::checkAllRoles(*)()
         });
   }-*/;

   private static native void selectDomain(String domain)
   /*-{
       $wnd.managementUser_selectDomain(domain);
   }-*/;

   private static native void selectUsage(String usage)
   /*-{
       $wnd.managementUser_selectUsage(usage);
   }-*/;
   
   private static native void validateSubs()
   /*-{
       $wnd.user_validateSubDomain();
       $wnd.user_validateSubUsage();
   }-*/;
      
   @Override
   public void refreshMe() 
   {
      refresh();
   }
   
   private static void refresh()
   {
      refreshUsers ();
      refreshRoles ();
      setNothingState ();
   }

   private static void init ()
   {
      showUserManagement ();
      
      username = TextBox.wrap (RootPanel.get ("managementUser_username").getElement ());
      mail = TextBox.wrap (RootPanel.get ("managementUser_mail").getElement ());
      firstname = TextBox.wrap (RootPanel.get ("managementUser_firstname").getElement ());
      lastname = TextBox.wrap (RootPanel.get ("managementUser_lastname").getElement ());
      phone = TextBox.wrap (RootPanel.get ("managementUser_phone").getElement ());
      address = TextBox.wrap (RootPanel.get ("managementUser_address").getElement ());
      country = TextBox.wrap (RootPanel.get ("managementUser_country").getElement ());
      domain = ListBox.wrap (RootPanel.get ("managementUser_domain").getElement ());
      subDomain = TextBox.wrap (RootPanel.get ("managementUser_subDomain").getElement ());
      usage = ListBox.wrap (RootPanel.get ("managementUser_usage").getElement ());
      subUsage = TextBox.wrap (RootPanel.get ("managementUser_subUsage").getElement ());
      locked = SimpleCheckBox.wrap (RootPanel.get ("managementUser_locked").getElement ());
      lockedReason = TextBox.wrap (RootPanel.get ("managementUser_lockedReason").getElement ());  
      createButton = RootPanel.get ("managementUser_buttonCreate");
      resetButton = RootPanel.get ("managementUser_buttonReset");
      saveButton = RootPanel.get ("managementUser_buttonSave");
      updateButton = RootPanel.get ("managementUser_buttonUpdate");
      deleteButton = RootPanel.get ("managementUser_buttonDelete");
      cancelButton = RootPanel.get ("managementUser_buttonCancel");
      
      locked.addClickHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            lockedReason.setEnabled (locked.getValue ());
            // enabled
            if (locked.getValue ())
            {
               lockedReason.setValue (oldLockedValue);
            }
            // disabled
            else
            {
               oldLockedValue = lockedReason.getValue ();
               lockedReason.setValue (null);
            }
         }
      });
      lockedReason.setEnabled (false);
      createButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (createButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            deselect();
            setState (State.CREATE, true);
         }
      }, ClickEvent.getType ());
      resetButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (resetButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            setState (State.CREATE, true);
         }
      }, ClickEvent.getType ());
      saveButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (saveButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            save (true);
         }
      }, ClickEvent.getType ());
      updateButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (updateButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            save (false);
         }
      }, ClickEvent.getType ());
      deleteButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (deleteButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            UserData selected = selectedUser;
            disableAll ();

            userService.deleteUser (selected.getId (),
               new AsyncCallback<Void> ()
               {

                  @Override
                  public void onFailure (Throwable caught)
                  {
                     if (caught instanceof UserServiceMailingException)
                     {
                        Window
                           .alert ("User has been deleted, there was an error while sending email to user.\n" +
                              caught.getMessage ());
                     }
                     else
                     {
                        Window.alert ("User cannot be deleted.\n " +
                           caught.getMessage ());
                     }

                     setNothingState ();
                     refreshUsers();
                  }

                  @Override
                  public void onSuccess (Void result)
                  {
                     setNothingState ();
                     refreshUsers();
                  }
               });
         }
      }, ClickEvent.getType ());
      cancelButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (cancelButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            setNothingState ();
         }
      }, ClickEvent.getType ());

      setNothingState ();
   }
   
   private static void deselect()
   {
      selectedUser = new UserData();
      oldLockedValue = null;
      rolesCheckAll.setValue (false);
      deselectUser();
   }
   
   private static void setNothingState()
   {
      if (selectedUser != null)
      {
         deselect();
      }
      setState (State.NOTHING, true);
   }
   
   private static void edit(int userId)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      userService.getUser (new Long(userId), new AsyncCallback<UserData> ()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (),
               "cursor", "default");
            Window.alert ("There was an error while getting user data.\n"+caught.getMessage ());
         }

         @Override
         public void onSuccess (UserData user)
         {
            selectedUser = user;
            setState (State.EDIT, true);
            
            DOM.setStyleAttribute (RootPanel.getBodyElement (),
               "cursor", "default");
         }         
      }); 
   }
   
   private static void save (final boolean create)
   {
      UserData toSave =
         selectedUser != null ? selectedUser.copy () : new UserData ();
                  
      toSave.setAddress (address.getValue ());
      toSave.setEmail (mail.getValue ());
      toSave.setFirstname (firstname.getValue ());
      toSave.setLastname (lastname.getValue ());
      toSave.setPhone (phone.getValue ());
      toSave.setUsername (username.getValue ());
      toSave.setLockedReason (locked.getValue()?lockedReason.getValue ():null);
      toSave.setCountry (country.getValue ());
      String domainStr = domain.getItemText (domain.getSelectedIndex ());
      toSave.setDomain (domainStr);
      toSave.setSubDomain ("other".equals (domainStr.toLowerCase ()) ? subDomain.getValue () : "unknown" );
      String usageStr = usage.getItemText (usage.getSelectedIndex ());
      toSave.setUsage (usageStr);
      toSave.setSubUsage ("other".equals (usageStr.toLowerCase ()) ? subUsage.getValue () : "unknown" );

      if (toSave.getUsername () == null ||
         toSave.getUsername ().trim ().isEmpty () ||
         toSave.getEmail () == null || toSave.getEmail ().trim ().isEmpty () || 
                  toSave.getCountry () == null ||
                  toSave.getCountry ().trim ().isEmpty () ||
                  toSave.getFirstname () == null ||
                  toSave.getFirstname ().trim ().isEmpty () ||
                  toSave.getLastname () == null ||
                  toSave.getLastname ().trim ().isEmpty ())
      {
         Window.alert ("At least one required field (*) is missing.");
         return;
      }

      disableAll ();

      AsyncCallback<Void> callback = new AsyncCallback<Void> ()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            if (caught instanceof UserServiceMailingException)
            {
               Window
                  .alert ("User has been saved, but there was an error while sending email to user.\n" +
                     caught.getMessage ());
               setNothingState ();
               return;
            }
            else
            {
               Window.alert ("User cannot be saved.\n" + caught.getMessage ());
            }

            setState (create ? State.CREATE : State.EDIT, false);
         }

         @Override
         public void onSuccess (Void result)
         {
            setNothingState ();
            refreshUsers ();
         }
      };

      if (create)
      {
         userService.createUser (toSave, callback);
      }
      else
      {
         userService.updateUser (toSave, callback);
      }
   }

   private static void setState (State s, boolean setValue)
   {
      state = s;
      boolean isNotRoot =
         selectedUser == null ||
            ! (selectedUser.getUsername ().equals ("root"));

      boolean updatable =
         isNotRoot && state == State.EDIT && selectedUser != null &&
            selectedUser.getUsername () != null &&
            !selectedUser.getUsername ().trim ().isEmpty () &&
            selectedUser.getEmail () != null &&
            !selectedUser.getEmail ().trim ().isEmpty ();

      // Datagrids
      setUsersTableEnabled (state == State.NOTHING || state == State.EDIT);
      setRolesTableEnabled ((state == State.EDIT && isNotRoot) ||
         state == State.CREATE);

      // Enable Buttons
      cancelButton.getElement ().setClassName ("button_black");
      createButton.getElement ().setClassName ("button_black");
      deleteButton.getElement ().setClassName (isNotRoot?"button_black":"button_disabled");
      resetButton.getElement ().setClassName ("button_black");
      saveButton.getElement ().setClassName ("button_disabled");
      updateButton.getElement ().setClassName (updatable?"button_black":"button_disabled");

      // Buttons Visibility
      cancelButton.setVisible (state == State.EDIT || state == State.CREATE);
      createButton.setVisible (state == State.NOTHING || state == State.EDIT);
      deleteButton.setVisible (state == State.EDIT);
      resetButton.setVisible (state == State.CREATE);
      saveButton.setVisible (state == State.CREATE);
      updateButton.setVisible (state == State.EDIT);

      // Enable Fields
      username.setEnabled (state == State.CREATE);
      firstname.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      lastname.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      mail.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      phone.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      address.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      country.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      domain.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      usage.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      subDomain.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      subUsage.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
      locked.setEnabled ( (state == State.EDIT && isNotRoot) ||
         state == State.CREATE);

      // Fields Value
      if (setValue)
      {
         username
            .setValue ( (state == State.EDIT && selectedUser != null) ? selectedUser
               .getUsername () : "");
         firstname
            .setValue ( (state == State.EDIT && selectedUser != null) ? selectedUser
               .getFirstname () : "");
         lastname
            .setValue ( (state == State.EDIT && selectedUser != null) ? selectedUser
               .getLastname () : "");
         mail
            .setValue ( (state == State.EDIT && selectedUser != null) ? selectedUser
               .getEmail () : "");
         phone
            .setValue ( (state == State.EDIT && selectedUser != null) ? selectedUser
               .getPhone () : "");
         address
            .setValue ( (state == State.EDIT && selectedUser != null) ? selectedUser
               .getAddress () : "");
         locked
            .setValue ( (state == State.EDIT && selectedUser != null) ? selectedUser
               .getLockedReason () != null : false);
         lockedReason
            .setValue ( (state == State.EDIT && selectedUser != null) ? selectedUser
               .getLockedReason () : "");
         
         country.setValue ((state == State.EDIT && selectedUser != null) ? selectedUser
            .getCountry () : "");
         
         selectDomain ((state == State.EDIT && selectedUser != null) ? selectedUser
            .getDomain () : "");
         selectUsage ((state == State.EDIT && selectedUser != null) ? selectedUser
            .getUsage () : "");
         
         
            subDomain.setValue ((state == State.EDIT && selectedUser != null && 
                     "other".equals (selectedUser.getDomain ().toLowerCase ())) ? selectedUser
               .getSubDomain () : "");
         
            subUsage.setValue ((state == State.EDIT && selectedUser != null && 
                     "other".equals (selectedUser.getUsage ().toLowerCase ())) ? selectedUser
               .getSubUsage () : "");

            validateSubs();
         refreshRoles ();
      }
      // hack to enable Save button if needed by revalidating fields.
      username.setFocus (true);
      username.setFocus (false);
      mail.setFocus (true);
      mail.setFocus (false);
      hideUsersCustomValidity();
      username.setFocus (state == State.CREATE);
   }

   private static void disableAll ()
   {
      setUsersTableEnabled (false);
      setRolesTableEnabled (false);
      rolesCheckAll.setEnabled (false);

      saveButton.getElement ().setClassName ("button_disabled");
      resetButton.getElement ().setClassName ("button_disabled");
      createButton.getElement ().setClassName ("button_disabled");
      updateButton.getElement ().setClassName ("button_disabled");
      deleteButton.getElement ().setClassName ("button_disabled");
      cancelButton.getElement ().setClassName ("button_disabled");

      username.setEnabled (false);
      firstname.setEnabled (false);
      lastname.setEnabled (false);
      mail.setEnabled (false);
      phone.setEnabled (false);
      address.setEnabled (false);
      country.setEnabled (false);
      locked.setEnabled (false);
      domain.setEnabled (false);
      usage.setEnabled (false);
      subDomain.setEnabled (false);
      subUsage.setEnabled (false);
   }

   private static void getUsers (final int start, final int length, final String search,
      final JavaScriptObject function)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      
      userService.count (search, new AsyncCallback<Integer> ()
      {

         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
               "default");
            Window.alert ("There was an error while counting users");
         }

         @Override
         public void onSuccess (final Integer total)
         {
            userService.getUsers (start, length, search,
               new AsyncCallback<List<UserData>> ()
               {
                  @Override
                  public void onFailure (Throwable caught)
                  {
                     DOM.setStyleAttribute (RootPanel.getBodyElement (),
                        "cursor", "default");
                     Window.alert ("There was an error while searching for '" +
                        search + "'");
                  }

                  @Override
                  public void onSuccess (List<UserData> users)
                  {
                     String json = "{\"aaData\": [";

                     for (UserData user : users)
                     {                        
                        json +=
                           "[\""+user.getUsername ()+"\", "+user.getId ()+"],";
                     }
                     if (users.size () >= 1)
                     {
                        json = json.substring (0, json.length () - 1);
                     }
                     json +=
                        "],\"iTotalRecords\" : " + total +
                           ", \"iTotalDisplayRecords\" : " + total + "}";

                     GWTClient.callback (function, JsonUtils.safeEval (json));
                     DOM.setStyleAttribute (RootPanel.getBodyElement (),
                        "cursor", "default");
                  }
               });
         }
      });
   }
   
   private static void checkRole(String authority)
   {
      RoleData role = RoleData.valueOf (authority);
      if (selectedUser.containsRole (role))
      {
         selectedUser.removeRole (role);
      }
      else
      {
         selectedUser.addRole (role);
      }
      refreshRoles ();
   }
   
   private static void checkAllRoles()
   {
      if (rolesCheckAll.getValue ())
      {
         selectedUser.setRoles (RoleData.getEffectiveRoles ());
      }
      else
      {
         selectedUser.setRoles (new ArrayList<RoleData>());
      }
      refreshRoles ();
   }
   
   private static void getRoles (final int start, final int length,
      final JavaScriptObject function)
   {
      boolean allChecked = true;
      String json = "{\"aaData\": [";
      int total = RoleData.getEffectiveRoles ().size ();
      for (RoleData r : RoleData.getEffectiveRoles ())
      {         
         boolean checked = (selectedUser != null && selectedUser.containsRole (r));
         allChecked = allChecked && checked;
         // authority.substring (5) : no 'ROLE_' prefix
         json += "[{\"checked\":"+checked+", \"authority\":\""+r.getAuthority ().substring (5)+"\" }, \"" + r.toString () + "\"],";
      }
      if (total >= 1)
      {
         json = json.substring (0, json.length () - 1);
      }
      json +=
         "],\"iTotalRecords\" : " + total + ", \"iTotalDisplayRecords\" : " +
            total + "}";

      GWTClient.callback (function, JsonUtils.safeEval (json));
      
      rolesCheckAll = SimpleCheckBox.wrap (RootPanel.get ("rolesCheckAll").getElement ());
      rolesCheckAll.setValue (allChecked);
      boolean isNotRoot =
         selectedUser == null ||
            ! (selectedUser.getUsername ().equals ("root"));
      rolesCheckAll.setEnabled ((state == State.EDIT && isNotRoot) ||
         state == State.CREATE);
   }

   private enum State
   {
      NOTHING, EDIT, CREATE;
   }
}
