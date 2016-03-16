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
import fr.gael.dhus.database.object.Country;

@Repository
public class CountryDao extends HibernateDao<Country, Long>
{
   @Override
   public void deleteAll ()
   {
      throw new UnsupportedOperationException ();
   }

   @Override
   public void delete (Country c)
   {
      throw new UnsupportedOperationException ();
   }

   @Override
   public void update (Country country)
   {
      throw new UnsupportedOperationException ();
   }

   @Override
   public Country create (Country country)
   {
      throw new UnsupportedOperationException ();
   }

   public List<String> readAllNames ()
   {
      return getHibernateTemplate ().execute (
         new HibernateCallback<List<String>> ()
         {
            @SuppressWarnings ("unchecked")
            @Override
            public List<String> doInHibernate (Session session)
               throws HibernateException, SQLException
            {
               String hql =
                  "SELECT name FROM Country "+
                        " ORDER BY name";
               Query query = session.createQuery (hql).setReadOnly (true);
               return (List<String>) query.list ();
            }
         });
   }

   public Country getCountryByName (String name)
   {
      List<Country> countries = (List<Country>) getHibernateTemplate ().find (
            "FROM Country WHERE name=?", name);
      if (countries.isEmpty ())
      {
         return null;
      }
      return countries.get (0);
   }

   public Country getCountryByAlpha2 (String alpha2)
   {
      List<Country> countries = (List<Country>) getHibernateTemplate ().find (
            "FROM Country WHERE alpha2=?", alpha2);
      if (countries.isEmpty ())
      {
         return null;
      }
      return countries.get (0);
   }

   public Country getCountryByAlpha3 (String alpha3)
   {
      List<Country> countries = (List<Country>) getHibernateTemplate ().find (
            "FROM Country WHERE alpha3=?", alpha3);
      if (countries.isEmpty ())
      {
         return null;
      }
      return countries.get (0);
   }

   public Country getCountryByNumeric (Integer numeric)
   {
      List<Country> countries = (List<Country>) getHibernateTemplate ().find (
            "FROM Country WHERE numeric=?", numeric);
      if (countries.isEmpty ())
      {
         return null;
      }
      return countries.get (0);
   }
}
