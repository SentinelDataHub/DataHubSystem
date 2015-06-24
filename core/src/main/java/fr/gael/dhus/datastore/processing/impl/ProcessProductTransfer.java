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
package fr.gael.dhus.datastore.processing.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.DefaultDataStore;
import fr.gael.dhus.datastore.HierarchicalDirectoryBuilder;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.datastore.exception.DataStoreException;
import fr.gael.dhus.datastore.processing.ProcessingProduct;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Event;
import fr.gael.dhus.datastore.scanner.AsynchronousLinkedList.Listener;
import fr.gael.dhus.datastore.scanner.Scanner;
import fr.gael.dhus.datastore.scanner.ScannerFactory;
import fr.gael.dhus.datastore.scanner.URLExt;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.MultipleDigestInputStream;
import fr.gael.dhus.util.UnZip;
import fr.gael.drb.DrbFactory;
import fr.gael.drb.DrbNode;
import fr.gael.drb.impl.ftp.Transfer;
import fr.gael.drb.impl.spi.DrbNodeSpi;

/**
 * processing of product information
 *
 */
@Component
public class ProcessProductTransfer implements ProcessingProduct
{
   private static Logger logger = 
         Logger.getLogger (ProcessProductTransfer.class);
   
   @Autowired
   private ScannerFactory scannerFactory;
   
   @Autowired
   private DefaultDataStore datastore;

   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;
   
   @Autowired
   private ProductDao productDao;
   
   @Autowired
   IncomingManager incomingManager;
   
