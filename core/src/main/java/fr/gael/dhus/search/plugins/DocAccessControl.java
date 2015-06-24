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
package fr.gael.dhus.search.plugins;

import java.io.IOException;

import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DelegatingCollector;
import org.apache.solr.search.ExtendedQueryBase;
import org.apache.solr.search.PostFilter;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SyntaxError;

import fr.gael.dhus.search.SolrUtils;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class DocAccessControl extends QParserPlugin
{

   @Override
   @SuppressWarnings ("rawtypes")
   public void init (NamedList args)
   {
   }

   @Override
   public QParser createParser (String qstr, SolrParams localParams,
      SolrParams params, SolrQueryRequest req)
   {
      SolrParams solrParams = SolrParams.wrapDefaults (localParams, params);
      final Long userId =
         Long.valueOf (solrParams.get (SolrUtils.CURRENT_USER_ID, "-1"));
      return new QParser (qstr, localParams, params, req)
      {
         @Override
         public Query parse () throws SyntaxError
         {
            return new DocAccess (userId);
         }
      };
   }

   static class DocAccess extends ExtendedQueryBase implements PostFilter
   {
      private final ProductService pservice;
      private final Long userId;

      DocAccess (Long uid)
      {
         this.pservice =
            ApplicationContextProvider.getBean (ProductService.class);
         this.userId = uid;
      }

      @Override
      public boolean getCache ()
      {
         return false;
      }

      @Override
      public int getCost ()
      {
         return Math.max (SolrUtils.MIN_COST_FILTER, super.getCost ());
      }

      @Override
      public DelegatingCollector getFilterCollector (
         final IndexSearcher searcher)
      {
         return new DelegatingCollector ()
         {
            @Override
            public void collect (int doc) throws IOException
            {
               IndexableField field =
                  searcher.doc (doc).getField ("id");
               if (field != null)
               {
                  long productId = field.numericValue ().longValue ();
                  if (pservice.hasAccessToProduct (userId, productId))
                     super.collect (doc);
               }
            }

            @Override
            public boolean acceptsDocsOutOfOrder ()
            {
               return true;
            }
         };
      }

      @Override
      public int hashCode ()
      {
         return userId.hashCode ();
      }

      @Override
      public boolean equals (final Object obj)
      {
         if (obj == null) return false;
         if (obj == this) return true;
         if ( !DocAccess.class.equals (obj.getClass ())) return false;
         DocAccess other = (DocAccess) obj;
         return this.userId.equals (other);
      }
   }
}
