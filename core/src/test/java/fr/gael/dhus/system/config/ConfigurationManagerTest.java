package fr.gael.dhus.system.config;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import fr.gael.dhus.database.object.config.Configuration;
import fr.gael.dhus.database.object.config.cron.ArchiveSynchronizationCronConfiguration;
import fr.gael.dhus.database.object.config.cron.CleanDatabaseCronConfiguration;
import fr.gael.dhus.database.object.config.cron.CleanDatabaseDumpCronConfiguration;
import fr.gael.dhus.database.object.config.cron.CronConfiguration;
import fr.gael.dhus.database.object.config.cron.DumpDatabaseCronConfiguration;
import fr.gael.dhus.database.object.config.cron.EvictionCronConfiguration;
import fr.gael.dhus.database.object.config.cron.FileScannersCronConfiguration;
import fr.gael.dhus.database.object.config.cron.SearchesCronConfiguration;
import fr.gael.dhus.database.object.config.cron.SendLogsCronConfiguration;
import fr.gael.dhus.database.object.config.cron.SystemCheckCronConfiguration;
import fr.gael.dhus.database.object.config.gui.GuiConfiguration;
import fr.gael.dhus.database.object.config.messaging.MailConfiguration;
import fr.gael.dhus.database.object.config.messaging.MailFromConfiguration;
import fr.gael.dhus.database.object.config.messaging.MailServerConfiguration;
import fr.gael.dhus.database.object.config.messaging.MessagingConfiguration;
import fr.gael.dhus.database.object.config.messaging.jms.JmsConfiguration;
import fr.gael.dhus.database.object.config.messaging.jms.JmsDestination;
import fr.gael.dhus.database.object.config.network.ChannelType;
import fr.gael.dhus.database.object.config.network.NetworkConfiguration;
import fr.gael.dhus.database.object.config.network.TrafficShapingType;
import fr.gael.dhus.database.object.config.product.ProductConfiguration;
import fr.gael.dhus.database.object.config.search.OdataConfiguration;
import fr.gael.dhus.database.object.config.search.SearchConfiguration;
import fr.gael.dhus.database.object.config.server.AbstractServerConfiguration;
import fr.gael.dhus.database.object.config.system.AdministratorConfiguration;
import fr.gael.dhus.database.object.config.system.ArchiveConfiguration;
import fr.gael.dhus.database.object.config.system.DatabaseConfiguration;
import fr.gael.dhus.database.object.config.system.NameConfiguration;
import fr.gael.dhus.database.object.config.system.ProcessingConfiguration;
import fr.gael.dhus.database.object.config.system.SupportConfiguration;
import fr.gael.dhus.database.object.config.system.SystemConfiguration;
import fr.gael.dhus.database.object.config.system.TomcatConfiguration;
import fr.gael.dhus.system.config.ConfigurationManager.ConfigurationLoader;

public class ConfigurationManagerTest
{
   Configuration conf ;
   @BeforeClass
   public void init () throws JAXBException, IOException
   {
      ConfigurationLoader loader = new ConfigurationLoader ();
      conf = loader.loadConfiguation (ClassLoader.
         getSystemResource ("dhus-config-test.xml"));
   }
  @Test
  public void  checkConfigurationLoaderCron ()
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     Assert.assertNotNull (cron, "cron configuration is null.");
     
     ArchiveSynchronizationCronConfiguration as = 
        cron.getArchiveSynchronizationConfiguration ();
     Assert.assertNotNull (as, "ArchivesSync :");
     Assert.assertNotNull (as.getSchedule (), "ArchivesSync schedule :");
     Assert.assertNotNull (as.isActive (), "ArchivesSync active flag :");
     
