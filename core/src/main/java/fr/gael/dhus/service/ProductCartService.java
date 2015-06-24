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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.ProductCartDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.ProductCart;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.exception.ProductNotExistingException;
import fr.gael.dhus.service.exception.UserNotExistingException;

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

   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   public ProductCart getCartOfUser(Long uId) throws UserNotExistingException
   {
      User user = userDao.read (uId);
      if (user == null)
      {
         throw new UserNotExistingException();
      }
      ProductCart cart = productCartDao.getCartOfUser(user);
      if (cart == null)
      {   
         cart = new ProductCart();
         cart.setUser (user);
         productCartDao.create (cart);
      }
      return cart;
   }
   
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   public void addProductToCart(Long uId, Long pId) throws UserNotExistingException,
      ProductNotExistingException
   {
      Product product = productDao.read (pId);
      if (product == null)
      {
         throw new ProductNotExistingException();
      }
      ProductCart cart = getCartOfUser (uId);
      if (cart.getProducts () == null)
      {
         cart.setProducts (new HashSet<Product> ());
      }
      cart.getProducts ().add (product);
      productCartDao.update (cart);
   }
   
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   public void removeProductFromCart(Long uId, Long pId) throws UserNotExistingException,
      ProductNotExistingException
   {
      Product product = productDao.read (pId);
      if (product == null)
      {
         throw new ProductNotExistingException();
      }
      ProductCart cart = getCartOfUser (uId);
      if (cart.getProducts () == null)
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

   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   public List<Long> getProductsIdOfCart(Long uId) throws UserNotExistingException,
      ProductNotExistingException
   {
      User user = userDao.read (uId);
      if (user == null)
      {
         throw new UserNotExistingException();
      }
      return productCartDao.getProductsIdOfCart (user);
   }
   
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   public List<Product> getProductsOfCart(Long uId, int skip, int top) 
            throws UserNotExistingException, ProductNotExistingException
   {
      User user = userDao.read (uId);
      if (user == null)
      {
         throw new UserNotExistingException();
      }
      return productCartDao.scrollCartOfUser (user, skip, top);
   }   
      
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   public int countProductsInCart (Long uId)
   {
      ProductCart cart = getCartOfUser (uId);
      return cart.getProducts () == null ? 0 : cart.getProducts ().size ();
   }
   
   @PreAuthorize ("hasRole('ROLE_DOWNLOAD')")
   public void clearCart (Long uId)
   {
      ProductCart cart = getCartOfUser (uId);
      cart.getProducts ().clear ();
      productCartDao.update (cart);
   }
}
