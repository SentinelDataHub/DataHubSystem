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
package fr.gael.dhus.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import fr.gael.dhus.database.dao.ActionRecordWritterDao;
import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.search.SearchResult;
import fr.gael.dhus.search.SolrDao;

/**
 * @author pidancier
 *
 */
@Service
public class SearchService extends WebService
{
   private static Log logger = LogFactory.getLog (SearchService.class);
   
   @Autowired
   private SolrDao solrDao;

   @Autowired
   private ProductDao productDao;
   
   @Autowired
   private SecurityService securityService;
      
   @Autowired
   private ActionRecordWritterDao actionRecordWritterDao;   
      
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   public List<Product> search (String query, int startIndex, int numElement)
   {
      if ((startIndex<0)||(numElement<=0)) return ImmutableList.of ();

      User u = securityService.getCurrentUser ();
      actionRecordWritterDao.search(query, startIndex, numElement, u);
      
      SearchResult docs = solrDao.search (query, true, u);
      
      List<SolrDocument>documents = docs.get (startIndex, numElement);
      
      logger.debug ("docs Found -> " + docs.size () + " [" + startIndex + ", " + (startIndex+numElement) +"]");
      
      List<Product>products = solrDao.getProductListByDocList (documents);
      
      return products;
   }
   
   @PreAuthorize ("hasRole('ROLE_SEARCH')")
   public int getResultCount (String query)
   {
      User u = securityService.getCurrentUser ();
      return (int)solrDao.count (query, u);
   }
   
   public List<String> getSuggestions (String query)
   {
      return solrDao.getSuggestions (query);
   }
}
