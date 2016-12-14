package fr.gael.dhus.util;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.jts.JtsWKTReaderShapeParser;
import com.spatial4j.core.context.jts.DatelineRule;
import com.spatial4j.core.context.jts.ValidationRule;
import com.spatial4j.core.shape.jts.JtsGeometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class JTSFootprintParser
{
   private static final Logger LOGGER = LogManager.getLogger(JTSFootprintParser.class);

   /**
    * Check JTS Footprint validity.
    *
    * @param footprint to check.
    * @return corrected JTS footprint or null.
    */
   public static String checkJTSFootprint(String footprint)
   {
      JtsSpatialContextFactory defaultFactory = new JtsSpatialContextFactory();
      JtsSpatialContext defaultContext = defaultFactory.newSpatialContext();
      JtsWKTReaderShapeParser defaultParser =
            new JtsWKTReaderShapeParser(defaultContext, defaultFactory);
      try
      {
         defaultParser.parse(footprint);
         return footprint;
      }
      catch (Exception e)
      {
         LOGGER.warn("JTS Footprint error : " + e.getMessage());
         LOGGER.warn("Trying to cut the footprint in MultiPolygons.");

         JtsSpatialContextFactory noCheckFactory = new JtsSpatialContextFactory();
         noCheckFactory.datelineRule = DatelineRule.none;
         noCheckFactory.validationRule = ValidationRule.none;
         JtsSpatialContext noCheckContext = noCheckFactory.newSpatialContext();
         JtsWKTReaderShapeParser noCheckParser =
               new JtsWKTReaderShapeParser(noCheckContext, noCheckFactory);

         try
         {
            JtsGeometry footprintGeometry = (JtsGeometry) noCheckParser.parse(footprint);
            JtsGeometry antimeridianLine =
                  (JtsGeometry) defaultParser.parse("LINESTRING (180 -90, 180 90)");

            Coordinate last = null;
            boolean positiveSide = false;
            Coordinate first = null;

            String positivePolygon = "POLYGON ((";
            String negativePolygon = "POLYGON ((";
            Coordinate positivePolygonStart = null;
            Coordinate negativePolygonStart = null;
            for (Coordinate coord: footprintGeometry.getGeom().getCoordinates())
            {
               if (first == null)
               {
                  first = coord;
                  positiveSide = coord.x > 0;
                  if (positiveSide && positivePolygonStart == null)
                  {
                     positivePolygonStart = coord;
                  }
                  else if (negativePolygonStart == null)
                  {
                     negativePolygonStart = coord;
                  }
               }

               if (last != null)
               {
                  JtsGeometry currentSegment = (JtsGeometry) defaultParser.parse("LINESTRING (" +
                        last.x + " " + last.y + ", " + coord.x + " " + coord.y + ")");
                  // Test if segment is too much long (possible bad way)
                  if (currentSegment.getGeom().getLength() > 90)
                  {
                     // Then adding a point in center to force the right way
                     double x = last.x +
                           (last.x < coord.x ? -(coord.x - last.x) / 2 : (coord.x - last.x) / 2);
                     Coordinate center = new Coordinate(x, (last.y + coord.y) / 2);

                     if (positiveSide)
                     {
                        positivePolygon += center.x + " " + center.y + ", ";
                     }
                     else
                     {
                        negativePolygon += center.x + " " + center.y + ", ";
                     }

                     currentSegment = (JtsGeometry) defaultParser.parse("LINESTRING (" +
                           center.x + " " + center.y + ", " + coord.x + " " + coord.y + ")");
                  }

                  // Cutting polygon on antimeridian line if crossed
                  if (antimeridianLine.getGeom().intersects(currentSegment.getGeom()))
                  {
                     positivePolygon += "180 " + (last.y + coord.y) / 2 + ", ";
                     negativePolygon += "-180 " + (last.y + coord.y) / 2 + ", ";

                     positiveSide = coord.x > 0;

                     if (positiveSide && positivePolygonStart == null)
                     {
                        positivePolygonStart = new Coordinate(180, (last.y + coord.y) / 2);
                     }
                     else if (negativePolygonStart == null)
                     {
                        negativePolygonStart = new Coordinate(-180, (last.y + coord.y) / 2);
                     }
                  }
               }

               if (positiveSide)
               {
                  positivePolygon += coord.x + " " + coord.y + ", ";
               }
               else
               {
                  negativePolygon += coord.x + " " + coord.y + ", ";
               }
               last = coord;
            }

            List<Polygon> res = new ArrayList<>();

            if (positivePolygonStart != null)
            {
               String Q1STR = cutOnPoles(positivePolygon + positivePolygonStart.x + " " +
                     positivePolygonStart.y + "))");
               JtsGeometry q1g = (JtsGeometry) defaultParser.parse(Q1STR);
               res.add((Polygon) q1g.getGeom());
            }
            if (negativePolygonStart != null)
            {
               String Q2STR = cutOnPoles(negativePolygon + negativePolygonStart.x + " " +
                     negativePolygonStart.y + "))");
               JtsGeometry q2g = (JtsGeometry) defaultParser.parse(Q2STR);
               res.add((Polygon) q2g.getGeom());
            }

            GeometryFactory gf = new GeometryFactory();
            MultiPolygon mp = gf.createMultiPolygon(res.toArray(new Polygon[res.size()]));

            WKTWriter wkw = new WKTWriter();
            return wkw.write(mp);
         }
         catch (Exception e1)
         {
            LOGGER.error("JTS Footprint error : " + e1.getMessage(), e1);
            return null;
         }
      }
   }

   /**
    * Cut given polygon on poles (89 and -89)
    */
   private static String cutOnPoles(String polygonWKT) throws Exception
   {
      JtsSpatialContextFactory noCheckFactory = new JtsSpatialContextFactory();
      noCheckFactory.datelineRule = DatelineRule.none;
      noCheckFactory.validationRule = ValidationRule.none;
      JtsSpatialContext noCheckContext = noCheckFactory.newSpatialContext();
      JtsWKTReaderShapeParser noCheckParser =
            new JtsWKTReaderShapeParser(noCheckContext, noCheckFactory);

      JtsGeometry polygon = (JtsGeometry) noCheckParser.parse(polygonWKT);
      JtsGeometry northPole =
            (JtsGeometry) noCheckParser.parse("LINESTRING(180 89, 0 89, -180 89)");
      JtsGeometry southPole =
            (JtsGeometry) noCheckParser.parse("LINESTRING(180 -89, 0 -89, -180 -89)");

      LineMerger lm = new LineMerger();
      lm.add(polygon.getGeom());
      lm.add(northPole.getGeom());
      lm.add(southPole.getGeom());

      Geometry geometry = UnaryUnionOp.union(lm.getMergedLineStrings());

      Polygonizer polygonizer = new Polygonizer();
      polygonizer.add(geometry);

      List<Polygon> foundPolygons = (List<Polygon>) polygonizer.getPolygons();
      List<Polygon> filteredPolygons = new ArrayList<>();
      for (Polygon p: foundPolygons)
      {
         // removing polygons over the poles
         if (p.getCentroid().getCoordinate().y < 89 && p.getCentroid().getCoordinate().y > -89)
         {
            filteredPolygons.add(p);
         }
      }

      Geometry res = null;

      if (!filteredPolygons.isEmpty())
      {
         res = filteredPolygons.get(0);
      }
      if (filteredPolygons.size() > 1)
      {
         // Should not happen...
         LOGGER.error("A Multipolygon was found, instead of a single polygon. Only the first one is retained.");
      }

      WKTWriter wkw = new WKTWriter();
      return wkw.write(res);
   }
}
