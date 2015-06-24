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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;

import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.SecurityService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * @author pidancier
 *
 */
public class SearchResult implements Iterator<SolrDocument> 
{
   private static Log logger = LogFactory.getLog (SearchResult.class);
   private int fetchSize = 50;
   
   private SolrServer server;
   private SolrQuery query;
   private String squery;
   private String filterQuery;
   
   private long offset=0;
   private long totalResults=0;
   
   
   /**
    * Defines the default boost function
    */
   public final String BOOST_FUNCTION_SECTION = "";
   // "{!boost b=recip(ms(NOW/HOUR,ingestiondate),3.16e-11,1,1)}";
   
   public SearchResult (SolrServer server, String query, int fetchSize,
      String fq)
   {
      this (server, query, fetchSize);
      this.filterQuery = fq;
   }
   
   public SearchResult (SolrServer server, String query, int fetchSize)
   {
      this.server = server;
      this.squery = query;
      this.fetchSize = fetchSize;
   }
   
   public SearchResult (SolrServer server, String query)
   {
      this.server = server;
      this.squery = query;
   }

   @Override
   public boolean hasNext ()
   {
      initQuery ();
      return offset<totalResults ;
   }
   
   public List<SolrDocument>get (int start, int keep)
   {
      initQuery ();
      query.setStart(start);
      query.setRows(keep);
      
      try
      {
         return server.query(query).getResults();
      }
      catch (SolrServerException e)
      {
         throw new DHusSearchException ("Cannot get next result.",e);
      }
   }

   @Override
   public SolrDocument next ()
   {
      initQuery ();
      query.setStart((int) offset);
      query.setRows(fetchSize);
      
      try
      {
         offset++;
         SolrDocumentList lst = server.query(query).getResults();
         return lst.get (0);
      }
      catch (SolrServerException e)
      {
         throw new DHusSearchException ("Cannot get next result.",e);
      }
   }

   
   @Override
   public void remove ()
   {
      query.setStart((int) offset-1);
      query.setRows(fetchSize);
      SolrDocumentList lst;
      try
      {
         lst = server.query(query).getResults();
         SolrDocument doc=lst.get (0);
         Object o = doc.get ("id");
         String id = o.toString ();
         server.deleteById (id);
      }
      catch (SolrServerException e)
      {
         throw new DHusSearchException ("Cannot get result to remove.",e);
      }
      catch (IOException e)
      {
         throw new DHusSearchException ("Remove document failed.",e);
      }
      offset--;
      totalResults--;
   }
   
   public long size ()
   {
      initQuery ();
      return totalResults;
   }
   
   void initQuery ()
   {
      if (query == null)
      {
         SolrQuery query = new SolrQuery ();
         query.setQuery (BOOST_FUNCTION_SECTION + squery);
         query.setRows (fetchSize);
         if (this.filterQuery != null)
         {
            query.addFilterQuery (this.filterQuery);
         }

         if (Boolean.parseBoolean (System.getProperty ("solr.filter.user",
            "false")))
         {
            SecurityService secuService =
               ApplicationContextProvider.getBean (SecurityService.class);

            User user = secuService.getCurrentUser ();
            if (user == null)
            {
               user =
                  ApplicationContextProvider.getBean (UserDao.class)
                     .getRootUser ();
            }
            query.add (SolrUtils.CURRENT_USER_ID, user.getId ().toString ());
         }

         QueryResponse rsp;
         try
         {
            rsp = server.query (query, SolrRequest.METHOD.POST);
         }
         catch (SolrServerException e)
         {
            logger.error ("Error in query \"" + query + "\" : " +
               e.getMessage ());
            totalResults = 0;
            this.query = query;
            return;
            // throw new DHusSearchException ("Cannot execute query", e);
         }
         catch (SolrException e)
         {
            logger.error ("Error in query \"" + query + "\" : " +
               e.getMessage ());
            totalResults = 0;
            this.query = query;
            return;
            // throw new DHusSearchException ("Cannot execute query", e);
         }
         totalResults = rsp.getResults ().getNumFound ();
         this.query = query;
      }
   }
   
   public void setOffset (long offset)
   {
      this.offset = offset;
   }
   public long getOffset ()
   {
      return this.offset;
   }

}
