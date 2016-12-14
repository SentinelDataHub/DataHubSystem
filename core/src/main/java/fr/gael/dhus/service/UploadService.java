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
package fr.gael.dhus.service;

import fr.gael.dhus.database.dao.FileScannerDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.exception.DataStoreAlreadyExistException;
import fr.gael.dhus.datastore.exception.DataStoreNotReadableProduct;
import fr.gael.dhus.datastore.scanner.Scanner;
import fr.gael.dhus.datastore.scanner.ScannerException;
import fr.gael.dhus.datastore.scanner.ScannerFactory;
import fr.gael.dhus.datastore.scanner.URLExt;
import fr.gael.dhus.service.exception.FileScannerNotModifiableException;
import fr.gael.dhus.service.exception.ProductNotAddedException;
import fr.gael.dhus.service.job.JobScheduler;
import fr.gael.dhus.util.UnZip;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UploadService extends WebService
{
   private static final Logger LOGGER = LogManager.getLogger(UploadService.class);

   @Autowired
   private SecurityService securityService;
   
   @Autowired
   private FileScannerDao fileScannerDao;
   
   @Autowired
   private UserDao userDao;

   @Autowired
   private ProductService productService;

   @Autowired
   private ScannerFactory scannerFactory;

   @Autowired
   private JobScheduler scheduler;
   
   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   @Transactional (propagation=Propagation.REQUIRED)
   public boolean addProduct (URL path, final User owner, 
      final List<Collection> collections) throws ProductNotAddedException
   {
      File product = null;
      File newProduct = null;
      try
      {
         LOGGER.info("Reading uploaded product : " + path.toExternalForm ());
         
         product = new File(path.toURI ());
         if (product.isFile () && UnZip.supported (product.getPath ()))
         {
            try
            {
               UnZip.unCompress (product.getPath (), product.getParent ());
            }
            catch (Exception e)
            {
               LOGGER.error("Failure during decompression.", e);
               DataStoreNotReadableProduct dse = 
                  new DataStoreNotReadableProduct ();
               dse.initCause (e);
               throw dse;
            }
            
            newProduct = new File(product.getParent ());
            path = newProduct.toURI ().toURL ();
            product.delete ();
            
            Scanner scanner = scannerFactory.getScanner (
               path.toExternalForm ());
            scanner.setSupportedClasses (scannerFactory.getScannerSupport ());
            scanner.getScanList ().simulate (false);
            try
            {
               scanner.scan ();
            }
            catch (InterruptedException e)
            {
               throw new DataStoreNotReadableProduct (
                  "Process stopped by the user");
            }
            
            if (scanner.getScanList ().size () == 0)
            {
               throw new DataStoreNotReadableProduct ("No product recognized");
            }
            
            for (URLExt url: scanner.getScanList ())
            {
               Product p = productService.addProduct (
                     url.getUrl (), owner, null);
               productService.processProduct (
                     p, owner, collections, scanner, null);
            }
            return true;
         }
         /* Case of one file product i.e. ENVISAT uploaded uncompressed */
         else if (product.isFile ())
         {
            Product p = productService.addProduct (path, owner, null);
            productService.processProduct (p, owner, collections, null, null);
            return true;
         }
         else
         {
            throw new DataStoreNotReadableProduct (
               "Uploaded product media not supported.");
         }
      }
      catch (DataStoreAlreadyExistException e)
      {
         return false;
      }
      catch (DataStoreNotReadableProduct e)
      {
         if (product != null && product.exists ())
         {
            product.delete ();
         }
         if (newProduct != null && newProduct.exists ())
         {
            deleteDir(newProduct);
         }
         throw new ProductNotAddedException("Upload failed.", e);
      }
      catch (MalformedURLException e1)
      {
         LOGGER.warn("There was a problem accessing \""+path+"\"");
         return false;
      }
      catch (URISyntaxException e1)
      {
         LOGGER.warn("There was a problem accessing \""+path+"\"");
         return false;
      }
   }
   
   private static boolean deleteDir(File dir)
   {
      if (dir.isDirectory())
      {
          String[] children = dir.list();
          for (int i=0; i<children.length; i++)
          {
              boolean success = deleteDir(new File(dir, children[i]));
              if (!success)
              {
                  return false;
              }
          }
      }
  
      // The directory is now empty so delete it
      return dir.delete();
  } 

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public void processScan (final Long scan_id)
   {
      User user = securityService.getCurrentUser ();
      try
      {
         scannerFactory.processScan (scan_id, user);
      }
      catch (ScannerException e)
      {
         LOGGER.info("Scanner id #" + scan_id + " not started: " +
            e.getMessage ());
      }
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public void stopScan (final Long scan_id)
   {
      try
      {
         scannerFactory.stopScan (scan_id);
      }
      catch (ScannerException e)
      {
         LOGGER.info("Scanner id #" + scan_id + " not started: " +
            e.getMessage ());
      }
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   @Transactional
   public FileScanner addFileScanner (String url, String username, 
      String password, String pattern, Set<Collection> collections)
   {
      User user = securityService.getCurrentUser ();
      return userDao.addFileScanner (user, url, username, password, pattern, "",
         collections);
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public void removeFileScanner (Long id)
   {
      User user = securityService.getCurrentUser ();
      userDao.removeFileScanner (user, id);
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   @Transactional
   public Set<FileScanner> getFileScanners ()
   {
      User user = securityService.getCurrentUser ();
      return userDao.getFileScanners (user);
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public int countFileScanners ()
   {
      User user = securityService.getCurrentUser ();
      return userDao.getFileScanners (user).size ();
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   @Transactional
   public void updateFileScanner (Long id, String url, String username, 
      String password, String pattern, Set<Collection> collections)
   {
      FileScanner fileScanner = fileScannerDao.read (id);
      if ((fileScanner == null) || 
          (fileScanner.getStatus () == FileScanner.STATUS_RUNNING))
      {
         // Why ??
         throw new FileScannerNotModifiableException (
            "File scanner is running and cannot be modified.");
      }
      userDao.updateFileScanner (id, url, username, password, pattern, "", 
         collections);
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   @Transactional
   public void setFileScannerActive (Long id, boolean active)
   {
      FileScanner fileScanner = fileScannerDao.read (id);
      if (fileScanner == null)
      {
         throw new FileScannerNotModifiableException (
            "Scanner Id #" + id + " not found.");
      }
      userDao.setFileScannerActive (id, active);
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public List<String> getFileScannerCollections (Long id)
   {
      return fileScannerDao.getScannerCollections (id);
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public Date getNextScheduleFileScanner() throws SchedulerException
   {
      return scheduler.getNextFileScannerJobSchedule ();
   }
}
