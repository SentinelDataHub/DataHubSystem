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

import java.sql.SQLException;

import fr.gael.dhus.database.dao.UserDao;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;

public class DaoUtils
{
   public static final int DEFAULT_ELEMENTS_PER_PAGE = 3;

   /**
    * Hide utility class constructor
    */
   private DaoUtils ()
   {

   }
   
   /**
    * Escape quote marks in the given string by doubling it (' -> '').
    * @param s String to secure.
    * @return a string with escaped quotes.
    */
   public static String secureString (String s)
   {
      if (s==null) return null;
      return s.replace ("'", "''");
   }
   
   public static String userRestriction (User u, String pattern)
   {
      ConfigurationManager cfgManager = ApplicationContextProvider
            .getBean(ConfigurationManager.class);
      if (cfgManager.isDataPublic () || u == null
            || u.getRoles ().contains (Role.DATA_MANAGER))
      {
         return "";
      }

      User pData = ApplicationContextProvider.getBean (
            UserDao.class).getPublicData ();
      return "('" + u.getUUID () + "' in elements(" + pattern + "authorizedUsers"
         + ")" + " OR '" + pData.getUUID () + "' in elements(" +
         pattern + "authorizedUsers))";
   }
   
   public static void optimize ()
   {
      HibernateDaoLocalSupport support = ApplicationContextProvider.getBean (
            HibernateDaoLocalSupport.class);
      support.getHibernateTemplate ().flush ();
      support.getHibernateTemplate ().executeWithNativeSession (
         new HibernateCallback<Void> ()
         {
            @Override
            public Void doInHibernate (Session session) throws
                  HibernateException, SQLException
            {
               SQLQuery query = session.createSQLQuery ("CHECKPOINT DEFRAG");
               query.executeUpdate ();
               return null;
            }
         });
   }
   
   @Repository ("hibernateDaoLocalSupport")
   private static class HibernateDaoLocalSupport extends HibernateDaoSupport
   {           
      @Autowired
      public void init (SessionFactory session_factory)
      {
         setSessionFactory (session_factory);
      }
   }
}
