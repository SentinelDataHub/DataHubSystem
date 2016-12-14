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
package fr.gael.dhus.server.http.webapp.api.controller;

import fr.gael.dhus.service.ProductUploadService;
import fr.gael.dhus.service.exception.ProductNotAddedException;
import fr.gael.dhus.service.exception.RootNotModifiableException;
import fr.gael.dhus.service.exception.UploadingException;
import fr.gael.dhus.service.exception.UserNotExistingException;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Deprecated /** To be moved in odata **/
@Controller
public class UploadController
{   
   private static final Logger LOGGER = LogManager.getLogger(UploadController.class);
   
   private static final String COLLECTIONSKEY = "collections";
   private static final String PRODUCTKEY = "product";

   @Autowired
   private ProductUploadService productUploadService;
   
   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   @RequestMapping (value = "/upload", method = {RequestMethod.POST})
   public void upload(Principal principal, HttpServletRequest req,
         HttpServletResponse res) throws IOException
   {
      // process only multipart requests
      if (ServletFileUpload.isMultipartContent (req))
      {
         // Create a factory for disk-based file items
         FileItemFactory factory = new DiskFileItemFactory ();
         // Create a new file upload handler
         ServletFileUpload upload = new ServletFileUpload (factory);

         // Parse the request
         try
         {
            ArrayList<String> collectionIds = new ArrayList<> ();
            FileItem product = null;

            List<FileItem> items = upload.parseRequest (req);
            for (FileItem item : items)
            {
               if (COLLECTIONSKEY.equals (item.getFieldName ()))
               {
                  if (item.getString () != null &&
                     !item.getString ().isEmpty ())
                  {
                     for (String cid : item.getString ().split (","))
                     {
                        collectionIds.add (cid);
                     }
                  }
               }
               else
                  if (PRODUCTKEY.equals (item.getFieldName ()))
                  {
                     product = item;                     
                  }
            }
            if (product == null)
            {
               res.sendError (HttpServletResponse.SC_BAD_REQUEST,
                        "Your request is missing a product file to upload.");
               return;
            }
            productUploadService.upload (product, collectionIds);
            res.setStatus (HttpServletResponse.SC_CREATED);
            res.getWriter ().print ("The file was created successfully.");
            res.flushBuffer ();
         }
         catch (FileUploadException e)
         {
            LOGGER.error("An error occurred while parsing request.", e);
            res.sendError (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
               "An error occurred while parsing request : " + e.getMessage ());
         }
         catch (UserNotExistingException e)
         {
            LOGGER.error("You need to be connected to upload a product.", e);
            res.sendError (HttpServletResponse.SC_UNAUTHORIZED,
               "You need to be connected to upload a product.");
         }
         catch (UploadingException e)
         {
            LOGGER.error("An error occurred while uploading the product.", e);
            res.sendError (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
               "An error occurred while uploading the product : " +
                     e.getMessage ());
         }
         catch (RootNotModifiableException e)
         {
            LOGGER.error("An error occurred while uploading the product.", e);
            res.sendError (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
               "An error occurred while uploading the product : " +
                     e.getMessage ());
         }
         catch (ProductNotAddedException e)
         {
            LOGGER.error("Your product can not be read by the system.", e);
            res.sendError (HttpServletResponse.SC_NOT_ACCEPTABLE,
               "Your product can not be read by the system.");
         }
      }
      else
      {
         res.sendError (HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
            "Request contents type is not supported by the servlet.");
      }
   }
}
