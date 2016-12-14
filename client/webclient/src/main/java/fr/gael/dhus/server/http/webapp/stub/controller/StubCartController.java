package fr.gael.dhus.server.http.webapp.stub.controller;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
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
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.MetadataIndexData;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.ProductData;
import fr.gael.dhus.server.http.webapp.stub.controller.stub_share.exceptions.ProductCartServiceException;
import fr.gael.dhus.service.exception.UserNotExistingException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.MetalinkBuilder;
import fr.gael.dhus.util.MetalinkBuilder.MetalinkFileBuilder;

@RestController
public class StubCartController
{
   private static Log logger = LogFactory.getLog(StubUserController.class);

   @Autowired
   private ConfigurationManager configurationManager;

   @RequestMapping (value = "/users/{userid}/cart/{cartid}/addproduct",
      method = RequestMethod.POST)
   public void addProductToCart(Principal principal,
      @PathVariable (value = "userid") String userid,
      @PathVariable (value = "cartid") String cartid,
      @RequestParam (value = "productId", defaultValue = "") Long pId) 
         throws ProductCartServiceException
   {
      User user = (User) ((UsernamePasswordAuthenticationToken) principal).
         getPrincipal();
      fr.gael.dhus.service.ProductCartService productCartService =
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductCartService.class);

