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
import fr.gael.dhus.database.object.SynchronizerConf;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;

/**
 * Manages {@link SynchronizerConf}.
 */
@Repository
public class SynchronizerDao extends HibernateDao<SynchronizerConf, Long>
{
   /**
    * Returns a list of active synchronizers.
    * @return a list of active synchronizers.
    */
   public List<SynchronizerConf> getActiveSynchronizers ()
   {
      String hql = "FROM SynchronizerConf WHERE Active = TRUE";
      return (List<SynchronizerConf>) getHibernateTemplate ().find (hql);
   }

   /**
    * Returns an iterator on {@link SynchronizerConf}s.
    * @param type (optional) see {@link SynchronizerConf#getType()}.
    * @return iterator on {@link SynchronizerConf}s.
    */
   public Iterator<SynchronizerConf> getAllSynchronizerConfs (String type)
   {
      StringBuilder query = new StringBuilder("FROM ").append(entityClass.getName());
      if (type != null && !type.isEmpty())
      {
         query.append(" WHERE Type = ").append('\'').append(type).append('\'');
      }
      return new PagedIterator<>(this, query.toString());
   }
}
