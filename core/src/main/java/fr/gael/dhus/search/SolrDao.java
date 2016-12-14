/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;

import org.springframework.beans.factory.annotation.Autowired;

import fr.gael.dhus.database.object.config.search.GeocoderConfiguration;
import fr.gael.dhus.search.geocoder.CachedGeocoder;
import fr.gael.dhus.search.geocoder.Geocoder;
import fr.gael.dhus.search.geocoder.impl.NominatimGeocoder;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Low level Solr interface.
 */
public class SolrDao
{
   /** Logger. */
   private static final Logger LOGGER = LogManager.getLogger(SolrDao.class);

   /**
    * Inner connections timeouts to the private solr service.
    */
   public static final int INNER_TIMEOUT = 
      Integer.getInteger("dhus.search.innerTimeout", 5000);

   /** URL path to solr service. */
   private static final String SOLR_SVC = "/solr/dhus";

   /** SolrJ client. */
   private final HttpSolrClient solrClient;

   /** Default Geocoder. */
   private final Geocoder geocoder;

   /** Dependency injection. */
   @Autowired
   private ConfigurationManager configurationManager;

   /**
    * DO NOT CALL! use Spring instead.
    * @param geocoder_conf geocoder's configuration object.
    */
   public SolrDao(GeocoderConfiguration geocoder_conf)
   {
       geocoder = new CachedGeocoder(new NominatimGeocoder(geocoder_conf));
       solrClient = new HttpSolrClient("");
       solrClient.setConnectionTimeout(INNER_TIMEOUT);
       solrClient.setSoTimeout(INNER_TIMEOUT);
   }

   /**
    * Initialises this DAO when the Tomcat server is started.
    * (because getUrl() calls getPort() which delegates to TomcatServer).
    */
   public void initServerStarted()
   {
      String dhus_url = configurationManager.getServerConfiguration().getLocalUrl();
      solrClient.setBaseURL(dhus_url + SOLR_SVC);
   }

   /**
    * System search.
    * @param query a complete and well configured query.
    * @return Solr response to given query.
    * @throws SolrServerException a solr error occured.
    * @throws IOException in case of network error.
    */
   public QueryResponse search(SolrQuery query) throws SolrServerException, IOException
   {
      return solrClient.query(query);
   }

   /**
    * Retrives SolrDocuments through a paginated iterator.
    * Should not be used to delete documents because of the built-in lazy pagination.
    * @param query to perform.
    * @return an iterator on SolrDocument.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public Iterator<SolrDocument> scroll(SolrQuery query) throws IOException, SolrServerException
   {
      return new IterableSearchResult(solrClient, query);
   }

   /**
    * Indexes a new document.
    * @param doc to index.
    * @return solr response.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public UpdateResponse index(SolrInputDocument doc) throws IOException, SolrServerException
   {
      return solrClient.add(doc);
   }

   /**
    * Performs a batch index of _many_ documents, uses the ConcurrentUpdateSolrClient.
    * <p>If you want faster indexing, disable the autoCommit and autoSoftCommit functionalities,
    * see {@link #disableAutoCommit()}.
    * @param source of document to index.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public void batchIndex(Iterator<SolrInputDocument> source) throws SolrServerException, IOException
   {
      String dhus_url = configurationManager.getServerConfiguration().getUrl() + SOLR_SVC;
      try (ConcurrentUpdateSolrClient client = new ConcurrentUpdateSolrClient(dhus_url, 1000, 10))
      {
         client.add(source);
         client.commit(true, true);
      }
   }

   /**
    * Get one document by its unique Id.
    * @param id unique identifier.
    * @return a doc or null if does not exist.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public SolrDocument getById(Long id) throws IOException, SolrServerException
   {
      return solrClient.getById(String.valueOf(id));
   }

   /**
    * Deletes a SolrDocument with the given id.
    * @param id of the SolrDocument to remove.
    * @return solr response.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public UpdateResponse remove(long id) throws IOException, SolrServerException
   {
      UpdateResponse res = solrClient.deleteById(String.valueOf(id));
      solrClient.commit(false, true, true); // mandatory explicit soft-commit.
      return res;
   }

   /**
    * Deletes all document, commit and optimizes the index.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public void removeAll() throws IOException, SolrServerException
   {
      // FIXME is it faster/more efficient to create a new empty core?
      long etimedelete = solrClient.deleteByQuery("*:*").getElapsedTime();
      long etimecommit = solrClient.commit(true, true, true).getElapsedTime();
      long etimeoptimi = solrClient.optimize().getElapsedTime();
      LOGGER.debug(String.format("removeAll:  delete: %dms    commit: %dms    optimize: %dms",
                                              etimedelete,     etimecommit,     etimeoptimi));
   }

   /**
    * Get suggestions from the suggester component.
    * @param input analysed by the suggester component.
    * @return the suggester component response.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public SuggesterResponse getSuggestions(String input) throws IOException, SolrServerException
   {
      SolrQuery query = new SolrQuery(input);
      query.setRequestHandler("/suggest");

      return search(query).getSuggesterResponse();
   }

   /**
    * Optimize the index, merges every segment of the index into one monolithic file.
    * Optimizing is very expensive, and if the index is constantly changing,
    * the slight performance boost will not last long...
    * The tradeoff is not often worth it for a non static index.
    * <p>
    * Blocking method, will block until optimization is complete. Solr won't respond to
    * search queries until optimization is done.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public void optimize() throws IOException, SolrServerException
   {
      solrClient.optimize();
   }

   /**
    * Set the given properties and values using the config API.
    * @param props properties to set.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public void setProperties(Map<String, String> props) throws SolrServerException, IOException
   {
      // Solrj does not support the config API yet.
      StringBuilder command = new StringBuilder("{\"set-property\": {");
      for (Map.Entry<String, String> entry: props.entrySet())
      {
         command.append('"').append(entry.getKey()).append('"').append(':');
         command.append(entry.getValue()).append(',');
      }
      command.setLength(command.length()-1); // remove last comma
      command.append("}}");

      GenericSolrRequest rq = new GenericSolrRequest(SolrRequest.METHOD.POST, "/config", null);
      ContentStream content = new ContentStreamBase.StringStream(command.toString());
      rq.setContentStreams(Collections.singleton(content));
      rq.process(solrClient);
   }

   /**
    * Unset the given properties that have been previously set with {@link #setProperties(Map)}.
    * @param props properties to unset.
    * @throws IOException network error.
    * @throws SolrServerException solr error.
    */
   public void unsetProperties(Set<String> props) throws SolrServerException, IOException
   {
      // Solrj does not support the config API yet.
      StringBuilder command = new StringBuilder("{\"unset-property\": [");
      for (String prop: props)
      {
         command.append('"').append(prop).append('"').append(',');
      }
      command.setLength(command.length()-1); // remove last comma
      command.append("]}");

      GenericSolrRequest rq = new GenericSolrRequest(SolrRequest.METHOD.POST, "/config", null);
      ContentStream content = new ContentStreamBase.StringStream(command.toString());
      rq.setContentStreams(Collections.singleton(content));
      rq.process(solrClient);
   }

