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
package fr.gael.dhus.datastore.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.FileScannerDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.DefaultDataStore;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Event;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Listener;
import fr.gael.drbx.cortex.DrbCortexItemClass;
import fr.gael.drbx.cortex.DrbCortexModel;

/**
 * Provide the scanner instances.
 */
@Component ("scannerFactory")
public class ScannerFactory
{
   private static Log logger = LogFactory.getLog (ScannerFactory.class);

   @Autowired
   private DefaultDataStore datastore;

   @Autowired
   private FileScannerDao fileScannerDao;

   @Autowired
   private CollectionDao collectionDao;

   @Autowired
   private ProductDao productDao;

   private HashMap<Long, Scanner> runningScanners =
         new HashMap<Long, Scanner> ();

   private String[] itemClasses;

   /**
    * Retrieve the list of items that the scanner is able to retrieve. This
    * support allow not to perform selective ingest according to the item
    * classes.
    *
    * @return the list of supported items.
    */
   public List<DrbCortexItemClass> getScannerSupport ()
   {
      if (itemClasses == null)
      {
         itemClasses = getDefaultCortexSupport ();
      }

      if (itemClasses == null)
         throw new UnsupportedOperationException (
            "Empty item list: no scanner support.");

      List<DrbCortexItemClass> supported = new ArrayList<DrbCortexItemClass> ();
      if (itemClasses != null)
      {
         for (String s : itemClasses)
         {
            try
            {
               supported.add (DrbCortexItemClass.getCortexItemClassByName (s));
            }
            catch (Exception e)
            {
               logger.error ("Cannot add support for class " + s);
            }
         }
      }
      return supported;
   }

   /**
    * Retrieve the dhus system supported items for file scanning processing.
    * Is considered supported all classes having
    * <code>http://www.gael.fr/dhus#metadataExtractor</code> property
    * connection.
    * @return the list of supported class names.
    */
   public static synchronized String[] getDefaultCortexSupport ()
   {
      DrbCortexModel model;
      try
      {
         model = DrbCortexModel.getDefaultModel ();
      }
      catch (IOException e)
      {
         throw new UnsupportedOperationException (
            "Drb cortex not properly initialized.");
      }

      ExtendedIterator it=model.getCortexModel ().getOntModel ().listClasses ();
      List<String>list = new ArrayList<String> ();

      while (it.hasNext ())
      {
         OntClass cl = (OntClass)it.next ();

         OntProperty metadata_extractor_p = cl.getOntModel().getOntProperty(
               "http://www.gael.fr/dhus#support");

         StmtIterator properties = cl.listProperties (metadata_extractor_p);
         while (properties.hasNext ())
         {
            Statement stmt = properties.nextStatement ();
            logger.debug ("Scanner Support Added for " +
               stmt.getSubject ().toString ());
            list.add (stmt.getSubject ().toString ());
         }
      }
      return list.toArray (new String[list.size ()]);
   }

   /**
    * Retrieve scanner for given url
    */
   public Scanner getScanner (String url)
   {
      return getScanner(url, null, null, null);
   }
   /**
    * Retrieve the scanner according to the passed archive.
    *
    * @param archive used to define the scanner.
    * @return the scanner able to scan passed archive, null if no scanner found.
    */
   public Scanner getScanner (String url, String username, String password,
         String pattern)
   {
      if (url == null) throw new NullPointerException ("URL is required.");

      if ( (new File (url)).exists ())
      {
         FileScanner scan = new FileScanner (url, false);
         scan.setUserPattern (pattern);
         return scan;
      }
      if (url.startsWith ("file:"))
      {
         FileScanner scan = new FileScanner (url.split ("file:", 2)[1], false);
         scan.setUserPattern (pattern);
         return scan;
      }
      if (url.startsWith ("ftp"))
      {
         FtpScanner s = new FtpScanner (url, false, username, password);
         s.setUserPattern (pattern);
         return s;
      }
      if (url.startsWith ("http")) // http or https
      {
         ODataScanner scan = null;
         try
         {
            scan = new ODataScanner (url, false, username, password);
         }
         catch (URISyntaxException | IOException | ODataException e)
         {
            throw new RuntimeException (e);
         }
         scan.setUserPattern (pattern);
         return scan;
      }

      throw new UnsupportedOperationException ("Url not supported (\"" +
         url + "\").");
   }

   /**
    * Provides a scanner able to fully scan&upload passed URL.
    *
    * @param archive
    * @param url
    * @param username
    * @param password
    * @return
    */
   public Scanner getUploadScanner (String url, final String username,
      final String password, String pattern, final User owner,
      final List<Collection> collections,
      final FileScannerWrapper wrapper)
   {
      final Scanner scanner = getScanner (url, username, password, pattern);
      scanner.setSupportedClasses (getScannerSupport ());

      final AsynchronousLinkedList<URLExt> list = scanner.getScanList ();

      list.addListener (new Listener<URLExt> ()
      {
         @Override
         public void addedElement (final Event<URLExt> e)
         {
            String url = e.getElement ().getUrl ().toString ();
            Product product = null;
            if ((product=productDao.getProductByOrigin (url)) != null)
            {
               String action = "scheduled";
               if (product.getProcessed ()) action="ingested";

               logger.info ("Product \"" +
                  showPublicURL(e.getElement ().getUrl ()) +
                  "\" already "+ action + ".");
               return;
            }

            try
            {
               if (wrapper != null)
               {
                  wrapper.incrementTotalProcessed ();
               }

               datastore.addProduct (new URL(url), owner, collections, url,
                  scanner, wrapper);
            }
            catch (Exception exception)
            {
               logger.error ("Cannot add product from url " + url, exception);
               // Listener must be notified of this error.
               if (wrapper != null)
               {
                  wrapper.fatalError (exception);
               }
            }
         }

         @Override
         public void removedElement (Event<URLExt> e)
         {
         }
      });
      return scanner;
   }

