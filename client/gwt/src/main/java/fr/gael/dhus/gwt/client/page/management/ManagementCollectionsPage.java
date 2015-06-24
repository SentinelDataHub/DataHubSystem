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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.client.page.AbstractPage;
import fr.gael.dhus.gwt.services.CollectionServiceAsync;
import fr.gael.dhus.gwt.services.ProductServiceAsync;
import fr.gael.dhus.gwt.share.CollectionData;
import fr.gael.dhus.gwt.share.ProductData;

public class ManagementCollectionsPage extends AbstractPage
{
   private static CollectionServiceAsync collectionService = CollectionServiceAsync.Util.getInstance ();
   private static ProductServiceAsync productService = ProductServiceAsync.Util.getInstance ();
   
   private static CollectionData selectedCollection;
   private static List<ProductData> displayedProducts = new ArrayList<ProductData> ();
   private static HashMap<Long, CollectionData> displayedCollections = new HashMap<Long, CollectionData> ();
   private static CollectionData root;
   
   private static TextBox name;
   private static TextArea description;
   private static TextBox parent;
   private static RootPanel createButton;
   private static RootPanel createSubButton;
   private static RootPanel resetButton;
   private static RootPanel saveButton;
   private static RootPanel updateButton;
   private static RootPanel deleteButton;
   private static RootPanel cancelButton;
   private static SimpleCheckBox productsCheckAll;
   
   private static boolean productsCheckAllDisabled = false;

   private static State state;
   
