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

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.ProductCart;
import fr.gael.dhus.database.object.User;

@Repository
public class ProductCartDao extends HibernateDao<ProductCart, Long>
{   
   public ProductCart getCartOfUser(final User user)
   {
      class ReturnValue
      {
         ProductCart value;
      }
      final ReturnValue rv = new ReturnValue ();
      getHibernateTemplate().execute  (
         new HibernateCallback<Void>()
         {
            public Void doInHibernate(Session session) 
               throws HibernateException, SQLException
            {
               rv.value = (ProductCart)session.createQuery (
                  "from ProductCart where user_id='" + user.getId () + "'").
                  uniqueResult ();
               return null;
            }
         });
      return rv.value;
   }   
   
   public void deleteCartOfUser(User user)
   {
      ProductCart cart = getCartOfUser (user);
      if (cart!=null) delete(cart);
   }
   
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
   
   @SuppressWarnings ("unchecked")
   public List<Product> scrollCartOfUser (final User user, final int skip,
         final int top)
   {
      if (user == null)
         return Collections.emptyList ();

      return getHibernateTemplate ().execute (
            new HibernateCallback<List<Product>> ()
            {
               @Override
               public List<Product> doInHibernate (Session session)
                     throws HibernateException, SQLException
               {
                  String hql =
                        "SELECT p " +
                              "FROM ProductCart pc LEFT OUTER JOIN pc.products p " +
                              "WHERE pc.user = ?";
                  Query query = session.createQuery (hql);
                  query.setEntity (0, user);

                  if (skip > 0)
                     query.setFirstResult (skip);
                  if (top > 0)
                     query.setMaxResults (top);

                  return (List<Product>) query.list ();
               }
            });
   }
   
   @SuppressWarnings ("unchecked")
   public List<Long> getProductsIdOfCart (User user)
   {
      long start = new Date ().getTime ();
      List<Long>ret = (List<Long>)find (
            "SELECT p.id " +
            "   FROM fr.gael.dhus.database.object.ProductCart pc " +
            "   LEFT OUTER JOIN pc.products p " +
            "   LEFT OUTER JOIN pc.user u" +
            "   WHERE u.id = " + user.getId ());
      long end = new Date ().getTime ();
      logger.info ("Query getProductsIdOfCart spent " + (end-start) + "ms");
      return ret;
   }
}
