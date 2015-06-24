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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.services.ProductCartServiceAsync;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.UserData;

public class CartPage extends AbstractPage
{
   private static UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
   private static ProductCartServiceAsync productCartService = ProductCartServiceAsync.Util
      .getInstance ();

   private static RootPanel clear;
   private static RootPanel download;
         
   public CartPage()
   {
      super.name = "Cart";  
      super.roles = Arrays.asList (RoleData.DOWNLOAD);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.CartPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.CartPage::refresh()();
      }
   }-*/;
   
   private static native void showCart()
   /*-{
      $wnd.showCart(
         function ( sSource, aoData, fnCallback, oSettings ) {
            @fr.gael.dhus.gwt.client.page.CartPage::getCart(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, fnCallback)},
         function ( value ) {
            @fr.gael.dhus.gwt.client.page.CartPage::viewProduct(*)
               (value)},
         function ( id ) {
            @fr.gael.dhus.gwt.client.page.CartPage::removeProductFromCart(*)
               (id)});
   }-*/;
      
   private static native void refreshCart()
   /*-{
       $wnd.profile_refreshCart();
   }-*/;   
      
   private static void refresh()
   {
      refreshCart ();
   }
   
   private static void init()
   {
      final Long uId = GWTClient.getCurrentUser ().getId ();
      showCart();      
      clear = RootPanel.get ("cart_clear");
      clear.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
            
            if (clear.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            productCartService.clearCart (uId, new AsyncCallback<Void>()
            {               
               @Override
               public void onFailure (Throwable caught)
               {
                  DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                  "default");
                  Window.alert("There was an error while clearing your cart.");
               }
               
               @Override
               public void onSuccess (Void result)
               {            
                  DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                  "default");   
                  refreshCart();
               }
            });
         }
      }, ClickEvent.getType ());
      
      download = RootPanel.get ("cart_download");
      download.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            if (download.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            Window.Location.replace (GWT.getHostPageBaseURL ()+"api/user/cart");
         }
      }, ClickEvent.getType ());
      
      refresh();
   }
      
   private static void viewProduct(int id)
   {
      SearchViewPage.viewProduct(id);
   }
   
   private static void removeProductFromCart(int id)
   {
      productCartService.removeProductFromCart (GWTClient.getCurrentUser ().getId (),
         new Long(id), new AsyncCallback<Void>()
         {
   
            @Override
            public void onFailure (Throwable caught)
            {
               Window.alert("There was an error while adding product to your cart.");
            }
   
            @Override
            public void onSuccess (Void result)
            {               
               refreshCart();
            }
         });
   }
   
   private static void getCart(final int start, final int length, final JavaScriptObject function)
   {        
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
         "wait");
      
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      
      final UserData user = GWTClient.getCurrentUser ();
      productCartService.countProductsInCart (user.getId (), new AsyncCallback<Integer>()
      {
         
         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
            "default");
            Window.alert("There was an error while counting products in your cart.");
         }

         @Override
         public void onSuccess (final Integer total)
         {
            productCartService.getProductsOfCart (start, length, user.getId (), new AsyncCallback<List<ProductData>>()
            {
               
               @Override
               public void onFailure (Throwable caught)
               {
                  DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                  "default");
                  Window.alert("There was an error while accessing to your cart.");
               }
      
               @Override
               public void onSuccess (final List<ProductData> products)
               {
                  String json = toJson (GWT.getHostPageBaseURL (), products, 
                     GWTClient.getCurrentUser (), total);
                 
                  clear.getElement ().setClassName (products.size() >= 1 ? "button_black":"button_disabled");
                  download.getElement ().setClassName (products.size() >= 1 ? "button_black":"button_disabled");
                  
                  GWTClient.callback (function, JsonUtils.safeEval (json));
                  DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                  "default");
               }
            });
         }
      });
   }
   /**
    * Produces the JSON output required by Cart tab.
    */
   public static String toJson (ProductData product, String base_url, UserData user)
   {
      String summary = "";
      for (int i = 0; i < product.getSummary ().size (); i++)
      {
         summary = summary + product.getSummary ().get (i) + 
            (i == product.getSummary ().size ()-1 ?"":", ");
      }

      boolean can_download = GWTClient.getCurrentUser ().getRoles ().
         contains (RoleData.DOWNLOAD);
      
//      String product_path = product.getOdataPath (base_url);
      String identifier   = product.getIdentifier ();
      String download  = product.getOdataDownaloadPath (base_url);
      String quicklook = product.getOdataQuicklookPath (base_url);
      String thumbnail = product.getOdataThumbnailPath (base_url);
      
      if (!can_download) download = "null";
      else download = "\"" + download + "\"";
      
      if (!product.hasThumbnail ()) thumbnail = "null";
      else thumbnail = "\"" + thumbnail + "\"";
      
      if (!product.hasQuicklook ()) quicklook = "null";
      else quicklook = "\"" + quicklook + "\"";
      
      String json = "[\n" +
           "   {\n" +
           "      \"quicklook\": " + quicklook + ",\n" +
           "      \"thumbnail\": " + thumbnail + "\n" +
           "   },\n" +
           "   {\n" +
           "      \"identifier\": \""  + identifier + "\",\n" +
           "      \"link\": "          + download + ",\n"+
           "      \"summary\": \""     + summary + "\"\n" +
           "   },\n" +
           "   " + product.getId () + ",\n" +
           "   " + download + ",\n" +
           "   " + product.getId () + "\n" +
           "]" ;
      return json;
    }
   
   public static String toJson (String base_url, List<ProductData> products, 
      UserData user, int total)
   {
      String json = "{\"aaData\": [\n";
      
      Iterator<ProductData>it = products.iterator ();
      while (it.hasNext ())
      {
         ProductData product = it.next ();
         json += toJson (product, base_url, user) + (it.hasNext ()?",\n":"\n");      
      }

      json += "],\"iTotalRecords\" : " + total +
              ", \"iTotalDisplayRecords\" : " + total+"}";
      
      return json;
   }
}
