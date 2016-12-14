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
package fr.gael.dhus.datastore.scanner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.http.client.utils.URIBuilder;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;

import fr.gael.dhus.olingo.ODataClient;
import static org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind.DateTime;

/**
 * This class scans another DHuS OData interface.
 * It will retrieve every unknown product to this DHuS.
 */
public class ODataScanner extends AbstractScanner
{
   private static final Logger LOGGER = LogManager.getLogger(ODataScanner.class);
   
   private final ODataClient client;
   private final String uri, username, password;
   
   private long lastScanTime = 0L;
   
   public ODataScanner (String uri, boolean store_scan_list, String username,
      String password) throws URISyntaxException, IOException, ODataException
   {
      super (store_scan_list);
      
      this.uri = uri;
      this.username = username;
      this.password = password;
      
      // Workaround: if we are using this scanner to transfer a product.
      // see ProcessProductTransfer.upload()
      if (!uri.endsWith ("$value"))
      {
         // Creates an ODataClient for `uri`.
         this.client = new ODataClient (uri, username, password);
         LOGGER.info (
            "ODataScanner on " + client.getServiceRoot () + " created.");
      }
      else
      {
         this.client = null;
      }
   }
   
   @Override
   public int scan () throws InterruptedException
   {
      // Workaround: transferring 1 product. see l.50
      if (this.client == null)
      {
         try
         {
            URL url = new URL(this.uri);
            HttpURLConnection co = (HttpURLConnection) url.openConnection ();
            
            // HTTP Basic Authentication.
            String userpass = this.username + ":" + this.password;
            String basicAuth = "Basic " + 
               new String(new Base64 ().encode (userpass.getBytes ()));
            co.setRequestProperty ("Authorization", basicAuth);
            
            co.connect ();
            String cd = co.getHeaderField ("Content-Disposition");
            co.disconnect ();
            
            Matcher m = Pattern.compile (".*?filename=\"(.*?)\\.zip\".*?")
               .matcher ((cd));
            if (!m.matches ()) throw new Exception ("filename not in header");
            
            String filename = m.group (1);
            
            //url = new URIBuilder (url.toURI ())
            //   .setUserInfo (this.username, this.password).build ().toURL ();
            
            this.getScanList ().add (new URLExt (url, false, filename));
         }
         catch (Exception e)
         {
            LOGGER.error ("failed to transfer " + this.uri, e);
            return 0;
         }
         return 1;
      }
      
      // The actual scan() code.
      int res = 0;
      
      try
      {
         // Prepare OData request, we want Products.
         String resource_path = new String ("/Products");
         Facets facets = (new Facets ()).setNullable (false);
         Map<String, String> query_params = new HashMap<> ();
         
         long now = System.currentTimeMillis ();
         EdmSimpleType type = RuntimeDelegate.getEdmSimpleType (DateTime);
         
         // Filtering by ingestionDate.
         if (this.lastScanTime != 0L)
         {
            StringBuilder sb = new StringBuilder ("IngestionDate ge ");
            Date l_time = new Date (this.lastScanTime);
            sb.append (type.valueToString (l_time, EdmLiteralKind.URI, facets));
            
            sb.append (" and IngestionDate lt ");
            l_time = new Date (now);
            sb.append (type.valueToString (l_time, EdmLiteralKind.URI, facets));
            query_params.put ("$filter", sb.toString ());
         }
         else
         {
            Date l_time = new Date (now);
            String value = "IngestionDate lt " +
               type.valueToString (l_time, EdmLiteralKind.URI, facets);
            
            query_params.put ("$filter", value);
         }
         
         // Ordering by ingestionDate.
         query_params.put ("$orderby", "IngestionDate");
         
         // Pagination.
         int page_len = 50; // TODO get this value from config.
         query_params.put ("$top", String.valueOf (page_len));
         
         try
         {
            for (long i=0; ; i++)
            {
               if (i!=0)
                  query_params.put ("$skip", String.valueOf (page_len * i));
               
               ODataFeed of =
                  this.client.readFeed (resource_path, query_params);
               
               for (ODataEntry entry: of.getEntries ())
               {
                  String pdt_name = (String) entry.getProperties ().get("Name");
                  if (this.getUserPattern () == null ||
                     this.getUserPattern ().matcher (pdt_name).matches ())
                  {
                     String key = (String) entry.getProperties ().get("Id");
                     URL url = new URIBuilder (
                        this.client.getServiceRoot ()
                        + "/Products" + "('" + key + "')/$value")
                     .setUserInfo (this.username, this.password)
                     .build ().toURL ();
                     
                     this.getScanList ().add (
                        new URLExt (url, false, pdt_name));
                     
                     res += 1;
                  }
               }
               
               if (of.getEntries ().size () != page_len) // End of pagination.
                  break;
            }
         }
         catch (ODataException | IOException | URISyntaxException e)
         {
            LOGGER.error ("Product retrieval failed", e);
         }
         
         this.lastScanTime = now;
      }
      catch (ODataException e)
      {
         LOGGER.error (e);
      }
      
      return res;
   }
}
