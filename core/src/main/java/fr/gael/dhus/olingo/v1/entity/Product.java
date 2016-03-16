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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import fr.gael.dhus.util.DownloadStreamCloserListener;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.w3c.dom.Document;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.processing.ProcessingUtils;
import fr.gael.dhus.network.RegulatedInputStream;
import fr.gael.dhus.network.TrafficDirection;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.V1Util;
import fr.gael.dhus.olingo.v1.entityset.NodeEntitySet;
import fr.gael.dhus.olingo.v1.entityset.ProductEntitySet;
import fr.gael.dhus.service.EvictionService;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.SecurityService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.DownloadActionRecordListener;
import fr.gael.dhus.util.MetalinkBuilder;
import fr.gael.drb.DrbNode;

/**
 * A OData representation of a DHuS Product.
 */
public class Product extends Node
{
   private static final Logger LOGGER = Logger.getLogger (Product.class);

   private static final EvictionService EVICTION_SERVICE =
      ApplicationContextProvider.getBean (EvictionService.class);

   private static final ProductService PRODUCT_SERVICE =
      ApplicationContextProvider.getBean (ProductService.class);

   /** To get the path of the incoming directory, to make localPaths. */
   private static final ConfigurationManager CONFIG_MGR =
      ApplicationContextProvider.getBean (ConfigurationManager.class);

   /** Provides access to the user's roles (Expose more infos to admins). */
   private static final SecurityService SECURITY_SERVICE =
      ApplicationContextProvider.getBean (SecurityService.class);

   protected final fr.gael.dhus.database.object.Product product;

   protected Map<String, Node> nodes;

   protected Map<String, Attribute> attributes;

   private Map<String, Product> products;

   public Product (fr.gael.dhus.database.object.Product product)
   {
      super (product.getPath ().toString ());
      this.product = product;
   }

   /**
    * Retrieve the Class from this product entity.
    *
    * @return the DrbCortex class name.
    * @throws UnsupportedOperationException if the model cannot be computed.
    * @throws NullPointerException if this product does not related any class.
    */
   @Override
   public fr.gael.dhus.olingo.v1.entity.Class getItemClass ()
   {
      // Case of ingestion performed before DHuS 0.4.4
      if (product.getItemClass () == null)
      {
         try
         {
            return new fr.gael.dhus.olingo.v1.entity.Class (
               ProcessingUtils.getItemClassUri (ProcessingUtils
                  .getClassFromProduct (this.product)));
         }
         catch (Exception e)
         {
            throw new UnsupportedOperationException ("Cannot find product.", e);
         }
      }
      return new fr.gael.dhus.olingo.v1.entity.Class (product.getItemClass ());
   }

   @Override
   public String getId ()
   {
      return product.getUuid ();
   }

   @Override
   public String getName ()
   {
      return product.getIdentifier ();
   }

   @Override
   public String getContentType ()
   {
      return "application/octet-stream";
   }

   @Override
   public Long getContentLength ()
   {
      return product.getDownload ().getSize ();
   }

   @Override
   public Integer getChildrenNumber ()
   {
      int number = 0;
      if (this.product != null)
      {
         if (this.product.getQuicklookFlag ()) number++;
         if (this.product.getThumbnailFlag ()) number++;
      }
      return number;
   }

   @Override
   public Object getValue ()
   {
      return null;
   }

   public Date getIngestionDate ()
   {
      return product.getIngestionDate ();
   }

   public Date getEvictionDate ()
   {
      // dynamic date
      return EVICTION_SERVICE.getEvictionDate (product.getId ());
   }

   public Date getCreationDate ()
   {
      return product.getCreated ();
   }

   public String getGeometry ()
   {
      return product.getFootPrint ();
   }

   public Date getContentStart ()
   {
      return product.getContentStart ();
   }

   public Date getContentEnd ()
   {
      return product.getContentEnd ();
   }

   public boolean hasChecksum ()
   {
      return ! (product.getDownload ().getChecksums ().isEmpty ());
   }

