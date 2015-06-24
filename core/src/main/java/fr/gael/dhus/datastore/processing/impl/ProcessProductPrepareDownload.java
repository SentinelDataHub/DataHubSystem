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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.DefaultDataStore;
import fr.gael.dhus.datastore.HierarchicalDirectoryBuilder;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.datastore.processing.ProcessingProduct;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.MultipleDigestInputStream;
import fr.gael.dhus.util.MultipleDigestOutputStream;
import fr.gael.dhus.util.UnZip;

/**
 * Generates a zip archive of the selected product.
 *
 */
@Component
public class ProcessProductPrepareDownload implements ProcessingProduct
{
   private static Log logger = LogFactory.getLog (ProcessProductPrepareDownload.class);
   
   @Autowired
   DefaultDataStore dataStore;
   
   @Autowired
   private ConfigurationManager cfgManager;
   
   @Autowired
   IncomingManager incomingManager;
   
   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#getDescription()
    */
   @Override
   public String getDescription ()
   {
      return "Generates Downloadable archive";
   }

   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#getLabel()
    */
   @Override
   public String getLabel ()
   {
      return "Prepare Downloadables";
   }

   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#run(java.lang.Object)
    */
   @Override
   public void run (Product product)
   {
      String product_id = product.getIdentifier ();
      Map<String, String>  checksums = null;
      String[] algorithms = cfgManager.getDownloadConfiguration ().
         getChecksumAlgorithms ().split (",");
      
      if (product_id == null)
         throw new NullPointerException ("Product \"" + product.getPath () + 
            "\" identifier not initialized.");
      
      String product_path = product.getPath ().getPath (); 
      if (UnZip.supported (product_path))
      {
         product.setDownloadablePath (product_path);
         product.setDownloadableSize (new File(product_path).length ());
      }
      
      File zip_file = null;
      
      String zip_file_string = product.getDownloadablePath ();
      if ((zip_file_string == null) ||
          (!(new File(zip_file_string).exists ())))
      {
         File incoming = incomingManager.getNewIncomingPath ();
         LockFactory lf = new NativeFSLockFactory (incoming);
         Lock lock = lf.makeLock (".lock-writing");
         try
         {
            lock.obtain (900000);
         }
         catch (Exception e)
         {
            logger.warn ("Cannot lock incoming directory - " +
               "continuing without (" + e.getMessage () +")");
         }
         
         zip_file = new File (incoming, (product_id+".zip"));
         
         logger.info (zip_file.getName () + 
            ": Generating zip file and its checksum.");
         
         zip_file_string = zip_file.getPath ();
         
         try
         {
            long start = System.currentTimeMillis ();
            logger.info ("Creation of downloadable archive into " + 
               zip_file_string);
            checksums = processZip (product.getPath ().getPath (), zip_file);
            long delay_ms= System.currentTimeMillis () - start;
            long size_read = new File (product.getPath ().getPath ()).length ()/
                     (1024*1024);
            long size_write = zip_file.length ()/(1024*1024);
            
            String message = " in " + delay_ms + "ms. Read " +
               size_read + "MB, Write " + size_write + "MB at " +
               (size_write/((float)(delay_ms+1)/1000)) + "MB/s";
            logger.info ("Downloadable archive saved (" +
               product.getPath ().getFile() + ")" + message);
         }
         catch (IOException e)
         {
            logger.error ("Cannot generate Zip archive for product \"" + 
               product.getPath () + "\".", e);
         }
         finally
         {
            try
            {
               lock.close ();
            } catch (IOException e) {}
         }
         
         product.setDownloadablePath (zip_file_string);
         product.setDownloadableSize (zip_file.length ());
      }
      else
      {
         try
         {
            if ((checksums = findLocalChecksum (zip_file_string))==null)
            {
               long start = System.currentTimeMillis ();
               
               logger.info (new File(zip_file_string).getName () + 
                        ": Computing checksum only.");
               
               checksums = ProcessProductPrepareDownload.processChecksum (
                  zip_file_string, algorithms);
               
               /* Compute the output message */
               long delay_ms= System.currentTimeMillis () - start;
               long size = new File(zip_file_string).length ()/(1024*1024);
               
               String message = " in " + delay_ms + "ms. Read " +
                  size + "MB at " + (size/((float)(delay_ms+1)/1000)) + "MB/s";
               
               logger.info ("Checksum processed " + message);
            }
            else
            {
               logger.info (new File(zip_file_string).getName () + 
                        ": Checksum retrieved from transfert.");
            }
         }
         catch (Exception ioe)
         {
            logger.warn("cannot compute checksum.", ioe);
         }
      }
      
      if (checksums != null)
      {
         product.getDownload().getChecksums().clear ();
         product.getDownload().getChecksums().putAll (checksums);
      }
   }
   
