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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.services.ProductCartServiceAsync;
import fr.gael.dhus.gwt.services.ProductServiceAsync;
import fr.gael.dhus.gwt.services.SearchServiceAsync;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.SearchData;
import fr.gael.dhus.gwt.share.UserData;

public class SearchPage extends AbstractPage
{
   private static TextBox search_value;
   private static TextBox search_request;
   
   private static boolean searchClicked = false;
   
   private static RootPanel search_button;
   private static boolean firstSearch = true;
   private static boolean firstRefresh = true;
   private static SearchServiceAsync searchService = SearchServiceAsync.Util
            .getInstance ();
   private static UserServiceAsync userService = UserServiceAsync.Util
            .getInstance ();
   private static ProductServiceAsync productService = ProductServiceAsync.Util
            .getInstance ();
   private static ProductCartServiceAsync productCartService = ProductCartServiceAsync.Util
            .getInstance ();
   
   private static List<Long> cart;
   
   private static HashMap<Long, SearchData> displayedSearches = new HashMap<Long, SearchData> ();
   
   private static String ADVANCED_COLLECTION = "productType";
   private static String ADVANCED_INSTRUMENT = "sensorMode";
   private static String ADVANCED_RESOLUTION = "resolution";
   private static String ADVANCED_PLATFORM = "polarisation";
   private static String ADVANCED_SENSINGDATE = "sensingDate";
   private static String ADVANCED_SENSINGDATEEND = "sensingDateEnd";
   private static String ADVANCED_INGESTIONDATE = "ingestionDate";
   private static String ADVANCED_INGESTIONDATEEND = "ingestionDateEnd";   

   private static TextBox advancedProductType;
   private static TextBox advancedPolarisation;
   private static TextBox advancedSensorMode;
   private static TextBox advancedSwath;
   private static TextBox advancedSensingDate;
   private static TextBox advancedIngestionDate;
   private static TextBox advancedSensingDateEnd;
   private static TextBox advancedIngestionDateEnd;
   
   private static boolean advancedSearchActive = false;
   
   private static SearchData givenSearch;
   
