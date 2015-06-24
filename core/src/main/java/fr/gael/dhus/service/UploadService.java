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

import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.FileScannerDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.DefaultDataStore;
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

@Service
public class UploadService extends WebService
{
   private static Log logger = LogFactory.getLog (UploadService.class);
   
   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;
   
   @Autowired
   private SecurityService securityService;
   
   @Autowired
   private FileScannerDao fileScannerDao;
   
   @Autowired
   private UserDao userDao;

   @Autowired
   private DefaultDataStore dataStore;

   @Autowired
   private ScannerFactory scannerFactory;

   @Autowired
   private JobScheduler scheduler;
   
   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public boolean addProduct (URL path, final User owner, 
      final List<Collection> collections) throws ProductNotAddedException
   {
      File product = null;
      File newProduct = null;
      try
      {
         logger.info ("Reading uploaded product : " + path.toExternalForm ());
         
         product = new File(path.toURI ());
         if (product.isFile () && UnZip.supported (product.getName ()))
         {
            try
            {
               UnZip.unCompress (product.getPath (), product.getParent ());
            }
            catch (Exception e)
            {
               logger.error ("Failure during decompression.", e);
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
               actionRecordWritterDao.uploadEnd (path, owner.getUsername (), 
                  collections, false);
               throw new DataStoreNotReadableProduct ("No product recognized");
            }
            
            for (URLExt url: scanner.getScanList ())
            {
               dataStore.addProduct (url.getUrl (),
                  owner, collections, null, scanner, null);
               actionRecordWritterDao.uploadEnd (url.getUrl(), 
                  owner.getUsername (), collections, true);
            }
            return true;
         }
         /* Case of one file product i.e. ENVISAT uploaded uncompressed */
         else if (product.isFile ())
         {
            dataStore.addProduct (path, owner, collections, null, null, null);
            actionRecordWritterDao.uploadEnd (path, owner.getUsername (),
               collections, true);
            return true;
         }
         else
         {
            actionRecordWritterDao.uploadEnd (path, owner.getUsername (), 
               collections, false);
            throw new DataStoreNotReadableProduct (
               "Uploaded product media not supported.");
         }
      }
      catch (DataStoreAlreadyExistException e)
      {
         // later
         actionRecordWritterDao.uploadEnd (path, owner.getUsername (), 
            collections, false);
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
         actionRecordWritterDao.uploadFailed (path.toString (), 
            owner.getUsername ());
         throw new ProductNotAddedException();
      }
      catch (MalformedURLException e1)
      {
         logger.warn ("There was a problem accessing \""+path+"\"");
         actionRecordWritterDao.uploadEnd (path, owner.getUsername (), 
            collections, false);
         return false;
      }
      catch (URISyntaxException e1)
      {
         logger.warn ("There was a problem accessing \""+path+"\"");
         actionRecordWritterDao.uploadEnd (path, owner.getUsername (), 
            collections, false);
         return false;
      }
   }
   
   private static boolean deleteDir(File dir) {
      if (dir.isDirectory()) {
          String[] children = dir.list();
          for (int i=0; i<children.length; i++) {
              boolean success = deleteDir(new File(dir, children[i]));
              if (!success) {
                  return false;
              }
          }
      }
  
      // The directory is now empty so delete it
      return dir.delete();
  } 

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public void processScan (final Long scanId)
   {
      User user = securityService.getCurrentUser ();
      try
      {
         scannerFactory.processScan (scanId, user);
      }
      catch (ScannerException e)
      {
         logger.info ("Scanner id #" + scanId + " not started: " +
            e.getMessage ());
      }
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public void stopScan (final Long scanId)
   {
      try
      {
         scannerFactory.stopScan (scanId);
      }
      catch (ScannerException e)
      {
         logger.info ("Scanner id #" + scanId + " not started: " +
            e.getMessage ());
      }
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
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
   public List<BigInteger> getFileScannerCollections (Long id)
   {
      return fileScannerDao.getScannerCollections (id);
   }

   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   public Date getNextScheduleFileScanner() throws SchedulerException
   {
      return scheduler.getNextFileScannerJobSchedule ();
   }
}
