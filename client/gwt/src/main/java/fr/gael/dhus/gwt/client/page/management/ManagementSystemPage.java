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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.services.ArchiveServiceAsync;
import fr.gael.dhus.gwt.services.SystemServiceAsync;
import fr.gael.dhus.gwt.share.ConfigurationData;

public class ManagementSystemPage extends AbstractPage
{
   private static SystemServiceAsync systemService = SystemServiceAsync.Util.getInstance ();
   private static ArchiveServiceAsync archiveService = ArchiveServiceAsync.Util.getInstance ();

   private static AsyncCallback<ConfigurationData> systemSettingsCallback;
   private static AsyncCallback<List<Date>> getDumpDatabaseListCallback;

   private static List<Date> dumpDates;

   private static TextBox smtpBox;
   private static TextBox portBox;
   private static SimpleCheckBox tlsBox;
   private static TextBox username;
   private static TextBox password;
   private static TextBox fromName;
   private static TextBox fromMail;
   private static TextBox replyTo;
   private static TextBox supportMail;
   private static TextBox supportName;
   private static SimpleCheckBox mailDelete;
   private static SimpleCheckBox mailCreate;
   private static SimpleCheckBox mailUpdate;
   private static RootPanel synchronizeLocalArchive;
   private static RootPanel resetDefault;
   private static RootPanel saveModifications;
   private static TextBox rootOldPassword;
   private static TextBox rootPassword;
   private static TextBox root2Password;
   private static RootPanel saveRootPassword;
   private static ListBox dumpBox;
   private static RootPanel restoreButton;
        
