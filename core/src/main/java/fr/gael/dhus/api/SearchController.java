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
package fr.gael.dhus.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.config.server.ServerConfiguration;
import fr.gael.dhus.search.SolrDao;
import fr.gael.dhus.service.SearchService;
import fr.gael.dhus.system.config.ConfigurationManager;

@Controller
public class SearchController
{   
   private static Log logger = LogFactory.getLog (SearchController.class);

   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;   
   
   @Autowired
   private ConfigurationManager configurationManager;
   
   @Autowired
   private SearchService searchService;

   @Autowired
   private SolrDao solrDao;

   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @RequestMapping (value = "/search/suggest/{query}")
   public void suggestions (@PathVariable String query, HttpServletResponse res) throws IOException
   {
      List<String> suggestions = searchService.getSuggestions (query);
      res.setStatus (HttpServletResponse.SC_OK);
      res.setContentType ("text/plain");
      ServletOutputStream outputStream = res.getOutputStream ();
      if (suggestions != null)
      {
          for (String suggestion : suggestions)
          {
             outputStream.println (suggestion);
          }
      }
      outputStream.close ();
   }

   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   @RequestMapping (value = "/search")
   public void search(Principal principal, @RequestParam(value="q") String orginalQuery,
      @RequestParam(value="rows", defaultValue="") String rowsStr,
      @RequestParam(value="start", defaultValue="") String startStr,
      @RequestParam(value="format", defaultValue="") String format,
      HttpServletResponse res) throws IOException, JSONException
   {
      User user = (User)((UsernamePasswordAuthenticationToken)principal).getPrincipal ();
      ServerConfiguration dhusServer = configurationManager.getServerConfiguration ();
      
      String query = convertQuery(orginalQuery, user);
      logger.info ("Rewritted Query: " + query);
      
      String urlStr = "&dhusLongName=" + configurationManager.getNameConfiguration ().getLongName ()
         + "&dhusServer=" + dhusServer.getExternalUrl ();
      urlStr += "&originalQuery=" + orginalQuery;
      urlStr = urlStr.replace (" ", "%20");
      int skip = 0;
      int rows = 10;
      if (rowsStr != null && !rowsStr.isEmpty ())
      {
         try
         {
            rows=Integer.parseInt (rowsStr);
            urlStr += "&rows=" + rowsStr;
         }
         catch (NumberFormatException nfe)
         {
            /* noting to do : keep the default value */
         }
      }
      if (startStr != null && !startStr.isEmpty ())
      {
         try
         {
            skip=Integer.parseInt(startStr);
            urlStr += "&start=" + startStr;
         }
         catch (NumberFormatException nfe)
         {
            /* noting to do : keep the default value */
         }
      }
      
      URLEncoder.encode (urlStr, "UTF-8");

      actionRecordWritterDao.search(orginalQuery, skip, rows, user);
      
      // solr is only accessible from localhost
      String local_url = dhusServer.getProtocol () + "://localhost:" + dhusServer.getPort (); 
      URL obj = new URL(local_url + "/solr/dhus/select?"+query+"&wt=xslt&tr=opensearch_atom.xsl"+urlStr);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection ();
      con.setRequestMethod ("GET");
      
      InputStream is = con.getInputStream();
      ServletOutputStream os = res.getOutputStream ();
            
      res.setStatus (HttpServletResponse.SC_OK);
      if ("json".equalsIgnoreCase (format))
      {
         res.setContentType ("application/json");
         toJSON (is, os);
      }
      else
      {
         res.setContentType (con.getContentType ());
         IOUtils.copy (is, os);
      }
      
      is.close ();
      os.close ();
   }
   
   private String convertQuery (String query, User user)
   {
      String ret=solrDao.updateQuery (query);
      if (user != null)
      {
         ret = solrDao.getRestrictedQuery (ret, user);
      }
      ret = ret.replaceAll (" ", "%20");
      return "q="+ret;
   }
   
   /**
    * Converts input xml stream into output json stream.
    * WARNING this implementation stores xml data into memory. 
    * @param xmlStream
    * @param output
    * @throws IOException 
    * @throws JSONException 
    */
   void toJSON (InputStream xmlStream, OutputStream output) 
            throws IOException, JSONException
   {
      StringWriter writer = new StringWriter();
      IOUtils.copy(xmlStream, writer, "UTF-8");
      
      JSONObject xmlJSONObj = XML.toJSONObject (writer.toString ().trim ());
      String jsonPrettyPrintString = xmlJSONObj.toString(3);
      
      output.write (jsonPrettyPrintString.getBytes ());      
   }   
}
