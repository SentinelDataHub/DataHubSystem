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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataResponse.ODataResponseBuilder;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.w3c.dom.Document;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.processing.impl.ProcessingUtils;
import fr.gael.dhus.network.RegulatedInputStream;
import fr.gael.dhus.network.TrafficDirection;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.V1Util;
import fr.gael.dhus.olingo.v1.entitySet.NodeEntitySet;
import fr.gael.dhus.olingo.v1.entitySet.ProductEntitySet;
import fr.gael.dhus.service.EvictionService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.util.DownloadActionRecordListener;
import fr.gael.dhus.util.MetalinkBuilder;
import fr.gael.drb.DrbNode;

/**
 * Product Bean. A product served by the DHuS.
 */
public class Product extends Node
{
   private static final Logger logger = Logger.getLogger (Product.class);

   private static final EvictionService evictionService =
      ApplicationContextProvider.getBean (EvictionService.class);

   protected final fr.gael.dhus.database.object.Product product;

   private Map<String, Product> products;

   protected Map<String, Node> nodes;

   protected Map<String, Attribute> attributes;

   private static final long DEFAULT_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;

   private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

   /**
    * Make a model Product from a database Product.
    * 
    * @param product database Product
    * @return model Product
    */
   public static Product fromDatabase (
      fr.gael.dhus.database.object.Product product)
   {
      if (product == null) return null;
      Product res = new Product (product);
      return res;
   }
   
   public Product (fr.gael.dhus.database.object.Product product)
   {
      super (product.getPath ().toString ());
      this.product = product;
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
      return evictionService.getEvictionDate (product.getId ());
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
         DrbNode product_node = ProcessingUtils.getNodeFromPath (
            product.getPath ().getPath ());
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
         for (MetadataIndex index : this.product.getIndexes ())
         {
            if ("product".equalsIgnoreCase (index.getCategory ()))
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

   public String getDownloadableFileName ()
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
         logger.error ("Error when creating Product EntityResponse", e);
      }
      catch (TransformerException e)
      {
         logger.error ("Error when creating Product EntityResponse", e);
      }
      return res;
   }

   @Override
   public Object getProperty (String propName) throws ODataException
   {
      if (propName.equals (ProductEntitySet.CREATION_DATE))
         return getCreationDate ();

      if (propName.equals (ProductEntitySet.INGESTION_DATE))
         return getIngestionDate ();

      if (propName.equals (ProductEntitySet.EVICTION_DATE))
         return getEvictionDate ();

      if (propName.equals (ProductEntitySet.CONTENT_GEOMETRY))
         return getGeometry ();

      return super.getProperty (propName);
   }

   @Override
   public Map<String, Object> getComplexProperty (String propName)
      throws ODataException
   {
      if (propName.equals (ProductEntitySet.CONTENT_DATE))
      {
         Map<String, Object> values = new HashMap<String, Object> ();
         values.put (V1Model.TIME_RANGE_START, getContentStart ());
         values.put (V1Model.TIME_RANGE_END, getContentEnd ());
         return values;
      }
      if (propName.equals (ProductEntitySet.CHECKSUM))
      {
         Map<String, Object> values = new HashMap<String, Object> ();
         values.put (V1Model.ALGORITHM, getChecksumAlgorithm ());
         values.put (V1Model.VALUE, getChecksumValue ());
         return values;
      }
      throw new ODataException ("Complex property '" + propName +
         "' not found.");
   }

