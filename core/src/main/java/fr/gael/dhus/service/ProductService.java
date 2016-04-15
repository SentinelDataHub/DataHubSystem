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
package fr.gael.dhus.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;

import fr.gael.dhus.spring.cache.IncrementCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.exception.DataStoreAlreadyExistException;
import fr.gael.dhus.datastore.exception.DataStoreException;
import fr.gael.dhus.datastore.exception.DataStoreLocalArchiveNotExistingException;
import fr.gael.dhus.datastore.processing.ProcessingManager;
import fr.gael.dhus.datastore.processing.fair.FairRunnable;
import fr.gael.dhus.datastore.processing.fair.FairThreadPoolTaskExecutor;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList;
import fr.gael.dhus.datastore.scanner.FileScannerWrapper;
import fr.gael.dhus.datastore.scanner.Scanner;
import fr.gael.dhus.datastore.scanner.ScannerFactory;
import fr.gael.dhus.datastore.scanner.URLExt;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Event;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Listener;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.drbx.cortex.DrbCortexItemClass;

/**
 * Product Service provides connected clients with a set of method
 * to interact with it.
 */
@Service
public class ProductService extends WebService
{
   private static Log logger = LogFactory.getLog (ProductService.class);
   
   @Autowired
   private ProductDao productDao;
   
   @Autowired
   private CollectionDao collectionDao;
   
   @Autowired
   private CollectionService collectionService;
   
   @Autowired
   private UserDao userDao;
   
   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;
   
   @Autowired
   private FairThreadPoolTaskExecutor taskExecutor;
   
   @Autowired
   private ProcessingManager processingManager;
   
   @Autowired
   private SearchService searchService;

   @Autowired
   private ScannerFactory scannerFactory;
   
