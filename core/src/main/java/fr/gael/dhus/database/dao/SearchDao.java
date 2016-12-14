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
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Search;
import fr.gael.dhus.database.object.User;

@Repository
public class SearchDao extends HibernateDao<Search, String>
{
   public List<Search> scrollSearchesOfUser (final User user, final int skip,
      final int top)
   {
      return getHibernateTemplate ().execute (
         new HibernateCallback<List<Search>> ()
         {
            @Override
            @SuppressWarnings ("unchecked")
            public List<Search> doInHibernate (Session session)
               throws HibernateException, SQLException
            {
               String hql =
                  "SELECT s FROM User u LEFT OUTER JOIN u.preferences p "
                     + "LEFT OUTER JOIN p.searches s "
                     + "WHERE u.uuid like ? ORDER BY s.value";
               Query query = session.createQuery (hql).setReadOnly (true);
               query.setString (0, user.getUUID ());
               query.setFirstResult (skip);
               query.setMaxResults (top);
               return (List<Search>) query.list ();
            }
         });
   }
   
   @Override
   public void delete (final Search search)
   {
      getHibernateTemplate ().execute (new HibernateCallback<Void>()
      {
         @Override
         public Void doInHibernate (Session session) throws HibernateException,
            SQLException
         {
            String sql = "DELETE FROM SEARCH_PREFERENCES WHERE SEARCHES_UUID = ?";
            Query query = session.createSQLQuery (sql);
            query.setString (0, search.getUUID ());
            query.executeUpdate ();
            return null;
         }
      });
      super.delete (search);
   }

   /**
    * Inactive all saved search notifications of users.
    */
   public void disableAllSearchNotifications ()
   {
      getHibernateTemplate ().execute (new HibernateCallback<Void> ()
      {
         @Override
         public Void doInHibernate (Session session) throws HibernateException,
               SQLException
         {
            String query = "UPDATE SEARCHES SET NOTIFY = false";
            session.createSQLQuery (query).executeUpdate ();
            return null;
         }
      });
   }
}