   public SearchPage()
   {
      super.name = "Search";
      super.roles = Arrays.asList (RoleData.SEARCH);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.SearchPage::init()();
      }
   }-*/;
   
   private static native void showSearch()
   /*-{
      $wnd.showSearch(function ( sSource, aoData, fnCallback, oSettings ) {
          @fr.gael.dhus.gwt.client.page.SearchPage::search(*)(oSettings._iDisplayStart, oSettings._iDisplayLength, fnCallback);
       }, function(data) { 
          @fr.gael.dhus.gwt.client.page.SearchPage::viewItem(*)(data);
       }, function(id, inUserCart) { 
          @fr.gael.dhus.gwt.client.page.SearchPage::modifyItemInCart(*)(id, inUserCart);
       }, function(active) { 
          @fr.gael.dhus.gwt.client.page.SearchPage::setAdvancedSearchActive(*)(active);
       }, function(id) { 
          @fr.gael.dhus.gwt.client.page.SearchPage::selectSavedSearch(*)(id);
       }, function(data, name) { 
          @fr.gael.dhus.gwt.client.page.SearchPage::deleteProduct(*)(data, name);
       });
   }-*/;
   
   private static native void doSearch()
   /*-{
      $wnd.doSearch();
   }-*/;
   
   private static native String getCurrentPolygon()
   /*-{
      return $wnd.getCurrentPolygon();
   }-*/;
   
   private static native String getCurrentPolygonSearchString()
   /*-{
      return $wnd.getCurrentPolygonSearchString();
   }-*/;
      
   private static native void destroyFeaturesFromAllFootprintLayer()
   /*-{
      $wnd.destroyFeaturesFromAllFootprintLayer();
   }-*/;
      
   private static native void addFeatureToAllFootprintLayer(JavaScriptObject feature)
   /*-{
      $wnd.addFeatureToAllFootprintLayer(feature);
   }-*/;
      
   private static native void setSearchedFootprint(JavaScriptObject feature)
   /*-{
      $wnd.search_setSearchedFootprint(feature);
   }-*/;
   
   private static native void searchIsEmpty()
   /*-{
      $wnd.searchIsEmpty();
   }-*/;
   
   private static native boolean searchTableDefined()
   /*-{
      return $wnd.searchTableDefined();
   }-*/;
   
   private static native void search_filling()
   /*-{
      return $wnd.search_filling();
   }-*/;     
   
   private static native void setSearches(JavaScriptObject searches)
   /*-{
      return $wnd.setSearches(searches);
   }-*/;       

   private static native void refreshSearch()
   /*-{
      return $wnd.search_refreshSearch();
   }-*/;      

   private static native void displayAdvancedSearch(boolean displayed)
   /*-{
      return $wnd.search_displayAdvancedSearch(displayed);
   }-*/;   
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.SearchPage::refresh()();
      }
   }-*/;
   
   @Override
   public void unload()
   {
      super.unload();
      firstSearch = true;
      firstRefresh = true;
   }
   
   private static void refresh()
   {      
      if (givenSearch != null)
      {
         search_value.setValue (givenSearch.getValue ());
         search_filling();
         advancedProductType.setValue ("");
         advancedSensorMode.setValue ("");
         advancedPolarisation.setValue ("");
         advancedSwath.setValue ("");
         advancedIngestionDate.setValue ("");
         advancedIngestionDateEnd.setValue ("");
         advancedSensingDate.setValue ("");
         advancedSensingDateEnd.setValue ("");
         if (givenSearch.getAdvanced () != null && !givenSearch.getAdvanced ().isEmpty ())
         {
            displayAdvancedSearch(true);                        
            for (String key : givenSearch.getAdvanced ().keySet ())
            {
               String value = givenSearch.getAdvanced ().get (key);
               if (key == ADVANCED_COLLECTION)
               {
                  advancedProductType.setValue (value);
               }
               else if (key == ADVANCED_INSTRUMENT)
               {
                  advancedSensorMode.setValue (value);
               }
               else if (key == ADVANCED_PLATFORM)
               {
                  advancedPolarisation.setValue (value);
               }
               else if (key == ADVANCED_RESOLUTION)
               {
                  advancedSwath.setValue (value);
               }
               else if (key == ADVANCED_SENSINGDATE)
               {
                  advancedSensingDate.setValue (value);
               }
               else if (key == ADVANCED_SENSINGDATEEND)
               {
                  advancedSensingDateEnd.setValue (value);
               }
               else if (key == ADVANCED_INGESTIONDATE)
               {
                  advancedIngestionDate.setValue (value);
               }
               else if (key == ADVANCED_INGESTIONDATEEND)
               {
                  advancedIngestionDateEnd.setValue (value);
               }
            }
         }
         else
         {
            displayAdvancedSearch(false);
         }        
         if (givenSearch.getFootprint () != null)
         {
            Double[][]pts = givenSearch.getFootprint ();
            JavaScriptObject footPrintJS=ProductData.getJsFootprintLayer (pts);
            setSearchedFootprint (footPrintJS);
         }
         else
         {
            setSearchedFootprint (null);
         }
         NativeEvent evt = Document.get().createClickEvent (0,0,0,0,0,false,false,false,false);
         DomEvent.fireNativeEvent(evt, search_button);
         givenSearch = null;
      }
      else if (!firstRefresh && searchClicked)
      {
         refreshSearch ();
      }
      else
      {
         firstRefresh = false;
      }
      setSearches(null);
      displayedSearches.clear();
      reloadSavedSearches();
   }   
   
   private static void reloadSavedSearches()
   {
      userService.getAllUserSearches (GWTClient.getCurrentUser ().getId (), new AsyncCallback<List<SearchData>>()
         {

            @Override
            public void onFailure (Throwable caught)
            {
               Window.alert("There was an error while getting your saved searches.");
            }

            @Override
            public void onSuccess (List<SearchData> result)
            {
               if (result == null)
               {
                  return;
               }
               String searches = "[";               
               for (SearchData search : result)
               {
                  String advancedText = "";
                  Map<String, String> advanceds = search.getAdvanced ();     
                  if (advanceds != null && !advanceds.isEmpty ())
                  {
                     advancedText += "<i> (";
                     boolean first = true;
                     List<String> keys = new ArrayList<String> (advanceds.keySet ());
                     Collections.sort (keys);        
                     String lastKey = "";
                     for (String key : keys)
                     {
                        if ((lastKey+"End").equals(key))
                        {
                           advancedText += " to "+advanceds.get (key);
                        }  
                        else
                        {
                           if (key.endsWith ("End"))
                           {
                              advancedText += (first?"":", ") + key.substring (0, key.length ()-3)+": * to "+advanceds.get (key);
                           }
                           else
                           {
                              advancedText += (first?"":", ") + key+": "+advanceds.get (key);
                           }
                        }
                        first = false;
                        lastKey = key;
                     }
                     advancedText += ")</i>";
                  }
                  displayedSearches.put (search.getId (), search);
                  searches += "{\"id\":"+search.getId ()+",\"value\":\""+search.getValue().replace ("\"", "\\\"")+advancedText+"\"},";
               }
               if (result.size () > 0)
               {
                  searches = searches.substring (0, searches.length ()-1);
               }
               searches += "]";
               setSearches(JsonUtils.safeEval (searches));
            }
         });
   }
   
   private static void init()
   {
      firstSearch = true;

      advancedProductType = TextBox.wrap (RootPanel.get ("search_advancedFieldProductType").getElement ());
      advancedIngestionDate = TextBox.wrap (RootPanel.get ("search_advancedFieldIngestionDate").getElement ());
      advancedSensorMode = TextBox.wrap (RootPanel.get ("search_advancedFieldSensorMode").getElement ());
      advancedPolarisation = TextBox.wrap (RootPanel.get ("search_advancedFieldPolarisation").getElement ());
      advancedSwath = TextBox.wrap (RootPanel.get ("search_advancedFieldSwath").getElement ());
      advancedSensingDate = TextBox.wrap (RootPanel.get ("search_advancedFieldSensingDate").getElement ());
      advancedIngestionDateEnd = TextBox.wrap (RootPanel.get ("search_advancedFieldIngestionDateEnd").getElement ());
      advancedSensingDateEnd = TextBox.wrap (RootPanel.get ("search_advancedFieldSensingDateEnd").getElement ());
      
      search_button = RootPanel.get ("search_button");
      search_button.addDomHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {            
            searchClicked = true;
            doSearch();
         }
      }, ClickEvent.getType ());
      search_request = TextBox.wrap (RootPanel.get ("search_request").getElement ());
      search_value = TextBox.wrap (RootPanel.get ("search_value").getElement ());
      search_value.addKeyUpHandler (new KeyUpHandler()
      {         
         @Override
         public void onKeyUp (KeyUpEvent event)
         {
            if (event.getNativeKeyCode () == KeyCodes.KEY_ENTER)
            {
               NativeEvent evt = Document.get().createClickEvent (0,0,0,0,0,false,false,false,false);
               DomEvent.fireNativeEvent(evt, search_button);
            }
            else
            {
               search_filling();
            }
         }
      });
      final Image saveSearch = Image.wrap (RootPanel.get ("search_saveImage").getElement ());
      saveSearch.addClickHandler (new ClickHandler()
      {         
         @Override
         public void onClick (ClickEvent event)
         {
            if (saveSearch.getUrl ().contains("saveSearch_disabled"))
            {
               return;
            }

            String searchPoly = getCurrentPolygonSearchString();
            String footprint = getCurrentPolygon();
            String advancedSearch = "";
            HashMap<String, String> advancedSearchMap = new HashMap<String, String>();
            if (advancedSearchActive)
            {
               boolean first = true;
               String productType = advancedProductType.getValue ();
               String polarisation = advancedPolarisation.getValue ();
               String sensorMode = advancedSensorMode.getValue ();
               String swath = advancedSwath.getValue ();
               String sensingDate = advancedSensingDate.getValue ();
               String ingestionDate = advancedIngestionDate.getValue ();
               String sensingDateEnd = advancedSensingDateEnd.getValue ();
               String ingestionDateEnd = advancedIngestionDateEnd.getValue ();
               if (productType != null && !productType.isEmpty ())
               {
                  advancedSearchMap.put (ADVANCED_COLLECTION, productType);
                  advancedSearch += " productType:\""+productType+"\" ";
                  first = false;
               }
               if (polarisation != null && !polarisation.isEmpty ())
               {
                  advancedSearchMap.put (ADVANCED_PLATFORM, polarisation);
                  advancedSearch += (!first?" AND ":"")+" polarisationMode:\""+polarisation+"\" ";
                  first = false;
               }
               if (sensorMode != null && !sensorMode.isEmpty ())
               {
                  advancedSearchMap.put (ADVANCED_INSTRUMENT, sensorMode);
                  advancedSearch += (!first?" AND ":"")+" sensorOperationalMode:\""+sensorMode+"\" ";
                  first = false;
               }
               if (swath != null && !swath.isEmpty ())
               {
                  advancedSearchMap.put (ADVANCED_RESOLUTION, swath);
                  advancedSearch += (!first?" AND ":"")+" swathIdentifier:\""+swath+"\" ";
                  first = false;
               }
               if (sensingDate != null && !sensingDate.isEmpty ())
               {
                  advancedSearchMap.put (ADVANCED_SENSINGDATE, sensingDate);
               }
               if (sensingDateEnd != null && !sensingDateEnd.isEmpty ())
               {
                  advancedSearchMap.put (ADVANCED_SENSINGDATEEND, sensingDateEnd);
               }
               if (ingestionDate != null && !ingestionDate.isEmpty ())
               {
                  advancedSearchMap.put (ADVANCED_INGESTIONDATE, ingestionDate);
               }
               if (ingestionDateEnd != null && !ingestionDateEnd.isEmpty ())
               {
                  advancedSearchMap.put (ADVANCED_INGESTIONDATEEND, ingestionDateEnd);
               }
               if ((sensingDate != null && !sensingDate.isEmpty ()) || (sensingDateEnd != null && !sensingDateEnd.isEmpty ()))
               {
                  String range = "[" + ((sensingDate != null && !sensingDate.isEmpty ())?
                        sensingDate+"T00:00:00.000Z":"*") +
                     " TO " + ((sensingDateEnd != null && !sensingDateEnd.isEmpty ())?
                        sensingDateEnd+"T23:59:59.999Z":"NOW") + "]";
                  advancedSearch += (!first?" AND ":"")+"( beginPosition:"+range+" AND endPosition:"+range+" ) ";
                  first = false;
               }
               if ((ingestionDate != null && !ingestionDate.isEmpty ()) || (ingestionDateEnd != null && !ingestionDateEnd.isEmpty ()))
               {
                  String range = "[" + ((ingestionDate != null && !ingestionDate.isEmpty ())?
                        ingestionDate+"T00:00:00.000Z":"*") +
                     " TO " + ((ingestionDateEnd != null && !ingestionDateEnd.isEmpty ())?
                        ingestionDateEnd+"T23:59:59.999Z":"NOW") + " ]";
                  advancedSearch += (!first?" AND ":"")+"( ingestionDate:"+range+" ) ";
                  first = false;
               }
            }
            
            String tmpSearch = search_value.getValue ();
            tmpSearch += (!tmpSearch.isEmpty () && !advancedSearch.isEmpty ()?" AND ": "")+ advancedSearch;
            tmpSearch += (!tmpSearch.isEmpty () && !searchPoly.isEmpty ()?" AND ": "")+ searchPoly;

            String completeSearch = tmpSearch.isEmpty () ? "*" : tmpSearch.trim ();
            String search = search_value.getValue ();
            search = search.isEmpty () ? "*" : search.trim ();
            
            userService.storeUserSearch (GWTClient.getCurrentUser ().getId (), 
               search, footprint, advancedSearchMap, completeSearch.trim(), new AsyncCallback<Void>()
            {
      
               @Override
               public void onFailure (Throwable caught)
               {
                  Window.alert("There was an error while saving your search.");
               }
      
               @Override
               public void onSuccess (Void result)
               {
                  reloadSavedSearches();
               }
            });
         }
      });
      showSearch();
      refresh();
   }

   private static void search(final int start, final int length, final JavaScriptObject function)
   {        
      destroyFeaturesFromAllFootprintLayer();
      String searchPoly = getCurrentPolygonSearchString();
      String advancedSearch = "";
      if (advancedSearchActive)
      {
         boolean first = true;
         String productType = advancedProductType.getValue ();
         String polarisation = advancedPolarisation.getValue ();
         String sensorMode = advancedSensorMode.getValue ();
         String swath = advancedSwath.getValue ();
         String sensingDate = advancedSensingDate.getValue ();
         String ingestionDate = advancedIngestionDate.getValue ();
         String sensingDateEnd = advancedSensingDateEnd.getValue ();
         String ingestionDateEnd = advancedIngestionDateEnd.getValue ();
         if (productType != null && !productType.isEmpty ())
         {
            advancedSearch += " productType:\""+productType+"\" ";
            first = false;
         }
         if (polarisation != null && !polarisation.isEmpty ())
         {
            advancedSearch += (!first?" AND ":"")+" polarisationMode:\""+polarisation+"\" ";
            first = false;
         }
         if (sensorMode != null && !sensorMode.isEmpty ())
         {
            advancedSearch += (!first?" AND ":"")+" sensorOperationalMode:\""+sensorMode+"\" ";
            first = false;
         }
         if (swath != null && !swath.isEmpty ())
         {
            advancedSearch += (!first?" AND ":"")+" swathIdentifier:\""+swath+"\" ";
            first = false;
         }
         if ((sensingDate != null && !sensingDate.isEmpty ()) || (sensingDateEnd != null && !sensingDateEnd.isEmpty ()))
         {
            String range = "[" + ((sensingDate != null && !sensingDate.isEmpty ())?
                  sensingDate+"T00:00:00.000Z":"*") +
               " TO " + ((sensingDateEnd != null && !sensingDateEnd.isEmpty ())?
                  sensingDateEnd+"T23:59:59.999Z":"NOW") + "]";
            advancedSearch += (!first?" AND ":"")+"( beginPosition:"+range+" AND endPosition:"+range+" ) ";
            first = false;
         }
         if ((ingestionDate != null && !ingestionDate.isEmpty ()) || (ingestionDateEnd != null && !ingestionDateEnd.isEmpty ()))
         {
            String range = "[" + ((ingestionDate != null && !ingestionDate.isEmpty ())?
                  ingestionDate+"T00:00:00.000Z":"*") +
               " TO " + ((ingestionDateEnd != null && !ingestionDateEnd.isEmpty ())?
                  ingestionDateEnd+"T23:59:59.999Z":"NOW") + " ]";
            advancedSearch += (!first?" AND":"")+"( ingestionDate:"+range+" ) ";
            first = false;
         }
      }
      
      String tmpSearch = search_value.getValue ();
      tmpSearch += (!tmpSearch.isEmpty () && !advancedSearch.isEmpty ()?" AND ": "")+ advancedSearch;      
      tmpSearch += (!tmpSearch.isEmpty () && !searchPoly.isEmpty ()?" AND ": "")+ searchPoly;

      if (firstSearch && tmpSearch.isEmpty ())
      {
         firstSearch = false;
         GWTClient.callback (function, JsonUtils.safeEval (
            "{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
         return;
      }
      firstSearch = false;
      
      final String search = tmpSearch.isEmpty () ? "*" : tmpSearch.trim ();
      
      search_request.setValue (search);
      
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
         "wait");
      productCartService.getProductsIdOfCart (GWTClient.getCurrentUser ()
         .getId (), new AsyncCallback<List<Long>> ()
      {

         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
               "default");
            Window.alert ("There was an error while getting your cart.\n" + caught.getMessage ());
         }

         @Override
         public void onSuccess (final List<Long> result)
         {
            cart = result;
            searchService.count (search, new AsyncCallback<Integer> ()
            {

               @Override
               public void onFailure (Throwable caught)
               {
                  DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                     "default");
                  Window
                     .alert ("There was an error while counting your search of '" +
                        search + "'\n" + caught.getMessage ());
               }

               @Override
               public void onSuccess (final Integer total)
               {
                  searchService.search (search, start, length, GWTClient
                     .getCurrentUser ().getId (),
                     new AsyncCallback<List<ProductData>> ()
                     {
                        @Override
                        public void onFailure (Throwable caught)
                        {
                           DOM.setStyleAttribute (RootPanel.getBodyElement (),
                              "cursor", "default");
                           Window
                              .alert ("There was an error while searching for '" +
                                 search + "'\n" + caught.getMessage ());
                        }

                        @Override
                        public void onSuccess (List<ProductData> products)
                        {
                           String json =
                              toJson (GWT.getHostPageBaseURL (), products,
                                 GWTClient.getCurrentUser (), total);
                           // Update the layer with footprints.
                           for (ProductData product : products)
                           {
                              JavaScriptObject js = ProductData.
                                 getJsFootprintLayer (product.getFootprint ());
                              if (js != null)
                                 addFeatureToAllFootprintLayer (js);
                           }

                           if (JsonUtils.safeToEval (json))
                              GWTClient.callback (function,
                                 JsonUtils.safeEval (json));

                           DOM.setStyleAttribute (RootPanel.getBodyElement (),
                              "cursor", "default");
                        }
                     });
               }
            });
         }
      });
   }
   
   public static void search(SearchData search)
   {
      givenSearch = search;
      Page.SEARCH.load ();
   }
   
   private static void viewItem(int id)
   {
      SearchViewPage.viewProduct(id);
   }
   
   private static void deleteProduct(int id, String name)
   { 
      if ( !Window.confirm ("Are you sure to delete the product '"+name+"' ?"))
      {
         return;
      }
      productService.deleteProduct (
         new Long(id), new AsyncCallback<Void>()
         {
   
            @Override
            public void onFailure (Throwable caught)
            {
               Window.alert("There was an error while deleting product.\n "+caught.getMessage ());
            }
   
            @Override
            public void onSuccess (Void result)
            {               
               refreshSearch();
            }
         });
      return;
   }
   
   private static void modifyItemInCart(int id, boolean inUserCart)
   {
      if (inUserCart)
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
                  refreshSearch();
               }
            });
         return;
      }
      // else
      productCartService.addProductToCart (GWTClient.getCurrentUser ().getId (),
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
               refreshSearch();
            }
         });
   }
   
   private static void selectSavedSearch(int id)
   {
      givenSearch = displayedSearches.get (new Long(id));
      Page.SEARCH.load ();
   }
   
   private static void setAdvancedSearchActive(boolean active)
   {
      advancedSearchActive = active;
   }
   
   /**
    * Produces the JSON output required by search tab.
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
      
      String identifier   = product.getIdentifier ();
      String download  = product.getOdataDownaloadPath (base_url);
      String quicklook = product.getOdataQuicklookPath (base_url);
      String thumbnail = product.getOdataThumbnailPath (base_url);
      boolean deletable = user != null && 
                          user.getRoles() != null && 
                          user.getRoles ().contains (RoleData.DATA_MANAGER);
      
      if (!can_download) download = "null";
      else download = "\"" + download + "\"";
      
      if (!product.hasThumbnail ()) thumbnail = "null";
      else thumbnail = "\"" + thumbnail + "\"";
      
      if (!product.hasQuicklook ()) quicklook = "null";
      else quicklook = "\"" + quicklook + "\"";
      
      String footprintString = "[";
      Double[][] footprint = product.getFootprint ();
      if (footprint != null && footprint.length > 0)
      {                           
         for (Double[] d : footprint)
            footprintString += d[0]+","+d[1]+",";
      }
      if (footprint != null && footprint.length >= 1)
      {
         footprintString = footprintString.substring (0, footprintString.length ()-1);
      }
      footprintString += "]";
      
      boolean canDownload = GWTClient.getCurrentUser ().getRoles ().contains (RoleData.DOWNLOAD);
      
      String json = "[\n" +
           "   {\n" +
           "      \"quicklook\": " + quicklook + ",\n" +
           "      \"thumbnail\": " + thumbnail + "\n" +
           "   },\n" +
           "   {\n" +
           "      \"identifier\": \""  + identifier + "\",\n" +
           "      \"link\": "          + download + ",\n"+
           "      \"summary\": \""     + summary + "\",\n" +
           "      \"footprint\": "     + footprintString + "\n" +
           "   },\n" +
           "   " + product.getId () + ",\n" +
           "   " + download + ",\n" +
           "   {\n" +
           "      \"id\": "   + (canDownload?product.getId ():null) + ",\n" +
           "      \"name\": \"" + identifier + "\",\n" +
           "      \"inUserCart\": " + (cart != null && cart.contains (product.getId ())) + ",\n" +
           "      \"deletable\": "  + deletable + "\n" +
           "   }\n]" ;
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
