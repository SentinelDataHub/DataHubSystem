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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.config.system.IncomingConfiguration;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * @author pidancier
 *
 */
@Component
public class IncomingManager
{
   private static Logger logger = Logger.getLogger (IncomingManager.class);
   
   final public static String INCOMING_PRODUCT_DIR = "product";

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

   
   
   public void initIncoming ()
   {
      getIncomingBuilder ().recomputeCounterFirstFreePath ();
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
         logger.error ("Cannot control incoming folder", e);
      }
   }

   class CheckIncomingWalker extends DirectoryWalker<File>
   {
      public List<File> check (File startDirectory) throws IOException
      {
         List<File> results = new ArrayList<File> ();
         walk (startDirectory, results);
         return results;
      }

      @Override
      protected boolean handleDirectory (File directory, int depth,
         java.util.Collection<File> results)
      {
         if (HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME.equals (directory
            .getName ()))
         {
            LockFactory lf = new NativeFSLockFactory (directory);
            Lock lock = lf.makeLock (".lock-writing");
            try
            {
               if (lock.isLocked ())
               {
                  throw new IOException ("Folder " + directory.getPath () +
                     " currently ingesting data.");
               }
            }
            catch (IOException e)
            {
               logger.warn (e.getMessage ());
               return false;
            }

            String like = "LIKE '%" + directory.getPath () + "%'";
            @SuppressWarnings ("unchecked")
            List<Product> list =
               (List<Product>) productDao.find ( ("from Product " +
                  " where path " + like + " OR" + "  quicklookPath " + like +
                  " OR" + "  thumbnailPath " + like + " OR" +
                  "  download.path " + like).trim ());

            if ( (list == null) || list.isEmpty ())
            {
               logger.warn ("Product located at " + directory.getPath () +
                  " not referenced in database: removing..");
               FileUtils.deleteQuietly (directory);
               results.add (directory);
            }

            return false;
         }
         else
            return true;
      }

      @Override
      protected void handleFile (File file, int depth,
         java.util.Collection<File> results)
      {
         // Nothing to do
      }
   }

}
