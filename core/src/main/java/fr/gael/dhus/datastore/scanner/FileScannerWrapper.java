package fr.gael.dhus.datastore.scanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.gael.dhus.database.dao.FileScannerDao;
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class FileScannerWrapper
{
   private static Log logger = LogFactory.getLog (FileScannerWrapper.class);
   
   fr.gael.dhus.database.object.FileScanner persistentScanner;

   int startCounter =0;
   int endCounter =0;
   int errorCounter =0;
   int totalProcessed;
   String scannerStatus;
   String scannerMessage;
   String processingErrors = "";

   public FileScannerWrapper (FileScanner persistent_scanner)
   {
      this.persistentScanner = persistent_scanner;
   }

   /**
    * Case of error during processing: informations are accumulated to be
    * displayed to the user.
    * @param event
    */
   public synchronized void error (Product p, Exception e)
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
         processingErrors +=e.getMessage () + message + "<br>";
      }
      errorCounter++;

      // As far as endIngestion is not called in case of error, it is
      // necessary to run it manually.
      if ((endCounter + errorCounter) >= getTotalProcessed ())
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
   public void fatalError (Exception e)
   {
      // Force the scanner status to ERROR.
      scannerStatus = fr.gael.dhus.database.object.FileScanner.STATUS_ERROR;
      processingsDone(e.getMessage ());
   }

   /**
    * Called at products ingestion start.
    * @param event
    */
   public void startIngestion ()
   {
      startCounter++;
   }

   /**
    * End of a product ingestion: check if the scanner is finished, and all
    * processing are completed, in this case, it modifies the scanner status
    * and message to inform user of finished processings.
    * @param event
    */
   public void endIngestion ()
   {
      endCounter++;
      logger.info ("End of product ingestion: processed=" +
            endCounter + ", error="  + errorCounter + ", inbox=" +
         (totalProcessed-(endCounter + errorCounter)) +
         ", total=" + totalProcessed + ".");

      // Total number of product processed shall be coherent with
      // passed/non-passed number of products.
      if ((endCounter + errorCounter) >= getTotalProcessed ())
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
      if ((startCounter == (endCounter + errorCounter)) &&
          (startCounter == getTotalProcessed ()))
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
         sdf.format (new Date ()) + "<br>with " + endCounter +
         " products processed and " + errorCounter +
         " error" + (errorCounter >1?"s":"") +
         " during this processing.<br>";

      if (!processingErrors.isEmpty ())
         processing_message += "<u>Processing error(s):</u><br>" +
               processingErrors;

      if (ended_message!= null)
      {
         processing_message += ended_message + "<br>";
      }

      FileScannerDao fileScannerDao =
         ApplicationContextProvider.getBean (FileScannerDao.class);
      
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

   public void setTotalProcessed (int total_processed)
   {
      this.totalProcessed = total_processed;
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
