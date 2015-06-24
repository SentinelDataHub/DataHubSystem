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
package fr.gael.dhus.datastore.processing.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.dao.ProductDao;
import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.processing.ProcessingProduct;
import fr.gael.dhus.search.DHusSearchException;
import fr.gael.dhus.search.SolrDao;

/**
 * @author pidancier
 *
 */
@Component
public class ProcessProductSolrIndex implements ProcessingProduct
{
   private static Logger logger = Logger.getLogger (ProcessProductSolrIndex.class);
   
   @Autowired
   private SolrDao solrDao;
   
   @Override
   public String getDescription ()
   {
      return "Processes the Solr Index to perform full-text search";
   }

   @Override
   public String getLabel ()
   {
      return "Solr Index processing";
   }

   @Override
   public void run (Product product)
   {
      List<MetadataIndex>indexes = product.getIndexes ();
      if ((indexes == null) || indexes.isEmpty ())
      {
         logger.warn ("No index defined for full-text search in product '" + 
            product.getIdentifier () + "'.");
         return;
      }
      try
      {
         solrDao.saveIndex (product, indexes);
      }
      catch (Exception e)
      {
         logger.error ("Cannot add entry into Solr Index.");
         throw new DHusSearchException ("Cannot add entry into Solr Index.", e);
      }
      
      if (logger.isDebugEnabled ())
      {
         logger.debug ("Inserted new Solr index (" + 
            solrDao.getDocumentsNumber () + ") - " + 
            ProductDao.getPathFromProduct (product));
      }
      return;
   }

   /**
    * Remove entry from Solr index related to the passed product
    */
   @Override
   public void removeProcessing (Product product)
   {
      try
      {
         solrDao.removeIndexes (product);
      }
      catch (Exception e)
      {
         logger.error ("Cannot remove indexes for path \"" + 
            ProductDao.getPathFromProduct (product) + "\".");
         throw new DHusSearchException ("Cannot add entry into Solr Index.", e);
      }
   }
}
