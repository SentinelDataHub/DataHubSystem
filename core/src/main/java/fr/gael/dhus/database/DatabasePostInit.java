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
package fr.gael.dhus.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.reasoner.IllegalParameterException;

import fr.gael.dhus.database.dao.ActionRecordReaderDao;
import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.CountryDao;
import fr.gael.dhus.database.dao.FileScannerDao;
import fr.gael.dhus.database.dao.NetworkUsageDao;
import fr.gael.dhus.database.dao.ProductCartDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.SearchDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.dao.interfaces.DaoUtils;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.HierarchicalDirectoryBuilder;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.datastore.exception.DataStoreLocalArchiveNotExistingException;
import fr.gael.dhus.datastore.processing.ProcessingUtils;
import fr.gael.dhus.server.http.web.WebPostProcess;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.SearchService;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Initialization to be executed of database when all the service started
 */
@Component
public class DatabasePostInit extends WebPostProcess
{
   private static Log logger = LogFactory.getLog (DatabasePostInit.class);

   @Autowired
   private CountryDao countryDao;
   
   @Autowired
   private ProductService productService;

   @Autowired
   private ProductDao productDao;

   @Autowired
   private CollectionDao collectionDao;

   @Autowired
   private SearchService searchService;

   @Autowired
   private UserDao userDao;
   
   @Autowired
   private FileScannerDao fileScannerDao;
   
   @Autowired
   private NetworkUsageDao networkUsageDao;

   @Autowired
   private ActionRecordReaderDao actionRecordDao;

   @Autowired
   private SearchDao searchDao;
   
   @Autowired
   private ProductCartDao cartDao;

   @Autowired
   private TaskExecutor taskExecutor;
   
   @Autowired
   private IncomingManager incomingManager;
   
   @Autowired
   private ConfigurationManager cfgManager;

   @Override
   public void launch ()
   {
      initDefaultArchiveSettings ();
   }

   /**
    * Initializes archive settings: at this step, we consider only one archive
    * is configured into the system. If an archive is already present in the
    * database, methods checks if it is present. If configured archive not
    * available, it is removed and a new archive is created. If the archive path
    * was changed, is is upgraded accordingly.
    */
   private void initDefaultArchiveSettings ()
   {
      // Update User table with countries synonyms
      updateUserCountries ();
      // Reset the file scanners
      fileScannerDao.resetAll ();
      // Displays database raws statistics
      printDatabaseRowCounts ();
      
      // Not processed product management: when ingestion has been stopped
      // software stop, reprocessed products...  
      processUnprocessed ();

      // User startup commands
      doIncomingRepopulate ();
      if ( !doForceReset ())
         doProductReindex ();

      // Reload the archive on user request
      doSynchronizeLocalArchive ();
      doArchiveCheck ();

      // delete old search queries
      inactiveOldSearchQueries ();

      doforcePublic ();
   }

   private void printDatabaseRowCounts ()
   {
      logger.info ("Database tables rows :");
      logger.info ("  Products       = " + productDao.count() + " rows.");
      logger.info ("  Collections    = " + collectionDao.count() + " rows.");
      logger.info ("  Users          = " + userDao.count() + " rows.");
      logger.info ("  Network Usage  = " + networkUsageDao.count() + " rows.");
      logger.info ("  Logs           = " + actionRecordDao.count() + " rows.");
      logger.info ("    Logons    = " + actionRecordDao.countLogons ()
            + " rows.");
      logger.info ("    Downloads = " + actionRecordDao.countDownloads ()
            + " rows.");
      logger.info ("    Searches  = " + actionRecordDao.countSearches ()
            + " rows.");
      logger.info ("    Uploads   = " + actionRecordDao.countUploads ()
            + " rows.");
      logger.info ("  File scanners  = " + fileScannerDao.count() + " rows.");
      logger.info ("  Saved searches = " + searchDao.count() + " rows.");
      logger.info ("  User carts     = " + cartDao.count() + " rows.");
   }

