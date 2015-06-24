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
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.DefaultDataStore;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.messaging.jms.Message;
import fr.gael.dhus.messaging.jms.Message.MessageType;
import fr.gael.dhus.service.exception.ProductNotAddedException;
import fr.gael.dhus.service.exception.RootNotModifiableException;
import fr.gael.dhus.service.exception.UploadingException;

@Service
public class ProductUploadService extends WebService
{
   private static Log logger = LogFactory.getLog (ProductUploadService.class);
   
   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;
   
   @Autowired
   private SecurityService securityService;
   
   @Autowired
   private CollectionService collectionService;
   
   @Autowired
   private UploadService uploadService;
   
   @Autowired
   IncomingManager incomingManager;
      
   @Autowired
   private DefaultDataStore dataStore;   
   
   @PreAuthorize("hasRole('ROLE_UPLOAD')") // throws UploadingException, UserNotExistingException
   public void upload (Long userId, FileItem product, ArrayList<Long> collectionIds) throws UploadingException, RootNotModifiableException, ProductNotAddedException 
   {
      User owner = securityService.getCurrentUser (); //userService.getUser (userId);
      ArrayList<Collection> collections = new ArrayList<Collection> ();
      for (Long cId : collectionIds)
      {
         Collection c;
//         try
//         {
            c = collectionService.getCollection (cId);
//         }
//         catch (CollectionNotExistingException e)
//         {
//            continue;
//         }
         collections.add (c);
      }

      String fileName = product.getName ();
      try
      {
         logger.info (new Message(MessageType.UPLOADS, owner.getUsername () + 
            " tries to upload product '" +fileName+ "'"));
         actionRecordWritterDao
            .uploadStart (fileName, owner.getUsername ());
         if (fileName != null)
         {
            fileName = FilenameUtils.getName (fileName);
         }
         File path =null;
         try
         {
            path = incomingManager.getNewProductIncomingPath ();
            if (path == null)
               throw new UnsupportedOperationException (
                  "Computed upload path is not available.");
         }
         catch (Exception e)
         {
            // actionRecordWritterDao.uploadFailed (fileName, owner);
            throw e;
         }
         File uploadedFile = new File (path, fileName);
         if (uploadedFile.createNewFile ())
            product.write (uploadedFile);
         else
            throw new IOException ("The file already exists in repository.");
//         uploadDone (uploadedFile.toURI ().toURL (), owner, collections);
         uploadService.addProduct (uploadedFile.toURI ().toURL (), owner, collections);
      }
      catch (ProductNotAddedException e)
      {
         throw e;
      }
//      catch (UploadingException e)
//      {
//         throw new UploadingException ("An error occured when uploading '" +
//                  fileName + "'", e);
//      }      
      catch (Exception e)
      {
         actionRecordWritterDao.uploadFailed (fileName,
            owner.getUsername ());
         throw new UploadingException ("An error occured when uploading '" +
            fileName + "'", e);
      }
   }
}
