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
package fr.gael.dhus.datastore.processing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.datastore.processing.impl.ProcessProductFootprint;
import fr.gael.dhus.datastore.processing.impl.ProcessProductIdentifier;
import fr.gael.dhus.datastore.processing.impl.ProcessProductImages;
import fr.gael.dhus.datastore.processing.impl.ProcessProductIndexes;
import fr.gael.dhus.datastore.processing.impl.ProcessProductInfo;
import fr.gael.dhus.datastore.processing.impl.ProcessProductPrepareDownload;
import fr.gael.dhus.datastore.processing.impl.ProcessProductSolrIndex;
import fr.gael.dhus.datastore.processing.impl.ProcessProductTransfer;
import fr.gael.dhus.datastore.processing.impl.ProcessingProductCollections;
import fr.gael.dhus.datastore.processing.impl.ProcessingProductRights;

/**
 * wake-up all registered services providers interfaces dedicated to
 * DHuS dataset processing. 
 *
 */
@Component
public class ProcessingFactory
{
   @Autowired
   ProcessProductTransfer processingProductTransfer;
   
   @Autowired
   ProcessProductInfo processingProductInfo;
   
   @Autowired
   ProcessProductIndexes processingProductIndex;
   
   @Autowired
   ProcessProductIdentifier processingProductIdentifier;
   
   @Autowired
   ProcessProductImages processingProductQuicklook; 
   
   @Autowired
   ProcessProductSolrIndex processProductLuceneIndex; 
   
   @Autowired
   ProcessProductFootprint processProductFootprint;
   
   @Autowired
   ProcessProductPrepareDownload processProductPrepareDownload;
   
   @Autowired
   ProcessingProductRights processProductRights;
   
   @Autowired
   ProcessingProductCollections processingProductCollections;
   
   public List<Processing<Product>> getProcessings ()
   {
      List<Processing<Product>> list = new ArrayList<Processing<Product>> ();
      list.add (processingProductTransfer);
      list.add (processingProductInfo);
      list.add (processingProductIdentifier);
      list.add (processingProductIndex);
      list.add (processProductFootprint);
      list.add (processingProductQuicklook);
      list.add (processProductPrepareDownload);
      list.add (processProductLuceneIndex);
      list.add (processProductRights);
      list.add (processingProductCollections);
      return list;
   }
}