   public ManagementSystemPage()
   {
      // name is automatically prefixed in JS by "management_"
      super.name = "System";      
      super.roles = Arrays.asList (RoleData.SYSTEM_MANAGER);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.management.ManagementSystemPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.management.ManagementSystemPage::refresh()();
      }
   }-*/;
   
   @Override
   public void load()
   {
      // This page can only be loaded from Management Page
   }
   
   private static native void showSystemManagement()
   /*-{
      $wnd.showSystemManagement();
   }-*/;
   
   private static native void reCheckAll()
   /*-{
      $wnd.system_reCheckAll();
   }-*/;
   
   @Override
   public void refreshMe() 
   {
      refresh();
   }

   private static void refresh()
   {
      systemService.getConfiguration (systemSettingsCallback);
      systemService.getDumpDatabaseList (getDumpDatabaseListCallback);
   }
   
   private static void init()
   {
      showSystemManagement();
      
      smtpBox = TextBox.wrap (RootPanel.get ("managementSystem_server").getElement ());
      portBox = TextBox.wrap (RootPanel.get ("managementSystem_port").getElement ());
      tlsBox = SimpleCheckBox.wrap (RootPanel.get ("managementSystem_tls").getElement ());
      username = TextBox.wrap (RootPanel.get ("managementSystem_username").getElement ());
      password = TextBox.wrap (RootPanel.get ("managementSystem_password").getElement ());
      fromName = TextBox.wrap (RootPanel.get ("managementSystem_expeditorName").getElement ());
      fromMail = TextBox.wrap (RootPanel.get ("managementSystem_expeditorMail").getElement ());
      replyTo = TextBox.wrap (RootPanel.get ("managementSystem_reply").getElement ());
      supportMail = TextBox.wrap (RootPanel.get ("managementSystem_supportMail").getElement ());
      supportName = TextBox.wrap (RootPanel.get ("managementSystem_supportName").getElement ());
      mailDelete = SimpleCheckBox.wrap (RootPanel.get ("managementSystem_mailOnDelete").getElement ());
      mailCreate = SimpleCheckBox.wrap (RootPanel.get ("managementSystem_mailOnCreate").getElement ());
      mailUpdate = SimpleCheckBox.wrap (RootPanel.get ("managementSystem_mailOnUpdate").getElement ());
      synchronizeLocalArchive = RootPanel.get ("managementSystem_resetArchive");
      resetDefault = RootPanel.get ("managementSystem_resetDefault");
      saveModifications = RootPanel.get ("managementSystem_saveModifications");
      rootOldPassword = TextBox.wrap (RootPanel.get ("managementSystem_oldRootPassword").getElement ());
      rootPassword = TextBox.wrap (RootPanel.get ("managementSystem_newRootPassword").getElement ());
      root2Password = TextBox.wrap (RootPanel.get ("managementSystem_newRootPasswordConfirm").getElement ());
      saveRootPassword = RootPanel.get ("managementSystem_saveRoot");
      dumpBox = ListBox.wrap (RootPanel.get ("managementSystem_restoreSelect").getElement ());
      restoreButton = RootPanel.get ("managementSystem_restoreButton");      
      
   // click handlers
      resetDefault.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (resetDefault.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            disableGeneralPanel ();
            systemService.resetToDefaultConfiguration (systemSettingsCallback);
         }
      }, ClickEvent.getType ());
      synchronizeLocalArchive.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (synchronizeLocalArchive.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            archiveService.synchronizeLocalArchive (new AsyncCallback<Integer> ()
            {
               @Override
               public void onFailure (Throwable caught)
               {
                  Window.alert ("Local archive was not synchronized.\n" +
                     caught.getMessage ());
               }

               @Override
               public void onSuccess (Integer result)
               {
                  Window.alert ("Local archive synchronization is successfully launched. Ingestion of "+result+" found product" +
                     (result > 1 ? "s" : "")+" is now running.");
               }
            });
         }
      }, ClickEvent.getType ());
      saveModifications.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (saveModifications.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            disableGeneralPanel ();
            ConfigurationData confData = new ConfigurationData ();

            confData.setMailWhenCreate (mailCreate.getValue ());
            confData.setMailWhenUpdate (mailUpdate.getValue ());
            confData.setMailWhenDelete (mailDelete.getValue ());

            confData.setMailServerSmtp (smtpBox.getValue ());
            confData.setMailServerPassword (password.getValue ());
            confData.setMailServerTls (tlsBox.getValue ());
            confData.setMailServerPort (Integer.parseInt (portBox.getValue ()));
            confData.setMailServerUser (username.getValue ());

            confData.setMailServerFromMail (fromMail.getValue ());
            confData.setMailServerFromName (fromName.getValue ());
            confData.setMailServerReplyTo (replyTo.getValue ());

            confData.setSupportMail (supportMail.getValue ());
            confData.setSupportName (supportName.getValue ());

            systemService.saveConfiguration (confData,
               systemSettingsCallback);
         }
      }, ClickEvent.getType ());
      saveRootPassword.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (saveRootPassword.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            root2Password.setEnabled (false);
            rootOldPassword.setEnabled (false);
            rootPassword.setEnabled (false);
            saveRootPassword.getElement ().setClassName ("button_disabled");
            String newPassword = rootPassword.getValue ();
            String oldPassword = rootOldPassword.getValue ();
            systemService.changeRootPassword (newPassword, oldPassword,
               new AsyncCallback<Void> ()
               {
                  @Override
                  public void onFailure (Throwable caught)
                  {
                     Window.alert ("Root password was not changed.\n" +
                        caught.getMessage ());
                     root2Password.setEnabled (true);
                     rootOldPassword.setEnabled (true);
                     rootPassword.setEnabled (true);
                     saveRootPassword.getElement ().setClassName ("button_black");
                  }

                  @Override
                  public void onSuccess (Void result)
                  {
                     Window.alert ("Root password was changed.");
                     root2Password.setValue ("");
                     rootOldPassword.setValue ("");
                     rootPassword.setValue ("");
                     root2Password.setEnabled (true);
                     rootOldPassword.setEnabled (true);
                     rootPassword.setEnabled (true);
                     saveRootPassword.getElement ().setClassName ("button_black");
                  }
               });
         }
      }, ClickEvent.getType ());
      restoreButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (restoreButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            dumpBox.setEnabled (false);
            restoreButton.getElement ().setClassName ("button_disabled");
            systemService.restoreDatabase (
               dumpDates.get (dumpBox.getSelectedIndex ()),
               new AsyncCallback<Void> ()
               {

                  @Override
                  public void onFailure (Throwable caught)
                  {
                     Window.alert ("Error while restoring database.\n" +
                        caught.getMessage ());
                     dumpBox.setEnabled (true);
                     restoreButton.getElement ().setClassName ("button_black");
                  }

                  @Override
                  public void onSuccess (Void result)
                  {
                     Window.alert ("Database was successfully restored.");
                     dumpBox.setEnabled (true);
                     restoreButton.getElement ().setClassName ("button_black");
                  }
               });
         }
      }, ClickEvent.getType ());

      systemSettingsCallback = new AsyncCallback<ConfigurationData> ()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert ("Error while loading system data.\n" +
               caught.getMessage ());
            smtpBox.setValue ("");
            portBox.setValue ("");
            tlsBox.setValue (false);
            username.setValue ("");
            password.setValue ("");
            fromMail.setValue ("");
            fromName.setValue ("");
            replyTo.setValue ("");
            mailCreate.setValue (false);
            mailUpdate.setValue (false);
            mailDelete.setValue (false);
            supportMail.setValue ("");
            supportName.setValue ("");

            root2Password.setValue ("");
            rootOldPassword.setValue ("");
            rootPassword.setValue ("");
            saveRootPassword.getElement ().setClassName ("button_disabled");
            enableGeneralPanel ();
         }

         @Override
         public void onSuccess (ConfigurationData result)
         {
            smtpBox.setValue (result.getMailServerSmtp ());
            portBox.setValue (new Integer (result.getMailServerPort ()).toString ());
            tlsBox.setValue (result.isMailServerTls ());
            username.setValue (result.getMailServerUser ());
            password.setValue (result.getMailServerPassword ());
            fromMail.setValue (result.getMailServerFromMail ());
            fromName.setValue (result.getMailServerFromName ());
            replyTo.setValue (result.getMailServerReplyTo ());
            mailCreate.setValue (result.isMailWhenCreate ());
            mailUpdate.setValue (result.isMailWhenUpdate ());
            mailDelete.setValue (result.isMailWhenDelete ());
            supportMail.setValue (result.getSupportMail ());
            supportName.setValue (result.getSupportName ());

            root2Password.setValue ("");
            rootOldPassword.setValue ("");
            rootPassword.setValue ("");
            saveRootPassword.getElement ().setClassName ("button_disabled");
            enableGeneralPanel ();
         }

      };

      getDumpDatabaseListCallback = new AsyncCallback<List<Date>> ()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert ("Error while requesting dump dates.\n" +
               caught.getMessage ());
            dumpDates = null;
            dumpBox.clear ();
            dumpBox.setEnabled (false);
            restoreButton.getElement ().setClassName ("button_disabled");
         }

         @Override
         public void onSuccess (List<Date> result)
         {
            DateTimeFormat sdf = DateTimeFormat.getFormat("EEEE dd MMMM yyyy - HH:mm:ss");
            dumpDates = result;
            dumpBox.clear ();
            for (Date date : dumpDates)
            {
               dumpBox.addItem (sdf.format (date));
            }
            dumpBox.setEnabled (dumpDates.size () > 0);
            restoreButton.getElement ().setClassName (
               dumpDates.size () > 0 ? "button_black":"button_disabled");
         }
      };

      systemService.getConfiguration (systemSettingsCallback);
      systemService.getDumpDatabaseList (getDumpDatabaseListCallback);
   }

   public static void disableGeneralPanel ()
   {
      smtpBox.setEnabled (false);
      portBox.setEnabled (false);
      tlsBox.setEnabled (false);
      username.setEnabled (false);
      password.setEnabled (false);
      fromMail.setEnabled (false);
      fromName.setEnabled (false);
      replyTo.setEnabled (false);
      mailCreate.setEnabled (false);
      mailUpdate.setEnabled (false);
      mailDelete.setEnabled (false);
      supportMail.setEnabled (false);
      supportName.setEnabled (false);

      saveModifications.getElement ().setClassName ("button_disabled");
      resetDefault.getElement ().setClassName ("button_disabled");
   }

   public static void enableGeneralPanel ()
   {
      smtpBox.setEnabled (true);
      portBox.setEnabled (true);
      tlsBox.setEnabled (true);
      username.setEnabled (true);
      password.setEnabled (true);
      fromMail.setEnabled (true);
      fromName.setEnabled (true);
      replyTo.setEnabled (true);
      mailCreate.setEnabled (true);
      mailUpdate.setEnabled (true);
      mailDelete.setEnabled (true);
      supportMail.setEnabled (true);
      supportName.setEnabled (true);

      resetDefault.getElement ().setClassName ("button_black");
      reCheckAll();
   }
}
