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
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.FileScannerDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.DefaultDataStore;
import fr.gael.dhus.datastore.processing.ProcessingEvent;
import fr.gael.dhus.datastore.processing.ProcessingListener;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Event;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Listener;
import fr.gael.dhus.system.config.ConfigurationManager;
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
   private TaskExecutor taskExecutor;
   
   @Autowired
   private FileScannerDao fileScannerDao;

   @Autowired
   private CollectionDao collectionDao;
   
   @Autowired
   private ProductDao productDao;
      
   @Autowired
   private ConfigurationManager cfgManager;
   
   @Autowired
   ActionRecordWritterDao actionRecordWritterDao;

   private HashMap<Long, Scanner> runningScanners = new HashMap<Long, Scanner> ();
   
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
   public synchronized String[] getDefaultCortexSupport ()
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
               "http://www.gael.fr/dhus#metadataExtractor");

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
   public Scanner getScanner (String url, String username, String password, String pattern)
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
      final ProcessingListener process_listener)
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
            if (productDao.getProductByOrigin (url) != null)
            {
               logger.info ("Product \"" + 
                  showPublicURL(e.getElement ().getUrl ()) + 
                  "\" already scheduled.");
               return;
            }

            try
            {
               if ((process_listener != null) && 
                   (process_listener instanceof ScannerProcessListener))
               {
                    ScannerProcessListener listener = 
                       (ScannerProcessListener)process_listener;
                    listener.incrementTotalProcessed ();
               }

               datastore.addProduct (new URL(url), owner, collections, url, 
                  scanner, process_listener);
            }
            catch (Exception exception)
            {
               logger.error ("Cannot add product from url " + url, exception);
               // Listener must be notified of this error.
               if ((process_listener != null) && 
                   (process_listener instanceof ScannerProcessListener))
               {
                  ScannerProcessListener listener = 
                     (ScannerProcessListener)process_listener;
                            
                  listener.fatalError (new ProcessingEvent(null, exception));
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
    * @param scanId
    * @param user
    * @throws ScannerException when scanner cannot be started.
    */
   public void processScan (final Long scanId, final User user)
      throws ScannerException
   {
      fr.gael.dhus.database.object.FileScanner fileScanner = null;
      SimpleDateFormat sdf = new SimpleDateFormat (
         "EEEE dd MMMM yyyy - HH:mm:ss", Locale.ENGLISH);
      
      // Check if scanner is already running. If not set it status.
      synchronized (fileScannerDao)
      {
         fileScanner = fileScannerDao.read (scanId);
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
      }

      List<Collection> collections = new ArrayList<Collection> ();
      List<BigInteger> colIds = fileScannerDao.getScannerCollections (scanId);
      for (BigInteger colId : colIds)
      {
         Collection col = collectionDao.read(new Long(colId.longValue ()));
         if (col != null)
         {
            collections.add (col);
         }
      }
      
      // Processing scanner is used to manage synchronization between
      // processing and this scanner. It is in charge of setting and updating 
      // the scanner status.
      ScannerProcessListener process_listener = 
         new ScannerProcessListener (fileScanner);
      
      Hook hook = new Hook (fileScanner);
      
      String status = fr.gael.dhus.database.object.FileScanner.STATUS_OK;
      String message = "Error while scanning.";
      try 
      {
         Scanner scanner = getUploadScanner (fileScanner.getUrl (), 
            fileScanner.getUsername (), fileScanner.getPassword (), 
            fileScanner.getPattern (), user, collections, process_listener);
         runningScanners.put (scanId, scanner);
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
         process_listener.setScannerDone (status, message);   
      }
   }
   public void stopScan (final Long scanId)
            throws ScannerException
   {
      Scanner scanner = null;
      // Thread-safe retrieve the scanner and remove it from the list. 
      synchronized (runningScanners)
      {
         scanner = runningScanners.remove (scanId);
      }
      if (scanner == null)
      {
         logger.error ("Scanner already stopped.");
         return;
      }
      logger.info ("Scanner stopped.");
      scanner.stop ();
      
      synchronized (fileScannerDao)
      {
         fr.gael.dhus.database.object.FileScanner fileScanner = 
            fileScannerDao.read (scanId);
         if (fileScanner != null)
         {
            // Just update the message
            fileScanner.setStatusMessage (fileScanner.getStatusMessage () +
               "<b>Interrupted</b>: waiting ongoing processings ends...<br>");
            fileScannerDao.update (fileScanner);
         }
      }
   }
   
   public static String getFileFromPath (String path)
   {
      String[] p = path.split ("/");
      return p[p.length - 1];
   }

   public static String getParentPath (String path)
   {
      String file = getFileFromPath (path);
      int last = path.lastIndexOf (file);
      return path.substring (0, last);
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

   
   class ScannerProcessListener implements ProcessingListener
   {
      fr.gael.dhus.database.object.FileScanner persistentScanner;
      
      int start_counter=0, end_counter=0, error_counter=0;
      String scannerStatus;
      String scannerMessage;
      
      int totalProcessed;
      

      String processing_errors="";
      
      
      public ScannerProcessListener (
         fr.gael.dhus.database.object.FileScanner persistent_scanner)
      {
         this.persistentScanner = persistent_scanner;
      }
      
      @Override
      public void start (ProcessingEvent event) { }
      @Override
      public void end (ProcessingEvent event) { }

      /**
       * Case of error during processing: informations are accumulated to be
       * displayed to the user.
       * @param event
       */
      @Override
      public synchronized void error (ProcessingEvent event)
      {
         Exception e = event.getException ();
         if ((e!=null) && (e.getMessage ()!=null))
         {
            Product p = event.getProduct ();
            String message = "";
            if (p != null)
            {
               String o = p.getOrigin ();
               if (o!=null)
               {
                  String file=o.substring (o.lastIndexOf ("/")+1, o.length ());
                  message="(" + file + ")";
               }
            }
            processing_errors+=e.getMessage () + message + "<br>";
         }
         error_counter++;
         
         // As far as endIngestion is not called in case of error, it is 
         // necessary to run it manually.
         if ((end_counter+error_counter) >= getTotalProcessed ())
         {
            processingsDone(null);
         }
      }
      /**
       * Called on fatal error: the scanner crashed and no processing
       * are expected passed this event. scanner status forced to ERROR,
       * and error message is reported.
       * @param event
       */
      public synchronized void fatalError (ProcessingEvent event)
      {
         // Force the scanner status to ERROR.
         scannerStatus = fr.gael.dhus.database.object.FileScanner.STATUS_ERROR;
         processingsDone(event.getException ().getMessage ());
      }

      /**
       * Called at products ingestion start.
       * @param event
       */
      @Override
      public void startIngestion (ProcessingEvent event)
      {
         start_counter++;
      }

      /**
       * End of a product ingestion: check if the scanner is finished, and all
       * processing are completed, in this case, it modifies the scanner status
       * and message to inform user of finished processings.  
       * @param event
       */
      @Override
      public void endIngestion (ProcessingEvent event)
      {
         end_counter++;
         logger.info ("End of product ingestion: processed=" + 
            end_counter + ", error="  + error_counter + ", inbox=" + 
            (totalProcessed-(end_counter+error_counter)) + 
            ", total=" + totalProcessed + ".");
         
         // Total number of product processed shall be coherent with
         // passed/non-passed number of products.
         if ((end_counter+error_counter) >= getTotalProcessed ())
         {
            processingsDone(null);
         }
      }

      /**
       * Notifies that the scanned finished its processing.
       * If the status is "ERROR" 
       * @param status
       * @param message
       */
      public void setScannerDone (String status, String message)
      {
         this.scannerStatus = status;
         this.scannerMessage = message;
         
         // CASE of scanner stopped before first processing or no processing
         // to be performed.
         // If all processing started are finished and all processing
         // provided by the scanner to the processing manager are taken into
         // account.
         if ((start_counter == (end_counter+error_counter)) &&
             (start_counter == getTotalProcessed ()))
         {
            processingsDone(null);
         }
      }
      
      /**
       * Notifies the scanner that the processings are done. 
       */
      private synchronized void processingsDone(String ended_message)
      {
         logger.info (
            "Scanner and processings are completed: update the UI status.");
         SimpleDateFormat sdf = new SimpleDateFormat (
            "EEEE dd MMMM yyyy - HH:mm:ss", Locale.ENGLISH);
         
         String processing_message = "Ingestion completed at " + 
            sdf.format (new Date ()) + "<br>with " + end_counter + 
            " products processed and " + error_counter + 
            " error" + (error_counter>1?"s":"") + 
            " during this processing.<br>";
               
         if (!processing_errors.isEmpty ())
            processing_message += "<u>Processing error(s):</u><br>" +
               processing_errors;
         
         if (ended_message!= null)
         {
            processing_message += ended_message + "<br>";
         }
               
         // Reload the scanner object from the database that may be modified.
         synchronized (fileScannerDao)
         {
            persistentScanner=fileScannerDao.read (persistentScanner.getId ());
            if (persistentScanner != null)
            {
               // Set the scanner info
               persistentScanner.setStatus (scannerStatus);
               persistentScanner.setStatusMessage (truncateMessageForDB(
                  persistentScanner.getStatusMessage () + scannerMessage + 
                  "<br>" + processing_message));
               fileScannerDao.update (persistentScanner);
            }
            else
            {
               logger.error ("Scanner has been removed.");
            }
         }
      }
      
      /**
       * Total processed is the effective number of products that are submitted
       * to the processing manager to be ingested. This count excludes the 
       * products recognized as already ingested, or products that generates 
       * exception during submission. This count includes submitted products
       * even if they causes exception during processing steps.
       * 
       * To be able to return this value, scanner execution should be finished
       * with a status. Otherwise, the method waits for the availability. 
       */
      public int getTotalProcessed ()
      {
         return totalProcessed;
      }

      public void setTotalProcessed (int totalProcessed)
      {
         this.totalProcessed = totalProcessed;
      }
      
      public void incrementTotalProcessed ()
      {
         this.totalProcessed++;
      }
      
      /**
       * Database status message length is limited to 4096 
       * @since 0.4.0 
       * @param message to truncate
       * @return truncated message
       */
      private String truncateMessageForDB (String message)
      {
         if (message.length ()>4096)
         {
            return message.substring (0, 4090)+"...";
         }
         return message;
      }
      

   }
}
