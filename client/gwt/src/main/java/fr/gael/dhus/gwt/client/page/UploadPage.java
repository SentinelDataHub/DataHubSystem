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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;

import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.services.CollectionServiceAsync;
import fr.gael.dhus.gwt.services.UploadServiceAsync;
import fr.gael.dhus.gwt.share.CollectionData;
import fr.gael.dhus.gwt.share.FileScannerData;
import fr.gael.dhus.gwt.share.RoleData;

public class UploadPage extends AbstractPage
{
   private static CollectionServiceAsync collectionService = CollectionServiceAsync.Util.getInstance ();
   private static UploadServiceAsync uploadService = UploadServiceAsync.Util.getInstance ();
   
   private static HashMap<Long, CollectionData> displayedCollections = new HashMap<Long, CollectionData> ();
   private static CollectionData root;
   private static List<Long> selectedCollections = new ArrayList<Long> ();
   
   private static FormPanel uploadForm;   
   private static FileUpload uploadProductFile;
   private static RootPanel uploadButton;
   private static TextBox url;
   private static TextBox username;
   private static TextBox pattern;
   private static RootPanel patternResult;   
   private static PasswordTextBox password;
   private static RootPanel status;   
   private static RootPanel scanButton;   
   private static RootPanel stopButton;   
   private static RootPanel addButton;   
   private static RootPanel deleteButton;   
   private static RootPanel cancelButton;   
   private static RootPanel saveButton;   
   private static RootPanel refreshButton;
   private static RootPanel scannerInfos;
   private static SimpleCheckBox collectionsCheckAll;
   private static boolean collectionsTableDisabled = false;
   private static boolean collectionsCheckAllChecked;
   
   private static HashMap<Long, FileScannerData> displayedScanners = new HashMap<Long, FileScannerData> ();
   private static Integer editedScannerId = null;
   private static boolean newScannerEdited = false;

   private static State state;
   