      try
      {
         productCartService.addProductToCart(user.getUUID(), pId);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ProductCartServiceException(e.getMessage());
      }
   }

   @RequestMapping (value = "/users/{userid}/cart/{cartid}/removeproduct",
      method = RequestMethod.POST)
   public void removeProductFromCart(Principal principal,
      @PathVariable (value = "userid") String userid,
      @PathVariable (value = "cartid") String cartid,
      @RequestParam (value = "productId", defaultValue = "") Long pId)
         throws ProductCartServiceException
   {
      User user = (User) ((UsernamePasswordAuthenticationToken) principal).
         getPrincipal();
      fr.gael.dhus.service.ProductCartService productCartService =
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductCartService.class);
      try
      {
         productCartService.removeProductFromCart(user.getUUID(), pId);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ProductCartServiceException(e.getMessage());
      }
   }

   @RequestMapping (value = "/users/{userid}/cart/{cartid}/getcartids",
      method = RequestMethod.GET)
   public List<Long> getProductsIdOfCart(Principal principal,
      @PathVariable (value = "userid") String userid,
      @PathVariable (value = "cartid") String cartid)
         throws ProductCartServiceException
   {

      User user = (User) ((UsernamePasswordAuthenticationToken) principal).
         getPrincipal();
      fr.gael.dhus.service.ProductCartService productCartService =
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductCartService.class);
      try
      {
         return productCartService.getProductsIdOfCart(user.getUUID());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ProductCartServiceException(e.getMessage());
      }
   }

   @RequestMapping (value = "/users/{userid}/carts/{cartid}",
      method = RequestMethod.GET)
   public List<ProductData> getProductsOfCart(Principal principal,
      @PathVariable (value = "userid") String userid,
      @PathVariable (value = "cartid") String cartid,
      @RequestParam (value = "offset", defaultValue = "") int start,
      @RequestParam (value = "count", defaultValue = "")  int count)
         throws ProductCartServiceException
   {
      User user = (User) ((UsernamePasswordAuthenticationToken) principal).
         getPrincipal();
      fr.gael.dhus.service.ProductCartService productCartService =
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductCartService.class);
      fr.gael.dhus.service.ProductService productService =
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductService.class);

      try
      {
         List<Product> products = productCartService.getProductsOfCart(
            user.getUUID(), start, count);
         
         ArrayList<ProductData> productDatas = new ArrayList<ProductData>();
         Configuration configuration = new GMLConfiguration();
      
         @SuppressWarnings ("unused")
         Parser parser = new Parser(configuration);

         if (products != null)
         {
            logger.info("products not null");
            for (Product product : products)
            {
               if (product != null)
               {
                  logger.info("product not null");
                  ProductData productData =
                     new ProductData(product.getId(), product.getUuid(),
                                     product.getIdentifier());

                  // Set the Footprint if any
                  productData.setFootprint(StubCartController.
                     convertGMLToDoubleLonLat(product.getFootPrint()));

                  ArrayList<String> summary = new ArrayList<String>();
                  ArrayList<MetadataIndexData> indexes =
                     new ArrayList<MetadataIndexData>();

                  for (MetadataIndex index :
                       productService.getIndexes(product.getId()))
                  {
                     MetadataIndexData category =
                        new MetadataIndexData(index.getCategory(), null);
                     int i = indexes.indexOf(category);
                     if (i < 0)
                     {
                        category.addChild(new MetadataIndexData(index.getName(),
                           index.getValue()));
                        indexes.add(category);
                     }
                     else
                     {
                        indexes.get(i).addChild(
                           new MetadataIndexData(index.getName(), 
                              index.getValue()));
                     }

                     if ("summary".equals(index.getCategory()))
                     {
                        summary.add(index.getName() + " : " + index.getValue());
                        Collections.sort(summary, null);
                     }

                     if ("Instrument".equalsIgnoreCase(index.getName()))
                     {
                        productData.setInstrument(index.getValue());
                     }

                     if ("Product type".equalsIgnoreCase(index.getName()))
                     {
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
         }
         return productDatas;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ProductCartServiceException(e.getMessage());
      }
   }

   @RequestMapping (value = "/users/{userid}/cart/{cartid}/getcount",
      method = RequestMethod.GET)
   public int countProductsInCart(Principal principal,
      @PathVariable (value = "userid") String userid,
      @PathVariable (value = "cartid") String cartid)
         throws ProductCartServiceException
   {
      User user = (User)((UsernamePasswordAuthenticationToken) principal).
         getPrincipal();
      fr.gael.dhus.service.ProductCartService productCartService =
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductCartService.class);

      try
      {
         return productCartService.countProductsInCart(user.getUUID());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ProductCartServiceException(e.getMessage());
      }
   }

   @RequestMapping (value = "/users/{userid}/cart/{cartid}/clear",
      method = RequestMethod.POST)
   public void clearCart(Principal principal,
      @PathVariable (value = "userid") String userid,
      @PathVariable (value = "cartid") String cartid)
         throws ProductCartServiceException
   {
      User user = (User) ((UsernamePasswordAuthenticationToken) principal).
         getPrincipal();
      fr.gael.dhus.service.ProductCartService productCartService =
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductCartService.class);

      try
      {
         productCartService.clearCart(user.getUUID());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ProductCartServiceException(e.getMessage());
      }
   }

   @RequestMapping (value = "/users/{userid}/cart/{cartid}/download",
      method = RequestMethod.GET)
   public void cartToMetalink(Principal principal,
      @PathVariable (value = "userid") String userid,
      @PathVariable (value = "cartid") String cartid,
      HttpServletResponse res)
         throws UserNotExistingException, IOException, 
            ParserConfigurationException, TransformerException
   {
      User user = (User) ((UsernamePasswordAuthenticationToken) principal).
         getPrincipal();
      fr.gael.dhus.service.ProductCartService productCartService =
         ApplicationContextProvider.getBean(
            fr.gael.dhus.service.ProductCartService.class);

      if (productCartService.hasProducts(user.getUUID())) return;
      
      res.setContentType("application/metalink+xml");
      res.setHeader("Content-Disposition", "inline; filename=products" +
         MetalinkBuilder.FILE_EXTENSION);

      res.getWriter().println(makeMetalinkDocument(
         productCartService.getProductsOfCart(user.getUUID(), -1, -1)));
   }

   /**
    * Makes the metalink XML Document for given products.
    * 
    * @throws ParserConfigurationException
    * @throws TransformerException
    */
   private String makeMetalinkDocument(Iterable<Product> lp)
      throws ParserConfigurationException, TransformerException
   {
      MetalinkBuilder mb = new MetalinkBuilder();

      for (Product p : lp)
      {
         String product_entity =
            configurationManager.getServerConfiguration().getExternalUrl() +
               "odata/v1/Products('" + p.getUuid() + "')/$value";

         MetalinkFileBuilder fb = mb.addFile(
            new File(p.getDownload().getPath()).getName()).
               addUrl(product_entity, null, 0);

         if ( !p.getDownload().getChecksums().isEmpty())
         {
            Map<String, String> checksums = p.getDownload().getChecksums();
            for (String algo : checksums.keySet())
               fb.setHash(algo, checksums.get(algo));
         }
      }
      StringWriter sw = new StringWriter();

      Document doc = mb.build();
      Transformer transformer=TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                                    "2");
      transformer.transform(new DOMSource(doc), new StreamResult(sw));
      return sw.toString();
   }

   @RequestMapping (value = "/users/{userid}/cart/{cartid}/testcart",
      method = RequestMethod.GET)
   public String signupValidate(
      @PathVariable (value = "userid") String userid,
      @PathVariable (value = "cartid") String cartid)
   {
      return "hello from cart stub test webservice";
   }

   public static Double[][][] convertGMLToDoubleLonLat(String gml)
   {
      if (gml == null || gml.trim().isEmpty()) return null;
      Configuration configuration = new GMLConfiguration();
      Parser parser = new Parser(configuration);

      Geometry footprint;
      try
      {
         footprint =
            (Geometry) parser.parse(new InputSource(new StringReader(gml)));
      }
      catch (Exception e)
      {
         logger.error("Cannot read GML coordinates: " +
            (gml == null ? gml : gml.trim()), e);
         return null;
      }

      JtsSpatialContext ctx = JtsSpatialContext.GEO;
      GeometryFactory geometryFactory = ctx.getGeometryFactory();

      List<Coordinate> sequence = new ArrayList<Coordinate>();
      for (Coordinate coord : footprint.getCoordinates())
      {
         ctx.verifyX(coord.y);
         ctx.verifyY(coord.x);
         sequence.add(new Coordinate(coord.y, coord.x));
      }

      LinearRing shell = geometryFactory.createLinearRing(sequence.toArray(
         new Coordinate[sequence.size()]));

      Polygon p = geometryFactory.createPolygon(shell, null);
      JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
      ValidationRule validationRule = factory.validationRule;
      JtsGeometry jts;
      try
      {
         jts = ctx.makeShape(p, true, ctx.isAllowMultiOverlap());
         if (validationRule != ValidationRule.none) jts.validate();
      }
      catch (RuntimeException e)
      {
         // repair:
         if (validationRule == ValidationRule.repairConvexHull)
         {
            jts=ctx.makeShape(p.convexHull(), true, ctx.isAllowMultiOverlap());
         }
         else
         if (validationRule == ValidationRule.repairBuffer0)
         {
            jts=ctx.makeShape(p.buffer(0), true, ctx.isAllowMultiOverlap());
         }
         else
         {
            // TODO there are other smarter things we could do like repairing
            // inner holes and subtracting
            // from outer repaired shell; but we needn't try too hard.
            throw e;
         }
      }
      if (factory.autoIndex) jts.index();

      Double[][][] pts;
      if (jts.getGeom() instanceof MultiPolygon)
      {
         pts=new Double[((MultiPolygon)jts.getGeom()).getNumGeometries()][][];
         for (int j=0; j<((MultiPolygon)jts.getGeom()).getNumGeometries(); j++)
         {
            pts[j]=new Double[((MultiPolygon) jts.getGeom()).getGeometryN(j).
               getNumPoints()][2];
            int i = 0;
            for (Coordinate coord:((MultiPolygon) jts.getGeom()).
               getGeometryN(j).getCoordinates())
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
         pts = new Double[1][jts.getGeom().getNumPoints()][2];
         int i = 0;
         for (Coordinate coord : jts.getGeom().getCoordinates())
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
