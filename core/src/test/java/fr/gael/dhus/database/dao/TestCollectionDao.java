package fr.gael.dhus.database.dao;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.util.CheckIterator;
import fr.gael.dhus.util.TestContextLoader;

@ContextConfiguration (
      locations = "classpath:fr/gael/dhus/spring/context-test.xml",
      loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestCollectionDao extends
      TestAbstractHibernateDao<Collection, String>
{

   @Autowired
   private CollectionDao dao;

   @Autowired
   private UserDao udao;

   @Autowired
   private ProductDao pdao;

   @Override
   protected HibernateDao<Collection, String> getHibernateDao ()
   {
      return dao;
   }

   @Override
   protected int howMany ()
   {
      return 6;
   }

   @Override
   public void create ()
   {
      String uuidPattern = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

      long p0id = 1;
      Product p0 = new Product ();
      p0.setId (p0id);
      p0.setUuid (uuidPattern + p0id);

      long p1id = 4;
      Product p1 = new Product ();
      p1.setId (p1id);
      p1.setUuid (uuidPattern + p1id);

      String userId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1";
      User user = new User ();
      user.setUUID (userId);
      user.setUsername ("riko");

      List<Product> products = Arrays.asList (p0, p1);

      String collectioName = "CollectionTestCreate";
      Collection collection = new Collection ();
      collection.setName (collectioName);
      collection.setDescription ("Unit Test create");
      collection.getProducts ().addAll (products);
      collection.getAuthorizedUsers ().add (user);

      Collection nc = dao.create (collection);
      Assert.assertNotNull (nc);
      Assert.assertEquals (nc.getName (), collectioName);

      Assert.assertEquals (nc.getProducts ().size (), 2);
      Assert.assertTrue (nc.getProducts ().containsAll (products));

      Assert.assertEquals (nc.getAuthorizedUsers ().size (), 1);
      Assert.assertTrue (nc.getAuthorizedUsers ().contains (user));
   }

   @Override
   @Test
   public void read ()
   {
      String id = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3";
      String name = "Japan";

      Collection c = dao.read (id);
      Assert.assertNotNull (c);
      Assert.assertEquals (c.getUUID (), id);
      Assert.assertEquals (c.getName (), name);

      Product p0 = new Product ();
      p0.setId (0L);
      p0.setUuid ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0");
      Product p1 = new Product ();
      p1.setId (1L);
      p1.setUuid ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1");
      Product p2 = new Product ();
      p2.setId (2L);
      p2.setUuid ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2");
      Assert.assertEquals (c.getProducts ().size (), 3);
      Assert.assertTrue (c.getProducts ().containsAll (
            Arrays.asList (p0, p1, p2)));

      User u0 = udao.read ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0");
      User u1 = udao.read ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1");
      User u2 = udao.read ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2");

      Set<User> authorizedUsers = c.getAuthorizedUsers ();
      Assert.assertEquals (authorizedUsers.size (), 3);
      Assert.assertTrue (authorizedUsers.contains (u0));
      Assert.assertTrue (authorizedUsers.contains (u1));
      Assert.assertTrue (authorizedUsers.contains (u2));
   }

   @Override
   public void update ()
   {
      String id = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5";
      String description = "Test update collection !";
      Product p = new Product ();
      p.setId (6L);
      p.setUuid ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa6");
      User u = new User ();
      u.setUUID ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3");
      u.setUsername ("babar");
      Collection collection = dao.read (id);

      collection.setDescription (description);
      collection.getProducts ().add (p);
      collection.getAuthorizedUsers ().add (u);
      dao.update (collection);

      collection = dao.read (id);
      Assert.assertEquals (collection.getDescription (), description);
      Assert.assertTrue (collection.getAuthorizedUsers ().contains (u));
      Assert.assertTrue (collection.getProducts ().contains (p));
   }

   @Override
   @Test(dependsOnMethods = "count")
   public void delete ()
   {
      String id = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1";
      User root = udao.getRootUser ();
      int expectedCount = dao.count () - 1;
      Collection collection = dao.read (id);
      Assert.assertNotNull (collection);

      List<Long> products = dao.getProductIds (id, null);
      List<User> users = dao.getAuthorizedUsers (collection);

      dao.delete (collection);
      Assert.assertNull (dao.read (id));
      Assert.assertEquals (dao.count (), expectedCount);

      // check existence of any linked objects.
      for (Long pid : products)
      {
         Assert.assertNotNull (pdao.read (pid));
      }
      for (User u : users)
      {
         Assert.assertNotNull (udao.read (u.getUUID ()));
      }
   }

   @Override
   public void deleteAll ()
   {
      dao.deleteAll ();
      Assert.assertEquals (dao.count (), 0);
   }

   @Override
   public void scroll ()
   {
      String hql = "WHERE uuid not like 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2'";
      Iterator<Collection> it = dao.scroll (hql, -1, -1).iterator ();
      Assert.assertTrue (CheckIterator.checkElementNumber (it, howMany () - 1));
   }

   @Override
   public void first ()
   {
      String hql = "FROM Collection WHERE uuid not like 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1' AND uuid not like 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5' ORDER BY uuid DESC";
      Collection collection = dao.first (hql);
      Assert.assertNotNull (collection);
      Assert.assertEquals (collection.getUUID (), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4");
   }

   @Test
   public void contains ()
   {
      long productId = 5;
      String collectionIdTrue = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2";
      String collectionIdFalse = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3";

      Assert.assertTrue (dao.contains (collectionIdTrue, productId));
      Assert.assertFalse (dao.contains (collectionIdFalse, productId));
   }

   @Override
   @Test
   public void count ()
   {
      // classic count
      Assert.assertEquals (dao.count (), howMany ());

      // count (User u)
      Assert.assertEquals (dao.count (udao.getRootUser ()), howMany ());
      Assert.assertEquals (dao.count (udao.read ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0")), 3);
   }

   @Test
   public void getAuthorizedCollections ()
   {
      String userId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3";
      List<String> collections = dao.getAuthorizedCollections (userId);
      Assert.assertNotNull (collections);
      Assert.assertEquals (collections.size (), 2);
      // check if collections contains collections {4; 5}
      Assert.assertTrue (collections.contains ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4"));
      Assert.assertTrue (collections.contains ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5"));
   }

   @Test
   public void getCollectionsOfProduct ()
   {
      long pid = 1;
      List<Collection> collections = dao.getCollectionsOfProduct (pid);
      Assert.assertNotNull (collections);
      Assert.assertEquals (collections.size (), 3);
      Assert.assertTrue (collections.contains (dao.read ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1")));
      Assert.assertTrue (collections.contains (dao.read ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3")));
      Assert.assertTrue (collections.contains (dao.read ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4")));
   }

   @Test
   public void getProductIds ()
   {
      String cid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1";
      String uid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2";
      User user = udao.read (uid);
      List<Long> pids = dao.getProductIds (cid, user);
      Assert.assertNotNull (pids);
      Assert.assertEquals (pids.size (), 5);
      Assert.assertTrue (pids.contains (0L));
      Assert.assertTrue (pids.contains (2L));
      Assert.assertTrue (pids.contains (3L));
      Assert.assertTrue (pids.contains (5L));
   }

   @Test (dependsOnMethods = {"contains"})
   public void removeProduct ()
   {
      String cid;
      long pid = 5;
      User user = udao.read ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2");
      Collection collection;

      cid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1";
      collection = dao.read (cid);
      Assert.assertTrue (dao.contains (cid, pid));
      dao.removeProduct (cid, pid, user);
      Assert.assertFalse (dao.contains (cid, pid));
   }

   @Test
   public void getCollectionByName ()
   {
      Assert.assertEquals(dao.getCollectionUUIDByName("Asia"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1" );
   }

   @Test (dependsOnMethods = "removeProduct")
   public void removeProducts ()
   {
      Long[] pids = { 0L, 3L };
      String cid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1";
      User user = udao.getRootUser ();

      for (Long pid : pids)
      {
         Assert.assertTrue (dao.contains (cid, pid));
      }
      dao.removeProducts (cid, pids, user);
      for (Long pid : pids)
      {
         Assert.assertFalse (dao.contains (cid, pid));
      }
   }
}
