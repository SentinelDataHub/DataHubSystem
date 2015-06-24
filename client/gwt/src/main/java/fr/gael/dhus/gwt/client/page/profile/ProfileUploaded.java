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

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.client.page.SearchViewPage;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.share.UserData;

public class ProfileUploaded extends AbstractPage
{   
   private static UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
   
   public ProfileUploaded ()
   {
      // name is automatically prefixed in JS by "profileUploaded_"
      super.name = "Uploaded";
      super.roles = Arrays.asList (RoleData.UPLOAD);
   }

   @Override
   public native JavaScriptObject getJSInitFunction ()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.profile.ProfileUploaded::init()();
      }
   }-*/;

   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.profile.ProfileUploaded::refresh()();
      }
   }-*/;
   
   private static native void showProfileUploaded()
   /*-{
      $wnd.showProfileUploaded(
         function ( sSource, aoData, fnCallback, oSettings ) {
            @fr.gael.dhus.gwt.client.page.profile.ProfileUploaded::getUploadedProducts(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, fnCallback)},
         function ( value ) {
            @fr.gael.dhus.gwt.client.page.profile.ProfileUploaded::viewProduct(*)
               (value)});
   }-*/;
   
   private static native void refreshUploaded()
   /*-{
       $wnd.profileUploaded_refreshUploaded();
   }-*/;

   @Override
   public void refreshMe() 
   {
      refresh();
   }
   
   private static void refresh()
   {
      refreshUploaded ();
   }

   private static void init ()
   {
      showProfileUploaded ();
   }
   
   private static void getUploadedProducts(final int start, final int length, final JavaScriptObject function)
   {        
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
         "wait");
      
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      
      final UserData user = GWTClient.getCurrentUser ();
      userService.countUploadedProducts (user.getId (), new AsyncCallback<Integer>()
      {
         
         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
            "default");
            Window.alert("There was an error while counting your uploaded products.");
         }
   
         @Override
         public void onSuccess (final Integer total)
         {
            userService.getUploadedProductsIdentifiers (start, length, user.getId (), new AsyncCallback<List<String>>()
            {
               
               @Override
               public void onFailure (Throwable caught)
               {
                  DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                  "default");
                  Window.alert("There was an error while accessing to your uploaded products.");
               }
      
               @Override
               public void onSuccess (final List<String> products)
               {                    
                  String json = "{\"aaData\": [";
                  
//                  for (ProductData product : products)
//                  {               
//                     Long id = product.getId ();
//                     
//                     String summary = "";
//                     for (int i = 0; i < product.getSummary ().size (); i++)
//                     {
//                        summary = summary + product.getSummary ().get (i) + (i == product.getSummary ().size ()-1 ?"":", ");
//                     }
//      
//                     String download = "\""+GWT.getHostPageBaseURL ()+"odata/product("+id+")/"+"Download\"";
//                     
//                     json += "["+(product.hasThumbnail () ? "\""+GWT.getHostPageBaseURL ()+"odata/product("+id+")/"+"\"" : null)
//                              +",{\"identifier\": \""+product.getIdentifier ()+
//                              "\", \"link\": \""+GWT.getHostPageBaseURL ()+"odata/product("+id+")/\", \"summary\": \""+
//                              summary+"\"},"+id+","+download+","+id+"],";                        
//                  }            
//                  if (products.size () >= 1)
//                  {
//                     json = json.substring (0, json.length ()-1);
//                  }
//                  json += "],\"iTotalRecords\" : "+total+", \"iTotalDisplayRecords\" : "+total+"}";
                  
                  for (String product : products)
                  {               
                     json += "[\""+product+"\"],";                        
                  }            
                  if (products.size () >= 1)
                  {
                     json = json.substring (0, json.length ()-1);
                  }
                  json += "],\"iTotalRecords\" : "+total+", \"iTotalDisplayRecords\" : "+total+"}";
                  GWTClient.callback (function, JsonUtils.safeEval (json));
                  DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                  "default");
               }
            });
         }
      });
   }   
   
   private static void viewProduct(int id)
   {
      SearchViewPage.viewProduct(id);
   }
}
