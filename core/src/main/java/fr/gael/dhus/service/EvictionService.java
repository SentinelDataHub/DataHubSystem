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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.EvictionDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.eviction.EvictionManager;
import fr.gael.dhus.datastore.eviction.EvictionStrategy;

/**
 * @author valette
 */
@Service
public class EvictionService extends WebService
{
   @Autowired
   private EvictionManager evictionMgr;
   
   @Autowired
   private EvictionDao evictionDao;

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public int getKeepPeriod()
   {
      return evictionDao.getEviction ().getKeepPeriod ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public int getMaxDiskUsage()
   {
      return evictionDao.getEviction ().getMaxDiskUsage ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public EvictionStrategy getStrategy()
   {
      return evictionDao.getEviction ().getStrategy ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public void save(EvictionStrategy strategy, int keepPeriod, int maxDiskUsage)
   {
      evictionDao.update (strategy, keepPeriod, maxDiskUsage);
      evictionMgr.computeNextProducts ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public List<Product> getEvictableProducts()
   {
      return new ArrayList<Product> (evictionMgr.getProducts ());
   }
   
   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public void doEvict()
   {
      evictionMgr.doEvict ();
   }
   
   @PreAuthorize ("isAuthenticated ()")
   public Date getEvictionDate (Long pid)
   {
      // TODO Compute eviction date for given product
      return null;
   }
}
