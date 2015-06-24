package fr.gael.dhus;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.dao.interfaces.DaoListener;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Product.Download;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@ContextConfiguration(locations = "classpath:fr/gael/dhus/spring/context-test.xml")
public class PerformanceTest extends AbstractTestNGSpringContextTests
{

   private final int productNumber = 3000;

   @Autowired
   private ProductDao pdao;
   
   @BeforeSuite
   public void removeLogs()
   {
      //LogManager.shutdown ();
   }

   @BeforeClass
   public void initProducts () throws MalformedURLException
   {
      // First of all: no need to start ingestion process
      // with non existing datasets: remove all listeners
      for (DaoListener<Product>listener:pdao.getListeners ().clone ())
      {
         pdao.removeListener (listener);;
      }
      int i = pdao.count ();
      logger.info ("### initial product number : " + i + " / " + productNumber);
      for (; i < productNumber; i++)
      {
         Product p = new Product ();
         Download download = new Product.Download ();

         p.setCreated (new Date ());
         p.setIdentifier ("product #" + i);
         p.setLocked (false);
         p.setOrigin ("orign " + i % 32);
         p.setPath (new URL ("file:///home/lambert/dhusTestPerf/product" + i));
         p.setProcessed (true);
         p.setSize (new Long(i * 100));
         p.setDownload (download);
         p.setDownloadablePath ("dowloadablePath_" + i);
         p.setUuid (UUID.randomUUID ().toString ());

//         for (int j = 0; j < indexNumberPerProduct; j++)
//         {
//            MetadataIndex mi = new MetadataIndex ();
//            mi.setName ("");
//            mi.setValue ("p#" + i + ";mi#" + j + "***************************"
//               + "***********************************************************"
//               + "***********************************************************"
//               + "***********************************************************");
//            mi.setType ("String");
//            mi.setCategory ("productTest#"+i);
//            mi.setQueryable ("testField#" + j);
//            p.getIndexes ().add (miDao.create (mi));
//         }
         
         pdao.create (p);
      }
   }
   
   @Test
   public void TestMessage1 ()
   {
      logger.info (" ***********  Starting concurrent Reads **************" );
      logger.info ("    1000 invocations with 50 concurency " );
   }

   Long max_time = 0L;
   Long min_time = 10000L;
   Long total_time = 0L;
   Integer count=0;
   @Test(invocationCount = 1000, threadPoolSize=50, dependsOnMethods={"TestMessage1"})
   public void performanceTestSimpleRead ()
   {
      Long key = new Random ().nextLong () % productNumber;
      key = (key > 0) ? key : -key;
      //logger.info ("Trying to read product #" + key);
      long start = System.currentTimeMillis ();
      long end = System.currentTimeMillis ();
      long time = end - start;
      if (time>max_time) synchronized (max_time) {max_time=time;}
      if (time<min_time) synchronized (min_time) {min_time=time;}
      synchronized (total_time) { total_time+=time;}
      synchronized (count) { count++;}
   }

   
   @Test (dependsOnMethods={"performanceTestSimpleRead"})
   public void TestMessage2 ()
   {
      logger.info ("    Iteration of read : " + count);
      logger.info ("    Maximum read delay: " + max_time + "ms");
      logger.info ("    Minimum read delay: " + min_time + "ms");
      logger.info ("    Total elapsed time   : " + total_time + "ms (" + (total_time/60000.0) + " mn)");
      logger.info (" ***********  Starting concurrent ReadAll **************" );
      logger.info ("    100 invocations with 50 concurency " );
      max_time = 0L;
      total_time = 0L;
      min_time = 10000L;
      count=0;
      
   }

   @Test(invocationCount = 100, threadPoolSize=50, dependsOnMethods={"TestMessage2"})
   public void performanceTestSimpleReadAll ()
   {
      //logger.info ("Trying to read product #" + key);
      long start = System.currentTimeMillis ();
      long end = System.currentTimeMillis ();
      long time = end - start;
      if (time>max_time) synchronized (max_time) {max_time=time;}
      if (time<min_time) synchronized (min_time) {min_time=time;}
      synchronized (total_time) { total_time+=time;}
      synchronized (count) { count++;}
   }

   @Test (dependsOnMethods={"performanceTestSimpleReadAll"})
   public void TestMessage3 ()
   {
      logger.info ("    Iteration of readAll : " + count);
      logger.info ("    Maximum readAll delay: " + max_time + "ms");
      logger.info ("    Minimum readAll delay: " + min_time + "ms");
      logger.info ("    Total elapsed time   : " + total_time + "ms (" + (total_time/60000.0) + " mn)");
      logger.info (" ***********  Starting ReadAll **************" );
      logger.info ("    100 invocations with no concurency " );
      max_time = 0L;
      min_time=1000L;
      count=0;
      total_time = 0L;
   }
   
