package fr.gael.drb.cortex.topic.sentinel2;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DrbImageManagerTest
{
   DrbImageManager mgr = new DrbImageManager();

   @Test
   public void getBand_Old()
   {
      String name_pvi="S2A_OPER_PVI_L1C_TL_EPA__20160704T162718_A000700_T52JFR.jp2";
      String name="S2A_OPER_MSI_L1C_TL_EPA__20160704T162718_A000700_T52JFR_B01.jp2";
      
      Assert.assertEquals(mgr.getBandIdFromName(name), "B01");
      Assert.assertEquals(mgr.getBandIdFromName(name_pvi), "no_band");
   }
   
   @Test
   public void getBand_Compact()
   {
      String name_pvi="T36JTT_20160914T074612_PVI.jp2";
      String name="T36JTT_20160914T074612_B01.jp2";

      Assert.assertEquals(mgr.getBandIdFromName(name), "B01");
      Assert.assertEquals(mgr.getBandIdFromName(name_pvi), "no_band");
   }
   
   @Test
   public void getMgrs_Old()
   {
      String name_pvi="S2A_OPER_PVI_L1C_TL_EPA__20160704T162718_A000700_T52JFR.jp2";
      String name="S2A_OPER_MSI_L1C_TL_EPA__20160704T162718_A000700_T52JFR_B01.jp2";
      
      Assert.assertEquals(mgr.getMGRSFromName(name), "52JFR");
      Assert.assertEquals(mgr.getMGRSFromName(name_pvi), "52JFR");
   }
   
   @Test
   public void getMgrs_Compact()
   {
      String name_pvi="T36JTT_20160914T074612_PVI.jp2";
      String name="T36JTT_20160914T074612_B01.jp2";

      Assert.assertEquals(mgr.getMGRSFromName(name), "36JTT");
      Assert.assertEquals(mgr.getMGRSFromName(name_pvi), "36JTT");
   }
   
   @Test 
   public void checkMGRS ()
   {
      String ok  = "T36JTT_20160914T074612_PVI.jp2";
      String nok = "T34MFR_20160914T074612_B01.jp2";
      String nok1 = "XXXXX_20160914T074612_B01.jp2";
      Assert.assertEquals(mgr.checkFilename (ok, false), true);
      Assert.assertEquals(mgr.checkFilename (nok, false), false);
      Assert.assertEquals(mgr.checkFilename (nok1, false), false);
   }
}
