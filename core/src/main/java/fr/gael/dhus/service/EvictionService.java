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
package fr.gael.dhus.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.interfaces.IEvictionDao;
import fr.gael.dhus.database.object.Eviction;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.eviction.EvictionManager;
import fr.gael.dhus.datastore.eviction.EvictionStrategy;

@Service
public class EvictionService extends WebService
{
   @Autowired
   private EvictionManager evictionMgr;
   
   @Autowired
   private IEvictionDao evictionDao;
   
   @Autowired
   private ProductDao productDao;
   
   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int getKeepPeriod()
   {
      return evictionDao.getEviction ().getKeepPeriod ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public int getMaxDiskUsage()
   {
      return evictionDao.getEviction ().getMaxDiskUsage ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public EvictionStrategy getStrategy()
   {
      return evictionDao.getEviction ().getStrategy ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void save (EvictionStrategy strategy, int keep_period,
         int max_disk_usage)
   {
      evictionDao.update (strategy, keep_period, max_disk_usage);
      evictionMgr.computeNextProducts ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public List<Product> getEvictableProducts()
   {
      return new ArrayList<> (evictionMgr.getProducts ());
   }
   
   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public void doEvict()
   {
      evictionMgr.doEvict ();
   }
   
   @PreAuthorize ("isAuthenticated ()")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   @Cacheable (value = "product_eviction_date", key = "#pid")
   public Date getEvictionDate (Long pid)
   {
      Eviction eviction = evictionDao.getEviction (); 
      if (eviction.getStrategy () == EvictionStrategy.NONE)
      {
         return null;
      }
      Product p = productDao.read (pid);
      DateTime dt = new DateTime (p.getIngestionDate ());
      DateTime res = dt.plusDays (eviction.getKeepPeriod ());
      return res.toDate ();
   }

   // Methods for unit tests
   void setEvictionDao (IEvictionDao eviction_dao)
   {
      this.evictionDao = eviction_dao;
   }

   void setEvictionMgr (EvictionManager eviction_mgr)
   {
      this.evictionMgr = eviction_mgr;
   }
}
