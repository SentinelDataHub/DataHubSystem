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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import fr.gael.dhus.datastore.exception.DataStoreLocalArchiveNotExistingException;

/**
 * Archive Service provides connected clients with a set of method
 * to interact with it.
 *
 */
@Service ("archiveService")
public class ArchiveService extends WebService
{
   private static final Logger LOGGER = LogManager.getLogger(ArchiveService.class);

   @Autowired
   private ProductService productService;

   /**
    * Resets the archive according to the properties. In the future, a specific
    * panel will be implemented to set the archive settings. 
    * @throws DataStoreLocalArchiveNotExistingException 
    */
   // Role temporaire en attendant le panneau d'archive.
   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public int synchronizeLocalArchive () throws
         DataStoreLocalArchiveNotExistingException
   {
      try
      {
         return productService.processArchiveSync();
      }
      catch (InterruptedException e)
      {
         LOGGER.warn("Synchronization stopped by the user.");
      }
      return -1;
   }

   // Methods for unit tests
   void setDefaultDataStore (ProductService productService)
   {
      this.productService = productService;
   }
}