   public String getChecksumAlgorithm ()
   {
      if ( ! (hasChecksum ())) return null;

      Map<String, String> checksum = product.getDownload ().getChecksums ();
      String algorithm = "MD5";
      if (checksum.get (algorithm) != null) return algorithm;
      return checksum.keySet ().iterator ().next ();
   }

   public String getChecksumValue ()
   {
      if ( ! (hasChecksum ())) return null;
      return product.getDownload ().getChecksums ()
         .get (getChecksumAlgorithm ());
   }

   /**
    * This product requires system controls (statistics/quotas)
    *
    * @return true is control is required, false otherwise.
    */
   public boolean requiresControl ()
   {
      // TODO This method shall be replaced by RABAC mechanism
      return true;
   }

   // Getters
   public Map<String, Product> getProducts ()
   {
      if (this.products == null)
      {
         Map<String, Product> products = new LinkedHashMap<String, Product> ();
         if (this.product.getQuicklookFlag ())
         {
            products.put ("Quicklook", new QuicklookProduct (product));
         }

         if (this.product.getThumbnailFlag ())
         {
            products.put ("Thumbnail", new ThumbnailProduct (product));
         }
         this.products = products;
      }
      return products;
   }

   @Override
   public Map<String, Node> getNodes ()
   {
      if (this.nodes == null)
      {
         this.nodes = new LinkedHashMap<String, Node> ();
         DrbNode product_node =
            ProcessingUtils.getNodeFromPath (product.getPath ().getPath ());
         if (product_node == null)
            throw new NullPointerException ("Cannot compute DRB node from " +
               product.getPath ().getPath ());

         this.nodes.put (product_node.getName (), new Node (product_node));
      }
      return this.nodes;
   }

   @Override
   public Map<String, Attribute> getAttributes ()
   {
      if (this.attributes == null)
      {
         this.attributes = new LinkedHashMap<String, Attribute> ();
         boolean has_role = SECURITY_SERVICE.getCurrentUser ().
            getRoles ().contains (Role.ARCHIVE_MANAGER);
         for (MetadataIndex index :
              PRODUCT_SERVICE.getIndexes (this.product.getId ()))
         {
            if (has_role || "product".equalsIgnoreCase (index.getCategory ()))
            {
               Attribute attr =
                  new Attribute (index.getName (), index.getValue ());
               // attr.setContentType (index.getType ());
               this.attributes.put (attr.getName (), attr);
            }
         }
      }
      return this.attributes;
   }

   /**
    * Returns the absolute local path to this product.
    *
    * @return path to this product.
    */
   public String getDownloadablePath ()
   {
      return product.getDownload ().getPath ();
   }

   public InputStream getInputStream () throws IOException
   {
      return new FileInputStream (product.getDownload ().getPath ());
   }

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      // superclass node response is not required. Only Item response is
      // necessary.
      Map<String, Object> res = super.itemToEntityResponse (root_url);

      res.put (NodeEntitySet.CHILDREN_NUMBER, getChildrenNumber ());

      LinkedHashMap<String, Date> dates = new LinkedHashMap<String, Date> ();
      dates.put (V1Model.TIME_RANGE_START, getContentStart ());
      dates.put (V1Model.TIME_RANGE_END, getContentEnd ());
      res.put (ProductEntitySet.CONTENT_DATE, dates);

      HashMap<String, String> checksum = new LinkedHashMap<String, String> ();
      checksum.put (V1Model.ALGORITHM, getChecksumAlgorithm ());
      checksum.put (V1Model.VALUE, getChecksumValue ());
      res.put (ProductEntitySet.CHECKSUM, checksum);

      res.put (ProductEntitySet.INGESTION_DATE, getIngestionDate ());
      res.put (ProductEntitySet.CREATION_DATE, getCreationDate ());
      res.put (ProductEntitySet.EVICTION_DATE, getEvictionDate ());
      res.put (ProductEntitySet.CONTENT_GEOMETRY, getGeometry ());

