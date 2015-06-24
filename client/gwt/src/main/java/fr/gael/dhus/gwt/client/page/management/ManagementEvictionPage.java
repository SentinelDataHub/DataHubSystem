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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.client.page.SearchViewPage;
import fr.gael.dhus.gwt.services.EvictionServiceAsync;
import fr.gael.dhus.gwt.share.EvictionStrategyData;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.UserData;

public class ManagementEvictionPage extends AbstractPage
{
   private static EvictionServiceAsync evictionService = EvictionServiceAsync.Util.getInstance ();
   
   private static TextBox keepingPeriod;
   private static ListBox strategy;   
   private static RootPanel maxDiskUsage;
   
   private static RootPanel saveButton;
   private static RootPanel runButton;
   private static RootPanel cancelButton;
   
   public ManagementEvictionPage()
   {
      // name is automatically prefixed in JS by "management_"
      super.name = "Eviction";      
      super.roles = Arrays.asList (RoleData.SYSTEM_MANAGER);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.management.ManagementEvictionPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.management.ManagementEvictionPage::refresh()();
      }
   }-*/;
   
   @Override
   public void load()
   {
      // This page can only be loaded from Management Page
   }
   
   private static native void showSystemEviction(int maxProductsToEvict)
   /*-{
      $wnd.showSystemEviction( function ( sSource, aoData, fnCallback, oSettings ) {
            @fr.gael.dhus.gwt.client.page.management.ManagementEvictionPage::getEvictableProductsFunction(*)
               (fnCallback)},
         function ( value ) {
            @fr.gael.dhus.gwt.client.page.management.ManagementEvictionPage::viewProduct(*)
               (value)}, maxProductsToEvict);
   }-*/;
   
   private static native void setMaxDiskUsageValue(int value)
   /*-{
      $wnd.setMaxDiskUsageValue(value);
   }-*/;
   
   private static native void validateKeepingPeriod()
   /*-{
      $wnd.managementEviction_keepingPeriodValidate();
   }-*/;
   
   private static native void refreshProductsTable()
   /*-{
      $wnd.managementEviction_refreshProducts();
   }-*/;
   
   @Override
   public void refreshMe() 
   {
      refresh();
   }

   private static void refresh()
   {
      evictionService.getMaxDiskUsage (new AsyncCallback<Integer>()
      {         
         @Override
         public void onSuccess (Integer result)
         {
            setMaxDiskUsageValue(result);
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert("There was an error while accessing eviction properties.");
         }
      });
      evictionService.getKeepPeriod (new AsyncCallback<Integer>()
      {         
         @Override
         public void onSuccess (Integer result)
         {
            keepingPeriod.setValue (""+result);   
            validateKeepingPeriod();
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert("There was an error while accessing eviction properties.");
         }
      });
      strategy.clear ();
      evictionService.getAllStrategies (new AsyncCallback<List<EvictionStrategyData>>()
      {         
         @Override
         public void onSuccess (List<EvictionStrategyData> result)
         {
            for (EvictionStrategyData strat : result)
            {
               strategy.addItem (strat.getDescription (), strat.getId ());
            }
            evictionService.getStrategy (new AsyncCallback<String>()
               {         
               @Override
               public void onSuccess (String result)
               {
                  for (int i = 0; i < strategy.getItemCount (); i++)
                  {
                     if (strategy.getValue (i) == result)
                     {
                        strategy.setSelectedIndex (i);
                        return;
                     }
                  }
               }
               
               @Override
               public void onFailure (Throwable caught)
               {
                  Window.alert("There was an error while accessing eviction properties.");
               }
            });
         }
         
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert("There was an error while accessing eviction properties.");
         }
      }); 
      refreshProductsTable();
   }
   
   private static void init()
   {
      showSystemEviction(50);

      saveButton = RootPanel.get ("managementEviction_saveButton");
      runButton = RootPanel.get ("managementEviction_runButton");
      cancelButton = RootPanel.get ("managementEviction_cancelButton");
      
      cancelButton.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            if (cancelButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            refresh();
         }
      }, ClickEvent.getType ());
      
      saveButton.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            if (saveButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            evictionService.save (strategy.getValue (strategy.getSelectedIndex ()), new Integer(keepingPeriod.getValue ()), 
               new Integer(maxDiskUsage.getElement ().getInnerText ()), (new AsyncCallback<Void>()
               {         
               @Override
               public void onSuccess (Void result)
               {
                  refresh();
               }
               
               @Override
               public void onFailure (Throwable caught)
               {
                  Window.alert("There was an error while saving your new eviction properties.\n"+caught.getMessage ());
               }
            }));
         }
      }, ClickEvent.getType ());
      
      runButton.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            if (runButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            evictionService.save (strategy.getValue (strategy.getSelectedIndex ()), new Integer(keepingPeriod.getValue ()), 
               new Integer(maxDiskUsage.getElement ().getInnerText ()), (new AsyncCallback<Void>()
               {         
               @Override
               public void onSuccess (Void result)
               {
                  evictionService.doEvict (
                     new AsyncCallback<Void>()
                     {         
                         @Override
                         public void onSuccess (Void result)
                         {
                            refresh();
                            Window.alert("Product successfully evicted.");
                         }
                         @Override
                         public void onFailure (Throwable caught)
                         {
                            Window.alert("There was an error while evicting your new eviction properties.\n"+caught.getMessage ());
                         }
                     }
                  );
               }
               
               @Override
               public void onFailure (Throwable caught)
               {
                  Window.alert("There was an error while saving your new eviction properties.\n"+caught.getMessage ());
               }
            }));
         }
      }, ClickEvent.getType ());
      
      maxDiskUsage = RootPanel.get ("managementEviction_tooltip");
      
      keepingPeriod = TextBox.wrap (RootPanel.get ("managementEviction_keepingPeriod").getElement ());
      
      strategy = ListBox.wrap (RootPanel.get ("managementEviction_strategy").getElement ());
      
      refresh();
   }
   
   private static void viewProduct(int id)
   {
      SearchViewPage.viewProduct(id);
   }
      
   private static void getEvictableProductsFunction(final JavaScriptObject function)
   {        
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
         "wait");
      
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      
      evictionService.getEvictableProducts (new AsyncCallback<List<ProductData>>()
      {
         
         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
            "default");
            Window.alert("There was an error while accessing to evictable products.\n"+caught.getMessage ());
         }

         @Override
         public void onSuccess (final List<ProductData> products)
         {                    
            String json = toJson (GWT.getHostPageBaseURL (), products, 
               GWTClient.getCurrentUser ());
            
            GWTClient.callback (function, JsonUtils.safeEval (json));
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
            "default");
         }
      });
   }
   /**
    * Produces the JSON output required by Eviction tab.
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
      UserData user)
   {
      String json = "{\"aaData\": [\n";
      
      Iterator<ProductData>it = products.iterator ();
      while (it.hasNext ())
      {
         ProductData product = it.next ();
         json += toJson (product, base_url, user) + (it.hasNext ()?",\n":"\n");      
      }

      json += "],\"iTotalRecords\" : " + products.size () +
              ", \"iTotalDisplayRecords\" : " + products.size ()+"}";
      
      return json;
   }
}
