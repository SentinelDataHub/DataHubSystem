/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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

import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.gael.dhus.database.dao.NetworkUsageDao;
import fr.gael.dhus.database.object.NetworkUsage;
import fr.gael.dhus.database.object.User;

@Service
public class NetworkUsageService
{
   @Autowired
   private NetworkUsageDao networkUsageDao;

   public NetworkUsageDao getNetworkUsageDao ()
   {
      return networkUsageDao;
   }

   public void setNetworkUsageDao (NetworkUsageDao networkUsageDao)
   {
      this.networkUsageDao = networkUsageDao;
   }

   /**
    * Saves persistently a download performed by a user.
    *
    * @param size       download size.
    * @param start_date date downloading start.
    * @param user       associate user to download.
    */
   @Transactional
   @Caching (evict = {
      @CacheEvict (value = "network_download_count", key = "#user.getUUID ()", condition = "#user != null"),
      @CacheEvict (value = "network_download_size", key = "#user.getUUID ()", condition = "#user != null") })
   public void createDownloadUsage (final Long size, final Date start_date,
         final User user)
   {
      if (size == null || size < 0 || start_date == null || user == null)
      {
         throw new IllegalArgumentException ("Invalid parameters");
      }

      NetworkUsage download_usage = new NetworkUsage ();
      download_usage.setSize (size);
      download_usage.setDate (start_date);
      download_usage.setUser (user);
      download_usage.setIsDownload (true);
      networkUsageDao.create (download_usage);
   }

   /**
    * Returns number of downloads by a user on a given period.
    *
    * @param user   associate user to downloads.
    * @param period period time in millisecond.
    * @return number of downloads.
    */
   @Transactional (readOnly = true)
   @Cacheable (value = "network_download_count", key = "#user.getUUID ()", condition = "#user != null")
   public int countDownloadsByUserSince (final User user, final Long period)
   {
      Objects.requireNonNull (user, "'user' parameter is null");
      Objects.requireNonNull (period, "'period' parameter is null");

      long current_timestamp = System.currentTimeMillis ();
      if (period < 0 || period > current_timestamp)
      {
         throw new IllegalArgumentException ("period time too high");
      }
      Date date = new Date (current_timestamp - period);
      return networkUsageDao.countDownloadByUserSince (user, date);
   }

   /**
    * Returns the cumulative downloaded size by a user on a given period.
    *
    * @param user   associate user to downloads.
    * @param period period time in millisecond.
    * @return cumulative downloaded size.
    */
   @Transactional (readOnly = true)
   @Cacheable (value = "network_download_size", key = "#user.getUUID ()", condition = "#user != null")
   public Long getDownloadedSizeByUserSince (final User user, final Long period)
   {
      Objects.requireNonNull (user, "'user' parameter is null");
      Objects.requireNonNull (period, "'period' parameter is null");

      long current_timestamp = System.currentTimeMillis ();
      if (period < 0 || period > current_timestamp)
      {
         throw new IllegalArgumentException ("period time too high");
      }
      Date date = new Date (current_timestamp - period);

      return networkUsageDao.getDownloadedSizeByUserSince (user, date);
   }
}
