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
import fr.gael.dhus.database.object.Eviction;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.eviction.EvictionStrategy;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * @author pidancier
 *
 */
@Repository
public class EvictionDao extends HibernateDao<Eviction, Long>
{
   @Autowired
   private ConfigurationManager cfgManager;
   
   public Eviction getFactoryDefault ()
   {
      // TODO rename method (getDefaultEviction)      
      Eviction eviction = new Eviction ();
      eviction.setMaxDiskUsage (cfgManager.getArchiveConfiguration ().getEvictionConfiguration ().getMaxDiskUsage ());
      eviction.setKeepPeriod (cfgManager.getArchiveConfiguration ().getEvictionConfiguration ().getKeepPeriod ());
      eviction.setStrategy (EvictionStrategy.NONE);
      eviction.setMaxProductNumber (cfgManager.getArchiveConfiguration ().getEvictionConfiguration ().getMaxEvictedProducts ());

      return eviction;
   }

   public Eviction getEviction ()
   {
      //List<Eviction> evictions = readAll ();
      // Eviction eviction = null;
      // if (evictions.isEmpty ())
      // {
      // eviction = getFactoryDefault ();
      // eviction = create (eviction);
      // }
      // else
      // {
      // // Get the last inserted one
      // eviction = evictions.get (evictions.size ()-1);
      // }
      // return eviction;
      if (count () == 0)
      {
         return null; //create (getFactoryDefault ());
      }
      return first ("FROM Eviction ORDER BY id DESC LIMIT 1");
   }

   public void update (EvictionStrategy strategy, int keepPeriod,
      int maxDiskUsage)
   {
      // TODO move to service
      Eviction eviction = getEviction ();
      eviction.setKeepPeriod (keepPeriod);
      eviction.setMaxDiskUsage (maxDiskUsage);
      eviction.setStrategy (strategy);
      update (eviction);
   }

   public void setProducts (Set<Product> products)
   {
      // TODO move to service
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
