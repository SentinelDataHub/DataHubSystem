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
package fr.gael.dhus.datastore;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.dao.interfaces.DaoUtils;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.exception.DataStoreAlreadyExistException;
import fr.gael.dhus.datastore.exception.DataStoreException;
import fr.gael.dhus.datastore.exception.DataStoreLocalArchiveNotExistingException;
import fr.gael.dhus.datastore.exception.DataStoreNotReadableProduct;
import fr.gael.dhus.datastore.processing.Processing;
import fr.gael.dhus.datastore.processing.ProcessingEvent;
import fr.gael.dhus.datastore.processing.ProcessingFactory;
import fr.gael.dhus.datastore.processing.ProcessingListener;
import fr.gael.dhus.datastore.processing.ProcessingProduct;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Event;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Listener;
import fr.gael.dhus.datastore.scanner.Scanner;
import fr.gael.dhus.datastore.scanner.ScannerFactory;
import fr.gael.dhus.datastore.scanner.URLExt;
import fr.gael.dhus.search.SolrDao;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.drbx.cortex.DrbCortexItemClass;

/**
 * @author pidancier
 */
@Component
public class DefaultDataStore implements DataStore
{
   private static Logger logger = Logger.getLogger (DefaultDataStore.class);

   
   @Autowired
   private ProductDao productDao;

   @Autowired
   private UserDao userDao;
   
   @Autowired
   private SolrDao solrDao;

   @Autowired
   private CollectionDao collectionDao;

   @Autowired
   private ScannerFactory scannerFactory;

   @Autowired
   private TaskExecutor taskExecutor;

   @Autowired
   private ProcessingFactory processingFactory;
   
   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;

   @Autowired
   private ConfigurationManager cfgManager;

   public void addProduct (URL path, User owner,
      final List<Collection> collections, String origin, Scanner scanner, 
      ProcessingListener listener)
      throws DataStoreAlreadyExistException
   {
      if (productDao.exists (path))
         throw new DataStoreAlreadyExistException ("Product \"" +
            path.toExternalForm () + "\" already present in the system.");

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
      
      if (cfgManager.isDataPublic ())
         users.add (userDao.read (userDao.getPublicData ().getId ()));

      product = productDao.create (product);
            
      if (collections != null)
      {
         for (Collection c : collections)
         {
            collectionDao.addProduct (c.getId (), product.getId ());
         }
      }
      // FIX
      product = productDao.read (product.getId ());
      /* **** CRITICAL SECTION *** */
      processProduct (product, owner, collections, scanner, listener);
   }

