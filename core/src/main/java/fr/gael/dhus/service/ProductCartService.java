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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import fr.gael.dhus.util.BlockingObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.gael.dhus.database.dao.ProductCartDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.ProductCart;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.exception.ProductNotExistingException;
import fr.gael.dhus.service.exception.UserNotExistingException;
import org.hibernate.Hibernate;

/**
 * Product Service provides connected clients with a set of method
 * to interact with it.
 */
@Service
public class ProductCartService extends WebService
{
   @Autowired
   private UserDao userDao;
   
   @Autowired
   private ProductCartDao productCartDao;
   
   @Autowired
   private ProductDao productDao;
   
   /**
    * Creates a new cart for the passed user. If the user has already a cart,
    * this cart will be returned. To force reset of cart use {@link ProductCartService#deleteCartOfUser(Long)}.
    * @param uuid the user to create a new cart.
    * @return the created cart.
    * @throws UserNotExistingException when passed user is unknown.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public ProductCart createCartOfUser (String uuid)
   {
      ProductCart cart = getCartOfUser(uuid);
      if (cart == null)
      {
         User user = getUser (uuid);
         cart = new ProductCart();
         cart.setUser (user);
      }
      return productCartDao.create (cart);
   }
   
   /**
    * Removes a cart attached to a user.
    * @param uuid the user to remove the cart.
    * @throws UserNotExistingException when passed user is unknown.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void deleteCartOfUser (String uuid)
   {
      ProductCart cart = getCartOfUser(uuid);
      if (cart!=null) productCartDao.delete(cart);
   }

   /**
    * Get the cart of the related user. If the user has no cart configured, null
    * is returned.
    * @param uuid the related user to retrieve the cart.
    * @return the cart
    * @throws UserNotExistingException when passed user is unknown.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public ProductCart getCartOfUser(String uuid) throws UserNotExistingException
   {
      User user = getUser (uuid);
      ProductCart pc = productCartDao.getCartOfUser(user);
      if (pc != null)
      {
         Hibernate.initialize(pc.getProducts());
      }
      return pc;
   }
   
   /**
    * Add a product into a user's cart. Is user has no cart, it will be created.
    * @param uuid id of the expected user
    * @param p_id id of the product to add.
    * @throws UserNotExistingException when passed user is unknown.
    * @throws ProductNotExistingException when the passed to add does not exists.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (propagation=Propagation.REQUIRED)
   public void addProductToCart(String uuid, Long p_id)
      throws UserNotExistingException, ProductNotExistingException
   {
      Product product = productDao.read (p_id);
      if (product == null)
      {
         throw new ProductNotExistingException();
      }

      String key = "{" + uuid.toString () + "-" + p_id.toString () + "}";
      synchronized (BlockingObject.getBlockingObject (key))
      {
         ProductCart cart = getCartOfUser (uuid);
         if (cart == null) cart = createCartOfUser (uuid);
         if (cart.getProducts () == null)
         {
            cart.setProducts (new HashSet<Product> ());
         }
         cart.getProducts ().add (product);
         productCartDao.update (cart);
      }
   }
   
   /**
    * remove the specified product from cart.
    * @param uuid user to remove the product.
    * @param p_id product to be removed.
    * @throws UserNotExistingException when passed user is unknown.
    * @throws ProductNotExistingException when the passed product to add does not exists.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (propagation=Propagation.REQUIRED)
   public void removeProductFromCart(String uuid, Long p_id) throws
         UserNotExistingException,
      ProductNotExistingException
   {
      Product product = productDao.read (p_id);
      if (product == null)
      {
         throw new ProductNotExistingException();
      }
      ProductCart cart = getCartOfUser (uuid);
      if ((cart==null) || (cart.getProducts ()==null))
      {
         return;
      }
      Iterator<Product> iterator = cart.getProducts ().iterator ();
      while (iterator.hasNext ())
      {
         if (iterator.next ().equals (product))
         {
            iterator.remove ();
         }
      }
      productCartDao.update (cart);
   }

   /**
    * Retrieve the list of product ids from the cart of the passed user.
    * @param uuid the user to retrieve the products.
    * @return a list of product identifiers.
    * @throws UserNotExistingException when passed user is unknown.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (propagation=Propagation.REQUIRED)
   public List<Long> getProductsIdOfCart(String uuid)
      throws UserNotExistingException
   {
      return productCartDao.getProductsIdOfCart (getUser (uuid));
   }

   /**
    * Retrieve the product list from a product cart of a user.
    * @param uuid the user to retrieve the products.
    * @param skip product number to skip from the list.
    * @param top number of product to keep.
    * @return the list of product within the passed window.
    * @throws UserNotExistingException when passed user is unknown.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Product> getProductsOfCart(String uuid, int skip, int top)
      throws UserNotExistingException
   {
      return productCartDao.scrollCartOfUser (getUser (uuid), skip, top);
   }   
   
   /**
    * Count the number of products from a user's cart.
    * @param uuid the user to retrieve the cart.
    * @return the number of products in the cart.
    * @throws UserNotExistingException when passed user is unknown.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int countProductsInCart (String uuid)
      throws UserNotExistingException
   {
      ProductCart cart = getCartOfUser (uuid);
      if (cart == null) return 0;
      return cart.getProducts () == null ? 0 : cart.getProducts ().size ();
   }
   
   /**
    * Reports if the passed user has product in its cart. 
    * @param uuid the user to retrieve the cart.
    * @return false is cart is empty, true otherwise.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public boolean hasProducts (String uuid)
   {
      return countProductsInCart (uuid) != 0 ;
   }
   
   /**
    * 
    * @param uuid
    * @throws UserNotExistingException when passed user is unknown.
    */
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   @Transactional (propagation=Propagation.REQUIRED)
   public void clearCart (String uuid) throws UserNotExistingException
   {
      ProductCart cart = getCartOfUser (uuid);
      if ((cart!=null) && (cart.getProducts()!=null)) 
      {
         cart.getProducts ().clear ();
         productCartDao.update (cart);
      }
   }

   /**
    * 
    * @param uuid
    * @return
    * @throws UserNotExistingException
    */
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   private User getUser(String uuid) throws UserNotExistingException
   {
      User user = userDao.read (uuid);
      if (user == null)
         throw new UserNotExistingException();
      return user;

   }
}
