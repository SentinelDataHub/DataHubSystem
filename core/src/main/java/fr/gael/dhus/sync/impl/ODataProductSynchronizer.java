/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.SynchronizerConf;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.olingo.ODataClient;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.service.ISynchronizerService;
import fr.gael.dhus.service.MetadataTypeService;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.SearchService;
import fr.gael.dhus.service.metadata.MetadataType;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.sync.Synchronizer;
import fr.gael.dhus.util.http.HttpAsyncClientProducer;
import fr.gael.dhus.util.http.ParallelizedDownloadManager;
import fr.gael.dhus.util.http.ParallelizedDownloadManager.DownloadResult;
import fr.gael.dhus.util.http.Timeouts;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;

import org.hibernate.HibernateException;
import org.hibernate.exception.LockAcquisitionException;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.security.crypto.codec.Hex;

/**
 * A synchronizer using the OData API of another DHuS.
 */
public class ODataProductSynchronizer extends Synchronizer
{
   /** Log. */
   private static final Logger LOGGER = LogManager.getLogger(ODataProductSynchronizer.class);

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
   private final String targetCollectionUUID;

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
      if (!dec_name.equals(Model.ENTITY_CONTAINER))
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
         this.targetCollectionUUID = target_collection;
      }
      else
      {
         this.targetCollectionUUID = null;
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


   /**
    * Downloads a product,
    * returns 3 Futures, 1st is the product, 2nd is the quicklook and 3rd is the thumbnail.
    * 2nd and 3rd Futures may be null!
    */
   private Future<DownloadResult>[] download(ParallelizedDownloadManager downloader, Product p)
         throws IOException, InterruptedException
   {
      @SuppressWarnings("unchecked")
      Future<DownloadResult>[] res = new Future[3];

      res[0] = downloader.download(p.getOrigin());

      // Downloads and sets the quicklook and thumbnail (if any)
      if (p.getQuicklookFlag())
      {
         res[1] = downloader.download(p.getQuicklookPath());
      }
      if (p.getThumbnailFlag())
      {
         res[2] = downloader.download(p.getThumbnailPath());
      }

      return res;
   }

   /**
    * Gets `pageSize` products from the data source.
    * @param optional_skip an optional $skip parameter, may be null.
    * @param expand_navlinks if `true`, the query will contain: `$expand=Class,Attributes,Products`.
    */
   private ODataFeed getPage(Integer optional_skip, boolean expand_navlinks)
         throws ODataException, IOException, InterruptedException
   {
      // Makes the query parameters
      Map<String, String> query_param = new HashMap<>();

      String lup_s = EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance()
            .valueToString(lastCreated, EdmLiteralKind.URI, null);
      // 'GreaterEqual' because of products with the same IngestionDate
      String filter = "IngestionDate ge " + lup_s;

      // Appends custom $filter parameter
      if (filterParam != null)
      {
         filter += " and (" + filterParam + ")";
      }

      query_param.put("$filter", filter);

      query_param.put("$top", String.valueOf(pageSize));

      query_param.put("$orderby", "IngestionDate");

      if (optional_skip != null && optional_skip > 0)
      {
         query_param.put("$skip", optional_skip.toString());
      }

      if (expand_navlinks)
      {
         query_param.put("$expand", "Class,Attributes,Products");
      }

      // Executes the query
      long delta = System.currentTimeMillis();
      ODataFeed pdf = client.readFeed(sourceCollection + "/Products", query_param);
      logODataPerf("Products", System.currentTimeMillis() - delta);

      return pdf;
   }

   /** Returns the IngestionDate of the given product entry. */
   private Date getIngestionDate(ODataEntry entry) {
      return ((GregorianCalendar) entry.getProperties().get("IngestionDate")).getTime();
   }

   /** Returns `true` if the given product entry already exists in the database. */
   private boolean exists(ODataEntry entry)
   {
      String uuid = (String) entry.getProperties().get("Id");
      // FIXME: might not be the same product
      return PRODUCT_SERVICE.systemGetProduct(uuid) != null;
   }

   /** Creates and returns a new Product from the given entry. */
   private Product entryToProducts(ODataEntry entry)
         throws ODataException, IOException, InterruptedException
   {
      long delta;
      Map<String, Object> props = entry.getProperties();

      // (`UUID` and `PATH` have unique constraint), PATH references the UUID
      String uuid = (String) props.get("Id");

      // Makes the product resource path
      String pdt_p = "/Products('" + uuid + "')";

      Product product = new Product();
      product.setUuid(uuid);

      // Reads the properties
      product.setIdentifier((String) props.get("Name"));
      product.setIngestionDate(((GregorianCalendar) props.get("IngestionDate")).getTime());
      product.setCreated(((GregorianCalendar) props.get("CreationDate")).getTime());
      product.setFootPrint((String) props.get("ContentGeometry"));
      product.setProcessed(Boolean.TRUE);
      product.setSize((Long) props.get("ContentLength"));

      // Reads the ContentDate complex type
      Map contentDate = (Map) props.get("ContentDate");
      product.setContentStart(((GregorianCalendar) contentDate.get("Start")).getTime());
      product.setContentEnd(((GregorianCalendar) contentDate.get("End")).getTime());

      // Sets the origin to the remote URI
      product.setOrigin(client.getServiceRoot() + pdt_p + "/$value");
      product.setPath(new URL(entry.getMetadata().getId() + "/$value"));

      // Sets the size, ContentType and Checksum of product
      Product.Download d = new Product.Download();
      Map<String, String> checksum = (Map) props.get("Checksum");
      d.setSize(product.getSize());
      d.setType((String) props.get("ContentType"));
      d.setChecksums(
            Collections.singletonMap(
                  checksum.get(Model.ALGORITHM),
                  checksum.get(Model.VALUE)));
      product.setDownload(d);

      // Sets the download path to LocalPath (if LocalPaths are exposed)
      if (this.remoteIncoming != null && !this.copyProduct)
      {
         String path = (String) props.get("LocalPath");
         if (path != null && !path.isEmpty())
         {
            d.setPath(Paths.get(this.remoteIncoming, path).toString());

            File f = new File(d.getPath());
            if (!f.exists())
            {
               // The incoming path is probably false
               // Throws an exception to notify the admin about this issue
               throw new RuntimeException("ODataSynchronizer: Local file '" + path +
                      "' not found in remote incoming '" + this.remoteIncoming + '\'');
            }
            product.setPath(new URL("file://" + d.getPath()));
         }
         else
         {
            throw new RuntimeException("RemoteIncoming is set" +
                   " but the LocalPath property is missing in remote products");
         }
      }

      // Retrieves the Product Class if not inlined
      ODataEntry pdt_class_e;
      if (entry.containsInlineEntry() && props.get("Class") != null)
      {
         pdt_class_e = ODataEntry.class.cast(props.get("Class"));
      }
      else
      {
         delta = System.currentTimeMillis();
         pdt_class_e = client.readEntry(pdt_p + "/Class", null);
         logODataPerf(pdt_p + "/Class", System.currentTimeMillis() - delta);
      }
      Map<String, Object> pdt_class_pm = pdt_class_e.getProperties();
      String pdt_class = String.class.cast(pdt_class_pm.get("Uri"));
      product.setItemClass(pdt_class);

      // Retrieves Metadata Indexes (aka Attributes on odata) if not inlined
      ODataFeed mif;
      if (entry.containsInlineEntry() && props.get("Attributes") != null)
      {
         mif = ODataDeltaFeed.class.cast(props.get("Attributes"));
      }
      else
      {
         delta = System.currentTimeMillis();
         mif = client.readFeed(pdt_p + "/Attributes", null);
         logODataPerf(pdt_p + "/Attributes", System.currentTimeMillis() - delta);
      }
      List<MetadataIndex> mi_l = new ArrayList<>(mif.getEntries().size());
      for (ODataEntry mie: mif.getEntries())
      {
         Map<String, Object> mi_pm = mie.getProperties();
         MetadataIndex mi = new MetadataIndex();
         String mi_name = (String) mi_pm.get("Name");
         mi.setName(mi_name);
         mi.setType((String) mi_pm.get("ContentType"));
         mi.setValue((String) mi_pm.get("Value"));
         MetadataType mt = METADATA_TYPE_SERVICE.getMetadataTypeByName(pdt_class, mi_name);
         if (mt != null)
         {
            mi.setCategory(mt.getCategory());
            if (mt.getSolrField() != null)
            {
               mi.setQueryable(mt.getSolrField().getName());
            }
         }
         else if (mi_name.equals("Identifier"))
         {
            mi.setCategory("");
            mi.setQueryable("identifier");
         }
         else if (mi_name.equals("Ingestion Date"))
         {
            mi.setCategory("product");
            mi.setQueryable("ingestionDate");
         }
         else
         {
            mi.setCategory("");
         }
         mi_l.add(mi);

      }
      product.setIndexes(mi_l);

      // Retrieves subProducts if not inlined
      ODataFeed subp;
      if (entry.containsInlineEntry() && props.get("Products") != null)
      {
         subp = ODataDeltaFeed.class.cast(props.get("Products"));
      }
      else
      {
         delta = System.currentTimeMillis();
         subp = client.readFeed(pdt_p + "/Products", null);
         logODataPerf(pdt_p + "/Products", System.currentTimeMillis() - delta);
      }
      for (ODataEntry subpe: subp.getEntries())
      {
         String id = (String) subpe.getProperties().get("Id");
         Long content_len = (Long) subpe.getProperties().get("ContentLength");

         String path = (String) subpe.getProperties().get("LocalPath");
         if (this.remoteIncoming != null && !this.copyProduct &&
                path != null && !path.isEmpty())
         {
            path = Paths.get(this.remoteIncoming, path).toString();
         }
         else
         {
            path = client.getServiceRoot() + pdt_p +
                   "/Products('" + subpe.getProperties().get("Id") + "')/$value";
         }

         // Retrieves the Quicklook
         if (id.equals("Quicklook"))
         {
            product.setQuicklookSize(content_len);
            product.setQuicklookPath(path);
         }

         // Retrieves the Thumbnail
         else if (id.equals("Thumbnail"))
         {
            product.setThumbnailSize(content_len);
            product.setThumbnailPath(path);
         }
      }

      // `processed` must be set to TRUE
      product.setProcessed(Boolean.TRUE);

      return product;
   }

   private void save(Product product)
   {
      List<MetadataIndex> metadatas = product.getIndexes();

      // Stores `product` in the database
      product = PRODUCT_SERVICE.addProduct(product);
      product.setIndexes(metadatas); // DELME lazy loading not working atm ...

      // Stores `product` in the index
      try
      {
         long delta = System.currentTimeMillis();
         SEARCH_SERVICE.index(product);
         LOGGER.debug("Synchronizer#" + getId() + " indexed product " +
               product.getIdentifier() + " in " + (System.currentTimeMillis() - delta) + "ms");
      }
      catch (Exception e)
      {
         // Solr errors are not considered fatal
         LOGGER.error("Synchronizer#" + getId() + " Failed to index product " +
               product.getIdentifier() + " in Solr's index", e);
      }

      // Sets the target collection both in the DB and Solr
      if (this.targetCollectionUUID != null)
      {
         try
         {
            COLLECTION_SERVICE.systemAddProduct(this.targetCollectionUUID, product.getId(), false);
         }
         catch (HibernateException e)
         {
            LOGGER.error("Synchronizer#" + getId() + " Failed to set collection#" +
                  this.targetCollectionUUID + " for product " + product.getIdentifier(), e);
            // Reverting ...
            PRODUCT_SERVICE.systemDeleteProduct(product.getId());
            throw e;
         }
         catch (Exception e)
         {
            LOGGER.error("Synchronizer#" + getId() + " Failed to update product " +
                  product.getIdentifier() + " in Solr's index", e);
         }
      }
   }

   /** move file at `path_to_file` to `path_to_dir`. Returns the resulting Path. */
   private Path chDir(Path path_to_file, Path path_to_dir) throws IOException
   {
      Path res = path_to_dir.resolve(path_to_file.getFileName());
      Files.move(path_to_file, res, StandardCopyOption.ATOMIC_MOVE);
      return res;
   }

   /** Retrieves and download new products, downloads are parallelized. */
   private int getAndCopyNewProduct() throws InterruptedException
   {
      int res = 0;
      int count = this.pageSize;
      int skip = 0;

      ParallelizedDownloadManager downloader = new ParallelizedDownloadManager(
            this.pageSize, this.pageSize, 0, TimeUnit.SECONDS,
            new BasicAuthHttpClientProducer(), INCOMING_MANAGER.getTempDir().toPath());

      // Downloads are done asynchronously in another threads
      List<Product> products = new ArrayList<>(this.pageSize);
      List<Future<DownloadResult>[]> futures = new ArrayList<>(this.pageSize);

      try
      {
         // Downloads at least `pageSize` products
         while (count > 0)
         {
            ODataFeed pdf = getPage(skip, false);
            if (pdf.getEntries().isEmpty()) // No more products
            {
               break;
            }

            skip += this.pageSize;

            for (ODataEntry pdt: pdf.getEntries ())
            {
               if (exists(pdt))
               {
                  continue;
               }
               count--;

               Product product = entryToProducts(pdt);
               Future<DownloadResult>[] future = download(downloader, product);
               products.add(product);
               futures.add(future);
            }
         }

         // Get download results from Futures, and create product entries in DB, Solr
         boolean update_lid = true; // Controls whether we are updating LastIngestionDate or not
         for (int it=0; it<products.size(); it++)
         {
            Product product = products.get(it);
            Future<DownloadResult>[] future = futures.get(it);
            try
            {
               DownloadResult prod_res = future[0].get();

               String data_md5 = String.valueOf(Hex.encode(prod_res.md5sum)).toUpperCase();
               String sync_md5 = product.getDownload().getChecksums().get("MD5").toUpperCase();
               if (!data_md5.equals(sync_md5))
               {
                  throw new DigestException(data_md5 + " != " + sync_md5);
               }

               // Asks the Incoming manager for dest directory
               Path dir = Paths.get(INCOMING_MANAGER.getNewIncomingPath().toURI());

               // Sets download info in the product
               Path prod_path = chDir(prod_res.data, dir);
               product.setPath(prod_path.toUri().toURL());
               product.setDownloadablePath(prod_path.toString());
               product.setDownloadableType(prod_res.dataType);
               product.setDownloadableSize(prod_res.dataSize);

               // Sets its QuickLook image (if any)
               if (product.getQuicklookFlag() && future[1] != null)
               {
                  DownloadResult ql_res = future[1].get();
                  Path ql_path = chDir(ql_res.data, dir);
                  product.setQuicklookPath(ql_path.toString());
                  product.setQuicklookSize(ql_res.dataSize);
               }

               // Sets its Thumbnail image (if any)
               if (product.getThumbnailFlag() && future[2] != null)
               {
                  DownloadResult tn_res = future[2].get();
                  Path tn_path = chDir(tn_res.data, dir);
                  product.setThumbnailPath(tn_path.toString());
                  product.setThumbnailSize(tn_res.dataSize);
               }

               save(product);
               if (update_lid)
               {
                  this.lastCreated = product.getIngestionDate();
                  this.dateChanged = true;
               }
               res++;

               LOGGER.info(String.format(
                     "Synchronizer#%d Product %s (%d bytes compressed) successfully synchronized from %s",
                     getId(), product.getIdentifier(), product.getSize(), this.client.getServiceRoot()));
            }
            catch (DigestException | ExecutionException ex)
            {

               if (ex instanceof DigestException)
               {
                  LOGGER.error(String.format("Synchronizer#%d Product %s md5sum comparison failed",
                     getId(), product.getIdentifier()), ex);
               }
               else
               {

                  LOGGER.error(String.format("Synchronizer#%d Product %s failed to download",
                        getId(), product.getIdentifier()), ex);
               }

               // Remove temp files (cleaning up downloaded files)
               for (int ju=0; ju<3; ju++) {
                  if (future[ju] != null)
                  {
                     try
                     {
                        Files.delete(future[ju].get().data);
                     }
                     catch (ExecutionException | IOException none) {}
                  }
               }

               if (update_lid)
               {
                  this.lastCreated = product.getIngestionDate();
                  this.dateChanged = true;
                  // Only update the lastIngestionDate to the first consecutive successful downloads
                  update_lid = false;
               }
            }
         }

      }
      catch (IOException | ODataException ex)
      {
         LOGGER.error ("OData failure", ex);
      }
      catch (InterruptedException ex)
      {
         // Interruption required, stopping downloads and cleaning temp files
         for (Future<DownloadResult>[] future: futures)
         {
            for (Future<DownloadResult> f: future)
            {
               if (f != null)
               {
                  f.cancel(true);
                  try
                  {
                     Files.delete(f.get().data);
                  }
                  catch (CancellationException | ExecutionException | IOException none) {}
               }
            }
         }
      }
      finally
      {
         // Save the ingestionDate of the last created Product
         this.syncConf.setConfig ("last_created", String.valueOf (this.lastCreated.getTime ()));
         downloader.shutdownNow();
      }
      return res;
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
         ODataFeed pdf = getPage(null, true);

         // For each entry, creates a DataBase Object
         for (ODataEntry pdt: pdf.getEntries ())
         {
            if (exists(pdt))
            {
               this.lastCreated = getIngestionDate(pdt);
               this.dateChanged = true;
               continue;
            }

            Product product = entryToProducts(pdt);
            save(product);

            this.lastCreated = product.getIngestionDate ();
            this.dateChanged = true;

            LOGGER.info("Synchronizer#" + getId () + " Product " + product.getIdentifier () +
                  " ("+ product.getDownloadableSize() + " bytes compressed) " +
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
         if (this.copyProduct)
         {
            retrieved = getAndCopyNewProduct();
         }
         else
         {
            retrieved = getNewProducts();
         }
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

   /** Creates a client producer that produces HTTP Basic auth aware clients. */
   private class BasicAuthHttpClientProducer implements HttpAsyncClientProducer
   {
      @Override
      public CloseableHttpAsyncClient generateClient ()
      {
         CredentialsProvider credsProvider = new BasicCredentialsProvider();
         credsProvider.setCredentials(new AuthScope (AuthScope.ANY),
                 new UsernamePasswordCredentials(serviceUser, servicePass));
         RequestConfig rqconf = RequestConfig.custom()
               .setCookieSpec(CookieSpecs.DEFAULT)
               .setSocketTimeout(Timeouts.SOCKET_TIMEOUT)
               .setConnectTimeout(Timeouts.CONNECTION_TIMEOUT)
               .setConnectionRequestTimeout(Timeouts.CONNECTION_REQUEST_TIMEOUT)
               .build();
         CloseableHttpAsyncClient res = HttpAsyncClients.custom ()
               .setDefaultCredentialsProvider (credsProvider)
               .setDefaultRequestConfig(rqconf)
               .build ();
         res.start ();
         return res;
      }
   }
}
