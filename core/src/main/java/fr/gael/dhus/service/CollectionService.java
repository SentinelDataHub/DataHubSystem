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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.exception.CollectionNameExistingException;
import fr.gael.dhus.service.exception.RequiredFieldMissingException;
import fr.gael.dhus.system.config.ConfigurationManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Collection Service provides connected clients with a set of method to
 * interact with it.
 */
@Service
public class CollectionService extends WebService
{
   private static final Logger LOGGER = LogManager.getLogger(CollectionService.class);

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

   @Autowired
   private ConfigurationManager cfgManager;

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public Collection createCollection(Collection collection) throws
         RequiredFieldMissingException, CollectionNameExistingException
   {
      // Can user securityService.getCurrentUser() because
      // there is a required role.
      User user = securityService.getCurrentUser();
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
      Collection c = collectionDao.read (collection.getUUID ());

      String old_name = c.getName();
      String new_name = collection.getName();
      c.setName(new_name);
      c.setDescription (collection.getDescription ());

      if (!new_name.equals(old_name))
      {
         Iterator<Collection> collectionIterator =
               collectionDao.getAllCollections ();

         while (collectionIterator.hasNext ())
         {
            c = collectionIterator.next ();
            for (Product product : c.getProducts ())
            {
               searchService.index (product);
            }
         }
      }
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void deleteCollection(String uuid)
   {
      Collection collection = collectionDao.read (uuid);
      LOGGER.info("Removing collection " + collection.getName ());
      for (Product product : collection.getProducts ())
      {
         searchService.index (product);
      }
      collectionDao.delete (collection);
   }

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Collection getCollection (String uuid)
   {
      return systemGetCollection (uuid);
   }
   
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Collection> getCollections (Product product)
   {
      return collectionDao.getCollectionsOfProduct(product.getId());
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void removeProducts (String uuid, Long[] pids)
   {
      collectionDao.removeProducts (uuid, pids, null);
      long start = new Date ().getTime ();
      for (Long pid: pids)
      {
         searchService.index(productDao.read(pid));
      }
      long end = new Date ().getTime ();
      LOGGER.info("[SOLR] Remove " + pids.length +
         " product(s) from collection spent " + (end-start) + "ms" );
   }

   @PreAuthorize ("hasRole('ROLE_DATA_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void addProducts (String uuid, Long[] pids)
   {
      for (int i = 0; i < pids.length; i++)
      {
         systemAddProduct (uuid, pids[i], true);
      }
   }

   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void systemAddProduct (String uuid, Long pid, boolean followRights)
   {
      Collection collection = collectionDao.read (uuid);
      Product product = productDao.read (pid);

      this.addProductInCollection(collection, product);
      searchService.index(product);
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

   @PreAuthorize ("hasAnyRole('ROLE_DATA_MANAGER','ROLE_SEARCH')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Long> getProductIds(String uuid)
   {
      User user = securityService.getCurrentUser();
      return collectionDao.getProductIds (uuid, user);
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
      final String toCheck = collection.getName ();
      if (toCheck == null)
      {
         throw new RequiredFieldMissingException (
            "At least one required field is empty.");
      }

      Iterator<Collection> it = collectionDao.getAllCollections ();
      while (it.hasNext ())
      {
         final String name = it.next ().getName ();
         if (toCheck.equals (name))
         {
            throw new CollectionNameExistingException ("Collection name '" +
                  collection.getName () + "' is already used.");
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

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Product getProduct (String uuid, String collection_uuid, User u)
   {
      Product p = productDao.getProductByUuid(uuid);
      if (collectionDao.contains(collection_uuid, p.getId()))
         return p;
      return null;
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Product> getAuthorizedProducts (String uuid, User u)
   {
      Collection collection = collectionDao.read (uuid);
      if (collection == null)
      {
         return Collections.emptyList ();
      }

      List<Product> products = new LinkedList<> ();
      Iterator<Long> it = getProductIds (collection.getUUID ()).iterator ();
      while (it.hasNext ())
      {
         Long pid = it.next ();
         if (pid != null)
         {
            Product p = productDao.read (pid);
            if (p != null)
            {
               products.add (p);
            }
         }
      }

      return products;
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public String getCollectionUUIDByName(String collection_name)
   {
      return collectionDao.getCollectionUUIDByName(collection_name);
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public boolean hasAccessToCollection (String cid, String uid)
   {
      User user = userDao.read (uid);
      if (user == null) return false;
      if (user.getRoles ().contains (Role.DATA_MANAGER)) return true;
      return collectionDao.hasAccessToCollection (cid, uid);
   }

   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public boolean containsProduct (String uuid, Long pid)
   {
      if (uuid == null) return false;
      return collectionDao.contains (uuid, pid);
   }

   @Transactional (readOnly=true)
   public List<Collection> getCollectionsOfProduct(Product p)
   {
      return collectionDao.getCollectionsOfProduct(p.getId());
   }

   /**
    * Retrieves collections higher authorized collection of the given user in
    * function of the given criteria.
    *
    * @param criteria criteria contains filter and order of required collection.
    * @param user
    * @param skip     number of skipped valid results.
    * @param top      max of valid results.
    * @return a list of {@link Collection}
    */
   @Transactional(readOnly = true)
   public List<Collection> getHigherCollections (DetachedCriteria criteria,
         User user, int skip, int top)
   {
      if (criteria == null)
      {
         criteria = DetachedCriteria.forClass (Collection.class);
      }

      List<String> cids = new ArrayList<> ();
      if (cfgManager.isDataPublic () ||
            user.getRoles ().contains (Role.DATA_MANAGER))
      {
         Iterator<Collection> it = collectionDao.getAllCollections ();
         while (it.hasNext ())
         {
            cids.add (it.next ().getUUID ());
         }
      }
      else
      {
         List<String> collections =
               collectionDao.getAuthorizedCollections (user.getUUID ());
         Iterator<String> it = collections.iterator ();
         while (it.hasNext ())
         {
            cids.add (it.next ());
         }
      }

      if (!cids.isEmpty())
      {
         criteria.add(Restrictions.in("uuid", cids));
      }
      return collectionDao.listCriteria (criteria, skip, top);
   }

   @Transactional(readOnly = true)
   public int countHigherCollections (DetachedCriteria detached, User user)
   {
      return getHigherCollections (detached, user, 0, 0).size ();
   }

   /**
    * Counts of authorized collections for the given user.
    * @param user the user to filter collections.
    * @return number of collections than can see the user.
    */
   @Transactional(readOnly = true)
   public int countAuthorizedCollections (User user)
   {
      if (user == null)
      {
         throw new IllegalArgumentException ("User must not be null !");
      }

      if (cfgManager.isDataPublic () ||
            user.getRoles ().contains (Role.DATA_MANAGER))
      {
         return productDao.count ();
      }

      return 0;
   }

   /**
    * Retrieves all authorized collections for the given user.
    *
    * @param u the given user.
    * @return a set of authorized collections.
    */
   @Transactional(readOnly = true)
   public Set<Collection> getAuthorizedCollection (User u)
   {
      HashSet<Collection> collections = new HashSet<> ();

      for (String cid : collectionDao.getAuthorizedCollections (u.getUUID ()))
      {
         Collection collection = collectionDao.read (cid);
         if (collection != null)
         {
            collections.add (collection);
         }
      }

      return collections;
   }

   /**
    * Retrieves a collection by its name.
    * <p>Checks also if the given user is authorized to access it.</p>
    *
    * @param name collection name.
    * @param u    the current user.
    * @return the named collection or null.
    */
   @Transactional(readOnly = true)
   public Collection getAuthorizedCollectionByName (String name, User u)
   {
      if (name != null && u != null)
      {
         Collection collection = collectionDao.read (
               getCollectionUUIDByName (name));
         if (collection != null
               && (cfgManager.isDataPublic () ||
               collection.getAuthorizedUsers ().contains (u)))
         {
            return collection;
         }
      }
      return null;
   }

   /**
    * Retrieves all products contained in a collection.
    * @return a set of products.
    */
   @Transactional(readOnly = true)
   public Set<Product> getAllProductInCollection ()
   {
      Set<Product> products = new HashSet<> ();
      Iterator<Collection> it = collectionDao.getAllCollections ();
      while (it.hasNext ())
      {
         products.addAll (it.next ().getProducts ());
      }
      return products;
   }

   public Collection systemGetCollection (String id)
   {
      return collectionDao.read (id);
   }
}
