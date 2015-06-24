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
import java.util.Set;

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
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.datastore.processing.Processing;
import fr.gael.dhus.system.config.ConfigurationManager;

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
   private ConfigurationManager cfgManager;

   @Autowired
   private UserDao userDao;
   
   @Autowired
   EvictionDao evictionDao;
   
   public Product getProductByPath (final URL path)
   {
      if (path == null)
         return null;

      long start = new Date ().getTime ();
      class ReturnValue
      {
         Product value;
      }
      final ReturnValue rv = new ReturnValue ();
      getHibernateTemplate().execute  (
         new HibernateCallback<Void>()
         {
            public Void doInHibernate(Session session) 
               throws HibernateException, SQLException
            {
               rv.value = (Product)session.createQuery (
                  "from Product where path='" + path.toString () + "' AND " +
                     " processed = true ").uniqueResult ();
               return null;
            }
         });
      long end = new Date ().getTime ();
      String uuid = (rv.value==null?"not found":rv.value.getUuid ());
      logger.info (" Reading product '" + uuid +"' in " + (end-start) + "ms");
      return rv.value;
   }
   
   /**
    * Retrieve a list of products to their ids. returned list is not controlled
    * with user rights nor processing completion.
    * @param ids the list of ids to retrieve
    * @return the list of products
    */
   @SuppressWarnings ("unchecked")
   public List<Product>read(List<Long>ids)
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
   
   public boolean exists (URL url)
   {
      Product p = getProductByPath (url);
      return p!=null;
   }

   public List<Product> scrollFiltered (String filter, final Long parentId,
      User user, int skip, int top)
   {
      // TODO move in CollectionDao
//    String userString = "";
//    // Bypass for Data Right Managers. They can see all products and collections.
//    if (!publicData.dataAccessPublic() && 
//        (user != null) && !user.getRoles ().contains (Role.DATA_MANAGER))
//    {
//       userString = "("+user.getId()+" in elements(p.authorizedUsers)  OR "+
//       publicData.getUser ().getId ()+" in elements(p.authorizedUsers)) and ";
//    }
//    if (parentId != null)
//    {
//       return scroll("select p " +
//                "  from Collection c left outer join c.products p" +
//                " where "+userString+" c.id = "+parentId);
//    }      
//    return scroll ("FROM " + entityClass.getName () +
//       " p WHERE "+userString+" upper(p.identifier) LIKE upper('%" + filter + "%') AND " +
//       "   p.processed=true " +
//       " ORDER BY identifier");
    if (parentId != null)
    {
       Collection collection = collectionDao.read (parentId);
       String pattern = "p.identifier LIKE '%" + filter.toUpperCase () + "%'";
       return collectionDao.getAuthorizedProducts (user, collection, pattern,
          null, skip, top);
    }
      
      String hql =
         "WHERE identifier LIKE '%" + filter.toUpperCase () +
            "%' AND processed = true";
      if ( !cfgManager.isDataPublic () && user != null &&
         !user.getRoles ().contains (Role.DATA_MANAGER))
      {
         hql = hql + " AND " + user.getId () + "in elements(authorizedUsers)";
      }
      return scroll (hql, skip, top);
   }


   public int count (String filter, final Long parentId, User user)
   {
      String userString = "";
      // Bypass for Data Right Managers. They can see all products and collections.
      if (!cfgManager.isDataPublic () &&
          user != null && !user.getRoles ().contains (Role.DATA_MANAGER))
      {
         userString = "("+user.getId()+" in elements(p.authorizedUsers) OR "+
         userDao.getPublicData ().getId ()+" in elements(p.authorizedUsers)) and ";
      }
      if (parentId != null)
      {
         class ReturnValue
         {
            Long value;
         }         
         final String userRestriction = userString;
         final ReturnValue rv = new ReturnValue ();
         getHibernateTemplate().execute  (
            new HibernateCallback<Void>()
            {
               public Void doInHibernate(Session session) 
                  throws HibernateException, SQLException
               {
                  rv.value = (Long) (session.createQuery (
                     "select count(*) " +
                     "  from Collection c left outer join c.products p" +
                     " where "+userRestriction+" c.id = :cid").
                     setParameter ("cid", parentId).uniqueResult ());
                  return null;
               }
            });
         return rv.value.intValue ();
      }      
      return DataAccessUtils.intResult (find (
         "select count(*) FROM " + entityClass.getName () +
            " p WHERE "+userString+" upper(p.identifier) LIKE upper('%" + filter + "%')  AND " +
            "      p.processed=true "));
   }

    @Override
    public void deleteAll() {
        int top = DaoUtils.DEFAULT_ELEMENTS_PER_PAGE;
        List<Product> deletableProduct = scroll(null, 0, top);

        while (deletableProduct.size() == top) {
            for (Product product : deletableProduct)
            {
               delete (product);
            }
            deletableProduct = scroll(null, 0, top);
        }
        for (Product product : deletableProduct)
            delete(product);
    }
   
   @Override
   public void delete (Product product)
   {
      Product p = read (product.getId ());
      List<Collection>cls = collectionDao.getCollectionsOfProduct (p.getId ());
      // Remove collection references
      // Must use rootUser to remove every reference of this product (or maybe a new non usable user ?)
      User user = userDao.getRootUser();
      if (cls!=null)
      {
         for (Collection c: cls)
         {
            collectionDao.removeProduct (c.getId (), p.getId (), user);
         }
      }
      
      // Remove cart references
      productCartDao.deleteProductReferences(p);
      p.setAuthorizedUsers (new HashSet<User> ());
      p.getIndexes ().clear ();
      p.getDownload ().getChecksums ().clear ();
      update (p);
      evictionDao.removeProduct (p);
     
      super.delete (p);
   }
   
   public void updateIndexes (Product p, List<MetadataIndex>indexes)
   {
      p.getIndexes ().retainAll (indexes);
      Set<MetadataIndex> reallyNew = new HashSet<>(indexes);
      reallyNew.removeAll(p.getIndexes ());
      p.getIndexes ().addAll (reallyNew);
      update (p);
   }
   
   /**
    * Retrieve products ordered by it date of ingestion, updated. 
    * The list of product is returned prior to the passed date argument.
    * Currently processed and locked products are not returned.
    * @param maxDate maximum date to retrieve products. More recent product will not be returned.
    * @return a scrollable list of the products.
    */
   public List<Product> getProductsByIngestionDate (Date maxDate, int skip,
      int top)
   {
      SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
      String date = sdf.format (maxDate);
      return scroll("WHERE created < '" + date + "' AND processed = true AND " +
         "locked = false ORDER BY created ASC, updated ASC", skip, top);
   }
   
   /**
    * Retrieve the list of product ordered by lowest access.
    * The list of product is returned prior to the passed date argument.
    * Currently processed and locked products are not returned.
    * @return the ordered list of products.
    */
   public List<Product>getProductsLowerAccess (Date maxDate, int skip, int top)
   {
      SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
      String date = sdf.format (maxDate);
         return scroll("WHERE created < '" + date + "' AND processed=true AND " +
            "locked=false ORDER BY updated ASC, created ASC", skip, top);
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
    * Do process all products in database that their ingestion is not finished.
    * If provided parameter is null, default processing consists in removing 
    * the data from database. Otherwise, the provided processing is launched.
    * @param proc the processing to execute. if null, remove processing will 
    *             be performed. 
    * @return the list of products reprocessed.
    */
   public void processUnprocessed (Processing<Product> proc)
   {
      long start = new Date ().getTime ();
      
      int top = DaoUtils.DEFAULT_ELEMENTS_PER_PAGE;
      String hql = "WHERE processed = false";
      
      int removed;
      
      if (proc == null)
      {
         do
         {
            removed = 0;
            Iterator <Product>products = scroll (hql, 0, top).iterator ();
            while (products.hasNext ())
            {
               delete (read (products.next ().getId ()));
               removed++;
            }
         }
         while (removed == top);
         
         logger.debug ("Cleanup incomplete processed products in " + 
            (new Date().getTime ()-start) + "ms");
      }
      else
      {
         int product_number;
         int skip=0;
         do
         {
            product_number=0;
            removed = 0;
            
            Iterator <Product>products = scroll (hql, skip, top).iterator ();
            while (products.hasNext ())
            {
               Product product = read (products.next ().getId ());
               product_number++;
               // Do reporcess only already transfered products
               if (product.getPath ().toString ().equals(product.getOrigin ()))
               {
                  delete (product);
                  removed++;
               }
               else
                  proc.run (product);
            }
            skip = (skip + top)-removed;
         }
         while (product_number == top);
      }
   }
   
   @SuppressWarnings ("unchecked")
   public List<Long> getAuthorizedProducts (Long userId)
   {  
      String restiction_query="";
      
      if (!cfgManager.isDataPublic ())
      {
         User user = userDao.read (userId);
         if (user != null && !user.getRoles ().contains (Role.DATA_MANAGER))
         {
            restiction_query=" AND (" + 
               userId + " in elements(p.authorizedUsers) OR " +
               userDao.getPublicData ().getId()+" in elements(p.authorizedUsers))";
         }
      }
      
      return (List<Long>) find (
         "select id FROM " + entityClass.getName () +
         " p WHERE p.processed=true" + restiction_query);
   }

   public Product getProductByDownloadableFilename (final String filename,
         final Collection collection)
   {
      class ReturnValue
      {
         Product value;
      }
      final ReturnValue rv = new ReturnValue ();
      getHibernateTemplate().execute  (
         new HibernateCallback<Void>()
         {
            @SuppressWarnings ("unchecked")
            public Void doInHibernate(Session session) 
               throws HibernateException, SQLException
            {
               // root collection products are in fact "no collection" products
               if (collection == null || collectionDao.isRoot (collection))
               {
                  List<Product> res = (List<Product>) session.createQuery (
                     "from Product where download.path LIKE '%" + filename +
                     "' AND processed = true").
                    list ();
                  if (res != null && res.size () > 0)
                  {
                     rv.value = res.get (0);
                  }
               }
               else
               {
                  List<Product> res = (List<Product>) session.createQuery (
                     "select p from Collection c left outer join c.products p" +
                     "   where c.id=" + collection.getId() + " and " +
                     "   p.download.path LIKE '%" + filename + "' " +
                        "AND p.processed = true").list ();
                  if (res != null && res.size () > 0)
                  {
                     rv.value = res.get (0);
                  } 
               }
               
               return null;
            }
         });
      return rv.value;
   }
   
   @SuppressWarnings("unchecked")
   public Product getProductByOrigin (final String origin)
   {
      List<Product> products=find(
         "from Product where origin='" + origin + "' AND processed = true");
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
      String user_string = "";
      // Bypass for Data Right Managers. They can see all products and collections.
      if (!cfgManager.isDataPublic () &&
          user != null && !user.getRoles ().contains (Role.DATA_MANAGER))
      {
         user_string = " AND ("+user.getId()+" in elements(p.authorizedUsers) OR "+
         userDao.getPublicData ().getId ()+" in elements(p.authorizedUsers))";
      }
      
      
      @SuppressWarnings ("unchecked")
      Product product = (Product)DataAccessUtils.uniqueResult(
         find("from Product p where p.uuid='" + uuid + "' AND p.processed=true " +
           user_string));
      
      return product;
   }
   

   public List<Product> getNoCollectionProducts (User user)
   {
      ArrayList<Product> products = new ArrayList<> ();
      if (user == null)
      {
         return products;
      }
      final Long uid = user.getId ();
      StringBuilder sqlBuilder = new StringBuilder ();
      sqlBuilder.append ("SELECT DISTINCT pu.PRODUCTS_ID ");
      sqlBuilder.append ("FROM PRODUCTS p LEFT OUTER JOIN PRODUCT_USER_AUTH pu ");
      sqlBuilder.append ("ON p.ID = pu.PRODUCTS_ID ");
      sqlBuilder.append ("WHERE p.PROCESSED = TRUE AND ");
      sqlBuilder.append ("(pu.USERS_ID = ").append (uid).append (" ");
      sqlBuilder.append ("OR pu.USERS_ID = ").append (userDao.getPublicData ().getId ()).append (
            " )");
      sqlBuilder.append ("AND pu.PRODUCTS_ID not in");
      sqlBuilder
         .append ("(SELECT cp.PRODUCTS_ID FROM COLLECTION_PRODUCT cp WHERE cp.COLLECTIONS_ID in ");
      sqlBuilder
         .append ("(SELECT cu.COLLECTIONS_ID FROM COLLECTION_USER_AUTH cu WHERE (cu.USERS_ID = ");
      sqlBuilder.append (uid).append (" ");
      sqlBuilder.append ("OR cu.USERS_ID = ").append (userDao.getPublicData ().getId ()).append (" )))");

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
      return getHibernateTemplate ().execute (new HibernateCallback<List<Product>>()
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
      return getHibernateTemplate ().execute (
         new HibernateCallback<List<Product>>()
      {
            @Override
            public List<Product> doInHibernate (Session session)
               throws HibernateException, SQLException
            {
               String hql = "FROM Product WHERE owner = ? AND processed = true";
               Query query = session.createQuery (hql);
               query.setEntity (0, user);
               return (List<Product>) query.list ();
            }
      });
   }
   
   public List<User> getAuthorizedUsers (final Product product)
   {
      return getHibernateTemplate ().execute (
         new HibernateCallback<List<User>> ()
         {
            @Override
            @SuppressWarnings ("unchecked")
            public List<User> doInHibernate (Session session)
               throws HibernateException, SQLException
            {
               String hql =
                  "SELECT users "
                     + "FROM Product p LEFT OUTER JOIN p.authorizedUsers users "
                     + "WHERE p.id = ?";
               Query query = session.createQuery (hql).setReadOnly (true);
               query.setLong (0, product.getId ());
               return (List<User>) query.list ();
            }
         });
   }

   public User getOwnerOfProduct (final Product product)
   {
      return getHibernateTemplate ().execute (new HibernateCallback<User>()
      {
         @Override
         public User doInHibernate (Session session) throws HibernateException,
            SQLException
         {
            String hql = "SELECT owner FROM Product WHERE id = ?";
            Query query = session.createQuery (hql).setReadOnly (true);
            query.setLong (0, product.getId ());
            return (User) query.uniqueResult ();
         }
      });
   }

   public boolean isAuthorized (final long userId, final long productId)
   {
      return getHibernateTemplate ().execute (new HibernateCallback<Boolean>()
      {
         @Override
         public Boolean doInHibernate (Session session)
            throws HibernateException, SQLException
         {
            SQLQuery query = session.createSQLQuery ("SELECT count(*) " +
               "FROM PRODUCT_USER_AUTH " +
               "WHERE (USERS_ID = ? OR USERS_ID = ?) AND PRODUCTS_ID = ?");
            query.setLong (0, userId);
            query.setLong (1, userDao.getPublicData ().getId ());
            query.setLong (2, productId);
            query.setReadOnly (true);
            return ((BigInteger) query.uniqueResult ()).intValue () == 1;
         }
      });
   }
}
