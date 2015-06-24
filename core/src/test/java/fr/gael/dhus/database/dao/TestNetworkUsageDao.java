package fr.gael.dhus.database.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.NetworkUsage;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.util.CheckIterator;
import fr.gael.dhus.util.TestContextLoader;

/*
 * used from spring-test v 3.2.2 (current version on DHuS 3.2.1)
 * @WebAppConfiguration
 * @ContextHierarchy(
 * @ContextConfiguration(locations = "classpath:spring/context-test.xml"))
 */
@ContextConfiguration (locations = "classpath:fr/gael/dhus/spring/context-test.xml", loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestNetworkUsageDao extends
   TestAbstractHibernateDao<NetworkUsage, Long>
{

   @Autowired
   private NetworkUsageDao dao;

   @Autowired
   private UserDao udao;

   private final Long period = new Date ().getTime ();

   @Override
   protected HibernateDao<NetworkUsage, Long> getHibernateDao ()
   {
      return dao;
   }

   @Override
   protected int howMany ()
   {
      return 8;
   }

   @Override
   public void create ()
   {
      User user = new User ();
      user.setId (3L);
      user.setUsername ("babar");

      NetworkUsage nu = new NetworkUsage ();
      nu.setDate (new Date ());
      nu.setIsDownload (false);
      nu.setSize (42L);
      nu.setUser (user);

      nu = dao.create (nu);
      assertNotNull (nu);
      assertEquals (nu.getSize ().intValue (), 42);
      assertEquals (nu.getUser (), user);
   }

   @Override
   public void read ()
   {
      User u = new User ();
      u.setId (2L);
      NetworkUsage nu = dao.read (0L);
      assertNotNull (nu);
      assertEquals (nu.getSize ().intValue (), 2);
      assertEquals (nu.getUser ().getUsername (), "toto");
   }

   @Override
   public void update ()
   {
      Long id = 4L;
      NetworkUsage nu = dao.read (id);
      boolean bool = true;

      assertNotNull (nu);
      assertNotEquals (nu.getIsDownload (), bool);
      nu.setIsDownload (bool);
      dao.update (nu);

      nu = dao.read (id);
      assertTrue (nu.getIsDownload ());
   }

   @Override
   public void delete ()
   {
      long id = 5;
      dao.delete (dao.read (id));
      assertEquals (dao.count (), (howMany () - 1));
      assertNull (dao.read (id));
      assertNotNull (udao.read (3L));
   }

   @Override
   public void scroll ()
   {
      String hql = "WHERE isDownload IS FALSE";
      Iterator<NetworkUsage> it = dao.scroll (hql, -1, -1).iterator ();
      assertTrue (CheckIterator.checkElementNumber (it, 3));
   }

   @Override
   public void first ()
   {
      String hql = "FROM NetworkUsage WHERE isDownload = TRUE ORDER BY id DESC";
      NetworkUsage nu = dao.first (hql);
      assertNotNull (nu);
      assertEquals (nu.getId ().intValue (), 7);
   }

   @Test
   public void testGetDownloadedCountPerUser ()
   {
      User u = udao.getByName ("koko");

      int n = dao.getDownlaodedCountPerUser (u, period);
      assertEquals (n, 1);
   }

   @Test
   public void testGetDownloadSizePerUser ()
   {
      User u = udao.getByName ("koko");

      long expected = 16;
      long size = dao.getDownlaodedSizePerUser (u, period);
      assertEquals (size, expected);
   }
}
