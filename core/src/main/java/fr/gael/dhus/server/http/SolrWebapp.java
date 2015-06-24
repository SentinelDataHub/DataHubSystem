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
package fr.gael.dhus.server.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.object.config.search.SolrConfiguration;
import fr.gael.dhus.system.config.ConfigurationManager;

@Component
@WebApp (
   name = "solr",
   allowIps = "127.\\d+.\\d+.\\d+|::1|0:0:0:0:0:0:0:1")
public class SolrWebapp extends WebApplication implements InitializingBean
{
   private static Logger logger = LogManager.getLogger ();

   @Autowired
   private ConfigurationManager configurationManager;
   
   @Override
   public void configure(String destFolder) throws IOException
   {
      String configurationFolder = "fr/gael/dhus/server/http/solr/webapp";
      URL u = Thread.currentThread ().getContextClassLoader ().getResource (configurationFolder);
      if (u != null && "jar".equals (u.getProtocol ()))
      {
         extractJarFolder(u, configurationFolder, destFolder);
      }
      else if (u != null)
      {
         File webAppFolder = new File(destFolder);
         copyFolder(new File(u.getFile ()), webAppFolder);
      }
   }
   
   @Override
   public InputStream getWarStream ()
   {
      return SolrWebapp.class.getClassLoader ().getResourceAsStream ("solr.war");
   }

   @Override
   public boolean hasWarStream ()
   {
      return true;
   }

   @Override
   public void afterPropertiesSet () throws Exception
   {
      try
      {
         SolrConfiguration solr = configurationManager.getSolrConfiguration ();
         
         File solrroot = new File (solr.getPath ());
         System.setProperty ("solr.solr.home", solrroot.getAbsolutePath ());

         File confdir = new File (solrroot, "conf");
         confdir.mkdirs ();
         File libdir = new File (solrroot, "lib");
         libdir.mkdirs ();
      
         InputStream input = ClassLoader.getSystemResourceAsStream ("fr/gael/dhus/server/http/solr/solr.xml");
         OutputStream output = new FileOutputStream (new File (solrroot, "solr.xml"));
         IOUtils.copy (input, output);
         output.close ();
         input.close ();
         
         String schemapath = solr.getSchemaPath ();
         if ((schemapath == null)     ||
             ("".equals (schemapath)) ||
             (!(new File (schemapath)).exists ()))
            input = ClassLoader.getSystemResourceAsStream ("fr/gael/dhus/server/http/solr/schema.xml");
         else
            input = new FileInputStream(new File (schemapath));
         
         output = new FileOutputStream (new File (confdir, "schema.xml"));
         IOUtils.copy (input, output);
         output.close ();
         input.close ();
         
         if (Boolean.parseBoolean (System.getProperty ("solr.filter.user", "false")))
         {
            input = ClassLoader.getSystemResourceAsStream ("fr/gael/dhus/server/http/solr/solrconfigAccessFilter.xml");
         }
         else
         {
            input = ClassLoader.getSystemResourceAsStream ("fr/gael/dhus/server/http/solr/solrconfig.xml");
         }
         output = new FileOutputStream (new File (confdir, "solrconfig.xml"));
         IOUtils.copy (input, output);
         output.close ();
         input.close ();
         
         input = ClassLoader.getSystemResourceAsStream ("fr/gael/dhus/server/http/solr/stopwords.txt");
         output = new FileOutputStream (new File (confdir, "stopwords.txt"));
         IOUtils.copy (input, output);
         output.close ();
         input.close ();
         
         String synonympath = solr.getSynonymPath ();
         if ((synonympath == null)     ||
             ("".equals (synonympath)) ||
             (!(new File (synonympath)).exists ()))
            input = ClassLoader.getSystemResourceAsStream ("fr/gael/dhus/server/http/solr/synonyms.txt");
         else
            input = new FileInputStream(new File (synonympath));
         output = new FileOutputStream (new File (confdir, "synonyms.txt"));
         IOUtils.copy (input, output);
         output.close ();
         input.close ();
         
         input = ClassLoader.getSystemResourceAsStream ("fr/gael/dhus/server/http/solr/xslt/opensearch_atom.xsl");
         if (input != null)
         {
            File xslt_dir = new File(confdir, "xslt");
            if (!xslt_dir.exists ()) xslt_dir.mkdirs ();
            output = new FileOutputStream (
               new File (xslt_dir, "opensearch_atom.xsl"));
            IOUtils.copy (input, output);
            output.close ();
            input.close ();
         }
         else
         {
            logger.warn ("Cannot file opensearch xslt file. " +
               "Opensearch interface is not available.");
         }
         
      }
      catch (IOException e)
      {
         throw new UnsupportedOperationException (
            "Cannot initialize Solr service.", e);
      }
   }

   @Override
   public void checkInstallation () throws Exception
   {
//      SolrManager solrManager = ApplicationContextProvider.getBean (SolrManager.class);
//      solrManager.ping ();
   }
}
