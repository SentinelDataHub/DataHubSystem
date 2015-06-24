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
import java.util.HashMap;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import fr.gael.dhus.gwt.share.RoleData;
import fr.gael.dhus.gwt.client.GWTClient;
import fr.gael.dhus.gwt.services.CollectionServiceAsync;
import fr.gael.dhus.gwt.services.ProductServiceAsync;
import fr.gael.dhus.gwt.share.MetadataIndexData;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.XMLNodeData;

public class SearchViewPage extends AbstractPage
{   
   private static ProductServiceAsync productService = ProductServiceAsync.Util.getInstance ();
   private static ProductData displayedProduct = null;
   private static HashMap<String, XMLNodeData> displayedNodes = new HashMap<String, XMLNodeData> ();
   private static HashMap<String, Object> pathCounter = new HashMap<String, Object> ();
   private static XMLNodeData root;   
   private static boolean initialized = false;
   private static boolean firstCallDone = false;
         
   private static CollectionServiceAsync collectionService = CollectionServiceAsync.Util.getInstance ();
   
   public SearchViewPage()
   {
      super.name = "SearchView";
   }
   
   @Override
   public native JavaScriptObject getJSInitFunction()
   /*-{
      return function() {
         @fr.gael.dhus.gwt.client.page.SearchViewPage::init()();
      }
   }-*/;
   
   @Override
   public native JavaScriptObject getJSRefreshFunction()
   /*-{
      return function() {      
         @fr.gael.dhus.gwt.client.page.SearchViewPage::refresh(*)();
      }
   }-*/;
      
   private static native void showSearchView()
   /*-{
      $wnd.showSearchView(
         function ( sSource, aoData, fnCallback, oSettings ) {            
            @fr.gael.dhus.gwt.client.page.SearchViewPage::getDrbTree(*)
               (fnCallback)},
         function (event, path, isLoadMoreNode) {
             if (event.stopPropagation) {
                 event.stopPropagation();   // W3C model
             } else {
                 event.cancelBubble = true; // IE model
             }
             @fr.gael.dhus.gwt.client.page.SearchViewPage::openItem(*)(path, isLoadMoreNode !== undefined)
            });
   }-*/;
   
   private static native void resetSearchView(String title)
   /*-{
      $wnd.resetSearchView(title);
   }-*/;

   private static native void setSearchViewFootprint(JavaScriptObject feature)
   /*-{
      $wnd.setSearchViewFootprint(feature);
   }-*/;
   
   private static native void hideSearchViewFootprint()
   /*-{
      $wnd.hideSearchViewFootprint();
   }-*/;

   private static native void setSearchViewQuicklook(String url)
   /*-{
      $wnd.setSearchViewQuicklook(url);
   }-*/;

   private static native void addInformationTab(String id, String data, boolean active)
   /*-{
      $wnd.addInformationTab(id, data, active);
   }-*/;
   
   private static native void refreshViewDrbTree()
   /*-{
       $wnd.refreshViewDrbTree();
   }-*/;
   
   public static native void openUrl(String url)
   /*-{
      return $wnd.open(url, 'target=_blank')
   }-*/;
      
   private static void refresh()
   {           
      resetSearchView("View product '"+displayedProduct.getIdentifier ()+"'");
      initialized = false;
      Double[][] footprint = displayedProduct.getFootprint ();

      if (footprint != null && footprint.length > 0)
      {                           
         JavaScriptObject footPrintJS = 
            ProductData.getJsFootprintLayer (footprint); 
         setSearchViewFootprint(footPrintJS);
      }
      else
      {
         hideSearchViewFootprint();
      }     
      
      if (displayedProduct.hasQuicklook ()) 
      {
         setSearchViewQuicklook(
            displayedProduct.getOdataQuicklookPath (GWT.getHostPageBaseURL ())); 
      }             

      String data = "";
      for (String summary : displayedProduct.getSummary ())
      {
         int idx = summary.indexOf (":");
         data += "<b>"+summary.substring (0, idx-1)+"</b>"+summary.substring (idx)+"</br>";
      }
      addInformationTab("Summary", data, true);
      
      for (MetadataIndexData mdi : displayedProduct.getIndexes ())
      {
         if (mdi.getName ().toLowerCase () != "summary")
         {
            data = "";
            for (MetadataIndexData m : mdi.getChildren ())
            {
               // Do not display footprints
               if ((m.getName ()!= null) && 
                   m.getName ().toLowerCase ().matches (".*footprint.*"))
               {
                  Log.debug ("Removing " + m.getName ());
                  continue;
               }
               
               data += "<b>"+m.getName ()+"</b> : "+m.getValue ()+"</br>";
            }       
            addInformationTab(mdi.getName ().substring (0, 1).toUpperCase ()+mdi.getName().substring (1), data, false);
         }
      }   
      
      displayedNodes.clear ();
      pathCounter.clear ();
      root = null;    
      refreshViewDrbTree ();
   }
   
