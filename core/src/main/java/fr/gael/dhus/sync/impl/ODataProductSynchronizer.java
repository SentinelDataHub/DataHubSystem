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
package fr.gael.dhus.sync.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gael.dhus.service.ISynchronizerService;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.HibernateException;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.SynchronizerConf;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.sync.Synchronizer;
import fr.gael.dhus.olingo.ODataClient;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.service.MetadataTypeService;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.SearchService;
import fr.gael.dhus.service.metadata.MetadataType;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.util.http.HttpAsyncClientProducer;
import fr.gael.dhus.util.http.InterruptibleHttpClient;
import org.springframework.dao.CannotAcquireLockException;

/**
 * A synchronizer using the OData API of another DHuS.
 */
public class ODataProductSynchronizer extends Synchronizer
{
   /** Log. */
   private static final Logger LOGGER = Logger.getLogger (ODataProductSynchronizer.class);

   /** Synchronizer Service, to save the  */
   private static final ISynchronizerService SYNC_SERVICE =
         ApplicationContextProvider.getBean (ISynchronizerService.class);

   /** Product service, to store Products in the database. */
   private static final ProductService PRODUCT_SERVICE =
         ApplicationContextProvider.getBean (ProductService.class);

   /** Metadata Type Service, MetadataIndex name to Queryable. */
   private static final MetadataTypeService METADATA_TYPE_SERVICE =
         ApplicationContextProvider.getBean (MetadataTypeService.class);

   /** Search Service, to add a new product in the index. */
   private static final SearchService SEARCH_SERVICE =
         ApplicationContextProvider.getBean (SearchService.class);

   /** Collection Service, for to add a Product in the configured targetCollection. */
   private static final CollectionService COLLECTION_SERVICE =
         ApplicationContextProvider.getBean (CollectionService.class);

   /** Incoming manager, tells where to download products. */
   private static final IncomingManager INCOMING_MANAGER =
         ApplicationContextProvider.getBean (IncomingManager.class);

   /** An {@link ODataClient} configured to query another DHuS OData service. */
   private final ODataClient client;

   /** Credentials: username. */
   private final String serviceUser;

   /** Credentials: password. */
   private final String servicePass;

   /** Path to the remote DHuS incoming directory (if accessible). */
   private final String remoteIncoming;

   /** Adds every new product in this collection. */
   private final Long targetCollection;

   /** OData resource path to a remote source collection: "Collections('a')/.../Collections('z')" */
   private final String sourceCollection;

   /** True if this synchronizer must download a local copy of the product. */
   private final boolean copyProduct;

   /** Custom $filter parameter, to be added to the query URI. */
   private final String filterParam;

   /** Last created product's updated time. */
   private Date lastCreated;

   /** Last updated product's updated time. */
   private Date lastUpdated;

   /** Last deleted product's deletion time. */
   private Date lastDeleted;

   /** Set to true whenever one of the three date fields is modified. */
   private boolean dateChanged = false;

   /** Size of a Page (count of products to retrieve at once). */
   private int pageSize;

