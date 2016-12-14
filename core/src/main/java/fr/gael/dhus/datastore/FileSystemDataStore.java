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

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.exception.DataStoreException;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.UnZip;

import org.apache.commons.io.FileUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manages binaries of Product on the local file system.
 */
@Component
public class FileSystemDataStore implements DataStore<Product>
{
   private static final Logger LOGGER = LogManager.getLogger(FileSystemDataStore.class);
   private final int BUFFER_SIZE = 1024;

   @Autowired
   private ConfigurationManager cfgManager;

   @Autowired
   private IncomingManager incomingManager;

   // Getters & Setters
   public ConfigurationManager getCfgManager ()
   {
      return cfgManager;
   }

   public void setCfgManager (ConfigurationManager cfgManager)
   {
      this.cfgManager = cfgManager;
   }

   public IncomingManager getIncomingManager ()
   {
      return incomingManager;
   }

   public void setIncomingManager (IncomingManager incomingManager)
   {
      this.incomingManager = incomingManager;
   }

   @Override
   public void add (Product product)
   {
      throw new UnsupportedOperationException ("No supported yet !");
   }

   @Override
   public void remove (Product product, Destination destination)
   {
      if (product == null)
      {
         throw new DataStoreException ("Cannot remove a null product");
      }
      if (product.getLocked ())
      {
         throw new DataStoreException ("Cannot remove product: " + product +
               ", it is locked by the system");
      }

      if (!isRemovable (product))
      {
         LOGGER.warn("Cannot delete product :" + product);
      }

      switch (destination)
      {
         case TRASH:
            moveProduct (product,
                  cfgManager.getArchiveConfiguration ()
                        .getEvictionConfiguration ().getTrashPath ());
            break;
         case ERROR:
            moveProduct (product,
                  cfgManager.getArchiveConfiguration ()
                        .getIncomingConfiguration ().getErrorPath ());
            break;

         case NONE:
            break;

         default:
            LOGGER.warn("Unknown destination the product will be deleted");
      }

      removeFiles (product);
   }

   /**
    * Deletes all binaries of the given product.
    *
    * @param product product to delete
    */
   private void removeFiles (Product product)
   {
      try
      {
         // Delete product file from path
         String prodPath = product.getPath ().toString ();
         if (!prodPath.equals (product.getOrigin ()))
         {
            prodPath = prodPath.replaceAll ("file://?", "/");
            deleteIncomingFolder (prodPath);
         }
         // Delete product file from download path
         deleteIncomingFolder (product.getDownloadablePath ());
         // Delete product thumbnail
         if (product.getThumbnailFlag ())
         {
            deleteIncomingFolder (product.getThumbnailPath ());
         }
         // Delete product quick-look
         if (product.getQuicklookFlag ())
         {
            deleteIncomingFolder (product.getQuicklookPath ());
         }
      }
      catch (Exception e)
      {
         LOGGER.error("There was an error while removing processed files for"
               + " product '" + product.getIdentifier () + "'", e);
      }
   }

   /**
    * Delete a file or a directory via it path.
    *
    * @param path path of file to delete.
    * @throws IOException in case deletion is unsuccessful.
    */
   private void deleteIncomingFolder (String path) throws IOException
   {
      if (path == null)
      {
         return;
      }
      File container = new File (path);
      if (!container.exists () || !incomingManager.isInIncoming (container))
      {
         return;
      }
      if (IncomingManager.INCOMING_PRODUCT_DIR.equals (container
            .getParentFile ().getName ()))
      {
         container = container.getParentFile ();
      }
      if (HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME.equals (container
            .getParentFile ().getName ()))
      {
         container = container.getParentFile ();
      }
      if (container != null)
      {
         FileUtils.forceDelete (container);
      }
   }

   /**
    * Returns true if binaries of product are in the file system storage defined
    * in configuration.
    *
    * @param product product to check for deletion.
    * @return true if product is present in file system storage.
    */
   private boolean isRemovable (Product product)
   {
      String a_path = cfgManager.getArchiveConfiguration ()
            .getIncomingConfiguration ().getPath ();
      if (a_path.startsWith ("file:/"))
      {
         a_path = a_path.substring (6);
      }

      File a_file = new File (a_path);
      if (a_file.exists () && a_file.isDirectory ())
      {
         a_path = a_file.getAbsolutePath ();
         String p_file = product.getPath ().getPath ();
         return p_file.startsWith (a_path) && !product.getLocked ();
      }
      return false;
   }