   @Test(invocationCount = 100, dependsOnMethods={"TestMessage3"})
   public void performanceTestSynchronousReadAll ()
   {
      //logger.info ("Trying to read product #" + key);
      long start = System.currentTimeMillis ();
      long end = System.currentTimeMillis ();
      long time = end - start;
      if (time>max_time) synchronized (max_time) {max_time=time;}
      if (time<min_time) synchronized (min_time) {min_time=time;}
      synchronized (total_time) { total_time+=time;}
      synchronized (count) { count++;}
   }

   Long max_read_time;
   Long min_read_time;
   Long max_write_time;
   Long min_write_time;
   
   @Test (dependsOnMethods={"performanceTestSynchronousReadAll"})
   public void TestMessage4 ()
   {
      logger.info ("    Iteration of readAll : " + count);
      logger.info ("    Maximum readAll delay: " + max_time + "ms");
      logger.info ("    Minimum readAll delay: " + min_time + "ms");
      logger.info ("    Total elapsed time   : " + total_time + "ms (" + (total_time/60000.0) + " mn)");
      logger.info (" ***********  Starting Async Read/Write test **************" );
      logger.info ("    100 invocations with 50 concurency " );
      max_read_time = 0L;
      min_read_time=1000L;
      max_write_time = 0L;
      min_write_time=1000L;
      count=0;
      total_time = 0L;
   }
   
   @Test(invocationCount = 100, threadPoolSize=50, dependsOnMethods={"TestMessage4"})
   public void performanceTestAsyncReadWrite ()
   {
      Long key = new Random ().nextLong () % productNumber;
      key = (key > 0) ? key : -key;
      
      // Read
      long start_read = System.currentTimeMillis ();
      pdao.read (key);
      long end_read = System.currentTimeMillis ();

      // Write
      long start_write = end_read;
      long end_write = System.currentTimeMillis ();
      
      
      long read_time = end_read - start_read;
      long write_time = end_write - start_write;
      
      if (read_time>max_read_time) synchronized (max_read_time) {max_read_time=read_time;}
      if (read_time<min_read_time) synchronized (min_read_time) {min_read_time=read_time;}
      
      if (write_time>max_write_time) synchronized (max_write_time) {max_write_time=write_time;}
      if (write_time<min_write_time) synchronized (min_write_time) {min_write_time=write_time;}

      
      synchronized (total_time) { total_time+=(read_time+write_time);}
      synchronized (count) { count++;}
   }
   
   @Test (dependsOnMethods={"performanceTestAsyncReadWrite"})
   public void TestMessage5 ()
   {
      logger.info ("    Iteration of read/write : " + count);
      logger.info ("    Maximum read delay      : " + max_read_time + "ms");
      logger.info ("    Minimum read delay      : " + min_read_time + "ms");
      logger.info ("    Maximum write delay     : " + max_write_time + "ms");
      logger.info ("    Minimum write delay     : " + min_write_time + "ms");
      logger.info ("    Total elapsed time   : " + total_time + "ms (" + (total_time/60000.0) + " mn)");

      
      max_read_time = 0L;
      min_read_time=1000L;
      max_write_time = 0L;
      min_write_time=1000L;
      count=0;
      total_time = 0L;
   }

/*
   @Test(invocationCount = 5)
   public void performanceTestReadAll ()
   {
      long start = System.currentTimeMillis ();
      List<Product> list = pdao.readAll ();
      long end = System.currentTimeMillis ();
      logger.info ("readAll in " + (end - start) + "ms [" + list.size () + "]");
   }

   @Test
   public void testMultiAccessProduct () throws InterruptedException
   {
      logger.info ("## Multi Access #########################################");
      Runnable read = new Runnable ()
      {

         @Override
         public void run ()
         {
            String name = Thread.currentThread ().getName ();
            long start = System.currentTimeMillis ();
            Long key = new Random ().nextLong () % productNumber;
            key = (key > 0) ? key : -key;
            pdao.read (key);
            long end = System.currentTimeMillis ();
            logger.info (name + "\tfinished (read) in " + (end - start) + "ms");
         }
      };
      Runnable readAll = new Runnable ()
      {

         @Override
         public void run ()
         {
            String name = Thread.currentThread ().getName ();
            long start = System.currentTimeMillis ();
            pdao.readAll ();
            long end = System.currentTimeMillis ();
            logger.info (name + "\tfinished (readAll) in " + (end - start) + "ms");
         }
      };
      
      Runnable[] runnables = new Runnable[]{read, readAll};
      List<Thread> threads = new ArrayList<Thread> ();
      for (int i = 0; i < threadNumber; i++)
      {
         Runnable run = runnables[(i % runnables.length)];
         threads.add (new Thread (run, "ThreadTest#" + i));
      }

      for (Thread thread : threads)
      {
         thread.start ();
      }
      Thread.sleep (30000);
      logger.info ("#########################################################");
   }
   */
}
