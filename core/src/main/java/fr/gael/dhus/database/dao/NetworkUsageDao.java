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
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.NetworkUsage;
import fr.gael.dhus.database.object.User;

@Repository
public class NetworkUsageDao extends HibernateDao<NetworkUsage, Long>
{
   public int getDownlaodedCountPerUser (final User user, long period)
   {
      long current_timestamp = new Date ().getTime ();
      final Date query_date = new Date (current_timestamp - period);

      Long result =
         getHibernateTemplate ().execute (new HibernateCallback<Long> ()
         {
            @Override
            public Long doInHibernate (Session session)
               throws HibernateException, SQLException
            {
               Query query =
                  session.createQuery ("SELECT count(networks) "
                     + "FROM NetworkUsage networks "
                     + "WHERE networks.user = :user AND "
                     + "networks.isDownload = true AND networks.date > :date");
               query.setEntity ("user", user);
               query.setDate ("date", query_date);
               return (Long) query.uniqueResult ();
            }
         });
      return (result != null) ? result.intValue () : 0;
   }

   public long getDownlaodedSizePerUser (final User user, long period)
   {
      long current_timestamp = new Date ().getTime ();
      final Date query_date = new Date (current_timestamp - period);

      Long result =
         getHibernateTemplate ().execute (new HibernateCallback<Long> ()
         {
            @Override
            public Long doInHibernate (Session session)
               throws HibernateException, SQLException
            {
               Query query =
                  session.createQuery ("select sum(networks.size) "
                     + "from NetworkUsage networks "
                     + " where networks.user=:user and "
                     + " networks.isDownload=true and "
                     + " networks.date>:date");
               query.setParameter ("user", user);
               query.setParameter ("date", query_date);
               return (Long) query.uniqueResult ();
            }
         });
      return (result == null) ? 0 : result;
   }
}
