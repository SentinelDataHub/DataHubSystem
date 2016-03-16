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

import java.math.BigInteger;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableList;

import fr.gael.dhus.database.dao.interfaces.DaoUtils;
import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
/**
 * Product Data Access Object provides interface to Product Table into the
 * database.
 */
@Repository
public class ProductDao extends HibernateDao<Product, Long>
{
   private static Log logger = LogFactory.getLog (ProductDao.class);

   @Autowired
   private CollectionDao collectionDao;

   @Autowired
   private ProductCartDao productCartDao;

   @Autowired
   private UserDao userDao;

   @Autowired
   private EvictionDao evictionDao;
   
   private final static Integer MAX_PRODUCT_PAGE_SIZE=
      Integer.getInteger("max.product.page.size",100);
   
   /**
    * Checks if the passed number as a number of product is acceptable 
    * according to the current configuration
    * @param n the number of product to retrieve.
    * @throws UnsupportedOperationException if the passed number cannot be 
    *    handled.
    */
   static void checkProductNumber (int n)
   {
      if (n>MAX_PRODUCT_PAGE_SIZE)
      {
         throw new UnsupportedOperationException (
            "Product page size exceeds the authorized size (" + 
            MAX_PRODUCT_PAGE_SIZE + ")."); 
      }
   }
   /**
    * Returns the maximum number of product that a page of request can handled.
    * @return the max number of products.
    */
   public static int getMaxPageSize ()
   {
      return MAX_PRODUCT_PAGE_SIZE;
   }
   
   public Product getProductByPath (final URL path)
   {
      if (path == null)
         return null;

      Product p = (Product)DataAccessUtils.uniqueResult(getHibernateTemplate().
         find("from Product where path=? AND processed=true",path));
      
      return p;
   }
   
   /**
    * Retrieve a list of products to their ids. returned list is not controlled
    * with user rights nor processing completion.
    * @param ids the list of ids to retrieve
    * @return the list of products
    */
   @SuppressWarnings ("unchecked")
   public List<Product> read(List<Long>ids)
   {
      if ((ids == null)|| ids.isEmpty ()) return ImmutableList.of ();
      String facet = "";
      String logic_op="";
      for (Long id: ids)
      {
         facet += " " + logic_op + " p.id=" + id;
         logic_op = "or";
      }
      
      return (List<Product>) find (
         "from " + entityClass.getName () +
         " p WHERE " + facet);
   }

   /**
    * Does the product corresponding to the given url exist in the database ?
    * Processed or not.
    */
   public boolean exists (URL url)
   {
      if (url == null)
         return false;

      Product p = (Product)DataAccessUtils.uniqueResult(getHibernateTemplate().
         find("from Product where path=?", url));

      return p != null;
   }

   /**
    * Override Hibernate scroll to add the page size limitation.
    * @see {@link HibernateDao#scroll(String, int, int)}
    */
   @Override
   public List<Product> scroll(String clauses, int skip, int n)
   {
      checkProductNumber (n);
      if (n<0) n=ProductDao.getMaxPageSize();

      return super.scroll(clauses, skip, n);
   }

   public Iterator<Product> scrollFiltered (String filter, final Long parent_id,
         int skip)
   {
      StringBuilder sb = new StringBuilder ();
      if (parent_id != null)
      {
         // filters products of a collection
         sb.append ("SELECT p ");
         sb.append ("FROM Collection c LEFT OUTER JOIN c.products p ");
         sb.append ("WHERE c.id=").append (parent_id).append (" AND ")
               .append ("p.identifier LIKE '%").append (filter.toUpperCase ())
               .append ("%' AND p.processed=true");
      }
      else
      {
         // filters all products
         sb.append ("FROM ").append (entityClass.getName ()).append (" ");
         sb.append ("WHERE identifier LIKE '%").append (filter).append ("%' ");
         sb.append ("AND processed=true");
      }
      return new PagedIterator<> (this, sb.toString (), skip);
   }