   public void processProduct (Product product, User owner, 
      List<Collection>collections, Scanner scanner,
      ProcessingListener listener)
   {
      int retry = 10;
      while (retry > 0)
      {
         try
         {
            ProcessingRunnable pr = new ProcessingRunnable (product, owner, 
               collections, scanner);
            if (listener != null) pr.addListener (listener);
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
               ;
            }
         }
      }
   }

   public void removeProduct (Long pId)
   {
      Product product = productDao.read (pId);

      if (product == null)
      {
         throw new DataStoreException ("Product #" + pId +
            " not found in the system.");
      }

      if (product.getLocked ())
      {
         throw new DataStoreException ("Cannot delete product #" + pId +
            ". Product is locked in the system.");
      }

      productDao.delete (product);
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
   public int processArchiveSync (final boolean async) 
      throws DataStoreLocalArchiveNotExistingException, InterruptedException
   {
      String archivePath = cfgManager.getArchiveConfiguration ().getPath ();
      File archive = new File(archivePath);
      if (!archive.exists ())
      {
         throw new DataStoreLocalArchiveNotExistingException ("Local archive \"" + 
                  archivePath + "\" does not exist.");
      }
      
      logger.info ("Looking for new product in archive \"" + 
               archivePath + "\".");

      final List<DrbCortexItemClass> supported =
         scannerFactory.getScannerSupport ();
      
      Scanner scanner =
         scannerFactory.getScanner (archivePath);
      scanner.setSupportedClasses (supported);
      AsynchronousLinkedList<URLExt> list = scanner.getScanList ();

      list.addListener (new Listener<URLExt> ()
      {
         @Override
         public void addedElement (final Event<URLExt> e)
         {
            try
            {
               if (productDao.getProductByOrigin (e.getElement ().getUrl ()
                  .toString ()) != null)
                  throw new DataStoreAlreadyExistException (
                     "Already in database");
               if (async)
               {
                  taskExecutor.execute (new AddProductTask (e.getElement ()
                     .getUrl ()));
               }
               else
                  new AddProductTask (e.getElement ().getUrl ()).run ();
            }
            catch (DataStoreAlreadyExistException excp)
            {
               logger.info ("Product already in database : \"" +
                  e.getElement ().getUrl ().toString () + "\".");
            }
            catch (DataStoreNotReadableProduct excp)
            {
               logger.error ("Cannot add product \"" +
                  e.getElement ().toString () + "\"", excp);
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

   
   
   private class AddProductTask implements Runnable
   {
      private URL url;

      public AddProductTask (URL url)
      {
         this.url = url;
      }

      public void run ()
      {
         if ( !productDao.exists (url))
         {
            logger.info ("Adding product \"" + url.getFile () + "\".");
            addProduct (url, userDao.getRootUser (), null, null, null, null);
         }
      }
   }


   class ProcessingRunnable implements Runnable
   {
      Product product;
      User owner;
      Scanner scanner;
      List<Collection>collections;
      List<ProcessingListener> listeners = new ArrayList<ProcessingListener> ();
      
      

      public ProcessingRunnable (Product product, User owner, 
         List<Collection>collections, Scanner scanner)
      {
         this.product = productDao.read (product.getId ());
         this.owner = owner;
         this.collections = collections;
         this.scanner = scanner;
      }

      public void run ()
      {
         fireStartIngestionEvent (new ProcessingEvent (product));
         if (scanner != null && scanner.isStopped ())
         {
            logger.info("Scanner stopped, deleting product #"+product.getId ());
            fireErrorEvent (new ProcessingEvent (product,
               new InterruptedException ("Processing stopped by the user")));
            productDao.delete (product);
            return;
         }
         logger.debug ("Add product \"" +
            ProductDao.getPathFromProduct (product) + "\".");

         boolean processed = true;

         long processing_start = System.currentTimeMillis ();

         for (Processing<?> proc : processingFactory.getProcessings ())
         {
            try
            {
               long start = System.currentTimeMillis ();
               logger.info ("* Ingestion Processing : '" + proc.getLabel () +
                  "' started.");
               if (proc instanceof ProcessingProduct)
               {
                  fireStartEvent (new ProcessingEvent (proc, product));
                  ((ProcessingProduct) proc).run (product);
                  fireEndEvent (new ProcessingEvent (proc, product));
               }
               long end = System.currentTimeMillis ();
               logger.info ("* Ingestion Processing '" + proc.getLabel () +
                  "' done in " + (end - start) + "ms.");
            }
            catch (Exception excp)
            {
               fireErrorEvent (new ProcessingEvent (proc, product, excp));
               logger.error (
                  "Processing called \"" + proc.getLabel () +
                     "\" Cannot process product " +
                     ProductDao.getPathFromProduct (product), excp);
               processed = false;
               break;
            }
         }
         // Validates that the product is well processed.
         product.setProcessed (processed);
         
         try
         {
            productDao.update (product);
         }
         catch (Exception e)
         {
            // Exception appears at system stop, while the processing is 
            // still running and database already stopped
            // Do never continue after such a problem...
            logger.error ("Cannot save processed information: exited.");
            return;
         }
         
         solrDao.setProcessed (product);

         long processing_end = System.currentTimeMillis ();
         if ( !processed)
         {
            logger.warn ("Unrecoverable error happen during ingestion of " +
               product.getPath () + " (removed from database)");
            try
            {
               productDao.delete (product);
            }
            catch (Exception e)
            {
               logger.error (
                  "Unable to remove product after processing failure", e);
            }
            actionRecordWritterDao.uploadFailed (
               this.product.getPath ().toString (), this.owner.getUsername ());
            actionRecordWritterDao.uploadEnd (this.product.getPath (), 
               this.owner.getUsername (), collections, false);
            return;
         }

         logger.info ("Ingestion processing complete for product " +
            product.getPath ().toExternalForm () + " in " +
            (processing_end - processing_start) + "ms.");
         
         actionRecordWritterDao.uploadEnd (this.product.getPath (), 
            this.owner.getUsername (), collections, true);

         fireEndIngestionEvent (new ProcessingEvent (product));
      }
      
      public void addListener (ProcessingListener listener)
      {
         listeners.add (listener);
      }

      public void removeListener (ProcessingListener listener)
      {
         listeners.remove (listener);
      }

      public List<ProcessingListener>  getListeners ()
      {
         return listeners;
      }
      
      protected void fireStartEvent (ProcessingEvent event)
      {
         for (ProcessingListener listener : getListeners ())
         {
            listener.start (event);;
         }
      }
      protected void fireEndEvent (ProcessingEvent event)
      {
         for (ProcessingListener listener : getListeners ())
         {
            listener.end (event);;
         }
      }
      protected void fireErrorEvent (ProcessingEvent event)
      {
         for (ProcessingListener listener : getListeners ())
         {
            listener.error (event);;
         }
      }
      
      protected void fireStartIngestionEvent(ProcessingEvent event)
      {
         for (ProcessingListener listener : getListeners ())
         {
            listener.startIngestion (event);;
         }
      }
      protected void fireEndIngestionEvent(ProcessingEvent event)
      {
         for (ProcessingListener listener : getListeners ())
         {
            listener.endIngestion (event);;
         }
      }

   }
   
   
   // Check if product present is the DB is still present into the repository.
   public void checkDBProducts ()
   {
      logger.info ("Syncing database with repositories...");
      Iterator<Product> products;
      int top = DaoUtils.DEFAULT_ELEMENTS_PER_PAGE;
      int loop = productDao.count () / top;
      if (productDao.count () % top != 0) loop += 1;
      int removed = 0;
      for (int i = 0; i < loop; i++)
      {
         products = productDao.scroll (null, (top*i)-removed, top).iterator ();
         while (products.hasNext ())
         {
            Product product = products.next ();
            if ( !DefaultDataStore.checkUrl (ProductDao
               .getPathFromProduct (product)))
            {
               logger.info ("Removing Product " + product.getPath () +
                  " not found in repository.");
               removeProduct (product.getId ());
               removed++;
            }
            else
               logger.info ("Product " + product.getPath () +
                  " found in repository.");
         }
      }
   }

   public static boolean checkUrl (String url)
   {
      if (url == null) throw new NullPointerException ("url cannot be null.");

      // Case of simple file
      try
      {
         File f = new File (url);
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
         URI uri = local.resolve (url);
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
         URL u = new URL (url);
         URLConnection con = u.openConnection ();
         con.connect ();
         InputStream is = con.getInputStream ();
         return is != null;
      }
      catch (Exception e)
      {
         logger.debug ("url \"" + url + "\" not a remote URL");
      }
      // Unrecovrable case
      return false;
   }
}
