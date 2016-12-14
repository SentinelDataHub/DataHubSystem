/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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
package fr.gael.dhus.server.http.webapp.symmetricDS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;
import com.jolbox.bonecp.BoneCPDataSource;

import fr.gael.dhus.server.ScalabilityManager;
import fr.gael.dhus.server.http.webapp.WebApp;
import fr.gael.dhus.server.http.webapp.WebApplication;
import fr.gael.dhus.system.config.ConfigurationManager;

@Component
@WebApp(name = "sync", scalability="true")
public class SymmetricDSWebapp extends WebApplication implements InitializingBean
{
   private static final Logger LOGGER = LogManager.getLogger(SymmetricDSWebapp.class);

   @Autowired
   BoneCPDataSource dataSource;
   
   @Autowired
   ScalabilityManager scalabilityManager;
   
   @Autowired
   ConfigurationManager cfgManager;
   
   @Qualifier ("boneCPDataSource")
   @Autowired
   DataSource datasource;
   
   @Override
   public void configure(String dest_folder) throws IOException
   {
      String configurationFolder = "fr/gael/dhus/server/http/webapp/symmetricDS/web";
      URL u = Thread.currentThread().getContextClassLoader()
            .getResource(configurationFolder);
      if (u != null && "jar".equals(u.getProtocol()))
      {
         extractJarFolder(u, configurationFolder, dest_folder);
      }
      else if (u != null)
      {
         File webAppFolder = new File(dest_folder);
         copyFolder(new File(u.getFile()), webAppFolder);
      }  
      
      String properties = "fr/gael/dhus/server/http/webapp/symmetricDS/"+
               (scalabilityManager.isMaster () ? "master" : "replica")
               +".properties";
      u = Thread.currentThread().getContextClassLoader()
            .getResource(properties);
      
      String propFile = dest_folder+"/WEB-INF/classes/symmetric.properties";
      if (u != null && "jar".equals(u.getProtocol()))
      {
         extractJarFile(u, properties, propFile);
      }
      else if (u != null)
      {
         File webAppFolder = new File(propFile);
         Files.copy (new File(u.getFile()), webAppFolder);
      }
               
      Path path = Paths.get(propFile);
      Charset charset = StandardCharsets.UTF_8;

      String content = new String(java.nio.file.Files.readAllBytes(path), charset);
      content = content.replaceAll("%id%", String.format("%03d", scalabilityManager.getReplicaId ()));
      content = content.replaceAll("%masterUrl%", scalabilityManager.getMasterUrl ());
      content = content.replaceAll("%localUrl%", scalabilityManager.getLocalUrl ());

      content = content.replaceAll("%dbDriver%", dataSource.getDriverClass ());
      content = content.replaceAll("%dbUrl%", dataSource.getJdbcUrl ());
      content = content.replaceAll("%dbUser%", dataSource.getUsername ());
      content = content.replaceAll("%dbPassword%", dataSource.getPassword ());      
      java.nio.file.Files.write(path, content.getBytes(charset));
   }

   @Override
   public InputStream getWarStream()
   {
      return SymmetricDSWebapp.class.getClassLoader().getResourceAsStream(
            "fr/gael/dhus/server/http/webapp/symmetricDS/symmetric-ds.war");
   }

   @Override
   public boolean hasWarStream()
   {
      return true;
   }

   @Override
   public void afterPropertiesSet() throws Exception
   {
      if (!scalabilityManager.getClearDB ())
      {
         return;
      }
      PreparedStatement ps = datasource.getConnection ().prepareStatement (
         "SELECT TRIGGER_NAME FROM INFORMATION_SCHEMA.TRIGGERS WHERE TRIGGER_NAME LIKE 'SYM_%';",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      
      ResultSet rs = ps.executeQuery ();
      while (rs.next ())
      {   
         PreparedStatement ps2 = datasource.getConnection ().prepareStatement (
            "DROP TRIGGER " +
            rs.getString ("TRIGGER_NAME"));
         ps2.execute ();
         ps2.close ();
      } 
      ps.close ();
      
      ps = datasource.getConnection ().prepareStatement (
         "SELECT CONSTRAINT_NAME, TABLE_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_NAME LIKE 'SYM_%';",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      rs = ps.executeQuery ();
      while (rs.next ())
      {   
         PreparedStatement ps2 = datasource.getConnection ().prepareStatement (
            "ALTER TABLE "+rs.getString ("TABLE_NAME")+" DROP CONSTRAINT " +
            rs.getString ("CONSTRAINT_NAME"));
         ps2.execute ();
         ps2.close ();
      }
      ps.close ();
      
      ps = datasource.getConnection ().prepareStatement (
         "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'SYM_%';",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      
      rs = ps.executeQuery ();
      while (rs.next ())
      {   
         PreparedStatement ps2 = datasource.getConnection ().prepareStatement (
            "DROP TABLE " +
            rs.getString ("TABLE_NAME"));
         ps2.execute ();
         ps2.close ();
      }
      ps.close ();
   }

   @Override
   public void checkInstallation() throws Exception 
   {
      if (!scalabilityManager.isMaster ()) return;
      
      // Check database is ready for SymmetricDS
      PreparedStatement ps = datasource.getConnection ().prepareStatement (
            "SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_NAME='SYM_FK_TRGPLT_2_TR';",
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
         
      while (!ps.executeQuery ().first ())
      {
         try
         {
            Thread.sleep (1000);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }         
      }
      ps.close ();
      // Check if init.sql has already been executed
      ps = datasource.getConnection ().prepareStatement (
         "SELECT node_group_id FROM SYM_NODE_GROUP WHERE node_group_id = 'dhus-replica-group';",
         ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      
      if (!ps.executeQuery ().first ())
      {   
         // Wait for master group to be inserted         
         PreparedStatement ps2 = datasource.getConnection ().prepareStatement (
            "SELECT * FROM SYM_NODE_GROUP WHERE node_group_id='dhus-master-group';",
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
         
         while (!ps2.executeQuery ().first ())
         {
            try
            {
               Thread.sleep (1000);
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }         
         }         
         ps2.close ();

         ScriptUtils.executeSqlScript (datasource.getConnection (), 
            new ClassPathResource("fr/gael/dhus/server/http/webapp/symmetricDS/init.sql"));
         LOGGER.info("SymmetricDS initialization script loaded");

         // Force the synchronizers to be reloaded
         HttpClient httpclient = HttpClients.createDefault();
         HttpPost httppost = new HttpPost(cfgManager.getServerConfiguration().getLocalUrl() + "/sync/api/engine/synctriggers");

         httpclient.execute(httppost);
      }    
      ps.close ();  
   }
}