   /**
    * Run recovery of stopped scanners.
    * WARNING: do never perform Archive.check when recovery is expected. This 
    *    may cause data lost.
    */
   private void processUnprocessed ()
   {
      boolean clean_processings = Boolean.getBoolean (
         "Archive.processings.clean");
      
      logger.info ("Archives processing clean instead of recovery " +
         "(Archive.processings.clean) requested by user (" + 
               clean_processings + ")");
      productService.processUnprocessed (!clean_processings);
   }

   private boolean doIncomingRepopulate ()
   {
      boolean force_relocate = Boolean.getBoolean ("Archive.incoming.relocate");

      logger.info ("Archives incoming relocate (Archive.incoming.relocate)" +
         " requested by user (" + force_relocate + ")");

      if ( !force_relocate) return false;
      
      String incoming_path = System.getProperty (
            "Archive.incoming.relocate.path",
            incomingManager.getIncomingBuilder ().getRoot ().getPath ());

      // Force reset the counter.
      HierarchicalDirectoryBuilder output_builder = 
        new HierarchicalDirectoryBuilder (new File(incoming_path), cfgManager.
        getArchiveConfiguration ().getIncomingConfiguration ().getMaxFileNo ());

      Iterator<Product> products = productDao.getAllProducts ();
      while (products.hasNext ())
      {
         Product product = products.next ();
         boolean shared_path = false;

         // Copy the product path
         File old_path = new File (product.getPath ().getPath ());
         File new_path = null;

         // Check is same products are use for path and download
         if (product.getDownloadablePath ().equals(old_path.getPath()))
            shared_path = true;

         if (incomingManager.isInIncoming (old_path))
         {
            new_path = getNewProductPath (output_builder);
            try
            {
               logger.info ("Relocate " + old_path.getPath () + " to " +
                  new_path.getPath ());
               FileUtils.moveToDirectory (old_path, new_path, true);

               File path = old_path;
               while ( !incomingManager.isAnIncomingElement (path))
               {
                  path = path.getParentFile ();
               }
               FileUtils.cleanDirectory (path);
            }
            catch (IOException e)
            {
               logger.error ("Cannot move directory " + old_path.getPath () +
                  " to " + new_path.getPath (), e);
               logger.error ("Aborting relocation process.");
               return false;
            }

            URL product_path = null;
            try
            {
               product_path =
                  new File (new_path, old_path.getName ()).toURI ().toURL ();
            }
            catch (MalformedURLException e)
            {
               logger.error ("Unrecoverable error : aboting relocate.", e);
               return false;
            }
            product.setPath (product_path);
            // Commit this change
            productDao.update (product);
            searchService.index(product);
         }

         // copy the downloadable path
         if (product.getDownload ().getPath () != null)
         {
            if (shared_path)
            {
               product.getDownload ().setPath (product.getPath ().getPath());
            }
            else
            {
               new_path = getNewProductPath (output_builder);
               old_path = new File (product.getDownload ().getPath ());
               try
               {
                  logger.info ("Relocate " + old_path.getPath () + " to " +
                     new_path.getPath ());
                  FileUtils.moveFileToDirectory (old_path, new_path, false);
               }
               catch (IOException e)
               {
                  logger.error (
                     "Cannot move downloadable file " + old_path.getPath () +
                        " to " + new_path.getPath (), e);
                  logger.error ("Aborting relocation process.");
                  return false;
               }
               product.getDownload ().setPath (
                  new File (new_path, old_path.getName ()).getPath ());
               // Commit this change
            }
            productDao.update (product);
         }

         // Copy Quicklooks
         new_path = null;
         if (product.getQuicklookFlag ())
         {
            old_path = new File (product.getQuicklookPath ());
            if (new_path == null)
               new_path = output_builder.getDirectory ();
            try
            {
               logger.info ("Relocate " + old_path.getPath () + " to " +
                  new_path.getPath ());
               FileUtils.moveToDirectory (old_path, new_path, false);
            }
            catch (IOException e)
            {
               logger.error (
                  "Cannot move quicklook file " + old_path.getPath () +
                  " to " + new_path.getPath (), e);
               logger.error ("Aborting relocation process.");
               return false;
            }
            File f = new File (new_path, old_path.getName ());
            product.setQuicklookPath (f.getPath ());
            product.setQuicklookSize (f.length ());
            productDao.update (product);
         }
         // Copy Thumbnails in the same incoming path as quicklook
         if (product.getThumbnailFlag ())
         {
            old_path = new File (product.getThumbnailPath ());
            if (new_path == null)
               new_path = output_builder.getDirectory ();
            try
            {
               logger.info ("Relocate " + old_path.getPath () + " to " +
                  new_path.getPath ());

               FileUtils.moveToDirectory (old_path, new_path, false);
            }
            catch (IOException e)
            {
               logger.error (
                  "Cannot move thumbnail file " + old_path.getPath () +
                  " to " + new_path.getPath (), e);
               logger.error ("Aborting relocation process.");
               return false;
            }
            File f = new File (new_path, old_path.getName ());
            product.setThumbnailPath (f.getPath ());
            product.setThumbnailSize (f.length ());
            productDao.update (product);
         }
      }
      // Remove unused directories
      try
      {
         cleanupIncoming (incomingManager.getIncomingBuilder ().getRoot ());
      }
      catch (Exception e)
      {
         logger.error ("Cannot cleanup incoming folder", e);
      }
      return true;
   }
   
