package fr.gael.dhus.api.stub;



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
import fr.gael.dhus.api.stub.stub_share.MetadataIndexData;
import fr.gael.dhus.api.stub.stub_share.ProductData;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



@RestController
public class StubSearchController {

	private static Log logger = LogFactory.getLog (StubSearchController.class);
	

   @RequestMapping (value = "/stub/products")
   public List<ProductData> newsearch (@RequestParam(value="filter", defaultValue="") String filter, @RequestParam(value="offset", defaultValue="0")int startIndex, @RequestParam(value="limit", defaultValue="")int numElement)
   {
      fr.gael.dhus.service.SearchService searchService = ApplicationContextProvider
              .getBean(fr.gael.dhus.service.SearchService.class);

      fr.gael.dhus.service.ProductService productService= 
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductService.class);
      try {
         List<Product> products =
                 searchService.search(filter, startIndex, numElement);

         ArrayList<ProductData> productDatas = new ArrayList<ProductData>();

         Configuration configuration = new GMLConfiguration();
         @SuppressWarnings("unused")
         Parser parser = new Parser(configuration);

         if (products != null) {
            for (Product product : products) {
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

               productData.setHasQuicklook(product.getQuicklookFlag());
               productData.setHasThumbnail(product.getThumbnailFlag());

               productDatas.add(productData);
            }
         }
         return productDatas;
      } catch (Exception e) {
         e.printStackTrace();

      }
      return null;
   }

   @RequestMapping (value = "/stub/products/count")
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