   public ManagementCollectionsPage()
   {
      // name is automatically prefixed in JS by "management_"
      super.name = "Collections";
      super.roles = Arrays.asList (RoleData.DATA_MANAGER);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage::refresh()();
      }
   }-*/;
   
   @Override
   public void load()
   {
      // This page can only be loaded from Management Page
   }
   
   private static native void refreshProducts()
   /*-{
       $wnd.coll_refreshProducts();
   }-*/;
   
   private static native void refreshCollections()
   /*-{
       $wnd.coll_refreshCollections();
   }-*/;

   private static native void deselectCollection()
   /*-{
       $wnd.coll_deselectCollection();
   }-*/;
   
   private static native void hideCollectionsCustomValidity()
   /*-{
       $wnd.coll_hideCollectionsCustomValidity();
   }-*/;
   
   private static native void setCollectionsTableEnabled(boolean enabled)
   /*-{
       $wnd.coll_setCollectionsTableEnabled(enabled);
   }-*/;
   
   private static native void setProductsTableEnabled(boolean enabled)
   /*-{
       $wnd.coll_setProductsTableEnabled(enabled);
   }-*/;
   
   private static native void saveScrollPosition()
   /*-{
       $wnd.coll_saveScrollPosition();
   }-*/;
   
   private static native void resetScrollPosition()
   /*-{
       $wnd.coll_resetScrollPosition();
   }-*/;
   
   private static native void showCollectionManagement()
   /*-{
      $wnd.showCollectionManagement(
         function ( sSource, aoData, fnCallback, oSettings ) {
            @fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage::getProducts(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, 
               oSettings.oPreviousSearch.sSearch, fnCallback)},
         function ( sSource, aoData, fnCallback, oSettings ) {            
            @fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage::getCollections(*)
               (fnCallback)},
         function (data) {
            if (data == null) {
               @fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage::setNothingState(*)()
            } else {
               @fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage::edit(*)(data[0].id)
            }},
         function (authority) {
               @fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage::checkProduct(*)(authority)
            },
         function () {
             @fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage::checkAllProducts(*)()
            },
         function (event, id) {
             if (event.stopPropagation) {
                 event.stopPropagation();   // W3C model
             } else {
                 event.cancelBubble = true; // IE model
             }
             @fr.gael.dhus.gwt.client.page.management.ManagementCollectionsPage::openCollection(*)(id)
            }
         );
   }-*/;
   
   @Override
   public void refreshMe() 
   {
      refresh();
   }
   
   private static void refresh()
   {
      refreshCollections ();
      refreshProducts ();
      setNothingState ();
   }

   private static void init()
   {
      showCollectionManagement();
            
      name = TextBox.wrap (RootPanel.get ("managementCollection_name").getElement ());
      description = TextArea.wrap (RootPanel.get ("managementCollection_description").getElement ());
      parent = TextBox.wrap (RootPanel.get ("managementCollection_parent").getElement ());
      createButton = RootPanel.get ("managementCollection_buttonCreate");
      createSubButton = RootPanel.get ("managementCollection_buttonCreateSub");
      resetButton = RootPanel.get ("managementCollection_buttonReset");
      saveButton = RootPanel.get ("managementCollection_buttonSave");
      updateButton = RootPanel.get ("managementCollection_buttonUpdate");
      deleteButton = RootPanel.get ("managementCollection_buttonDelete");
      cancelButton = RootPanel.get ("managementCollection_buttonCancel");
            
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
            CollectionData parent = selectedCollection;
            selectedCollection = new CollectionData ();
            selectedCollection.setParent (parent);
            setState (State.CREATE, true);
         }
      }, ClickEvent.getType ());
      createSubButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (createSubButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            CollectionData parent = selectedCollection;
            selectedCollection = new CollectionData ();
            selectedCollection.setParent (parent);
            setState (State.CREATESUB, true);
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
            setState (state, true);
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
            Long selectedId = selectedCollection.getId();
            List<CollectionData> selectedDisplayedChildren = selectedCollection.getDisplayedChildren ();
            disableAll ();
            displayedCollections.remove (selectedId);
            if (selectedDisplayedChildren != null && selectedDisplayedChildren.size () > 0)
            {
               removeFromDisplayedCollections(selectedDisplayedChildren);
            }
            
            collectionService.deleteCollection (selectedId,
               new AsyncCallback<Void> ()
               {

                  @Override
                  public void onFailure (Throwable caught)
                  {
                     Window.alert ("Cannot delete collection.\n" +
                        caught.getMessage ());
                     setNothingState ();
                     refreshCollections ();
                  }

                  @Override
                  public void onSuccess (Void result)
                  {
                     setNothingState ();
                     refreshCollections ();
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

   private static void deselect()
   {
      selectedCollection = null;
      productsCheckAll.setValue (false);
      resetScrollPosition ();
      deselectCollection ();
   }
   
   private static void setNothingState ()
   {
      boolean deselect = selectedCollection != null;
      if (deselect && state != State.CREATESUB && state != State.CREATE)
      {
         deselect();
         setState (State.NOTHING, true);
      }
      else if (state == State.CREATE)
      {
         selectedCollection = null;
         setState (State.NOTHING, true);
      }
      else
      {
         setState (State.NOTHING, true);
      }
   }

   private static void edit (final int collectionId)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      
      collectionService.getProductIds (new Long(collectionId),
         new AsyncCallback<List<Long>> ()
         {

            @Override
            public void onFailure (Throwable caught)
            {
               Window
                  .alert ("Cannot load products of selected collection.\n" +
                     caught.getMessage ());
               setNothingState ();
            }

            @Override
            public void onSuccess (final List<Long> productIds)
            {                     
               selectedCollection = displayedCollections.get (new Long(collectionId)).copy ();
               selectedCollection.setProductIds (productIds);

               resetScrollPosition ();
               setState (State.EDIT, true);
               
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");                  
         }
      });
   }

   private static void save (final boolean create)
   {
      CollectionData toSave =
         selectedCollection != null ? selectedCollection.copy ()
            : new CollectionData ();
      toSave.setName (name.getValue ());
      toSave.setDescription (description.getValue ());

      if (toSave.getName () == null || toSave.getName ().trim ().isEmpty ())
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
            Window.alert ("Cannot save collection.\n" + caught.getMessage ());
            setState (create ? State.CREATE : State.EDIT, false);
         }

         @Override
         public void onSuccess (Void result)
         {
            setNothingState ();
            refreshCollections ();
         }
      };
      if (create)
      {
         collectionService.createCollection (toSave, callback);
      }
      else
      {
         collectionService.updateCollection (toSave, callback);
      }
   }

   private static void setState (State s, boolean setValue)
   {
      state = s;
      boolean updatable =
         state == State.EDIT && selectedCollection != null &&
            selectedCollection.getName () != null &&
            !selectedCollection.getName ().trim ().isEmpty ();

      // Trees & Datagrids
      setCollectionsTableEnabled (state == State.NOTHING || state == State.EDIT);
      setProductsTableEnabled (state == State.EDIT || state == State.CREATE ||
         state == State.CREATESUB);

      // Enable Buttons
      cancelButton.getElement ().setClassName ("button_black");
      createButton.getElement ().setClassName ("button_black");
      createSubButton.getElement ().setClassName ("button_black");
      deleteButton.getElement ().setClassName ("button_black");
      resetButton.getElement ().setClassName ("button_black");
      saveButton.getElement ().setClassName ("button_disabled");
      updateButton.getElement ().setClassName (updatable?"button_black":"button_disabled");

      // Buttons Visibility
      cancelButton.setVisible (state == State.EDIT || state == State.CREATE ||
         state == State.CREATESUB);
      createButton.setVisible (state == State.NOTHING || state == State.EDIT);
      createSubButton.setVisible (state == State.EDIT);
      deleteButton.setVisible (state == State.EDIT);
      resetButton.setVisible (state == State.CREATE || state == State.CREATESUB);
      saveButton.setVisible (state == State.CREATE || state == State.CREATESUB);
      updateButton.setVisible (state == State.EDIT);

      // Enable Fields
      name.setEnabled (state == State.EDIT || state == State.CREATE ||
         state == State.CREATESUB);
      description.setEnabled (state == State.EDIT || state == State.CREATE ||
         state == State.CREATESUB);
      parent.setEnabled (false);
      productsCheckAllDisabled = false;

      // Fields Value
      if (setValue)
      {
         name
            .setValue ( (state == State.EDIT && selectedCollection != null) ? selectedCollection
               .getName () : "");
         description
            .setValue ( (state == State.EDIT && selectedCollection != null) ? selectedCollection
               .getDescription () : "");
         parent
            .setValue ( ( (state == State.EDIT || state == State.CREATE || state == State.CREATESUB) &&
               selectedCollection != null && selectedCollection.getParent () != null) ? selectedCollection
               .getParent ().getName () : "");
         
         refreshProducts ();
      }
      
      // hack to enable Save button if needed by revalidating fields.
      name.setFocus (true);
      name.setFocus (false);
      hideCollectionsCustomValidity();
      name.setFocus (state == State.CREATE || state == State.CREATESUB);
   }

   private static void disableAll ()
   {
      selectedCollection = null;
      setCollectionsTableEnabled (false);
      setProductsTableEnabled (false);
      productsCheckAll.setEnabled (false);
      productsCheckAllDisabled = true;

      saveButton.getElement ().setClassName ("button_disabled");
      resetButton.getElement ().setClassName ("button_disabled");
      createButton.getElement ().setClassName ("button_disabled");
      createSubButton.getElement ().setClassName ("button_disabled");
      updateButton.getElement ().setClassName ("button_disabled");
      deleteButton.getElement ().setClassName ("button_disabled");
      cancelButton.getElement ().setClassName ("button_disabled");

      name.setEnabled (false);
      parent.setEnabled (false);
      description.setEnabled (false);
   }
   
   private static void openCollection (int id)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      deselect();
      setState (State.NOTHING, true);
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
               refreshCollections ();
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
            }
         });
      }
   }
   
   private static void getCollections (final JavaScriptObject function)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      
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
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
            }

            @Override
            public void onSuccess (Void result)
            {
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
            }
         });      
   }
   
   private static void requestCollections(final CollectionData parent, final AsyncCallback<Void> callback)
   {
      collectionService.getSubCollections (parent,
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
   
   private static ArrayList<Long> toRefresh;
   
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
         json +=
            "[{\"name\":\""+c.getName ()+"\", \"id\":"+c.getId ()+", \"deep\":"+c.getDeep()+
            ", \"hasChildren\":"+c.hasChildren ()+", \"open\":"+
            (c.getDisplayedChildren () != null && c.getDisplayedChildren ().size () > 0)+"}],";
         if (c.hasChildren ())
         {
            json+=computeJSON(c);
         }
      }
      return json;
   }
   
   private static void checkProduct(int pId)
   {
      Long productId = new Long(pId);
      if (selectedCollection != null &&
         selectedCollection.contains (productId))
      {
         selectedCollection.removeProduct (productId);
      }
      else
      {
         selectedCollection.addProduct (productId);
      }       
      saveScrollPosition();
      refreshProducts ();  
   }
   
   private static void checkAllProducts()
   {
      Long[] pids = new Long[displayedProducts.size ()];
      int i = 0;
      for (ProductData product : displayedProducts)
      {
         pids[i] = product.getId ();
         i++;
      }

      if (productsCheckAll.getValue ())
      {
         selectedCollection.addProducts (pids);
      }
      else
      {
         selectedCollection.removeProducts (pids);
      }      
      saveScrollPosition();
      refreshProducts ();
   }
   
   private static void getProducts (final int start, final int length,
      final String search, final JavaScriptObject function)
   {
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
      final Long parentId = selectedCollection != null  && selectedCollection.getParent() != null ? selectedCollection.getParent ().getId () : null;
      
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      productService.count (search, parentId, new AsyncCallback<Integer> ()
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
            //selectedCollection.getParent ().getId ()
            productService.getProducts (start, length, search, parentId, 
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
                     String json = "{\"aaData\": [";
                     for (ProductData product : products)
                     {         
                        boolean checked = (selectedCollection != null && selectedCollection.contains (product.getId ()));
                        allChecked = allChecked && checked;
                        json += "[{\"checked\":"+checked+", \"id\":\""+product.getId()+"\" }, \"" + product.getIdentifier () + "\"],";
                     }
                     if (total > 0)
                     {
                        json = json.substring (0, json.length () - 1);
                     }
                     else
                     {
                        allChecked = false;
                        productsCheckAllDisabled = true;
                     }                     
                     json +=
                        "],\"iTotalRecords\" : " + total + ", \"iTotalDisplayRecords\" : " +
                           total + "}";

                     GWTClient.callback (function, JsonUtils.safeEval (json));
                     DOM.setStyleAttribute (RootPanel.getBodyElement (),
                        "cursor", "default");
                     
                     productsCheckAll = SimpleCheckBox.wrap (RootPanel.get ("productsCheckAll").getElement ());
                     productsCheckAll.setValue (allChecked);
                     productsCheckAll.setEnabled (!productsCheckAllDisabled && 
                        (state == State.EDIT || state == State.CREATE ||
                        state == State.CREATESUB));
                  }
               });
         }
      });
   }
   
   private enum State
   {
      NOTHING, EDIT, CREATESUB, CREATE;
   }
}
