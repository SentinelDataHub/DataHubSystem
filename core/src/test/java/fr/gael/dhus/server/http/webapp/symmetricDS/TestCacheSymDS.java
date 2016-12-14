/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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
package fr.gael.dhus.server.http.webapp.symmetricDS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jumpmind.db.model.Column;
import org.jumpmind.db.model.Table;
import org.jumpmind.symmetric.io.data.CsvData;
import org.jumpmind.symmetric.io.data.DataEventType;
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

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.SandBoxUser;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.service.exception.RootNotModifiableException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.util.TestContextLoader;

@ContextConfiguration (
      locations = { "classpath:fr/gael/dhus/spring/context-test.xml",
            "classpath:fr/gael/dhus/spring/context-security-test.xml" },
      loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestCacheSymDS
      extends AbstractTransactionalTestNGSpringContextTests
{
   private SolrReplicationExtensionPoint extPoint;
   private CacheManager cacheManager;
   
   private UserService userService;
   private UserDao userDao;
   
   private ProductService productService;
   private ProductDao productDao;

   @BeforeClass
   public void setUp ()
   {
      this.extPoint = ApplicationContextProvider
         .getBean (SolrReplicationExtensionPoint.class);
      this.cacheManager =
         ApplicationContextProvider.getBean (CacheManager.class);
      this.userService = ApplicationContextProvider.getBean (UserService.class);
      this.userDao = ApplicationContextProvider.getBean (UserDao.class);
      this.productService =
         ApplicationContextProvider.getBean (ProductService.class);
      this.productDao = ApplicationContextProvider.getBean (ProductDao.class);
      authenticate ();
      
      productService.count ();
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

   private void simulateUserReplication (String username, String uuid)
   {
      User u = new User();
      u.setUsername (username);
      u.setPassword ("a");
      u.setUUID (uuid);
      
      userDao.create (u);
      CsvData data = new CsvData(DataEventType.INSERT, new String[]{uuid, username});
      
      Column column = new Column ("UUID");
      Table table = new Table ("USERS");
      table.addColumn (column);
      Column col2 = new Column("LOGIN");
      table.addColumn (col2);
      extPoint.afterWrite (null, table, data);
      extPoint.batchCommitted (null);
   }

   private void simulateUserUpdate (User u)
   {
      userDao.update (u);
      CsvData data = new CsvData(DataEventType.UPDATE, new String[]{u.getUUID(), u.getUsername()});

      Column column = new Column ("UUID");
      Table table = new Table ("USERS");
      table.addColumn (column);
      Column col2 = new Column("LOGIN");
      table.addColumn(col2);
      extPoint.afterWrite (null, table, data);
      extPoint.batchCommitted (null);
   }

   private void simulateUserDelete (User u)
   {
      CsvData data = new CsvData(DataEventType.DELETE, null, new String[]{u.getUUID(), u.getUsername()}, null);

      Column column = new Column ("UUID");
      Table table = new Table ("USERS");
      table.addColumn (column);
      Column col2 = new Column("LOGIN");
      table.addColumn(col2);
      extPoint.beforeWrite (null, table, data);
      userDao.delete (u);
   }

   private Long simulateProductReplication (String uuid)
   {
      Product p = new Product ();
      p.setUuid (uuid);
      p.setIdentifier("product_" + uuid);
      p.setProcessed (true);
      try
      {
         p.setPath (new URL ("http://www.google.fr"));
      }
      catch (MalformedURLException e)
      {
      }
      
      p = productDao.create (p);

      CsvData data = new CsvData(DataEventType.INSERT, new String[]{p.getId().toString(), p.getIdentifier()});

      Column column = new Column ("ID");
      Table table = new Table ("PRODUCTS");
      table.addColumn (column);
      Column col2 = new Column("IDENTIFIER");
      table.addColumn(col2);
      extPoint.afterWrite (null, table, data);
      
      data = new CsvData (DataEventType.INSERT, new String [] {p.getId ().toString ()});
      column = new Column ("PRODUCT_ID");
      table = new Table ("METADATA_INDEXES");
      table.addColumn (column);
      extPoint.afterWrite (null, table, data);
      extPoint.batchCommitted (null);
      return p.getId ();
   }

   private void simulateProductUpdate (Product p)
   {
      productDao.update (p);
      CsvData data = new CsvData(DataEventType.UPDATE, new String[]{p.getId().toString(), p.getIdentifier()});

      Column column = new Column ("ID");
      Table table = new Table ("PRODUCTS");
      table.addColumn (column);
      Column col2 = new Column("IDENTIFIER");
      table.addColumn(col2);
      extPoint.afterWrite (null, table, data);
      extPoint.batchCommitted (null);
   }

   private void simulateProductDelete (Product p)
   {
      CsvData data = new CsvData(DataEventType.DELETE, null, new String[]{p.getId().toString(), p.getIdentifier()}, null);

      Column column = new Column ("ID");
      Table table = new Table ("PRODUCTS");
      table.addColumn (column);
      Column col2 = new Column("IDENTIFIER");
      table.addColumn(col2);
      extPoint.beforeWrite (null, table, data);
      extPoint.batchCommitted (null);
      productDao.delete (p);
   }

   private void simulateProductCollectionModification (Product p)
   {      
      CsvData data = new CsvData (DataEventType.DELETE, null, new String [] {p.getId ().toString ()}, null);

      Column column = new Column ("PRODUCTS_ID");
      Table table = new Table ("COLLECTION_PRODUCT");
      table.addColumn (column);
      extPoint.afterWrite (null, table, data);
      extPoint.batchCommitted (null);
   }
   
   @Test
   public void testReplicateUser () throws RootNotModifiableException
   {
      String cache_name = "user";
      String cache2_name = "userByName";
      String username = "user1";
      String uid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab0";
      User user;
      
      Cache cache = cacheManager.getCache (cache_name);
      Cache cache2 = cacheManager.getCache (cache2_name);
      
      user = userService.getUser (uid);
      Assert.assertNull (user);
      Assert.assertNull (cache.get (uid, User.class));
      user = userService.getUserByName (username);
      Assert.assertNull (user);
      Assert.assertNull (cache2.get (username, User.class));

      simulateUserReplication (username, uid);
      
      user = userDao.read (uid);
      Assert.assertNotNull (user);
      Assert.assertEquals (cache.get (uid, User.class), user);
      Assert.assertEquals (cache2.get (username, User.class), user);
      Assert.assertEquals (userService.getUser (uid), user);
      Assert.assertEquals (userService.getUserByName (username), user);
      
      Assert.assertNull (user.getFirstname ());
      user.setFirstname ("noname");
      simulateUserUpdate (user);

      user = userDao.read (uid);
      Assert.assertNotNull (user);
      Assert.assertEquals (user.getFirstname (), "noname");
      Assert.assertEquals (cache.get (uid, User.class).getFirstname (), "noname");
      Assert.assertEquals (cache2.get (username, User.class).getFirstname (), "noname");
      Assert.assertEquals (userService.getUser (uid).getFirstname (), "noname");
      Assert.assertEquals (userService.getUserByName (username).getFirstname (), "noname");
      
      simulateUserDelete (user);

      user = userDao.read (uid);
      Assert.assertNull (user);
      Assert.assertNull (cache.get (uid, User.class));
      Assert.assertNull (cache2.get (username, User.class));
      Assert.assertNull (userService.getUser (uid));
      Assert.assertNull (userService.getUserByName (username));
   }
   
   @Test
   public void testReplicateProducts ()
   {
      String cache_name = "products";
      String uid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab0";
      List<Product> products;
      
      Cache cache = cacheManager.getCache (cache_name);
      
      products = productService.getProducts (null, null, -1, -1);
      Assert.assertNotNull (products);
      Assert.assertTrue (products.size () > 0);
      Object key1 = Arrays.asList (null, null, -1, -1);
      Assert.assertEquals (cache.get (key1, List.class), products);
   
      simulateProductReplication (uid);
      
      Assert.assertNull (cache.get (key1, List.class));

      // restore cache
      products = productService.getProducts (null, null, -1, -1);
      Assert.assertNotNull (products);
      Assert.assertTrue (products.size () > 0);
      Assert.assertEquals (cache.get (key1, List.class), products);
      
      Product product = productDao.getProductByUuid (uid);
      Assert.assertNull (product.getFootPrint ());
      product.setFootPrint ("footprint");
      
      simulateProductUpdate (product);

      Assert.assertNull (cache.get (key1, List.class));

      // restore cache
      products = productService.getProducts (null, null, -1, -1);
      Assert.assertNotNull (products);
      Assert.assertTrue (products.size () > 0);
      Assert.assertEquals (cache.get (key1, List.class), products);

      simulateProductDelete (product);
      
      Assert.assertNull (cache.get (key1, List.class));
      
      products = productService.getProducts (null, null, -1, -1);
      Assert.assertNotNull (products);
      Assert.assertTrue (products.size () > 0);
      Assert.assertEquals (cache.get (key1, List.class), products);
            
      simulateProductCollectionModification (product);
      
      Assert.assertNull (cache.get (key1, List.class));
      
      products = productService.getProducts (null, null, -1, -1);
      Assert.assertNotNull (products);
      Assert.assertTrue (products.size () > 0);
      Assert.assertEquals (cache.get (key1, List.class), products);            
   }
   
   @Test
   public void testReplicateProduct ()
   {
      String cache_name = "product";
      String uid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab0";
      Long id;
      Product product;
      
      Cache cache = cacheManager.getCache (cache_name);
      
      id = new Long(productDao.count ());
      
      product = productService.getProduct (uid);
      Assert.assertNull (product);
      Assert.assertNull (cache.get (uid, Product.class));
      product = productService.getProduct (id);
      Assert.assertNull (product);
      Assert.assertNull (cache.get (id, Product.class));

      id = simulateProductReplication (uid);
      
      product = productDao.getProductByUuid (uid);
      Assert.assertNotNull (product);
      Assert.assertEquals (cache.get (uid, Product.class), product);
      Assert.assertEquals (cache.get (id, Product.class), product);
      Assert.assertEquals (productService.getProduct (uid), product);
      Assert.assertEquals (productService.getProduct (id), product);
      
      Assert.assertNull (product.getFootPrint ());
      product.setFootPrint ("footprint");
      simulateProductUpdate (product);

      product = productDao.getProductByUuid (uid);
      Assert.assertNotNull (product);
      Assert.assertEquals (product.getFootPrint (), "footprint");
      Assert.assertEquals (cache.get (uid, Product.class).getFootPrint (), "footprint");
      Assert.assertEquals (cache.get (id, Product.class).getFootPrint (), "footprint");
      Assert.assertEquals (productService.getProduct (uid).getFootPrint (), "footprint");
      Assert.assertEquals (productService.getProduct (id).getFootPrint (), "footprint");
      
      simulateProductCollectionModification (product);
      product = productDao.getProductByUuid (uid);
      Assert.assertNotNull (product);
      Assert.assertEquals (cache.get (uid, Product.class), product);
      Assert.assertEquals (cache.get (id, Product.class), product);
      Assert.assertEquals (productService.getProduct (uid), product);
      Assert.assertEquals (productService.getProduct (id), product);
      
      simulateProductDelete (product);

      product = productDao.getProductByUuid (uid);
      Assert.assertNull (product);
      Assert.assertNull (cache.get (uid, Product.class));
      Assert.assertNull (cache.get (id, Product.class));
      Assert.assertNull (productService.getProduct (uid));
      Assert.assertNull (productService.getProduct (id));
   }
   
   @Test
   public void testReplicateProductCount ()
   {
      String cache_name = "product_count";
      String uid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab0";
      int count;
      int baseCount;

      Object key1 = Arrays.asList (null, null);
      
      Cache cache = cacheManager.getCache (cache_name);
      
      baseCount = productService.count ();
      Assert.assertNotNull (baseCount);
      Assert.assertTrue (baseCount > 0);
      Assert.assertEquals (cache.get ("all", Integer.class).intValue (), baseCount);
      int filteredCount = productService.count (null);
      Assert.assertNotNull (filteredCount);
      Assert.assertTrue (filteredCount > 0);
      Assert.assertEquals (cache.get (key1, Integer.class).intValue (), filteredCount);
            
      simulateProductReplication (uid);
      
      count = productDao.count ();
      Assert.assertEquals (count, baseCount + 1);
      Assert.assertEquals (cache.get ("all", Integer.class).intValue (), count);
      Assert.assertEquals (productService.count (), count);

      Assert.assertNull (cache.get (key1, Integer.class));
            
      Product product = productDao.getProductByUuid (uid);
      Assert.assertNull (product.getFootPrint ());
      product.setFootPrint ("footprint");
      
      simulateProductUpdate (product);

      count = productDao.count ();
      Assert.assertEquals (count, baseCount + 1);
      Assert.assertEquals (cache.get ("all", Integer.class).intValue (), count);
      Assert.assertEquals (productService.count (), count);

      simulateProductDelete (product);

      count = productDao.count ();
      Assert.assertEquals (count, baseCount);
      Assert.assertEquals (cache.get ("all", Integer.class).intValue (), count);
      Assert.assertEquals (productService.count (), count);

      simulateProductCollectionModification (product);
      Assert.assertEquals (count, baseCount);
      Assert.assertEquals (cache.get ("all", Integer.class).intValue (), count);
      Assert.assertEquals (productService.count (), count);
   }

}