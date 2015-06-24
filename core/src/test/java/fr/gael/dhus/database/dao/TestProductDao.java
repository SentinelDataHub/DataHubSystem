package fr.gael.dhus.database.dao;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.*;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product.Download;
import fr.gael.dhus.util.CheckIterator;
import fr.gael.dhus.util.TestContextLoader;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

@ContextConfiguration (
      locations = "classpath:fr/gael/dhus/spring/context-test.xml",
      loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestProductDao extends TestAbstractHibernateDao<Product, Long>
{

   @Autowired
   private ProductDao dao;

   @Autowired
   private UserDao udao;

   @Override
   protected HibernateDao<Product, Long> getHibernateDao ()
   {
      return dao;
   }

   @Override
   protected int howMany ()
   {
      return 7;
   }

   @Override
   public void create ()
   {
      String identifier = "test-create-product";
      String indexName = "index-name";
      String indexCategory = "category";
      String indexValue = "test";

      MetadataIndex mi = new MetadataIndex ();
      mi.setName (indexName);
      mi.setCategory (indexCategory);
      mi.setQueryable (null);
      mi.setValue (indexValue);

      Product product = new Product ();
      product.setIdentifier (identifier);
      product.setLocked (false);
      product.setProcessed (true);
      product.setIndexes (Arrays.asList (mi));
      try
      {
         product.setPath (new URL ("file:/titi/tata"));
      }
      catch (MalformedURLException e)
      {
         Assert.fail (e.getMessage (), e);
      }

      Product createdProduct = dao.create (product);
      Assert.assertNotNull (createdProduct);
      Assert.assertEquals (dao.count (), (howMany () + 1));
      Assert.assertEquals (createdProduct.getUuid (), product.getUuid ());

      List<MetadataIndex> indexes = createdProduct.getIndexes ();
      Assert.assertEquals (indexes.size (), 1);
      Assert.assertEquals (indexes.get (0), mi);
   }

   @Override
   public void read ()
   {
      Product p = dao.read (6L);
      Assert.assertNotNull (p);
      Assert.assertEquals (p.getIdentifier (), "prod6");
      Download dl = p.getDownload ();
      Map<String, String> checksums = dl.getChecksums ();
      Assert.assertEquals (checksums.get ("MD5"), "abc");
   }

   @Override
   public void update ()
   {
      String indexName = "testable";
      String productIdentifier = "test-prod-6";
      Long pid = Long.valueOf (6);

      Product product = dao.read (pid);
      product.setIdentifier (productIdentifier);
      List<MetadataIndex> indexes = product.getIndexes ();
      Assert.assertFalse (indexes.isEmpty ());
      for (MetadataIndex mi : indexes)
      {
         mi.setName (indexName);
      }
      dao.update (product);

      product = dao.read (pid);
      Assert.assertNotNull (product);
      Assert.assertEquals (product.getIdentifier (), productIdentifier);
      for (MetadataIndex mi : product.getIndexes ())
      {
         Assert.assertEquals (mi.getName (), indexName);
      }
   }

   private int countElements (final String table, final Long pid)
   {
      return dao.getHibernateTemplate ().execute (
            new HibernateCallback<Integer> ()
            {
               @Override
               public Integer doInHibernate (Session session)
                     throws HibernateException, SQLException
               {
                  String sql =
                        "SELECT count(*) FROM " + table +
                              " WHERE PRODUCT_ID = ?";
                  Query query = session.createSQLQuery (sql).setLong (0, pid);
                  return ((BigInteger) query.uniqueResult ()).intValue ();
               }
            });
   }

   @Override
   public void delete ()
   {
      Long pid = Long.valueOf (6);
      Product product = dao.read (pid);
      Assert.assertNotNull (product);
      Set<User> authorizedUsers = product.getAuthorizedUsers ();

      Assert.assertFalse (product.getIndexes ().isEmpty ());
      Assert.assertFalse (authorizedUsers.isEmpty ());

      dao.delete (product);
      Assert.assertNull (dao.read (pid));
      Assert.assertEquals (countElements ("METADATA_INDEXES", pid), 0);
      Assert.assertEquals (countElements ("CHECKSUMS", pid), 0);

      for (User user : authorizedUsers)
      {
         Assert.assertNotNull (udao.read (user.getId ()));
      }
   }

   @Override
   public void scroll ()
   {
      String hql = "WHERE processed IS TRUE";
      Iterator<Product> it = dao.scroll (hql, -1, -1).iterator ();
      Assert.assertTrue (CheckIterator.checkElementNumber (it, 3));
   }

   @Override
   public void first ()
   {
      String hql = "FROM Product WHERE processed IS FALSE ORDER BY id DESC";
      Product product = dao.first (hql);
      Assert.assertNotNull (product);
      Assert.assertEquals (product.getId ().intValue (), 4);
   }

   @Test
   public void exists ()
   {
      boolean bool;
      String path;
      try
      {
         path = "file:/home/lambert/test/prod0";
         bool = dao.exists (new URL (path));
         Assert.assertTrue (bool,
               "product with path '" + path + "' should be" + "exists");

         path = "file:/home/lambert/test/product999";
         bool = dao.exists (new URL (path));
         Assert.assertFalse (bool,
               "product with path '" + path + "' should be npt exists");
      }
      catch (MalformedURLException e)
      {
         Assert.fail ("Error: malformed URL", e);
      }
   }

   @Test
   public void getAuthorizedProduct ()
   {
      long uid;
      List<Long> expected;
      List<Long> products;

      uid = 0;
      expected = Arrays.asList (0L, 5L, 6L); // product 1 is in processing
      products = dao.getAuthorizedProducts (uid);
      Assert.assertEquals (products.size (), expected.size ());
      Assert.assertTrue (products.containsAll (expected));

      uid = 3;
      expected = Arrays.asList (0L, 5L); // product 3 is in processing
      products = dao.getAuthorizedProducts (uid);
      Assert.assertEquals (products.size (), expected.size ());
      Assert.assertTrue (products.containsAll (expected));
   }

   @Test
   public void getAuthorizedUsers ()
   {
      long pid;
      Product product = new Product ();
      List<User> expected;
      List<User> users;

      pid = 5;
      expected =
            Arrays.asList (emulateUser (0, "koko"), emulateUser (2, "toto"),
                  emulateUser (3, "babar"));
      product.setId (pid);
      users = dao.getAuthorizedUsers (product);

      Assert.assertEquals (users.size (), expected.size ());
      Assert.assertTrue (users.containsAll (expected));
   }

   private User emulateUser (long uid, String username)
   {
      User user = new User ();
      user.setId (uid);
      user.setUsername (username);
      return user;
   }

   @Test
   public void getNoCollectionProducts ()
   {
      long uid = 0;
      long expectedPid = 6;

      List<Product> products =
            dao.getNoCollectionProducts (emulateUser (uid, "koko"));
      Assert.assertEquals (products.size (), 1);
      Assert.assertEquals (products.get (0).getId ().intValue (), expectedPid);
   }

   @Test
   public void getOwerOfProduct ()
   {
      User expectedUser = new User ();
      expectedUser.setId (0L);
      expectedUser.setUsername ("koko");
      Product product = dao.read (0L);

      User user = dao.getOwnerOfProduct (product);
      Assert.assertNotNull (user);
      Assert.assertTrue (user.equals (expectedUser));

      product = dao.read (6L);
      user = dao.getOwnerOfProduct (product);
      Assert.assertNull (user);
   }

   @Test
   public void getProductByDownloadableFilename ()
   {
      fr.gael.dhus.database.object.Collection validCollection;
      fr.gael.dhus.database.object.Collection invalidCollection;
      Product product;

      validCollection = new Collection ();
      validCollection.setId (3L);
      validCollection.setName ("Japan");

      invalidCollection = new Collection ();
      invalidCollection.setId (4L);
      invalidCollection.setName ("China");

      product = dao.getProductByDownloadableFilename ("prod0", validCollection);
      Assert.assertNotNull (product);
      Assert.assertEquals (product.getId ().intValue (), 0);

      product = dao.getProductByDownloadableFilename ("prod6", validCollection);
      Assert.assertNull (product);

      product = dao.getProductByDownloadableFilename ("prod0", invalidCollection);
      Assert.assertNull (product);

      product = dao.getProductByDownloadableFilename ("prod6", null);
      Assert.assertNotNull (product);
      Assert.assertEquals (product.getId ().intValue (), 6);

      product = dao.getProductByDownloadableFilename (null, null);
      Assert.assertNull (product);
   }

   @Test
   public void getProductByOrigin ()
   {
      Product product = dao.getProductByOrigin ("space");
      Assert.assertNotNull (product);
      Assert.assertEquals (product.getId ().intValue (), 6);

      product = dao.getProductByOrigin ("unknown origin");
      Assert.assertNull (product);

      product = dao.getProductByOrigin (null);
      Assert.assertNull (product);
   }

   @Test
   public void getProductByPath ()
   {
      URL valid = null;
      URL invalid = null;
      try
      {
         valid = new URL ("file:/home/lambert/test/prod5");
         invalid = new URL ("file:/home/lambert/test/prod512");
      }
      catch (MalformedURLException e)
      {
         Assert.fail ("Malformed URL !", e);
      }

      Product product = dao.getProductByPath (valid);
      Assert.assertNotNull (product);
      Assert.assertEquals (product.getId ().intValue (), 5);

      product = dao.getProductByPath (invalid);
      Assert.assertNull (product);

      product = dao.getProductByPath (null);
      Assert.assertNull (product);
   }

   @Test
   public void getProductByUuid ()
   {
      String valid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa6";
      String invalid = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb6";
      User user = new User ();
      user.setId (0L);
      user.setUsername ("koko");

      Product product = dao.getProductByUuid (valid, user);
      Assert.assertNotNull (product);
      Assert.assertEquals (product.getId ().intValue (), 6);
      Assert.assertTrue (user.getRoles ().contains (Role.DATA_MANAGER) ||
            dao.getAuthorizedUsers (product).contains (user));

      product = dao.getProductByUuid (invalid, user);
      Assert.assertNull (product);

      product = dao.getProductByUuid (valid, null);
      Assert.assertNotNull (product);
      Assert.assertEquals (product.getId ().intValue (), 6);

      product = dao.getProductByUuid (null, null);
      Assert.assertNull (product);
   }

   @Test
   public void getProductByIngestionDate ()
   {
      Date date = new Date ();
      List<Product> products = dao.getProductsByIngestionDate (date, 0, 5);
      Assert.assertEquals (products.size (), 3);

      Calendar calendar =  Calendar.getInstance ();
      calendar.set (2014, Calendar.JUNE, 07, 0, 0, 0);
      date = calendar.getTime ();
      products = dao.getProductsByIngestionDate (date, 0, 5);
      Assert.assertEquals (products.size (), 1);
   }

   // TODO merge others test
   
   @Test (groups={"non-regression"})
   public void testChecksumUpdate () throws MalformedURLException
   {
      Product product = new Product ();
      product.setPath (new URL ("file:/product/path"));
      
      Download download = new Product.Download ();
      download.setPath ("/no/path/file");
      download.setSize (0L);
      download.setType ("application/octet-stream");
      download.setChecksums (
         Maps.newHashMap (ImmutableMap.of(
            "MD5", "54ABCDEF98765", 
            "SHA-1", "9876FEDCBA1234")));
      
      product.setDownload (download);
      
      
      // First create the defined product: 
      try
      {
         product = dao.create (product);
      }
      catch (Exception e)
      {
         Assert.fail ("Creation of product fails", e);
      }
      
      /**
       * Clear/putAll feature testing
       */
      product.getDownload ().getChecksums ().clear ();
      product.getDownload ().getChecksums ().putAll (
         Maps.newHashMap (ImmutableMap.of(
            "SHA-256", "4554ABCDEF98765", 
            "SHA-512", "ABDEFFE9876FEDCBA1234")));
      try
      {
         dao.update (product);
      }
      catch (Exception e)
      {
         Assert.fail ("Modifying checksums with map clear/put fails", e);
      }
      
      /**
       * Set feature testing
       */
      product.getDownload ().setChecksums (
         Maps.newHashMap (ImmutableMap.of(
            "MD5", "54ABCDEF98765", 
            "SHA-1", "9876FEDCBA1234")));
      try
      {
         dao.update (product);
      }
      catch (Exception e)
      {
         Assert.fail ("Modifying checksums with \"set\" fails", e);
      }
      
      /**
       * Remove residuals for this test
       */
      dao.delete (product);
      
   }
   
}