   @Override
   public ODataResponse getEntityMedia (ODataSingleProcessor processor)
      throws ODataException
   {
      ODataResponse rsp = null;
      try
      {
         User u = V1Util.getCurrentUser ();
         String userName = (u == null ? null : u.getUsername ());
         InputStream is;
         if (requiresControl ())
         {
            RegulatedInputStream.Builder builder =
               new RegulatedInputStream.Builder (getInputStream (),
                  TrafficDirection.OUTBOUND);
            builder.userName (userName);
            builder.copyStreamListener (new DownloadActionRecordListener (
               product, u));

            is = builder.build ();
         }
         else
         {
            is = getInputStream ();
         }
         rsp = downloadResponseBuilder (this, is, processor).entity (is).
            build ();
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

   /**
    * Builds an ODataResponse according to the current http context header. The
    * response manages resume or partial requests, and controls inputs requests
    * to return expected return status. The passed input string is used to move
    * its current position if required by the header.
    * 
    * @param product the product to handle.
    * @param is the input stream references data to download.
    * @return the OData response builder
    */
   private ODataResponseBuilder downloadResponseBuilder (Product product,
      InputStream is, ODataSingleProcessor processor)
   {
      ODataResponseBuilder response = ODataResponse.newBuilder ();
      long length = product.getContentLength ();
      String fileName =
         new File (product.getDownloadableFileName ()).getName ();
      String etag = product.getChecksumValue ();
      if (etag == null) etag = product.getId ();
      Date lastModifiedObj = product.getCreationDate ();

      long lastModified = lastModifiedObj.getTime ();
      String contentType = product.getContentType ();
      
      // Validate request headers for caching ---------------------------------

      // If-None-Match header should contain "*" or ETag. If so, then return 304
      String ifNoneMatch =
         processor.getContext ().getRequestHeader ("If-None-Match");
      if (ifNoneMatch != null && matches (ifNoneMatch, etag))
      {
         response.header ("ETag", etag); // Required in 304.
         response.status (HttpStatusCodes.NOT_MODIFIED);
         return response;
      }

      // If-Modified-Since header should be greater than LastModified. If so,
      // then return 304.
      // This header is ignored if any If-None-Match header is specified.
      long ifModifiedSince =
         getHttpDate (processor.getContext ().getRequestHeader (
            "If-Modified-Since"));

      if ( (ifNoneMatch == null) && (ifModifiedSince != -1) &&
         (ifModifiedSince + 1000 > lastModified))
      {
         response.header ("ETag", etag); // Required in 304.
         response.status (HttpStatusCodes.NOT_MODIFIED);
         return response;
      }

      // Validate request headers for resume ----------------------------------

      // If-Match header should contain "*" or ETag. If not, then return 412.
      String ifMatch = processor.getContext ().getRequestHeader ("If-Match");
      if ( (ifMatch != null) && !matches (ifMatch, etag))
      {
         response.status (HttpStatusCodes.PRECONDITION_FAILED);
         return response;
      }

      // If-Unmodified-Since header should be greater than LastModified.
      // If not, then return 412.
      long ifUnmodifiedSince =
         getHttpDate (processor.getContext ().getRequestHeader (
            "If-Unmodified-Since"));
      if ( (ifUnmodifiedSince != -1) &&
         (ifUnmodifiedSince + 1000 <= lastModified))
      {
         response.status (HttpStatusCodes.PRECONDITION_FAILED);
         return response;
      }

      // Validate and process range --------------------------------------------

      // Prepare some variables. The full Range represents the complete file.
      Range full = new Range (0, length - 1, length);
      List<Range> ranges = new ArrayList<Range> ();

      // Validate and process Range and If-Range headers.
      String range = processor.getContext ().getRequestHeader ("Range");
      if (range != null)
      {
         // Range header should match format "bytes=n-n,n-n,n-n...".
         // If not, then return 416.
         if ( !range.matches ("^bytes=\\d*-\\d*(,\\d*-\\d*)*$"))
         {
            // Required in 416.
            response.header ("Content-Range", "bytes */" + length);
            response.status (HttpStatusCodes.REQUESTED_RANGE_NOT_SATISFIABLE);
            return response;
         }

         String ifRange = processor.getContext ().getRequestHeader ("If-Range");
         if ( (ifRange != null) && !ifRange.equals (etag))
         {
            ranges.add (full);
         }

         // If any valid If-Range header, then process each part of byte range.
         if (ranges.isEmpty ())
         {
            for (String part : range.substring (6).split (","))
            {
               // Assuming a file with length of 100, the following examples
               // returns bytes at:
               // 50-80 (50 to 80),
               // 40- (40 to length=100),
               // -20 (length-20=80 to length=100).
               long start = sublong (part, 0, part.indexOf ("-"));
               long end =
                  sublong (part, part.indexOf ("-") + 1, part.length ());

               if (start == -1)
               {
                  start = length - end;
                  end = length - 1;
               }
               else
                  if (end == -1 || end > length - 1)
                  {
                     end = length - 1;
                  }

               // Check if Range is syntactically valid. If not, then return
               // 416.
               if (start > end)
               {
                  // Required in 416.
                  response.header ("Content-Range", "bytes */" + length);
                  response
                     .status (HttpStatusCodes.REQUESTED_RANGE_NOT_SATISFIABLE);
                  return response;
               }
               // Add range.
               ranges.add (new Range (start, end, length));
            }
         }
      }
      // Prepare and initialize response --------------------------------------

      // Get content type by file name and set content disposition.
      String disposition = "inline";

      /*
       * If content type is unknown, then set the default value. For all content
       * types, see: http://www.w3schools.com/media/media_mimeref.asp To add new
       * content types, add new mime-mapping entry in web.xml.
       */
      if (contentType == null)
      {
         contentType = "application/octet-stream";
      }
      else
         if ( !contentType.startsWith ("image"))
         {
            // Else, expect for images, determine content disposition.
            // If content type is supported by the browser, then set to inline,
            // else attachment which will pop a 'save as' dialogue.
            String accept = processor.getContext ().getRequestHeader ("Accept");
            disposition =
               accept != null && accepts (accept, contentType) ? "inline"
                  : "attachment";
         }

      // Initialize response.
      response.header ("Content-Disposition", disposition + ";filename=\"" +
         fileName + "\"");
      response.header ("Accept-Ranges", "bytes");
      response.header ("ETag", etag);
      response.header ("Last-Modified", asHttpDate (lastModified));
      response.header ("Expires", asHttpDate (System.currentTimeMillis () +
         DEFAULT_EXPIRE_TIME));

      if (ranges.isEmpty () || ranges.get (0) == full)
      {
         // Return full file.
         Range r = full;
         response.header ("Content-Type", contentType);
         response.header ("Content-Range", "bytes " + r.start + "-" + r.end +
            "/" + r.total);
         response.header ("Content-Length", String.valueOf (r.length));
         return response;
      }
      else
         if (ranges.size () == 1)
         {
            // Return single part of file.
            Range r = ranges.get (0);
            response.header ("Content-Type", contentType);
            response.header ("Content-Range", "bytes " + r.start + "-" + r.end +
               "/" + r.total);
            response.header ("Content-Length", String.valueOf (r.length));

            try
            {
               is.skip (r.start);
               response.status (HttpStatusCodes.PARTIAL_CONTENT); // 206.
            }
            catch (IOException e)
            {
               logger.error ("Cannot skip input stream of " + fileName +
                  " to offset " + r.start);
               response
                  .status (HttpStatusCodes.REQUESTED_RANGE_NOT_SATISFIABLE);
            }

            return response;
         }
         else
         {
            // Return multiple parts of file.
            response.header ("Content-Type", "multipart/byteranges; boundary=" +
               MULTIPART_BOUNDARY);
            response.status (HttpStatusCodes.NOT_IMPLEMENTED);
            logger.error ("MULTIPART NOT SUPPORTED !");
            return response;
         }
   }

   /**
    * Returns true if the given match header matches the given value.
    * 
    * @param matchHeader The match header.
    * @param toMatch The value to be matched.
    * @return True if the given match header matches the given value.
    */
   private boolean matches (String matchHeader, String toMatch)
   {
      String[] matchValues = matchHeader.split ("\\s*,\\s*");
      Arrays.sort (matchValues);
      return Arrays.binarySearch (matchValues, toMatch) > -1 ||
         Arrays.binarySearch (matchValues, "*") > -1;
   }

   /**
    * Returns a substring of the given string value from the given begin index
    * to the given end index as a long. If the substring is empty, then -1 will
    * be returned.
    * 
    * @param value The string value to return a substring as long for.
    * @param beginIndex The begin index of the substring to be returned as long.
    * @param endIndex The end index of the substring to be returned as long.
    * @return A substring of the given string value as long or -1 if substring
    *         is empty.
    */
   private long sublong (String value, int beginIndex, int endIndex)
   {
      String substring = value.substring (beginIndex, endIndex);
      return (substring.length () > 0) ? Long.parseLong (substring) : -1;
   }

   /**
    * Returns true if the given accept header accepts the given value.
    * 
    * @param acceptHeader The accept header.
    * @param toAccept The value to be accepted.
    * @return True if the given accept header accepts the given value.
    */
   private boolean accepts (String acceptHeader, String toAccept)
   {
      String[] acceptValues = acceptHeader.split ("\\s*(,|;)\\s*");
      Arrays.sort (acceptValues);
      return 
         Arrays.binarySearch (acceptValues, toAccept) > -1 ||
         Arrays.binarySearch (acceptValues, 
            toAccept.replaceAll ("/.*$", "/*")) > -1 ||
         Arrays.binarySearch (acceptValues, "*/*") > -1;
   }

   /**
    * Returns long representation of the HTTP defined RFC 1123 date format.
    * 
    * @param date to parse
    * @return the long value of date since 1st January 1970
    */
   private long getHttpDate (String date)
   {
      SimpleDateFormat dateFormat =
         new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
      try
      {
         return dateFormat.parse (date).getTime ();
      }
      catch (Exception e)
      {
         return -1;
      }
   }

   /**
    * Returns string representation of the HTTP defined RFC 1123 date format.
    * 
    * @param date to parse
    * @return the long value of date since 1st January 1970
    */
   private String asHttpDate (long date)
   {
      SimpleDateFormat dateFormat =
         new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

      return dateFormat.format (new Date (date));
   }

   protected class Range
   {
      long start;
      long end;
      long length;
      long total;

      /**
       * Construct a byte range.
       * 
       * @param start Start of the byte range.
       * @param end End of the byte range.
       * @param total Total length of the byte source.
       */
      public Range (long start, long end, long total)
      {
         this.start = start;
         this.end = end;
         this.length = end - start + 1;
         this.total = total;
      }
   }
}
