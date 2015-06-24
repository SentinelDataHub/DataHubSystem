package fr.gael.dhus.database.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.database.object.restriction.LockedAccessRestriction;
import fr.gael.dhus.util.CheckIterator;
import fr.gael.dhus.util.TestContextLoader;

@ContextConfiguration (locations = "classpath:fr/gael/dhus/spring/context-test.xml", loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestAccessRestrictionDao extends
   TestAbstractHibernateDao<AccessRestriction, Long>
{

   @Autowired
   private AccessRestrictionDao dao;

   @Override
   protected HibernateDao<AccessRestriction, Long> getHibernateDao ()
   {
      return dao;
   }

   @Override
   protected int howMany ()
   {
      return 4;
   }

   @Override
   public void create ()
   {
      int expected = dao.count () + 1;
      String blockingReason = "Test create AccessRestriction";

      AccessRestriction ar = new LockedAccessRestriction ();
      ar.setBlockingReason (blockingReason);
      ar = dao.create (ar);

      Assert.assertNotNull (ar);
      LockedAccessRestriction lar = (LockedAccessRestriction) ar;
      assertEquals (lar.getBlockingReason (), blockingReason);
      assertEquals (dao.count (), expected);
   }

   @Override
   public void read ()
   {
      long trueId = 0;
      long fakeId = 99;

      Assert.assertNotNull (dao.read (trueId));
      Assert.assertNull (dao.read (fakeId));
   }

   @Override
   public void update ()
   {
      long id = 2;
      String reason = "Test update reason";
      AccessRestriction ar = dao.read (id);

      Assert.assertNotEquals (ar.getBlockingReason (), reason);
      ar.setBlockingReason (reason);
      dao.update (ar);

      ar = dao.read (id);
      Assert.assertEquals (ar.getBlockingReason (), reason);
   }

   @Override
   public void delete ()
   {
      Long id = 3L;
      int expected = dao.count () - 1;

      AccessRestriction ar = dao.read (id);
      assertNotNull (ar);

      dao.delete (ar);
      Assert.assertEquals (dao.count (), expected);
      Assert.assertNull (dao.read (id));
   }

   @Override
   public void scroll ()
   {
      String hql = "WHERE blockingReason LIKE 'punition%'";
      Iterator<AccessRestriction> it = dao.scroll (hql, -1, -1).iterator ();
      assertTrue (CheckIterator.checkElementNumber (it, 3));
   }

   @Override
   public void first ()
   {
      String hql =
         "FROM AccessRestriction ar WHERE ar.blockingReason LIKE "
            + "'punition%' ORDER BY id DESC";
      AccessRestriction ar = dao.first (hql);
      assertNotNull (ar);
      assertEquals (ar.getId ().intValue (), 3);
   }

}
