package fr.gael.dhus.database.dao;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.util.CheckIterator;
import fr.gael.dhus.util.TestContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@ContextConfiguration (
      locations = "classpath:fr/gael/dhus/spring/context-test.xml",
      loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestCollectionDao extends
      TestAbstractHibernateDao<Collection, Long>
{

   @Autowired
   private CollectionDao dao;

   @Autowired
   private UserDao udao;

   @Autowired
   private ProductDao pdao;

   @Override
   protected HibernateDao<Collection, Long> getHibernateDao ()
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

      long userId = 1;
      User user = new User ();
      user.setId (userId);
      user.setUsername ("riko");

      List<Product> products = Arrays.asList (p0, p1);
      Collection parent = dao.read (0L);

      String collectioName = "CollectionTestCreate";
      Collection collection = new Collection ();
      collection.setName (collectioName);
      collection.setDescription ("Unit Test create");
      collection.setParent (parent);
      collection.getProducts ().addAll (products);
      collection.getAuthorizedUsers ().add (user);

      Collection nc = dao.create (collection);
      Assert.assertNotNull (nc);
      Assert.assertEquals (nc.getName (), collectioName);

      Assert.assertNotNull (nc.getParent ());
      Assert.assertEquals (nc.getParent ().getName (), parent.getName ());

      Assert.assertEquals (nc.getProducts ().size (), 2);
      Assert.assertTrue (nc.getProducts ().containsAll (products));

      Assert.assertEquals (nc.getAuthorizedUsers ().size (), 1);
      Assert.assertTrue (nc.getAuthorizedUsers ().contains (user));
   }

   @Override
   @Test
   public void read ()
   {
      Long id = 3L;
      String name = "Japan";

      Collection c = dao.read (id);
      Assert.assertNotNull (c);
      Assert.assertEquals (c.getId (), id);
      Assert.assertEquals (c.getName (), name);

      Assert.assertNotNull (c.getParent ());
      Assert.assertEquals (c.getParent ().getName (), "Asia");

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

      User u0 = udao.read (0L);
      User u1 = udao.read (1L);
      User u2 = udao.read (2L);

      Set<User> authorizedUsers = c.getAuthorizedUsers ();
      Assert.assertEquals (authorizedUsers.size (), 3);
//      Assert.assertTrue (c.getAuthorizedUsers ().containsAll (
//         Arrays.asList (u0, u1, u2)));
      Assert.assertTrue (authorizedUsers.contains (u0));
      Assert.assertTrue (authorizedUsers.contains (u1));
      Assert.assertTrue (authorizedUsers.contains (u2));

      Assert.assertEquals (c.getSubCollections ().size (), 0);
   }

   @Override
   public void update ()
   {
      Long id = 5L;
      String description = "Test update collection !";
      Product p = new Product ();
      p.setId (6L);
      p.setUuid ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa6");
      User u = new User ();
      u.setId (3L);
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
   public void delete ()
   {
      Long id = 1L;
      User root = udao.getRootUser ();
      Collection collection = dao.read (id);
      Assert.assertNotNull (collection);

      List<Collection> subCollections = dao.getSubCollections (id, root);
      List<Product> products = dao.getAuthorizedProducts (root, collection);
      List<User> users = dao.getAuthorizedUsers (collection);
      Collection parent = dao.getParent (collection);

      dao.delete (collection);
      Assert.assertNull (dao.read (id));
      Assert.assertNotNull (dao.read (parent.getId ()));

      int expectedCount = 3;
      Assert.assertEquals (dao.count (), expectedCount);
      for (Collection sub : subCollections)
      {
         Assert.assertNull (dao.read (sub.getId ()));
      }

      for (Product p : products)
      {
         Assert.assertNotNull (pdao.read (p.getId ()));
      }

      for (User u : users)
      {
         Assert.assertNotNull (udao.read (u.getId ()));
      }
   }

   @Override
   public void deleteAll ()
   {
      dao.deleteAll ();
      Assert.assertEquals (dao.count (), 1);
      Assert.assertNotNull (dao.getRootCollection ());
   }

   @Override
   public void scroll ()
   {
      String hql = "WHERE id > 2";
      Iterator<Collection> it = dao.scroll (hql, -1, -1).iterator ();
      Assert.assertTrue (CheckIterator.checkElementNumber (it, 3));
   }

   @Override
   public void first ()
   {
      String hql = "FROM Collection WHERE id > 1 AND id < 5 ORDER BY id DESC";
      Collection collection = dao.first (hql);
      Assert.assertNotNull (collection);
      Assert.assertEquals (collection.getId ().intValue (), 4);
   }

   @Test (dependsOnMethods = "contains")
   public void addProduct ()
   {
      long productId = 0;
      long collectionId = 5;
      long collectionParentId = 2;

      Assert.assertFalse (dao.contains (collectionId, productId));
      dao.addProduct (collectionId, productId);
      Assert.assertTrue (dao.contains (collectionId, productId));
      Assert.assertTrue (dao.contains (collectionParentId, productId));
   }

   @Test (dependsOnMethods = "addProduct")
   public void addProducts ()
   {
      Long[] productIds = { 0L, 2L, 5L };
      long collectionId = 5;
      long collectionParentId = 2;

      for (Long pid : productIds)
      {
         Assert.assertFalse (dao.contains (collectionId, pid));
      }
      dao.addProducts (collectionId, productIds);
      for (Long pid : productIds)
      {
         Assert.assertTrue (dao.contains (collectionParentId, pid));
      }
   }

   @Test
   public void contains ()
   {
      long productId = 5;
      long collectionIdTrue = 2;
      long collectionIdFalse = 3;

      Assert.assertTrue (dao.contains (collectionIdTrue, productId));
      Assert.assertFalse (dao.contains (collectionIdFalse, productId));
   }

   @Override
   public void count ()
   {
      // classic count
      Assert.assertEquals (dao.count (), howMany ());

      // count (User u)
      Assert.assertEquals (dao.count (udao.getRootUser ()), howMany ());
      Assert.assertEquals (dao.count (udao.read (0L)), 3);
   }

   @Test
   public void countAuthorizedProducts ()
   {
      long collectionId = 1;
      long userId = 2;
      String filter = "p.processed IS TRUE AND p.locked IS FALSE";
      Collection collection = dao.read (collectionId);
      User user = udao.read (userId);

      Assert.assertEquals (dao.countAuthorizedProducts (user, collection), 4);
      Assert.assertEquals (
            dao.countAuthorizedProducts (user, collection, filter), 2);
      Assert.assertEquals (dao.countAuthorizedProducts (null, collection), 5);
      Assert.assertEquals (dao.countAuthorizedProducts (user, null), 4);
      Assert
            .assertEquals (dao.countAuthorizedProducts (null, null), 7);
   }

   @Test
   public void countAuthorizedSubCollection ()
   {
      long collectionId = 1;
      long userId = 2;
      String filter = "c.name LIKE 'C%'";
      Collection collection = dao.read (collectionId);
      User user = udao.read (userId);

      Assert.assertEquals (
            dao.countAuthorizedSubCollections (user, collection), 2);
      Assert.assertEquals (
            dao.countAuthorizedSubCollections (user, collection, filter), 0);
      Assert.assertEquals (
            dao.countAuthorizedSubCollections (null, collection), 2);
      Assert.assertEquals (dao.countAuthorizedSubCollections (user, null), 1);
      Assert.assertEquals (dao.countAuthorizedSubCollections (null, null), 2);
   }

   @Test
   public void getAllSubCollection ()
   {
      Collection collection = dao.read (0L);
      List<Collection> subCollections = dao.getAllSubCollection (collection);
      Assert.assertNotNull (subCollections);
      Assert.assertEquals (subCollections.size (), 2);
      // check if subCollections contains collection{1; 2}
      Assert.assertTrue (subCollections.contains (dao.read (1L)));
      Assert.assertTrue (subCollections.contains (dao.read (2L)));

      collection = dao.read (2L);
      subCollections = dao.getAllSubCollection (collection);
      Assert.assertNotNull (subCollections);
      Assert.assertEquals (subCollections.size (), 1);
      // check if subCollections contains collection{5}
      Assert.assertTrue (subCollections.contains (dao.read (5L)));
   }

   @Test
   public void getAuthorizedCollections ()
   {
      long userId = 3;
      List<Long> collections = dao.getAuthorizedCollections (userId);
      Assert.assertNotNull (collections);
      Assert.assertEquals (collections.size (), 2);
      // check if collections contains collections {4; 5}
      Assert.assertTrue (collections.contains (4L));
      Assert.assertTrue (collections.contains (5L));
   }

   @Test (dependsOnMethods = "countAuthorizedProducts")
   public void getAuthorizedProducts ()
   {
      long collectionId = 1;
      long userId = 2;
      String filter = "";
      String orderBy = "p.identifier DESC";
      int skip = 1;
      int top = 2;
      Collection collection = dao.read (collectionId);
      User user = udao.read (userId);

      List<Product> products = dao.getAuthorizedProducts (user, collection);
      Product p0 = pdao.read (0L);
      Product p5 = pdao.read (5L);
      Product p6 = pdao.read (6L);

      Assert.assertNotNull (products);
      Assert.assertEquals (products.size (), 2);
      Assert.assertTrue (products.contains (p0));
      Assert.assertTrue (products.contains (p5));

      products =
            dao.getAuthorizedProducts (user, null, filter, orderBy, skip, top);
      Assert.assertNotNull (products);
      Assert.assertEquals (products.size (), 1);
      Assert.assertTrue (products.contains (p0));

      products = dao.getAuthorizedProducts (null, collection);
      Assert.assertNotNull (products);
      Assert.assertEquals (products.size (), 2);
      Assert.assertTrue (products.contains (p0));
      Assert.assertTrue (products.contains (p5));

      products = dao.getAuthorizedProducts (null, null, filter, null, 0, 5);
      Assert.assertNotNull (products);
      Assert.assertEquals (products.size (), 3);
      Assert.assertTrue (products.contains (p0));
      Assert.assertTrue (products.contains (p5));
      Assert.assertTrue (products.contains (p6));
   }

   @Test (dependsOnMethods = "countAuthorizedSubCollection")
   public void getAuthorizedSubCollection ()
   {
      long uid = 2;
      long cid = 1;
      User user = udao.read (uid);
      Collection collection = dao.read (cid);
      String filter = "";
      String order = "sub.name";
      int top = 10;
      int skip = 0;

      List<Collection> collections =
            dao.getAuthorizedSubCollections (user, collection, filter, order,
                  skip, top);
      Assert.assertNotNull (collections);
      Assert.assertEquals (collections.size (), 2);
      Assert.assertEquals (collections.get (0), dao.read (4L));

      cid = 2;
      collection = dao.read (cid);
      collections =
            dao.getAuthorizedSubCollections (user, collection, filter, order,
                  skip, top);
      Assert.assertNotNull (collections);
      Assert.assertEquals (collections.size (), 0);
   }

   @Test
   public void getCollectionsOfProduct ()
   {
      long pid = 1;
      List<Collection> collections = dao.getCollectionsOfProduct (pid);
      Assert.assertNotNull (collections);
      Assert.assertEquals (collections.size (), 3);
      Assert.assertTrue (collections.contains (dao.read (1L)));
      Assert.assertTrue (collections.contains (dao.read (3L)));
      Assert.assertTrue (collections.contains (dao.read (4L)));
   }

   @Test (dependsOnMethods = { "isRoot", "getRootCollection", "read" })
   public void getParent ()
   {
      Collection collection = dao.read (1L);
      Collection parentCollection = dao.getParent (collection);
      Assert.assertNotNull (parentCollection);
      Assert.assertTrue (dao.isRoot (parentCollection));

      collection = dao.read (5L);
      parentCollection = dao.getParent (collection);
      Assert.assertNotNull (parentCollection);
      Collection actual = dao.read (2L);
      Assert.assertEquals (parentCollection, actual);

      collection = dao.getRootCollection ();
      parentCollection = dao.getParent (collection);
      Assert.assertNull (parentCollection);
   }

   @Test
   public void getProductIds ()
   {
      long cid = 1;
      long uid = 2;
      User user = udao.read (uid);
      List<Long> pids = dao.getProductIds (cid, user);
      Assert.assertNotNull (pids);
      Assert.assertEquals (pids.size (), 4);
      Assert.assertTrue (pids.contains (0L));
      Assert.assertTrue (pids.contains (2L));
      Assert.assertTrue (pids.contains (3L));
      Assert.assertTrue (pids.contains (5L));
   }

   @Test
   public void getRootCollection ()
   {
      Collection collection = dao.getRootCollection ();
      Assert.assertNotNull (collection);
      Assert.assertEquals (collection.getName (), "#.root");
      Assert.assertEquals (collection.getId (), Long.valueOf (0));
      Assert.assertNull (collection.getParent ());
   }

   @Test
   public void getSubCollections ()
   {
      User user = udao.read (2L);
      List<Collection> collections = dao.getSubCollections (1L, user);
      Assert.assertNotNull (collections);
      Assert.assertEquals (collections.size (), 2);
      Assert.assertTrue (collections.contains (dao.read (3L)));
      Assert.assertTrue (collections.contains (dao.read (4L)));
   }

   @Test
   public void hasChildrenCollection ()
   {
      User user = udao.read (2L);
      Assert.assertTrue (dao.hasChildrenCollection (1L, user));
      Assert.assertFalse (dao.hasChildrenCollection (2L, user));
      Assert.assertFalse (dao.hasChildrenCollection (4L, user));
   }

   @Test
   public void hasViewableCollection ()
   {
      Collection collection = dao.read (2L);
      User user = udao.read (3L);
      Assert.assertTrue (dao.hasViewableCollection (collection, user));

      user = udao.read (2L);
      Assert.assertFalse (dao.hasViewableCollection (collection, user));
   }

   @Test
   public void isRoot ()
   {
      Collection collection = dao.read (0L);
      Assert.assertTrue (dao.isRoot (collection));

      collection = dao.read (5L);
      Assert.assertFalse (dao.isRoot (collection));
   }

   @Test (dependsOnMethods = { "contains", "getAllSubCollection" })
   public void removeProduct ()
   {
      long cid;
      long pid = 5;
      User user = udao.read (2L);
      Collection collection;

      cid = 1;
      collection = dao.read (cid);
      Assert.assertEquals (
            dao.getAuthorizedProducts (user, collection).size (), 2);
      dao.removeProduct (cid, pid, user);

      Assert.assertEquals (
            dao.getAuthorizedProducts (user, collection).size (), 1);
      Assert.assertFalse (dao.contains (cid, pid));
      for (Collection child : dao.getAllSubCollection (collection))
      {
         Assert.assertFalse (dao.contains (child.getId (), pid));
      }
   }

   @Test (dependsOnMethods = "removeProduct")
   public void removeProducts ()
   {
      Long[] pids = { 0L, 3L };
      Long cid = 1L;
      User user = udao.getRootUser ();
      dao.removeProducts (cid, pids, user);
      for (Long pid : pids)
      {
         Assert.assertFalse (dao.contains (cid, pid));
      }

      for (Collection child : dao.getAllSubCollection (dao.read (cid)))
      {
         for (Long pid : pids)
         {
            Assert.assertFalse (dao.contains (child.getId (), pid));
         }
      }
   }
}
