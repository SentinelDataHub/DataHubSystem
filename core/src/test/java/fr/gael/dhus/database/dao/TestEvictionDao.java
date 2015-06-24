package fr.gael.dhus.database.dao;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Eviction;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.eviction.EvictionStrategy;
import fr.gael.dhus.util.CheckIterator;
import fr.gael.dhus.util.TestContextLoader;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.testng.Assert.*;

@ContextConfiguration (
      locations = "classpath:fr/gael/dhus/spring/context-test.xml",
      loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestEvictionDao extends TestAbstractHibernateDao<Eviction, Long>
{

   @Autowired
   private EvictionDao dao;

   @Autowired
   private ProductDao pdao;

   private Set<Product> products;

   @Override
   protected HibernateDao<Eviction, Long> getHibernateDao ()
   {
      return dao;
   }

   @Override
   protected int howMany ()
   {
      return 1;
   }

   private Set<Product> getProducts ()
   {
      if (products != null) return products;

      long pid = 6L;
      products = new HashSet<Product> ();
      products.add (pdao.read (pid));
      return products;
   }

   private Eviction createEviction (int period, int disk, int number,
         EvictionStrategy strategy)
   {
      Eviction eviction = new Eviction ();
      eviction.setKeepPeriod (period);
      eviction.setMaxDiskUsage (disk);
      eviction.setMaxProductNumber (number);
      eviction.setStrategy (strategy);
      eviction.setProducts (getProducts ());
      return eviction;
   }

   @Override
   @Test (dependsOnMethods = "read")
   public void create ()
   {
      int value = 42;
      EvictionStrategy strategy = EvictionStrategy.LRU;
      Eviction eviction = createEviction (value, value, value, strategy);

      eviction = dao.create (eviction);
      eviction = dao.read (eviction.getId ());
      assertEquals (dao.count (), (howMany () + 1));
      assertNotNull (eviction);
      assertEquals (eviction.getKeepPeriod (), value);
      assertEquals (eviction.getMaxDiskUsage (), value);
      assertEquals (eviction.getMaxProductNumber (), value);
      assertEquals (eviction.getProducts (), getProducts ());
      assertEquals (eviction.getStrategy (), strategy);
   }

   @Override
   @Test
   public void read ()
   {
      Eviction eviction = dao.read (1L);
      assertNotNull (eviction);
      assertEquals (eviction.getId (), Long.valueOf (1));
      assertEquals (eviction.getKeepPeriod (), 10);
      assertEquals (eviction.getMaxDiskUsage (), 80);
      assertEquals (eviction.getMaxProductNumber (), 1000);
      assertEquals (eviction.getProducts ().size (), 0);
      assertEquals (eviction.getStrategy (), EvictionStrategy.NONE);
   }

   @Override
   @Test (dependsOnMethods = { "read", "getEviction" })
   public void update ()
   {
      long id = 1;
      int expected = 50;
      Product p2 = new Product ();
      p2.setId (6L);
      p2.setUuid ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa6");
      Eviction eviction = dao.read (id);

      eviction.setMaxProductNumber (expected);
      assertTrue (eviction.getProducts ().isEmpty ());
      eviction.getProducts ().add (p2);
      dao.update (eviction);

      eviction = dao.read (id);
      products = eviction.getProducts ();
      assertEquals (eviction.getMaxProductNumber (), expected);
      assertTrue (products.contains (p2));

      // second update method
      dao.update (EvictionStrategy.LRU, 5, 8);
      eviction = dao.getEviction ();
      assertEquals (eviction.getStrategy (), EvictionStrategy.LRU);
      assertEquals (eviction.getKeepPeriod (), 5);
      assertEquals (eviction.getMaxDiskUsage (), 8);
   }

   @Override
   @Test (dependsOnMethods = { "read", "create" })
   public void delete ()
   {
      long id = 1;
      Eviction eviction = dao.read (id);
      Set<Product> ps = eviction.getProducts ();
      Hibernate.initialize (ps);

      dao.delete (eviction);
      assertEquals (dao.count (), (howMany () - 1));
      assertNull (dao.read (id));
      for (Product p : ps)
      {
         assertNotNull (pdao.read (p.getId ()));
      }
   }

   @Override
   @Test (dependsOnMethods = { "create" })
   public void scroll ()
   {
      dao.create (createEviction (2, 3, 5, EvictionStrategy.NONE));
      String hql = "WHERE keepPeriod <= 8";
      Iterator<Eviction> it = dao.scroll (hql, -1, -1).iterator ();
      assertTrue (CheckIterator.checkElementNumber (it, 1));
   }

   @Override
   public void first ()
   {
      dao.create (createEviction (2, 3, 5, EvictionStrategy.NONE));
      String hql = "FROM Eviction ORDER BY keepPeriod";
      Eviction eviction = dao.first (hql);

      assertNotNull (eviction);
      assertEquals (eviction.getKeepPeriod (), 2);
   }

   @Test
   public void getEviction ()
   {
      Eviction eviction = dao.getEviction ();
      assertNotNull (eviction);
      assertEquals (eviction.getId (), Long.valueOf (1));
   }

   @Test
   public void getFactoryDefault ()
   {
      Eviction eviction = dao.getFactoryDefault ();
      assertEquals (eviction.getKeepPeriod (), 10);
      assertEquals (eviction.getMaxDiskUsage (), 80);
      assertEquals (eviction.getMaxProductNumber (), 1000);
      assertEquals (eviction.getStrategy (), EvictionStrategy.NONE);
      assertTrue (eviction.getProducts ().isEmpty ());
   }

   @Test
   public void getProduct ()
   {
      Set<Product> toEvict = dao.getProducts ();
      assertNotNull (toEvict);
      assertTrue (toEvict.isEmpty ());
   }

   @Test (dependsOnMethods = "getProduct")
   public void setProducts ()
   {
      dao.setProducts (getProducts ());
      Set<Product> toEvict = dao.getProducts ();
      assertEquals (toEvict, getProducts ());
   }

}
