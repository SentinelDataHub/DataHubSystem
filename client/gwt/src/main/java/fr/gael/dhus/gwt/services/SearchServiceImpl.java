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
package fr.gael.dhus.gwt.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.MetadataIndexData;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.exceptions.SearchServiceException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

@RPCService ("searchService")
public class SearchServiceImpl extends RemoteServiceServlet implements
   SearchService
{
   private static final long serialVersionUID = 503312188474097406L;

   @Override
   public List<ProductData> search (String filter, int startIndex, int numElement, Long userId)
      throws SearchServiceException
   {      
      fr.gael.dhus.service.SearchService searchService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.SearchService.class);
      try
      {
         List<Product> products =
            searchService.search (filter, startIndex, numElement);
         
         ArrayList<ProductData> productDatas = new ArrayList<ProductData> ();

         Configuration configuration = new GMLConfiguration ();
         @SuppressWarnings ("unused")
         Parser parser = new Parser (configuration);

         if (products != null)
         {
            for (Product product : products)
            {
               ProductData productData =
                  new ProductData (product.getId (), product.getUuid (),
                     product.getIdentifier ());
               
               // Set the Footprint if any
               productData.setFootprint (
                  ProductServiceImpl.convertGMLToDoubleLonLat (
                     product.getFootPrint ()));


               ArrayList<String> summary = new ArrayList<String> ();
               ArrayList<MetadataIndexData> indexes =
                  new ArrayList<MetadataIndexData> ();

               for (MetadataIndex index : product.getIndexes ())
               {
                  MetadataIndexData category =
                     new MetadataIndexData (index.getCategory (), null);
                  int i = indexes.indexOf (category);
                  if (i < 0)
                  {
                     category.addChild (new MetadataIndexData (
                        index.getName (), index.getValue ()));
                     indexes.add (category);
                  }
                  else
                  {
                     indexes.get (i).addChild (
                        new MetadataIndexData (index.getName (), index
                           .getValue ()));
                  }

                  if ("summary".equals (index.getCategory ()))
                  {
                     summary.add (index.getName () + " : " + index.getValue ());
                     Collections.sort (summary, null);
                  }
               }
               productData.setSummary (summary);
               productData.setIndexes (indexes);
               
               productData.setHasQuicklook (product.getQuicklookFlag ());
               productData.setHasThumbnail (product.getThumbnailFlag ());         
               
               productDatas.add (productData);
            }
         }
         return productDatas;
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new SearchServiceException (e.getMessage ());
      }
   }

   public Integer count (String query) throws SearchServiceException
   {
      fr.gael.dhus.service.SearchService searchService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.SearchService.class);

      try
      {
         return searchService.getResultCount (query);
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new SearchServiceException (e.getMessage ());
      }
   }
}