   /**
    * Creates a new ODataSynchronizer.
    *
    * @param sc configuration for this synchronizer.
    *
    * @throws IllegalStateException if the configuration doe not contains the
    *    required fields, or those fields are malformed.
    * @throws IOException when the OdataClient fails to contact the server
    *    at {@code url}.
    * @throws ODataException when no OData service have been found at the
    *    given url.
    * @throws NumberFormatException if the value of the `target_collection`
    *    configuration field is not a number.
    */
   public ODataProductSynchronizer (SynchronizerConf sc)
         throws IOException, ODataException
   {
      super (sc);

      // Checks if required configuration is set
      String urilit = sc.getConfig ("service_uri");
      serviceUser = sc.getConfig ("service_username");
      servicePass = sc.getConfig ("service_password");
      if (urilit == null || urilit.isEmpty ())
      {
         throw new IllegalStateException ("`service_uri` is not set");
      }

      try
      {
         client = new ODataClient (urilit, serviceUser, servicePass);
      }
      catch (URISyntaxException e)
      {
         throw new IllegalStateException ("`service_uri` is malformed");
      }

      String dec_name = client.getSchema ().getDefaultEntityContainer ().getName ();
      if (!dec_name.equals (V1Model.ENTITY_CONTAINER))
      {
         throw new IllegalStateException ("`service_uri` does not reference a DHuS odata service");
      }

      String last_cr = sc.getConfig ("last_created");
      if (last_cr != null && !last_cr.isEmpty ())
      {
         lastCreated = new Date (Long.decode (last_cr));
      }
      else
      {
         lastCreated = new Date (0L);
      }

      String last_up = sc.getConfig ("last_updated");
      if (last_up != null && !last_up.isEmpty ())
      {
         lastUpdated = new Date (Long.decode (last_up));
      }
      else
      {
         lastUpdated = new Date (0L);
      }

      String last_del = sc.getConfig ("last_deleted");
      if (last_del != null && !last_del.isEmpty ())
      {
         lastDeleted = new Date (Long.decode (last_del));
      }
      else
      {
         lastDeleted = new Date (0L);
      }

      String page_size = sc.getConfig ("page_size");
      if (page_size != null && !page_size.isEmpty ())
      {
         pageSize = Integer.decode (page_size);
      }
      else
      {
         pageSize = 30; // FIXME get that value from the config?
      }

      String remote_incoming = sc.getConfig ("remote_incoming_path");
      if (remote_incoming != null && !remote_incoming.isEmpty ())
      {
         File ri = new File (remote_incoming);
         if (!ri.exists () || !ri.isDirectory () || !ri.canRead ())
         {
            throw new IOException ("Cannot access remote incoming " + remote_incoming);
         }
         this.remoteIncoming = remote_incoming;
      }
      else
      {
         this.remoteIncoming = null;
      }

      String target_collection = sc.getConfig ("target_collection");
      if (target_collection != null && !target_collection.isEmpty ())
      {
         this.targetCollection = Long.parseLong (target_collection);
      }
      else
      {
         this.targetCollection = null;
      }

      String filter_param = sc.getConfig ("filter_param");
      if (filter_param != null && !filter_param.isEmpty ())
      {
         filterParam = filter_param;
      }
      else
      {
         filterParam = null;
      }

      String source_collection = sc.getConfig("source_collection");
      if (source_collection != null && !source_collection.isEmpty())
      {
         sourceCollection = source_collection;
      }
      else
      {
         sourceCollection = "";
      }

      String copy_product = sc.getConfig ("copy_product");
      if (copy_product != null && !copy_product.isEmpty ())
      {
         this.copyProduct = Boolean.parseBoolean (copy_product);
      }
      else
      {
         this.copyProduct = false;
      }
   }

   /** Logs how much time an OData command consumed. */
   private void logODataPerf(String query, long delta_time)
   {
      LOGGER.debug ("Synchronizer#" + getId () +
                    " query(" + query + ") done in " + delta_time + "ms");
   }

   /** Result type for {@link #downloadValidateRename(InterruptibleHttpClient, Path, String)}. */
   private class DownloadResult
   {
      /** Path to downloaded data. */
      public final Path data;
      /** Content-Type of downloaded data. */
      public final String dataType;
      /** Content-Length of downloaded data. */
      public final long dataSize;

      /** Create new instance, sets public fields. */
      public DownloadResult(Path data, String dataType, long dataSize)
      {
         this.data = data;
         this.dataType = dataType;
         this.dataSize = dataSize;
      }
   }