   public static void viewProduct(final int id)
   {          
      productService.getProduct (new Long(id), new AsyncCallback<ProductData>()
      {
         @Override
         public void onFailure (Throwable caught)
         {
            Window.alert ("An error occured while getting product '#"+id+"'");
         }
         @Override
         public void onSuccess (ProductData product)
         {
            displayedProduct = product;
            Page.SEARCHVIEW.load();
         }
      });
   }
   
   private static void init()
   {      
      showSearchView();
      refresh();
   }
   
   private static void removeFromDisplayedNodes(List<XMLNodeData> collections)
   {
      for (XMLNodeData col : collections)
      {
         displayedNodes.remove (col.getPath());
         pathCounter.remove (col.getPath ());
         if (col.getDisplayedChildren () != null && col.getDisplayedChildren ().size () > 0)
         {
            removeFromDisplayedNodes(col.getDisplayedChildren ());
         }
      }
   }
   
   private static void openItem (String request, boolean loadMoreNodeClicked)
   {
      Log.debug ("DrbTree Request: " + request);
      XMLNodeData parent = displayedNodes.get (request); 
      if (parent == null)
      {
         Window.alert("Error while opening node '"+request+"'");
         return;
      }  
      Log.debug ("Found parent" + parent.getName ());
      // close item
      if (!loadMoreNodeClicked && 
               parent.getDisplayedChildren () != null && 
               parent.getDisplayedChildren ().size () > 0)
      {
         removeFromDisplayedNodes(parent.getDisplayedChildren ());
         parent.clearDisplayedChildren ();
         displayedNodes.put (parent.getPath(), parent);
         refreshViewDrbTree ();
      }
      // open item
      else
      {
         DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");
         if (!loadMoreNodeClicked && parent.getDisplayedChildren () != null && 
                  !parent.getDisplayedChildren ().isEmpty ())
         {
            refreshViewDrbTree ();
            DOM.setStyleAttribute (RootPanel.getBodyElement (),
               "cursor", "default");
            return;
         }
         requestXMLNode(parent, loadMoreNodeClicked, new AsyncCallback<Void>()
         {
            @Override
            public void onFailure (Throwable caught) {
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
            }

            @Override
            public void onSuccess (Void result)
            {
               refreshViewDrbTree ();
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
            }
         });
      }
   }
   
