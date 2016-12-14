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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.config.system.IncomingConfiguration;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.AsyncFileLock;

/**
 * @author pidancier
 *
 */
@Component
public class IncomingManager
{
   public static final String INCOMING_TEMP_DIR = "tmp";
   public static final String INCOMING_PRODUCT_DIR = "product";
   private static final Logger LOGGER = LogManager.getLogger(IncomingManager.class);

   @Autowired
   private ConfigurationManager cfgManager;

   @Autowired
   private ProductDao productDao;

   public HierarchicalDirectoryBuilder getIncomingBuilder ()
   {
      IncomingConfiguration incoming = cfgManager.getArchiveConfiguration ().
         getIncomingConfiguration ();
      File root = new File (incoming.getPath ());
      if ( !root.exists ())
      {
         root.mkdirs ();
      }
      return new HierarchicalDirectoryBuilder (root, incoming.getMaxFileNo ());
   }

   /**
    * Returns existing available path into incoming folder.
    * @return the path to an existing directory
    */
   public synchronized File getNewIncomingPath ()
   {
      return getIncomingBuilder ().getDirectory ();
   }

   public boolean isInIncoming (File path)
   {
      File root = new File (cfgManager.getArchiveConfiguration ().
         getIncomingConfiguration ().getPath ());
      if (path == null) return false;
      if (path.equals (root))
         return true;
      else
         return isInIncoming (path.getParentFile ());
   }

   public boolean isAnIncomingElement (File file)
   {
      int maxfileno = cfgManager.getArchiveConfiguration ().
         getIncomingConfiguration ().getMaxFileNo ();

      boolean is_digit = true;
      try
      {
         // Incoming folders are "X5F" can be parse "0X5F" by decode
         // Warning '09' means octal value that raise error because 9>8...
         String filename = file.getName ();
         if (filename.toUpperCase ().startsWith ("X")) filename="0"+filename;

         if (Long.decode (filename) > maxfileno)
            throw new NumberFormatException ("Expected value exceeded.");
      }
      catch (NumberFormatException e)
      {
         is_digit = false;
      }

      return isInIncoming (file) &&
         (is_digit ||
            file.getName ().equals (
               HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME) || (file
            .getName ().equals (INCOMING_PRODUCT_DIR) && file.getParentFile ()
            .getName ().equals (HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME)));
   }

   /**
    * Returns existing available path into incoming folder to store products.
    * @return the path to an existing product directory
    */
   public synchronized File getNewProductIncomingPath ()
   {
      File file = new File (getNewIncomingPath (), INCOMING_PRODUCT_DIR);
      file.mkdirs ();
      return file;
   }

   /**
    * Returns an existing directory on the same filesystem for temporary files.
    * Files in this temp dir should be atomically moved into the incoming.
    * @return the path to the temp directory.
    */
   public File getTempDir() {
      File res = new File(getIncomingBuilder().getRoot(), INCOMING_TEMP_DIR);
      if (!res.exists())
      {
         res.mkdir();
      }
      return res;
   }

   /**
    * The initialization of incoming manager.
    */
   public void initIncoming ()
   {
      getIncomingBuilder().init ();
   }

   public void checkIncomming ()
   {
      HierarchicalDirectoryBuilder builder = getIncomingBuilder ();
      File root = builder.getRoot ();
      CheckIncomingWalker walker = new CheckIncomingWalker ();
      try
      {
         walker.check (root);
      }
      catch (IOException e)
      {
         LOGGER.error("Cannot control incoming folder", e);
      }
   }

   class CheckIncomingWalker extends DirectoryWalker<File>
   {
      public List<File> check (File start_directory) throws IOException
      {
         List<File> results = new ArrayList<File> ();
         walk (start_directory, results);
         return results;
      }

      @Override
      protected boolean handleDirectory (File directory, int depth, Collection<File> results)
      {
         if (!HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME.equals(directory.getName()))
         {
            return true;
         }

         Path lock_path = Paths.get(directory.getAbsolutePath(), ".lock-writing");
         try (AsyncFileLock afl = new AsyncFileLock(lock_path))
         {
            if (afl.isLocked())
            {
               return false;
            }
         }
         catch (IOException e)
         {
            LOGGER.warn(e.getMessage ());
            return false;
         }

         String like = "like '%" + directory.getPath () + "%'";
         String query = String.format(
               "where path %s or quicklookPath %s or thumbnailPath %s or download.path %s",
               like, like, like, like);

         List list = productDao.find(query);

         if ( (list == null) || list.isEmpty ())
         {
            LOGGER.warn("Product located at " + directory.getPath() +
               " not referenced in database: removing..");
            FileUtils.deleteQuietly (directory);
            results.add (directory);
         }

         return false;
      }

      @Override
      protected void handleFile (File file, int depth, Collection<File> results)
      {
         // Nothing to do
      }
   }

}
