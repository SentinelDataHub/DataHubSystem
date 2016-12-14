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
package fr.gael.dhus.database.dao;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.ProductCart;
import fr.gael.dhus.database.object.User;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Repository
public class ProductCartDao extends HibernateDao<ProductCart, String>
{
   /**
    * Returns the first result of a Collection.
    * @param results collection of elements
    * @return the first element if the list, null otherwise.
    */
   public static <T> T firstResult(Collection<T> results)
   {
      int size = (results != null ? results.size() : 0);
      if (size == 0)
      {
         return null;
      }
      return results.iterator().next();
   }

   /**
    * Retrieve the cart of a specified user. Is the user has more than one cart
    * the first one will be returned.
    * @param user the user to retrieve the cart
    * @return the cart of the user, null if user has no cart.
    */
   public ProductCart getCartOfUser(final User user)
   {
      return (ProductCart)firstResult(getCartsOfUser(user));
   }
   
   /**
    * Returns the list of cart related to a user.
    * @param user the user to retrieve the carts.
    * @return the list of carts.
    */
   @SuppressWarnings ("unchecked")
   public List<ProductCart> getCartsOfUser(final User user)
   {
      return (List<ProductCart>)getHibernateTemplate().find(
         "from ProductCart where user=?", user);
   }

   /**
    * Deletes a cart entry from its owner user. If more than one cart is
    * configured for one user, all the carts will be removed.
    * @param user the owner user.
    */
   public void deleteCartOfUser(User user)
   {
      List<ProductCart> carts = getCartsOfUser (user);
      if (carts!=null)
      {
         for (ProductCart cart: carts)
            delete(cart);
      }
   }

   /**
    * Removes all the references of one product from all the existing carts.
    * @param product the product to be removed from carts.
    */
   public void deleteProductReferences(final Product product)
   {
      getHibernateTemplate().execute  (
         new HibernateCallback<Void>()
         {
            public Void doInHibernate(Session session) 
               throws HibernateException, SQLException
            {
               session.createSQLQuery(
                  "DELETE FROM CART_PRODUCTS p " +
                  " WHERE p.PRODUCT_ID = :pid").
                  setParameter ("pid", product.getId()).executeUpdate ();
               return null;
            }
         });
   }

   /**
    * Computes a scrollable list of all products contained into a cart owned by
    * specified user. If the user has more than one cart, only content of
    * the first one will be returned.
    * If the cart is empty or does not exists, or skip/top parameters are
    * outside the limit of the list, an empty list is returned.
    * @param user the user owner of the cart to be retrieved.
    * @param skip number of products to skip (-1 means 0).
    * @param top number of product to kept (-1 means all the entries of the list).
    * @return the list of product according to the passed limitation.
    * @see {@link ProductCartDao#getCartOfUser(User)}
    */
   public List<Product> scrollCartOfUser (final User user, final int skip,
         final int top)
   {
      if (user == null) return Collections.emptyList ();
      ProductCart cart = getCartOfUser(user);
      if (cart == null) return Collections.emptyList ();
      
      return setToLimitedList(cart.getProducts(), skip, top);
   }

   // Computes a limited list (skip, n) from a full list.
   private List<Product> setToLimitedList (Set<Product> input, int skip, int n)
   {
      List <Product>lst = Lists.newArrayList();

      // Special use case -1, -1 :(
      if (skip<0) skip=0;
      if (n<1) n=ProductDao.getMaxPageSize();

      Iterator<Product>it = input.iterator();
      while (skip>0)
      {
         if (it.hasNext()) it.next();
         else return lst;
         skip--;
      }

      while (n>0)
      {
         Product p=null;
         if (it.hasNext()) p=it.next();
         else return lst;
         lst.add(p);
         n--;
      }
      return lst;
   }

   /**
    * Retrieve the entire list of products ids contained in the user cart.
    * @param user to retrieve the cart.
    * @return a list of ids
    * @see ProductCartDao#scrollCartOfUser(User, int, int)
    */
   public List<Long> getProductsIdOfCart (User user)
   {
      long start = new Date ().getTime ();
      List<Long>ret = toId(scrollCartOfUser (user, 0, -1));
      long end = new Date ().getTime ();
      logger.info("Query getProductsIdOfCart spent " + (end-start) + "ms");
      return ret;
   }

   // Convert a list of products to a list of these products ids....
   private List<Long>toId (List<Product> products)
   {
      List<Long>lst = Lists.newArrayList();
      if (products !=null)
      {
         for (Product product:products)
         {
            lst.add(product.getId());
         }
      }
      return lst;
   }
}
