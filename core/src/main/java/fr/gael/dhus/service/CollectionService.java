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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.dao.interfaces.DaoUtils;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.exception.CollectionNameExistingException;
import fr.gael.dhus.service.exception.RequiredFieldMissingException;

/**
 * Collection Service provides connected clients with a set of method to
 * interact with it.
 */
@Service
public class CollectionService extends WebService
{
   private static Log logger = LogFactory.getLog (CollectionService.class);

   @Autowired
   private CollectionDao collectionDao;

   @Autowired
   private ProductDao productDao;

   @Autowired
   private UserDao userDao;

   @Autowired
   private SecurityService securityService;

   @Autowired
   private SearchService searchService;

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public Collection createCollection(Collection collection) throws
         RequiredFieldMissingException, CollectionNameExistingException
   {
      // Can user securityService.getCurrentUser() because
      // there is a required role.
      User user = securityService.getCurrentUser();
      // Ensure root collection exists
      if (collection.getParent ()==null)
         collection.setParent (collectionDao.getRootCollection ());
      checkRequiredFields(collection);
      checkName(collection);
      return collectionDao.create (collection, user);
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void updateCollection(Collection collection) throws
         RequiredFieldMissingException
   {
      checkRequiredFields(collection);
      Collection c = collectionDao.read (collection.getId ());

      String old_name = c.getName();
      String new_name = collection.getName();

      c.setName(new_name);
      c.setDescription (collection.getDescription ());
      collectionDao.update (c);

      if (!new_name.equals(old_name))
      {
         int nb = collectionDao.countAuthorizedProducts(null, collection);

         int top = DaoUtils.DEFAULT_ELEMENTS_PER_PAGE;
         int loop = nb / top;
         if (nb % top != 0) loop += 1;

         Iterator<Product> products;
         for (int i = 0; i < loop; i++)
         {
            products = collectionDao
                  .getAuthorizedProducts(null, c, null, null, (top * i), top)
                  .iterator();
            while (products.hasNext())
            {
               searchService.index(products.next());
            }
         }
      }
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void deleteCollection(Long id)
   {
      Collection collection = collectionDao.read (id);
      logger.info ("Removing collection " + collection.getName ());
      
      int skip=0;
      int step=DaoUtils.DEFAULT_ELEMENTS_PER_PAGE;
      
      do
      {
         List<Product>products = collectionDao.getAuthorizedProducts(null,
            collection, null, null, (skip++)*step,
            DaoUtils.DEFAULT_ELEMENTS_PER_PAGE);

         if ((products==null) || products.isEmpty())
            break;
         
         for (Product p:products)
         {
            searchService.index(p);
         }
      } while (true);
      collectionDao.delete (collection);
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Collection getRootCollection ()
   {
      return collectionDao.getRootCollection ();
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Collection getCollection (Long id)
   {
      return collectionDao.read (id);
   }
   
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Collection> getCollections (Product product)
   {
      return collectionDao.getCollectionsOfProduct(product.getId());
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH','ROLE_UPLOAD')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Collection>getChildren (Long id)
   {
      // Can user securityService.getCurrentUser() because
      // there is a required role.
      User user = securityService.getCurrentUser();
      if (id == null)
         return getHigherCollections (user);

      return collectionDao.getSubCollections (id, user);
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH','ROLE_UPLOAD')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public boolean hasChildren (Long cid)
   {
      // Can user securityService.getCurrentUser() because
      // there is a required role.
      User user = securityService.getCurrentUser();
      return collectionDao.hasChildrenCollection (cid, user);
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void removeProducts (Long cid, Long[] pids)
   {
      collectionDao.removeProducts (cid, pids, null);
      long start = new Date ().getTime ();
      for (Long pid: pids)
      {
         searchService.index(productDao.read(pid));
      }
      long end = new Date ().getTime ();
      logger.info ("[SOLR] Remove " + pids.length + 
         " product(s) from collection spent " + (end-start) + "ms" );
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void addProducts (Long cid, Long[] pids)
   {
      for (int i = 0; i < pids.length; i++)
      {
         systemAddProduct (cid, pids[i], true);
      }
   }

   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void systemAddProduct (Long cid, Long pid, boolean followRights)
   {
      Collection collection = collectionDao.read (cid);
      Product product = productDao.read (pid);

      this.addProductInCollection(collection, product);
      searchService.index(product);
      
      Collection parent = collection.getParent ();
      if (parent != null && !collectionDao.isRoot (parent))
      {
         systemAddProduct (parent.getId (), pid, followRights);
      }
   }
   
   private void addProductInCollection (Collection collection, Product product)
   {
      Hibernate.initialize(collection.getProducts());
      if (!collection.getProducts().contains(product))
      {
         collection.getProducts().add(product);
         collectionDao.update (collection);
      }
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Long> getProductIds(Long cid)
   {
      User user = securityService.getCurrentUser();
      return collectionDao.getProductIds (cid, user);
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Integer count ()
   {
      // Can use securityService.getCurrentUser() because
      // there is a required
      // role.
      User user = securityService.getCurrentUser ();
      return collectionDao.count (user);
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   private void checkName (Collection collection)
      throws RequiredFieldMissingException, CollectionNameExistingException
   {
      if (collection.getName () == null)
      {
         throw new RequiredFieldMissingException (
            "At least one required field is empty.");
      }
      Collection parent = collectionDao.getParent (collection);
      List<Collection> subCollections =
         collectionDao.getSubCollections (parent.getId (), null);
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

   private void checkRequiredFields(Collection collection) throws
         RequiredFieldMissingException
   {
      if (collection.getName () == null || collection.getName ().trim ()
            .isEmpty ())
      {
         throw new RequiredFieldMissingException (
               "At least one required field is empty.");
      }
   }

   @PreAuthorize("isAuthenticated ()")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Set<Collection> getAuthorizedSubCollections (Long cid,
         final User user)
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

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Product getProduct (String uuid, Long collection_id, User u)
   {
      Product p = productDao.getProductByUuid(uuid, u);
      if (collectionDao.contains(collection_id, p.getId()))
         return p;
      return null;
   }                  

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Product> getAuthorizedProducts (Long cid, User u)
   {
      Collection collection = collectionDao.read (cid);
      if (collection == null)
      {
         return Collections.emptyList ();
      }
      return collectionDao.getAuthorizedProducts (u, collection);
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int countAuthorizedSubCollections (Collection c, User u)
   {
      if (c == null)
         return getHigherCollections (u).size ();
      return collectionDao.getSubCollections (c.getId (), u).size ();
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
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

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Long getCollectionByName (String collection_name)
   {
      return collectionDao.getCollectionByName (collection_name);
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public boolean hasAccessToCollection (Long cid, Long uid)
   {
      User user = userDao.read (uid);
      if (user == null) return false;
      if (user.getRoles ().contains (Role.DATA_MANAGER)) return true;
      return collectionDao.hasAccessToCollection (cid, uid);
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public boolean containsProduct (Long cid, Long pid)
   {
      if (cid == null) return false;
      return collectionDao.contains (cid, pid);
   }

   @Transactional (readOnly=true)
   public List<Collection> getCollectionsOfProduct(Product p)
   {
      return collectionDao.getCollectionsOfProduct(p.getId());
   }
}
