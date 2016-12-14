package fr.gael.dhus.database.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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

   private Date period;

   @BeforeClass
   public void setUp ()
   {
      Calendar calendar = Calendar.getInstance ();
      calendar.set (2014, 01, 01);
      this.period = calendar.getTime ();
   }

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
      user.setUUID ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3");
      user.setUsername ("babar");

      NetworkUsage nu = new NetworkUsage ();
      nu.setDate (new Date ());
      nu.setIsDownload (false);
      nu.setSize (42L);
      nu.setUser (user);

      nu = dao.create (nu);
      Assert.assertNotNull (nu);
      Assert.assertNotNull (nu.getId ());
      Assert.assertEquals (nu.getSize ().intValue (), 42);
      Assert.assertEquals (nu.getUser (), user);
   }

   @Override
   public void read ()
   {
      User u = new User ();
      u.setUUID ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2");
      NetworkUsage nu = dao.read (0L);
      Assert.assertNotNull (nu);
      Assert.assertEquals (nu.getSize ().intValue (), 2);
      Assert.assertEquals (nu.getUser ().getUsername (), "toto");
   }

   @Override
   public void update ()
   {
      Long id = 4L;
      NetworkUsage nu = dao.read (id);
      boolean bool = true;

      Assert.assertNotNull (nu);
      Assert.assertNotEquals (nu.getIsDownload (), bool);
      nu.setIsDownload (bool);
      dao.update (nu);

      nu = dao.read (id);
      Assert.assertTrue (nu.getIsDownload ());
   }

   @Override
   public void delete ()
   {
      long id = 5;
      dao.delete (dao.read (id));
      Assert.assertEquals (dao.count (), (howMany () - 1));
      Assert.assertNull (dao.read (id));
      Assert.assertNotNull (udao.read ("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3"));
   }

   @Override
   public void scroll ()
   {
      String hql = "WHERE isDownload IS FALSE";
      Iterator<NetworkUsage> it = dao.scroll (hql, -1, -1).iterator ();
      Assert.assertTrue (CheckIterator.checkElementNumber (it, 3));
   }

   @Override
   public void first ()
   {
      String hql = "FROM NetworkUsage WHERE isDownload = TRUE ORDER BY id DESC";
      NetworkUsage nu = dao.first (hql);
      Assert.assertNotNull (nu);
      Assert.assertEquals (nu.getId ().intValue (), 7);
   }

   @Test
   public void testGetDownloadedCountPerUser ()
   {
      User u = udao.getByName ("babar");

      int n = dao.countDownloadByUserSince (u, period);
      Assert.assertEquals (n, 2);
   }

   @Test
   public void testGetDownloadSizePerUser ()
   {
      User u = udao.getByName ("babar");

      long expected = 68;
      long size = dao.getDownloadedSizeByUserSince (u, period);
      Assert.assertEquals (size, expected);
   }
}
