/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 GAEL Systems
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gael.dhus.system.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.Transient;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import fr.gael.dhus.database.dao.CollectionDao;
import fr.gael.dhus.database.dao.ConfigurationDao;
import fr.gael.dhus.database.dao.EvictionDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.Collection;
import fr.gael.dhus.database.object.Eviction;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.config.Configuration;
import fr.gael.dhus.database.object.config.cron.ArchiveSynchronizationCronConfiguration;
import fr.gael.dhus.database.object.config.cron.CleanDatabaseCronConfiguration;
import fr.gael.dhus.database.object.config.cron.CleanDatabaseDumpCronConfiguration;
import fr.gael.dhus.database.object.config.cron.DumpDatabaseCronConfiguration;
import fr.gael.dhus.database.object.config.cron.EvictionCronConfiguration;
import fr.gael.dhus.database.object.config.cron.FileScannersCronConfiguration;
import fr.gael.dhus.database.object.config.cron.SearchesCronConfiguration;
import fr.gael.dhus.database.object.config.cron.SendLogsCronConfiguration;
import fr.gael.dhus.database.object.config.cron.SystemCheckCronConfiguration;
import fr.gael.dhus.database.object.config.gui.GuiConfiguration;
import fr.gael.dhus.database.object.config.messaging.MailConfiguration;
import fr.gael.dhus.database.object.config.messaging.jms.JmsConfiguration;
import fr.gael.dhus.database.object.config.network.NetworkConfiguration;
import fr.gael.dhus.database.object.config.product.DownloadConfiguration;
import fr.gael.dhus.database.object.config.product.ProductConfiguration;
import fr.gael.dhus.database.object.config.search.GeocoderConfiguration;
import fr.gael.dhus.database.object.config.search.GeonameConfiguration;
import fr.gael.dhus.database.object.config.search.NominatimConfiguration;
import fr.gael.dhus.database.object.config.search.OdataConfiguration;
import fr.gael.dhus.database.object.config.search.SolrConfiguration;
import fr.gael.dhus.database.object.config.server.FtpServerConfiguration;
import fr.gael.dhus.database.object.config.server.ServerConfiguration;
import fr.gael.dhus.database.object.config.system.AdministratorConfiguration;
import fr.gael.dhus.database.object.config.system.ArchiveConfiguration;
import fr.gael.dhus.database.object.config.system.DatabaseConfiguration;
import fr.gael.dhus.database.object.config.system.EvictionConfiguration;
import fr.gael.dhus.database.object.config.system.NameConfiguration;
import fr.gael.dhus.database.object.config.system.ProcessingConfiguration;
import fr.gael.dhus.database.object.config.system.SupportConfiguration;
import fr.gael.dhus.database.object.config.system.TomcatConfiguration;
import fr.gael.dhus.datastore.eviction.EvictionStrategy;
import fr.gael.dhus.messaging.jms.Message;
import fr.gael.dhus.messaging.jms.Message.MessageType;

@Service
public class ConfigurationManager implements InitializingBean
{      
   @Autowired
   private CollectionDao collectionDao;
   
   @Autowired
   private ConfigurationDao configurationDao;
   
   @Autowired
   private EvictionDao evictionDao;
   
   @Autowired
   private UserDao userDao;
   
   @Autowired
   private ConfigurationLoader configLoader;
   
   /**
    * Configuration containing only non stored fields.
    * See {@link Transient} fields of {@link Configuration} Object and its 
    * children.
    */
   private Configuration notStoredPartOfConfiguration;

