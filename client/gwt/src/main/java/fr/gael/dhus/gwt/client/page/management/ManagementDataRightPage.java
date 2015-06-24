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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.services.CollectionServiceAsync;
import fr.gael.dhus.gwt.services.ProductServiceAsync;
import fr.gael.dhus.gwt.services.UserServiceAsync;
import fr.gael.dhus.gwt.share.CollectionData;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.UserData;
import fr.gael.dhus.gwt.share.exceptions.UserServiceMailingException;

public class ManagementDataRightPage extends AbstractPage
{
   private static UserServiceAsync userService = UserServiceAsync.Util.getInstance ();
   private static ProductServiceAsync productService = ProductServiceAsync.Util.getInstance ();
   private static CollectionServiceAsync collectionService = CollectionServiceAsync.Util.getInstance ();
   
   private static List<ProductData> displayedProducts = new ArrayList<ProductData> ();
   private static HashMap<Long, CollectionData> displayedCollections = new HashMap<Long, CollectionData> ();
   
   private static ArrayList<Long> toRefresh;
   private static UserData selectedUser;
   private static UserData publicData;
   private static CollectionData root;
   
   private static RootPanel updateButton;
   private static RootPanel cancelButton;
   private static SimpleCheckBox productsCheckAll;
   private static SimpleCheckBox collectionsCheckAll;
   private static boolean collectionsCheckAllChecked;
   private static boolean collectionsAllPublic;
   
   private static State state;
   
   public ManagementDataRightPage ()
   {
      // name is automatically prefixed in JS by "management_"
      super.name = "DataRight";
      super.roles = Arrays.asList (RoleData.DATA_MANAGER);
   }