   /**
    * Uses the given `http_client` to download `url` into `out_tmp`.
    * Renames `out_tmp` to the value of the filename param of the Content-Disposition header field.
    * Returns a path to the renamed file.
    *
    * @param http_client synchronous interruptible HTTP client.
    * @param out_tmp download destination file on disk (will be created if does not exist).
    * @param url what to download.
    * @return Path to file with its actual name.
    * @throws IOException Anything went wrong (with IO or network, or if the HTTP header field
    *       Content-Disposition is missing).
    * @throws InterruptedException Thread has been interrupted.
    */
   private DownloadResult downloadValidateRename(InterruptibleHttpClient http_client, Path out_tmp,
         String url) throws IOException, InterruptedException
   {
      try (FileChannel output = FileChannel.open(out_tmp,
            StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))
      {

         HttpResponse response = http_client.interruptibleGet(url, output);

         // If the response's status code is not 200, something wrong happened
         if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
         {
            Formatter ff = new Formatter();
            ff.format("Synchronizer#%d cannot download product at %s,"
                  + " remote dhus returned message '%s' (HTTP%d)",
                  getId(),
                  url,
                  response.getStatusLine().getReasonPhrase(),
                  response.getStatusLine().getStatusCode());
            throw new IOException(ff.out().toString());
         }

         // Gets the filename from the HTTP header field `Content-Disposition'
         Pattern pat = Pattern.compile("filename=\"(.+?)\"", Pattern.CASE_INSENSITIVE);
         String contdis = response.getFirstHeader("Content-Disposition").getValue();
         Matcher m = pat.matcher(contdis);
         if (!m.find())
         {
            throw new IOException("Synchronizer#" + getId()
                  + " Missing HTTP header field `Content-Disposition` that determines the filename");
         }
         String filename = m.group(1);
         if (filename == null || filename.isEmpty())
         {
            throw new IOException("Synchronizer#" + getId()
                  + " Invalid filename in HTTP header field `Content-Disposition`");
         }

         // Renames the downloaded file
         output.close();
         Path dest = out_tmp.getParent().resolve(filename);
         Files.move(out_tmp, dest, StandardCopyOption.ATOMIC_MOVE);

         DownloadResult res = new DownloadResult(
               dest,
               response.getEntity().getContentType().getValue(),
               response.getEntity().getContentLength());

         return res;
      }
      finally
      {
         if (Files.exists(out_tmp))
         {
            Files.delete(out_tmp);
         }
      }
   }

   /** Downloads a product. */
   private void downloadProduct(Product p) throws IOException, InterruptedException
   {
      // Creates a client producer that produces HTTP Basic auth aware clients
      HttpAsyncClientProducer cliprod = new HttpAsyncClientProducer ()
      {
         @Override
         public CloseableHttpAsyncClient generateClient ()
         {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope (AuthScope.ANY),
                    new UsernamePasswordCredentials(serviceUser, servicePass));
            CloseableHttpAsyncClient res = HttpAsyncClients.custom ()
                  .setDefaultCredentialsProvider (credsProvider)
                  .build ();
            res.start ();
            return res;
         }
      };

      // Asks the Incoming manager for a download path
      Path dir = Paths.get(INCOMING_MANAGER.getNewIncomingPath().toURI());
      Path tmp_pd = dir.resolve(p.getIdentifier() + ".part");    // Temporary names
      Path tmp_ql = dir.resolve(p.getIdentifier() + "-ql.part");
      Path tmp_tn = dir.resolve(p.getIdentifier() + "-tn.part");
      // These files will be moved once download is complete

