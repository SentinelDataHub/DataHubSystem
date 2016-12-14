package fr.gael.dhus.datastore.scanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import fr.gael.dhus.service.FileScannerService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class FileScannerWrapper
{
   private static final Logger LOGGER = LogManager.getLogger(FileScannerWrapper.class);

   final Long fs_id;
   final AtomicInteger startCounter;
   final AtomicInteger endCounter;
   final AtomicInteger errorCounter;
   final AtomicInteger totalProcessed;
   String scannerStatus;
   String scannerMessage;
   String processingErrors = "";

   public FileScannerWrapper (final FileScanner persistent_scanner)
   {
      this.startCounter = new AtomicInteger (0);
      this.endCounter = new AtomicInteger (0);
      this.errorCounter = new AtomicInteger (0);
      this.totalProcessed = new AtomicInteger (0);

      this.fs_id = persistent_scanner.getId ();
   }

   /**
    * Case of error during processing: informations are accumulated to be
    * displayed to the user.
    */
   public synchronized void error (Product p, Throwable e)
   {
      if ((e!=null) && (e.getMessage ()!=null))
      {
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
         processingErrors +=e.getMessage () + message + "<br>\n";
      }
      errorCounter.incrementAndGet ();

      // As far as endIngestion is not called in case of error, it is
      // necessary to run it manually.
      if ((endCounter.get () + errorCounter.get ()) >= getTotalProcessed ())
      {
         processingsDone(null);
      }
   }
   /**
    * Called on fatal error: the scanner crashed and no processing
    * are expected passed this event. scanner status forced to ERROR,
    * and error message is reported.
    */
   public synchronized void fatalError (Throwable e)
   {
      // Force the scanner status to ERROR.
      scannerStatus = fr.gael.dhus.database.object.FileScanner.STATUS_ERROR;
      processingsDone(e.getMessage ());
   }

   /**
    * Called at products ingestion start.
    */
   public synchronized void startIngestion ()
   {
      startCounter.incrementAndGet ();
   }

   /**
    * End of a product ingestion: check if the scanner is finished, and all
    * processing are completed, in this case, it modifies the scanner status
    * and message to inform user of finished processings.
    */
   public synchronized void endIngestion ()
   {
      endCounter.incrementAndGet ();
      LOGGER.info("End of product ingestion: processed=" +
            endCounter.get () + ", error="  + errorCounter.get () + ", inbox=" +
         (totalProcessed.get () - (endCounter.get () + errorCounter.get ())) +
         ", total=" + totalProcessed.get () + ".");

      // Total number of product processed shall be coherent with
      // passed/non-passed number of products.
      if ((endCounter.get () + errorCounter.get ()) >= getTotalProcessed ())
      {
         this.scannerStatus = FileScanner.STATUS_OK;
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
      if ((startCounter.get () >= (endCounter.get () + errorCounter.get ())) &&
          (startCounter.get () >= getTotalProcessed ()))
      {
         processingsDone(null);
      }
   }

   /**
    * Notifies the scanner that the processings are done.
    */
   protected synchronized void processingsDone(String ended_message)
   {
      LOGGER.info(
         "Scanner and processings are completed: update the UI status.");
      SimpleDateFormat sdf = new SimpleDateFormat (
         "EEEE dd MMMM yyyy - HH:mm:ss", Locale.ENGLISH);

      String processing_message = "Ingestion completed at " +
         sdf.format (new Date ()) + "<br>\nwith " + endCounter.get () +
         " products processed and " + errorCounter.get () +
         " error" + (errorCounter.get () >1?"s":"") +
         " during this processing.<br>\n";

      if (!processingErrors.isEmpty ())
         processing_message += "<u>Processing error(s):</u><br>\n" +
               processingErrors;

      if (ended_message!= null)
      {
         processing_message += ended_message + "<br>\n";
      }

      FileScannerService fs_service =
         ApplicationContextProvider.getBean (FileScannerService.class);
      if (fs_id != null)
      {
         // Set the scanner info
         FileScanner persistentScanner = fs_service.getFileScanner (fs_id);
         persistentScanner.setStatus (scannerStatus);
         persistentScanner.setStatusMessage (truncateMessageForDB(
            persistentScanner.getStatusMessage () + scannerMessage +
            "<br>\n" + processing_message));
         fs_service.updateFileScanner (persistentScanner);
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
      return totalProcessed.get ();
   }

   public void setTotalProcessed (int total_processed)
   {
      totalProcessed.set (total_processed);
   }

   public void incrementTotalProcessed ()
   {
      totalProcessed.incrementAndGet ();
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
