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
package fr.gael.dhus.olingo.v1.entity;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.processing.ProcessingUtils;
import fr.gael.dhus.network.RegulatedInputStream;
import fr.gael.dhus.network.TrafficDirection;
import fr.gael.dhus.olingo.Security;
import fr.gael.dhus.olingo.v1.Expander;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidTargetException;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.MediaResponseBuilder;
import fr.gael.dhus.olingo.v1.entityset.NodeEntitySet;
import fr.gael.dhus.olingo.v1.map.impl.NodesMap;
import fr.gael.dhus.util.DownloadActionRecordListener;
import fr.gael.dhus.util.DownloadStreamCloserListener;

import fr.gael.drb.DrbAttribute;
import fr.gael.drb.DrbAttributeList;
import fr.gael.drb.DrbFactory;
import fr.gael.drb.DrbNode;
import fr.gael.drb.impl.DrbNodeImpl;
import fr.gael.drb.impl.spi.DrbNodeSpi;
import fr.gael.drb.impl.xml.XmlFactory;
import fr.gael.drb.value.Value;
import fr.gael.drbx.cortex.DrbCortexModel;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.NavigationSegment;

/**
 * The OData representation of a DRB Node.
 */
public class Node extends Item implements Closeable
{
   private static final Logger LOGGER = LogManager.getLogger(Node.class);

   private final long ONE_YEAR_MS = (long)365.25*24*60*60*1000;

   protected DrbNode drbNode;
   private String path;
   private String contentType;
   private Long contentLength;
   private fr.gael.dhus.olingo.v1.entity.Class itemClass;

   private Map<String, Node> nodes;
   private Object value;
   private Map<String, Attribute> attributes;
   private Integer childrenNumber;

   public Node (String path)
   {
      super (getNodeId (path));
      this.path=path;
   }
   
   public Node (DrbNode node)
   {
      super (getNodeId (node));
      if (node == null)
         throw new NullPointerException ("Passed node cannot be null.");
      this.drbNode = node;
   }

   public void changeId (String id)
   {
      super.id = id;
   }
   
   private void initNode ()
   {
      if (this.drbNode == null)
      {
         if (path == null)
         {
            throw new NullPointerException ("Node path cannot be null.");
         }
         this.drbNode = DrbFactory.openURI (path);
         LOGGER.debug("Initialized node : " + path);
      }
      if (this.drbNode==null)
         throw new NullPointerException ("Node cannot be null");
   }
   

   @Override
   public String getName ()
   {
      initNode ();
      return drbNode.getName ();
   }

   @Override
   public String getContentType ()
   {
      initNode ();
      if (contentType == null)
      {
         try
         {
            DrbCortexModel model = DrbCortexModel.getDefaultModel ();
            contentType = model.getClassOf (drbNode).getLabel ();
         }
         catch (Exception e)
         {
            contentType = "Item";
         }
      }
      return contentType;
   }

   @Override
   public Long getContentLength ()
   {
      initNode ();
      if (contentLength == null)
      {
         contentLength = -1L;
         if (hasStream ())
         {
            InputStream stream = getStream ();
            if (stream instanceof FileInputStream)
            {
               try
               {
                  contentLength =
                     ((FileInputStream) stream).getChannel ().size ();
               }
               catch (IOException e)
               {
                  // Error while accessing file size: using -1L
               }
            }
            // Still not initialized ?
            if (contentLength == -1)
            {
               DrbAttribute attr = this.drbNode.getAttribute ("size");
               if (attr != null)
               {
                  try
                  {
                     contentLength = Long.decode (attr.getValue ().toString ());
                  }
                  catch (NumberFormatException nfe)
                  {
                     // Error in attribute...
                  }
               }
            }
         }
         else
         {
            contentLength = 0L;
         }
      }
      return contentLength;
   }

