/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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

import java.util.ArrayList;
import java.util.List;

import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.FileScannerDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.database.object.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileScannerService
{
   @Autowired
   private FileScannerDao fileScannerDao;

   @Autowired
   private CollectionDao collectionDao;

   /**
    * Updates a existing file scanner in database.
    * @param file_scanner file scanner to update.
    */
   @Transactional
   public void updateFileScanner (final FileScanner file_scanner)
   {
      if (fileScannerDao.exists (file_scanner))
      {
         // The usage of merge is required here because the file_scanner entity
         // is already referenced via eager reference to user/pref/filescanner
         // This later is never modified, so the merge can be done safty.
         fileScannerDao.merge (file_scanner);
      }
   }

   /**
    * Retrieves a file scanner from the given primary key.
    * @param id the file scanner identifier.
    * @return a persistent file scanner instance or null if the id is not valid.
    */
   @Transactional(readOnly = true)
   public FileScanner getFileScanner (Long id)
   {
      return fileScannerDao.read (id);
   }

   /**
    * Retrieves all active file scanners.
    * @return a list of {@link FileScanner}
    */
   @Transactional (readOnly = true)
   public List<FileScanner> getActiveScanner ()
   {
      return fileScannerDao.find ("From FileScanner WHERE active = true");
   }

   /**
    * Retrieves the creator of the given file scanner.
    * @param file_scanner the file scanner
    * @return the {@link User} which own the given file scanner.
    */
   @Transactional(readOnly = true)
   public User getFileScannerOwner (FileScanner file_scanner)
   {
      return fileScannerDao.getUserFromScanner (file_scanner);
   }

   /**
    * Returns all associated collections to the given file scanner.
    * @param fileScanner
    * @return a list of Collection.
    */
   @Transactional
   public List<Collection> getScannerCollection (FileScanner fileScanner)
   {
      List<Collection> result = new ArrayList<> ();

      List<String> cids = fileScannerDao.getScannerCollections(fileScanner.getId());

      if (cids != null && !cids.isEmpty ())
      {
         for (String cid : cids)
         {
            Collection collection = collectionDao.read (cid);
            if (collection != null)
            {
               result.add (collection);
            }
         }
      }
      return result;
   }
}
