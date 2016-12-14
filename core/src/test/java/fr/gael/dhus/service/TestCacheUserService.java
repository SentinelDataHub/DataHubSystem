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

import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.exception.EmailNotSentException;
import fr.gael.dhus.service.exception.MalformedEmailException;
import fr.gael.dhus.service.exception.RequiredFieldMissingException;
import fr.gael.dhus.service.exception.RootNotModifiableException;
import fr.gael.dhus.service.exception.UsernameBadCharacterException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.util.TestContextLoader;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

@ContextConfiguration (
      locations = { "classpath:fr/gael/dhus/spring/context-test.xml",
            "classpath:fr/gael/dhus/spring/context-security-test.xml" },
      loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestCacheUserService
      extends AbstractTransactionalTestNGSpringContextTests
{
   private fr.gael.dhus.service.UserService userService;

   private CacheManager cacheManager;

   @BeforeClass
   public void setUp ()
   {
      this.userService = ApplicationContextProvider.getBean (
            fr.gael.dhus.service.UserService.class);
      this.cacheManager =
            ApplicationContextProvider.getBean (CacheManager.class);
      authenticate ();
   }

   private void authenticate ()
   {
      String name = "authenticatedUser";
      Set<GrantedAuthority> roles = new HashSet<> ();
      roles.add (new SimpleGrantedAuthority (Role.DOWNLOAD.getAuthority ()));
      roles.add (new SimpleGrantedAuthority (Role.SEARCH.getAuthority ()));
      roles.add (
            new SimpleGrantedAuthority (Role.USER_MANAGER.getAuthority ()));

      SandBoxUser user = new SandBoxUser (name, name, true, 0, roles);
      Authentication auth = new UsernamePasswordAuthenticationToken (
            user, user.getPassword (), roles);
      SecurityContextHolder.getContext ().setAuthentication (auth);

      logger.info ("userTest roles: " + auth.getAuthorities ());
   }

   @Test
   public void testUserCache () throws RootNotModifiableException,
         RequiredFieldMissingException
   {
      String cache_name = "user";
      String cache2_name = "userByName";
      String username;
      String uid;
      User user;

      // getUser (Long)
      uid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0";
      user = userService.getUser (uid);
      Cache cache = cacheManager.getCache (cache_name);
      Cache cache2 = cacheManager.getCache (cache2_name);
      Assert.assertEquals (cache.get (uid, User.class), user);

      // getUserByName (String)
      username = "babar";
      user = userService.getUserByName (username);
      Assert.assertEquals (cache2.get (username, User.class), user);

      // getUserNoCheck (String)
      user = userService.getUserNoCheck (username);
      Assert.assertEquals (user, cache2.get (username, User.class));

      // updateUser (User)
      user.setEmail ("test@test.com");
      userService.updateUser (user);
      Assert.assertNull (cache.get (user.getUUID (), User.class));
      Assert.assertNull (cache2.get (user.getUsername (), User.class));

      // selfUpdateUser (User)
      uid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0";
      user = userService.getUser (uid);
      user = userService.getUserNoCheck (user.getUsername ());
      user.setPhone ("+336******45");
      userService.selfUpdateUser (user);
      Assert.assertNull (cache.get (user.getUUID (), User.class));
      Assert.assertNull (cache2.get (user.getUsername (), User.class));

      // selfChangePassword (Long, String, String)
      uid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0";
      username = userService.getUser (uid).getUsername ();
      userService.getUserByName (username);
      userService.selfChangePassword (uid, "koko", "password");
      Assert.assertNull (cache.get (uid, User.class));
      Assert.assertNull (cache2.get (username, User.class));

      // deleteUser (Long)
      uid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0";
      username = userService.getUser (uid).getUsername ();
      Assert.assertNotNull (userService.getUserByName (username));
      userService.deleteUser (uid);
      Assert.assertNull (cache.get (uid, User.class));
      Assert.assertNull (cache2.get (username, User.class));

   }

   @Test
   public void MalformedUserCreateTest() throws EmailNotSentException,
      RequiredFieldMissingException, RootNotModifiableException
   {
      // Case of well formed...
      User u = new User();
      u.setPassword ("test001");
      u.setUsername ("test001");
      u.setEmail ("test@test.com");
      u.setCountry ("France");
      userService.createUser(u);
      
      // Case of well formed...
      u = new User();
      u.setPassword ("test001");
      u.setUsername ("_.-");
      u.setEmail ("test@test.com");
      u.setCountry ("France");
      userService.createUser(u);
      
      // Case of malformed...
      u = new User();
      u.setPassword ("test001");
      u.setUsername ("toto.A{}\\01");
      u.setEmail ("test@test.com");
      u.setCountry ("France");
      try
      {
         userService.createUser(u);
         Assert.fail("Malformed username created.");
      }
      catch (UsernameBadCharacterException e)
      {
         // Expected result
      }
      
      // Case of malformed...
      u = new User();
      u.setPassword ("test001");
      u.setUsername ("toto002");
      u.setEmail (".+@test.com");
      u.setCountry ("France");
      try
      {
         userService.createUser(u);
         // No mail characters restrictions...
         Assert.fail("User created with malformed email.");
      }
      catch (MalformedEmailException e)
      {
         // Expected result
      }
   }
   
   @Test (dependsOnMethods="MalformedUserCreateTest")
   public void MalformedUserUpdateTest() throws EmailNotSentException,
      RequiredFieldMissingException, RootNotModifiableException
   {
      UserDao udao = ApplicationContextProvider.getBean (UserDao.class);
      // Normal update
      User u = new User ();
      u.setUsername ("test001");
      u.setPassword ("test001");
      u.setEmail ("test@test.com");
      u.setCountry ("France");
      udao.create(u);
      
      // Normal update
      u = userService.getUserByName("test001");
      u.setUsername("test0001");
      userService.updateUser (u);
      u.setUsername("test001{}\\;+");
      try
      {
         userService.updateUser (u);
         Assert.fail("User created with malformed email.");
      }
      catch (UsernameBadCharacterException e) {}
      
      User u1 = userService.getUserByName("babar");
      u1.setEmail("azaza");
      try
      {
         userService.updateUser (u1);
         Assert.fail("User created with malformed email.");
      }
      catch (MalformedEmailException e) {}
   }
   
   @Test
   public void ExtendedMalformedUserTest() throws EmailNotSentException,
      RequiredFieldMissingException, RootNotModifiableException
   {
      UserDao udao = ApplicationContextProvider.getBean (UserDao.class);
      // Normal update
      User u = new User ();
      u.setUsername ("toto.A{}\\01");
      u.setPassword ("test001");
      u.setEmail ("test@test.com");
      u.setCountry ("France");
      udao.create(u);
      
      u = userService.getUserByName("toto.A{}\\01");
      u.setUsername("correctedUsername001");
      userService.updateUser(u);
   }

}
