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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;

import fr.gael.dhus.olingo.v1.entitySet.NodeEntitySet;
import fr.gael.dhus.olingo.v1.map.impl.NodesMap;
import fr.gael.drb.DrbAttribute;
import fr.gael.drb.DrbAttributeList;
import fr.gael.drb.DrbFactory;
import fr.gael.drb.DrbNode;
import fr.gael.drb.impl.spi.DrbNodeSpi;
import fr.gael.drb.impl.xml.XmlFactory;
import fr.gael.drb.value.Value;
import fr.gael.drbx.cortex.DrbCortexModel;

/**
 * Node Bean.
 */
public class Node extends Item
{
   private DrbNode drbNode;
   private String path;
   private String contentType;
   private Long contentLength;

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
               Attribute attribute = new Attribute (attr.getName (), value);
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
   public Object getProperty (String propName) throws ODataException
   {
      initNode ();
      if (propName.equals (NodeEntitySet.CHILDREN_NUMBER))
         return getChildrenNumber ();

      if (propName.equals (NodeEntitySet.VALUE)) return getValue ();

      return super.getProperty (propName);
   }

   @Override
   public ODataResponse getEntityMedia (ODataSingleProcessor processor)
      throws ODataException
   {
      initNode ();
      ODataResponse rsp = null;
      if (hasStream ())
      {
         rsp = ODataResponse.entity (getStream ())
            .header ("Content-Type", getContentType ())
            .header ("Content-Length", "" +  getContentLength())
            .header ("Content-Disposition", "inline; filename=" + getName ())
            .build ();
      }
      else
      {
         throw new ODataException ("No stream for node " + getName ());
         // rsp = ODataResponse.entity (n.toXML ())
         // .header ("Content-Type", n.getContentType ())
         // .header ("Content-Length", ""+ n.getContentLength ())
         // .header ("Content-Disposition", "inline; filename=" +
         // n.getName ()+".xml")
         // .build ();
      }
      return rsp;
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
      if ( (id == null) || "".equals (id)) id = (new File(path)).getName ();
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
}