   /**
    * Creates a zip file at the specified path with the contents of the 
    * specified directory.
    * @param Input directory path. The directory were is located directory to archive.
    * @param The full path of the zip file.
    * @return the checksum accordig to fr.gael.dhus.datastore.processing.impl.zip.digest variable.
    * @throws IOException If anything goes wrong
    */
   protected Map<String,String> processZip (String inpath, File output) throws IOException
   {
      // Retrieve configuration settings
      String[] algorithms = cfgManager.getDownloadConfiguration ().
         getChecksumAlgorithms ().split (",");
      int compressionLevel = cfgManager.getDownloadConfiguration ().
         getCompressionLevel ();
      
      FileOutputStream fOut = null;
      BufferedOutputStream bOut = null;
      ZipArchiveOutputStream tOut = null;
      MultipleDigestOutputStream dOut = null;
     
      try 
      {
         fOut = new FileOutputStream(output);
         if ((algorithms != null) && (algorithms.length>0))
         {
            try
            {
               dOut =  new MultipleDigestOutputStream (fOut, algorithms);
               bOut = new BufferedOutputStream(dOut);
            }
            catch (NoSuchAlgorithmException e)
            {
               logger.error ("Problem computing checksum algorithms.", e);
               dOut = null;
               bOut = new BufferedOutputStream(fOut);
            }
            
         }
         else
            bOut = new BufferedOutputStream(fOut);
         tOut = new ZipArchiveOutputStream(bOut);
         tOut.setLevel (compressionLevel);
         
         addFileToZip(tOut, inpath, "");
      }
      finally
      {
         try
         {
            tOut.finish();
            tOut.close();
            bOut.close();
            if (dOut != null) dOut.close ();
            fOut.close();
         }
         catch (Exception e)
         {
            logger.error ("Exception raised during ZIP stream close", e);
         }
      }
      if (dOut != null)
      {
         Map<String,String> checksums = new HashMap<String, String> ();
         for (String algorithm: algorithms)
         {
            String chk = dOut.getMessageDigestAsHexadecimalString (algorithm);
            if (chk!=null) checksums.put (algorithm, chk);
         }
         return checksums;
      }
      return null;
   }
   