   @Override
   public native JavaScriptObject getJSInitFunction ()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::init()();
      }
   }-*/;

   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::refresh()();
      }
   }-*/;
   
   @Override
   public void load ()
   {
      // This page can only be loaded from Management Page
   }
   
   private static native void refreshCollections()
   /*-{
       $wnd.dr_refreshDRCollections();
   }-*/;
   
   private static native void refreshProducts()
   /*-{
       $wnd.dr_refreshDRProducts();
   }-*/;
   
   private static native void refreshUsers()
   /*-{
       $wnd.dr_refreshDRUsers();
   }-*/;

   private static native void deselectUser()
   /*-{
       $wnd.dr_deselectUser();
   }-*/;
      
   private static native void setUsersTableEnabled(boolean enabled)
   /*-{
       $wnd.dr_setUsersTableEnabled(enabled);
   }-*/;
   
   private static native void setProductsTableEnabled(boolean enabled)
   /*-{
       $wnd.dr_setProductsTableEnabled(enabled);
   }-*/;
   
   private static native void setCollectionsTableEnabled(boolean enabled)
   /*-{
       $wnd.dr_setCollectionsTableEnabled(enabled);
   }-*/;
   
   private static native void saveProductsScrollPosition()
   /*-{
       $wnd.dr_saveProductsScrollPosition();
   }-*/;
   
   private static native void resetProductsScrollPosition()
   /*-{
       $wnd.dr_resetProductsScrollPosition();
   }-*/;
   
   private static native void saveCollectionsScrollPosition()
   /*-{
       $wnd.dr_saveCollectionsScrollPosition();
   }-*/;
   
   private static native void resetCollectionsScrollPosition()
   /*-{
       $wnd.dr_resetCollectionsScrollPosition();
   }-*/;
   
   private static native void showDataRightManagement ()
   /*-{     
      $wnd.showDataRightManagement(
         function ( sSource, aoData, fnCallback, oSettings ) {   
            @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::getUsers(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, 
                oSettings.oPreviousSearch.sSearch, fnCallback)},
         function ( sSource, aoData, fnCallback, oSettings ) {
            @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::getProducts(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, 
               oSettings.oPreviousSearch.sSearch, fnCallback)},
         function ( sSource, aoData, fnCallback, oSettings ) {            
            @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::getCollections(*)
               (fnCallback)},             
         function (data) {
            if (data == null) {
               @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::setNothingState(*)()
            } else {
               @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::edit(*)(data[1])
            }},
         function (id) {
             @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::checkProduct(*)(id)
            },
         function () {
             @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::checkAllProducts(*)()
         },
         function (event, id) {
             if (event.stopPropagation) {
                 event.stopPropagation();   // W3C model
             } else {
                 event.cancelBubble = true; // IE model
             }
             @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::openCollection(*)(id)
         },
         function (id) {
             @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::checkCollection(*)(id)
            },
         function () {
             @fr.gael.dhus.gwt.client.page.management.ManagementDataRightPage::checkAllCollections(*)()
         });
   }-*/;
   
   @Override
   public void refreshMe() 
   {
      refresh();
   }
   
   private static void refresh()
   {
      refreshUsers ();
      refreshCollections ();
      refreshProducts ();
      setNothingState ();
   }

   private static void init ()
   {
      showDataRightManagement ();
       
      updateButton = RootPanel.get ("managementDataRight_buttonUpdate");
      cancelButton = RootPanel.get ("managementDataRight_buttonCancel");
                  
      updateButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (updateButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            save ();
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
      selectedUser = null;
      collectionsCheckAll.setValue (false);
      productsCheckAll.setValue (false);
      resetCollectionsScrollPosition ();
      resetProductsScrollPosition ();
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
      userService.getUserWithDataAccess (new Long(userId), new AsyncCallback<UserData> ()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (),
               "cursor", "default");            
            Window.alert ("There was an error while getting user data.\n"+caught.getMessage ());
         }

         @Override
         public void onSuccess (final UserData user)
         {
            userService.getPublicData (new AsyncCallback<UserData>()
            {

               @Override
               public void onFailure (Throwable caught)
               {
                  DOM.setStyleAttribute (RootPanel.getBodyElement (),
                     "cursor", "default");            
                  Window.alert ("There was an error while getting public data.\n"+caught.getMessage ());
                  
                  selectedUser = user;
                  resetCollectionsScrollPosition ();
                  resetProductsScrollPosition ();
                  setState (State.EDIT, true);
               }

               @Override
               public void onSuccess (UserData result)
               {
                  publicData = result;
                  selectedUser = user;
                  resetCollectionsScrollPosition ();
                  resetProductsScrollPosition ();
                  setState (State.EDIT, true);
                              
                  DOM.setStyleAttribute (RootPanel.getBodyElement (),
                     "cursor", "default");
               }
            });
         }         
      }); 
   }
   
   private static void save ()
   {               
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

            setState (State.EDIT, false);
         }

         @Override
         public void onSuccess (Void result)
         {
            setNothingState ();
            refreshUsers ();
         }
      };
    
      userService.updateDataAccess (selectedUser, callback);
   }

   private static void setState (State s, boolean setValue)
   {
      state = s;
      boolean isNotRoot =
         selectedUser == null ||
            ! (selectedUser.getUsername ().equals ("root"));

      boolean updatable =
         isNotRoot && state == State.EDIT;
      
      // Datagrids
      setUsersTableEnabled (state == State.NOTHING || state == State.EDIT);
      setCollectionsTableEnabled (state == State.EDIT && isNotRoot);
      setProductsTableEnabled (state == State.EDIT && isNotRoot);

      // Enable Buttons
      cancelButton.getElement ().setClassName (updatable?"button_black":"button_disabled");
      updateButton.getElement ().setClassName (updatable?"button_black":"button_disabled");

      // Buttons Visibility
      cancelButton.setVisible (true);
      updateButton.setVisible (true);

      // Fields Value
      if (setValue)
      {                  
         refreshCollections ();
         refreshProducts ();
      }      
   }

   private static void disableAll ()
   {
      setUsersTableEnabled (false);
      setCollectionsTableEnabled (false);
      setProductsTableEnabled (false);
      productsCheckAll.setEnabled (false);
      collectionsCheckAll.setEnabled (false);

      updateButton.getElement ().setClassName ("button_disabled");
      cancelButton.getElement ().setClassName ("button_disabled");
   }

   private static void getUsers (final int start, final int length, final String search,
      final JavaScriptObject function)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      
      userService.countForDataRight (search, new AsyncCallback<Integer> ()
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
            userService.getUsersForDataRight (start, length, search,
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
                        boolean publicData = user.getUsername().startsWith ("~");
                        json +=
                           "[{\"username\":\""+(publicData ? user.getUsername().substring (1) : user.getUsername ())+"\", \"publicData\":"+publicData+"},"+user.getId ()+"],";
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

   private static void checkCollection(int cId)
   {
      Long collectionId = new Long(cId);
      if (selectedUser != null &&
          selectedUser.containsCollection (collectionId))
      {
         selectedUser.removeCollection (collectionId);
      }
      else
      {
         selectedUser.addCollection (collectionId);
         List<Long> productIds = displayedCollections.get (collectionId).getProductIds ();
         if (productIds != null && !productIds.isEmpty ())
         {
            for (Long pId : productIds)
            {
               if (publicData.getId ()==selectedUser.getId () || !publicData.containsProduct (pId))
               {
                  selectedUser.addProduct (pId);
               }
            }
         }
      }
                 
      saveCollectionsScrollPosition ();
      saveProductsScrollPosition ();
      refreshCollections (); 
      refreshProducts ();  
   }
   
   private static void checkProduct(int pId)
   {
      Long productId = new Long(pId);
      if (selectedUser != null &&
          selectedUser.containsProduct (productId))
      {
         selectedUser.removeProduct (productId);
      }
      else
      {
         selectedUser.addProduct (productId);         
      }
      saveProductsScrollPosition ();
      refreshProducts ();  
   }

   private static void checkAllCollections()
   {
      Iterator<Long> idIterator = displayedCollections.keySet ().iterator ();
      while (idIterator.hasNext ())
      {
         Long id = idIterator.next ();
         if (id != root.getId () && (publicData.getId ()==selectedUser.getId () || !publicData.containsCollection (id)))
         {
            if (collectionsCheckAll.getValue ())
            {
               selectedUser.addCollection (id);
               List<Long> productIds = displayedCollections.get (id).getProductIds ();               
               if (productIds != null && !productIds.isEmpty ())
               {
                  for (Long pId : productIds)
                  {
                     if (publicData.getId ()==selectedUser.getId () || !publicData.containsProduct (pId))
                     {
                        selectedUser.addProduct (pId);
                     }
                  }
               }
            }
            else
            {
               selectedUser.removeCollection (id);
            } 
         }
      }     
      saveCollectionsScrollPosition ();
      saveProductsScrollPosition ();
      refreshCollections ();
      refreshProducts ();  
   }

   private static void checkAllProducts()
   {
      for (ProductData product : displayedProducts)
      {
         if (publicData.getId ()==selectedUser.getId () || !publicData.containsProduct (product.getId()))
         {
            if (productsCheckAll.getValue ())
            {
               selectedUser.addProduct (product.getId());
            }
            else
            {
               selectedUser.removeProduct (product.getId());
            }
         }
      }      
      saveProductsScrollPosition ();
      refreshProducts ();
   }
   
   private static void getProducts (final int start, final int length,
      final String search, final JavaScriptObject function)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      
      productService.count (search, null, new AsyncCallback<Integer> ()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
               "default");
            Window.alert ("There was an error while counting products");
         }

         @Override
         public void onSuccess (final Integer total)
         {
            productService.getProducts (start, length, search, null, 
               new AsyncCallback<List<ProductData>> ()
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
                  public void onSuccess (List<ProductData> products)
                  {
                     displayedProducts = products;
                     boolean allChecked = true;
                     boolean allPublic = true;
                     boolean children = true;
                     String json = "{\"aaData\": [";
                     for (ProductData product : products)
                     {         
                        boolean checked = (selectedUser != null && 
                                 selectedUser.containsProduct (product.getId ()));                        
                        allChecked = allChecked && checked;
                        boolean publicProduct = (publicData != null && publicData.getId ()!=selectedUser.getId () && publicData.containsProduct(product.getId ()));
                        allPublic = allPublic && publicProduct;
                        json += "[{\"checked\":"+checked+", \"publicData\":"+publicProduct+", \"id\":\""+product.getId()+"\" }, \"" + product.getIdentifier () + "\"],";
                     }
                     if (total > 0)
                     {
                        json = json.substring (0, json.length () - 1);
                     }
                     else
                     {
                        allChecked = false;
                        children = false;
                     }                     
                     json +=
                        "],\"iTotalRecords\" : " + total + ", \"iTotalDisplayRecords\" : " +
                           total + "}";

                     GWTClient.callback (function, JsonUtils.safeEval (json));
                     DOM.setStyleAttribute (RootPanel.getBodyElement (),
                        "cursor", "default");

                     productsCheckAll = SimpleCheckBox.wrap (RootPanel.get ("dr_productsCheckAll").getElement ());
                     productsCheckAll.setValue (allChecked);
                     productsCheckAll.setEnabled (children && state == State.EDIT && !allPublic);
                     productsCheckAll.setTitle (allPublic ? "All products are public" : "");
                  }
               });
         }
      });
   }
   
   private static void getCollections (final JavaScriptObject function)
   {
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
         
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      if (root == null)
      {
         root = new CollectionData();
         root.setId (null);
         root.setDeep (-1);
      }
      toRefresh = new ArrayList<Long> ();
      for (CollectionData c : displayedCollections.values ())
      {
         if (c.getDisplayedChildren () != null && c.getDisplayedChildren ().size () > 0)
         {
            toRefresh.add (c.getId ());
         }
      }
      displayedCollections.clear ();
      requestCollections(root, new AsyncCallback<Void>()
         {
            @Override
            public void onFailure (Throwable caught) 
            {
               Window.alert("There was an error while requesting collections.\n"+caught.getMessage ());
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
            }

            @Override
            public void onSuccess (Void result)
            {
               collectionsCheckAllChecked = true;
               collectionsAllPublic = true;
               String json = "{\"aaData\": [";
               json += computeJSON(root);
               boolean children = false;
               if (root.getDisplayedChildren () != null && root.getDisplayedChildren ().size () > 0)
               {
                  json = json.substring (0, json.length ()-1);   
                  children = true;
               }
               json +=
                  "],\"iTotalRecords\" : "+(children?1:0)+", \"iTotalDisplayRecords\" : "+(children?1:0)+"}";
               GWTClient.callback (function, JsonUtils.safeEval (json));
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
               
               collectionsCheckAll = SimpleCheckBox.wrap (RootPanel.get ("dr_collectionsCheckAll").getElement ());
               collectionsCheckAll.setValue (children && collectionsCheckAllChecked);
               collectionsCheckAll.setEnabled (children && state == State.EDIT && !collectionsAllPublic);
               collectionsCheckAll.setTitle (collectionsAllPublic ? "All collections are public" : "");
            }
         });      
   }
   
   private static void openCollection (int id)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      CollectionData parent = displayedCollections.get (new Long(id));      
      if (parent == null)
      {
         Window.alert("Error while opening collection #"+id);
         return;
      }
      if (parent.getDisplayedChildren () != null && parent.getDisplayedChildren ().size () > 0)
      {
         removeFromDisplayedCollections(parent.getDisplayedChildren ());
         parent.getDisplayedChildren ().clear ();
         displayedCollections.put (parent.getId(), parent);
         saveCollectionsScrollPosition ();
         refreshCollections ();
      }
      else
      {
         DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
         requestCollections(parent, new AsyncCallback<Void>()
         {
            @Override
            public void onFailure (Throwable caught) {
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
            }

            @Override
            public void onSuccess (Void result)
            {
               saveCollectionsScrollPosition ();
               refreshCollections ();
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
            }
         });
      }
   }
   
   private static void requestCollections(final CollectionData parent, final AsyncCallback<Void> callback)
   {
      collectionService.getSubCollectionsWithProductsIds (parent,
         new AsyncCallback<List<CollectionData>> ()
         {
            @Override
            public void onFailure (Throwable caught)
            {
               callback.onFailure (caught);
            }

            @Override
            public void onSuccess (List<CollectionData> collections)
            {
               parent.setDisplayedChildren (collections);
               displayedCollections.put (parent.getId(), parent);
               for (CollectionData collection : collections)
               {
                  // if already added, don't replace it
                  if (!displayedCollections.containsKey (collection.getId ()))
                  {
                     displayedCollections.put (collection.getId (), collection);
                  }
               }
               refreshEnded(parent, callback);
            }
         });
   }
   
   private static void refreshEnded(CollectionData refreshed, AsyncCallback<Void> callback)
   {      
      toRefresh.remove (refreshed.getId ());
      if (toRefresh.size () == 0)
      {
         callback.onSuccess (null);
      }
      else
      {
         CollectionData parent = displayedCollections.get(toRefresh.get (0));
         requestCollections(parent, callback);
      }      
   }
   
   private static String computeJSON(CollectionData collection)
   {
      String json = "";
      if (collection == null || collection.getDisplayedChildren () == null)
      {
         return "";
      }
      for (CollectionData col : collection.getDisplayedChildren ())
      {
         CollectionData c = displayedCollections.get (col.getId ());

         boolean checked = (selectedUser != null && 
                  selectedUser.containsCollection (c.getId ()));
         boolean publicCollection = (publicData != null && publicData.getId ()!=selectedUser.getId () && publicData.containsCollection(col.getId ()));
         collectionsCheckAllChecked = collectionsCheckAllChecked && checked;
         collectionsAllPublic = collectionsAllPublic && publicCollection;
         json +=
            "[{\"checked\":"+checked+", \"publicData\":"+publicCollection+", \"id\":"+c.getId ()+"},{\"name\":\""+c.getName ()+"\", \"id\":"+c.getId ()+", \"deep\":"+c.getDeep()+
            ", \"hasChildren\":"+c.hasChildren ()+", \"open\":"+
            (c.getDisplayedChildren () != null && c.getDisplayedChildren ().size () > 0)+"}],";
         if (c.hasChildren ())
         {
            json+=computeJSON(c);
         }
      }
      return json;
   }

   private static void removeFromDisplayedCollections(List<CollectionData> collections)
   {
      for (CollectionData col : collections)
      {
         displayedCollections.remove (col.getId());
         if (col.getDisplayedChildren () != null && col.getDisplayedChildren ().size () > 0)
         {
            removeFromDisplayedCollections(col.getDisplayedChildren ());
         }
      }
   }
   
   private enum State
   {
      NOTHING, EDIT;
   }
}
