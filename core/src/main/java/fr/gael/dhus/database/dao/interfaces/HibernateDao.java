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
package fr.gael.dhus.database.dao.interfaces;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import javax.swing.event.EventListenerList;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Hibernate DAO Implementation, containing minimal CRUD operations.
 * 
 * @param <T> Object concerned by this DAO
 * @param <PK> Primary Key of this Object.
 */
public class HibernateDao<T, PK extends Serializable> extends
   HibernateDaoSupport implements GenericDao<T, PK>, Pageable<T>
{
   protected Class<T> entityClass;
   private final EventListenerList listeners = new EventListenerList ();

   @SuppressWarnings ("unchecked")
   public HibernateDao ()
   {
      ParameterizedType genericSuperclass =
         (ParameterizedType) getClass ().getGenericSuperclass ();
      this.entityClass =
         (Class<T>) genericSuperclass.getActualTypeArguments ()[0];
   }

   @SuppressWarnings ("unchecked")
   @Override
   public T create (T t)
   {
      T sent = t;
      long start = new Date ().getTime ();
      PK id = (PK) getHibernateTemplate ().save (t);
      t = getHibernateTemplate ().get ((Class<T>)t.getClass (), id);
      long end = new Date ().getTime ();
      logger.info("Create/save " + entityClass.getSimpleName () + "("+ id 
         +") spent " + (end-start) + "ms" );

      fireCreatedEvent (new DaoEvent<T> (sent));
      return t;
   }

   @Override
   public T read (PK id)
   {
      long start = new Date ().getTime ();
      T ret = getHibernateTemplate ().get (entityClass, id);
      long end = new Date ().getTime ();
      logger.debug("Read " + entityClass.getSimpleName ()
            + "(" + id + ") spent " + (end-start) + "ms" );
      return ret;
   }

   @Override
   public void update (T t)
   {
      long start = new Date ().getTime ();
      getHibernateTemplate ().update (t);
      long end = new Date ().getTime ();
      logger.info("Update " + entityClass.getSimpleName () + " spent "
            + (end-start) + "ms" );

      fireUpdatedEvent (new DaoEvent<T> (t));
   }

   /**
    * Merge the provided object into the current session.
    * This could be useful when one session handle the same object twice.
    * @param t the entity to merge.
    */
   public void merge (T t)
   {
      long start = new Date ().getTime ();
      getHibernateTemplate().getSessionFactory().getCurrentSession().merge(t);
      long end = new Date ().getTime ();
      logger.info("Merge " + entityClass.getSimpleName () + " spent "
            + (end-start) + "ms" );
   }

   @Override
   public void delete (T t)
   {
      long start = new Date ().getTime ();
      getHibernateTemplate ().delete (t);
      long end = new Date ().getTime ();
      logger.info("Delete " + entityClass.getSimpleName () + " spent "
            + (end-start) + "ms" );

      fireDeletedEvent (new DaoEvent<T> (t));
   }

   /**
    * Remove all the element from the db og this <T> instance.
    */
   public void deleteAll ()
   {
      for (T entity : readAll ())
         delete (entity);

//      String hql = "DELETE FROM " + entityClass.getName ();
//      getHibernateTemplate ().bulkUpdate (hql);
   }

   /**
    * <p>Returns a List of <b>T</b> entities, where HQL clauses can be
    * specified.</p>
    * 
    * Note: This method is useful in read only. It can be use to delete or 
    * create <b>T</b> entities, but caution with <code>top</code> and 
    * <code>skip</code> arguments.
    * 
    * @param clauses query clauses (WHERE, ORDER BY, GROUP BY), if null no
    * clauses are apply.
    * @param skip number of entities to skip.
    * @param n  number of entities max returned.
    * @return a list of <b>T</b> entities.
    */
   @SuppressWarnings ("unchecked")
   public List<T> scroll (final String clauses, final int skip, final int n)
   {
      StringBuilder hql = new StringBuilder ();
      hql.append ("FROM ").append (entityClass.getName ());
      if (clauses != null)
         hql.append (" ").append (clauses);

      Session session;
      boolean newSession = false;
      try
      {
         session = getSessionFactory ().getCurrentSession ();
      }
      catch (HibernateException e)
      {
         session = getSessionFactory ().openSession ();
         newSession = true;
      }

      Query query = session.createQuery (hql.toString ());
      if (skip > 0) query.setFirstResult (skip);
      if (n > 0) 
      {
         query.setMaxResults (n);
         query.setFetchSize (n);
      }
      
      logger.info("Execution of HQL: " + hql.toString ());
      long start = System.currentTimeMillis ();

      List<T> result = (List<T>) query.list ();
      logger.info("HQL executed in " + 
         (System.currentTimeMillis() -start) + "ms.");

      if (newSession)
      {
         session.disconnect ();
      }

      return result;
   }

   /**
    * Retrieve the first element of the results.
    * 
    * @param query_string
    * @return
    */
   public T first (String query_string)
   {
      @SuppressWarnings ("unchecked")
      List<T> result = (List<T>) getHibernateTemplate ().find (query_string);
      return (result.isEmpty ()) ? null : result.get (0);
   }

   /**
    * Returns all Objects in a List.
    * 
    * @return List containing all Objects.
    */
   @SuppressWarnings ("unchecked")
   @Override
   public List<T> readAll ()
   {
      return find ("FROM " + entityClass.getName ());
   }

   /**
    * Count objects in table.
    * 
    * @return Objects count.
    */
   public int count ()
   {
      int count = 0;
      @SuppressWarnings ("unchecked")
      List<Long>counts = find (
         "select count(*) FROM " + entityClass.getName ());
      if (counts != null) for (Long c:counts) count +=c;
      return count;
   }
   
   @SuppressWarnings ("rawtypes")
   public List find(String query_string) throws DataAccessException
   {
      long start = new Date ().getTime ();
      List ret= getHibernateTemplate ().find (query_string);
      
      long end = new Date ().getTime ();
      logger.debug("Query \"" + 
         query_string.replaceAll("(\\r|\\n)", " ").trim () +
         "\" spent " + (end-start) + "ms" );
      return ret;
   }

   public void addListener (DaoListener<T> listener)
   {
      listeners.add (DaoListener.class, listener);
   }

   public void removeListener (DaoListener<T> listener)
   {
      listeners.remove (DaoListener.class, listener);
   }

   @SuppressWarnings ("unchecked")
   public DaoListener<T>[] getListeners ()
   {
      return listeners.getListeners (DaoListener.class);
   }
   
   protected void fireCreatedEvent (DaoEvent<T> e)
   {
      for (DaoListener<T> listener : getListeners ())
      {
         listener.created (e);
      }
   }

   protected void fireUpdatedEvent (DaoEvent<T> e)
   {
      for (DaoListener<T> listener : getListeners ())
      {
         listener.updated (e);
      }
   }

   protected void fireDeletedEvent (DaoEvent<T> e)
   {
      for (DaoListener<T> listener : getListeners ())
      {
         listener.deleted (e);
      }
   }
   
   @Autowired
   public void init (SessionFactory session_factory)
   {
      setSessionFactory (session_factory);
   }
   
   public void printCurrentSessions ()
   {
      int num_session = countOpenSessions ();
      logger.info(countOpenSessions () + " open sessions:");
      int index = 0;
      while (index<num_session)
      {
         logger.info("   SESSION_ID       "+ getSystemByName("SESSION_ID", index));
         logger.info("   CONNECTED        "+ getSystemByName("CONNECTED", index));
         logger.info("   SCHEMA           "+ getSystemByName("SCHEMA", index));
         //logger.info(
         //   "TRANSACTION      "+ getSystemByName("TRANSACTION", index));
         logger.info("   WAITING_FOR_THIS "+ getSystemByName("WAITING_FOR_THIS", index));
         logger.info("   THIS_WAITING_FOR "+ getSystemByName("THIS_WAITING_FOR", index));
         logger.info("   LATCH_COUNT      "+ getSystemByName("LATCH_COUNT", index));
         logger.info("   STATEMENT        "+ getSystemByName("CURRENT_STATEMENT",index));
         logger.info("");
         index++;
      }
   }
   
   @SuppressWarnings ("rawtypes")
   private int countOpenSessions ()
   {
      return DataAccessUtils.intResult (getHibernateTemplate ().execute (
         new HibernateCallback<List>()
         {
            @Override
            public List doInHibernate(Session session) 
               throws HibernateException, SQLException
            {
               String sql = 
                  "SELECT count (*) FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS";
               SQLQuery query = session.createSQLQuery (sql);
               return query.list ();
            }
         }));
   }
   
   @SuppressWarnings ({ "unchecked", "rawtypes" })
   private String getSystemByName (final String name, final int index)
   {
      return DataAccessUtils.uniqueResult (getHibernateTemplate ().execute (
         new HibernateCallback<List>()
         {
            @Override
            public List doInHibernate(Session session) 
               throws HibernateException, SQLException
            {
               String sql = 
                  "SELECT " + name +
                  " FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS" +
                  " LIMIT  1 OFFSET " + index;
               SQLQuery query = session.createSQLQuery (sql);
               return query.list ();
            }
         })).toString ();
   }

   /**
    * Returns a paged list of database entities.
    * 
    * @param query the passed query to retrieve the list.
    * @param skip the number of elements to skip in the list (0=no skip).
    * @param top number of element to be retained in the list.
    * @throws ClassCastException if query does not returns entity list of type T.
    * @see org.hibernate.Query
    */
   @Override
   public List<T> getPage (final String query, final int skip, final int top)
   {
      return getHibernateTemplate ().execute (new HibernateCallback<List<T>> ()
      {
         // List must be instance of List<T> otherwise ClassCast
         @SuppressWarnings ("unchecked")
         @Override
         public List<T> doInHibernate (Session session) throws
               HibernateException, SQLException
         {
            Query hql_query = session.createQuery (query);
            hql_query.setFirstResult (skip);
            hql_query.setMaxResults (top);
            return hql_query.list ();
         }
      });
   }

   @SuppressWarnings ("unchecked")
   public List<T> listCriteria (DetachedCriteria detached, int skip, int top)
   {
      SessionFactory factory = getSessionFactory ();
      org.hibernate.classic.Session session = factory.getCurrentSession ();

      Criteria criteria = detached.getExecutableCriteria (session);

      if (skip > 0)
         criteria.setFirstResult (skip);
      if (top > 0)
         criteria.setMaxResults (top);
      return criteria.list ();
   }

   @SuppressWarnings ("unchecked")
   public T uniqueResult (DetachedCriteria criteria)
   {
      return (T) criteria.getExecutableCriteria (
            getSessionFactory ().getCurrentSession ()).uniqueResult ();
   }

   public int count (DetachedCriteria detached)
   {
      Session session = getSessionFactory ().getCurrentSession ();
      Criteria criteria = detached.getExecutableCriteria (session);
      Object result = criteria.uniqueResult ();
      return ((Number) result).intValue ();
   }
}
