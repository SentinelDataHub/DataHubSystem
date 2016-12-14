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
package fr.gael.dhus.service;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.database.dao.ConfigurationDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.dao.interfaces.DHusDumpException;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.database.object.config.Configuration;
import fr.gael.dhus.database.object.config.search.SolrConfiguration;
import fr.gael.dhus.service.exception.UserBadEncryptionException;
import fr.gael.dhus.system.config.ConfigurationManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import org.hsqldb.lib.tar.DbBackupMain;
import org.hsqldb.lib.tar.TarMalformatException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemService extends WebService
{
   private static final String BACKUP_DATABASE_NAME = "database";
   private static final String BACKUP_INDEX_NAME = "index";
   private static final Logger LOGGER = LogManager.getLogger(SystemService.class);
   
   public static final String RESTORATION_PROPERTIES = 
      "dhus-restoration-system.properties";
   
   private static int SOLR_VERSION=4;
   
   @Autowired
   private ConfigurationDao cfgDao;

   @Autowired
   private UserDao userDao;

   @Autowired
   private ConfigurationManager cfgManager;
   
   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=true, propagation=Propagation.REQUIRED)
   public Configuration getCurrentConfiguration ()
   {
      return cfgDao.getCurrentConfiguration ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public Configuration saveSystemSettings (Configuration cfg) throws
         IllegalArgumentException, IllegalAccessException,
         InvocationTargetException, CloneNotSupportedException
   {
      Configuration db_cfg = cfgDao.getCurrentConfiguration ();
      cfg = cfg.completeWith (db_cfg);
      db_cfg.setCronConfiguration (cfg.getCronConfiguration ());
      db_cfg.setGuiConfiguration (cfg.getGuiConfiguration ());
      db_cfg.setMessagingConfiguration (cfg.getMessagingConfiguration ());
      db_cfg.setNetworkConfiguration (cfg.getNetworkConfiguration ());
      db_cfg.setProductConfiguration (cfg.getProductConfiguration ());
      db_cfg.setSearchConfiguration (cfg.getSearchConfiguration ());
      db_cfg.setServerConfiguration (cfg.getServerConfiguration ());
      db_cfg.setSystemConfiguration (cfg.getSystemConfiguration ());
      cfgDao.update (db_cfg);
      return cfgDao.getCurrentConfiguration ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   public Configuration resetToDefaultConfiguration () throws Exception
   {
      cfgManager.reloadConfiguration ();
      return cfgDao.getCurrentConfiguration ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   @Caching (evict = {
      @CacheEvict (value = "user", allEntries = true),
      @CacheEvict (value = "userByName", allEntries = true)})
   public void changeRootPassword (String new_pwd, String old_pwd)
   {
      User root =
         userDao.getByName (
               cfgManager.getAdministratorConfiguration ().getName ());
      PasswordEncryption encryption = root.getPasswordEncryption ();
      if (encryption != PasswordEncryption.NONE) 
      {
         try
         {
            MessageDigest md = MessageDigest.getInstance(
                  encryption.getAlgorithmKey());
            old_pwd = new String(
                  Hex.encode(md.digest(old_pwd.getBytes("UTF-8"))));
         }
         catch (Exception e)
         {
            throw new UserBadEncryptionException (
                  "There was an error while encrypting password of root user",
                  e);
         }
      }
      if ( (old_pwd == null) || ("".equals (old_pwd)) ||
         ( !root.getPassword ().equals (old_pwd)))
         throw new SecurityException ("Wrong password.");

      if ( (new_pwd == null) || "".equals (new_pwd.trim ()))
         throw new SecurityException ("New password cannot be empty.");

      String password = new_pwd.trim ();
      root.setPassword (password);
      userDao.update (root);
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public List<Date> getDumpDatabaseList ()
   {
      List<Date>timestamps = new ArrayList<Date> ();
      
      File path_file = new File (cfgManager.getDatabaseConfiguration ()
            .getDumpPath ());
      File[]lst=path_file.listFiles (new FilenameFilter()
      {
         
         @Override
         public boolean accept (File dir, String name)
         {
            if (name.startsWith ("dump-"))
               return true;
            return false;
         }
      });
      
      if (lst == null)
      {
         return timestamps;
      }
         
      for (File f:lst)
      {
         String stimesamp = f.getName ().replaceAll ("dump-(.*)", "$1");
         long timestamp = Long.parseLong (stimesamp);
         Date date = new Date (timestamp);
         
         timestamps.add (date);
      }
      
      Collections.sort (timestamps, Collections.reverseOrder());
      
      return timestamps;
   }
   
   /**
    * Restores the desired dump of the database and Solr index. To  restore the
    * system must be stopped.This method produces the properties file to
    * generates new restored database and Solr index.
    * @param date of the dump to restore.
    * @throws DHusDumpException if date does not corresponds to an
    * existing dump.
    */
   public void restoreDumpDatabase (Date date)
   {
      File retorationDir = new File (
            cfgManager.getDatabaseConfiguration ().getDumpPath (),
            String.format ("dump-%020d", date.getTime ()));
      if ( !(retorationDir.exists () && retorationDir.isDirectory ()))
      {
         throw new DHusDumpException ("Dump of \"" + date + "\" not found");
      }

      try
      {
         String path = retorationDir.getAbsolutePath ();
         SolrConfiguration solrConfig = cfgManager.getSolrConfiguration ();
         FileWriter writer = new FileWriter (RESTORATION_PROPERTIES);

         // Database
         writer.append ("dhus.db.backup=")
               .append (path).append ("/")
               .append (BACKUP_DATABASE_NAME).append (".tar.gz");
         writer.append ('\n');
         writer.append ("dhus.db.location=").append (getDBDirectory ());
         writer.append ('\n');

         // Solr index
         writer.append ("dhus.solr.backup.name=").append (BACKUP_INDEX_NAME);
         writer.append ('\n');
         writer.append ("dhus.solr.backup.location=").append (path);
         writer.append ('\n');
         writer.append ("dhus.solr.core.name=").append (solrConfig.getCore ());
         writer.append ('\n');
         writer.append ("dhus.solr.home=").append (solrConfig.getPath ());
         writer.append ('\n');

         writer.flush ();
         writer.close ();
      }
      catch (IOException e)
      {
         LOGGER.warn("Can not perform restoration.", e);
         return;
      }

      DHuS.stop (8);
   }

   private String getDBDirectory ()
   {      
      String hsqlpath = cfgManager.getDatabaseConfiguration ().getPath ();

      File db =
         new File (hsqlpath.replace ('/', File.separatorChar)).getParentFile ();
      
      return db.getPath ();
   }

   /**
    * Generate a backup of DHuS system (database and Solr index)
    */
   public void dumpDatabase()
   {
      Date date = new Date ();
      String dirName = String.format ("dump-%020d", date.getTime ());
      File dir = new File (
            cfgManager.getDatabaseConfiguration ().getDumpPath (), dirName);
      if ( !(dir.mkdirs ()))
      {
         LOGGER.error("Can not create directory to save backup system.");
         return;
      }

      String path = dir.getAbsolutePath ();
      if ( !(backupDatabase (path) && backupSolr (path)))
      {
         LOGGER.warn("Deleting invalid backup system...");
         try
         {
            FileUtils.deleteDirectory (dir);
         }
         catch (IOException e)
         {
            LOGGER.error("Can not delete invalid backup system: " +
            path + ". Please delete it manually.");
         }
      }
   }

   /**
    * Performs a backup of database.
    * @param backupDirectory directory where put the backup.
    * @return true if backup is successful, otherwise false.
    */
   @Transactional (readOnly=false, propagation=Propagation.REQUIRED)
   private boolean backupDatabase (final String backupDirectory)
   {
      return userDao.getHibernateTemplate ().execute (
            new HibernateCallback<Boolean> ()
            {
               @Override
               public Boolean doInHibernate (Session session) throws
                     HibernateException, SQLException
               {
                  String backup = backupDirectory + "/" + 
                                  BACKUP_DATABASE_NAME + ".tar.gz";
                  String sql = "BACKUP DATABASE TO '" + backup +
                               "' NOT BLOCKING";
                  try
                  {
                     session.createSQLQuery (sql).executeUpdate ();
                  }
                  catch (HibernateException e)
                  {
                     return Boolean.FALSE;
                  }
                  return Boolean.TRUE;
               }
            });
   }

   /**
    * Performs a backup of Solr index.
    * @param backupDirectory directory where put the backup.
    * @return true if backup is successful, otherwise false.
    */
   private boolean backupSolr (final String backupDirectory)
   {
      StringBuilder request = new StringBuilder ();
      request.append (cfgManager.getServerConfiguration ().getUrl ());
      request.append ("/solr/dhus/replication?");
      request.append ("command=backup&location=").append (backupDirectory);
      request.append ("&name=").append (BACKUP_INDEX_NAME);

      try
      {
         URL url = new URL (request.toString ());
         HttpURLConnection con = (HttpURLConnection) url.openConnection ();
         InputStream input = con.getInputStream ();
         StringBuilder response = new StringBuilder ();
         byte[] buff = new byte[1024];
         int length;
         while ((length = input.read (buff)) != -1)
         {
            response.append (new String (buff, 0, length));
         }
         input.close ();
         con.disconnect ();
         LOGGER.debug(response.toString ());
      }
      catch (IOException e)
      {
         return Boolean.FALSE;
      }
      return Boolean.TRUE;
   }

   public void cleanDumpDatabase(int keepno)
   {
      File[]dumps = new File(
            cfgManager.getDatabaseConfiguration ().getDumpPath ())
            .listFiles(new FilenameFilter()
      {
         @Override
         public boolean accept(File path, String name)
         {
            if (name.startsWith("dump-"))
               return true;
            return false;
         }
      });
      if ((dumps!=null) && (dumps.length > keepno))
      {
         Arrays.sort(dumps, NameFileComparator.NAME_COMPARATOR);
         int last = dumps.length - keepno;
         for (int index=0; index<last; index++)
         {
            File dir = dumps[index];
            try
            {
               Date date = new Date (Long.parseLong (dir.getName ()
                     .replaceAll ("dump-(.*)", "$1")));
               LOGGER.info("Cleaned dump of " + date);
               FileUtils.deleteDirectory(dir);
            }
            catch (IOException e)
            {
               LOGGER.warn("Cannot delete directory " + dir.getPath() + " (" +
                  e.getMessage() + ")");
            }
         }
      }
   }

   /**
    * Restores DHuS in a previous state.
    */
   public static boolean restore ()
   {
      File restoreConfig = new File (RESTORATION_PROPERTIES);
      if (restoreConfig.exists () && restoreConfig.isFile ())
      {
         LOGGER.info("Performing restoration DHuS system...");
         try (FileInputStream stream = new FileInputStream (restoreConfig))
         {
            Properties properties = new Properties ();
            properties.load (stream);

            restoreDatabase (properties);
            restoreSolrIndex (properties);
         }
         catch (UnsupportedOperationException e)
         {
            LOGGER.error("Incomplete DHuS restoration file.", e);
            // DHuS integrity database and Solr index
            System.setProperty ("Archive.check", "true");
         }
         catch (Exception e)
         {
            LOGGER.fatal("Restoration failure.", e);
            return false;
         }
         finally
         {
            restoreConfig.delete ();
         }
      }
      return true;
   }

   /**
    * Performs database restoration.
    * No need of transaction here: DBmain is called before starting datasource.
    *
    * @param properties properties containing arguments to execute the restoration.
    */
   private static void restoreDatabase (Properties properties) throws
         IOException, TarMalformatException
   {
      String backup = properties.getProperty ("dhus.db.backup");
      String location = properties.getProperty ("dhus.db.location");

      if (backup == null || location == null)
      {
         throw new UnsupportedOperationException ();
      }

      FileUtils.deleteDirectory (new File(location));
      String[] args = {"--extract", backup, location};
      DbBackupMain.main (args);
      LOGGER.info("Database restored.");
   }

   private static void restoreSolrIndex (Properties properties) throws
   IOException, SolrServerException
   {
      if (SOLR_VERSION==4)
         restoreSolr4Index(properties);
      else
         restoreSolr5Index(properties);
   }
   /**
    * Performs Solr restoration.
    *
    * @param properties properties containing arguments to execute the restoration.
    */
   private static void restoreSolr5Index (Properties properties) throws
         IOException, SolrServerException
   {
      String solrHome = properties.getProperty ("dhus.solr.home");
      String coreName = properties.getProperty ("dhus.solr.core.name");
      final String name = properties.getProperty ("dhus.solr.backup.name");
      final String location = properties.getProperty (
            "dhus.solr.backup.location");

      if (solrHome == null || coreName == null || name == null ||
          location == null)
      {
         throw new UnsupportedOperationException ();
      }

      System.setProperty ("solr.solr.home", solrHome);
      CoreContainer core = new CoreContainer (solrHome);
      EmbeddedSolrServer server = new EmbeddedSolrServer (core, coreName);
      try
      {
         server.getCoreContainer ().load ();

         SolrQuery query = new SolrQuery();
         query.setRequestHandler("/replication");
         query.set("command", "restore");
         query.set("name", name);
         query.set("location", location);

         server.query(query);
         LOGGER.info("SolR indexes restored.");
      }
      finally
      {
         server.close();
      }
      
   }
   
   /**
   * Performs Solr restoration.
   *
   * @param properties properties containing arguments to execute the restoration.
   */
  private static void restoreSolr4Index (Properties properties) throws
        IOException, SolrServerException
  {
     String solr_home = properties.getProperty ("dhus.solr.home");
     String core_name = properties.getProperty ("dhus.solr.core.name");
     final String name = properties.getProperty ("dhus.solr.backup.name");
     final String location = properties.getProperty (
           "dhus.solr.backup.location");

     if (solr_home==null || core_name==null || name==null || location==null)
        throw new UnsupportedOperationException ();
     
     System.setProperty ("solr.solr.home", solr_home);
     File index_path = new File (location, "snapshot."+name);
     File target_path = Paths.get (solr_home, core_name, "data", name).toFile ();
     
     if (!index_path.exists())
        throw new UnsupportedOperationException (
           "solr source to restore not found (" + index_path + ").");
     if (!target_path.exists())
        throw new UnsupportedOperationException (
           "solr restore path not found (" + target_path + ").");
     
     FileUtils.cleanDirectory(target_path);
     FileUtils.copyDirectory(index_path, target_path);
     
     LOGGER.info("SolR indexes restored.");
  }
}