   private File getNewProductPath (HierarchicalDirectoryBuilder builder)
   {
      File file = new File (builder.getDirectory (), 
         IncomingManager.INCOMING_PRODUCT_DIR);
      file.mkdirs ();
      return file;
   }

   /**
    * Recursively walk a directory tree and remove all the empty directory if
    * they are a part of {@link HierarchicalDirectoryNode} tree.
    *
    * @param aStartingDir is a valid directory, which can be read.
    */
   private void cleanupIncoming (File starting_dir)
   {
      if ( !starting_dir.isDirectory () && !starting_dir.canWrite ())
      {
         throw new IllegalParameterException (
            "starting dir shall be a writable directory.");
      }
      File[] files = starting_dir.listFiles ();
      for (File file : files)
      {

         if (incomingManager.isAnIncomingElement (file)) cleanupIncoming (file);
      }
      // recheck the children list to know if this direct can be removed.
      files = starting_dir.listFiles ();
      if (files.length == 0)
      {
         logger.info ("deleting empty folder :" + starting_dir.getPath ());
         starting_dir.delete ();
      }
   }

   private boolean doForceReset ()
   {
      // Case of user force reset requested.
      boolean force_reset = Boolean.getBoolean ("Archive.forceReset");
      logger.info ("Archives Reset (Archive.forceReset) " +
         "requested by user (" + force_reset + ")");
      if ( !force_reset)
      {
         return false;
      }

      productDao.deleteAll ();
      return true;
   }

   private boolean doProductReindex ()
   {
      boolean force_reindex = Boolean.getBoolean ("Archive.forceReindex");

      logger.info ("Archives Reindex (Archive.forceReindex) " +
         "requested by user (" + force_reindex + ")");
      if ( !force_reindex)
      {
         return false;
      }

        Iterator<Product> products = productDao.getAllProducts ();
         while (products.hasNext ())
         {
            Product product = products.next ();
            int retry = 10;
            while (retry > 0)
            {
               try
               {
                  // Must read the product again because
                  // ScrollableResultsIterator
                  // use its own session.
                  taskExecutor.execute (new IndexProductTask (productDao
                     .read (product.getId ())));
                  retry = 0;
               }
               catch (RejectedExecutionException ree)
               {
                  retry--;
                  if (retry <= 0) throw ree;
                  try
                  {
                     Thread.sleep (5000);
                  }
                  catch (InterruptedException e)
                  {
                     logger.warn (
                           "Current thread has interrupted by another", e);
                  }
               }
            }
         }
      return true;
   }