   public UploadPage()
   {
      super.name = "Upload";  
      super.roles = Arrays.asList (RoleData.UPLOAD);
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.UploadPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.UploadPage::refresh()();
      }
   }-*/;
   
   private static native void refreshCollections()
   /*-{
       $wnd.upload_refreshCollections();
   }-*/;
   
   private static native void refreshScanners()
   /*-{
       $wnd.upload_refreshScanners();
   }-*/;
   
   private static native void refreshingCollections()
   /*-{
       $wnd.upload_refreshingCollections();
   }-*/;
   
   private static native void urlChanged()
   /*-{
       $wnd.upload_urlChanged();
   }-*/;
   
   private static native void productFileChanged()
   /*-{
       $wnd.upload_productFileChanged();
   }-*/;
   
   private static native void setCollectionsTableEnabled(boolean enabled)
   /*-{
       $wnd.upload_setCollectionsTableEnabled(enabled);
   }-*/;
   
   private static native void setScannersTableEnabled(boolean enabled)
   /*-{
       $wnd.upload_setScannersTableEnabled(enabled);
   }-*/;
      
   private static native void disableButtons()
   /*-{
       $wnd.upload_disableButtons();
   }-*/;
   
   private static native void exitEditMode()
   /*-{
       $wnd.upload_exitEditMode();
   }-*/;
   
   private static native void switchToEditMode(boolean running)
   /*-{
       $wnd.upload_switchToEditMode(running);
   }-*/;
   
   private static native void enableRefreshButton()
   /*-{
      $wnd.upload_enableRefreshButton();
   }-*/;
   
   private static native void saveScrollPosition()
   /*-{
       $wnd.upload_saveScrollPosition();
   }-*/;
   
   private static native void resetScrollPosition()
   /*-{
       $wnd.upload_resetScrollPosition();
   }-*/;
   
   private static native void showUpload()
   /*-{
      $wnd.showUpload(
         function ( sSource, aoData, fnCallback, oSettings ) {            
            @fr.gael.dhus.gwt.client.page.UploadPage::getCollections(*)
               (fnCallback)},
         function (event, id) {
             if (event.stopPropagation) {
                 event.stopPropagation();   // W3C model
             } else {
                 event.cancelBubble = true; // IE model
             }
             @fr.gael.dhus.gwt.client.page.UploadPage::openCollection(*)(id)
            },
         function (id) {
             @fr.gael.dhus.gwt.client.page.UploadPage::checkCollection(*)(id)
            },
         function () {
             @fr.gael.dhus.gwt.client.page.UploadPage::checkAllCollections(*)()
            },
         function ( sSource, aoData, fnCallback, oSettings ) {            
             @fr.gael.dhus.gwt.client.page.UploadPage::getScanners(*)
               (oSettings._iDisplayStart, oSettings._iDisplayLength, fnCallback)},
         function (id) {
             @fr.gael.dhus.gwt.client.page.UploadPage::removeScanner(*)(id)
            },
         function (data) {
            if (data == null) {
               @fr.gael.dhus.gwt.client.page.UploadPage::setDefaultState(*)()
            } else {
               @fr.gael.dhus.gwt.client.page.UploadPage::editScanner(*)(data[2])
            }},
         function (event, id) {
             if (event.stopPropagation) {
                 event.stopPropagation();   // W3C model
             } else {
                 event.cancelBubble = true; // IE model
             }
               @fr.gael.dhus.gwt.client.page.UploadPage::switchScannerActivation(*)(id)
            });
   }-*/;
   
   private static void refresh()
   {
      setState(State.DEFAULT);
   }
   
   private static void init()
   {
      showUpload();   

      uploadProductFile = FileUpload.wrap (RootPanel.get ("upload_productFile").getElement ());
      final Hidden collectionsField = Hidden.wrap (RootPanel.get ("upload_collections").getElement ());
      uploadButton = RootPanel.get ("upload_uploadButton");
      url = TextBox.wrap (RootPanel.get ("upload_url").getElement ());
      username = TextBox.wrap (RootPanel.get ("upload_username").getElement ());
      pattern = TextBox.wrap (RootPanel.get ("upload_pattern").getElement ());
      patternResult = RootPanel.get ("upload_patternResult");
      password = PasswordTextBox.wrap (RootPanel.get ("upload_password").getElement ());
      scanButton = RootPanel.get ("upload_scanButton");
      stopButton = RootPanel.get ("upload_stopButton");
      addButton = RootPanel.get ("upload_addButton");
      cancelButton = RootPanel.get ("upload_cancelButton");
      deleteButton = RootPanel.get ("upload_deleteButton");
      saveButton = RootPanel.get ("upload_saveButton");
      status = RootPanel.get("upload_status");
      refreshButton = RootPanel.get ("upload_refreshButton");
      scannerInfos = RootPanel.get("upload_scannerInfos");
      
      uploadForm = new FormPanel();
      uploadForm.setAction (GWT.getHostPageBaseURL () + "/api/upload");
      uploadForm.setEncoding (FormPanel.ENCODING_MULTIPART);
      uploadForm.setMethod (FormPanel.METHOD_POST);   
      
      uploadForm.setWidget (RootPanel.get ("upload_form"));
      RootPanel.get ("upload_product").add (uploadForm);
      
      selectedCollections = new ArrayList<Long> ();
      
      uploadButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (uploadButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            
            String filename = uploadProductFile.getFilename ();
            if (filename.length () == 0)
            {
               Window.alert ("No file selected!");
            }
            else
            {
               DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
                  "wait");
               String collections = "";
               
               if (selectedCollections != null && !selectedCollections.isEmpty ())
               {
                  for (Long cId : selectedCollections)
                  {
                     collections += cId + ",";
                  }
                  collections =
                      collections.substring (0, collections.length () - 1);
               }

               collectionsField.setValue (collections);

               uploadForm.submit ();
               setState(State.UPLOADING);
            }
         }
      }, ClickEvent.getType ());
      
      uploadForm.addSubmitCompleteHandler (new FormPanel.SubmitCompleteHandler ()
      {
         @Override
         public void onSubmitComplete (SubmitCompleteEvent event)
         {
            RegExp regexp = RegExp.compile (".*HTTP Status ([0-9]+).*");

            Integer errCode = null;
            try
            {
               errCode =
                  new Integer (regexp.exec (event.getResults ()).getGroup (1));
            }
            catch (Exception e) {}

            if (errCode == null)
            {
               Window.alert ("Your product has been successfully uploaded.");
            }
            else
            {
               switch (errCode)
               {
                  case 400:
                     Window
                        .alert ("Your request is missing a product file to upload.");
                     break;
                  case 403:
                     Window
                        .alert ("You are not allowed to upload a file on Sentinel Data Hub.");
                     break;
                  case 406:
                     Window
                        .alert ("Your product was not added. It can not be read by the system.");
                     break;
                  case 415:
                     Window
                        .alert ("Request contents type is not supported by the servlet.");
                     break;
                  case 500:
                     Window
                        .alert ("An error occurred while creating the file.");
                     break;
                  default:
                     Window
                        .alert ("There was an untraceable error while uploading your product.");
                     break;
               }
            }

            DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor",
               "default");
            setState(State.DEFAULT);
         }
      });
      
      addButton.addDomHandler (new ClickHandler ()
      {

         @Override
         public void onClick (ClickEvent event)
         {
            if (addButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            uploadService.addFileScanner (url.getValue (), username.getValue (),
               password.getValue (), pattern.getValue(), selectedCollections, new AsyncCallback<Long> ()
               {

                  @Override
                  public void onFailure (Throwable caught)
                  {
                     Window.alert ("There was an error during adding '" +
                        url.getValue () + "' to your file scanners.\n" + caught.getMessage ());
                  }

                  @Override
                  public void onSuccess (Long result)
                  {
                     setState (State.ADDING_FILESCANNER);
                     setState (State.DEFAULT);
                  }

               });
         }
      }, ClickEvent.getType ());
      
      scanButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (scanButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            if (state == State.EDIT_FILESCANNER && editedScannerId != null)
            {
               uploadService.updateFileScanner (new Long(editedScannerId.longValue ()), url.getValue (), username.getValue (),
                  password.getValue (), pattern.getValue(), selectedCollections, new AsyncCallback<Void> ()
                  {
                     @Override
                     public void onFailure (Throwable caught)
                     {
                        Window.alert ("There was an error during adding '" +
                           url.getValue () + "' to your file scanners.\n" + caught.getMessage ());
                     }

                     @Override
                     public void onSuccess (Void scanId)
                     {
//                        final String sUrl = url.getValue();
                        setState (State.DEFAULT);
                        uploadService.processScan (new Long(editedScannerId.longValue ()), new AsyncCallback<Void> ()
                           {
                              @Override
                              public void onFailure (Throwable caught)
                              {}
            
                              @Override
                              public void onSuccess (Void result)
                              {}
                           });
                     }
                  });
            }
            else
            {
            uploadService.addFileScanner (url.getValue (), username.getValue (),
               password.getValue (), pattern.getValue(), selectedCollections, new AsyncCallback<Long> ()
               {
                  @Override
                  public void onFailure (Throwable caught)
                  {
                     Window.alert ("There was an error during adding '" +
                        url.getValue () + "' to your file scanners.\n" + caught.getMessage ());
                  }

                  @Override
                  public void onSuccess (Long scanId)
                  {
//                     final String sUrl = url.getValue();
                     setState (State.ADDING_FILESCANNER);
                     setState (State.DEFAULT);
                     uploadService.processScan (scanId, new AsyncCallback<Void> ()
                        {
                           @Override
                           public void onFailure (Throwable caught)
                           {}
         
                           @Override
                           public void onSuccess (Void result)
                           {}
                        });
                  }
               });
            }
         }
      }, ClickEvent.getType ());
      
      stopButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (stopButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            uploadService.stopScan (new Long(editedScannerId.longValue ()), new AsyncCallback<Void> ()
               {
                  @Override
                  public void onFailure (Throwable caught)
                  {}

                  @Override
                  public void onSuccess (Void result)
                  {
                     setState (State.ADDING_FILESCANNER);
                     setState (State.DEFAULT);
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
            setDefaultState ();
         }
      }, ClickEvent.getType ());
      
      refreshButton.addDomHandler (new ClickHandler ()
      {

         @Override
         public void onClick (ClickEvent event)
         {
            if (refreshButton.getElement ().getClassName ().contains ("disabled"))
            {
               return;
            }
            refreshScanners ();
         }
      }, ClickEvent.getType ());
      
      deleteButton.addDomHandler (new ClickHandler ()
      {
         @Override
         public void onClick (ClickEvent event)
         {
            if (deleteButton.getElement ().getClassName ().contains ("disabled") && editedScannerId != null)
            {
               return;
            }
            removeScanner (editedScannerId);
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
            uploadService.updateFileScanner (new Long(editedScannerId), url.getValue (), username.getValue (),
               password.getValue (), pattern.getValue(), selectedCollections, new AsyncCallback<Void> ()
               {

                  @Override
                  public void onFailure (Throwable caught)
                  {
                     Window.alert ("There was an error while updating your file scanner.\n" + caught.getMessage ());
                  }

                  @Override
                  public void onSuccess (Void result)
                  {
                     setDefaultState ();
                  }

               });
         }
      }, ClickEvent.getType ());
            
      refresh();
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
         saveScrollPosition();
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
               saveScrollPosition();
               refreshCollections ();
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
            }
         });
      }
   }
   
   private static void getCollections (final JavaScriptObject function)
   {
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));         
      refreshingCollections();
      
      if (root == null)
      {
         root = new CollectionData();
         root.setId (null);
         root.setDeep (-1);
      }
      collectionsToRefresh = new ArrayList<Long> ();
      for (CollectionData c : displayedCollections.values ())
      {
         if (c.getDisplayedChildren () != null && c.getDisplayedChildren ().size () > 0)
         {
            collectionsToRefresh.add (c.getId ());
         }
      }
      displayedCollections.clear ();
      requestCollections(root, new AsyncCallback<Void>()
         {
            @Override
            public void onFailure (Throwable caught) 
            {
            }

            @Override
            public void onSuccess (Void result)
            {
               collectionsCheckAllChecked = true;
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
               collectionsCheckAll = SimpleCheckBox.wrap (RootPanel.get ("upload_collectionsCheckAll").getElement ());
               collectionsCheckAll.setValue (children && collectionsCheckAllChecked);
               collectionsCheckAll.setEnabled (children && !collectionsTableDisabled);
            }
         });      
   }   
   
   private static void checkCollection(int cId)
   {
      if (selectedCollections.contains (new Long(cId)))
      {
         selectedCollections.remove (new Long(cId));
      }
      else
      {
         selectedCollections.add (new Long(cId));
      }
      saveScrollPosition();  
      refreshCollections (); 
   }
   
   private static void checkAllCollections()
   {
      // remove root Collection
      Iterator<Long> idIterator = displayedCollections.keySet ().iterator ();
      Long[] cids = new Long[displayedCollections.size ()-1]; 
      for (int i = 0; idIterator.hasNext ();)
      {
         Long id = idIterator.next ();
         if (id != root.getId ())
         {
            cids[i] = id;
            i++;
         }
      }
      if (collectionsCheckAll.getValue ())
      {
         selectedCollections.addAll(Arrays.asList (cids));
      }
      else
      {
         selectedCollections.removeAll (Arrays.asList (cids));
      }    
      saveScrollPosition();  
      refreshCollections ();
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
   
   private static ArrayList<Long> collectionsToRefresh;
   
   private static void refreshEnded(CollectionData refreshed, AsyncCallback<Void> callback)
   {      
      collectionsToRefresh.remove (refreshed.getId ());
      if (collectionsToRefresh.size () == 0)
      {
         callback.onSuccess (null);
      }
      else
      {
         CollectionData parent = displayedCollections.get(collectionsToRefresh.get (0));
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
         boolean checked = selectedCollections.contains(c.getId ());
         collectionsCheckAllChecked = collectionsCheckAllChecked && checked;
         json +=
            "[{\"checked\":"+checked+", \"id\":"+c.getId ()+"},{\"name\":\""+c.getName ()+"\", \"id\":"+c.getId ()+", \"deep\":"+c.getDeep()+
            ", \"hasChildren\":"+c.hasChildren ()+", \"open\":"+
            (c.getDisplayedChildren () != null && c.getDisplayedChildren ().size () > 0)+"}],";
         if (c.hasChildren ())
         {
            json+=computeJSON(c);
         }
      }
      return json;
   }

   private static void getScanners (final int start, final int length,
      final JavaScriptObject function)
   {
      displayedScanners.clear ();
      GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
      
      uploadService.countFileScanners (new AsyncCallback<Integer>()
      {         
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert("There was an error while counting your saved file scanners.");
         }

         @Override
         public void onSuccess (final Integer total)
         {
            uploadService.getFileScanners (new AsyncCallback<List<FileScannerData>>()
            {
               @Override
               public void onFailure (Throwable caught)
               {
                  Window.alert("There was an error while getting your saved file scanners.");
               }
      
               @Override
               public void onSuccess (List<FileScannerData> result)
               {
                  String json = "{\"aaData\": [";
                  for (FileScannerData scanner : result)
                  {
                     displayedScanners.put (scanner.getId(), scanner);
                     String display = scanner.getUrl ().replace ("\\", "/");
                     if (scanner.getUsername () != null && !scanner.getUsername ().isEmpty ())
                     {
                        display += " - user: "+scanner.getUsername ();
                     }
                     json += "[{\"status\": \""+scanner.getStatus()+"\", \"label\":\""+display+ "\"}," +
                     		"{\"id\":"+scanner.getId ()+", \"active\":"+scanner.isActive ()+"},"+scanner.getId ()+"],";
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
            });
         }
      });
   }
   
   private static void removeScanner(int id)
   {
      uploadService.removeFileScanner (new Long(id), new AsyncCallback<Void>()
      {         
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert("There was an error while deleting your saved file scanner.");
         }

         @Override
         public void onSuccess (final Void result)
         {
            setDefaultState ();
         }
      });
   }
   
   private static void editScanner(int id)
   {
      editedScannerId = id;
      newScannerEdited = true;
      resetScrollPosition();
      setState(State.EDIT_FILESCANNER);
   }
   
   private static void switchScannerActivation(int id)
   {
      FileScannerData scan = displayedScanners.get (new Long(id));
      if (scan == null)
      {
         return;
      }
      
      uploadService.setFileScannerActive (new Long(id), !scan.isActive (),
         new AsyncCallback<Void> ()
         {
            @Override
            public void onFailure (Throwable caught)
            {
               Window.alert ("There was an error while updating your file scanner.\n" + caught.getMessage ());
            }

            @Override
            public void onSuccess (Void result)
            {
               state = null; // hack to reset page
               setDefaultState ();
            }
         });
   }
   
   private static void setDefaultState()
   {
      setState(State.DEFAULT);
   }
   
   private static void setState (State s)
   {  
      // disable scannerTable when uploading
      setScannersTableEnabled(!s.equals (State.UPLOADING));
      
      // changing state = reset every fields
      if (state != s) 
      {         
         if (s == State.DEFAULT)
         {
            resetScrollPosition();
         }
         if (s == State.UPLOADING)
         {
            uploadForm.reset ();
            productFileChanged();
         }
         // Reset after adding scanner
         if (s == State.ADDING_FILESCANNER)
         {
            password.setValue("");
            url.setValue("");
            username.setValue("");
            pattern.setValue("");
            status.getElement ().setInnerText ("");
            urlChanged();
         }
         // only refreshing scanners when not editing
         if (s != State.EDIT_FILESCANNER)
         {
            refreshScanners();
         }
         // Reset fields & collections when exiting edit mode
         if (state == State.EDIT_FILESCANNER)
         {
            password.setValue("");
            url.setValue("");
            username.setValue("");
            pattern.setValue("");
            status.getElement ().setInnerText ("");
            urlChanged();
            selectedCollections.clear ();
            exitEditMode ();
         }
      }
      boolean running = false;
      if (s == State.EDIT_FILESCANNER && newScannerEdited)
      {
         FileScannerData scanner = displayedScanners.get(new Long(editedScannerId));      
         running = scanner.getStatus () == "running";
         password.setValue(scanner.getPassword ());
         url.setValue(scanner.getUrl ());
         username.setValue(scanner.getUsername ());
         pattern.setValue(scanner.getPattern());
         status.getElement ().setInnerHTML (scanner.getStatusMessage ());
         urlChanged ();    
         selectedCollections = new ArrayList<Long> (scanner.getCollections ());
         switchToEditMode (scanner.getStatus () == "running");
         saveButton.getElement ().setClassName (scanner.getStatus () != "running" ? 
            "button_black":"button_disabled");
         scanButton.getElement ().setClassName (scanner.getStatus () != "running" ? 
            "button_black":"button_disabled");
         deleteButton.getElement ().setClassName (scanner.getStatus () != "running" ? 
            "button_black":"button_disabled");
         newScannerEdited = false;
      }
      
      uploadProductFile.setEnabled (s.equals (State.DEFAULT));
      password.setEnabled (s.equals (State.DEFAULT) || (s.equals (State.EDIT_FILESCANNER) && !running));
      url.setEnabled (s.equals (State.DEFAULT) || (s.equals (State.EDIT_FILESCANNER) && !running));
      username.setEnabled (s.equals (State.DEFAULT) || (s.equals (State.EDIT_FILESCANNER) && !running));
      pattern.setEnabled (s.equals (State.DEFAULT) || (s.equals (State.EDIT_FILESCANNER) && !running));

      collectionsTableDisabled = s.equals (State.UPLOADING);
      setCollectionsTableEnabled(!collectionsTableDisabled);  
      refreshCollections ();  

      if (s.equals (State.UPLOADING) || s.equals(State.ADDING_FILESCANNER))
      {
         disableButtons ();
      }
      else
      {
         enableRefreshButton();
      }
      
      uploadService.getNextScheduleFileScanner (new AsyncCallback<Date>()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            scannerInfos.getElement ().setInnerText ("An active file scanner means that it will be run every day.");            
         }

         @Override
         public void onSuccess (Date result)
         {
            DateTimeFormat sdf = DateTimeFormat.getFormat("EEEE dd MMMM yyyy - HH:mm:ss");
            scannerInfos.getElement ().setInnerText ("An active file scanner means that it will be run" +
            	" on "+sdf.format (result));    
         }         
      });
      
      state = s;
   }
   
   private enum State
   {
      DEFAULT, EDIT_FILESCANNER, UPLOADING, ADDING_FILESCANNER;
   }
}
