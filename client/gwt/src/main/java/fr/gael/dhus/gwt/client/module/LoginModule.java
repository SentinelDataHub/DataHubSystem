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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.Page;
import fr.gael.dhus.gwt.services.SecurityServiceAsync;
import fr.gael.dhus.gwt.share.UserData;

public class LoginModule
{
   private static TextBox usernameInput;
   private static TextBox passwordInput;

   private static native void showLogin ()
   /*-{
      $wnd.showLogin();
   }-*/;

   private static native void showLogout (String name)
   /*-{
      $wnd.showLogout(name);
   }-*/;

   private static native void loginError (String error)
   /*-{
      $wnd.loginError(error);
   }-*/;

   public static native void load ()
   /*-{
      $wnd.loadLogin(function() {
            @fr.gael.dhus.gwt.client.module.LoginModule::init()();
         });
   }-*/;

   private static void init ()
   {
      final SecurityServiceAsync securityService =
         SecurityServiceAsync.Util.getInstance ();

      usernameInput =
         TextBox.wrap (RootPanel.get ("login_username").getElement ());
      passwordInput =
         PasswordTextBox.wrap (RootPanel.get ("login_password").getElement ());

      final RootPanel login_button = RootPanel.get ("login_button");
      final RootPanel logout_button = RootPanel.get ("logout_button");

      login_button.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            try
            {
               String url = GWT.getHostPageBaseURL () + "/login";
               StringBuilder data = new StringBuilder ();
               data.append ("login_username=").append (
                  URL.encodeQueryString (usernameInput.getValue ()));
               data.append ("&");
               data.append ("login_password=").append (
                  URL.encodeQueryString (passwordInput.getValue ()));
               RequestBuilder rb =
                  new RequestBuilder (RequestBuilder.POST, URL.encode (url));
               rb.setHeader ("Content-Type",
                  "application/x-www-form-urlencoded");
               rb.sendRequest (data.toString (), new RequestCallback ()
               {
                  @Override
                  public void onResponseReceived (Request request,
                     Response response)
                  {
                     if (response.getStatusCode () != 200)
                     {
                        loginError (response.getText());
                     }
                     else
                     {
                     securityService
                        .getCurrentUser (new AsyncCallback<UserData> ()
                        {

                           @Override
                           public void onSuccess (UserData result)
                           {
                              if (result == null)
                              {
                                 loginError ("There was an error with your login/password combination. Please try again.");
                                 return;
                              }
                              loginRefresh ();
                           }

                           @Override
                           public void onFailure (Throwable caught)
                           {
                              Window.alert (caught.getMessage ());
                              loginRefresh ();
                           }
                        });
                     }
                  }

                  @Override
                  public void onError (Request request, Throwable exception)
                  {
                     Window.alert (exception.getMessage ());
                     loginRefresh ();
                  }
               });
            }
            catch (Exception e)
            {
               Window.alert (e.getMessage ());
               loginRefresh ();
            }
         }
      }, ClickEvent.getType ());

      usernameInput.addKeyDownHandler (new KeyDownHandler ()
      {
         @Override
         public void onKeyDown (KeyDownEvent event)
         {
            if (event.getNativeKeyCode () == KeyCodes.KEY_ENTER)
            {
               passwordInput.setFocus (true);
            }
         }
      });
      passwordInput.addKeyDownHandler (new KeyDownHandler ()
      {
         @Override
         public void onKeyDown (KeyDownEvent event)
         {
            if (event.getNativeKeyCode () == KeyCodes.KEY_ENTER)
            {
               NativeEvent evt =
                  Document.get ().createClickEvent (0, 0, 0, 0, 0, false,
                     false, false, false);
               DomEvent.fireNativeEvent (evt, login_button);
            }
         }
      });

      logout_button.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            try
            {
               String url = GWT.getHostPageBaseURL () + "/logout";
               RequestBuilder rb =
                  new RequestBuilder (RequestBuilder.POST, URL.encode (url));
               rb.setHeader ("Content-Type",
                  "application/x-www-form-urlencoded");
               rb.sendRequest (null, new RequestCallback ()
               {

                  @Override
                  public void onResponseReceived (Request request,
                     Response response)
                  {
                     loginRefresh ();
                  }

                  @Override
                  public void onError (Request request, Throwable exception)
                  {
                     Window.alert ("Error while loging out user.\n" +
                        exception.getMessage ());
                     loginRefresh ();
                  }
               });
            }
            catch (RequestException e)
            {
               Window.alert ("Error while loging out user:\n" + e.getMessage ());
            }
         }
      }, ClickEvent.getType ());

      loginRefresh ();
   }

   private static void loginRefresh ()
   {
      final SecurityServiceAsync securityService =
         SecurityServiceAsync.Util.getInstance ();
      AsyncCallback<UserData> callback = new AsyncCallback<UserData> ()
      {
         public void onSuccess (UserData result)
         {
            if (result == null)
            {
               showLogin ();
               usernameInput.setValue ("");
               passwordInput.setValue ("");
               Label login_forgot =
                  Label.wrap (RootPanel.get ("login_forgot").getElement ());
               login_forgot.addClickHandler (new ClickHandler ()
               {
                  @Override
                  public void onClick (ClickEvent event)
                  {
                     Page.FORGOT.load ();
                  }
               });
               GWTClient.loggedOut ();
            }
            else
            {
               showLogout (result.getUsername ());
               GWTClient.loggedIn (result);
            }
         }

         public void onFailure (Throwable ex)
         {
            Window.alert ("Error while requesting user information.\n" +
               ex.getMessage ());
            showLogin ();
            GWTClient.loggedOut ();
         }
      };

      securityService.getCurrentUser (callback);
   }

}
