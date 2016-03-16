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

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.util.TestContextLoader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedSet;

@ContextConfiguration (
      locations = { "classpath:fr/gael/dhus/spring/context-test.xml",
            "classpath:fr/gael/dhus/spring/context-security-test.xml" },
      loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestCacheProductService
      extends AbstractTransactionalTestNGSpringContextTests
{
   private static final Logger LOGGER =
         Logger.getLogger (TestCacheProductService.class);

   @Autowired
   private ProductService productService;

   @Autowired
   private CacheManager cacheManager;

   private Product productTest;

   @BeforeClass
   public void setUp () throws MalformedURLException
   {
      initProductTest ();
      authenticate ();
   }

   private void authenticate ()
   {
      String name = "userTest";
      Set<GrantedAuthority> roles = new HashSet<> ();
      roles.add (new SimpleGrantedAuthority (Role.DOWNLOAD.getAuthority ()));
      roles.add (new SimpleGrantedAuthority (Role.SEARCH.getAuthority ()));
      roles.add (
            new SimpleGrantedAuthority (Role.DATA_MANAGER.getAuthority ()));

      SandBoxUser user = new SandBoxUser (name, name, true, 0, roles);
      Authentication auth = new UsernamePasswordAuthenticationToken (
            user, user.getPassword (), roles);
      SecurityContextHolder.getContext ().setAuthentication (auth);

      logger.info ("userTest roles: " + auth.getAuthorities ());
   }

   @Test
   public void testIndexesCache ()
   {
      // initialize variables
      Long productId1 = 1L;
      String uuid1 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1";

      Long productId2 = 2L;
      String uuid2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2";

      String productsCacheName = "products";
      String productIndexCacheName = "indexes";

      MetadataIndex mi = new MetadataIndex ("a", "b", "c", "d", "e");
      List<MetadataIndex> index = new ArrayList<> (5);
      index.add (mi);

      // validate cache
      Cache pCache = cacheManager.getCache (productsCacheName);
      Cache iCache = cacheManager.getCache (productIndexCacheName);

      Assert.assertNull (pCache.get (uuid1, Product.class));
      Assert.assertNull (pCache.get (uuid2, Product.class));
      Assert.assertNull (iCache.get (productId1, List.class));
      Assert.assertNull (iCache.get (productId2, List.class));

      // Cacheable
      Product product1 = productService.getProduct (uuid1);
      Assert.assertEquals (pCache.get (uuid1, Product.class), product1);
      Assert.assertNull (pCache.get (uuid2));

      Product product2 = productService.getProduct (uuid2);
      Assert.assertEquals (pCache.get (uuid2, Product.class), product2);

      List<MetadataIndex> index1 = productService.getIndexes (productId1);
      Assert.assertTrue (
            equalCollection (iCache.get (productId1, List.class), index1));
      Assert.assertNull (iCache.get (productId2));

      List<MetadataIndex> index2 = productService.getIndexes (productId2);
      Assert.assertTrue (
            equalCollection (iCache.get (productId2, List.class), index2));

      // CacheEvict
      productService.setIndexes (productId1, index);
      Assert.assertNull (iCache.get (productId1));
      Assert.assertNotNull (iCache.get (productId2));

      productService.getIndexes (productId1); // re-cached
      productService.systemDeleteProduct (productId2);
      Assert.assertNull (iCache.get (productId2));
      Assert.assertNotNull (iCache.get (productId1));
      Assert.assertNull (pCache.get (uuid1));
      Assert.assertNull (pCache.get (uuid2));
   }

   @Test
   public void testProductCountCache ()
   {
      fr.gael.dhus.database.object.Collection c =
            new fr.gael.dhus.database.object.Collection ();
      c.setId (1L);
      c.setName ("Asia");
      String cache_name = "product_count";
      String filter = "prod_";
      Object filter_collection_key = Arrays.asList (filter, c.getId ());
      Object filter_key = Arrays.asList (filter, null);
      Object all_key = "all";
      Cache cache;

      // count (Collection, String)
      Integer expected = productService.count (c, filter);
      cache = cacheManager.getCache (cache_name);
      Assert.assertEquals (
            cache.get (filter_collection_key, Integer.class), expected);
      expected = productService.count (null, filter);
      Assert.assertEquals (cache.get (filter_key, Integer.class), expected);

      // count (String)
      clearCache ();
      expected = productService.count (filter);
      Assert.assertEquals (cache.get (filter_key, Integer.class), expected);

      // count (String, Long)
      clearCache ();
      expected = productService.count (filter, c.getId ());
      Assert.assertEquals (
            cache.get (filter_collection_key, Integer.class), expected);

      // countAuthorizedProducts ()
      expected = productService.countAuthorizedProducts ();
      Assert.assertEquals (cache.get (all_key, Integer.class), expected);

      // addProduct (Product)
      expected = expected + 1;
      productService.addProduct (productTest);
      Assert.assertNull (cache.get (filter_collection_key, Integer.class));
      Assert.assertNull (cache.get (filter_key, Integer.class));
      Assert.assertEquals (cache.get (all_key, Integer.class), expected);

      // addProduct (URL, User, List, String, Scanner, FileScannerWrapper)
      // TODO too hard to simulate the call ProductService.processProduct

      // deleteProduct (Long)
      expected = productService.countAuthorizedProducts () - 1;
      productService.deleteProduct (7L);
      Assert.assertEquals (cache.get (all_key, Integer.class), expected);

      // systemDeleteProduct (Long)
      expected = productService.countAuthorizedProducts () - 1;
      productService.systemDeleteProduct (6L);
      Assert.assertEquals (cache.get (all_key, Integer.class), expected);

      // processUnprocessed
      productService.countAuthorizedProducts ();
      productService.count (filter);
      productService.count (filter, c.getId ());
      productService.processUnprocessed (false);
      Assert.assertNull (cache.get (all_key));
      Assert.assertNull (cache.get (filter_key));
      Assert.assertNull (cache.get (filter_collection_key));
   }

   @Test
   public void testProductCache () throws MalformedURLException
   {
      String cache_name = "product";
      String uuid;
      Long pid;
      Product product;

      // systemGetProduct (Long)
      pid = 0L;
      product = productService.systemGetProduct (pid);
      Cache cache = cacheManager.getCache (cache_name);
      Assert.assertNotNull (cache);
      Assert.assertEquals (cache.get (pid, Product.class), product);
      // getProduct (Long)
      pid = 1L;
      product = productService.getProduct (pid);
      Assert.assertEquals (cache.get (pid, Product.class), product);
      // getProductToDownload (Long)
      product = productService.getProductToDownload (pid);
      Assert.assertEquals (product, cache.get (pid, Product.class));

      // systemGetProduct (String)
      uuid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4";
      product = productService.systemGetProduct (uuid);
      Assert.assertEquals (cache.get (uuid, Product.class), product);
      // getProduct (String)
      product = productService.getProduct (uuid);
      Assert.assertEquals (product, cache.get (uuid, Product.class));
      // getProduct (String, User)
      product = productService.getProduct (uuid);
      Assert.assertEquals (product, cache.get (uuid, Product.class));

      // getProduct (URL)
      URL path = new URL ("file:/home/lambert/test/prod6");
      product = productService.getProduct (path);
      Assert.assertEquals (cache.get (path, Product.class), product);

      // addProduct (Product)
      productService.addProduct (productTest);
      Assert.assertNull (cache.get (pid, Product.class));
      Assert.assertNull (cache.get (uuid, Product.class));
      Assert.assertNull (cache.get (path, Product.class));

      // addProduct (URL, User, List, String, Scanner, FileScannerWrapper)
      // TODO too hard to simulate the call ProductService.processProduct

      // systemDeleteProduct (Long)
      product = productService.getProduct (0L);
      productService.systemDeleteProduct (product.getId ());
      Assert.assertNull (cache.get (product.getId (), Product.class));
      Assert.assertNull (cache.get (product.getUuid (), Product.class));
      Assert.assertNull (cache.get (product.getPath (), Product.class));

      // deleteProduct (Long)
      product = productService.getProduct (5L);
      productService.systemDeleteProduct (product.getId ());
      Assert.assertNull (cache.get (product.getId (), Product.class));
      Assert.assertNull (cache.get (product.getUuid (), Product.class));
      Assert.assertNull (cache.get (product.getPath (), Product.class));
   }

   @Test
   public void testProductsCache ()
   {
      String cache_name = "products";
      List<Product> products;

      // getProducts (Collection, String, String, int, int)
      products = productService.getProducts (
            null, null, null, -1, -1);
      Cache cache = cacheManager.getCache (cache_name);
      Object key1 = Arrays.asList (null, null, null, -1, -1);
      Assert.assertEquals (cache.get (key1, List.class), products);

      // getProducts (List<Long>)
      Object key2 = Arrays.asList (0L, 5L, 6L, 7L);
      products = productService.getProducts ((List<Long>) key2);
      Assert.assertEquals (cache.get (key2, List.class), products);

      // addProduct (Product)
      productService.addProduct (productTest);
      Assert.assertNull (cache.get (key1, List.class));
      Assert.assertNull (cache.get (key2, List.class));

      // addProduct (URL, User, List, String, Scanner, FileScannerWrapper)
      // TODO too hard to simulate the call ProductService.processProduct

      // deleteProduct (Long)
      productService.getProducts ((List<Long>) key2);
      productService.deleteProduct (0L);
      Assert.assertNull (cache.get (key2, List.class));

      // systemDeleteProduct (Long)
      productService.getProducts ((List<Long>) key2);
      productService.systemDeleteProduct (5L);
      Assert.assertNull (cache.get (key2, List.class));
   }

   @BeforeMethod
   private void clearCache ()
   {
      LOGGER.info ("### clearing cache for test.");
      for (String cache_name : cacheManager.getCacheNames ())
      {
         cacheManager.getCache (cache_name).clear ();
      }
   }

   /**
    * Returns {@code true} if the two specified collections have all
    * elements in common.
    * <p/>
    * <p>Care must be exercised if this method is used on collections that
    * do not comply with the general contract for {@code Collection}.
    * Implementations may elect to iterate over either collection and test
    * for containment in the other collection (or to perform any equivalent
    * computation).  If either collection uses a nonstandard equality test
    * (as does a {@link SortedSet} whose ordering is not <em>compatible with
    * equals</em>, or the key set of an {@link IdentityHashMap}), both
    * collections must use the same nonstandard equality test, or the
    * result of this method is undefined.
    * <p/>
    * <p>Care must also be exercised when using collections that have
    * restrictions on the elements that they may contain. Collection
    * implementations are allowed to throw exceptions for any operation
    * involving elements they deem ineligible. For absolute safety the
    * specified collections should contain only elements which are
    * eligible elements for both collections.
    * <p/>
    * <p>Note that it is permissible to pass the same collection in both
    * parameters, in which case the method will return {@code true}.
    *
    * @param c1 a collection
    * @param c2 a collection
    * @return {@code true} if the two specified collections have all
    * elements in common.
    * @throws NullPointerException if either collection is {@code null}.
    * @throws NullPointerException if one collection contains a {@code null}
    *                              element and {@code null} is not an eligible element for the other collection.
    *                              (<a href="Collection.html#optional-restrictions">optional</a>)
    * @throws ClassCastException   if one collection contains an element that is
    *                              of a type which is ineligible for the other collection.
    *                              (<a href="Collection.html#optional-restrictions">optional</a>)
    */
   private boolean equalCollection (final Collection<?> c1,
         final Collection<?> c2)
   {
      // The collection to be used for contains(). Preference is given to
      // the collection who's contains() has lower O() complexity.
      Collection<?> contains = c2;
      // The collection to be iterated. If the collections' contains() impl
      // are of different O() complexity, the collection with slower
      // contains() will be used for iteration. For collections who's
      // contains() are of the same complexity then best performance is
      // achieved by iterating the smaller collection.
      Collection<?> iterate = c1;

      int c1size = c1.size ();
      int c2size = c2.size ();
      if (c1size == 0 && c2size == 0)
      {
         // Both collections are empty.
         return true;
      }

      if (c1size != c2size)
      {
         return false;
      }

      // Performance optimization cases. The heuristics:
      //   1. Generally iterate over c1.
      //   2. If c1 is a Set then iterate over c2.
      //   3. If either collection is empty then result is always true.
      //   4. Iterate over the smaller Collection.
      if (c1 instanceof Set || c1 instanceof RandomAccess)
      {
         // Use c1 for contains as a Set's contains() is expected to perform
         // better than O(N/2)
         iterate = c2;
         contains = c1;
      } else if (!(c2 instanceof Set || c2 instanceof RandomAccess))
      {
         iterate = c2;
         contains = c1;
      }

      for (Object e : iterate)
      {
         if (!contains.contains (e))
         {
            // Found an uncommon element.
            return false;
         }
      }

      // No uncommon elements were found.
      return true;
   }

   private void initProductTest () throws MalformedURLException
   {
      productTest = new Product ();
      productTest.setUuid ("testaaaaaaaaaaaaaaaaaaaaaaaaaaa0");
      productTest.setIdentifier ("test");
      productTest.setPath (
            new URL ("file://home/lambert/dhus/productTest.zip"));
      productTest.setLocked (false);
      productTest.setProcessed (true);
      productTest.setOrigin ("space");
   }
}
