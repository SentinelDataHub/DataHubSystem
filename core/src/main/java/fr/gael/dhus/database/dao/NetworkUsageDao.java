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
import fr.gael.dhus.database.object.NetworkUsage;
import fr.gael.dhus.database.object.User;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;

@Repository
public class NetworkUsageDao extends HibernateDao<NetworkUsage, Long>
{
   public int countDownloadByUserSince (final User user, final Date date)
   {
      Long result =
            getHibernateTemplate ().execute (new HibernateCallback<Long> ()
            {
               @Override
               public Long doInHibernate (Session session)
                     throws HibernateException, SQLException
               {
                  Criteria criteria = session.createCriteria (
                        NetworkUsage.class);
                  criteria.setProjection (Projections.rowCount ());
                  criteria.add (Restrictions.eq ("isDownload", true));
                  criteria.add (Restrictions.eq ("user", user));
                  criteria.add (Restrictions.gt ("date", date));
                  return (Long) criteria.uniqueResult ();
               }
            });
      return (result != null) ? result.intValue () : 0;
   }

   public long getDownloadedSizeByUserSince (final User user, final Date date)
   {
      Long result =
            getHibernateTemplate ().execute (new HibernateCallback<Long> ()
            {
               @Override
               public Long doInHibernate (Session session)
                     throws HibernateException, SQLException
               {
                  Criteria criteria = session.createCriteria (
                        NetworkUsage.class);
                  criteria.setProjection (Projections.sum ("size"));
                  criteria.add (Restrictions.eq ("isDownload", true));
                  criteria.add (Restrictions.eq ("user", user));
                  criteria.add (Restrictions.gt ("date", date));
                  return (Long) criteria.uniqueResult ();
               }
            });
      return (result == null) ? 0 : result;
   }
}