     CleanDatabaseCronConfiguration db = cron.getCleanDatabaseConfiguration ();
     Assert.assertNotNull (db, "CleanDB :");
     Assert.assertNotNull (db.getSchedule (), "CleanDB schedule :");
     Assert.assertNotNull (db.isActive (), "CleanDB active flag :");
     Assert.assertNotNull (db.getLogStatConfiguration (), "db cleanup stats: ");
     Assert.assertNotNull (db.getLogStatConfiguration ().getKeepPeriod (),
        "db cleanup stats keep period: ");

  }
  @Test (dependsOnMethods="checkConfigurationLoaderCron")
  public void  checkConfigurationLoaderCronArchiveSynchronization ()
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     
     ArchiveSynchronizationCronConfiguration as = 
        cron.getArchiveSynchronizationConfiguration ();
     
     Assert.assertNotNull (as, "archiveSynchronization: ");
     Assert.assertNotNull (as.getSchedule (),
        "archiveSynchronization schedule:");
     Assert.assertNotNull (as.isActive (),
        "archiveSynchronization active flag :");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderCron")
  public void  checkConfigurationLoaderCronCleanDatabse () 
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     
     CleanDatabaseCronConfiguration db = cron.getCleanDatabaseConfiguration ();
     Assert.assertNotNull (db, "cleanDatabase :");
     Assert.assertNotNull (db.getSchedule (), "cleanDatabase schedule :");
     Assert.assertNotNull (db.isActive (), "cleanDatabase active flag :");
     Assert.assertNotNull (db.getLogStatConfiguration (), 
        "cleanDatabase stats: ");
     Assert.assertNotNull (db.getLogStatConfiguration ().getKeepPeriod (),
        "cleanDatabase stats keep period: ");
     Assert.assertNotNull (db.getTempUsersConfiguration (),
        "cleanDatabase temp users");
     Assert.assertNotNull (db.getTempUsersConfiguration ().getKeepPeriod (),
              "cleanDatabase temp users keep period");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderCron")
  public void  checkConfigurationLoaderCronDumpDatabase ()
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     
     DumpDatabaseCronConfiguration dd = cron.getDumpDatabaseConfiguration ();
     
     Assert.assertNotNull (dd, "dumpDatabase: ");
     Assert.assertNotNull (dd.getSchedule (),"dumpDatabase schedule:");
     Assert.assertNotNull (dd.isActive (),"dumpDatabase active flag :");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderCron")
  public void  checkConfigurationLoaderCronCleanDatabaseDump ()
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     
     CleanDatabaseDumpCronConfiguration cdd = 
           cron.getCleanDatabaseDumpConfiguration ();
     
     Assert.assertNotNull (cdd, "cleanDatabaseDump: ");
     Assert.assertNotNull (cdd.getSchedule (),"cleanDatabaseDump schedule:");
     Assert.assertNotNull (cdd.isActive (),"cleanDatabaseDump active flag :");
     Assert.assertNotNull (cdd.getKeep (),"cleanDatabaseDump keep :");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderCron")
  public void  checkConfigurationLoaderCronEviction ()
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     
     EvictionCronConfiguration e = cron.getEvictionConfiguration ();
     
     Assert.assertNotNull (e, "eviction: ");
     Assert.assertNotNull (e.getSchedule (),"eviction schedule:");
     Assert.assertNotNull (e.isActive (),"eviction active flag :");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderCron")
  public void  checkConfigurationLoaderCronFileScanners ()
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     
     FileScannersCronConfiguration fs = cron.getFileScannersConfiguration ();
     
     Assert.assertNotNull (fs, "fileScanners: ");
     Assert.assertNotNull (fs.getSchedule (),"fileScanners schedule:");
     Assert.assertNotNull (fs.isActive (),"fileScanners active flag :");
     Assert.assertNotNull (fs.isSourceRemove (),
        "fileScanners remove source flag :");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderCron")
  public void  checkConfigurationLoaderCronSearches ()
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     
     SearchesCronConfiguration s = cron.getSearchesConfiguration ();
     
     Assert.assertNotNull (s, "searches: ");
     Assert.assertNotNull (s.getSchedule (),"searches schedule:");
     Assert.assertNotNull (s.isActive (),"searches active flag :");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderCron")
  public void  checkConfigurationLoaderCronSendLogs ()
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     
     SendLogsCronConfiguration s = cron.getSendLogsConfiguration ();
     
     Assert.assertNotNull (s, "sendLogs: ");
     Assert.assertNotNull (s.getSchedule (),"sendLogs schedule:");
     Assert.assertNotNull (s.isActive (),"sendLogs active flag :");
     Assert.assertNotNull (s.getAddresses (),"sendLogs address:");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderCron")
  public void  checkConfigurationLoaderCronSytemCheck()
  {
     CronConfiguration cron = conf.getCronConfiguration ();
     
     SystemCheckCronConfiguration s = cron.getSystemCheckConfiguration ();
     
     Assert.assertNotNull (s, "systemCheck: ");
     Assert.assertNotNull (s.getSchedule (),"systemCheck schedule:");
     Assert.assertNotNull (s.isActive (),"systemCheck active flag :");
  }

  
  @Test
  public void  checkConfigurationLoaderMessage () 
  {
     MessagingConfiguration message = conf.getMessagingConfiguration ();
     Assert.assertNotNull (message, "messaging configuration is null.");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderMessage")
  public void  checkConfigurationLoaderMessageJms()
  {
     MessagingConfiguration message = conf.getMessagingConfiguration ();
     
     JmsConfiguration jms = message.getJmsConfiguration ();
     
     Assert.assertNotNull (jms, "Jms: ");
     Assert.assertNotNull (jms.getFolder (),"jms folder :");
     Assert.assertNotNull (jms.getPort (),"jms port:");
     List<JmsDestination>jmsds=jms.getDestinations ();
     Assert.assertNotNull (jmsds,"Jms destinations:");
     for(JmsDestination d:jmsds)
     {
        Assert.assertNotNull (d,"Jms destination:");
        Assert.assertNotNull (d.getName (), "Jms destination name:");
        Assert.assertNotNull (d.getMessageTypes (),"Jms destination type:");
        Assert.assertNotNull (d.getDestination (),"Jms destination:");
     }
  }
  @Test (dependsOnMethods="checkConfigurationLoaderMessage")
  public void  checkConfigurationLoaderMessageMail ()
  {
     MessagingConfiguration message = conf.getMessagingConfiguration ();
     
     MailConfiguration mail = message.getMailConfiguration ();
     
     Assert.assertNotNull (mail, "mail: ");
     Assert.assertNotNull (mail.isOnUserCreate (),"mail on user create flag:");
     Assert.assertNotNull (mail.isOnUserDelete (),"mail on user delete flag:");
     Assert.assertNotNull (mail.isOnUserUpdate (),"mail on user delete flag:");
     
     MailServerConfiguration ms = mail.getServerConfiguration ();
     Assert.assertNotNull (ms, "mail server configuration:");
     Assert.assertNotNull (ms.getUsername (), "mail server username:");
     Assert.assertNotNull (ms.getPassword (), "mail server password:");
     Assert.assertNotNull (ms.getPort (), "mail server port:");
     Assert.assertNotNull (ms.getReplyTo (), "mail server reply-to:");
     Assert.assertNotNull (ms.getSmtp (), "mail server smtp:");

     MailFromConfiguration  mf = ms.getMailFromConfiguration (); 
     Assert.assertNotNull (mf, "mail server from:");
     Assert.assertNotNull (mf.getAddress (), "mail server from address:");
     Assert.assertNotNull (mf.getName (), "mail server from name:");
  }

  
  @Test
  public void  checkConfigurationLoaderNetwork ()
  {
     NetworkConfiguration network = conf.getNetworkConfiguration ();
     Assert.assertNotNull (network, "network configuration is null.");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderNetwork")
  public void  checkConfigurationLoaderNetworkInbound ()
  {
     NetworkConfiguration network = conf.getNetworkConfiguration ();
     checkBounds (network.getInbound (),"inbound channel");
  }
  @Test (dependsOnMethods="checkConfigurationLoaderNetwork")
  public void  checkConfigurationLoaderNetworkOutbound ()
  {
     NetworkConfiguration network = conf.getNetworkConfiguration ();
     checkBounds (network.getOutbound (), "outbound channel");
  }
  
  void checkBounds (TrafficShapingType bound, String message)
  {
     Assert.assertNotNull (bound, message);
     List<ChannelType> channels = bound.getChannel ();
     Assert.assertNotNull (channels, message+" channel types");
     
     for (ChannelType c: channels)
     {
        checkChannel (c, message+" sub channel");
     }
  }
  
  void checkChannel (ChannelType channel, String message)
  {
     Assert.assertNotNull (channel.getName (), message+", channel name");
     Assert.assertNotNull (channel.getWeight (), message+", channel weight");

     Assert.assertNotNull (channel, message+" channel");
     Assert.assertNotNull (channel.getClassifier (), message+", classifier");
     Assert.assertNotNull (channel.getClassifier ().getExcludes (), 
        message+", classifier/excludes");
     Assert.assertNotNull (channel.getClassifier ().getExcludes ().getExclude(), 
        message+", classifier/excludes/exclude");
     Assert.assertNotNull (channel.getClassifier ().getIncludes (), 
        message+", classifier/includes");
     Assert.assertNotNull (channel.getClassifier ().getIncludes ().getInclude(), 
        message+", classifier/includes/include");
     
     
     Assert.assertNotNull (channel.getDefaultUserQuotas (), 
        message+", user quota");
     Assert.assertNotNull (channel.getDefaultUserQuotas ().getMaxBandwidth (), 
        message+", user quota/max bandwidth");
     Assert.assertNotNull (channel.getDefaultUserQuotas ().getMaxConcurrent (), 
        message+", user quota/max concurrent");
     Assert.assertNotNull (channel.getDefaultUserQuotas ().getMaxCount (),
        message+", user quota/max count");
     Assert.assertNotNull (channel.getDefaultUserQuotas ().
        getMaxCumulativeSize (),message+", user quota/max cumulative size");
     Assert.assertNotNull (channel.getDefaultUserQuotas ().getMaxSize (),
        message+", user quota/max size");
     
     if (channel.getChannel () != null)
        for (ChannelType c:channel.getChannel ())
           checkChannel (c, message);
  }
  
  @Test
  public void  checkConfigurationLoaderGUI ()
  {
     GuiConfiguration gui = conf.getGuiConfiguration ();
     Assert.assertNotNull (gui, "gui configuration is null.");
  }
  @Test
  public void  checkConfigurationLoaderProduct ()
  {
     ProductConfiguration product = conf.getProductConfiguration ();
     Assert.assertNotNull (product, "product configuration is null.");
  }
  @Test
  public void  checkConfigurationLoaderSearch ()
  {
     SearchConfiguration search = conf.getSearchConfiguration ();
     Assert.assertNotNull (search, "search configuration is null.");
  }
  
  @Test(dependsOnMethods="checkConfigurationLoaderSearch")
  public void  checkConfigurationLoaderOdata ()
  {
     SearchConfiguration search = conf.getSearchConfiguration ();
     
     OdataConfiguration oc = search.getOdataConfiguration ();
     Assert.assertNotNull (oc, "odata configuration is null.");
     Assert.assertNotNull (oc.getMaxRows (), 
        "odata configuration maxRow is null.");
     Assert.assertEquals (oc.getMaxRows ().intValue (), 50,  
        "odata configuration maxRow value not expected.");
  }
  
  @Test
  public void  checkConfigurationLoaderServer ()
  {
     AbstractServerConfiguration server = conf.getServerConfiguration ();
     Assert.assertNotNull (server, "server configuration is null.");
  }
  @Test
  public void  checkConfigurationLoaderSystem ()
  {
     SystemConfiguration system = conf.getSystemConfiguration ();
     Assert.assertNotNull (system, "system configuration is null.");
     
     AdministratorConfiguration ac = system.getAdministratorConfiguration ();
     Assert.assertNotNull (ac, "system admin configuration is null.");

     ArchiveConfiguration arc = system.getArchiveConfiguration ();
     Assert.assertNotNull (arc, "system archive configuration is null.");

     DatabaseConfiguration db = system.getDatabaseConfiguration ();
     Assert.assertNotNull (db, "system database configuration is null.");
     
     NameConfiguration nc = system.getNameConfiguration ();
     Assert.assertNotNull (nc, "system name configuration is null.");
     
     ProcessingConfiguration proc = system.getProcessingConfiguration ();
     Assert.assertNotNull (proc, "system processing configuration is null.");

     SupportConfiguration sp = system.getSupportConfiguration ();
     Assert.assertNotNull (sp, "system support configuration is null.");
     
     TomcatConfiguration tc = system.getTomcatConfiguration ();
     Assert.assertNotNull (tc, "system tomcat configuration is null.");


  }
}
