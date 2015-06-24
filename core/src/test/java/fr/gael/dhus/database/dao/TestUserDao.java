package fr.gael.dhus.database.dao;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Preference;
import fr.gael.dhus.database.object.Quota;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.Search;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.database.object.restriction.LockedAccessRestriction;
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
public class TestUserDao extends TestAbstractHibernateDao<User, Long>
{

   @Autowired
   private UserDao dao;

   @Override
   protected HibernateDao<User, Long> getHibernateDao ()
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
      String username = "usErTesT";
      List<Role> roles =
         Arrays.asList (Role.SEARCH, Role.DOWNLOAD, Role.UPLOAD);
      AccessRestriction lock = new LockedAccessRestriction ();
      lock.setBlockingReason ("Max connection exceeded !");
      Quota quota = new Quota ();
      quota.setMaxConnectionCount (BigInteger.TEN);
      quota.setMaxDownload (BigInteger.valueOf (3));
      quota.setMaxUpload (BigInteger.ZERO);

      User user = new User ();
      user.setUsername (username);
      user.setPassword ("pwd");
      user.setRoles (roles);
      user.setDeleted (false);
      user.setEmail ("usertest@gael.fr");
      user.setQuota (quota);
      user.addRestriction (lock);

      user = dao.create (user);
      Assert.assertEquals (dao.count (), (howMany () + 1));
      Assert.assertNotNull (user);
      Assert.assertTrue (user.getRoles ().containsAll (roles));
      Assert.assertFalse (user.isDeleted ());
      Assert.assertTrue (user.getUsername ().equalsIgnoreCase (username));
      Assert.assertEquals (user.getRestrictions ().size (), 1);
      Assert.assertEquals (user.getQuota ().getMaxConnectionCount ()
         .intValue (), 10);
   }

   @Override
   public void read ()
   {
      User user = dao.read (0L);
      Assert.assertNotNull (user);

      Set<AccessRestriction> restrictions = user.getRestrictions ();
      Preference preferences = user.getPreferences ();

      Assert.assertTrue (user.getUsername ().equalsIgnoreCase ("koko"));
      Assert.assertEquals (user.getQuota ().getMaxUpload (),
         BigInteger.valueOf (5));
      Assert.assertEquals (restrictions.size (), 1);
      Assert.assertEquals (preferences.getFileScanners ().size (), 2);
      Assert.assertEquals (preferences.getSearches ().size (), 2);
   }

   @Override
   public void update ()
   {
      Long uid = Long.valueOf (0);
      BigInteger uploadQuota = BigInteger.valueOf (50);
      String lastname = "Lambert";
      String advancedSearchKey = "advanceKey";
      String advancedSearchValue = "France";

      User user = dao.read (uid);
      Assert.assertNotNull (user);
      user.setLastname (lastname);
      user.getQuota ().setMaxUpload (uploadQuota);
      user.getRestrictions ().clear ();
      for (Search search : user.getPreferences ().getSearches ())
      {
         search.getAdvanced ().put (advancedSearchKey, advancedSearchValue);
      }
      dao.update (user);

      user = dao.read (uid);
      Assert.assertEquals (user.getLastname (), lastname);
      Assert.assertTrue (user.getRestrictions ().isEmpty ());
      Assert.assertEquals (user.getQuota ().getMaxUpload (), uploadQuota);
      for (Search search : user.getPreferences ().getSearches ())
      {
         Assert.assertEquals (search.getAdvanced ().get (advancedSearchKey),
            advancedSearchValue);
      }
   }

   @Override
   public void delete ()
   {
      Long uid = Long.valueOf (0);
      User user = dao.read (uid);
      Assert.assertNotNull (user);

      dao.delete (user);
      Assert.assertEquals (dao.count (), (howMany () - 1));
      Assert.assertNull (dao.read (uid));
      Assert.assertEquals (countRestriction (user), 0);
      Assert
         .assertEquals (
            countInTable ("PREFERENCES", "ID", user.getPreferences ().getId ()),
            0);
      Assert.assertEquals (
         countInTable ("QUOTAS", "ID", user.getQuota ().getId ()), 0);
      Assert.assertEquals (countInTable ("USER_ROLES", "USER_ID", uid), 0);

      Assert.assertEquals (
         countInTable ("SEARCH_PREFERENCES", "PREFERENCE_ID", 0L), 0);
      Assert.assertEquals (
         countInTable ("FILE_SCANNER_PREFERENCES", "PREFERENCE_ID", 0L), 0);
      
      Assert.assertEquals (countInTable ("SEARCHES", "ID", 0L), 0);
      Assert.assertEquals (countInTable ("SEARCHES", "ID", 2L), 0);
      Assert.assertEquals (countInTable ("FILE_SCANNER", "ID", 0L), 0);
      Assert.assertEquals (countInTable ("FILE_SCANNER", "ID", 2L), 0);
   }

   private int countInTable (final String table, final String IdName,
      final Long id)
   {
      return dao.getHibernateTemplate ().execute (
         new HibernateCallback<Integer> ()
         {
            @Override
            public Integer doInHibernate (Session session)
               throws HibernateException, SQLException
            {
               String sql =
                  "SELECT count(*) FROM " + table + " WHERE " + IdName + " = ?";
               Query query = session.createSQLQuery (sql);
               query.setLong (0, id);
               return ((BigInteger) query.uniqueResult ()).intValue ();
            }
         });
   }

   private int countRestriction (final User user)
   {
      return dao.getHibernateTemplate ().execute (
         new HibernateCallback<Integer> ()
         {
            @Override
            public Integer doInHibernate (Session session)
               throws HibernateException, SQLException
            {
               String sql =
                  "SELECT count(*) FROM ACCESS_RESTRICTION "
                     + "WHERE ID IN (:restriction)";
               Query query = session.createSQLQuery (sql);
               query.setParameterList ("restriction", user.getRestrictions ());
               return ((BigInteger) query.uniqueResult ()).intValue ();
            }
         });
   }

   @Override
   public void scroll ()
   {
      String hql = "WHERE deleted IS FALSE";
      Iterator<User> it = dao.scroll (hql, -1, -1).iterator ();
      Assert.assertTrue (CheckIterator.checkElementNumber (it, 5));
   }

   @Override
   public void first ()
   {
      String hql = "FROM User ORDER BY username DESC";
      User user = dao.first (hql);
      Assert.assertNotNull (user);
      Assert.assertEquals (user.getUsername (), "~public data");
   }

}
