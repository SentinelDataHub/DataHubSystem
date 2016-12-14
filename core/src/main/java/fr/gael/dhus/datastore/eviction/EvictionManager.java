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

import fr.gael.dhus.database.dao.EvictionDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.Eviction;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.config.system.ArchiveConfiguration;
import fr.gael.dhus.datastore.exception.DataStoreException;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.system.config.ConfigurationManager;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages eviction functions
 *
 */
@Service
public class EvictionManager
{
   private static final Logger LOGGER = LogManager.getLogger(EvictionManager.class);

   @Autowired
   private ProductDao productDao;

   @Autowired
   private EvictionDao evictionDao;

   @Autowired
   private ConfigurationManager cfgManager;

   @Autowired
   private ProductService productService;

   private EvictionManager(){}

   /**
    * Computes the path to be evicted. If the incoming path has been
    * entered, eviction spaces is managed according to this directory.
    * Otherwise it is computed from the archive data path.
    *
    * @return the path where the eviction is performed.
    */
   private String getPath ()
   {
      ArchiveConfiguration archive = cfgManager.getArchiveConfiguration ();
      String path = archive.getIncomingConfiguration ().getPath ();
      if (path == null) path = archive.getPath ();
      return path;
   }

   /**
    * Computes the date <i>days</i> days ago.
    *
    * @param days number of days
    * @return a date representation of date <i>days</i> ago.
    */
   public Date getKeepPeriod (int days)
   {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_YEAR, -days);
      LOGGER.info("Eviction Max date : " + cal.getTime());
      return cal.getTime();
   }

   /**
    * Computes free space on disk where the eviction works.
    *
    * @return number of available bytes on disk partition.
    */
   public long getFreeSpace ()
   {
      String path = getPath ();
      File fpath = new File(path);
      return fpath.getFreeSpace ();
   }

   /**
    * Computes the total space on disk where the eviction works.
    *
    * @return the total space in byte on disk partition.
    */
   public long getTotalSpace ()
   {
      String path = getPath ();
      File fpath = new File(path);
      return fpath.getTotalSpace ();
   }

   /**
    * Compute space disk usage on partition where the eviction works.
    *
    * @return space disk usage in bytes on disk partition.
    */
   public long getUsableSpace ()
   {
      String path = getPath ();
      File fpath = new File(path);
      return fpath.getUsableSpace ();
   }

   /**
    * Compute percentage of usage space disk of eviction partition.
    *
    * @return space disk usage in percent on disk partition.
    * @see EvictionManager#getUsableSpace()
    * @see EvictionManager#getTotalSpace()
    */
   public float getSpaceUsagePercentage ()
   {
      return 100-(((float)getUsableSpace ()/getTotalSpace ())*100.0f);
   }

   /**
    * Check if the limit of disk usage is exceeded
    *
    * @param eviction eviction system contains the limit of disk usage.
    * @return true if disk usage of <i>eviction</i> is exceeded, otherwise false
    */
   public boolean canEvictFromArchive (Eviction eviction)
   {
      if ((getSpaceUsagePercentage() > eviction.getMaxDiskUsage ()))
         return true;
      return false;
   }

   /**
    * Seeks all products ingested <i>keep_period</i> days ago or more.
    *
    * @param keep_period number of days.
    * @return a iterator of {@link Product}.
    */
   public Iterator<Product> getProductsByIngestionDate(int keep_period)
   {
      return productDao.getProductsByIngestionDate (getKeepPeriod (keep_period));
   }

   /**
    * Seeks the least watched products on the given period.
    * @param keep_period the period in day.
    * @return a iterator of {@link Product}.
    */
   public Iterator<Product> getProductsByLowestAccess (int keep_period)
   {
      // FIXME never call
      return productDao.getProductsLowerAccess (getKeepPeriod (keep_period));
   }

   /**
    * Compute of next evictable products
    */
   public void computeNextProducts ()
   {
      Eviction eviction = evictionDao.getEviction ();
      if (eviction == null)
      {
         LOGGER.warn("No Eviction setting found.");
         return;
      }
      Set<Product>products = eviction.getStrategy ().getProductsToEvict (
            eviction);
      evictionDao.setProducts (products);
   }

   /**
    * Returns the next evictable products.
    * @return a set of {@link Product}.
    */
   public Set<Product>getProducts ()
   {
      return evictionDao.getEviction ().getProducts ();
   }

   /**
    * Performs a eviction of products.
    */
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
         LOGGER.info("No product Evicted.");
      }
   }

   /**
    * Evicts <i>products</i> in a new {@link Thread}
    *
    * @param products set of products to evict.
    */
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

      @Override
      public void run()
      {
         for (Product product:products)
         {
            String path = ProductDao.getPathFromProduct (product);
            path = path.replaceAll ("file://?", "/");
            LOGGER.info("Trying to evict product \"" + path + "\".");

            try
            {
               productService.systemDeleteProduct (product.getId ());
               LOGGER.info("Evicted " + product.getIdentifier () + " (" +
                  product.getSize () + " bytes, " +
                  product.getDownloadableSize () + " bytes compressed)");
            }
            catch (DataStoreException e)
            {
               LOGGER.error("Unable to delete product at path \"" + path +
                  "\": " + e.getMessage (), e);
            }
         }
      }
   }

}
