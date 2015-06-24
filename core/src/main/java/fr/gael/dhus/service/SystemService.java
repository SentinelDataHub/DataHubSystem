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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.database.dao.ConfigurationDao;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.dao.interfaces.DHusDumpException;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.User.PasswordEncryption;
import fr.gael.dhus.database.object.config.Configuration;
import fr.gael.dhus.service.exception.UserBadEncryptionException;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * @author pidancier
 */
@Service
public class SystemService extends WebService
{
   private static Log logger = LogFactory.getLog (SystemService.class);
   
   @Autowired
   private ConfigurationDao cfgDao;

   @Autowired
   private UserDao userDao;

   @Autowired
   private ConfigurationManager cfgManager;
   
   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public Configuration getCurrentConfiguration ()
   {
      return cfgDao.getCurrentConfiguration ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public Configuration saveSystemSettings (Configuration cfg) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, CloneNotSupportedException
   {
      Configuration dbCfg = cfgDao.getCurrentConfiguration ();
      Configuration c = cfg.completeWith (dbCfg);
      // Id is not copied with "completeWith" method
      c.setID (dbCfg.getID ());
      
      cfgDao.update (c);
      return cfgDao.getCurrentConfiguration ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public Configuration resetToDefaultConfiguration () throws Exception
   {
      cfgManager.reloadConfiguration ();
      return cfgDao.getCurrentConfiguration ();
   }

   @PreAuthorize ("hasRole('ROLE_SYSTEM_MANAGER')")
   public void changeRootPassword (String new_pwd, String old_pwd)
   {
      User root =
         userDao.getByName (cfgManager.getAdministratorConfiguration ().getName ());
      PasswordEncryption encryption = root.getPasswordEncryption ();
      if (encryption != PasswordEncryption.NONE) 
      {
         try
         {
            MessageDigest md = MessageDigest.getInstance(encryption.getAlgorithmKey());
            old_pwd = new String(Hex.encode(md.digest(old_pwd.getBytes("UTF-8"))));
         }
         catch (Exception e)
         {
            throw new UserBadEncryptionException ("There was an error while encrypting password of root user", e);
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
      
      File path_file = new File (cfgManager.getDatabaseConfiguration ().getDumpPath ());
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
    * Restores the desired dump of the DB. To  restore the DB the system must
    * be stopped.This method produces the script that generates new retored DB. 
    * @param date of the dump to restore.
    * @throws DHusDumpException if date does not corresponds to an existing dump.
    */
   public void restoreDumpDatabase (Date date)
   {
      String dbDir = getDBDirectory ();
      
      File dump_file = new File (cfgManager.getDatabaseConfiguration ().getDumpPath (),
         String.format ("dump-%020d", date.getTime ()));
      
      if (!dump_file.exists ())
      {
         throw new DHusDumpException ("Dump of \"" + date.toString () + 
            "\" not found");
      }
      
      String restore_command = "java -cp " + 
         getDHuSJar ().replaceAll ("\\\\", "/") + 
         " fr.gael.dhus.database.util.RestoreDatabase " + 
         dump_file.getPath ().replaceAll ("\\\\", "/") + " " + 
         dbDir.replaceAll ("\\\\", "/");
      
      FileWriter  fstream=null; 
      BufferedWriter out=null;
      try
      {
         fstream = new FileWriter("start_first.sh");
         out = new BufferedWriter(fstream);
         out.write("#!/bin/sh\n");
         out.write(restore_command + "\n");
         out.write("rm $0");
      }
      catch (IOException e)
      {
         logger.error ("Cannot write \"start_first.sh\" script file", e);
      }
      finally
      {
         //Close the output stream
         try
         {
            out.close();
         }
         catch (IOException e) { /* noop */}
      }
      
      logger.info (restore_command);
      DHuS.stop (8);
      DHuS.start ();
   }
   
   private String getDHuSJar ()
   {
      File current_file =
         new File(ClassLoader.getSystemClassLoader().getResource(
            "fr/gael/dhus/DHuS.class").toString());

      current_file = new File(current_file.getPath());

      for (int i = 0; i < 4; i++)
      {
         current_file = current_file.getParentFile();
      }

      String current_path = current_file.getPath(); 

      // Remove potential protocols since we are now outside the original Jar
      // archive.
      current_path = current_path.replaceAll("jar:file:", "");
      current_path = (new File(current_path)).getPath();

      // Case of windows spaces automatically replaced by %20
      // Appends a trailing separator
      current_path = current_path.replaceAll("%20", " ");
      current_path = current_path.replaceAll("!", "");
      current_path = (new File(current_path)).getPath();
      
      return current_path;
   }
      
   private String getDBDirectory ()
   {      
      String hsqlpath = cfgManager.getDatabaseConfiguration ().getPath ();

      File db =
         new File (hsqlpath.replace ('/', File.separatorChar)).getParentFile ();
      
      return db.getPath ();
   }
   
   
   public void dumpDatabase()
   {
      long ts = new Date().getTime ();
      
      String dir_name = String.format ("dump-%020d", ts);

      File dump_file = new File (cfgManager.getDatabaseConfiguration ().getDumpPath (), dir_name);
      dump_file.mkdirs ();
      final String full_path = dump_file.getPath (); 
      
      logger.info ("Saving database into " + full_path);
      
      String hsqlpath = getDBDirectory ();
      File db = new File (hsqlpath);
      
      File[]dbfiles = db.listFiles (new FilenameFilter()
      {
         @Override
         public boolean accept (File dir, String name)
         {
            if ((name.endsWith (".script")) ||
                (name.endsWith (".properties")) ||
                (name.endsWith (".data")) ||
                (name.endsWith (".backup")) ||
                (name.endsWith (".lobs")) ||
                (name.endsWith (".log")))
               return true;
            return false;
         }
      });
      
      for (File f:dbfiles)
      {
         File output_file = new File (dump_file, f.getName ());
         FileInputStream fis=null;
         InputStream input=null;
         FileOutputStream fos=null;
         OutputStream output=null;
         
         try
         {
            fis = new FileInputStream (f);
            input = new BufferedInputStream (fis);
         
            
            fos = new FileOutputStream (output_file);
            output = new BufferedOutputStream (fos);
         
            IOUtils.copy (input, output);
         }
         catch (FileNotFoundException fnfe)
         {
            throw new DHusDumpException ("Cannot find file", fnfe);
         }
         catch (IOException ioe)
         {
            throw new DHusDumpException ("Cannot copy " + 
               f.getPath () + " into " + output_file.getPath ());
         }
         finally
         {
            try
            {
               input.close ();
               fis.close ();
               output.close ();
               fos.close ();
            }
            catch (Exception e) {}
         }
      }
      
      logger.info ("Database saved.");
   }
   
   public void cleanDumpDatabase(int keepno)
   {
      File[]dumps = new File(cfgManager.getDatabaseConfiguration ().getDumpPath ()).listFiles(new FilenameFilter()
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
               Date date = new Date (Long.parseLong (dir.getName ().replaceAll ("dump-(.*)", "$1")));
               logger.info ("Cleaned dump of " + date); 
               FileUtils.deleteDirectory(dir);
            }
            catch (IOException e)
            {
               logger.warn ("Cannot delete directory " + dir.getPath() + " (" +
                  e.getMessage() + ")");
            }
         }
      }
   }
}
