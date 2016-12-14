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
package fr.gael.dhus.server.http.webapp.search.controller;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.gael.dhus.database.object.config.server.ServerConfiguration;
import fr.gael.dhus.search.SolrDao;
import fr.gael.dhus.service.SearchService;
import fr.gael.dhus.system.config.ConfigurationManager;

@Controller
public class SearchController
{
   private static final Logger LOGGER = LogManager.getLogger(SearchController.class);

   /** Maximum value for the `rows` parameter. */
   private final static Integer ROW_LIMIT = Integer.getInteger("max.rows.search.value", 100);

   @Autowired
   private ConfigurationManager configurationManager;

   @Autowired
   private SolrDao solrDao;

   @Autowired
   private SearchService searchService;

   @PreAuthorize("hasRole('ROLE_SEARCH')")
   @RequestMapping(value = "/suggest/{query}")
   public void suggestions(@PathVariable String query, HttpServletResponse res)
         throws IOException
   {
      List<String> suggestions = searchService.getSuggestions(query);
      res.setStatus(HttpServletResponse.SC_OK);
      res.setContentType("text/plain");
      try (ServletOutputStream outputStream = res.getOutputStream())
      {
         for (String suggestion: suggestions)
         {
            outputStream.println(suggestion);
         }
      }
   }

   @PreAuthorize("hasRole('ROLE_SEARCH')")
   @RequestMapping(value = "/")
   public void search(Principal principal,
         @RequestParam(value = "q") String orginal_query,
         @RequestParam(value = "rows", defaultValue = "") String rows_str,
         @RequestParam(value = "start", defaultValue = "") String start_str,
         @RequestParam(value = "format", defaultValue = "") String format,
         @RequestParam(value = "orderby", required = false) String orderby,
         HttpServletResponse res) throws IOException, JSONException
   {
      ServerConfiguration dhusServer = configurationManager.getServerConfiguration();

      String query = convertQuery(orginal_query);
      LOGGER.info("Rewritted Query: " + query);

      // solr is only accessible from local server
      String local_url = dhusServer.getLocalUrl();

      StringBuilder sb = new StringBuilder(local_url);
      sb.append("/solr/dhus/select?").append(query)
        .append("&wt=xslt&tr=opensearch_atom.xsl")
        .append("&dhusLongName=")
           .append(urlenc(configurationManager.getNameConfiguration().getLongName()))
        .append("&dhusServer=").append(urlenc(dhusServer.getExternalUrl()))
        .append("&originalQuery=").append(urlenc(orginal_query));

      if (rows_str != null && !rows_str.isEmpty())
      {
         try
         {
            int rows = Integer.parseInt(rows_str);
            if (rows > ROW_LIMIT)
            {
               String errorMessage = String.format(
                     "Parameter `rows` exceeds the maximum value (%d) in search request", ROW_LIMIT);
               LOGGER.warn(errorMessage);
               res.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
               return;
            }
            sb.append("&rows=").append(rows_str);
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
            Integer.parseInt(start_str);
            sb.append("&start=").append(start_str);
         }
         catch (NumberFormatException nfe)
         {
            /* noting to do : keep the default value */
         }
      }
      // If `orderby` param is not defined, default to ordering by ingestiondate desc.
      // Define `orderby` to `""` (empty string) to irder by the full text seach score.
      if (orderby == null)
      {
         sb.append("&sort=ingestiondate+desc");
      }
      else
      {
         sb.append("&orderby=").append(urlenc(orderby));
         if (!orderby.isEmpty())
         {
            sb.append("&sort=").append(urlenc(orderby));
         }
      }

      URL obj = new URL(sb.toString());

      HttpURLConnection con = (HttpURLConnection) obj.openConnection();

      con.setConnectTimeout(SolrDao.INNER_TIMEOUT);
      con.setReadTimeout(SolrDao.INNER_TIMEOUT);

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
      catch (Exception e)
      {
         res.addHeader("cause-message",
               String.format("%s : %s", e.getClass().getSimpleName(), e.getMessage()));
         throw e;
      }
      finally
      {
         con.disconnect();
      }
   }

   private static String urlenc(String str) throws IOException
   {
      return URLEncoder.encode(str, "UTF-8");
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