   @Override
   public void afterPropertiesSet() throws Exception
   {
      // Need to load configuration every time to load non persistent fields
      Configuration loadedConf = configLoader.getLoadedConfiguration ();
      notStoredPartOfConfiguration = configLoader.
         getNotStoredPartOfConfiguration ();
      Configuration storedConf = configurationDao.getCurrentConfiguration ();

      // detect if no configuration is already stored or if a partial one is 
      // stored (typically stored by liquibase migration script, so this 
      // configuration is missing some fields like cron configuration)
      if (storedConf != null && storedConf.getCronConfiguration () == null)
      {
         loadedConf = storedConf.completeWith (loadedConf);
         configurationDao.update(loadedConf);
      }
            
      if (storedConf == null)
      {
         configurationDao.create(loadedConf);        
      }

      AdministratorConfiguration cfg = loadedConf.getSystemConfiguration ().
         getAdministratorConfiguration ();
      User rootUser = userDao.getByName (cfg.getName ());
      if (rootUser != null)
      {
         // If root User exists, update his roles by security
         ArrayList<Role> roles = new ArrayList<Role> ();
         for (Role role : Role.values ())
            roles.add (role);
         rootUser.setRoles (roles);
         userDao.update (rootUser);
      }
      else
      {
         // Create it
         rootUser = new User ();
         rootUser.setUsername (cfg.getName ());
         rootUser.setPassword (cfg.getPassword ());
         rootUser.setCreated (new Date ());
         ArrayList<Role> roles = new ArrayList<Role> ();
         for (Role role : Role.values ())
            roles.add (role);
         rootUser.setRoles (roles);
         rootUser.setDomain ("Other");
         rootUser.setSubDomain ("System");
         rootUser.setUsage ("Other");
         rootUser.setSubUsage ("System");
         userDao.create (rootUser);
      }
       
      // Then create the root Collection
      if (collectionDao.getRootCollection () == null)
      {
         Collection rootCollection = new Collection ();
         HashSet<User> users = new HashSet<User> ();
         rootCollection.setDescription ("Root of all the collections");
         rootCollection.setName (Collection.ROOT_NAME);
         if (isDataPublic())
         {
            users.add (userDao.getPublicData ());
//             rootCollection.setPublicData (true);
         }
         users.add (rootUser);
         rootCollection.setAuthorizedUsers (users);
         collectionDao.create (rootCollection);
      }
         
      // Store the default eviction settings
      if (evictionDao.getEviction () == null)
      {
         EvictionConfiguration evictionConf = loadedConf.
            getSystemConfiguration ().getArchiveConfiguration ().
            getEvictionConfiguration ();
         Eviction eviction = new Eviction ();
         eviction.setMaxDiskUsage (evictionConf.getMaxDiskUsage ());
         eviction.setKeepPeriod (evictionConf.getKeepPeriod ());
         eviction.setStrategy (EvictionStrategy.NONE);
         eviction.setMaxProductNumber (evictionConf.getMaxEvictedProducts ());
         evictionDao.create (eviction);
      }
   }
   
   public void reloadConfiguration()
   {
      Configuration loadedConf = configLoader.getLoadedConfiguration ();
      // not reloading not stored part of configuration
      Configuration storedConf = configurationDao.getCurrentConfiguration ();

      // Delete stored configuration when reloading it
      configurationDao.delete (storedConf);      
      configurationDao.create(loadedConf);               
   }
   
   // Crons configurations
   public ArchiveSynchronizationCronConfiguration 
      getArchiveSynchronizationCronConfiguration ()
   {
      return notStoredPartOfConfiguration.getCronConfiguration ().
         getArchiveSynchronizationConfiguration ();
   }
   
   public CleanDatabaseCronConfiguration getCleanDatabaseCronConfiguration ()
   {
      return notStoredPartOfConfiguration.getCronConfiguration ().
         getCleanDatabaseConfiguration ();
   }

   public DumpDatabaseCronConfiguration getDumpDatabaseCronConfiguration ()
   {
      return notStoredPartOfConfiguration.getCronConfiguration ().
         getDumpDatabaseConfiguration ();     
   }

   public CleanDatabaseDumpCronConfiguration 
      getCleanDatabaseDumpCronConfiguration()
   {
      return notStoredPartOfConfiguration.getCronConfiguration ().
         getCleanDatabaseDumpConfiguration ();      
   }

   public EvictionCronConfiguration getEvictionCronConfiguration ()
   {
      return notStoredPartOfConfiguration.getCronConfiguration ().
         getEvictionConfiguration ();      
   }

   public FileScannersCronConfiguration getFileScannersCronConfiguration ()
   {
      return notStoredPartOfConfiguration.getCronConfiguration ().
         getFileScannersConfiguration ();
   }

   public SearchesCronConfiguration getSearchesCronConfiguration ()
   {
      return notStoredPartOfConfiguration.getCronConfiguration ().
         getSearchesConfiguration ();
   }