      Path incoming_path =
         Paths.get (CONFIG_MGR.getArchiveConfiguration ()
            .getIncomingConfiguration ().getPath ());
      String prod_path = this.getDownloadablePath ();
      if (prod_path != null) // Can happen with not yet ingested products
      {
         Path prod_path_path = Paths.get (prod_path);
         if (prod_path_path.startsWith (incoming_path))
         {
            prod_path = incoming_path.relativize (prod_path_path).toString ();
         }
         else
         {
            prod_path = null;
         }
      }
      else
      {
         prod_path = null;
      }
      res.put (ProductEntitySet.LOCAL_PATH, prod_path);

      try
      {
         String url =
            root_url + V1Model.PRODUCT.getName () + "('" + getId () +
               "')/$value";
         MetalinkBuilder mb = new MetalinkBuilder ();
         mb.addFile (getName () + ".zip").addUrl (url, null, 0);

         StringWriter sw = new StringWriter ();
         Document doc = mb.build ();
         Transformer transformer =
            TransformerFactory.newInstance ().newTransformer ();
         transformer.transform (new DOMSource (doc), new StreamResult (sw));

         res.put (ProductEntitySet.METALINK, sw.toString ());
      }
      catch (ParserConfigurationException e)
      {
         LOGGER.error ("Error when creating Product EntityResponse", e);
      }
      catch (TransformerException e)
      {
         LOGGER.error ("Error when creating Product EntityResponse", e);
      }
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      if (prop_name.equals (ProductEntitySet.CREATION_DATE))
         return getCreationDate ();

      if (prop_name.equals (ProductEntitySet.INGESTION_DATE))
         return getIngestionDate ();

      if (prop_name.equals (ProductEntitySet.EVICTION_DATE))
         return getEvictionDate ();

      if (prop_name.equals (ProductEntitySet.CONTENT_GEOMETRY))
         return getGeometry ();

      return super.getProperty (prop_name);
   }

   @Override
   public Map<String, Object> getComplexProperty (String prop_name)
      throws ODataException
   {
      if (prop_name.equals (ProductEntitySet.CONTENT_DATE))
      {
         Map<String, Object> values = new HashMap<String, Object> ();
         values.put (V1Model.TIME_RANGE_START, getContentStart ());
         values.put (V1Model.TIME_RANGE_END, getContentEnd ());
         return values;
      }
      if (prop_name.equals (ProductEntitySet.CHECKSUM))
      {
         Map<String, Object> values = new HashMap<String, Object> ();
         values.put (V1Model.ALGORITHM, getChecksumAlgorithm ());
         values.put (V1Model.VALUE, getChecksumValue ());
         return values;
      }
      throw new ODataException ("Complex property '" + prop_name +
         "' not found.");
   }
   
   @Override
   public ODataResponse getEntityMedia (ODataSingleProcessor processor)
      throws ODataException
   {
      ODataResponse rsp = null;
      try
      {
         InputStream is=new BufferedInputStream(getInputStream());
         if (requiresControl ())
         {
            User u = V1Util.getCurrentUser ();
            String user_name = (u == null ? null : u.getUsername ());

            CopyStreamAdapter adapter = new CopyStreamAdapter ();
            CopyStreamListener recorder = new DownloadActionRecordListener (
                  product.getUuid (), product.getIdentifier (), u);
            CopyStreamListener closer = new DownloadStreamCloserListener (is);
            adapter.addCopyStreamListener (recorder);
            adapter.addCopyStreamListener (closer);

            RegulatedInputStream.Builder builder =
               new RegulatedInputStream.Builder (is,TrafficDirection.OUTBOUND);
            builder.userName (user_name);
            builder.copyStreamListener (adapter);

            is = builder.build ();
         }

         // Computes ETag
         String etag = getChecksumValue ();
         if (etag == null) etag = getId ();
         String filename = new File (getDownloadablePath ()).getName ();
         // Prepare the HTTP header for stream transfer.
         rsp = V1Util.prepareMediaResponse (etag, filename, getContentType (),
            getCreationDate ().getTime (), getContentLength (),
            processor.getContext (), is);
      }
      catch (Exception e)
      {
         String inner_message = ".";
         if (e.getMessage () != null) inner_message = " : " + e.getMessage ();
         throw new ODataException (
            "An exception occured while creating a stream" + inner_message, e);
      }
      return rsp;
   }
}