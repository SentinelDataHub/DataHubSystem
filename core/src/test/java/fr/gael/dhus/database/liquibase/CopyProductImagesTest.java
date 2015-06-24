package fr.gael.dhus.database.liquibase;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CopyProductImagesTest {

  @Test
  public void pattern_1()
  {
     String download_path = "/data_1/tmp/dhus/var/incoming/8/4/dhus_entry/S1A_EW_GRDH_1SDH_20140417T185659_20140417T185805_000203_000108_2434.zip";
     String expected = "/data_1/tmp/dhus/var/incoming/8/4/dhus_entry/S1A_EW_GRDH_1SDH_20140417T185659_20140417T185805_000203_000108_2434-ql.gif";
     String path = download_path.replaceAll ("(?i)(.*).zip", "$1-ql.gif");
     Assert.assertEquals (path, expected);
  }
}
