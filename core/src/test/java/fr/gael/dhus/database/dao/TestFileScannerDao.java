package fr.gael.dhus.database.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.FileScanner;
import fr.gael.dhus.util.CheckIterator;
import fr.gael.dhus.util.TestContextLoader;

@ContextConfiguration (locations = "classpath:fr/gael/dhus/spring/context-test.xml", loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestFileScannerDao extends
   TestAbstractHibernateDao<FileScanner, Long>
{

   @Autowired
   private FileScannerDao dao;

   @Autowired
   private CollectionDao cdao;

   @Override
   protected HibernateDao<FileScanner, Long> getHibernateDao ()
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
      String message = "testSsuccess";
      
      Collection collection = new Collection ();
      collection.setId (5L);
      
      HashSet<Collection> collections =  new HashSet<> ();
      collections.add (collection);
      
      FileScanner fs = new FileScanner ();
      fs.setActive (true);
      fs.setStatus ("ok");
      fs.setStatusMessage (message);
      fs.setCollections (collections);

      fs = dao.create (fs);
      assertNotNull (fs);
      assertEquals (dao.count (), (howMany () + 1));
      assertEquals (fs.getStatusMessage (), message);
      assertEquals (fs.getCollections ().size (), 1);      
   }

   @Override
   public void read ()
   {
      FileScanner fs = dao.read (0L);
      assertNotNull (fs);
      assertEquals (fs.getUrl (), "coco-abricot");
   }

   @Override
   public void update ()
   {
      Long cid = Long.valueOf (3);
      Long fid = Long.valueOf (0);
      String username = "toto";
      FileScanner fs = dao.read (fid);
      Collection collection = new Collection ();
      collection.setId (cid);

      assertNotEquals (fs.getUsername (), username);
      fs.setUsername (username);
      fs.getCollections ().add (collection);
      dao.update (fs);

      fs = dao.read (fid);
      assertEquals (fs.getUsername (), username);
      assertEquals (fs.getCollections ().size (), 3);
      assertTrue (fs.getCollections ().contains (collection));
   }

   @Override
   public void delete ()
   {
      Long id = 1L;
      FileScanner element = dao.read (id);
      Set<Collection> collections = element.getCollections ();
      Hibernate.initialize (collections);
      dao.delete (element);

      assertEquals (dao.count (), (howMany () - 1));
      assertNull (dao.read (id));
      for (Collection collection : collections)
      {
         assertNotNull (cdao.read (collection.getId ()));
      }
   }

   @Override
   public void scroll ()
   {
      String hql = "WHERE status = 'running'";
      Iterator<FileScanner> it = dao.scroll (hql, -1, -1).iterator ();
      assertTrue (CheckIterator.checkElementNumber (it, 2));
   }

   @Override
   public void first ()
   {
      String hql = "FROM FileScanner WHERE status = 'running' ORDER BY id DESC";
      FileScanner fs = dao.first (hql);
      assertNotNull (fs);
      assertEquals (fs.getId (), Long.valueOf (3));
      assertEquals (fs.getStatusMessage (), "running...");
   }

   @Test
   public void deleteProductReferences ()
   {
      Long fid0 = Long.valueOf (0);
      Long fid1 = Long.valueOf (1);
      FileScanner fs0 = dao.read (fid0);
      FileScanner fs1 = dao.read (fid1);
      Collection c = cdao.read (1L);

     
      assertTrue (fs0.getCollections ().contains (c));
      assertTrue (fs1.getCollections ().contains (c));
      
      int updated = dao.deleteCollectionReferences (c);
      dao.getSessionFactory ().getCurrentSession ().refresh (fs0);
      dao.getSessionFactory ().getCurrentSession ().refresh (fs1);

      assertEquals (updated, 2);
      assertFalse (fs0.getCollections ().contains (c));
      assertFalse (fs1.getCollections ().contains (c));
   }

   @Test
   public void getScannerCollections ()
   {
      List<BigInteger> list = dao.getScannerCollections (0L);
      assertNotNull (list);
      assertEquals (list.size (), 2);
      assertTrue (list.contains (BigInteger.valueOf (1)));
      assertTrue (list.contains (BigInteger.valueOf (2)));
   }
}
