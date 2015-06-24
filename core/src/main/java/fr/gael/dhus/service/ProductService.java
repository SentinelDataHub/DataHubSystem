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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.DefaultDataStore;

/**
 * Product Service provides connected clients with a set of method
 * to interact with it.
 */
@Service
public class ProductService extends WebService
{
   private static Log logger = LogFactory.getLog (ProductService.class);
   
   @Autowired
   private ProductDao productDao;
   
   @Autowired
   private UserDao userDoa;
   
   @Autowired
   private DefaultDataStore dataStore;
   
   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;

   @Autowired
   private SecurityService securityService;

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public List<Product> getProducts (String filter, Long parentId, int skip,
      int top)
   {      
      // Can user securityService.getCurrentUser() because there is a required role.
      User user = securityService.getCurrentUser();
      return productDao.scrollFiltered (filter, parentId, user, skip, top);
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   public Product getProduct (Long id)
   {
      Product p = productDao.read (id);
      return p;
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   public Product getProduct (String uuid)
   {
      User user = securityService.getCurrentUser ();
      Product p = productDao.getProductByUuid (uuid, user);
      return p;
   }

   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   public Product getProductToDownload (Long id)
   {
      User user = securityService.getCurrentUser ();
      Product p = productDao.read (id);
      actionRecordWritterDao.downloadStart(p.getIdentifier (), p.getSize (),
         user.getUsername ());
      return p;
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DOWNLOAD','ROLE_SEARCH')")
   public InputStream getProductQuickLook (Long id)
   {
      Product product = getProduct (id);
      if (!product.getQuicklookFlag ()) return null;
      try
      {
         return new FileInputStream (product.getQuicklookPath ());
      }
      catch (Exception e)
      {
         logger.warn ("Cannot retrieve Quicklook from product id #" + id,e);
      }
      return null;
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DOWNLOAD','ROLE_SEARCH')")
   public long getProductQuickLookContentLength (Long id)
   {
      return getProduct (id).getQuicklookSize ();
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DOWNLOAD','ROLE_SEARCH')")
   public InputStream getProductThumbnail (Long id)
   {
      Product product = getProduct (id);
      if (!product.getThumbnailFlag ()) return null;
      try
      {
         return new FileInputStream (product.getThumbnailPath ());
      }
      catch (Exception e)
      {
         logger.warn ("Cannot retrieve Thumbnail from product id #" + id,e);
      }
      return null;
   }
   
   
   @PreAuthorize ("hasAnyRole('ROLE_DOWNLOAD','ROLE_SEARCH')")
   public long getProductThumbnailContentLength (Long id)
   {
      return getProduct (id).getThumbnailSize ();
   } 

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   public Integer count(String filter, Long parentId)
   {
      // Can user securityService.getCurrentUser() because there is a required role.
      User user = securityService.getCurrentUser();
      return productDao.count (filter, parentId, user);
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   public Integer count(String filter)
   {
      // Can user securityService.getCurrentUser() because there is a required role.
      User user = securityService.getCurrentUser();
      return productDao.count (filter, null, user);
   }
   
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void deleteProduct(Long pid)
   {
      dataStore.removeProduct (pid);
   }

   @PreAuthorize ("isAuthenticated ()")
   public Product getProduct (String uuid, User user)
   {
      return productDao.getProductByUuid (uuid, user);
   }

   public int countAuthorizedProducts (User u)
   {
      return productDao.getAuthorizedProducts (u.getId ()).size ();
   }

   public boolean hasAccessToProduct (long userId, long productId)
   {
      User user = userDoa.read (userId);

      if (user == null) return false;

      if (user.getRoles ().contains (Role.DATA_MANAGER)) return true;

      return productDao.isAuthorized (userId, productId);
   }
}