   private void doSynchronizeLocalArchive ()
   {
      boolean synchronizeLocal = Boolean.getBoolean (
            "Archive.synchronizeLocal");

      logger.info ("Local archive synchronization " +
         "(Archive.synchronizeLocal) requested by user (" + synchronizeLocal
            + ")");
      if ( !synchronizeLocal) return;
      try
      {
         productService.processArchiveSync ();
      }
      catch (DataStoreLocalArchiveNotExistingException e)
      {
         logger.warn (e.getMessage ());
      }
      catch (InterruptedException e)
      {
         logger.info ("Process interrupted by user.");
      }
   }

   private void doArchiveCheck ()
   {
      boolean force_check = Boolean.getBoolean ("Archive.check");

      logger.info ("Archives check (Archive.check) requested by user (" +
         force_check + ")");
      if ( !force_check) return;

      try
      {
         logger.info ("Control of Database coherence...");
         long start = new Date ().getTime ();
         productService.checkDBProducts ();
         logger.info ("Control of Database coherence spent " +
            (new Date ().getTime () - start) + " ms");

         logger.info ("Control of Indexes coherence...");
         start = new Date ().getTime ();
         searchService.checkIndex();
         logger.info ("Control of Indexes coherence spent " +
            (new Date ().getTime () - start) + " ms");

         logger.info ("Control of incoming folder coherence...");
         start = new Date ().getTime ();
         incomingManager.checkIncomming ();
         logger.info ("Control of incoming folder coherence spent " +
            (new Date ().getTime () - start) + " ms");

         logger.info ("Optimizing database...");
         DaoUtils.optimize ();
      }
      catch (Exception e)
      {
         logger.error ("Cannot check DHus Archive.", e);
      }
   }

   private void doforcePublic ()
   {
      boolean force_public = Boolean.getBoolean ("force.public");

      logger.info ("Force public (force.public) requested by user (" +
         force_public + ")");
      if ( !force_public) return;

      Thread t = new Thread (new Runnable()
      {
         @Override
         public void run ()
         {
            Iterator<Collection> collections = collectionDao.getAllCollectons ();
            while (collections.hasNext ())
            {
               Collection collection = collectionDao.read(collections.next ().getId ());
               List<User> authUsers = collectionDao.getAuthorizedUsers (collection);
               if (collection.getId () == collectionDao.getRootCollection ().getId ())
               {
                  if (authUsers.contains (userDao.getPublicData ()))
                  {
                     authUsers.remove (userDao.getPublicData ());
                  }
                  else
                  {
                     continue;
                  }
               }
               else
               {
                  if (!authUsers.contains (userDao.getPublicData ()))
                  {
                     authUsers.add (userDao.getPublicData ());
                  }
                  else
                  {
                     continue;
                  }
               }
               collection.setAuthorizedUsers (new HashSet<User> (authUsers));
               collectionDao.update (collection);
            }
            logger.info ("Force public (force.public) ended.");
         }
      });
      t.start ();
   }

   private class IndexProductTask implements Runnable
   {
      private Product product;

      public IndexProductTask (Product product)
      {
         this.product = product;
      }