   private String showPublicURL (URL url)
   {
      String protocol = url.getProtocol();
      String host = url.getHost();
      int port = url.getPort();
      String path = url.getFile();
      if (protocol == null) protocol = "";
      else protocol += "://";

      String s_port = "";
      if (port != -1) s_port = ":"+port;

      return protocol + host + s_port + path;
   }

   /**
    * Process passed file scanner attached to a the passed user within
    * a separate thread. If the requested scanner is already running (
    * from schedule or UI), it will not restart.
    * @param scan_id
    * @param user
    * @throws ScannerException when scanner cannot be started.
    */
   public void processScan (final Long scan_id, final User user)
      throws ScannerException
   {
      fr.gael.dhus.database.object.FileScanner fileScanner = null;
      SimpleDateFormat sdf = new SimpleDateFormat (
         "EEEE dd MMMM yyyy - HH:mm:ss", Locale.ENGLISH);

      // Check if scanner is already running. If not set it status.
      //fileScannerDao.printCurrentSessions ();
      //synchronized (fileScannerDao)
      //{
         fileScanner = fileScannerDao.read (scan_id);
         if (fr.gael.dhus.database.object.FileScanner.STATUS_RUNNING.
             equals (fileScanner.getStatus ()))
         {
            throw new ScannerException ("Scanner to \"" +
               fileScanner.getUsername () + "@" + fileScanner.getUrl () +
               "\" already running.");
         }

         fileScanner.setStatus (
            fr.gael.dhus.database.object.FileScanner.STATUS_RUNNING);
         fileScanner.setStatusMessage ("Started on " + sdf.format (new Date()) +
                  "<br>");
         fileScannerDao.update (fileScanner);
      //}

      List<Collection> collections = new ArrayList<Collection> ();
      List<Long> colIds = fileScannerDao.getScannerCollections (scan_id);
      for (Long colId : colIds)
      {
         Collection col = collectionDao.read(colId);
         if (col != null)
         {
            collections.add (col);
         }
      }

      // Processing scanner is used to manage synchronization between
      // processing and this scanner. It is in charge of setting and updating
      // the scanner status.
      FileScannerWrapper wrapper =
         new FileScannerWrapper (fileScanner);

      Hook hook = new Hook (fileScanner);

      String status = fr.gael.dhus.database.object.FileScanner.STATUS_OK;
      String message = "Error while scanning.";
      try
      {
         Scanner scanner = getUploadScanner (fileScanner.getUrl (),
            fileScanner.getUsername (), fileScanner.getPassword (),
            fileScanner.getPattern (), user, collections, wrapper);
         runningScanners.put (scan_id, scanner);
         Runtime.getRuntime ().addShutdownHook (hook);
         int total = scanner.scan ();

         message = "Successfully completed on " + sdf.format (new Date()) +
            " with " + total + " product" + (total>1?"s":"") + " scanned.";
      }
      catch (InterruptedException e)
      {
         status = fr.gael.dhus.database.object.FileScanner.STATUS_OK;
         message = "Scanner stopped by user on " + sdf.format (new Date());
      }
      catch (Exception e)
      {
         status = fr.gael.dhus.database.object.FileScanner.STATUS_ERROR;
         message =  "Scanner error occurs on " + sdf.format (new Date()) +": "+
            e.getMessage ();
      }
      finally
      {
         Runtime.getRuntime ().removeShutdownHook (hook);
         wrapper.setScannerDone (status, message);
      }
   }

   public void stopScan (final Long scan_id)
            throws ScannerException
   {
      Scanner scanner = null;
      // Thread-safe retrieve the scanner and remove it from the list.
      synchronized (runningScanners)
      {
         scanner = runningScanners.remove (scan_id);
      }
      if (scanner == null)
      {
         logger.error ("Scanner already stopped.");
         return;
      }

      fr.gael.dhus.database.object.FileScanner fileScanner =
         fileScannerDao.read (scan_id);
      if (fileScanner != null)
      {
         // Just update the message
         fileScanner.setStatusMessage (fileScanner.getStatusMessage () +
            "<b>Interrupted</b>: waiting ongoing processings ends...<br>");
         fileScannerDao.update (fileScanner);

      }

      logger.info ("Scanner stopped.");
      scanner.stop ();
   }

   /**
    * Shutdown hook used to manage Scanner message when user stops dhus
    * while scanner is running.
    */
   class Hook extends Thread
   {
      private fr.gael.dhus.database.object.FileScanner scanner;
      public Hook (fr.gael.dhus.database.object.FileScanner scanner)
      {
         this.scanner = scanner;
      }

      public void run()
      {
         scanner.setStatus (
            fr.gael.dhus.database.object.FileScanner.STATUS_ERROR);
         scanner.setStatusMessage (
            scanner.getStatusMessage () +
            "Scanner interrupted because DHuS stopped.");
         fileScannerDao.update (scanner);
      }
   }
}