   /**
    * Moves product download zip into the given destination.
    * <p><b>Note:</b> generates the zip of product if necessary.</p>
    *
    * @param product     product to move.
    * @param destination destination of product
    */
   private void moveProduct (Product product, String destination)
   {

      if (destination == null || destination.trim ().isEmpty ())
      {
         return;
      }

      Path zip_destination = Paths.get (destination);
      String download_path = product.getDownloadablePath ();
      try
      {
         if (download_path != null)
         {
            File product_zip_file = Paths.get (download_path).toFile ();
            FileUtils.moveFileToDirectory (
                  product_zip_file, zip_destination.toFile (), true);
         }
         else
         {
            Path product_path = Paths.get (product.getPath ().getPath ());
            if (UnZip.supported (product_path.toAbsolutePath ().toString ()))
            {
               FileUtils.moveFileToDirectory (
                     product_path.toFile (), zip_destination.toFile (), true);
            }
            else
            {
               zip_destination.resolve (product_path.getFileName ());
               generateZip (product_path.toFile (), zip_destination.toFile ());
            }
         }
      }
      catch (IOException e)
      {
         LOGGER.error("Cannot move product: " + product.getPath () +
               " into " + destination, e);
      }
   }

   /**
    * Generates a zip file.
    *
    * @param source      source file or directory to compress.
    * @param destination destination of zipped file.
    * @return the zipped file.
    */
   private void generateZip (File source, File destination) throws IOException
   {
      if (source == null || !source.exists ())
      {
         throw new IllegalArgumentException ("source file should exist");
      }
      if (destination == null)
      {
         throw new IllegalArgumentException (
               "destination file should be not null");
      }

      FileOutputStream output = new FileOutputStream (destination);
      ZipOutputStream zip_out = new ZipOutputStream (output);
      zip_out.setLevel (
            cfgManager.getDownloadConfiguration ().getCompressionLevel ());

      List<QualifiedFile> file_list = getFileList (source);
      byte[] buffer = new byte[BUFFER_SIZE];
      for (QualifiedFile qualified_file : file_list)
      {
         ZipEntry entry = new ZipEntry (qualified_file.getQualifier ());
         InputStream input = new FileInputStream (qualified_file.getFile ());

         int read;
         zip_out.putNextEntry (entry);
         while ((read = input.read (buffer)) != -1)
         {
            zip_out.write (buffer, 0, read);
         }
         input.close ();
         zip_out.closeEntry ();
      }
      zip_out.close ();
      output.close ();
   }

   /**
    * Computes all normal files present in a file.
    * @param src
    * @return a list of {@link QualifiedFile}
    */
   private List<QualifiedFile> getFileList (File src)
   {
      ArrayList<QualifiedFile> result = new ArrayList<> ();
      getFileList (src, null, result);
      return result;
   }

   /**
    * Internal method of {@link FileSystemDataStore#getFileList(File)}
    */
   private void getFileList (File src, String prefix_folder,
         ArrayList<QualifiedFile> files)
   {
      String qualifier;
      if (prefix_folder == null)
      {
         qualifier = src.getName ();
      }
      else
      {
         qualifier = prefix_folder + File.separator + src.getName ();
      }

      if (src.isFile ())
      {
         files.add (new QualifiedFile (src, qualifier));
      }
      else if (src.isDirectory ())
      {
         for (File file : src.listFiles ())
         {
            getFileList (file, qualifier, files);
         }
      }
   }

   /**
    * Represent a {@link File} with a qualifier name.
    */
   private static class QualifiedFile
   {
      private final File file;
      private final String qualifier;

      public QualifiedFile (File file, String qualifier)
      {
         this.qualifier = qualifier;
         this.file = file;
      }

      public String getQualifier ()
      {
         return qualifier;
      }

      public File getFile ()
      {
         return file;
      }

      @Override
      public String toString ()
      {
         return file.getAbsolutePath () + " --> " + qualifier;
      }
   }
}