      InterruptibleHttpClient http_client = new InterruptibleHttpClient(cliprod);
      DownloadResult pd_res = null, ql_res = null, tn_res = null;
      try
      {
         long delta = System.currentTimeMillis();
         pd_res = downloadValidateRename(http_client, tmp_pd, p.getOrigin());
         logODataPerf(p.getOrigin(), System.currentTimeMillis() - delta);

         // Sets download info in the product (not written in the db here)
         p.setPath(pd_res.data.toUri().toURL());
         p.setDownloadablePath(pd_res.data.toString());
         p.setDownloadableType(pd_res.dataType);
         p.setDownloadableSize(pd_res.dataSize);

         // Downloads and sets the quicklook and thumbnail (if any)
         if (p.getQuicklookFlag())
         {
            // Failing at downloading a quicklook must not abort the download!
            try
            {
               ql_res = downloadValidateRename(http_client, tmp_ql, p.getQuicklookPath());
               p.setQuicklookPath(ql_res.data.toString());
               p.setQuicklookSize(ql_res.dataSize);
            }
            catch (IOException ex)
            {
               LOGGER.error("Failed to download quicklook at " + p.getQuicklookPath(), ex);
            }
         }
         if (p.getThumbnailFlag())
         {
            // Failing at downloading a thumbnail must not abort the download!
            try
            {
               tn_res = downloadValidateRename(http_client, tmp_tn, p.getThumbnailPath());
               p.setThumbnailPath(tn_res.data.toString());
               p.setThumbnailSize(tn_res.dataSize);
            }
            catch (IOException ex)
            {
               LOGGER.error("Failed to download thumbnail at " + p.getThumbnailPath(), ex);
            }
         }
      }
      catch (Exception ex)
      {
         // Removes downloaded files if an error occured
         if (pd_res != null)
         {
            Files.delete(pd_res.data);
         }
         if (ql_res != null)
         {
            Files.delete(ql_res.data);
         }
         if (tn_res != null)
         {
            Files.delete(tn_res.data);
         }
         throw ex;
      }
   }

   /**
    * Retrieve new/updated products.
    * @return how many products have been retrieved.
    */
   private int getNewProducts () throws InterruptedException
   {
      int res = 0;
      try
      {
         // Makes the query parameters
         Map<String, String> query_param = new HashMap<> ();

         String lup_s =EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance ()
               .valueToString (lastCreated, EdmLiteralKind.URI, null);
         // 'GreaterEqual' because of products with the same IngestionDate
         String filter = "IngestionDate ge " + lup_s;

         // Appends custom $filter parameter
         if (filterParam != null) {
            filter += " and (" + filterParam + ")";
         }

         query_param.put ("$filter", filter);

         query_param.put ("$top", String.valueOf (pageSize));

         query_param.put ("$orderby", "IngestionDate");

         // Executes the query
         long delta = System.currentTimeMillis ();
         ODataFeed pdf = client.readFeed (sourceCollection + "/Products", query_param);
         logODataPerf ("Products", System.currentTimeMillis () - delta);

         // For each entry, creates a DataBase Object
         for (ODataEntry pdt: pdf.getEntries ())
         {
            Map<String, Object> props = pdt.getProperties ();

            // Checks if a product with the same UUID already exist
            // (`UUID` and `PATH` have unique constraint), PATH references the UUID
            String uuid = (String) props.get ("Id");
            if (PRODUCT_SERVICE.systemGetProduct (uuid) != null)
            {
               // FIXME: might not be the same product
               this.lastCreated = (((GregorianCalendar) props.get ("IngestionDate")).getTime ());
               this.dateChanged = true;
               continue;
            }

            // Makes the product resource path
            String pdt_p = "/Products('" + uuid + "')";

            Product product = new Product ();
            product.setUuid (uuid);

            // Reads the properties
            product.setIdentifier ((String) props.get ("Name"));
            product.setIngestionDate (((GregorianCalendar) props.get ("IngestionDate")).getTime ());
            product.setCreated (((GregorianCalendar) props.get ("CreationDate")).getTime ());
            product.setFootPrint ((String) props.get ("ContentGeometry"));
            product.setProcessed (Boolean.TRUE);
            product.setSize ((Long) props.get ("ContentLength"));

            // Reads the ContentDate complex type
            Map contentDate = (Map)props.get ("ContentDate");
            product.setContentStart (((GregorianCalendar) contentDate.get ("Start")).getTime ());
            product.setContentEnd (((GregorianCalendar) contentDate.get ("End")).getTime ());

            // Sets the origin to the remote URI
            product.setOrigin (client.getServiceRoot() + pdt_p + "/$value");
            product.setPath (new URL (pdt.getMetadata ().getId () + "/$value"));

            // Sets the download path to LocalPath (if LocalPaths are exposed)
            if (this.remoteIncoming != null && !this.copyProduct)
            {
               String path = (String) props.get ("LocalPath");
               if (path != null && !path.isEmpty ())
               {
                  Map<String, String> checksum = (Map)props.get ("Checksum");

                  Product.Download d = new Product.Download ();
                  d.setPath (Paths.get(this.remoteIncoming, path).toString());
                  d.setSize (product.getSize ());
                  d.setType ((String) props.get ("ContentType"));
                  d.setChecksums (
                        Collections.singletonMap (
                              checksum.get (V1Model.ALGORITHM),
                              checksum.get (V1Model.VALUE)));
                  product.setDownload (d);

                  File f = new File (d.getPath ());
                  if (!f.exists ())
                  {
                     // The incoming path is probably false
                     // Throws an exception to notify the admin about this issue
                     throw new RuntimeException("ODataSynchronizer: Local file '" + path
                           + "' not found in remote incoming '" + this.remoteIncoming + '\'');
                  }
                  product.setPath (new URL("file://" + d.getPath ()));
               }
               else
               {
                  throw new RuntimeException("RemoteIncoming is set"
                        + " but the LocalPath property is missing in remote products");
               }
            }

            // Retrieves the Product Class
            delta = System.currentTimeMillis ();
            ODataEntry pdt_class_e = client.readEntry (pdt_p + "/Class", null);
            logODataPerf (pdt_p + "/Class", System.currentTimeMillis () - delta);

            Map<String, Object> pdt_class_pm = pdt_class_e.getProperties ();
            String pdt_class = (String) pdt_class_pm.get ("Uri");
            product.setItemClass(pdt_class);

            // Retrieves Metadata Indexes (aka Attributes on odata)
            delta = System.currentTimeMillis ();
            ODataFeed mif = client.readFeed (pdt_p + "/Attributes", null);
            logODataPerf (pdt_p + "/Attributes", System.currentTimeMillis () - delta);

            List<MetadataIndex> mi_l = new ArrayList<> (mif.getEntries ().size ());
            for (ODataEntry mie: mif.getEntries ())
            {
               props = mie.getProperties ();
               MetadataIndex mi = new MetadataIndex ();
               String mi_name = (String) props.get ("Name");
               mi.setName (mi_name);
               mi.setType ((String) props.get ("ContentType"));
               mi.setValue ((String) props.get ("Value"));
               MetadataType mt = METADATA_TYPE_SERVICE.getMetadataTypeByName (pdt_class, mi_name);
               if (mt != null)
               {
                  mi.setCategory (mt.getCategory ());
                  if (mt.getSolrField () != null)
                  {
                     mi.setQueryable (mt.getSolrField ().getName ());
                  }
               }
               else if (mi_name.equals ("Identifier"))
               {
                  mi.setCategory ("");
                  mi.setQueryable ("identifier");
               }
               else if (mi_name.equals ("Ingestion Date"))
               {
                  mi.setCategory ("product");
                  mi.setQueryable ("ingestionDate");
               }
               else
               {
                  mi.setCategory ("");
               }
               mi_l.add (mi);
            }
            product.setIndexes (mi_l);

            // Retrieves subProducts
            delta = System.currentTimeMillis ();
            ODataFeed subp = client.readFeed (pdt_p + "/Products", null);
            logODataPerf (pdt_p + "/Products", System.currentTimeMillis () - delta);

            for (ODataEntry subpe: subp.getEntries ())
            {
               String id = (String) subpe.getProperties ().get ("Id");
               Long content_len = (Long) subpe.getProperties ().get ("ContentLength");

               String path = (String) subpe.getProperties ().get ("LocalPath");
               if (this.remoteIncoming != null && !this.copyProduct
                     && path != null && !path.isEmpty ())
               {
                  path = Paths.get(this.remoteIncoming, path).toString();
               }
               else
               {
                  path = client.getServiceRoot() + pdt_p
                        + "/Products('" + subpe.getProperties().get("Id") + "')/$value";
               }

               // Retrieves the Quicklook
               if (id.equals ("Quicklook"))
               {
                  product.setQuicklookSize (content_len);
                  product.setQuicklookPath (path);
               }

               // Retrieves the Thumbnail
               else if (id.equals ("Thumbnail"))
               {
                  product.setThumbnailSize (content_len);
                  product.setThumbnailPath (path);
               }
            }

            // `processed` must be set to TRUE
            product.setProcessed (Boolean.TRUE);

            // Downloads the product if required
            if (this.copyProduct)
            {
               downloadProduct(product);
            }

            // Stores `product` in the database
            product = PRODUCT_SERVICE.addProduct (product);
            product.setIndexes (mi_l); // DELME lazy loading not working atm ...

            // Sets the target collection both in the DB and Solr
            if (this.targetCollection != null)
            {
               try
               {
                  COLLECTION_SERVICE.systemAddProduct (this.targetCollection, product.getId (), false);
               }
               catch (HibernateException e)
               {
                  LOGGER.error ("Synchronizer#" + getId () + " Failed to set collection#" +
                        this.targetCollection + " for product " + product.getIdentifier (), e);
                  // Reverting ...
                  PRODUCT_SERVICE.systemDeleteProduct (product.getId ());
                  throw e;
               }
               catch (Exception e)
               {
                  LOGGER.error ("Synchronizer#" + getId () + " Failed to update product " +
                        product.getIdentifier () + " in Solr's index", e);
               }
            }

            // Stores `product` in the index
            try
            {
               delta = System.currentTimeMillis();
               SEARCH_SERVICE.index (product);
               LOGGER.debug("Synchronizer#" + getId() + " indexed product " +
                     product.getIdentifier() + " in " + (System.currentTimeMillis() - delta) + "ms");
            }
            catch (Exception e)
            {
               // Solr errors are not considered fatal
               LOGGER.error ("Synchronizer#" + getId () + " Failed to index product " +
                     product.getIdentifier () + " in Solr's index", e);
            }

            this.lastCreated = product.getIngestionDate ();
            this.dateChanged = true;

            LOGGER.info("Synchronizer#" + getId () + " Product " + product.getIdentifier () +
                  " ("+ product.getSize () + " bytes compressed) " +
                  "successfully synchronized from " + this.client.getServiceRoot ());

            res++;

            // Checks if we have to abandon the current pass
            if (Thread.interrupted ())
            {
               throw new InterruptedException ();
            }
         }
      }
      catch (IOException | ODataException ex)
      {
         LOGGER.error ("OData failure", ex);
      }
      finally
      {
         // Save the ingestionDate of the last created Product
         this.syncConf.setConfig ("last_created", String.valueOf (this.lastCreated.getTime ()));
      }

      return res;
   }

   /**
    * Retrieves updated products.
    * Not Yet Implemented.
    * @return how many products have been retrieved.
    */
   private int getUpdatedProducts ()
   {
      // NYI
      return 0;
   }

   /**
    * Retrieves deleted products.
    * Not Yet Implemented.
    * @return how many products have been retrieved.
    */
   private int getDeletedProducts ()
   {
      // NYI
      return 0;
   }

   @Override
   public boolean synchronize () throws InterruptedException
   {
      int retrieved = 0, updated = 0, deleted = 0;

      LOGGER.info("Synchronizer#" + getId () + " started");
      try
      {
         retrieved = getNewProducts ();
         if (Thread.interrupted ())
         {
            throw new InterruptedException ();
         }

         updated = getUpdatedProducts ();
         if (Thread.interrupted ())
         {
            throw new InterruptedException ();
         }

         deleted = getDeletedProducts ();
      }
      catch (LockAcquisitionException | CannotAcquireLockException e)
      {
         throw new InterruptedException (e.getMessage ());
      }
      finally
      {
         // Logs a summary of what it has done this session
         StringBuilder sb = new StringBuilder ("Synchronizer#");
         sb.append (getId ()).append (" done:    ");
         sb.append (retrieved).append (" new Products,    ");
         sb.append (updated).append (" updated Products,    ");
         sb.append (deleted).append (" deleted Products");
         sb.append ("    from ").append (this.client.getServiceRoot ());
         LOGGER.info(sb.toString());

         // Writes the database only if there is a modification
         if (this.dateChanged)
         {
            SYNC_SERVICE.saveSynchronizer (this);
            this.dateChanged = false;
         }
      }

      return retrieved < pageSize && updated < pageSize && deleted < pageSize;
   }

   @Override
   public String toString ()
   {
      return "OData Product Synchronizer on " + syncConf.getConfig("service_uri");
   }
}
