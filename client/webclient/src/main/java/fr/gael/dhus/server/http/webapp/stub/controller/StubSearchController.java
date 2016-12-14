package fr.gael.dhus.server.http.webapp.stub.controller;



import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.InputSource;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.context.jts.ValidationRule;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.MetadataIndexData;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.ProductData;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import java.util.AbstractList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocumentList;



@RestController
public class StubSearchController {

	private static Log logger = LogFactory.getLog (StubSearchController.class);
	

   @RequestMapping (value = "/products")
   public List<ProductData> newsearch (@RequestParam(value="filter", defaultValue="") String filter, @RequestParam(value="offset", defaultValue="0")int startIndex, 
           @RequestParam(value="limit", defaultValue="")int numElement, @RequestParam(value="sortedby", defaultValue="ingestiondate") String sortedby,
           @RequestParam(value="order", defaultValue="desc") String order)
   {
      fr.gael.dhus.service.SearchService searchService = ApplicationContextProvider
              .getBean(fr.gael.dhus.service.SearchService.class);

      final fr.gael.dhus.service.ProductService productService= 
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductService.class);
      ArrayList<ProductData> productDatas = new ArrayList<ProductData>();
      try {
          SolrQuery.ORDER solrOrder = SolrQuery.ORDER.desc;
          if(order.equalsIgnoreCase("asc")){
              solrOrder = SolrQuery.ORDER.asc;
          }
          SolrQuery sQuery = new SolrQuery();
          sQuery.setQuery(filter);
          sQuery.setSort(sortedby, solrOrder);
          sQuery.setStart(startIndex);
          sQuery.setRows(numElement);
          
          final SolrDocumentList results = 
                 searchService.search(sQuery);
           List<Product > products = new AbstractList<Product>()
         {
            @Override
            public Product get(int index)
            {
               Long pid = (Long) results.get(index).get("id");
               return productService.getProduct(pid);
            }

            @Override
            public int size()
            {
               return results.size();
            }
         };

         if (products != null) {
            for (Product product : products) {
                if(product != null) {
                    ProductData productData =
                            new ProductData(product.getId(), product.getUuid(),
                                    product.getIdentifier());

                    // Set the Footprint if any
                    productData.setFootprint(
                            StubSearchController.convertGMLToDoubleLonLat(
                                    product.getFootPrint()));
                    logger.debug("JTS FOOTPRINT");
                    logger.debug(productData.getFootprint());

                    ArrayList<String> summary = new ArrayList<String>();
                    ArrayList<MetadataIndexData> indexes =
                            new ArrayList<MetadataIndexData>();

                    for (MetadataIndex index :
                            productService.getIndexes(product.getId())) {
                        MetadataIndexData category =
                                new MetadataIndexData(index.getCategory(), null);
                        int i = indexes.indexOf(category);
                        if (i < 0) {
                            category.addChild(new MetadataIndexData(
                                    index.getName(), index.getValue()));
                            indexes.add(category);
                        } else {
                            indexes.get(i).addChild(
                                    new MetadataIndexData(index.getName(), index
                                            .getValue()));
                        }

                        if ("summary".equals(index.getCategory())) {
                            summary.add(index.getName() + " : " + index.getValue());
                            Collections.sort(summary, null);
                        }

                        if ("Instrument".equalsIgnoreCase(index.getName())) {
                            productData.setInstrument(index.getValue());
                        }

                        if ("Product type".equalsIgnoreCase(index.getName())) {
                            productData.setProductType(index.getValue());
                        }

                    }
                    productData.setSummary(summary);
                    productData.setIndexes(indexes);
                    productData.setItemClass(product.getItemClass());
                    productData.setHasQuicklook(product.getQuicklookFlag());
                    productData.setHasThumbnail(product.getThumbnailFlag());

                    productDatas.add(productData);
                }
            }
         }
         return productDatas;
      } catch (Exception e) {
         e.printStackTrace();         
      }
      return productDatas;
   }

   @RequestMapping (value = "/products/count")
   public int getProductsCount (@RequestParam(value="filter", defaultValue="") String filter)
   {
	   int count=0;
	   
       fr.gael.dhus.service.SearchService searchService = ApplicationContextProvider
              .getBean(fr.gael.dhus.service.SearchService.class);      
       try {
         count = searchService.getResultCount(filter);
         //logger.info("Retrieved Products: " +  count);
       } catch (Exception e) {
    	   logger.error("Error while getting products count: " +  e.getMessage());
         e.printStackTrace();         

       }
       return count;
   }
   
   // test!

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
         ctx.verifyX(coord.y);
         ctx.verifyY(coord.x);         
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
      } catch (RuntimeException re) {
        //repair:
        if (validationRule == ValidationRule.repairConvexHull) {
          jts = ctx.makeShape(p.convexHull(), true, ctx.isAllowMultiOverlap());
        } else if (validationRule == ValidationRule.repairBuffer0) {
          jts = ctx.makeShape(p.buffer(0), true, ctx.isAllowMultiOverlap());
        } else if (validationRule == ValidationRule.error) {   
        	//get original coordinates without transformations
        	logger.debug("ValidationRule.error");
            return getDoubleLonLatFromOriginalCoordinates(footprint.getCoordinates ());
            
          }else {
          //TODO there are other smarter things we could do like repairing inner holes and subtracting
          //  from outer repaired shell; but we needn't try too hard.          
          try {
        	  jts = ctx.makeShape(p.getBoundary());
		} catch (Exception e) {
			logger.error("Not possible to get JTS footprint. Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
          re.printStackTrace();
          logger.error(re.getMessage());
          return null;
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
   
   /** 
    *
    * @param coords
    * @return Double Lon Lat from Original coordinates
    */
   public static Double [][][] getDoubleLonLatFromOriginalCoordinates(Coordinate[] coords) {
	   Double[][][] pts;
	    	    	  
     pts = new Double[1][coords.length][2];
     int i = 0;
     for (Coordinate coord : coords)
     {
        pts[0][i] = new Double[2];
        pts[0][i][0] = coord.y;
        pts[0][i][1] = coord.x;
        i++;
     }
	   return pts;
	   
   }

}
