package fr.gael.dhus.datastore;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.io.Files;

public class HierarchicalDirectoryBuilderTest
{
   private static Log logger = LogFactory.getLog (
      HierarchicalDirectoryBuilderTest.class);

   File tmp;
   @BeforeClass
   public void init ()
   {
      tmp = Files.createTempDir();
      //tmp = new File("/data_1/tmp/dhus/tmp-123");
      tmp.mkdirs ();
   }

   @Test (invocationCount=100)
   public void getDirectory255()
   {
      long start=new Date ().getTime ();
      HierarchicalDirectoryBuilder db=new HierarchicalDirectoryBuilder(tmp,3);
      File f = db.getDirectory ();
      long end=new Date ().getTime ();
      logger.info ("[" + (end-start) + " ms] " + f.getPath ());
   }
  
   @AfterClass
   public void finalize_me()
   {
      logger.info ("Removing tmp files.");
      FileUtils.deleteQuietly (tmp);
   }
}
