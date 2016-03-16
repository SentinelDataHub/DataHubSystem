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

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.dao.interfaces.IEvictionDao;
import fr.gael.dhus.database.object.Eviction;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.config.system.EvictionConfiguration;
import fr.gael.dhus.datastore.eviction.EvictionStrategy;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * @author pidancier
 *
 */
@Repository
public class EvictionDao extends HibernateDao<Eviction, Long> implements
      IEvictionDao
{
   @Autowired
   private ConfigurationManager cfgManager;

   public Eviction getFactoryDefault ()
   {
      Eviction eviction = new Eviction ();
      EvictionConfiguration config = cfgManager.getArchiveConfiguration ()
            .getEvictionConfiguration ();

      eviction.setMaxDiskUsage (config.getMaxDiskUsage ());
      eviction.setKeepPeriod (config.getKeepPeriod ());
      eviction.setStrategy (EvictionStrategy.NONE);
      eviction.setMaxProductNumber (config.getMaxEvictedProducts ());

      return eviction;
   }

   public Eviction getEviction ()
   {
      return first ("FROM Eviction ORDER BY id DESC LIMIT 1");
   }

   public void update (EvictionStrategy strategy, int keep_period,
      int max_disk_usage)
   {
      Eviction eviction = getEviction ();
      eviction.setKeepPeriod (keep_period);
      eviction.setMaxDiskUsage (max_disk_usage);
      eviction.setStrategy (strategy);
      update (eviction);
   }

   public void setProducts (Set<Product> products)
   {
      Eviction eviction = getEviction ();
      eviction.setProducts (products);
      update (eviction);
   }

   public Set<Product> getProducts ()
   {
      Eviction eviction = getEviction ();
      return eviction.getProducts ();
   }
   
   public void removeProduct (Product product)
   {
      Eviction eviction = getEviction ();
      
      if (eviction.getProducts ().contains (product))
      {
         eviction.getProducts ().remove (product);
         update (eviction);
      }
   }
}
