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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.search.SolrDao;
import fr.gael.dhus.service.exception.CollectionNameExistingException;
import fr.gael.dhus.service.exception.RequiredFieldMissingException;

/**
 * Collection Service provides connected clients with a set of method to
 * interact with it.
 */
@Service
public class CollectionService extends WebService
{
   @Autowired
   private CollectionDao collectionDao;
   
   @Autowired
   private ProductDao productDao;
   
   @Autowired
   private UserDao userDao;
   
   @Autowired 
   private SecurityService securityService;
   
   @Autowired
   private SolrDao solrDao;
   
   private static Log logger = LogFactory.getLog (CollectionService.class);
   
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public Collection createCollection(Collection collection) throws RequiredFieldMissingException, CollectionNameExistingException
   {
      // Can user securityService.getCurrentUser() because there is a required role.
      User user = securityService.getCurrentUser();
      // Ensure root collection exists
      if (collection.getParent ()==null) 
         collection.setParent (collectionDao.getRootCollection ());
      checkRequiredFields(collection);
      checkName(collection, user);
      return collectionDao.create (collection, user);
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void updateCollection(Collection collection) throws RequiredFieldMissingException
   {
      checkRequiredFields(collection);
      Collection c = collectionDao.read (collection.getId ());    
      
      c.setName (collection.getName ());
      c.setDescription (collection.getDescription ());
      // c.setProducts (collection.getProducts ());
      collectionDao.update (c);
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void deleteCollection(Long id) 
   {
      Collection collection = collectionDao.read (id);
      logger.info ("Removing collection " + collection.getName ());
      collectionDao.delete (collection);
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   public Collection getRootCollection ()
   {
      return collectionDao.getRootCollection ();
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   public Collection getCollection (Long id)
   {
      return collectionDao.read (id);
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH','ROLE_UPLOAD')")
   public List<Collection>getChildren (Long id)
   {
      // Can user securityService.getCurrentUser() because there is a required role.
      User user = securityService.getCurrentUser();
      if (id == null)
         return getHigherCollections (user);

      return collectionDao.getSubCollections (id, user);
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH','ROLE_UPLOAD')")
   public boolean hasChildren (Long cid)
   {
      // Can user securityService.getCurrentUser() because there is a required role.
      User user = securityService.getCurrentUser();
      return collectionDao.hasChildrenCollection (cid, user);
   }
      
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void removeProducts (Long cid, Long[] pids)
   {
      // Can user securityService.getCurrentUser() because there is a required role.
      User user = securityService.getCurrentUser();
      collectionDao.removeProducts (cid, pids, user);
      for (Long pid: pids)
         solrDao.removeProductFromCollection (productDao.read (pid), 
            collectionDao.read (cid).getName ());
      SolrDao.resetQueryCache ();
   }
   
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public void addProducts (Long cid, Long[] pids)
   {
      collectionDao.addProducts (cid, pids);
      
      for (Long pid: pids)
         solrDao.addProductInCollection (productDao.read (pid), 
            collectionDao.read (cid).getName ());
      SolrDao.resetQueryCache ();

   }
   
   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   public List<Long> getProductIds(Long cid)
   {
      // Can user securityService.getCurrentUser() because there is a required role.
      User user = securityService.getCurrentUser();
      return collectionDao.getProductIds (cid, user);
   }
   
   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   public Integer count ()
   {
      // Can use securityService.getCurrentUser() because there is a required
      // role.
      User user = securityService.getCurrentUser ();
      return collectionDao.count (user);
   }

   private void checkName (Collection collection, User user)
      throws RequiredFieldMissingException, CollectionNameExistingException
   {
      if (collection.getName () == null)
      {
         throw new RequiredFieldMissingException (
            "At least one required field is empty.");
      }
      Collection parent = collectionDao.getParent (collection);
      List<Collection> subCollections =
         collectionDao.getSubCollections (parent.getId (), user);
      for (Collection subCollection : subCollections)
      {
         if (subCollection.getName ().equals (collection.getName ()))
         {
            String parentStr =
               collectionDao.isRoot (parent) ? "root collection"
                  : "subcollection of '" + parent.getName () + "'";
            throw new CollectionNameExistingException ("A " + parentStr +
               " is already named '" + collection.getName () + "'.");
         }
      }
   }

   private void checkRequiredFields(Collection collection) throws RequiredFieldMissingException
   {
      if (collection.getName () == null || collection.getName ().trim ().isEmpty ())
      {
         throw new RequiredFieldMissingException ("At least one required field is empty.");
      }
   }
   
   @PreAuthorize("isAuthenticated ()")
   public Set<Collection> getAuthorizedSubCollections (Long cid, User user)
   {
      HashSet<Collection> authorizedSubCollections = new HashSet<Collection> ();

      if (user.getRoles ().contains (Role.DATA_MANAGER))
      {
         List<Collection> list;
         if (cid == null)
         {
            list = collectionDao.getAllSubCollection (
               collectionDao.getRootCollection ());
         }
         else
         {
            list = collectionDao.getAllSubCollection (collectionDao.read (cid));
         }

         for (Collection collection : list)
         {
            authorizedSubCollections.add (collection);
         }
         return authorizedSubCollections;
      }

      for (Collection subCollection : getChildren (cid))
      {
         if (collectionDao.getAuthorizedUsers (subCollection).contains (user))
         {
            authorizedSubCollections.add (subCollection);
         }
      }
      return authorizedSubCollections;
   }

   public Product getProduct (String uuid, Long collectionId, User u)
   {
      List<Product> products = productDao.read (collectionDao.getProductIds (
         collectionId, u));
      for (Product product : products)
      {
         if (product.getUuid ().equals (uuid))
         {
            if (u.getRoles ().contains (Role.DATA_MANAGER) && product.getProcessed ())
               return product;

            if (productDao.getAuthorizedProducts (u.getId ()).contains (
               product.getId ()))
               return product;

            break;
         }
      }
      return null;
   }
   
   public List<Product> getAuthorizedProducts (Long cid, User u)
   {
      List<Product> result = new ArrayList<Product> ();
      Collection collection = collectionDao.read (cid);
      if (collection == null || u == null) return result;

      List<Product> products =
         collectionDao.getAuthorizedProducts (u, collection);
      for (Product product : products)
      {
         if (productDao.getAuthorizedUsers (product).contains (u))
         {
            result.add (product);
         }
      }
      return result;
   }

   public int countAuthorizedSubCollections (Collection c, User u)
   {
      if (c == null)
         return getHigherCollections (u).size ();
      return collectionDao.getSubCollections (c.getId (), u).size ();
   }
   
   public List<Collection> getHigherCollections (User user)
   {
      if (user.getRoles ().contains (Role.DATA_MANAGER))
      {
         return collectionDao.getAllSubCollection (collectionDao
            .getRootCollection ());
      }

      ArrayList<Collection> result = new ArrayList<Collection> ();
      List<Long> authCollectionIds =
         collectionDao.getAuthorizedCollections (user.getId ());
      for (Long collectionId : authCollectionIds)
      {
         Collection collection = collectionDao.read (collectionId);
         Collection parent = collectionDao.getParent (collection);
         if (parent != null &&
            !collectionDao.getAuthorizedUsers (parent).contains (user))
         {
            result.add (collection);
         }
      }
      return result;
   }

   public Long getCollectionByName (String collectionName)
   {
      return collectionDao.getCollectionByName (collectionName);
   }

   public boolean hasAccessToCollection (Long cid, Long uid)
   {
      User user = userDao.read (uid);
      if (user == null) return false;
      if (user.getRoles ().contains (Role.DATA_MANAGER)) return true;
      return collectionDao.hasAccessToCollection (cid, uid);
   }

   public boolean containsProduct (Long cid, Long pid)
   {
      if (cid == null) return false;
      return collectionDao.contains (cid, pid);
   }
}
