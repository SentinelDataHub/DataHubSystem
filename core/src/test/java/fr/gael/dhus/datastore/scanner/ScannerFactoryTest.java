package fr.gael.dhus.datastore.scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

import fr.gael.dhus.datastore.scanner.Scanner;
import fr.gael.dhus.datastore.scanner.ScannerFactory;
import fr.gael.drbx.cortex.DrbCortexItemClass;

public class ScannerFactoryTest
{
   private static Log logger = LogFactory.getLog (ScannerFactoryTest.class);
   String [] classes_strings=new String [] 
      {
         "http://www.gael.fr/test#product1",
         "http://www.gael.fr/test#product2",
      };
   
   List<DrbCortexItemClass>classes;
   FakeFtpServer fakeFtpServer;
   
   File tmpDir;

   @BeforeClass
   protected void startftp() throws Exception
   {
      fakeFtpServer = new FakeFtpServer();
      fakeFtpServer.setServerControlPort(8089);  // use any free port

      FileSystem fileSystem = new UnixFakeFileSystem();
      fileSystem.add(new FileEntry("/data/s1-level-1-calibration.xsd", "<schema/>"));
      fileSystem.add(new FileEntry("/data/s1-object-types.xsd", "<schema/>"));
      fileSystem.add(new FileEntry("/data/GOM_EXT_2PNPDE20070312_232536_000000542056_00202_26308_1271.N1", "GOMOS DATA!"));
      fileSystem.add(new FileEntry ("/data/S1A_IW_SLC__1SDV_20141003T054235_20141003T054304_002661_002F66_D5C8.SAFE/manifest.safe", "<XFDU/>"));
      fileSystem.add(new FileEntry ("/data/S1A_EW_GRDH_1SSH_20120101T022934_20120101T022945_001770_000001_AF02.SAFE/manifest.safe", "<XFDU/>"));
      fileSystem.add(new FileEntry("/data/manifest.safe", "<XFDU/>"));
      
      fakeFtpServer.setFileSystem(fileSystem);

      UserAccount userAccount = new UserAccount("user", "password", "/");
      fakeFtpServer.addUserAccount(userAccount);
      
      fakeFtpServer.start();
  }

   @BeforeClass
   public void before () throws IOException
   {
      classes = new ArrayList<DrbCortexItemClass>();
      for (String cl: classes_strings)
      {
         try
         {
            classes.add(DrbCortexItemClass.getCortexItemClassByName(cl));
         }
         catch (NullPointerException e)
         {
            logger.error ("ScannerFactoryTest Initialization error: " +
               "unable to find class \"" + cl + "\" in default model.");
         }
      }
      
      tmpDir = File.createTempFile("scanner", ".test");
      String path = tmpDir.getPath();
      tmpDir.delete();
      tmpDir = new File(path);
      tmpDir.mkdirs();
      
      // empty XML files makes drb producing many error logs on standard output.
      File file = new File(tmpDir, "s1-level-1-calibration.test");
      FileUtils.touch(file);
      
      file = new File(tmpDir, "s1-object-types.titi");
      FileUtils.touch(file);
      
      file = new File(tmpDir, "GOM_EXT_2PNPDE20070312_232536_000000542056_00202_26308_1271.N1");
      FileUtils.touch(file);
      
      file = new File(tmpDir, "S1A_IW_SLC__1SDV_20141003T054235_20141003T054304_002661_002F66_D5C8.SAFE");
      file.mkdir();
      
      file = new File(tmpDir, "S1A_EW_GRDH_1SSH_20120101T022934_20120101T022945_001770_000001_AF02.SAFE");
      file.mkdir();
      
      file = new File(file, "manifest.safe");
      FileUtils.touch(file);
      
      file = new File(tmpDir, "manifest.safe");
      FileUtils.touch(file);      
      
   }

  @Test
  public void runFileScanner() throws InterruptedException
  {
     ScannerFactory sf = new ScannerFactory ();
     Scanner scanner = sf.getScanner (tmpDir.getPath());
     
     scanner.setSupportedClasses(classes);
     scanner.getScanList().simulate(false);
     
     scanner.scan();
     
     Assert.assertTrue (scanner.getScanList().size()>0, "No item found.");
     
     // Expected result is 2: only S1[AB]_ pattern signature is supported.
     Assert.assertEquals (scanner.getScanList().size(), 2,
        "Wrong number of items found.");
  }
  
  @Test (groups="slow")
  public void runFtpScanner() throws InterruptedException
  {
     ScannerFactory sf = new ScannerFactory ();
     Scanner scanner = sf.getScanner ("ftp://localhost:8089/data", 
           "user", "password", null);
     
     // scanner.setSupportedClasses(classes);
     // Scan all the items...
     scanner.setSupportedClasses(ImmutableList.of (DrbCortexItemClass
           .getCortexItemClassByName ("http://www.gael.fr/drb#item")));
     
     scanner.getScanList().simulate(false);
     
     scanner.scan();
     
     Assert.assertTrue (scanner.getScanList().size()>0, "No item found.");
  }
  
  @Test
  public void getScannerSupport ()
  {
     ScannerFactory sf = new ScannerFactory ();
     String[]supported_classes = sf.getDefaultCortexSupport ();
     // http://www.gael.fr/test#product1 and http://www.gael.fr/test#product2 
     // plus the ones defined by DHuS: http://www.gael.fr/drb#tgz, 
     // http://www.gael.fr/drb#gzip, http://www.gael.fr/drb#zip, 
     // http://www.gael.fr/drb#tar.
     
     // Scanner support is automatically computed according to the 
     // upcoming ontologies. by default scanner at least contains
     // http://www.gael.fr/drb#zip, so support list shall never be empty.
     Assert.assertTrue (supported_classes.length>1, "Missing supports");
  }
  
  @AfterClass
  public void after ()
  {
     FileUtils.deleteQuietly(tmpDir);
  }
  
  @AfterClass
  public void stopftp()
  {
     fakeFtpServer.stop ();
  }
}