   public int count (String filter, final Long parent_id, User user)
   {
      if (parent_id != null)
      {
         return DataAccessUtils.intResult (find (
            "select count(*) " +
            "from Collection c left outer join c.products p " +
            "where c.id=" + parent_id + " and upper(p.identifier) LIKE " +
            "upper('%" + filter + "%') and p.processed = true"));
      }
      return DataAccessUtils.intResult (find (
         "select count(*) FROM Product p " +
         "WHERE upper(p.identifier) LIKE upper('%" + filter + "%')  AND " +
         "p.processed=true "));
   }

    @Override
    public void deleteAll()
    {
       Iterator<Product> it = getAllProducts ();
       while (it.hasNext ())
       {
          it.next ();
          it.remove ();
       }
    }
   
   @Override
   public void delete (Product product)
   {
      Product p = read (product.getId ());
      List<Collection>cls = collectionDao.getCollectionsOfProduct (p.getId ());
      // Remove collection references
      // Must use rootUser to remove every reference of this product
      // (or maybe a new non usable user ?)
      User user = userDao.getRootUser();
      if (cls!=null)
      {
         for (Collection c: cls)
         {
            logger.info ("deconnect product from collection " + c.getName ());
            collectionDao.removeProduct (c.getId (), p.getId (), user);
         }
      }
      
      // Remove cart references
      productCartDao.deleteProductReferences(p);

      p.setAuthorizedUsers (new HashSet<User> ());
      p.getDownload ().getChecksums ().clear ();
      update (p);
      
      setIndexes (p.getId (), null);
      
      evictionDao.removeProduct (p);
     
      super.delete (p);
   }

   /**
    * Manage replacing existing lazy index into persistent structure.
    * @param product the product to modify.
    * @param indexes the index to set.
    */
   public void setIndexes(Product product, List<MetadataIndex>indexes)
   {
      product.setIndexes (indexes);
      update (product);
   }
   public void setIndexes(Long id, List<MetadataIndex>indexes)
   {
      setIndexes (read(id), indexes);
   }

   
   /**
    * Retrieve products ordered by it date of ingestion, updated. 
    * The list of product is returned prior to the passed date argument.
    * Currently processed and locked products are not returned.
    * @param max_date maximum date to retrieve products. More recent
    *                product will not be returned.
    * @return a scrollable list of the products.
    */
   public Iterator<Product> getProductsByIngestionDate (Date max_date)
   {
      SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
      String date = sdf.format (max_date);
      String query = "FROM " + entityClass.getName () + " " +
            "WHERE created < '" + date + "' AND processed=true AND " +
            "locked=false ORDER BY created ASC, updated ASC";
      return new PagedIterator<> (this, query);
   }
   
   /**
    * Retrieve the list of product ordered by lowest access.
    * The list of product is returned prior to the passed date argument.
    * Currently processed and locked products are not returned.
    * @return the ordered list of products.
    */
   public Iterator<Product>getProductsLowerAccess (Date max_date)
   {
      SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
      String date = sdf.format (max_date);
      String query = "FROM" + entityClass.getName () +
            "WHERE created < '" + date + "' AND processed=true AND " +
            "locked=false ORDER BY updated ASC, created ASC";
      return new PagedIterator<> (this, query);
   }

   public static String getPathFromProduct (Product product)
   {
      return getPathFromURL(product.getPath ());
   }
   public static String getPathFromURL (URL product)
   {
      return product.toString();
   }
   
   /**
    * THIS METHOD IS NOT SAFE: IT MUST BE REMOVED. 
    * TODO: manage access by page.
    * @param user_id
    * @return
    */
   @SuppressWarnings ("unchecked")
   public List<Long> getAuthorizedProducts (Long user_id)
   {
      return (List<Long>) find (
         "select id FROM " + entityClass.getName () +
         " p WHERE p.processed=true");
   }

   @SuppressWarnings ("unchecked")
   public Product getProductByDownloadableFilename (final String filename,
         final Collection collection)
   {
      List<Product>products=null; 
      if (collection == null || collectionDao.isRoot (collection))
      {
         products = (List<Product>)find(
            "from Product where download.path LIKE '%" + filename +
            "' AND processed = true");
      }
      else
      {
         products = (List<Product>)getHibernateTemplate ().find (
            "select p from Collection c left outer join c.products p " +
            "where c=? AND" +
            "      p.download.path LIKE ? AND" +
            "      processed=true", collection, "%"+filename +"%");
      }
         
      if ((products!=null) && (products.size ()>0))
         return products.iterator ().next ();
      return null;
   }
   