   /** Configuration (etc/dhus.xml). */
   @Autowired
   private ConfigurationManager cfgManager;


   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Iterator<Product> getProducts (String filter, Long collection_id,
         int skip)
   {
      return productDao.scrollFiltered (filter.toUpperCase (),
            collection_id, skip);
   }
   
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "product", key = "#path")
   public Product getProduct (URL path)
   {
      return productDao.getProductByPath(path);
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "product", key = "#id")
   public Product systemGetProduct (Long id)
   {
      return productDao.read (id);
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Cacheable (value = "product", key = "#id")
   public Product getProduct (Long id)
   {
      return systemGetProduct (id);
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "products", key = "#ids")
   public List<Product> getProducts (List<Long> ids)
   {
      return productDao.read(ids);
   }


   /**
    * Gets a {@link Product} by its {@code UUID} (Protected).
    * @see #getProduct(java.lang.String)
    * @param uuid UUID unique identifier
    * @return a {@link Product} or {@code null}
    */
   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "product", key = "#uuid")
   public Product getProduct (String uuid)
   {
      return systemGetProduct (uuid);
   }

   /**
    * Gets a {@link Product} by its {@code UUID} (Unprotected).
    * @param uuid UUID unique identifier
    * @return a {@link Product} or {@code null}
    */
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "product", key = "#uuid")
   public Product systemGetProduct (String uuid)
   {
      return productDao.getProductByUuid (uuid, null);
   }

   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "product", key = "#id")
   public Product getProductToDownload (Long id)
   {
      // TODO remove method cause duplicated and not used
      return productDao.read (id);
   }

   @PreAuthorize ("hasAnyRole('ROLE_DOWNLOAD','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public InputStream getProductQuickLook (Long id)
   {
      // TODO remove method cause not used
      Product product = getProduct (id);
      if (!product.getQuicklookFlag ()) return null;
      
      try
      {
         return new FileInputStream (product.getQuicklookPath ());
      }
      catch (Exception e)
      {
         logger.warn ("Cannot retrieve Quicklook from product id #" + id,e);
      }
      return null;
   }

   @PreAuthorize ("hasAnyRole('ROLE_DOWNLOAD','ROLE_SEARCH')")
   public long getProductQuickLookContentLength (Long id)
   {
      // TODO remove method cause not used
      return getProduct (id).getQuicklookSize ();
   }

   @PreAuthorize ("hasAnyRole('ROLE_DOWNLOAD','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public InputStream getProductThumbnail (Long id)
   {
      // TODO remove method cause not used
      Product product = getProduct (id);
      if (!product.getThumbnailFlag ()) return null;
      try
      {
         return new FileInputStream (product.getThumbnailPath ());
      }
      catch (Exception e)
      {
         logger.warn ("Cannot retrieve Thumbnail from product id #" + id,e);
      }
      return null;
   }


   @PreAuthorize ("hasAnyRole('ROLE_DOWNLOAD','ROLE_SEARCH')")
   public long getProductThumbnailContentLength (Long id)
   {
      // TODO remove method cause not used
      return getProduct (id).getThumbnailSize ();
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "product_count", key = "{#filter, #collection_id}")
   public Integer count(String filter, Long collection_id)
   {
      return productDao.count (filter, collection_id, null);
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Transactional (readOnly = true, propagation = Propagation.REQUIRED)
   @Cacheable (value = "product_count", key = "{#filter, null}")
   public Integer count(String filter)
   {
      return productDao.count (filter, null, null);
   }

   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   @Caching(evict = {
         @CacheEvict (value = "indexes", key = "#pid"),
         @CacheEvict (value = "product", key = "#pid"),
         @CacheEvict (value = "products", allEntries = true)})
   @IncrementCache (name = "product_count", key = "all", value = -1)
   public void systemDeleteProduct (Long pid)
   {
      Product product = productDao.read (pid);

      if (product == null)
      {
         throw new DataStoreException ("Product #" + pid +
            " not found in the system.");
      }

      if (product.getLocked ())
      {
         throw new DataStoreException ("Cannot delete product #" + pid +
            ". Product is locked in the system.");
      }
      productDao.delete (product);
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   @Caching (evict = {
         @CacheEvict (value = "product", key = "#pid"),
         @CacheEvict (value = "products", allEntries = true),
         @CacheEvict (value = "indexes", key = "#pid")
   })
   @IncrementCache (name = "product_count", key = "all", value = -1)
   public void deleteProduct(Long pid)
   {
      systemDeleteProduct (pid);
   }

   @PreAuthorize ("isAuthenticated ()")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value="product", key="#uuid")
   public Product getProduct (String uuid, User u)
   {
      return productDao.getProductByUuid (uuid, u);
   }
   
   @PreAuthorize ("isAuthenticated ()")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value="product_count", key="'all'")
   public int countAuthorizedProducts ()
   {
      return productDao.count ();
   }

   public boolean hasAccessToProduct (long user_id, long product_id)
   {
      return true;
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = {"indexes"}, key = "#product_id")
   public List<MetadataIndex>getIndexes(Long product_id)
   {
      Product product = productDao.read (product_id);
      Hibernate.initialize (product.getIndexes ());
      return product.getIndexes ();
   }

   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   @CacheEvict (value = {"indexes"}, key = "#product_id")
   public void setIndexes(Long product_id, List<MetadataIndex>indexes)
   {
      Product product = productDao.read (product_id);
      product.setIndexes (indexes);
      productDao.update (product);
   }

   /**
    * Adds a product in the database, the given product will not be queued for
    * processing nor it will be submitted to the search engine.
    * @param product a product to store in the database.
    * @return the created product.
    * @throws IllegalArgumentException incomplete products are not allowed.
    */
   @Transactional (readOnly = false, propagation = Propagation.REQUIRED)
   @Caching (evict = {
         @CacheEvict (value = "product", allEntries = true),
         @CacheEvict (value = "products", allEntries = true) }
   )
   @IncrementCache (name = "product_count", key = "all", value = 1)
   public Product addProduct (Product product) throws IllegalArgumentException
   {
      URL path = product.getPath ();
      String origin = product.getOrigin ();
      if (path == null || origin == null || origin.isEmpty ())
      {
         throw new IllegalArgumentException ("product must have a path and an origin");
      }
      // FIXME do I have to check every field? isn't it done by hibernate based on column constraints?

      Product final_product = this.productDao.create (product);
      return final_product;
   }

   @Caching(evict = {
         @CacheEvict(value = "product" , allEntries = true),
         @CacheEvict(value = "products", allEntries = true)
   })
   public void addProduct (URL path, User owner,
      final List<Collection> collections, String origin, Scanner scanner, 
      FileScannerWrapper wrapper)
      throws DataStoreAlreadyExistException
   {
      if (productDao.exists (path))
      {
         throw new DataStoreAlreadyExistException ("Product \"" +
            path.toExternalForm () + "\" already present in the system.");
      }

      /* **** CRITICAL SECTION *** */
      /** THIS SECTION SHALL NEVER BE STOPPED BY CNTRL-C OR OTHER SIGNALS */
      /* TODO: check if shutdownHook can protect this section */
      Product product = new Product ();
      product.setPath (path);
      product.setOrigin (origin);
      List<User> users = new ArrayList<User> ();
      
      if (owner != null)
      {
         product.setOwner (owner);
         users.add (userDao.read (owner.getId ()));
         product.setAuthorizedUsers (new HashSet<User> (users));
      }

      product = productDao.create (product);

      // FIX
      product = productDao.read (product.getId ());
      /* **** CRITICAL SECTION *** */
      processProduct (product, owner, collections, scanner, wrapper);
   }

   public void processProduct (Product product, User owner, 
      List<Collection>collections, Scanner scanner,
      FileScannerWrapper wrapper)
   {
      int retry = 10;
      while (retry > 0)
      {
         try
         {
            ProcessingRunnable pr = new ProcessingRunnable (product, owner, 
               collections, scanner, wrapper);
            taskExecutor.execute (pr);
            retry = 0;
         }
         catch (RejectedExecutionException ree)
         {
            retry--;
            if (retry <= 0) throw ree;
            try
            {
               Thread.sleep (500);
            }
            catch (InterruptedException e)
            {
               logger.warn ("Current thread has interrupted by another!", e);
            }
         }
      }
   }
   
   /**
    * Odata dedicated Services
    */
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value="products",
      key="{#collection?.id, #filter, #order, #skip, #top}")
   public List<Product> getProducts (Collection collection, String filter, 
      String order, int skip,int top)
   {
      return collectionDao.getAuthorizedProducts (null, collection, filter, order, skip, top);
   }
   
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "product_count", key = "{#filter, #collection?.id}")
   public int count (Collection collection, String filter)
   {
      if (collection == null)
      {
         return this.count (filter);
      }
      return this.count (filter, collection.getId ());
   }
   
   
   /*
    * Reported from DefaultDataStrore
    */
   private class ProcessingRunnable extends FairRunnable
   {
      Product product;
      User owner;
      Scanner scanner;
      List<Collection>collections;
      FileScannerWrapper wrapper;
      
      public ProcessingRunnable (Product product, User owner, 
         List<Collection> collections, Scanner scanner,
         FileScannerWrapper wrapper)
      {
         super (scanner == null ? null : scanner.toString ());
         this.product = product;
         this.owner = owner;
         this.collections = collections;
         this.scanner = scanner;
         this.wrapper = wrapper;
      }

      @Transactional (readOnly = false, propagation = Propagation.REQUIRED)
      @IncrementCache (name = "product_count", key = "all", value = 1)
      public void run ()
      {
         if (scanner != null && scanner.isStopped ())
         {
            logger.info("Scanner stopped, deleting product #"+product.getId ());
            if (wrapper != null)
            {
               wrapper.error (product,
                  new InterruptedException ("Processing stopped by the user"));
            }
            productDao.delete (product);
            return;
         }
         logger.debug ("Add product \"" +
            ProductDao.getPathFromProduct (product) + "\".");

         try
         {
            long processing_start = System.currentTimeMillis ();

            if (wrapper != null)
            {
               wrapper.startIngestion ();
            }

            processingManager.process (product);
            productDao.update (product);
            searchService.index(product);

            if (collections != null)
            {
               for (Collection c : collections)
               {
                  collectionService.systemAddProduct (
                        c.getId (), product.getId (), true);
               }
            }

            if (wrapper != null)
            {
               wrapper.endIngestion ();
            }

            long processing_end = System.currentTimeMillis ();
            logger.info ("Ingestion processing complete for product " +
               product.getPath ().toExternalForm () + " (" +
               product.getSize () + " bytes, " +
               product.getDownloadableSize () + " bytes compressed)" + " in " +
               (processing_end - processing_start) + "ms.");

            actionRecordWritterDao.uploadEnd (this.product.getPath (),
               this.owner.getUsername (), collections, true);
         }
         catch (Exception excp)
         {
            logger.warn ("Unrecoverable error happen during ingestion of " +
                     product.getPath () + " (removing from database)", excp);
            try
            {
               productDao.delete (product);
            }
            catch (Exception e)
            {
               logger.error (
                  "Unable to remove product after ingestion failure", e);
            }
            if (wrapper != null)
            {
               wrapper.error (product, excp);
            }

            if ((this.product.getPath () !=null) &&
                (this.owner != null))
            {
               actionRecordWritterDao.uploadFailed (
                  this.product.getPath ().toString (), this.owner.getUsername ());
               actionRecordWritterDao.uploadEnd (this.product.getPath (),
                  this.owner.getUsername (), collections, false);
            }
            return;
         }
      }
   }
   
   // Check if product present is the DB is still present into the repository.
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void checkDBProducts ()
   {
      logger.info ("Syncing database with repositories...");
      Iterator<Product> products = productDao.getAllProducts ();
      while (products.hasNext ())
      {
         Product product = products.next ();
         if ( !ProductService.checkUrl (product.getPath()))
         {
            logger.info ("Removing Product " + product.getPath () +
               " not found in repository.");
            products.remove ();
         }
         else
            logger.info ("Product " + product.getPath () +
               " found in repository.");
      }
   }

   private static boolean checkUrl (URL url)
   {
      Objects.requireNonNull (url, "`url` parameter must not be null");

      // OData Synchronized product, DELME
      if (url.getPath ().endsWith ("$value"))
      {
         // Ignoring ...
         return true;
      }

      // Case of simple file
      try
      {
         File f = new File (url.toString ());
         if (f.exists ()) return true;
      }
      catch (Exception e)
      {
         logger.debug ("url \"" + url + "\" not formatted as a file");
      }

      // Case of local URL
      try
      {
         URI local = new File (".").toURI ();
         URI uri = local.resolve (url.toURI ());
         File f = new File (uri);
         if (f.exists ()) return true;
      }
      catch (Exception e)
      {
         logger.debug ("url \"" + url + "\" not a local URL");
      }

      // Case of remote URL
      try
      {
         URLConnection con = url.openConnection ();
         con.connect ();
         InputStream is = con.getInputStream ();
         is.close ();
         return true;
      }
      catch (Exception e)
      {
         logger.debug ("url \"" + url + "\" not a remote URL");
      }
      // Unrecovrable case
      return false;
   }
   
   /**
    * Performs directory structure scan to retrieve relevant products, and run
    * declared processing.
    * 
    * @param archive the archive to be scan.
    * @param productDao Data access object to products.
    * @param indexDao Data access object to index in the products.
    * @throws InterruptedException if user 
    */
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public int processArchiveSync ()
      throws DataStoreLocalArchiveNotExistingException, InterruptedException
   {
      String archivePath = cfgManager.getArchiveConfiguration ().getPath ();
      File archive = new File(archivePath);
      if (!archive.exists ())
      {
         throw new DataStoreLocalArchiveNotExistingException (
               "Local archive \"" + archivePath + "\" does not exist.");
      }
      
      logger.info ("Looking for new product in archive \"" + 
               archivePath + "\".");

      final List<DrbCortexItemClass> supported =
         scannerFactory.getScannerSupport ();
      
      Scanner scanner =
         scannerFactory.getScanner (archivePath);
      scanner.setSupportedClasses (supported);
      AsynchronousLinkedList<URLExt> list = scanner.getScanList ();
      final Scanner s = scanner;

      list.addListener (new Listener<URLExt> ()
      {
         @Override
         public void addedElement (final Event<URLExt> e)
         {
            try
            {
               URL url = e.getElement ().getUrl ();
               if (productDao.getProductByOrigin (url.toString ()) != null ||
                  productDao.exists (url))
               {
                  throw new DataStoreAlreadyExistException (
                     "Already in database");
               }

               logger.info ("Adding product \"" + url + "\".");
               addProduct (url, userDao.getRootUser (), null, null, s, null);
            }
            catch (DataStoreAlreadyExistException excp)
            {
               logger.info ("Product already in database : \"" +
                  e.getElement ().getUrl ().toString () + "\".");
            }
            catch (DataStoreException excp)
            {
               logger.error ("Cannot add product \"" +
                  e.getElement ().toString () + "\"", excp);
            }
         }

         @Override
         public void removedElement (Event<URLExt> e)
         {
         }
      });
      return scanner.scan ();
   }


   /**
    * Do process all products in database that their ingestion is not finished.
    * If provided parameter is null, default processing consists in removing 
    * the data from database. Otherwise, the provided processing is launched.
    * @param proc the processing to execute. if null, remove processing will 
    *             be performed. 
    * @return the list of products reprocessed.
    */
   @Transactional (readOnly = false, propagation = Propagation.REQUIRED)
   @CacheEvict (
         value = { "product_count", "product", "products" },
         allEntries = true
   )
   public void processUnprocessed (boolean recover)
   {
      long start = new Date ().getTime ();

      if (!recover)
      {
         Iterator<Product> products = getUnprocessedProducts ();
         while (products.hasNext ())
         {
            products.next ();
            products.remove ();
         }

         logger.debug ("Cleanup incomplete processed products in " +
               (new Date ().getTime () - start) + "ms");
      }
      else
      {
         Iterator<Product> products = getUnprocessedProducts ();
         while (products.hasNext ())
         {
            Product product = products.next ();
            // Do reporcess only already transfered products
            if (product.getPath ().toString ().equals (product.getOrigin ()))
            {
               products.remove ();
            }
            else
            {
               try
               {
                  String path = product.getPath ().getPath ();
                  logger.info ("Recovering product from " + path);
                  // Check if product is still present in repository
                  if (!new File (path).exists ())
                  {
                     throw new DataStoreException ("Product " + path +
                           " not present locally.");
                  }
                  // Retrieve owner if any
                  User owner = productDao.getOwnerOfProduct (product);

                  // Retrieve collections
                  List<Collection> collections = collectionDao.
                        getCollectionsOfProduct (product.getId ());

                  processProduct (product, owner, collections, null, null);
               }
               catch (Exception e)
               {
                  logger.error ("Error while processing: " +
                        e.getMessage () + "- abort reprocessing.");
                  products.remove ();
               }
            }
         }
      }
   }

   @Transactional (readOnly = true)
   public Iterator<Product> getUnprocessedProducts ()
   {
      return productDao.getUnprocessedProducts ();
   }
}
