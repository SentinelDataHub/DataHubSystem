package fr.gael.dhus.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.ProductCart;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.util.TestContextLoader;

@SuppressWarnings ("deprecation")
@ContextConfiguration (
   locations = {"classpath:fr/gael/dhus/spring/context-test.xml",
                "classpath:fr/gael/dhus/spring/context-security-test.xml"},
   loader = TestContextLoader.class)
@DirtiesContext (classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProductCartServiceTest extends AbstractTestNGSpringContextTests
{
   Map<Integer, String> users = new HashMap<Integer, String>(2);
   {
      users.put(0, "test1");
      users.put(1, "test2");
   }
   HashSet<GrantedAuthority> roles =
      Sets.<GrantedAuthority> newHashSet(
         new GrantedAuthorityImpl("ROLE_DOWNLOAD"));

   @BeforeClass
   public void authenticate()
   {
      int sb = new Random().nextBoolean() ? 1 : 0;
      String name = users.get(sb);
      SandBoxUser user = new SandBoxUser(name, name, true, sb, roles);
      UsernamePasswordAuthenticationToken token =
         new UsernamePasswordAuthenticationToken(user, user.getPassword());
      SecurityContextHolder.getContext().setAuthentication(token);
   }
   @Autowired 
   ProductCartService pcs;
   
   @Autowired
   UserDao userDao;
   
   @Autowired
   ProductDao productDao;
   
   @Test (priority=10)
   public void addProductToCart() throws MalformedURLException
   {
      User u = new User();
      u.setUsername ("TMPUSER##1");
      u.setPassword ("TMPPASS##1");
      u = userDao.create(u);
      
      Product p = new Product();
      p.setPath(new URL("file:///tmp/product##1"));
      p = productDao.create(p);
      
      try
      {
         pcs.addProductToCart(u.getId(), p.getId());
      }
      catch (Exception e)
      {
         Assert.fail("Error while adding a product into a product cart.", e);
      }
   }

  @Test (priority=100)
  public void clearCart()
  {
     try
     {
        pcs.clearCart(1000L);
        Assert.fail("System remove cart of an unknown user ?");
     }
     catch (Exception e)
     {
        // Shall raise an exception because user is unknown.
     }
     
     try
     {
        pcs.clearCart(3L);
     }
     catch (Exception e)
     {
        Assert.fail("Cannot remove single cart of existing user.", e);
     }
     
     try
     {
        pcs.clearCart(1L);
     }
     catch (Exception e)
     {
        Assert.fail("Cannot remove empty cart of existing user.", e);
     }

     try
     {
        pcs.clearCart(2L);
     }
     catch (Exception e)
     {
        Assert.fail("Cannot remove multi-cart of existing user.", e);
     }
  }

  @Test (priority=1)
  public void countProductsInCart()
  {
     Assert.assertEquals(pcs.countProductsInCart(0L), 2);
     Assert.assertEquals(pcs.countProductsInCart(1L), 0);
     Assert.assertEquals(pcs.countProductsInCart(2L), 0);
     Assert.assertEquals(pcs.countProductsInCart(3L), 1);
  }
  
  @Test (priority=1)
  public void getCartOfUser()
  {
     // Users #0 and #3: normal users with single cart
     ProductCart cart = pcs.getCartOfUser(0L);
     Assert.assertEquals (cart.getId(), new Long(0L));
     cart = pcs.getCartOfUser(3L);
     Assert.assertEquals (cart.getId(), new Long(1L));

    
     // user #1 has no cart. The cart should be NULL.
     cart = pcs.getCartOfUser(1L);
     Assert.assertNull(cart);
    
     // User #2 has carts: first cart must be returned #2
     cart = pcs.getCartOfUser(2L);
     Assert.assertEquals (cart.getId(), new Long(2L));
  }

  @Test (priority=1)
  public void getProductsIdOfCart()
  {
     // Users ID #0 -> CARD ID #0 -> PRODUCT IDs #0 and #5 
     List<Long>products = pcs.getProductsIdOfCart(0L);
     Assert.assertEquals (products.size(), 2);
     Assert.assertEquals (products.get (0), new Long(5L));
     Assert.assertEquals (products.get (1), new Long(0L));
     

     // USER #1 -> no cart
     products = pcs.getProductsIdOfCart(1L);
     Assert.assertEquals (products.size(), 0);

     
     // USER #2 -> CART #2 -> no product 
     //            CART #3 -> product #0
     // DAO methods returns only first cart if multiple... 
     products = pcs.getProductsIdOfCart(2L);
     Assert.assertEquals (products.size(), 0);

     // Users ID #3 -> CARD ID #1 -> PRODUCT IDs #5
     products = pcs.getProductsIdOfCart(3L);
     Assert.assertEquals (products.size(), 1);
     Assert.assertEquals (products.get (0), new Long(5L));
  }

  @Test (priority=1)
  public void getProductsOfCart()
  {
     // Users ID #0 -> CARD ID #0 -> PRODUCT IDs #0 and #5 
     List<Product>products = pcs.getProductsOfCart(0L, 0, 100);
     Assert.assertEquals (products.size(), 2);
     Assert.assertEquals (products.get (0).getId(), new Long(5L));
     Assert.assertEquals (products.get (1).getId(), new Long(0L));
     

     // USER #1 -> no cart
     products = pcs.getProductsOfCart(1L, 0 , 100);
     Assert.assertEquals (products.size(), 0);

     
     // USER #2 -> CART #2 -> no product 
     //            CART #3 -> product #0
     // DAO methods returns only first cart if multiple... 
     products = pcs.getProductsOfCart(2L, 0, 100);
     Assert.assertEquals (products.size(), 0);

     // Users ID #3 -> CARD ID #1 -> PRODUCT IDs #5
     products = pcs.getProductsOfCart(3L, 0, 100);
     Assert.assertEquals (products.size(), 1);
     Assert.assertEquals (products.get (0).getId(), new Long(5L));
  }

  @Test (priority=100)
  public void removeProductFromCart()
  {
     // Users ID #0 -> CARD ID #0 -> PRODUCT IDs #0 and #5
     // Remove product #0 
     pcs.removeProductFromCart(0L, 0L);
     
     // recheck it product list size is now 1
     List<Long>products = pcs.getProductsIdOfCart(0L);
     Assert.assertEquals (products.size(), 1);
     Assert.assertEquals (products.get (0), new Long(5L));
  }
  
  @Test (priority=1000)
  public void createCartOfUser ()
  {
     User u = new User();
     u.setUsername ("TMPUSER##2");
     u.setPassword ("TMPPASS##2");
     u = userDao.create(u);
     
     ProductCart cart = pcs.createCartOfUser(u.getId());
     Assert.assertNotNull(cart);
     
     ProductCart cart_new = pcs.createCartOfUser(u.getId());
     Assert.assertEquals(cart, cart_new);
  }
  
  @Test (priority=1000)
  public void deleteCartOfUser ()
  {
     pcs.deleteCartOfUser(0L);
     Assert.assertNull (pcs.getCartOfUser(0L));
  }

}
