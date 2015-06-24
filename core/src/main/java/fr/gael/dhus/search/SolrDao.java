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
package fr.gael.dhus.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import fr.gael.dhus.database.dao.UserDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer.RemoteSolrException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.search.geocoder.AbstractGeocoder;
import fr.gael.dhus.search.geocoder.Geocoder;
import fr.gael.dhus.search.geocoder.GeocoderFactory;
import fr.gael.dhus.service.SecurityService;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * DAO other Solr Interface.
 *
 */
@Component
public class SolrDao
{
   private static Log logger = LogFactory.getLog (SolrDao.class);

   @Autowired
   private ProductDao productDao;
   
   @Autowired
   private SecurityService securityService;
   
   @Autowired
   private ConfigurationManager cfgManager;

   @Autowired
   private UserDao userDao;

   /**
    * Default Geocoder
    */
   private Geocoder geocoder;

   private static SolrServer solrServer=null;
   
   private static LoadingCache<String, SearchResult> cache=null;
   
   public Geocoder getGeocoder ()
   {
      if (this.geocoder == null)
         geocoder = GeocoderFactory.getDefault(cfgManager.getGeocoderConfiguration ().getUrl ());
      return geocoder;
   }

   private LoadingCache<String, SearchResult> getResultsCache ()
   {
      if (SolrDao.cache == null)
      {
         SolrDao.cache = CacheBuilder.newBuilder()
           .concurrencyLevel(4)
           .maximumSize(1000)
           .expireAfterWrite(10, TimeUnit.MINUTES)
           .expireAfterAccess(10, TimeUnit.MINUTES)
           .build
           (
               new CacheLoader<String, SearchResult>() 
               {
                  public SearchResult load(String key) 
                  {
                     String converted = key;
                     logger.info("Executing Query \"" +
                        ("" + converted).substring(0,
                           Math.min(("" + converted).length(), 512)) +
                           " (...)\"");
                     return new SearchResult (getSolrServer (), converted);
                  }
               }
            );
         }
      return SolrDao.cache;
   }
   
   public static void resetQueryCache()
   {
      if (SolrDao.cache != null)
      {
         SolrDao.cache.cleanUp ();
         SolrDao.cache.invalidateAll ();
      }
   }

   /**
    * Retrive the online solr Server. The dhus solr server is expected at
    *    http://localhost:port/solr/dhus
    * @return the solr server instance.
    */
   private SolrServer getSolrServer ()
   {
      if (solrServer == null)
      {
         int port = cfgManager.getServerConfiguration ().getPort ();
      
         String url = new String ("http://localhost:" + port + "/solr/dhus");
         SolrServer s = new HttpSolrServer (url);
         solrServer = s;
      }
      return solrServer;
   }
   
   /**
    * Saves index in solr service
    * @param productPath
    * @param indexes
    */
   public void saveIndex(Product product, List<MetadataIndex>indexes) 
   {      
      String productPath = ProductDao.getPathFromProduct (product);
      SolrServer server = getSolrServer ();
      // Prepare the document
      SolrDocument ro_doc=null;
      SolrInputDocument doc = null;
      
      if ((ro_doc=getDocumentByPath (productPath)) != null)
      {
         logger.info ("Adding or updating fields in solr path '" + productPath  +
            "'");
         doc = ClientUtils.toSolrInputDocument (ro_doc);
      }
      else
         doc = getInputDocByPath (productPath, product.getId ());
      
      // ingest indexes
      for (MetadataIndex index:indexes)
      {
         String type = index.getType ();
         // Only textual information stored in index
         if ((type == null)  ||
             type.isEmpty () ||
             "text/plain".equals (type))
         {
            //doc.addField ("contents", index.getName ());
            updateField (doc, "contents", index.getValue (), true);
         }
         
         if (index.getQueryable () != null)
         {
            updateField (doc, "contents", index.getQueryable (), true);
            
            updateField (doc, index.getQueryable ().toLowerCase(), 
               index.getValue (), false);
            
            if (logger.isDebugEnabled())
            {
               logger.debug ("Added " + index.getQueryable () + ":" +
                  index.getValue ());
            }
         }
      }
      
      try
      {
         server.add (doc);
         server.commit ();
      }
      catch (SolrServerException e)
      {
         logger.error ("Cannot save index changes in solr.", e);
         return;
      }
      catch (IOException e)
      {
         // should never happend
         e.printStackTrace();
      }
      resetQueryCache ();
   }
   
