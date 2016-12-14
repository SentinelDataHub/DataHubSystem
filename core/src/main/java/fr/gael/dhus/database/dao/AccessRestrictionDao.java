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

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.restriction.AccessRestriction;

import java.sql.SQLException;

@Repository
public class AccessRestrictionDao extends HibernateDao<AccessRestriction, String>
{
   @Override
   public void deleteAll ()
   {
      getHibernateTemplate ().execute (new HibernateCallback<Void> ()
      {
         @Override
         public Void doInHibernate (Session session)
               throws HibernateException, SQLException
         {
            SQLQuery query =
                  session.createSQLQuery ("DELETE FROM USER_RESTRICTIONS");
            query.executeUpdate ();
            query = session.createSQLQuery ("DELETE  FROM ACCESS_RESTRICTION");
            query.executeUpdate ();
            return null;
         }
      });
   }
}
