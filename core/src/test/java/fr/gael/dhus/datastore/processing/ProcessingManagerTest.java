package fr.gael.dhus.datastore.processing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.io.Files;

import fr.gael.dhus.util.UnZip;
import fr.gael.drb.impl.DrbFactoryResolver;
import fr.gael.drbx.cortex.DrbCortexMetadataResolver;
import fr.gael.drbx.cortex.DrbCortexModel;

public class ProcessingManagerTest
{
   File sample=null;
   // Same size for drb/system sizes.
   private static long SIZE=1650599;
   
   @BeforeClass
   void init() throws IOException
   {
      InputStream is=ProcessingManager.class.getResourceAsStream("size-test.zip");
      
      File tmp_folder = Files.createTempDir();
      File output = new File (tmp_folder, "size-test.zip");
      FileUtils.copyInputStreamToFile(is, output);
      is.close();
      
      sample = output;
   }
   
   @AfterClass
   void exit()
   {
      FileUtils.deleteQuietly(sample.getParentFile());
   }

   @Test
   public void drb_size() throws IOException
   {
      DrbFactoryResolver.setMetadataResolver (new DrbCortexMetadataResolver (
            DrbCortexModel.getDefaultModel ()));
      
      ProcessingManager mgr = new ProcessingManager();
      long size = mgr.drb_size(sample);
      Assert.assertEquals(size, SIZE);
   }

   @Test
   public void size()
   {
      ProcessingManager mgr = new ProcessingManager();
      long size = mgr.size(sample);
      Assert.assertEquals(size, SIZE);
   }

   @Test
   public void system_size() throws IOException, CompressorException, ArchiveException
   {
      ProcessingManager mgr = new ProcessingManager();
      long size = mgr.system_size(sample);
      Assert.assertEquals(size, 494928);
      
      File folder=sample.getParentFile();
      File extaction_folder=new File(folder, "unzip");
      extaction_folder.mkdirs();
      
      UnZip.unCompress(sample.getAbsolutePath(), 
         extaction_folder.getAbsolutePath());
      
      File tocheck = extaction_folder.listFiles()[0];
      size = mgr.system_size(tocheck);
      Assert.assertEquals(size, SIZE, tocheck.getAbsolutePath());
   }
}
