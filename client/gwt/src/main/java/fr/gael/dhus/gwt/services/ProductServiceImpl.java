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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.xml.sax.InputSource;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.jts.JtsWktShapeParser.ValidationRule;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.gwt.services.annotation.RPCService;
import fr.gael.dhus.gwt.share.MetadataIndexData;
import fr.gael.dhus.gwt.share.ProductData;
import fr.gael.dhus.gwt.share.exceptions.AccessDeniedException;
import fr.gael.dhus.gwt.share.exceptions.ProductServiceException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

@RPCService ("productService")
public class ProductServiceImpl extends RemoteServiceServlet implements
   ProductService
{
   private static Logger logger = Logger.getLogger (ProductServiceImpl.class);
   private static final long serialVersionUID = -2230916029630238051L;

   @Override
   public List<ProductData> getProducts (int start, int count, String filter, Long parentId) throws ProductServiceException, AccessDeniedException
   {
      fr.gael.dhus.service.ProductService productService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ProductService.class);

      try
      {
         Iterator<Product> products =
               productService.getProducts (filter, parentId, start);
         ArrayList<ProductData> productDatas = new ArrayList<ProductData> ();
         while (products.hasNext ())
         {
            Product product = products.next ();
            if (product == null)
            {
               continue;
            }
            ProductData product_data = new ProductData (product.getId (),
                  product.getUuid (), product.getIdentifier ());
            productDatas.add (product_data);
         }
         return productDatas;
      }
      catch (org.springframework.security.access.AccessDeniedException e)
      {
         e.printStackTrace ();
         throw new AccessDeniedException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductServiceException (e.getMessage ());
      }
   }

   @Override
   public Integer count (String filter, Long parentId) throws ProductServiceException, AccessDeniedException
   {
      fr.gael.dhus.service.ProductService productService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ProductService.class);
      
      try
      {
         return productService.count (filter, parentId);
      }
      catch (org.springframework.security.access.AccessDeniedException e)
      {
         e.printStackTrace ();
         throw new AccessDeniedException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductServiceException (e.getMessage ());
      }
   }
   
   @Override
   public ProductData getProduct(long pId) throws ProductServiceException, AccessDeniedException
   {
      fr.gael.dhus.service.ProductService productService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ProductService.class);
      
      try
      {
         Product product = productService.getProduct (pId);
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
         
         List<MetadataIndex>pindexes=productService.getIndexes(product.getId()); 
         if(indexes!=null)
         {
            for (MetadataIndex index:pindexes)
            {
               if (index.getCategory () == null || 
                   index.getCategory ().isEmpty ())
               {
                  continue;
               }
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
                     new MetadataIndexData (index.getName (), 
                        index.getValue ()));
               }

               if ("summary".equals (index.getCategory ()))
               {
                  summary.add (index.getName () + " : " + index.getValue ());
                  Collections.sort (summary, null);
               }
            }
         }
         for (MetadataIndexData meta : indexes)
         {
            List<MetadataIndexData> children = meta.getChildren ();
            Collections.sort (children, new Comparator<MetadataIndexData>()
            {
               @Override
               public int compare (MetadataIndexData o1, MetadataIndexData o2)
               {
                  return o1.getName ().compareTo (o2.getName());
               }
            });
         }
         productData.setSummary (summary);
         productData.setIndexes (indexes);
         
         productData.setHasQuicklook (product.getQuicklookFlag ());
         productData.setHasThumbnail (product.getThumbnailFlag ());       
         
         return productData;
      }
      catch (org.springframework.security.access.AccessDeniedException e)
      {
         e.printStackTrace ();
         throw new AccessDeniedException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductServiceException (e.getMessage ());
      }
   }

   @Override
   public void deleteProduct(Long pid) throws  ProductServiceException, AccessDeniedException
   {
      fr.gael.dhus.service.ProductService productService = ApplicationContextProvider
            .getBean (fr.gael.dhus.service.ProductService.class);

      try
      {
         productService.deleteProduct(pid);
      }
      catch (org.springframework.security.access.AccessDeniedException e)
      {
         e.printStackTrace ();
         throw new AccessDeniedException (e.getMessage ());
      }
      catch (Exception e)
      {
         e.printStackTrace ();
         throw new ProductServiceException (e.getMessage ());
      }
   }
   
   public static Double [][][]convertGMLToDoubleLonLat (String gml)
   {
      if (gml ==null || gml.trim ().isEmpty ()) return null;
      Configuration configuration = new GMLConfiguration ();
      Parser parser = new Parser (configuration);
      
      Geometry footprint;
      try
      {
         footprint = (Geometry) parser.parse (new InputSource (
            new StringReader (gml)));
      }
      catch (Exception e)
      {
         logger.error ("Cannot read GML coordinates: " +
            (gml==null?gml:gml.trim ()), e);
         return null;
      }
      
      JtsSpatialContext ctx = JtsSpatialContext.GEO;
      GeometryFactory geometryFactory = ctx.getGeometryFactory();

      List<Coordinate> sequence = new ArrayList<Coordinate>();
      for (Coordinate coord : footprint.getCoordinates ())
      {
         sequence.add(new Coordinate (coord.y, coord.x));
      }

      LinearRing shell = geometryFactory.createLinearRing
          (sequence.toArray(new Coordinate[sequence.size()]));
      
      Polygon p = geometryFactory.createPolygon(shell, null);
      JtsSpatialContextFactory factory = new JtsSpatialContextFactory ();
      ValidationRule validationRule = factory.validationRule;
      JtsGeometry jts;
      try {
        jts = ctx.makeShape(p, true, ctx.isAllowMultiOverlap());
        if (validationRule != ValidationRule.none)
          jts.validate();
      } catch (RuntimeException e) {
        //repair:
        if (validationRule == ValidationRule.repairConvexHull) {
          jts = ctx.makeShape(p.convexHull(), true, ctx.isAllowMultiOverlap());
        } else if (validationRule == ValidationRule.repairBuffer0) {
          jts = ctx.makeShape(p.buffer(0), true, ctx.isAllowMultiOverlap());
        } else {
          //TODO there are other smarter things we could do like repairing inner holes and subtracting
          //  from outer repaired shell; but we needn't try too hard.
          throw e;
        }
      }
      if (factory.autoIndex)
        jts.index();
      
      Double[][][] pts;
      if (jts.getGeom () instanceof MultiPolygon)
      {         
         pts = new Double [((MultiPolygon)jts.getGeom ()).getNumGeometries ()][][];
         for (int j = 0; j < ((MultiPolygon)jts.getGeom ()).getNumGeometries (); j++)
         {
            pts[j] = new Double[((MultiPolygon)jts.getGeom ()).getGeometryN (j).getNumPoints ()][2];
            int i = 0;
            for (Coordinate coord : ((MultiPolygon)jts.getGeom ()).getGeometryN (j).getCoordinates ())
            {
               pts[j][i] = new Double[2];
               pts[j][i][0] = coord.x;
               pts[j][i][1] = coord.y;
               i++;
            }
         }         
      }
      else
      {         
         pts = new Double[1][jts.getGeom ().getNumPoints ()][2];
         int i = 0;
         for (Coordinate coord : jts.getGeom ().getCoordinates ())
         {
            pts[0][i] = new Double[2];
            pts[0][i][0] = coord.x;
            pts[0][i][1] = coord.y;
            i++;
         }
      }
      return pts;
   }
}