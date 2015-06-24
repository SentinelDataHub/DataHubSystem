package fr.gael.dhus.database.dao;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;

import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.Test;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;

public abstract class TestAbstractHibernateDao<T, PK extends Serializable>
   extends AbstractTransactionalTestNGSpringContextTests
{

   protected abstract HibernateDao<T, PK> getHibernateDao ();

   protected abstract int howMany ();

   @Test
   public abstract void create ();

   @Test
   public abstract void read ();

   @Test
   public abstract void update ();

   @Test
   public abstract void delete ();

   @Test
   public abstract void scroll ();

   @Test
   public abstract void first ();

   @Test
   public void count ()
   {
      assertEquals (getHibernateDao ().count (), howMany ());
   }

   @Test
   public void readAll ()
   {
      assertEquals (getHibernateDao ().readAll ().size (), howMany ());
   }

   @Test
   public void deleteAll ()
   {
      getHibernateDao ().deleteAll ();
      assertEquals (getHibernateDao ().count (), 0);
   }
}