   @Autowired
   private ConfigurationManager cfgManager;
   
   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#getDescription()
    */
   @Override
   public String getDescription()
   {
      return "Processes Product Transfer";
   }

   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#getLabel()
    */
   @Override
   public String getLabel()
   {
      return "Product Transfer";
   }

   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#run(java.lang.Object)
    */
   @Override
   public void run(Product product)
   {
      String url = product.getOrigin ();
      if (url == null)
      {
         return;
      }
      if (!product.getPath().toString ().equals (url))
      {
         return;
      }
      File dest = incomingManager.getNewProductIncomingPath ();
      Boolean compute_checksum = null;
      try
      {
         compute_checksum = UnZip.supported ( (new URL (url)).getPath () );
      }
      catch (MalformedURLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      LockFactory lf = new NativeFSLockFactory (dest.getParentFile ());
      Lock lock = lf.makeLock (".lock-writing");
      try
      {
         lock.obtain (900000);
      }
      catch (Exception e)
      {
         logger.warn ("Cannot lock incoming directory - continuing without ("+
            e.getMessage () +")");
      }
      try
      {
         User owner = productDao.getOwnerOfProduct (product);
         actionRecordWritterDao.uploadStart (dest.getPath (), owner.getUsername ());
         URL u = new URL ( url );
         String userInfos = u.getUserInfo ();
         String username = null;
         String password = null;
         if (userInfos != null)
         {
            String[] infos = userInfos.split (":");
            username = infos[0];
            password = infos[1];
         }
         
         // Hooks to remove the partially transfered product
         Hook hook = new Hook (dest.getParentFile ());
         Runtime.getRuntime ().addShutdownHook (hook);
         upload (url, username, password, dest, compute_checksum);
         Runtime.getRuntime ().removeShutdownHook (hook);

         String local_filename = ScannerFactory.getFileFromPath (url);
         File productFile = new File (dest, local_filename);
         product.setPath (productFile.toURI ().toURL ());
         productDao.update (product);
      }
      catch (Exception e)
      {
         FileUtils.deleteQuietly(dest);
         throw new DataStoreException ("Cannot transfer product \"" + url + "\".", e);
      }
      finally
      {
         try
         {
            lock.close ();
         }
         catch (IOException e) {}
      }
   }
   
   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#removeProcessing(java.lang.Object)
    */
   @Override
   public void removeProcessing(Product product)
   {
      if (product.getPath().toString ().equals (product.getOrigin ()))
      {
         return;
      }
      String prodPath = ProductDao.getPathFromProduct (product);
      prodPath = prodPath.replaceAll ("file://?", "/");
      File pf = new File (prodPath);
      if (IncomingManager.INCOMING_PRODUCT_DIR.equals (
          pf.getParentFile ().getName ()))
         pf = pf.getParentFile ();
      if (HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME.equals (pf
         .getParentFile ().getName ())) pf = pf.getParentFile ();
      try
      {
         FileUtils.deleteDirectory (pf);
      }
      catch (IOException e)
      {
         throw new DataStoreException ("Error while deleting product file.", e);
      }
   }
   
   private void upload (String url, final String username,
      final String password, final File dest, final boolean compute_checksum)
   {
      String remote_base_dir;
      try
      {
         remote_base_dir = (new URL (url)).getPath ();
      }
      catch (MalformedURLException e1)
      {
         logger.error ("Problem during upload", e1);
         return;
      }
      
      final String remote_base = remote_base_dir;

      Scanner scanner = scannerFactory.getScanner (url, username, password, null);
      // Get all files supported
      scanner.setUserPattern(".*");
      scanner.setForceNavigate (true);

      scanner.getScanList ().addListener (new Listener<URLExt> ()
      {
         @Override
         public void addedElement (Event<URLExt> e)
         {
            URLExt element = e.getElement ();
            String remote_path = element.getUrl().getPath ();
            String local_filename = ScannerFactory.getFileFromPath (remote_path);

            String local_path_dir = "";
            
            if ( !remote_base.equals (remote_path))
               local_path_dir =
                  remote_path.replaceFirst (
                        ScannerFactory.getParentPath (remote_base), "");
            else
               local_path_dir = local_filename;

            File local_path = new File (dest, local_path_dir);

            if ( !local_path.getParentFile ().exists ())
            {
               logger.info ("Creating directory \"" +
                  local_path.getParentFile ().getPath () + "\".");
               local_path.getParentFile ().mkdirs ();
               local_path.getParentFile ().setWritable (true);
            }

            BufferedInputStream bis = null;
            InputStream is = null;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            int retry = 3;
            boolean source_remove = cfgManager.
               getFileScannersCronConfiguration ().isSourceRemove ();

            if (!element.isDirectory ()) 
            {
               DrbNode node = DrbFactory.openURI(element.getUrl().
                  toExternalForm());
               long start = System.currentTimeMillis ();
               do
               {
                  try
                  {
                     logger.info ("Transfering remote file \"" + remote_path +
                        "\" into \"" + local_path + "\".");

                     if ((node instanceof DrbNodeSpi) &&
                           (((DrbNodeSpi)node).hasImpl(File.class)))
                     {
                        File source= (File)((DrbNodeSpi)node).
                           getImpl(File.class);
                        {
                           if (source_remove)
                              moveFile(source, local_path, compute_checksum);
                           else
                              copyFile (source, local_path, compute_checksum);
                        }
                     }
                     else
                     // Case of Use Transfer class to run
                     if ((node instanceof DrbNodeSpi) &&
                         (((DrbNodeSpi)node).hasImpl(Transfer.class)))
                     {
                        fos = new FileOutputStream (local_path);
                        bos = new BufferedOutputStream (fos);

                        Transfer t =  (Transfer)((DrbNodeSpi)node).getImpl(Transfer.class);
                        t.copy(bos);
                        try
                        {
                           if (cfgManager.getFileScannersCronConfiguration ().isSourceRemove ())
                              t.remove();
                        }
                        catch (IOException ioe)
                        {
                           logger.error("Unable to remove " + 
                              local_path.getPath(), ioe);
                        }
                     }
                     else
                     {
                        if ((node instanceof DrbNodeSpi) &&
                            (((DrbNodeSpi)node).hasImpl(InputStream.class)))
                        {
                           is = (InputStream)((DrbNodeSpi)node).getImpl(InputStream.class);
                        }
                        else
                           is = element.getUrl ().openStream ();

                        bis = new BufferedInputStream (is);
                        fos = new FileOutputStream (local_path);
                        bos = new BufferedOutputStream (fos);
                        
                        IOUtils.copyLarge (bis, bos);
                     }
                     // Prepare message
                     long stop = System.currentTimeMillis ();
                     long delay_ms = stop-start;
                     long size = local_path.length ();
                     String message = " in " + delay_ms + "ms";
                     if ((size>0) && (delay_ms>0))
                        message += " at " + 
                           ((size/(1024*1024))/((float)delay_ms/1000.0))+"MB/s";
                        
                     logger.info ("Copy of " + node.getName() + " completed" +
                        message);
                     retry=0;
                  }
                  catch (Exception excp)
                  {
                     if ((retry-1)<=0)
                     {
                        logger.error("Cannot copy "+node.getName()+" aborted.");
                        throw new RuntimeException ("Transfer Aborted.", excp);
                     }
                     else
                     {
                        logger.warn ("Cannot copy " + node.getName () + 
                           " retrying... (" + excp.getMessage() +")");
                        try
                        {
                           Thread.sleep (1000);
                        }
                        catch (InterruptedException e1)
                        {
                           // Do nothing.
                        }
                     }
                  }
                  finally
                  {
                     try
                     {
                        if (bos != null) bos.close ();
                        if (fos != null) fos.close ();
                        if (bis != null) bis.close ();
                        if( is != null) is.close ();
                     }
                     catch (IOException exp)
                     {
                        logger.error ("Error while closing copy streams.");
                     }
                  }
               } while (--retry>0);
            }
            else
            {
               if (!local_path.exists ())
               {
                  logger.info ("Creating directory \"" + local_path.getPath () +
                     "\".");
                  local_path.mkdirs ();
                  local_path.setWritable (true);
               }
               return;
            }
         }

         @Override
         public void removedElement (Event<URLExt> e)
         {
         }
      });
      try
      {
         scanner.scan ();
         // Remove root product if required.
         if (cfgManager.getFileScannersCronConfiguration ().isSourceRemove ())
         {
            try
            {
               DrbNode node = DrbFactory.openURI(url);
               if (node instanceof DrbNodeSpi)
               {
                  DrbNodeSpi spi = (DrbNodeSpi)node;
                  if (spi.hasImpl(File.class))
                  {
                     FileUtils.deleteQuietly((File)spi.getImpl(File.class));
                  }
                  else if (spi.hasImpl(Transfer.class))
                  {
                     ((Transfer)spi.getImpl (Transfer.class)).remove ();
                  }
                  else
                  {
                     logger.error("Root product note removed (TBC)");
                  }
               }
            }
            catch (Exception e)
            {
               logger.warn ("Cannot remove input source (" + e.getMessage () + 
                  ").");
            }
         }
      }
      catch (Exception e)
      {
         if (e instanceof InterruptedException)
            logger.error ("Process interrupted by user");
         else
         logger.error ("Error while uploading product", e);
         
         // If something get wrong during upload: do not keep any residual 
         // data locally.
         logger.warn ("Remove residual uploaded data :" + dest.getPath ());
         FileUtils.deleteQuietly(dest);
         throw new UnsupportedOperationException ("Error during scan.", e);
      }
   }
   
   private void copyFile (File source, File dest, 
      boolean compute_checksum) throws IOException, NoSuchAlgorithmException
   {
      String[] algorithms = cfgManager.getDownloadConfiguration ().
         getChecksumAlgorithms ().split (",");
      
      FileInputStream fis = null;
      FileOutputStream fos = null;
      MultipleDigestInputStream dis = null;
      try
      {
         fis = new FileInputStream(source);
         fos = new FileOutputStream(dest);
         
         if (compute_checksum)
         {
            dis = new MultipleDigestInputStream (fis, algorithms);
            IOUtils.copyLarge (dis, fos);
            // Write the checksums if any
            for (String algorithm: algorithms)
            {
               String chk = dis.getMessageDigestAsHexadecimalString (algorithm);
               FileUtils.write (new File (dest.getPath ()+"."+algorithm), chk);
            }
         }
         else
            IOUtils.copyLarge (fis, fos);
         
      }
      finally
      {
         IOUtils.closeQuietly(fos);
         IOUtils.closeQuietly(dis);
         IOUtils.closeQuietly(fis);
      }

      if (source.length() != dest.length())
      {
          throw new IOException("Failed to copy full contents from '" +
                   source + "' to '" + dest + "'");
      }
   }
   
   public void moveFile(File srcFile, File destFile, 
      boolean compute_checksum) throws IOException, NoSuchAlgorithmException
   {
      if (srcFile == null)
      {
         throw new NullPointerException("Source must not be null");
      }
      if (destFile == null)
      {
         throw new NullPointerException("Destination must not be null");
      }
      if (!srcFile.exists())
      {
         throw new FileNotFoundException("Source '" + srcFile + 
            "' does not exist");
      }
      if (srcFile.isDirectory())
      {
         throw new IOException("Source '" + srcFile + "' is a directory");
      }
      if (destFile.exists())
      {
         throw new FileExistsException("Destination '" + destFile + 
            "' already exists");
      }
      if (destFile.isDirectory())
      {
         throw new IOException("Destination '" + destFile + "' is a directory");
      }

      boolean rename = srcFile.renameTo(destFile);
      if (!rename)
      {
         copyFile(srcFile, destFile, compute_checksum);
         if (!srcFile.delete())
         {
            FileUtils.deleteQuietly(destFile);
               throw new IOException("Failed to delete original file '" + 
               srcFile + "' after copy to '" + destFile + "'");
         }
      }
   }
   
   /**
    * Shutdown hook used to manage incomplete transfer of products
    */
   class Hook extends Thread
   {
      private File path;
      public Hook (File path)
      {
         this.path = path;
      }
      
      public void run()
      {
         logger.error ("Interruption during transfert to " + this.path);
         FileUtils.deleteQuietly (path);
      }
   }
}
