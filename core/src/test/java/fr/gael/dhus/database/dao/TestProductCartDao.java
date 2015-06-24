package fr.gael.dhus.database.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.ProductCart;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.util.CheckIterator;
import fr.gael.dhus.util.TestContextLoader;

@ContextConfiguration (locations = "classpath:fr/gael/dhus/spring/context-test.xml", loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestProductCartDao extends
   TestAbstractHibernateDao<ProductCart, Long>
{

   @Autowired
   private ProductCartDao dao;

   @Autowired
   private ProductDao pdao;

   @Autowired
   private UserDao udao;

   @Override
   protected HibernateDao<ProductCart, Long> getHibernateDao ()
   {
      return dao;
   }

   @Override
   protected int howMany ()
   {
      return 3;
   }

   @Override
   public void create ()
   {
      User user = udao.read (2L);
      ProductCart pc = new ProductCart ();
      pc.setUser (user);

      pc = dao.create (pc);
      assertNotNull (pc);
      assertEquals (pc.getUser (), user);
   }

   @Override
   public void read ()
   {
      ProductCart pc = dao.read (0L);
      assertNotNull (pc);
      assertEquals (pc.getProducts ().size (), 2);
   }

   @Override
   public void update ()
   {
      long cartId = 0;
      ProductCart pc = dao.read (cartId);
      Product p = new Product ();
      p.setId (3L);

      assertEquals (pc.getProducts ().size (), 2);
      pc.getProducts ().add (p);
      dao.update (pc);

      pc = dao.read (cartId);
      assertEquals (pc.getProducts ().size (), 3);
   }

   @Override
   public void delete ()
   {
      Long id = 0L;
      ProductCart cart = dao.read (id);
      assertNotNull (cart);
      Set<Product> products = cart.getProducts ();

      dao.delete (cart);
      assertNull (dao.read (id));
      for (Product product : products)
      {
         assertNotNull (pdao.read (product.getId ()));
      }
   }

   @Override
   public void scroll ()
   {
      String hql = "WHERE id > 0";
      Iterator<ProductCart> it = dao.scroll (hql, -1, -1).iterator ();
      assertTrue (CheckIterator.checkElementNumber (it, 2));
   }

   @Override
   public void first ()
   {
      String hql = "FROM ProductCart ORDER BY id DESC";
      ProductCart cart = dao.first (hql);
      assertNotNull (cart);
      assertEquals (cart.getId ().intValue (), 2);
   }

   @Test
   public void deleteCartOfUser ()
   {
      // emulate userDao.read (2L)
      User u = new User ();
      u.setId (2L);

      dao.deleteCartOfUser (u);
      assertNull (dao.read (2L));
   }

   @Test
   public void getCartOfUser ()
   {
      User user = udao.read (3L);
      ProductCart cart = dao.getCartOfUser (user);
      assertNotNull (cart);
      assertEquals (cart.getId ().intValue (), 1);
   }

   @Test
   public void deleteProductReferences ()
   {
      Product product = new Product ();
      product.setId (5L);
      dao.deleteProductReferences (product);

      List<ProductCart> pcs = dao.readAll ();
      for (ProductCart pc : pcs)
      {
         assertFalse (pc.getProducts ().contains (product));
      }
   }

   @Test
   public void getProductsIdOfCart ()
   {
      User user = new User ();
      user.setId (0L);

      List<Long> ids = dao.getProductsIdOfCart (user);
      assertNotNull (ids);

      assertEquals (ids.size (), 2);
      assertTrue (ids.contains (Long.valueOf (0)));
      assertTrue (ids.contains (Long.valueOf (5)));
   }

   @Test
   public void scrollCartOfUser ()
   {
      User user = new User ();
      user.setId (0L);

      Iterator<Product> it = dao.scrollCartOfUser (user, -1, -1).iterator ();
      assertTrue (CheckIterator.checkElementNumber (it, 2));
   }

}
