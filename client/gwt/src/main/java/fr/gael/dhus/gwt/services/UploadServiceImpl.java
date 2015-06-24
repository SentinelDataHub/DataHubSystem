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
package fr.gael.dhus.gwt.services;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.FileScannerData;
import fr.gael.dhus.gwt.share.exceptions.UploadServiceException;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * Implements the business methods for the customer service
 * 
 * @author shaines
 */
@RPCService ("uploadService")
public class UploadServiceImpl extends RemoteServiceServlet implements
   UploadService
{
   private static final long serialVersionUID = 4981049293450245170L;

   public void processScan (Long scanId) throws UploadServiceException
   {
      fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UploadService.class);

      try
      {
         uploadService.processScan (scanId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UploadServiceException (e.getMessage ());
      }
   }

   public void stopScan (Long scanId) throws UploadServiceException
   {
      fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UploadService.class);

      try
      {
         uploadService.stopScan (scanId);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UploadServiceException (e.getMessage ());
      }
   }
   
   public List<FileScannerData> getFileScanners () throws UploadServiceException
   {
      fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UploadService.class);
      try
      {
         Set<FileScanner>fs = uploadService.getFileScanners ();
         if (fs == null) return null;
         Iterator<FileScanner> iterator = fs.iterator ();
         ArrayList<FileScannerData> scanners = new ArrayList<FileScannerData> ();
         while (iterator != null && iterator.hasNext ())
         {
            FileScanner scanner = iterator.next ();
            if (scanner != null)
            {
               // BigInteger values. Need to be transformed in real Long
               List<BigInteger> scanCol = uploadService.getFileScannerCollections (scanner.getId());
               List<Long> collections = new ArrayList<Long> ();
               for (BigInteger l : scanCol)
               {
                  collections.add (new Long(l.longValue ()));
               }
               
               FileScannerData sData = new FileScannerData(scanner.getId (), scanner.getUrl (), 
                  scanner.getUsername (), scanner.getPassword (), scanner.getPattern (),
                  collections, scanner.getStatus (), scanner.getStatusMessage (),
                  scanner.isActive ());
               scanners.add(sData);
            }
         }
         Collections.sort (scanners, new Comparator<FileScannerData>()
         {
            @Override
            public int compare (FileScannerData o1, FileScannerData o2)
            {
               return o1.getUrl ().compareTo (o2.getUrl ());
            }
         });
         return scanners;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UploadServiceException (e.getMessage ());
      }
   }
   
   public int countFileScanners () throws UploadServiceException
   {
      fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UploadService.class);
      try
      {
         return uploadService.countFileScanners ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UploadServiceException (e.getMessage ());
      }
   }
   
   public long addFileScanner (String url, String username, String password, String pattern,
      List<Long> collectionDatas) throws UploadServiceException
   {
      fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UploadService.class);

      CollectionService collectionService = ApplicationContextProvider
            .getBean (CollectionService.class);
      try
      {
         Set<Collection> collections = new HashSet<Collection> ();
         for (Long cId : collectionDatas)
         {
            collections.add (collectionService.getCollection (cId));
         }
         FileScanner fs = uploadService.addFileScanner (url, username, password, pattern, collections);
         return fs.getId ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UploadServiceException (e.getMessage ());
      }
   }
   
   public void removeFileScanner (Long id) throws UploadServiceException
   {
      fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UploadService.class);
      try
      {
         uploadService.removeFileScanner (id);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UploadServiceException (e.getMessage ());
      }
   }

   public void updateFileScanner (Long id, String url, String username, String password, String pattern,
      List<Long> collectionDatas) throws UploadServiceException
   {
      fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UploadService.class);

      CollectionService collectionService =
         (CollectionService) ApplicationContextProvider
            .getBean (CollectionService.class);
      try
      {
         Set<Collection> collections = new HashSet<Collection> ();
         for (Long cId : collectionDatas)
         {
            collections.add (collectionService.getCollection (cId));
         }
         uploadService.updateFileScanner (id, url, username, password, pattern, collections);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UploadServiceException (e.getMessage ());
      }
   }
   
   public void setFileScannerActive (Long id, boolean active) throws UploadServiceException
   {
      fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UploadService.class);

      try
      {
         uploadService.setFileScannerActive (id, active);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UploadServiceException (e.getMessage ());
      }
   }
   
   public Date getNextScheduleFileScanner() throws UploadServiceException
   {
      fr.gael.dhus.service.UploadService uploadService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.UploadService.class);

      try
      {
         return uploadService.getNextScheduleFileScanner ();
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new UploadServiceException (e.getMessage ());
      }
   }
}
