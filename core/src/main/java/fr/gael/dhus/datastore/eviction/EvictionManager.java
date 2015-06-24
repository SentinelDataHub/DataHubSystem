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
package fr.gael.dhus.datastore.eviction;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.EvictionDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.Eviction;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.config.system.ArchiveConfiguration;
import fr.gael.dhus.datastore.DefaultDataStore;
import fr.gael.dhus.datastore.exception.DataStoreException;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Manages eviction functions
 *
 */
@Service
public class EvictionManager
{
   private static Log logger = LogFactory.getLog (EvictionManager.class);
      
   @Autowired
   private ProductDao productDao;
   
   @Autowired
   private EvictionDao evictionDao;
   
   @Autowired
   private DefaultDataStore dataStore;
   
   @Autowired
   private ConfigurationManager cfgManager;
      
   private EvictionManager(){}
   
   /**
    * Computes the path to be evicted. If the incoming path has been 
    * entered, eviction spaces is managed according to this directory.
    * Otherwise it is computed from the archive data path.
    * @param archive
    * @return
    */
   private String getPath ()
   {
      ArchiveConfiguration archive = cfgManager.getArchiveConfiguration ();
      String path = archive.getIncomingConfiguration ().getPath ();
      if (path == null) path = archive.getPath ();
      return path;      
   }
   
   public Date getKeepPeriod (int days)
   {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_YEAR, -days);
      logger.info ("Eviction Max date : " + cal.getTime());
      return cal.getTime();
   } 
   
   public long getFreeSpace ()
   {
      String path = getPath ();
      File fpath = new File(path);
      return fpath.getFreeSpace ();
   }   
   
   public long getTotalSpace ()
   {
      String path = getPath ();
      File fpath = new File(path);
      return fpath.getTotalSpace ();
   }
   
   public long getUsableSpace ()
   {
      String path = getPath ();
      File fpath = new File(path);
      return fpath.getUsableSpace ();
   }
   
   /**
    * Returns free usable space
    * @param archive
    * @return
    */
   public float getSpaceUsagePercentage ()
   {
      return 100-(((float)getUsableSpace ()/getTotalSpace ())*100.0f);
   }
   
   /**
    * Product can only be evicted if they have been uploaded (inside incoming 
    * path) and if system archive space is missing.
    * 
    * Uploaded products only are removed because other ones have been manually 
    * added in the archive and should be manually removed.
    * @param product
    * @param eviction
    * @return
    */
   public boolean canEvictFromArchive (Product product, Eviction eviction)
   {
      if ((getSpaceUsagePercentage() > eviction.getMaxDiskUsage ())
           && isRemovable (product))
         return true;
      return false;
   }   
   
   public List<Product> getProductsByIngestionDate(int keepPeriod, int skip, int top)
   {
      return productDao.getProductsByIngestionDate (getKeepPeriod (keepPeriod), skip, top);
   }
   
   public List<Product> getProductsByLowestAccess (int keepPeriod)
   {
      // FIXME never call
      return productDao.getProductsLowerAccess (getKeepPeriod (keepPeriod), -1,
         -1);
   }
   
   public void computeNextProducts ()
   {
      Eviction eviction = evictionDao.getEviction ();
      if (eviction == null)
      {
         logger.warn ("No Eviction setting found.");
         return;
      }
      Set<Product>products = eviction.getStrategy ().getProductsToEvict (eviction);
      evictionDao.setProducts (products);
   }
   
   public Set<Product>getProducts ()
   {
      return evictionDao.getEviction ().getProducts ();
   }
   
   public void doEvict ()
   {
      Set<Product>products = getProducts ();
      evictionDao.setProducts (new HashSet<Product> ());
      int evicted = 0;
      if (products != null)
      {
         evicted = products.size ();
         doEvict (products);
      }
      if (evicted == 0)
      {
         logger.info ("No product Evicted.");
      }
   }
   public void doEvict(Set<Product>products)
   {
      Thread t = new Thread (new DeleteProductTask (products), 
         "doEvictionTread");
      t.start ();
   }

   private class DeleteProductTask implements Runnable
   {
      private Set<Product> products;

      public DeleteProductTask (Set<Product> products)
      {
        this.products = products;
      }
      
      public void run()
      {
         for (Product product:products)
         {
            String path = ProductDao.getPathFromProduct (product);
            path = path.replaceAll ("file://?", "/");
            logger.info ("Evicting product \"" + path + "\".");

            try
            {
               if (isRemovable (product))
               {
                  dataStore.removeProduct (product.getId());
               }
               else
                  logger.warn ("File " + path + 
                     " cannot be removed from read-only archive.");
            }
            catch (DataStoreException e)
            {
               logger.error ("Unable to delete product at path \"" + path + 
                  "\": " + e.getMessage (), e);
            }
         }
      }
   }
   
   /**
    * Checks if the passed product can be removed:
    *  - if it is located in local filesystem and passed to the system by 
    *  upload process. (manually adds shall be manually removed).
    *  - if the product has not been locked.
    * @param product the product to check.
    * @return
    */
   private boolean isRemovable (Product product)
   {
      String a_path = cfgManager.getArchiveConfiguration ().getIncomingConfiguration ().getPath ();
      if (a_path.startsWith ("file:/")) a_path = a_path.substring (6);
      
      String p_file = product.getPath ().getPath ();
      
      return p_file.startsWith (a_path) && !product.getLocked ();
   }
}
