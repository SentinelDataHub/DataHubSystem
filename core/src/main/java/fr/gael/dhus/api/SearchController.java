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
import org.apache.log4j.Logger;
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
   private static final Logger LOGGER = Logger.getLogger(SearchController.class);

   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;

   @Autowired
   private ConfigurationManager configurationManager;

   @Autowired
   private SearchService searchService;

   @Autowired
   private SolrDao solrDao;

   @PreAuthorize("hasRole('ROLE_SEARCH')")
   @RequestMapping(value = "/search/suggest/{query}")
   public void suggestions(@PathVariable String query, HttpServletResponse res)
         throws IOException
   {
      List<String> suggestions = searchService.getSuggestions(query);
      res.setStatus(HttpServletResponse.SC_OK);
      res.setContentType("text/plain");
      try (ServletOutputStream outputStream = res.getOutputStream())
      {
         if (suggestions != null)
         {
            for (String suggestion: suggestions)
            {
               outputStream.println(suggestion);
            }
         }
      }
   }

   @PreAuthorize("hasRole('ROLE_SEARCH')")
   @RequestMapping(value = "/search")
   public void search(Principal principal,
         @RequestParam(value = "q") String orginal_query,
         @RequestParam(value = "rows", defaultValue = "") String rows_str,
         @RequestParam(value = "start", defaultValue = "") String start_str,
         @RequestParam(value = "format", defaultValue = "") String format,
         HttpServletResponse res) throws IOException, JSONException
   {
      User user = (User) ((UsernamePasswordAuthenticationToken) principal)
            .getPrincipal();
      ServerConfiguration dhusServer = configurationManager
            .getServerConfiguration();

      String query = convertQuery(orginal_query);
      LOGGER.info("Rewritted Query: " + query);

      String urlStr = "&dhusLongName="
            + configurationManager.getNameConfiguration().getLongName()
            + "&dhusServer=" + dhusServer.getExternalUrl();
      urlStr += "&originalQuery=" + orginal_query;
      urlStr = urlStr.replace(" ", "%20");
      int skip = 0;
      int rows = 10;
      if (rows_str != null && !rows_str.isEmpty())
      {
         try
         {
            rows = Integer.parseInt(rows_str);
            urlStr += "&rows=" + rows_str;
         }
         catch (NumberFormatException nfe)
         {
            /* noting to do : keep the default value */
         }
      }
      if (start_str != null && !start_str.isEmpty())
      {
         try
         {
            skip = Integer.parseInt(start_str);
            urlStr += "&start=" + start_str;
         }
         catch (NumberFormatException nfe)
         {
            /* noting to do : keep the default value */
         }
      }

      URLEncoder.encode(urlStr, "UTF-8");

      actionRecordWritterDao.search(orginal_query, skip, rows, user);

      // solr is only accessible from local server
      String local_url = dhusServer.getUrl();
      URL obj = new URL(local_url + "/solr/dhus/select?" + query
            + "&wt=xslt&tr=opensearch_atom.xsl" + urlStr);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();
      con.setRequestMethod("GET");

      try (InputStream is = con.getInputStream())
      {
         try (ServletOutputStream os = res.getOutputStream())
         {
            res.setStatus(HttpServletResponse.SC_OK);
            if ("json".equalsIgnoreCase(format))
            {
               res.setContentType("application/json");
               toJSON(is, os);
            }
            else
            {
               res.setContentType(con.getContentType());
               IOUtils.copy(is, os);
            }
         }
      }
   }

   private String convertQuery(String query)
   {
      String ret = solrDao.updateQuery(query);
      ret = ret.replaceAll(" ", "%20");
      return "q=" + ret;
   }

   /**
    * Converts input xml stream into output json stream.
    * WARNING this implementation stores xml data into memory.
    * <p>
    * @param xml_stream
    * @param output
    * @throws IOException
    * @throws JSONException
    */
   void toJSON(InputStream xml_stream, OutputStream output)
         throws IOException, JSONException
   {
      StringWriter writer = new StringWriter();
      IOUtils.copy(xml_stream, writer, "UTF-8");

      JSONObject xmlJSONObj = XML.toJSONObject(writer.toString().trim());
      String jsonPrettyPrintString = xmlJSONObj.toString(3);

      output.write(jsonPrettyPrintString.getBytes());
   }
}
