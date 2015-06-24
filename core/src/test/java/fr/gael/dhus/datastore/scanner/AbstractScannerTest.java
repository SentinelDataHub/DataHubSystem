package fr.gael.dhus.datastore.scanner;

import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

import fr.gael.dhus.datastore.scanner.AbstractScanner;
import fr.gael.dhus.datastore.scanner.Scanner;
import fr.gael.drb.DrbDefaultMutableNode;
import fr.gael.drb.DrbNode;
import fr.gael.drbx.cortex.DrbCortexItemClass;

public class AbstractScannerTest
{
   private static Log logger = LogFactory.getLog (AbstractScannerTest.class);
   
   final DrbNode[] testItem = 
   {
      new DrbDefaultMutableNode("MyOwnTestItem.1"),
      new DrbDefaultMutableNode("MyOwnTestItem.2"),
      new DrbDefaultMutableNode("MyOwnTestItem.3"),
      new DrbDefaultMutableNode("MyOwnTestItem.tar"),
      new DrbDefaultMutableNode("GOM_MM__0PNPDE20041204_012208_000007492032_00360_14442_0031.N1"),
      new DrbDefaultMutableNode("GOM_NL__0PNPDE20041215_033131_000053822033_00018_14601_1448.N1"),
      new DrbDefaultMutableNode("GOM_TRA_1PNPDE20041113_230620_000000712032_00073_14155_0012.N1"),
      new DrbDefaultMutableNode("S1A_IW_GRDH_1SDV_20141009T155724_20141009T155749_002755_003184_8C3F.zip"),
      new DrbDefaultMutableNode("S1A_EW_GRDH_1SDH_20141008T090011_20141008T090027_002736_003117_107F.zip"),
      new DrbDefaultMutableNode("S1A_S4_RAW__0SSV_20141004T062011_20141004T062031_002676_002FBE_3B30.zip"),
      new DrbDefaultMutableNode("S1A_S3_RAW__0SSV_20141003T104036_20141003T104106_002664_002F79_D4F3.zip"),
      new DrbDefaultMutableNode("S1A_IW_RAW__0SSV_20141004T212603_20141004T212640_002685_002FF6_E94F.zip"),
      new DrbDefaultMutableNode("S1A_IW_SLC__1SDV_20141004T155003_20141004T155031_002682_002FE4_20EA.zip"),
      new DrbDefaultMutableNode("S1A_IW_SLC__1SDV_20141003T164823_20141003T164850_002668_002F8B_47F1.zip"),
   };
   
   /* Fonctionne sous eclipse mais pas en ligne de commande "mvn test" 
    *   TBC
    * */
   
   @Test
   public void matchesAll() throws InterruptedException
   {
      logger.info ("match all list :");
      Scanner scanner = getTestScanner();
      scanner.setUserPattern (null);
      Assert.assertEquals(scanner.scan(), testItem.length);
   }
   
   @Test
   public void matchesOne() throws InterruptedException
   {
      logger.info ("matchOne (pattern=\".*\\.tar\") :");
      Scanner scanner = getTestScanner();
      scanner.setUserPattern(".*\\.tar");
      Assert.assertEquals(scanner.scan(), 1);
   }

   @Test
   public void matchesGomos () throws InterruptedException
   {
      logger.info ("match GOMOS only (pattern=\"GOM.*\\.N1\") :");
      Scanner scanner = getTestScanner();
      scanner.setUserPattern("GOM.*\\.N1");
      Assert.assertEquals(scanner.scan(), 3);
   }

   @Test
   public void matchesThree () throws InterruptedException
   {
      logger.info ("match tree products (pattern=\".*\\.[0-9]{1}\") :");
      Scanner scanner = getTestScanner();
      scanner.setUserPattern(".*\\.[0-9]*");
      Assert.assertEquals(scanner.scan(), 3);
   }
   
   @Test
   public void matchesWrongPattern () throws InterruptedException
   {
      logger.info ("match wrong (pattern=\"*][\") :");
      Scanner scanner = getTestScanner();
      boolean exception_raised = false;
      try
      {
         scanner.setUserPattern("*][");
      }
      catch (PatternSyntaxException pse)
      {
         // exception raised: as expected:
         exception_raised=true;
      }
      Assert.assertEquals(exception_raised, true, 
            "Error in pattern has not been detected.");
      Assert.assertEquals(scanner.scan(), testItem.length);
   }
   
   @Test
   public void matchesAllSentinels () throws InterruptedException
   {
      logger.info ("match \"S1A_.*\\.zip\"");
      Scanner scanner = getTestScanner();
      scanner.setUserPattern("S1A_.*\\.zip");
      Assert.assertEquals(scanner.scan(), 7);
   }
   
   @Test
   public void matchesAllSlcGdmSentinels () throws InterruptedException
   {
      logger.info ("match \"S1[AB]_\\p{Upper}{2}_(SLC|GRDM).*\"");
      Scanner scanner = getTestScanner();
      scanner.setUserPattern("S1[AB]_\\p{Upper}{2}_(SLC|GRDM).*");
      Assert.assertEquals(scanner.scan(), 2);
   }
   
   @Test
   public void matchesAllEwSlcGdSentinels () throws InterruptedException
   {
      logger.info ("match \"S1A_EW_(SLC_|GRD(F|H|M))_.*\"");
      Scanner scanner = getTestScanner();
      scanner.setUserPattern("S1A_EW_(SLC_|GRD(F|H|M))_.*");
      Assert.assertEquals(scanner.scan(), 1);
   }

   public Scanner getTestScanner ()
   {
      Scanner scanner = new AbstractScanner(false)
      {
         @Override
         public int scan()
         {
            int result_count = 0;
            // Use scan() method to launch test :-)
            for (DrbNode node:testItem)
            {
               String pattern = getUserPattern()==null?"":getUserPattern().pattern();
               /*
               String classes = "";
               for (DrbCortexItemClass cl : getSupportedClasses())
               {
                  classes += "'" + cl.getOntClass().getNameSpace() + cl.getOntClass().getLocalName() + "', ";
               }
               logger.info("Supported classes : " + classes);
               */ 
               if (matches(node))
               {
                  result_count ++;
                  logger.info ("  + \"" + pattern + "\" Node " + node.getName() + " match !");
               }
               else
               {
                  logger.info ("  - \"" + pattern + "\" Node " + node.getName() + " Not match scanner.");
               }
            }
            return result_count;
         }
      };
      scanner.setSupportedClasses(ImmutableList.of (DrbCortexItemClass
            .getCortexItemClassByName ("http://www.gael.fr/drb#item")));
      return scanner;
   }
}
