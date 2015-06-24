package fr.gael.dhus.datastore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.io.Files;

import fr.gael.dhus.datastore.exception.DataStoreLocalArchiveNotExistingException;
import fr.gael.dhus.system.config.ConfigurationManager;

@ContextConfiguration(
   locations="classpath:fr/gael/dhus/spring/context-test.xml")
public class DefaultDataStoreTest extends AbstractTestNGSpringContextTests
{
   private static Logger logger = Logger.getLogger (DefaultDataStoreTest.class);
   File tmp;
   File incoming_root ;
   String [][] folders_to_test={{},{}};
   IncomingPathChecker incomingPathChecker;
   
   @Autowired
   DefaultDataStore dataStore;
   
   @Autowired
   ConfigurationManager cfgManager;
   
   @Autowired
   IncomingManager incomingManager;
   
   @BeforeClass
   public void init () throws IOException, 
      DataStoreLocalArchiveNotExistingException, InterruptedException
   {
      tmp = Files.createTempDir();
      tmp.mkdirs ();
      HierarchicalDirectoryBuilder db = 
            new HierarchicalDirectoryBuilder (tmp, 100);
      int i=1000;
      while (i-->0) 
      {
         File root = db.getDirectory ();
         FileUtils.touch (new File(root, "toto.txt"));
         FileUtils.touch (new File(root,
            HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME));
         new File(root, "directory").mkdirs ();
      }
      
      //dataStore.processArchiveSync (false);
      
      incoming_root = new File(cfgManager.getArchiveConfiguration ().getIncomingConfiguration ().getPath ());
      
      int maxfileno = cfgManager.getArchiveConfiguration ().getIncomingConfiguration ().getMaxFileNo ();
      
      String[] folders_to_test_true =
      {
         incoming_root.getPath () + "/X1",
         incoming_root.getPath () + "/X2",
         incoming_root.getPath () + "/X3",
         incoming_root.getPath () + "/X4",
         incoming_root.getPath () + "/X1/X1/X1/X1/X1/X1/X1/X1/X1",
         incoming_root.getPath () + "/X1/X2/X3/X4/X5/X6/X7/X8/X9",
         incoming_root.getPath () + "/X1/X1/X1/XAF/X1/X1/X1/X1/X1/",
         incoming_root.getPath () + File.separator + Long.toHexString (maxfileno-1).substring (1),
         incoming_root.getPath () + "/X1/X1/X1/X1/X1/X1/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME,
         incoming_root.getPath () + "/X1/X1/X1/X1/X1/X1/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME + File.separator + IncomingManager.INCOMING_PRODUCT_DIR,
      };
      
      String[] folders_to_test_false =
      {
         "/tmp/toto",
         incoming_root.getPath () + "/my_data",
         incoming_root.getPath () + "/X2/X23453/X2/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME,
         incoming_root.getPath () + "/X3/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME + "/data",
         incoming_root.getPath () + "/X1/X1/X1/X1/X1/X1/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME + File.separator + IncomingManager.INCOMING_PRODUCT_DIR + File.separator + "mydata",
         incoming_root.getPath () + File.separator + maxfileno + "X1/X2/X3",
         incoming_root.getPath () + "/X3/data",
         incoming_root.getPath () + "/A4",
      };

      folders_to_test[0]=folders_to_test_true;
      folders_to_test[1]=folders_to_test_false;
      incomingPathChecker = new IncomingPathChecker ();

   }
   public class IncomingPathChecker
   {
      public Boolean validate(final String path)
      {
         return incomingManager.isAnIncomingElement (new File(path));
      }
   }
   
   @DataProvider(name = "incomingPathChecks")
   public Object[][] incomingPaths()
   {
      int maxfileno = cfgManager.getArchiveConfiguration ().
         getIncomingConfiguration ().getMaxFileNo ();
         
      incoming_root = new File(cfgManager.getArchiveConfiguration ().
            getIncomingConfiguration ().getPath ());

      return new Object[][]
       {
          { incoming_root.getPath () + "/X1", true },
          { incoming_root.getPath () + "/X2", true },
          { incoming_root.getPath () + "/X3", true },
          { incoming_root.getPath () + "/X4F", true },
          { incoming_root.getPath () + "/X1/X1/X1/1/1/1/1/1/1", true },
          { incoming_root.getPath () + "/X1/X2/X3/X4/5/6/7/8/9", true },
          { incoming_root.getPath () + "/1/1/X1/X1/1/1/1/1/1/", true },
          { incoming_root.getPath () + "/1/1/1/X1/1/1/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME, true },
          { incoming_root.getPath () + "/1/1/1/X1/1/1/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME + File.separator + IncomingManager.INCOMING_PRODUCT_DIR, true },
          { incoming_root.getPath () + File.separator + (maxfileno), true },
          
          { "/tmp/toto", false },
          { incoming_root.getPath () + "/my_data", false },
          { incoming_root.getPath () + "/2/23453", false },
          { incoming_root.getPath () + "/2/2/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME + "1", false },
          { incoming_root.getPath () + "/3/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME + "/data", false },
          { incoming_root.getPath () + "/1/1/1/1/1/1/" + HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME + File.separator + IncomingManager.INCOMING_PRODUCT_DIR + File.separator + "mydata", false },
          { incoming_root.getPath () + "/1/2/3/4/" + maxfileno+1, false },
          { incoming_root.getPath () + "/3/data", false },
          { incoming_root.getPath () + "/A4", false },
       };
   }
   
   @Test (dataProvider="incomingPathChecks")
   public void isAnIncomingElementTrue (String test, Boolean expectedResult)
   {
         Assert.assertEquals (incomingPathChecker.validate (test), 
            expectedResult, test);
   }
  
   @Test
   public void checkIncomming() throws IOException
   {
      Iterator<File>it = new TestWalker ().lookup (tmp).iterator ();
      while (it.hasNext ())
         logger.info (" -> " + it.next ().getPath ());
      
   }
   
   class TestWalker extends DirectoryWalker<File>
   {

      public List<File> lookup (File startDirectory) throws IOException
      {
         List<File> results = new ArrayList<File> ();
         walk(startDirectory, results);
         return results;
      }

      protected boolean handleDirectory (File directory, int depth, 
         Collection<File> results)
      {
        if (HierarchicalDirectoryBuilder.DHUS_ENTRY_NAME.
               equals (directory.getName()))
        {
           ((ArrayList<File>)results).add (0, directory);
           return false;
        }
        else
          return true;
      }

      protected void handleFile(File file, int depth, Collection<File> results)
      {
         // Nothing to do
      }
   }
   
   
   @AfterClass
   public void finalize_me()
   {
     FileUtils.deleteQuietly (tmp);
   }
}