   public void removeIndexes (Product product)
   {
      SolrServer server = getSolrServer ();
      try
      {
         server.deleteById (product.getId ().toString ());
         server.commit ();
      }
      catch (SolrServerException e)
      {
         logger.error ("Problem accessing the solr server.", e);
      }
      catch (IOException e)
      {
         logger.error ("IO error.", e);
      }
      resetQueryCache ();
   }
   
   /**
    * Processed Solr index optimization.
    * Shall be called asynchronously to avoid latencies.
    */
   public void optimize ()
   {
      SolrServer server = getSolrServer ();
      try
      {
         server.optimize();
      }
      catch (Exception e){}
   }
   
   public void relocate (Long id, String new_path)
   {
      SolrDocument doc = getDocumentById (id);
      
      if (doc == null)
      {
         throw new DHusSearchException ("Cannot retrieve document id(" + 
               id + ") to be relocated.");
      }
      
      SolrInputDocument input = ClientUtils.toSolrInputDocument (doc);
      Map<String, Object> partialUpdate = new HashMap<String, Object>();
      partialUpdate.put("set", toSolrPath (new_path));
      updateField (input, "path", partialUpdate, false);
      try
      {
         getSolrServer ().add (input);
         getSolrServer ().commit ();
      }
      catch (SolrServerException e)
      {
         throw new DHusSearchException ("Cannot product path changes in solr.", e);
      }
      catch (IOException e)
      {
         // should never happend
         e.printStackTrace();
      }
      resetQueryCache ();
   }
   
   
   /**
    * Retrieve the number of document stored into this solr server
    * @return number of documents.
    */
   public long getDocumentsNumber ()
   {
      return search ("*:*", false, null).size();
   }
   
   public long count (String query, User u)
   {
      return search (query, true, u).size ();
   }

   private SolrDocument getDocumentById (Long id)
   {
      SearchResult sr = search ("id:" + id, false, null);
      if (sr.hasNext ()) return sr.next ();
      return null;
   }

   private SolrDocument getDocumentByPath (String productPath)
   {
      SearchResult sr = searchNoFilterQuery ("path:" + toSolrPath(productPath));
      if (sr.hasNext ()) return sr.next ();
      return null;
   }

   public SearchResult search (String squery)
   {
      return search(squery, true, null);
   }
   
   public SearchResult search (String squery, boolean restricted, User user)
   {
      try
      {
         if (getSolrServer ().ping () == null)
         {
            throw new DHusSearchException ("Solr Server not ready.");
         }
      }
      catch (Exception e)
      {
         throw new DHusSearchException (e.getMessage ());
      }
      SearchResult docs;
      try
      {
         logger.info ("Updating query \"" +
            ("" + squery).substring (0,
               Math.min ( ("" + squery).length (), 512)) + " (...)\"");
         String query = updateQuery (squery);

         logger.debug ("Looking for docs...");
         logger
            .info ("Searching for \"" +
               ("" + query).substring (0,
                  Math.min ( ("" + query).length (), 512)) + " (...)\"");

         boolean accessFilterActif =
            Boolean.parseBoolean (System.getProperty ("solr.filter.user",
               "false"));
         if (accessFilterActif)
         {
            docs = getResultsCache ().get (query);
         }
         else
         {
            docs =
               getResultsCache ().get (
                  restricted ? getRestrictedQuery (query, user) : query);
         }

         // Rewind the iterator to the beginning
         docs.setOffset (0);
      }
      catch (ExecutionException e)
      {
         logger.error ("Cannot retrieve results for query \"" + squery + "\".",
            e);
         return null;
      }
      return docs;
   }
   
