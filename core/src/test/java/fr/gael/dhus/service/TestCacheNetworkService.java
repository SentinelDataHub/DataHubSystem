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

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.util.TestContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Date;

@ContextConfiguration (
      locations = { "classpath:fr/gael/dhus/spring/context-test.xml",
            "classpath:fr/gael/dhus/spring/context-security-test.xml" },
      loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestCacheNetworkService
      extends AbstractTransactionalTestNGSpringContextTests
{

   @Autowired
   private NetworkUsageService networkService;

   @Autowired
   private CacheManager cacheManager;

   @Test
   public void testNetworkServiceCache ()
   {
      String count_cache = "network_download_count";
      String size_cache = "network_download_size";
      User user = new User ();
      user.setId (2L);
      Calendar calendar = Calendar.getInstance ();
      calendar.set (2014, Calendar.JANUARY, 01);
      long period = System.currentTimeMillis () - calendar.getTime ().getTime ();

      // test cache
      int r0 = networkService.countDownloadsByUserSince (user, period);
      Assert.assertEquals (r0, 2);
      int a0 = cacheManager.getCache (count_cache).get (
            user.getId (), Integer.class);
      Assert.assertEquals (a0, r0);

      long r1 = networkService.getDownloadedSizeByUserSince (user, period);
      Assert.assertEquals (r1, 514);
      long a1 = cacheManager.getCache (size_cache).get (
            user.getId (), Long.class);
      Assert.assertEquals (a1, r1);

      // test cache eviction
      networkService.createDownloadUsage (64L, new Date (), user);
      Assert.assertNull (cacheManager.getCache (count_cache).get (
            user.getId (), Integer.class));
      Assert.assertNull (
            cacheManager.getCache (size_cache).get (user.getId (), Long.class));
   }

}