   public SendLogsCronConfiguration getSendLogsCronConfiguration ()
   {
      return notStoredPartOfConfiguration.getCronConfiguration ().
         getSendLogsConfiguration ();
   }
   
   public SystemCheckCronConfiguration getSystemCheckCronConfiguration ()
   {
      return notStoredPartOfConfiguration.getCronConfiguration ().
         getSystemCheckConfiguration ();
   }

   // GUI configurations
   public GuiConfiguration getGuiConfiguration ()
   {
      return notStoredPartOfConfiguration.getGuiConfiguration ();
   }
   
   // Messaging configurations
   public JmsConfiguration getJmsConfiguration ()
   {
      return notStoredPartOfConfiguration.getMessagingConfiguration ().
         getJmsConfiguration ();
   }
   
   public MailConfiguration getMailConfiguration ()
   {
      return configurationDao.getCurrentConfiguration ().
         getMessagingConfiguration ().getMailConfiguration ();
   }
   
   // Network configuration
   public NetworkConfiguration getNetworkConfiguration ()
   {
      return notStoredPartOfConfiguration.getNetworkConfiguration ();
   }

   // Products configuration  
   public boolean isDataPublic()
   {
      return notStoredPartOfConfiguration.getProductConfiguration ().
         isPublicData ();
   }
   
   public DownloadConfiguration getDownloadConfiguration ()
   {
      return notStoredPartOfConfiguration.
         getProductConfiguration ().getDownloadConfiguration ();
   }
   
   public ProductConfiguration getProductConfiguration ()
   {
      return notStoredPartOfConfiguration.
         getProductConfiguration ();
   }

   // Search configurations
   public SolrConfiguration getSolrConfiguration ()
   {
      return notStoredPartOfConfiguration.getSearchConfiguration ().
         getSolrConfiguration ();
   }
   
   public OdataConfiguration getOdataConfiguration ()
   {
      return notStoredPartOfConfiguration.
         getSearchConfiguration ().getOdataConfiguration ();
   }
   
   public GeonameConfiguration getGeonameConfiguration ()
   {
      return notStoredPartOfConfiguration.
         getSearchConfiguration ().getGeocoderConfiguration ().
         getGeonameConfiguration ();
   }
   
   public GeocoderConfiguration getGeocoderConfiguration ()
   {
      return notStoredPartOfConfiguration.
         getSearchConfiguration ().getGeocoderConfiguration ();
   }
   
   public NominatimConfiguration getNominatimConfiguration ()
   {
      return notStoredPartOfConfiguration.
         getSearchConfiguration ().getGeocoderConfiguration ().
         getNominatimConfiguration ();
   }
   
   // Server configurations
   public ServerConfiguration getServerConfiguration ()
   {
      return (ServerConfiguration) notStoredPartOfConfiguration.
         getServerConfiguration ();
   }
   
   public FtpServerConfiguration getFtpServerConfiguration ()
   {
      return notStoredPartOfConfiguration.getServerConfiguration ().
         getFtpServerConfiguration ();
   }
   
   // System configurations
   public ArchiveConfiguration getArchiveConfiguration ()
   {
      return notStoredPartOfConfiguration.getSystemConfiguration ().
         getArchiveConfiguration ();
   }

   public TomcatConfiguration getTomcatConfiguration()
   {
      return notStoredPartOfConfiguration.getSystemConfiguration ().
         getTomcatConfiguration ();
   }

   public SupportConfiguration getSupportConfiguration ()
   {
      return configurationDao.getCurrentConfiguration ().
         getSystemConfiguration ().getSupportConfiguration ();
   }

   public AdministratorConfiguration getAdministratorConfiguration ()
   {
      return notStoredPartOfConfiguration.getSystemConfiguration ().
         getAdministratorConfiguration ();
   }

   public NameConfiguration getNameConfiguration ()
   {
      return notStoredPartOfConfiguration.
         getSystemConfiguration ().getNameConfiguration ();
   }

   public ProcessingConfiguration getProcessingConfiguration ()
   {
      return notStoredPartOfConfiguration.getSystemConfiguration ().
         getProcessingConfiguration ();
   }

   public DatabaseConfiguration getDatabaseConfiguration ()
   {
      return notStoredPartOfConfiguration.getSystemConfiguration ().
         getDatabaseConfiguration ();
   }
   