   @SuppressWarnings("unchecked")
   public Product getProductByOrigin (final String origin)
   {
      List<Product> products=find(
         "from Product where origin='" + DaoUtils.secureString (origin) +
         "' AND processed = true");
      try
      {
         return DataAccessUtils.uniqueResult (products);
      }
      catch (IncorrectResultSizeDataAccessException e)
      {
         // Case of more than one product found.
         logger.warn(
            "More than one entry of product origin found in database: " + 
            origin);
         return products.get(0);
      }
   }
   
   public Product getProductByUuid (String uuid, User user)
   {
      @SuppressWarnings ("unchecked")
      Product product = (Product) DataAccessUtils.uniqueResult (
            find ("from Product p where p.uuid='" + uuid +
                  "' AND p.processed=true"));
      return product;
   }
   

   /**
    * TODO: manage access by page.
    * @param user
    * @return
    */
   public List<Product> getNoCollectionProducts (User user)
   {
      ArrayList<Product> products = new ArrayList<> ();
      StringBuilder sqlBuilder = new StringBuilder ();
      sqlBuilder.append ("SELECT p.ID ");
      sqlBuilder.append ("FROM PRODUCTS p ");
      sqlBuilder.append ("LEFT OUTER JOIN COLLECTION_PRODUCT cp ")
                .append ("ON p.ID = cp.PRODUCTS_ID ");
      sqlBuilder.append ("WHERE cp.COLLECTIONS_ID IS NULL");
      final String sql = sqlBuilder.toString ();
      List<BigInteger> queryResult =
         getHibernateTemplate ().execute (
            new HibernateCallback<List<BigInteger>> ()
            {
               @Override
               @SuppressWarnings ("unchecked")
               public List<BigInteger> doInHibernate (Session session)
                  throws HibernateException, SQLException
               {
                  SQLQuery query = session.createSQLQuery (sql);
                  return query.list ();
               }
            });

      for (BigInteger pid : queryResult)
      {
         Product p = read (pid.longValue ());
         if (p == null)
         {
            throw new IllegalStateException (
               "Existing product is null ! product id = " + pid.longValue ());
         }
         products.add (p);
      }

      return products;
   }

   public List<Product> scrollUploadedProducts (final User user, final int skip,
      final int top)
   {
      checkProductNumber (top);
      return getHibernateTemplate ().execute (
            new HibernateCallback<List<Product>>()
      {
         @Override
         @SuppressWarnings ("unchecked")
         public List<Product> doInHibernate (Session session)
            throws HibernateException, SQLException
         {
            String hql = "SELECT p FROM Product p, User u" +
                     " WHERE p.owner = u and u.id = ? AND p.processed = true";
            Query query = session.createQuery (hql);
            query.setLong (0, user.getId ());
            query.setFirstResult (skip);
            query.setMaxResults (top);
            return (List<Product>) query.list ();
         }
      });
   }

   @SuppressWarnings ("unchecked")
   public List<Product> getUploadedProducts (final User user)
   {
      return (List<Product>) getHibernateTemplate ().find (
         "FROM Product WHERE owner = ? AND processed = true", user);
   }

   public User getOwnerOfProduct (final Product product)
   {
      return (User)DataAccessUtils.uniqueResult(getHibernateTemplate().find(
         "select p.owner from Product p where p=?", product));
   }

   public boolean isAuthorized (final long user_id, final long product_id)
   {
      if(userDao.read (user_id) == null || read (product_id) == null)
      {
         return false;
      }
      return true;
   }

   public Iterator<Product> getUnprocessedProducts ()
   {
      String query = "FROM " + entityClass.getName ()
            + " WHERE processed is false";
      return new PagedIterator<> (this, query);
   }

   public Iterator<Product> getAllProducts ()
   {
      String query = "FROM " + entityClass.getName ();
      return new PagedIterator<> (this, query);
   }
}