   /**
    * Creates a zip entry for the path specified with a name built from the base
    * passed in and the file/directory name. If the path is a directory, a 
    * recursive call is made such that the full directory is added to the zip.
    *
    * @param zOut The zip file's output stream
    * @param path The filesystem path of the file/directory being added
    * @param base The base prefix to for the name of the zip file entry
    *
    * @throws IOException If anything goes wrong
    */
   private static void addFileToZip(ZipArchiveOutputStream zOut, String path, 
      String base)
      throws IOException
   {
      File f = new File(path);
      String entryName = base + f.getName();
      ZipArchiveEntry zipEntry = new ZipArchiveEntry(f, entryName);

      zOut.putArchiveEntry(zipEntry);

      if (f.isFile())
      {
         FileInputStream fInputStream = null;
         try
         {
            fInputStream = new FileInputStream(f);
            IOUtils.copy (fInputStream, zOut, 65535);
            zOut.closeArchiveEntry();
         }
         finally
         {
            fInputStream.close ();
         }
      }
      else
      {
         zOut.closeArchiveEntry();
         File[] children = f.listFiles();
         
         if (children != null)
         {
            for (File child : children)
            {
               logger.debug ("ZIP Adding " + child.getName ());
               addFileToZip(zOut, child.getAbsolutePath(), entryName + "/");
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see fr.gael.dhus.datastore.processing.Processing#removeProcessing(java.lang.Object)
    */
   @Override
   public void removeProcessing (Product product)
   {
      String zip_file_string = product.getDownloadablePath ();
      if (zip_file_string == null) return;
      File zip_file = new File (zip_file_string);
      
      if ((zip_file_string != null) &&
          ((zip_file.exists ()))    &&
          incomingManager.isInIncoming(zip_file))
      {
         if (IncomingManager.INCOMING_PRODUCT_DIR.equals (
            zip_file.getParentFile ().getName ()))
            zip_file = zip_file.getParentFile ();
         if (HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME.equals (
            zip_file.getParentFile ().getName ()))
            zip_file = zip_file.getParentFile ();
         try
         {
            FileUtils.forceDelete(zip_file);
         }
         catch (IOException e)
         {
            logger.error("Unable to remove downloadable path " + zip_file_string);
         }
      }
   }
   
   private static Map<String,String> processChecksum (String inpath, String[]algorithms) 
            throws IOException, NoSuchAlgorithmException
   {
      InputStream is= null;
      MultipleDigestInputStream dis = null;
      try 
      {
         is = new FileInputStream (inpath);
         dis = new MultipleDigestInputStream (is, algorithms);
         
         readAll (dis);
      }
      finally
      {
         try
         {
            dis.close();
            is.close();
         }
         catch (Exception e)
         {
            logger.error ("Exception raised during ZIP stream close", e);
         }
      }
         
      Map<String,String> checksums = new HashMap<String, String> ();
      for (String algorithm: algorithms)
      {
         String chk = dis.getMessageDigestAsHexadecimalString (algorithm);
         if (chk!=null) checksums.put (algorithm, chk);
      }
      return checksums;
   }
   
   
   private static final int EOF = -1;
   /**
    * Read all the bytes of a file without output.
    * @param is input stream to read
    * @return the number of bytes read
    * @throws IOException
    */
   private static long readAll (InputStream is) throws IOException
   {
      long count = 0;
      int n = 0;
      byte[] buffer = new byte[1024*4];
      while (EOF != (n = is.read(buffer)))
      {
         count += n;
      }
      return count;
   }
   /**
    * Retrieve checksums files located in the parent of the passed file.
    * checksum files are identified by their extension that must be the
    * digest manifest algorithm(SHA-1, SHA-256, MD5 ...) that 
    * @param file
    * @return
    */
   Map<String, String> findLocalChecksum (String file)
   {
      File _file = new File (file);
      File[]checksum_files = new File (_file.getParent ()).listFiles (
         new FilenameFilter()
         {
            @Override
            public boolean accept (File dir, String name)
            {
               String algo = name.substring (name.lastIndexOf ('.')+1);
               try
               {
                  MessageDigest.getInstance (algo);
                  return true;
               }
               catch (NoSuchAlgorithmException e)
               {
                  return false;
               }
            }
         });
      if ((checksum_files==null) || (checksum_files.length==0))
         return null;
      Map<String, String>checksums = new HashMap<String, String> ();
      for (File checksum_file:checksum_files)
      {
         String chk;
         try
         {
            chk = FileUtils.readFileToString (checksum_file);
         }
         catch (IOException e)
         {
            logger.error("Cannot read checksum in file " + 
               checksum_file.getPath ());
            // Something is wrong: stop it right now!
            return null;
         }
         
         String algo = checksum_file.getName ().substring (
            checksum_file.getName ().lastIndexOf ('.')+1);
         
         checksums.put (algo, chk);
         
      }
      return checksums;
   }
}