   @Override
   public Object navigate(NavigationSegment ns) throws ODataException
   {
      Object res;

      if (ns.getEntitySet().getName().equals(Model.NODE.getName()))
      {
         res = getNodes();
         if (!ns.getKeyPredicates().isEmpty())
         {
            res = ((NodesMap)res).get(
               ns.getKeyPredicates().get(0).getLiteral());
         }
      }
      else if (ns.getEntitySet().getName().equals(Model.ATTRIBUTE.getName()))
      {
         res = getAttributes();
         if (!ns.getKeyPredicates().isEmpty())
         {
            res = Map.class.cast(res).get(
               ns.getKeyPredicates().get(0).getLiteral());
         }
      }
      else if (ns.getEntitySet().getName().equals(Model.CLASS.getName()))
      {
         res = getItemClass();
      }
      else
      {
         throw new InvalidTargetException(this.getClass().getSimpleName(), ns.getEntitySet().getName());
      }

      return res;
   }

   public Integer getChildrenNumber ()
   {
      initNode ();
      if (childrenNumber == null)
      {
         childrenNumber = drbNode.getChildrenCount ();
      }
      return childrenNumber;
   }

   public Object getValue ()
   {
      initNode ();
      if (value == null)
      {
         Value val = drbNode.getValue ();
         String s_value = null;
         if (val != null) s_value = cleanInvalidXmlChars (val.toString (), "");
         value = s_value;
      }
      return value;
   }

   public Map<String, Node> getNodes ()
   {
      initNode ();
      if (nodes == null)
      {
         if (drbNode.hasChild ())
         {
            nodes = new NodesMap (drbNode);
         }
         else
         {
            nodes = Collections.emptyMap ();
         }
      }
      return nodes;
   }

   public Map<String, Attribute> getAttributes ()
   {
      initNode ();
      if (attributes == null)
      {
         DrbAttributeList attrs = drbNode.getAttributes ();
         Map<String, Attribute> attributes = new HashMap<String, Attribute> ();
         if ( (attrs != null) && (attrs.getLength () > 0))
         {
            for (int index = 0; index < attrs.getLength (); index++)
            {
               DrbAttribute attr = attrs.item (index);
               String value =
                  (attr.getValue () == null) ? "" : attr.getValue ()
                     .toString ();
               Attribute attribute = new Attribute(attr.getName(), value, null);
               attributes.put (attr.getName (), attribute);
            }
         }
         this.attributes =
            Collections.unmodifiableMap (new HashMap<String, Attribute> (
               attributes));
      }
      return attributes;
   }
   
   /**
    * Retrieve the Class from this Node entity.
    * @return the Class entity.
    * @throws UnsupportedOperationException if the model cannot be computed.
    * @throws NullPointerException if this product does not related any class.
    */
   @Override
   public fr.gael.dhus.olingo.v1.entity.Class getItemClass()
   {
      initNode ();
      if(this.itemClass==null)
      {
         try
         {
            itemClass = new fr.gael.dhus.olingo.v1.entity.Class(
               ProcessingUtils.getItemClassUri(
                  ProcessingUtils.getClassFromNode(drbNode)));
         }
         catch(Exception e)
         {
            //throw new UnsupportedOperationException("Cannot find Drb model.",e);
            // Item class not found: use drb root item URI.
            itemClass = new fr.gael.dhus.olingo.v1.entity.Class(
               "http://www.gael.fr/drb#item");
         }
      }
      return this.itemClass;
   }
   
   /**
    * Calls the superclass entity response not aggregated to this response.
    * @param root_url
    * @return the item class response.
    */
   protected Map<String, Object> itemToEntityResponse (String root_url)
   {
      return super.toEntityResponse (root_url);
   }

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      Map<String, Object> res = itemToEntityResponse  (root_url);
      
      initNode ();
      res.put (NodeEntitySet.CHILDREN_NUMBER, getChildrenNumber ());
      res.put (NodeEntitySet.VALUE, getValue ());
      res.put (NodeEntitySet.PATH, drbNode);

      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      initNode ();
      if (prop_name.equals (NodeEntitySet.CHILDREN_NUMBER))
         return getChildrenNumber ();

      if (prop_name.equals (NodeEntitySet.VALUE)) return getValue ();

