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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate DAO Implementation, containing minimal CRUD operations.
 * 
 * @author valette
 * @param <T> Object concerned by this DAO
 * @param <PK> Primary Key of this Object.
 */
public class HibernateDao<T, PK extends Serializable> extends
   HibernateDaoSupport implements GenericDao<T, PK>
{
   protected Class<T> entityClass;

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
      logger.info ("Create/save " + entityClass.getSimpleName () + "("+ id 
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
      logger.debug ("Read " + entityClass.getSimpleName () + "(" + id + ") spent " + (end-start) + "ms" );
      return ret;
   }

   @Override
   public void update (T t)
   {
      long start = new Date ().getTime ();
      getHibernateTemplate ().update (t);
      long end = new Date ().getTime ();
      logger.info ("Update " + entityClass.getSimpleName () + " spent " + (end-start) + "ms" );

      fireUpdatedEvent (new DaoEvent<T> (t));
   }

   @Override
   public void delete (T t)
   {
      long start = new Date ().getTime ();
      getHibernateTemplate ().delete (t);
      long end = new Date ().getTime ();
      logger.info ("Delete " + entityClass.getSimpleName () + " spent " + (end-start) + "ms" );

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
      if (skip > 0)
         query.setFirstResult (skip);
      if (n > 0)
         query.setMaxResults (n);

      List<T> result = (List<T>) query.list ();

      if (newSession)
      {
         session.disconnect ();
      }

      return result;
   }

   /**
    * Retrieve the first element of the results.
    * 
    * @param queryString
    * @return
    */
   public T first (String queryString)
   {
      @SuppressWarnings ("unchecked")
      List<T> result = (List<T>) getHibernateTemplate ().find (queryString);
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
   public List find(String queryString) throws DataAccessException 
   {
      long start = new Date ().getTime ();
      List ret= getHibernateTemplate ().find (queryString);
      
      long end = new Date ().getTime ();
      logger.debug ("Query \"" + 
         queryString.replaceAll("(\\r|\\n)", " ").trim () + 
         "\" spent " + (end-start) + "ms" );
      return ret;
   }
   
   // DAO listeners
   private final EventListenerList listeners = new EventListenerList ();

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
   public void init (SessionFactory sessionFactory)
   {
      setSessionFactory (sessionFactory);
   }
}