   /**
    * Retrieves suggested values from solr.
    * @param prefix the string parsed by solr to retrieve suggestions.
    * @return a list of suggestions.
    */
   public List<String>getSuggestions (String prefix)
   {
      ModifiableSolrParams params = new ModifiableSolrParams();
      params.set("qt", "/suggest");
      params.set("q", prefix);
      params.set("spellcheck", "on");

      QueryResponse response;
      try
      {
         response = getSolrServer ().query(params);
      }
      catch (SolrServerException e)
      {
         logger.warn ("Cannot get suggestion for prefix \"" + prefix + "\".");
         return ImmutableList.of();
      }
      
      SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse();      
      if (spellCheckResponse != null && !spellCheckResponse.isCorrectlySpelled())
      {
         List<Suggestion>lst = response.getSpellCheckResponse().getSuggestions();
         // Returns only the last suggestion...
         if (!lst.isEmpty ())
            return lst.get (lst.size ()-1).getAlternatives ();
      }
      return ImmutableList.of();
   }
   
   
   /**
    * Restricts the query with the allowed username field
    * @param query
    * @return
    */
   public String getRestrictedQuery (String query, User user)
   {
      if (user == null)
         user = securityService.getCurrentUser ();
      String userString = "";
      // Bypass for Data Right Managers. They can see all products and collections.
      if (!cfgManager.isDataPublic () &&
          user != null && !user.getRoles ().contains (Role.DATA_MANAGER))
      {
         userString = "AND (user:(\""+user.getUsername ()+"\" OR \""+userDao.getPublicDataName ()+"\"))";
      }
      return "(" + query +") "+userString;
   }
   
   public List<Product> getProductListByDocList (List<SolrDocument> docs)
   {
      List<Long> ids = new ArrayList<Long> ();
      for (SolrDocument doc: docs)
         ids.add ((Long)doc.get ("id"));

      return productDao.read (ids);
   }
   
   public Product getProductByDoc (SolrDocument doc)
   {
      Product p=null;
      
      try
      {
         long id = (Long)doc.get ("id");
         p = productDao.read (id);
      }
      catch (Exception e)
      {
         logger.error (
            "Cannot retrieve product by its solr Id (trying by path).", e);
         String path = (String)doc.get ("path");
         try
         {
            p=productDao.getProductByPath (new URL (toExternalPath (path)));
         }
         catch (MalformedURLException mfu)
         {
            logger.error ("Bad path \"" + toExternalPath (path) + "\".", mfu);
         }
      }
      return p;
   }
   
   private static final HashMap<String, String>special_keys = 
      new HashMap<String, String> () 
      {
         private static final long serialVersionUID = 6935030352074844317L;

         {
            put (":", "<colon>");
            /*
            put (";", "<semicolon>");
            put ("-", "<minus>");
            put ("+", "<plus>");
            put ("_", "<underscore>");
            put ("~", "<tilde>");
            put ("^", "<circ>");
            put (" ", "<space>");
            put ("/", "<slash>");
            put ("\\", "<backslash>");
            put ("]", "<closebracket>");
            put ("[", "<openbracket>");
            put ("&", "<amp>");
            put ("*", "<asterisk>");
            */
            
         }
      };
      
   private String toSolrPath (String path)
   {
      logger.debug ("Converting " + path);
      if (path.startsWith ("/")) path="file:/" + path;
      for (String spec: special_keys.keySet ())
      {
         path = path.replace (spec, special_keys.get (spec));
      }
      logger.debug ("   to " + path);
      return path;
   }
   private String toExternalPath (String path)
   {
      logger.debug ("Converting " + path);
      for (String spec: special_keys.keySet ())
      {
         path = path.replace (special_keys.get (spec), spec);
      }
      logger.debug ("   to " + path);
      return path;
   }