      return super.getProperty (prop_name);
   }

   @Override
   public ODataResponse getEntityMedia (ODataSingleProcessor processor)
         throws ODataException
   {
      initNode ();
      if (hasStream ())
      {
         try
         {
            User u = Security.getCurrentUser();
            String user_name = (u == null ? null : u.getUsername ());
            
            InputStream is = new BufferedInputStream (getStream());
            
            RegulatedInputStream.Builder builder = 
               new RegulatedInputStream.Builder (is, TrafficDirection.OUTBOUND);
            builder.userName (user_name);

            CopyStreamAdapter adapter = new CopyStreamAdapter ();
            CopyStreamListener recorder = new DownloadActionRecordListener (
                  this.getId (), this.getName (), u);
            CopyStreamListener closer = new DownloadStreamCloserListener (is);
            adapter.addCopyStreamListener (recorder);
            adapter.addCopyStreamListener (closer);
            builder.copyStreamListener (adapter);
            if (getContentLength ()>0) builder.streamSize(getContentLength());
            is = builder.build();

            String etag = getName () + "-" + getContentLength ();

            // A priori Node never change, so the lastModified should be as
            // far as possible than today.
            long last_modified =  System.currentTimeMillis () - ONE_YEAR_MS;

            // If node is not a data file, it cannot be downloaded and set to -1
            // As a stream exists, this control is probably obsolete.
            long content_length = getContentLength ()==0?-1:getContentLength ();

            return MediaResponseBuilder.prepareMediaResponse(etag, getName(),
               getContentType (), last_modified, content_length,
               processor.getContext (), is);
         }
         catch (Exception e)
         {
            throw new ODataException (
               "An exception occured while creating the stream for node " + 
               getName(), e);
         }
      }
      else
      {
         throw new ODataException ("No stream for node " + getName ());
      }
   }

   public boolean hasStream ()
   {
      initNode ();
      if (drbNode instanceof DrbNodeSpi)
      {
         return ((DrbNodeSpi) drbNode).hasImpl (InputStream.class);
      }
      return false;
   }

   public InputStream getStream ()
   {
      initNode ();
      if (drbNode instanceof DrbNodeSpi)
      {
         return (InputStream) ((DrbNodeSpi) drbNode)
            .getImpl (InputStream.class);
      }
      return null;
   }

   public InputStream toXML ()
   {
      initNode ();
      ByteArrayOutputStream out = new ByteArrayOutputStream ();
      XmlFactory.writeXML (drbNode, out);
      return new ByteArrayInputStream (out.toByteArray ());
   }

   /**
    * Computes the unique identifier within this parent node context. The
    * retrieved identifier is computed from the XPath that includes the name of
    * this node, and the occurrence of this node within the parent if any.
    * 
    * @param node this node to compute the id.
    * @return the unique identifier within this node.
    */
   public static String getNodeId (String path)
   {
      String id = path;
      if (id.endsWith ("/")) id = id.substring (0, id.length () - 1);
      if (id.contains ("/"))
      {
         id = id.substring (id.lastIndexOf ("/") + 1);
      }
      if ( (id == null) || "".equals (id)) id = (new File (path)).getName ();
      return id;
   }
   
   public static String getNodeId (DrbNode node)
   {
      return getNodeId (node.getPath ());
   }

   /**
    * From xml spec valid chars:<br>
    * #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]<br>
    * any Unicode character, excluding the surrogate blocks, FFFE, and FFFF.<br>
    * 
    * @param text The String to clean
    * @param replacement The string to be substituted for each match
    * @return The resulting String
    */
   public static String cleanInvalidXmlChars (String text, String replacement)
   {
      String re =
         "[^\\x09\\x0A\\x0D\\x20-\\xD7FF\\xE000-\\xFFFD\\x10000-x10FFFF]";
      return text.replaceAll (re, replacement);
   }

   @Override
   public void close () throws IOException
   {
      if (this.drbNode == null)
         return;

      if (this.drbNode instanceof DrbNodeImpl)
      {
         DrbNodeImpl.class.cast(this.drbNode).close(true);
      }
   }

   @Override
   public List<String> getExpandableNavLinkNames()
   {
      // Node inherits from Item
      List<String> res = new ArrayList<>(super.getExpandableNavLinkNames());
      res.add("Attributes");
      res.add("Nodes");
      return res;
   }

   @Override
   public List<Map<String, Object>> expand(String navlink_name, String self_url)
   {
      switch(navlink_name)
      {
         case "Attributes":
            return Expander.mapToData(getAttributes(), self_url);
         case "Nodes":
            return Expander.mapToData(getNodes(), self_url);
         default:
            return super.expand(navlink_name, self_url);
      }
   }
}