   private static void getDrbTree (final JavaScriptObject function)
   {
      if (! firstCallDone)
      {
         firstCallDone = true;
         GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
         generateDownloadLink();
         return;
      }
      if (! initialized)
      {
         initialized = true;
         GWTClient.callback (function, JsonUtils.safeEval ("{\"aaData\": [],\"iTotalRecords\" : 0, \"iTotalDisplayRecords\" : 0}"));
         generateDownloadLink();
      }
      
      DOM.setStyleAttribute (RootPanel.getBodyElement (), "cursor", "wait");          
      if (root == null)
      {
         root = new XMLNodeData(displayedProduct.getIdentifier (), "", "", 0);
         root.setDeep (-1);
      }
      
      if (root.getDisplayedChildren () != null && !root.getDisplayedChildren ().isEmpty ())
      {
         String json = "{\"aaData\": [";
         json += computeJSON(root);
         if (root.getDisplayedChildren () != null && root.getDisplayedChildren ().size () > 0)
         {
            json = json.substring (0, json.length ()-1);         
         }
         json +=
            "],\"iTotalRecords\" : 1, \"iTotalDisplayRecords\" : 1}";
         GWTClient.callback (function, JsonUtils.safeEval (json));
         DOM.setStyleAttribute (RootPanel.getBodyElement (),
            "cursor", "default");
         generateDownloadLink();
         return;
      }
      
      requestXMLNode(root, false, new AsyncCallback<Void>()
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
               if (root.getDisplayedChildren () != null && root.getDisplayedChildren ().size () > 0)
               {
                  json = json.substring (0, json.length ()-1);         
               }
               json +=
                  "],\"iTotalRecords\" : 1, \"iTotalDisplayRecords\" : 1}";
               GWTClient.callback (function, JsonUtils.safeEval (json));
               generateDownloadLink();
               DOM.setStyleAttribute (RootPanel.getBodyElement (),
                  "cursor", "default");
            }
         });      
   }
   
   private static void requestXMLNode(final XMLNodeData parent, boolean loadMoreNodeClicked, final AsyncCallback<Void> callback)
   {    
      getHTML(parent, loadMoreNodeClicked, new AsyncCallback<List<XMLNodeData>>()
      {

         @Override
         public void onFailure (Throwable caught)
         {
             callback.onFailure (caught);
         }

         @Override
         public void onSuccess (List<XMLNodeData> result)
         {
             parent.addDisplayedChildren (result);
             displayedNodes.put (parent.getPath(), parent);
             for (XMLNodeData node : result)
             {
                // if already added, don't replace it
                if (!displayedNodes.containsKey (node.getPath ()))
                {
                   displayedNodes.put (node.getPath (), node);
                }
             }
             callback.onSuccess (null);
         }
      
      }); 
      callback.onFailure(null);
   }
   
   private static String computeJSON(XMLNodeData node)
   {
      String json = "";
      if (node == null || node.getDisplayedChildren () == null)
      {
         return "";
      }
      for (XMLNodeData n : node.getDisplayedChildren ())
      {
         XMLNodeData child = displayedNodes.get (n.getPath ());
         if (child == null)
         {
            // not displayed child, go to next one
            continue;
         }
         json +=
            "[{\"name\":\""+child.getName ()+"\", \"path\":\""+child.getPath ().replace ("'", "&apos;")+"\", \"deep\":"+child.getDeep()+
            ", \"hasChildren\":"+!child.isLeaf ()+", \"open\":"+
            (child.getDisplayedChildren () != null && child.getDisplayedChildren ().size () > 0)+
              ", \"value\":\""+child.getValue ().trim().replace ("'", "").replace ("\"", "").replace("<","&lt;").replace ("}", "&#125;").replace ("{", "&#123;").replace (">", "&gt;")+"\"}],";
//         if (child.getAttributes () != null && !child.getAttributes ().isEmpty ())
//         {
//            String attributes = "";
//            for (XMLNodeAttribute attr : child.getAttributes ())
//            {
//               attributes += "@"+attr.getName ()+"="+
//               attr.getValue ().replace ("\"", "\\\"").replace("<","&lt;")
//               .replace (">", "&gt;")+", ";
//            }
//            if (child.getAttributes ().size () > 0)
//            {
//               attributes = attributes.substring (0, attributes.length () - 2);
//            }
//            json +=
//               "[{\"value\":\""+attributes+"\", \"deep\":"+(child.getDeep()+1)+
//               ", \"isAttribute\": true}],";
//         }
         if (!child.isLeaf ())
         {
            json+=computeJSON(child);
         }
      }    
      if (node.getLoadMoreRequest () != null && !node.getLoadMoreRequest ().isEmpty ())
      {
         json +=
            "[{\"name\":\"load more ("+(node.getChildrenNumber ()-node.getDisplayedChildren ().size ())+" left)\", \"path\":\""+node.getPath().replace ("'", "&apos;")+"\", \"deep\":"+(node.getDeep()+1)+
            ", \"hasChildren\":true, \"value\":\"\", \"open\":false, \"isLoadMoreNode\":true}, \"\"],";
      }  
      return json;
   }
   
   private static void getHTML(final XMLNodeData parent, boolean loadMoreNodeClicked, 
      final AsyncCallback<List<XMLNodeData>> callback) 
   {           
      String request =
         loadMoreNodeClicked ? parent.getLoadMoreRequest () : parent
            .getRequest ();
      String urlToRead = displayedProduct.getOdataPath (GWT.getHostPageBaseURL ()) + 
            "/" + request;
      urlToRead = URL.encode (urlToRead);
      urlToRead = urlToRead.replaceAll ("#", "%23");
      RequestBuilder builder =
         new RequestBuilder (RequestBuilder.GET, urlToRead);

      try
      {
         builder.sendRequest (null, new RequestCallback ()
         {

            @Override
            public void onResponseReceived (Request request, Response response)
            {
               Document doc = XMLParser.parse (response.getText ());

               ArrayList<XMLNodeData> xmlNodes = new ArrayList<XMLNodeData> ();

               NodeList list = doc.getFirstChild ().getChildNodes ();

               for (int listId = 0; listId < list.getLength (); listId++)
               {
                  if (list.item (listId).getNodeName () == "entry")
                  {
                     NodeList entryNodes = list.item (listId).getChildNodes ();
                     Integer childrenNumber = 0;
                     String name = null;
                     String value = "";
                     String path = parent.getPath ();
                     for (int entryNodeId = 0; entryNodeId < entryNodes.getLength (); entryNodeId++)
                     {
                        Node node = entryNodes.item (entryNodeId);
                        NodeList children = node.getChildNodes ();
                        if (node.getNodeName () == "title")
                        {
                           for (int childId = 0; childId < children.getLength (); childId++)
                           {
                              Node child = children.item(childId);
                              if (child.getNodeName () == "#text")
                              {
                                 name = child.getNodeValue ();
                              }
                           }
                        }                  
                        if (node.getNodeName () == "m:properties")
                        {
                           NodeList properties = node.getChildNodes ();
                           for (int propertyId = 0; propertyId < properties.getLength (); propertyId++)
                           {
                              Node property = properties.item (propertyId);
                              NodeList ns = property.getChildNodes ();
                              if (property.getNodeName () == "d:ChildrenNumber")
                              {
                                 for (int nId = 0; nId < ns.getLength (); nId++)
                                 {
                                    Node n = ns.item(nId);
                                    if (n.getNodeName () == "#text")
                                    {
                                       childrenNumber = new Integer(n.getNodeValue ());
                                    }
                                 }
                              }
                              if (property.getNodeName () == "d:Value")
                              {
                                 for (int nId = 0; nId < ns.getLength (); nId++)
                                 {
                                    Node n = ns.item(nId);
                                    if (n.getNodeName () == "#text")
                                    {
                                       value = n.getNodeValue () == null ? "" : n.getNodeValue ();
                                    }
                                 }
                              }
                           }
                        }
                     }
                     XMLNodeData xmlNode =
                        new XMLNodeData (name, value, (path.isEmpty () ? ""
                           : path + "/") + "Nodes('"+name+"')", childrenNumber);


                     if (pathCounter.containsKey (xmlNode.getPath ()))
                     {
                        Object item = pathCounter.get (xmlNode.getPath ());
                        int id;
                        if (item instanceof XMLNodeData)
                        {
                           XMLNodeData n = (XMLNodeData) item;
                           n.setPath ((path.isEmpty () ? ""
                              : path + "/") + "Nodes('"+name+"[" + 1 + "]')");
                           id = 2;
                        }
                        else
                        {
                           id =
                              (Integer) pathCounter.get (xmlNode.getPath ()) + 1;
                        }
                        pathCounter.put (xmlNode.getPath (), id);
                        xmlNode.setPath ((path.isEmpty () ? ""
                           : path + "/") + "Nodes('"+name+"[" + id + "]')");
                     }
                     else
                     {
                        pathCounter.put (xmlNode.getPath (), xmlNode);
                     }
                     xmlNode.setDeep (parent.getDeep () + 1);
                     xmlNodes.add (xmlNode);                        
                  }
               }

               callback.onSuccess (xmlNodes);
            }

            @Override
            public void onError (Request request, Throwable exception)
            {
               Window.alert (exception.getMessage ());
            }
         });
      }
      catch (RequestException e)
      {
         Window.alert (e.getMessage ());
         return;
      }
   }
   
   private static void generateDownloadLink()
   {
      Label downloadLink = Label.wrap (RootPanel.get ("searchView_download").getElement ());      
      if (GWTClient.getCurrentUser ().getRoles ().contains (RoleData.DOWNLOAD))
      {
         downloadLink.setText ("Download the product");
         downloadLink.getElement ().setClassName ("searchView_download");
         downloadLink.addClickHandler (new ClickHandler()
         {         
            @Override
            public void onClick (ClickEvent event)
            {
               openUrl (displayedProduct.getOdataDownaloadPath (
                  GWT.getHostPageBaseURL ()));
            }
         });
      }
      else
      {
         downloadLink.setText ("Explore the product");
         downloadLink.getElement ().setClassName ("");
      }
   }
}