   /**
    * Geocode query.
    * @param query query.
    * @return result.
    */
   public String updateQuery(String query)
   {
      for (String[]strs: SolrQueryParser.parse(query))
      {
         String key = strs[SolrQueryParser.INDEX_FIELD];
         String token = strs[SolrQueryParser.INDEX_VALUE];

         // If key defined, replace it by its lower case version.
         if (!"".equals(key))
         {
            query = query.replace(key, key.toLowerCase());
         }

         if (!(!"".equals(key) ||
               token.startsWith("{") ||
               token.startsWith("[") ||
               token.startsWith("(") ||
               token.contains("*") ||
               token.contains("?") ||
               token.contains("TO") ||
               token.contains("OR") ||
               token.contains("AND") ||
               token.matches(".*\\d.*")))
         {
            try
            {
               // If suggester knows the token: it is probably not a 
               // place location.
               if (!getSuggestions(token).getSuggestions().get("suggest").
                     isEmpty())
                  throw new Exception();
            }
            catch (Exception e)
            {
               return query;
            }

            String wtk_boundaries = geocoder.getBoundariesWKT(token);

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

   /**
    * An iterable SolrResponse, with pagination.
    */
   private static class IterableSearchResult implements Iterator<SolrDocument>
   {
      /** Logger. */
      private static final Logger LOGGER = LogManager.getLogger(IterableSearchResult.class);
      /** Default fetch size of 50 solr documents. */
      private static final int FETCH_SIZE = 50;

      /** Solr client. */
      private final SolrClient client;
      /** Solr query. */
      private final SolrQuery query;

      /** For iretation purposes: offset in the current response. */
      private int offset = 0;
      /** Current response being served by this class. */
      private QueryResponse rsp;

      /**
       * Creates a new SearchResult.
       * @param client Solr client instance.
       * @param query to perform.
       * @throws SolrServerException Solr client exception.
       * @throws IOException network exception.
       */
      public IterableSearchResult(SolrClient client, SolrQuery query)
            throws SolrServerException, IOException
      {
         Objects.requireNonNull(client);
         Objects.requireNonNull(query);

         this.client = client;
         this.query  = query;

         this.query.setRows(FETCH_SIZE);

         rsp = client.query(this.query, SolrRequest.METHOD.POST);
      }

      /** Run when every document in this.response have been served by next(). */
      private void getNextResponse() {
         int start = (this.query.getStart() != null)? this.query.getStart(): 0;
         this.query.setStart(start + offset);
         try
         {
            rsp = client.query(this.query, SolrRequest.METHOD.POST);
            offset = 0;
         }
         catch (SolrServerException | IOException ex)
         {
            LOGGER.warn("An exception occured, no more solr document to serve", ex);
         }
      }

      @Override
      public boolean hasNext()
      {
         if (offset >= this.rsp.getResults().size())
         {
            getNextResponse();
         }
         return offset < this.rsp.getResults().size();
      }

      @Override
      public SolrDocument next()
      {
         if (offset >= this.rsp.getResults().size())
         {
            getNextResponse();
            if (offset >= this.rsp.getResults().size())
            {
               throw new NoSuchElementException();
            }
         }
         int index = offset;
         offset += 1;
         return this.rsp.getResults().get(index);
      }

      @Override
      public void remove()
      {
         throw new UnsupportedOperationException("Not implemented.");
      }
   }
}