   public void addUserRight (Product p, String username)
   {
      int tries = 5;
      do
      {
         try
         {
            _addUserRight (p, username);
            tries = 0;
         }
         catch (RemoteSolrException e)
         {
            int error = e.code ();
            // Case of conflict access
            if (ErrorCode.getErrorCode (error) == ErrorCode.CONFLICT)
            {
               tries --;
               String message = "Solr concurrency access conflict detected " +
                        "(product id#" + p.getId () + ")";
               if (tries>0)
                  logger.warn (message + ", retring ...");
               else
               {
                  throw new DHusSearchException (message, e);
               }
            }
         }
      } while (tries>0);
   }
   public void _addUserRight (Product p, String username)
   {
      SolrServer server = getSolrServer ();
      String path = ProductDao.getPathFromProduct (p);
      SolrDocument doc = getDocumentByPath (path);
      
      if (doc == null)
      {
         logger.warn (
            "Cannot retrieve Product in solr to set user rights with path \"" + 
            path +"\"");
         return;
      }

      boolean user_already_known = false;
      if (doc.containsKey ("user"))
      {
         Collection<Object>users = doc.getFieldValues ("user");
         for (Object user:users)
         {
            // Case of user already agreed
            if (username.equals (user))
            {
               user_already_known = true;
               break;
            }
         }
      }
      if (!user_already_known)
      {
         SolrInputDocument new_doc = ClientUtils.toSolrInputDocument (doc);
         Collection<Object>users = doc.getFieldValues ("user");
         List<Object>list = users == null ? new ArrayList<Object> () : new ArrayList<Object> (users);
         list.add (username);
         new_doc.setField ("user", ImmutableMap.of ("set", list));     
         try
         {
            server.add (new_doc);
            server.commit ();
         }
         catch (SolrServerException e)
         {
            logger.error ("Cannot add user product rights for user \"" + 
               username + "\"", e);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }
   
   public void removeUserRight (Product p, String username)
   {
      SolrServer server = getSolrServer ();
      String path = ProductDao.getPathFromProduct (p);
      SolrDocument doc = getDocumentByPath (path);
      if (doc == null) return;
      
      if (doc.containsKey ("user"))
      {
         SolrInputDocument new_doc = null;
         Collection<Object>users = doc.getFieldValues ("user");
         for (Object user:users)
         {
            // Case of user already agreed
            if (username.equals (user))
            {
               List<Object>list = new ArrayList<Object> (users);
               list.remove (user);
               new_doc =  ClientUtils.toSolrInputDocument (doc);
               new_doc.setField ("user", ImmutableMap.of ("set", list));               
            }
         }

         if (new_doc != null)
         {
            try
            {
               server.add (new_doc);
               server.commit ();
            }
            catch (SolrServerException e)
            {
               logger.error ("Cannot remove user rights for \"" + 
                  username + "\"", e);
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
   
   public List<String> getAuthorizedUsers (Product p)
   {
      String path = ProductDao.getPathFromProduct (p);
      SolrDocument doc = getDocumentByPath (path);
      
      HashSet<String> list = new HashSet<String> (); 
      if (doc.containsKey ("user"))
      {
         for (Object o:doc.getFieldValues ("user"))
            list.add (o.toString ());
      }
      return new ArrayList<String> (list);
   }
   
   private SolrInputDocument getInputDocByPath (String path, Long id)
   {
      SolrInputDocument doc = new SolrInputDocument();
      doc.setField ("path", toSolrPath(path));
      try
      {
         if (id==null)
         {
            Product p = productDao.getProductByPath (new URL(path));
            if (p==null) 
               logger.error ("Path \"" + path + "\" not found in database.");
            doc.setField ("id", p.getId ());
         }
         else
            doc.setField ("id", id);
      }
      catch (MalformedURLException e)
      {
         logger.error ("Unknown product path " + path);
         return null;
      }
      return doc;
   }
   
   /**
    * Manages field add or replacement. document provided in intput shall be 
    * committed by the caller.
    * @param doc
    * @param field
    * @param value
    */
   private void updateField (SolrInputDocument doc, String field, Object value, 
      boolean update)
   {
      if (!update && doc.containsKey (field))
      {
         doc.remove (field);
      }
      doc.addField(field, value);
   }
   
   public String updateQuery (String query)
   {
      for (String[]strs: SolrQueryParser.parse (query))
      {
         String key = strs[SolrQueryParser.INDEX_FIELD];
         String token = strs[SolrQueryParser.INDEX_VALUE];
         
         // If key defined, replace it by its lower case version.
         if (!"".equals (key))
         {
            query = query.replace (key, key.toLowerCase());
         }
         
         if (!(!"".equals (key) ||
               token.startsWith ("{") ||
               token.startsWith ("[") ||    
               token.startsWith ("(") ||
               token.contains ("*") ||
               token.contains ("?") ||
               token.contains ("TO") ||
               token.contains ("OR") ||
               token.contains ("AND") ||
               token.matches(".*\\d.*") ||
               !getSuggestions(token).isEmpty()))
         {
            String wtk_boundaries=null;
            try
            {
               wtk_boundaries = ((AbstractGeocoder)getGeocoder ()).
                  getCachedBoundariesWKT (token);
            }
            catch (ExecutionException e)
            {
               logger.error ("Cannot get boundaries of \"" + token +"\"");
            }

            if (wtk_boundaries != null)
            {
               String locate = "(" + token +" OR footprint:\"Intersects(" +
                  wtk_boundaries + ") distErrPct=0\")";
               query = query.replace(token, locate).trim();
            }
         }
      }
      return query;
   }

   public void addProductInCollection (Product p, String cname)
   {
      SolrServer server = getSolrServer ();
      String path = ProductDao.getPathFromProduct (p);
      SolrDocument doc = getDocumentByPath (path);
      if (doc == null)
      {
         logger.warn (
            "Cannot retrieve Product in solr to set collection with path \"" + 
            ProductDao.getPathFromProduct (p));
         return;
      }
      boolean collection_already_known = false;
      if (doc.containsKey ("collection"))
      {
         Collection<Object>collections = doc.getFieldValues ("collection");
         for (Object collection:collections)
         {
            // Case of user already agreed
            if (cname.equals (collection))
            {
               collection_already_known = true;
               break;
            }
         }
      }
      if (!collection_already_known)
      {
         SolrInputDocument new_doc = ClientUtils.toSolrInputDocument (doc);
         new_doc.setField("collection", ImmutableMap.of ("add", cname));

         try
         {
            server.add (new_doc);
            server.commit ();
         }
         catch (SolrServerException e)
         {
            logger.error ("Cannot add product in collection \"" + 
               cname + "\"", e);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }
   
   public void removeProductFromCollection (Product p, String cname)
   {
      SolrServer server = getSolrServer ();
      String path = ProductDao.getPathFromProduct (p);
      SolrDocument doc = getDocumentByPath (path);
      if (doc == null)
      {
         logger.error (
            "Cannot retrieve Product to remove collection with path \"" + 
            ProductDao.getPathFromProduct (p));
         return;
      }
      if (doc.containsKey ("collection"))
      {
         Collection<Object>collections = doc.getFieldValues ("collection");
         for (Object collection:collections)
         {
            // Case of user already agreed
            if (cname.equals (collection))
            {
               List<Object>list = new ArrayList<Object> (collections);
               list.remove (collection);
               SolrInputDocument new_doc = ClientUtils.toSolrInputDocument (doc);
               // Seems not to work
               // new_doc.setField ("collection", ImmutableMap.of ("remove", user));
               new_doc.setField ("collection", ImmutableMap.of ("set", list));
               try
               {
                  server.add (new_doc);
                  server.commit ();
               }
               catch (SolrServerException e)
               {
                  logger.error ("Cannot remove product from collection \"" + 
                     cname + "\"", e);
               }
               catch (IOException e)
               {
                  e.printStackTrace();
               }
               break;
            }
         }
      }
   }
   
   public void checkIndexes ()
   {
      SolrServer server = getSolrServer ();
      SearchResult sr = search ("*:*", false, null);
      boolean changes = false;
      while (sr.hasNext ())
      {
         SolrDocument doc = sr.next ();
         Product product = getProductByDoc (doc);
         if (product == null)
         {
            Object o = doc.get ("path");
            String path  = "unknown";
            if (o!=null) path = toExternalPath ((String)o);
            try
            {
               sr.remove ();
               logger.warn ("Product \"" + path + 
                  "\" present in Solr Index but not in database: removed.");
               changes = true;
            }
            catch (Exception e)
            {
               logger.error ("Cannot remove Solr entry " + path, e);
            }
         }
         try
         {
            server.commit ();
         }
         catch (Exception e)
         {
            logger.error ("Cannot commit Solr changes.");
         }
      }
      if (changes) resetQueryCache ();
      
   }
   
   /**
    * Search all product bypassing the default query filter that hides all
    * the product under processing thanks to the passed query. The search is 
    * only performed on not processed products.
    * This call does not care of user rights. It is also no handled by
    * the search cache. Default fetch size used is 10.
    * @param query the product request query.
    * @return search result iterator.
    */
   private SearchResult searchNoFilterQuery (String query)
   {
      return new SearchResult (getSolrServer(), query, 10, "*");
      
   }
   
   public void setProcessed (Product p)
   {
      SolrServer server = getSolrServer ();
      String path = ProductDao.getPathFromProduct (p);
      SolrDocument doc = getDocumentByPath (path);
      if (doc == null)
      {
         logger.warn (
            "Cannot retrieve Product in solr to set it processed with path \"" + 
            path);
         return;
      } 
      
      SolrInputDocument new_doc = ClientUtils.toSolrInputDocument (doc);
      new_doc.setField("processed", true);

      try
      {
         server.add (new_doc);
         server.commit ();
      }
      catch (SolrServerException e)
      {
         logger.error ("Cannot set product processed", e);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