   @Component ("configurationLoader")
   static class ConfigurationLoader implements InitializingBean
   {
      private Logger logger = LogManager.getLogger ();
               
      /**
       * Configuration containing only non stored fields.
       * See {@link Transient} fields of {@link Configuration} Object and 
       * its children.
       */
      private Configuration notStoredPartOfConfiguration;
      
      private Configuration loadedConfiguration;
      
      @Override
      public void afterPropertiesSet() throws Exception
      {
         Configuration _loadedConfiguration  = null;
         Configuration internalConfiguration = null;
         try
         {
            _loadedConfiguration = loadConfiguation (ClassLoader.
               getSystemResource ("dhus.xml"));
         }
         catch (Exception e)
         {
            throw new ConfigurationException("User configuration error.", e);
         }
         
         try
         {
            internalConfiguration = loadConfiguation (
               ClassLoader.getSystemResource ("internal_dhus.xml"));
         }
         catch (Exception e)
         {
            throw new ConfigurationException("Internal configuration error.",e);
         }

         loadedConfiguration = _loadedConfiguration.completeWith (
            internalConfiguration);

         notStoredPartOfConfiguration = loadedConfiguration.getNotStoredPart ();
         // Temp fix waiting full configurability in GUI
         notStoredPartOfConfiguration.setCronConfiguration (
            loadedConfiguration.getCronConfiguration ());
         notStoredPartOfConfiguration.setProductConfiguration (
            loadedConfiguration.getProductConfiguration ());
         notStoredPartOfConfiguration.setSearchConfiguration (
            loadedConfiguration.getSearchConfiguration ());
         notStoredPartOfConfiguration.getSystemConfiguration ().setNameConfiguration (
            loadedConfiguration.getSystemConfiguration ().getNameConfiguration ());
      }
      
      /**
       * Load the passed configuration URL into {@link Configuration} class.
       * @param configuration the configuration to parsed.
       * @return the fulfilled configuration data set.
       * @throws JAXBException when input is wrong.
       * @throws IOException when input cannot be accessed.
       */
      Configuration loadConfiguation (URL configuration)
            throws JAXBException, IOException
      {
         JAXBContext context = JAXBContext.newInstance (
            "fr.gael.dhus.database.object.config");
         Unmarshaller unmarshaller = context.createUnmarshaller ();
         
         logger.info ("Loading configuration from " + 
            configuration.toExternalForm ());
         
         /* Validation fails ! */
         boolean schemaCheck = !("false".equalsIgnoreCase (
            System.getProperty ("checkUserConfiguration")));
         
         if (schemaCheck)
         {
            unmarshaller.setEventHandler (new ValidationEventHandler()
            {
               @Override
               public boolean handleEvent (ValidationEvent event)
               {
                  switch (event.getSeverity ())
                  {
                     case ValidationEvent.WARNING:
                     case ValidationEvent.ERROR:
                     case ValidationEvent.FATAL_ERROR:
                        logger.error (new Message (MessageType.SYSTEM,
                           "Configuration parsing failure at line " +
                           event.getLocator ().getLineNumber () + ", column " +
                           event.getLocator ().getColumnNumber () + ": " +
                           event.getMessage ()));
                        break;
                  }
                  return false;
               }
            });
         }

         InputStream configStream = configuration.openConnection ().
            getInputStream ();

         Configuration _loadedConfiguration = null;
         try
         {
            _loadedConfiguration =
               unmarshaller.unmarshal (new StreamSource (configStream),
                  Configuration.class).getValue ();
         }
         finally
         {
            configStream.close ();
         }
         
         return _loadedConfiguration;
      }
      
      // Used in dhus-core-database.xml 
      public String getDatabasePath()
      {
         return notStoredPartOfConfiguration.getSystemConfiguration ().
            getDatabaseConfiguration ().getPath ();
      }
      
      // Used in dhus-core-database.xml 
      public String getDatabaseSettings()
      {
         return notStoredPartOfConfiguration.getSystemConfiguration ().
            getDatabaseConfiguration ().getSettings ();
      }
      
      public Configuration getLoadedConfiguration ()
      {
         return loadedConfiguration;
      }
      
      public Configuration getNotStoredPartOfConfiguration ()
      {
         return notStoredPartOfConfiguration;
      }
   }
}