      public void run ()
      {
         logger.info ("Re-indexing Product " + product.getPath ().getFile () +
            "...");
         // retrieve ingestion Date
         MetadataIndex ingestion_date = null;
         for (MetadataIndex idx : productService.getIndexes (product.getId ()))
         {
            if ("ingestionDate".equals (idx.getQueryable ()))
            {
               ingestion_date = new MetadataIndex (idx);
               break;
            }
         }
         if (ingestion_date == null)
         {
            SimpleDateFormat df =
               new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            ingestion_date =
               new MetadataIndex ("Ingestion Date", null, "product",
                  "ingestionDate",
                  df.format (product.getUpdated () != null ? product
                     .getUpdated () : new Date ()));
         }
         MetadataIndex identifier = new MetadataIndex ("Identifier",
            null, "", "identifier", product.getIdentifier ());
         List<MetadataIndex> indexes =
                  ProcessingUtils.getIndexesFrom (product.getPath ());
         if (indexes == null)
         {
            logger.error ("Index cannot be extracted from " +
               product.getPath ());
            logger.error ("Removing Product from database...");
            try
            {
               productService.systemDeleteProduct (product.getId ());
            }
            catch (Exception e)
            {
               logger.error ("Cannot remove product " + product.getPath (), e);
            }
            return;
         }
         indexes.add (ingestion_date);
         indexes.add (identifier);

         product.setIndexes (indexes);

         // Footprint shall also be re-processed.

         for (MetadataIndex index : indexes)
         {  
            // Check GML footprint
            if (index.getName ().equalsIgnoreCase ("footprint"))
            {
               String gml_footprint = index.getValue ();
               if ( (gml_footprint != null) && ProcessingUtils.checkGMLFootprint (gml_footprint))
               {
                  product.setFootPrint (gml_footprint);
               }
               else
               {
                  logger.error ("Incorrect on empty footprint for product " + 
                     product.getPath ());
               }
            }

            // Check JTS footprint
            if (index.getName ().equalsIgnoreCase ("jts footprint"))
            {
               String jts_footprint = index.getValue ();
               if ( (jts_footprint != null) && !ProcessingUtils.checkJTSFootprint (jts_footprint))
               {
                  // If JTS footprint is wrong; remove the corrupted footprint.
                  product.getIndexes().remove(index);
               }
            }
         }
         
         productDao.update (product);

         // save the reprocessed index
         searchService.index(product);
      }
   }
   
   private void updateUserCountries()
   {
      String synonymsFile = System.getProperty ("country.synonyms");
      if (synonymsFile == null)
      {
         // TODO test it
         return;
      }
      logger.info("Loading country synonyms from '"+synonymsFile+"'");
      List<String> countriesNames = countryDao.readAllNames ();
      HashMap<String, List<String>> synonyms = 
         new HashMap<String, List<String>> ();
      
      try
      {
         BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(synonymsFile), "UTF-8"));
         String sCurrentLine;
         while ((sCurrentLine = br.readLine()) != null) 
         {
            if (sCurrentLine.startsWith ("#"))
            {
               // comments
               continue;
            }
            String[] split1 = sCurrentLine.split (": ");
            if (split1.length > 1)
            {
               String[] split2 = split1[1].split(", ");
               List<String> syns = new ArrayList<String> ();
               for (String s : split2)
               {
                  syns.add(s.toLowerCase ());
               }
               if (countriesNames.contains (split1[0]))
               {
                  synonyms.put (split1[0], syns);
               }
            }
         }
         br.close ();
      }
      catch (FileNotFoundException e)
      {
         logger.error("Can not load country synonyms");
         return;
      }
      catch (IOException e)
      {
         logger.error("Can not load country synonyms");
         return;
      }

      Iterator<User> users = userDao.getAllUsers ();
      while (users.hasNext ())
      {
         User u = users.next ();
         if (cfgManager.getAdministratorConfiguration ().getName ().equals (
               u.getUsername ())
               || userDao.getPublicData ().getUsername ().equals (
               u.getUsername ()))
         {
            continue;
         }
         if (!countriesNames.contains (u.getCountry ()))
         {
            boolean found = false;
            for (String country: synonyms.keySet ())
            {
               if (synonyms.get (country).contains (u.getCountry ().toLowerCase ()))
               {
                  u.setCountry (country);
                  userDao.update (u);
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               logger.warn("Unknown country for '"+u.getUsername ()+"' : "+u.getCountry ());
            }
         }
      }
   }

   private void inactiveOldSearchQueries ()
   {
      boolean inactiveSearchNotification = Boolean.parseBoolean (
            System.getProperty (
                  "users.search.notification.force.inactive", "false"));
      logger.info ("Inactive all saved search notifications (" +
                   "users.search.notification.force.inactive) requested by " +
                   "user ("+ inactiveSearchNotification + ")");

      if (inactiveSearchNotification)
      {
         searchDao.disableAllSearchNotifications ();
      }
   }
}
