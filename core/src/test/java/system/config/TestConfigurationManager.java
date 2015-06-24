package system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.gael.dhus.system.config.ConfigurationManager;

@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
//@ContextConfiguration(
//   locations = "classpath:fr/gael/dhus/spring/dhus-core-test-context.xml",
//   loader = DHuSTestContextLoader.class
//)
public class TestConfigurationManager extends AbstractTransactionalTestNGSpringContextTests
{
   @Autowired
   private ConfigurationManager configurationManager;
   
   private String varFolder = "local_dhus/";
   
   @Test
   public void testgetCleanDatabaseCronConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getCleanDatabaseCronConfiguration ());
      Assert.assertEquals (configurationManager.getCleanDatabaseCronConfiguration ().getSchedule (), "0 0 1 ? * *");
      Assert.assertEquals (configurationManager.getCleanDatabaseCronConfiguration ().getTempUsersConfiguration ().getKeepPeriod ().intValue (), 10);
   }

   @Test
   public void testgetDumpDatabaseCronConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getDumpDatabaseCronConfiguration ());
      Assert.assertEquals (configurationManager.getDumpDatabaseCronConfiguration ().getSchedule (), "0 0 3 ? * *");
   }

   @Test
   public void testgetCleanDatabaseDumpCronConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getCleanDatabaseDumpCronConfiguration ());
      Assert.assertEquals (configurationManager.getCleanDatabaseDumpCronConfiguration ().getSchedule (), "0 0 4 ? * *");
      Assert.assertEquals (configurationManager.getCleanDatabaseDumpCronConfiguration ().getKeep ().intValue (), 10);
   }

   @Test
   public void testgetEvictionCronConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getEvictionCronConfiguration ());
      Assert.assertEquals (configurationManager.getEvictionCronConfiguration ().getSchedule (), "0 0 21 ? * *");
   }

   @Test
   public void testgetFileScannersCronConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getFileScannersCronConfiguration ());
      Assert.assertEquals (configurationManager.getFileScannersCronConfiguration ().getSchedule (), "0 0 22 ? * *");
   }

   @Test
   public void testgetSearchesCronConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getSearchesCronConfiguration ());
      Assert.assertEquals (configurationManager.getSearchesCronConfiguration ().getSchedule (), "0 0 5 ? * *");
   }

   @Test
   public void testgetSendLogsCronConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getSendLogsCronConfiguration ());
      Assert.assertEquals (configurationManager.getSendLogsCronConfiguration ().getSchedule (), "0 0 0 ? * *");
      Assert.assertEquals (configurationManager.getSendLogsCronConfiguration ().getAddresses (), "dhus@xxx.xx");
   }

   @Test
   public void testgetGuiConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getGuiConfiguration ());
      Assert.assertEquals (configurationManager.getGuiConfiguration ().getCustomFolder (), "custom");
      Assert.assertEquals (configurationManager.getGuiConfiguration ().getBannerConfiguration ().getHeight ().intValue (), 100);
      Assert.assertEquals (configurationManager.getGuiConfiguration ().getBannerConfiguration ().getBackground (), "");
      Assert.assertEquals (configurationManager.getGuiConfiguration ().getBannerConfiguration ().getLeftImage (), "");
      Assert.assertEquals (configurationManager.getGuiConfiguration ().getBannerConfiguration ().getRightImage (), "");
      Assert.assertEquals (configurationManager.getGuiConfiguration ().getBannerConfiguration ().getTitle (), "");
      Assert.assertEquals (configurationManager.getGuiConfiguration ().getBannerConfiguration ().getTitleBackground (), "");
   }

   @Test
   public void testgetJmsConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getJmsConfiguration ());
      Assert.assertEquals (configurationManager.getJmsConfiguration ().getFolder (), varFolder+"/broker");
      Assert.assertEquals (configurationManager.getJmsConfiguration ().getPort ().intValue (), 61616);
      Assert.assertEquals (configurationManager.getJmsConfiguration ().getDestinations ().size (), 2);
   }

   @Test
   public void testgetMailConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getMailConfiguration ());
      Assert.assertEquals (configurationManager.getMailConfiguration ().isOnUserCreate ().booleanValue (), true);
      Assert.assertEquals (configurationManager.getMailConfiguration ().isOnUserDelete ().booleanValue (), true);
      Assert.assertEquals (configurationManager.getMailConfiguration ().isOnUserUpdate ().booleanValue (), true);
      Assert.assertEquals (configurationManager.getMailConfiguration ().getServerConfiguration ().getSmtp (), "smtp.gael.fr");
      Assert.assertEquals (configurationManager.getMailConfiguration ().getServerConfiguration ().getPort ().intValue (), 587);
      Assert.assertEquals (configurationManager.getMailConfiguration ().getServerConfiguration ().isTls ().booleanValue (), false);
      Assert.assertEquals (configurationManager.getMailConfiguration ().getServerConfiguration ().getUsername (), "dhus@gael.fr");
      Assert.assertEquals (configurationManager.getMailConfiguration ().getServerConfiguration ().getPassword (), "PASSWORD");
      Assert.assertEquals (configurationManager.getMailConfiguration ().getServerConfiguration ().getReplyTo (), "dhus@gael.fr");
      Assert.assertEquals (configurationManager.getMailConfiguration ().getServerConfiguration ().getMailFromConfiguration ().getAddress (), "dhus@gael.fr");
      Assert.assertEquals (configurationManager.getMailConfiguration ().getServerConfiguration ().getMailFromConfiguration ().getName (), "DHuS Support Team");
      Assert.assertEquals (configurationManager.getMailConfiguration ().getServerConfiguration ().getReplyTo (), "dhus@gael.fr");
   }

   @Test
   public void testisDataPublic ()
   {
      Assert.assertEquals (configurationManager.isDataPublic (), false);
   }

   @Test
   public void getProductConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getProductConfiguration ());
      Assert.assertEquals (configurationManager.getProductConfiguration ().getQuicklookConfiguration ().getHeight ().intValue (), 512);
      Assert.assertEquals (configurationManager.getProductConfiguration ().getQuicklookConfiguration ().getWidth ().intValue (), 512);
      Assert.assertEquals (configurationManager.getProductConfiguration ().getQuicklookConfiguration ().isCutting ().booleanValue (), false);
      Assert.assertEquals (configurationManager.getProductConfiguration ().getThumbnailConfiguration ().getHeight ().intValue (), 64);
      Assert.assertEquals (configurationManager.getProductConfiguration ().getThumbnailConfiguration ().getWidth ().intValue (), 64);
      Assert.assertEquals (configurationManager.getProductConfiguration ().getThumbnailConfiguration ().isCutting ().booleanValue (), false);
   }

   @Test
   public void testgetSolrConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getSolrConfiguration ());
      Assert.assertEquals (configurationManager.getSolrConfiguration ().getPath (), varFolder+"/solr");
      Assert.assertEquals (configurationManager.getSolrConfiguration ().getCore (), "dhus");
      Assert.assertEquals (configurationManager.getSolrConfiguration ().getSchemaPath (), "");
      Assert.assertEquals (configurationManager.getSolrConfiguration ().getSynonymPath (), "");
   }

   @Test
   public void testgetOdataConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getOdataConfiguration ());
      Assert.assertEquals (configurationManager.getOdataConfiguration ().getMaxRows ().intValue (), 50);
   }

   @Test
   public void testgetGeonameConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getGeonameConfiguration ());
      Assert.assertEquals (configurationManager.getGeonameConfiguration ().getUsername (), "dhus");
   }

   @Test
   public void testgetGeocoderConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getGeocoderConfiguration ());
      Assert.assertEquals (configurationManager.getGeocoderConfiguration ().getUrl (), "http://nominatim.openstreetmap.org");
   }

   @Test
   public void testgetNominatimConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getNominatimConfiguration ());
      Assert.assertEquals (configurationManager.getNominatimConfiguration ().isBoundingBox ().booleanValue (), false);
      Assert.assertEquals (configurationManager.getNominatimConfiguration ().getMaxPointNumber ().intValue (), 50);
   }

   @Test
   public void testgetServerConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getServerConfiguration ());
      Assert.assertEquals (configurationManager.getServerConfiguration ().getProtocol (), "http");
      Assert.assertEquals (configurationManager.getServerConfiguration ().getHost (), "localhost");
      Assert.assertEquals (configurationManager.getServerConfiguration ().getPort ().intValue (), 8080);
      Assert.assertEquals (configurationManager.getServerConfiguration ().getExternalProtocol (), "http");
      Assert.assertEquals (configurationManager.getServerConfiguration ().getExternalHostname (), "dhus2");
      Assert.assertEquals (configurationManager.getServerConfiguration ().getUrl (), "http://localhost:8080/");
      Assert.assertEquals (configurationManager.getServerConfiguration ().getExternalUrl (), "http://dhus2:8282/test/");      
   }

   @Test
   public void testgetFtpServerConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getFtpServerConfiguration ());
      Assert.assertEquals (configurationManager.getFtpServerConfiguration ().getPort ().intValue (), 2121);    
   }

   @Test
   public void testgetArchiveConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getArchiveConfiguration ());
      Assert.assertEquals (configurationManager.getArchiveConfiguration ().getPath (), varFolder+"/dhusdata");
      Assert.assertEquals (configurationManager.getArchiveConfiguration ().getEvictionConfiguration ().getMaxDiskUsage ().intValue (), 5);
      Assert.assertEquals (configurationManager.getArchiveConfiguration ().getEvictionConfiguration ().getMaxEvictedProducts ().intValue (), 500);
      Assert.assertEquals (configurationManager.getArchiveConfiguration ().getEvictionConfiguration ().getKeepPeriod ().intValue (), 12);
      Assert.assertEquals (configurationManager.getArchiveConfiguration ().getIncomingConfiguration ().getPath (), varFolder+"/incoming");
   }

   @Test
   public void testgetTomcatConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getTomcatConfiguration ());
      Assert.assertEquals (configurationManager.getTomcatConfiguration ().getPath (), varFolder+"/tomcat");    
   }

   @Test
   public void testgetSupportConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getSupportConfiguration ());
      Assert.assertEquals (configurationManager.getSupportConfiguration ().getName (), "DHuS Support");
      Assert.assertEquals (configurationManager.getSupportConfiguration ().getMail (), "dhus-support@gael.fr");
   }

   @Test
   public void testgetAdministratorConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getAdministratorConfiguration ());
      Assert.assertEquals (configurationManager.getAdministratorConfiguration ().getName (), "root");
      Assert.assertEquals (configurationManager.getAdministratorConfiguration ().getPassword (), "****");    
   }

   @Test
   public void testgetNameConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getNameConfiguration ());
      Assert.assertEquals (configurationManager.getNameConfiguration ().getLongName (), "Data Hub Service");
      Assert.assertEquals (configurationManager.getNameConfiguration ().getShortName (), "DHuS");    
   }

   @Test
   public void testgetProcessingConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getProcessingConfiguration ());
      Assert.assertEquals (configurationManager.getProcessingConfiguration ().getCorePoolSize ().intValue (), 1);
      Assert.assertEquals (configurationManager.getProcessingConfiguration ().getMaxPoolSize ().intValue (), 10);
      Assert.assertEquals (configurationManager.getProcessingConfiguration ().getQueueCapacity ().intValue (), 10000);
   }

   @Test
   public void testgetDatabaseConfiguration ()
   {
      Assert.assertNotNull (configurationManager.getDatabaseConfiguration ());
      Assert.assertEquals (configurationManager.getDatabaseConfiguration ().getPath (), varFolder+"/database/hsqldb");
      Assert.assertEquals (configurationManager.getDatabaseConfiguration ().getDumpPath (), varFolder+"/database_dump");    
   }
}