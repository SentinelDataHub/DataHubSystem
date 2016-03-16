package fr.gael.dhus.search.geocoder.impl;

import fr.gael.drb.impl.xml.XmlDocument;

import java.io.IOException;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Tests the NominatimGeocoder class implementing the Geocoder interface.
 */
public class NominatimGeocoderTest
{
   // A geocoder instance to test
   NominatimGeocoder geocoder;

   /**
    * Initialize the geocoder to be tested
    */
   @BeforeClass (alwaysRun = true)
   public void setUp()
   {
      this.geocoder = new NominatimGeocoder(null);
   }

   /**
    * Test the getBoundariesWKT() operation of the NominatimGeocoder.
    * The test consists in a call with "france" as search query that shall
    * contains paris, noumea, and shall not contains berlin.
    * @throws ParseException
    */
   @Test
   public void getBoundariesWKTFrance() throws ParseException
   {
      XmlDocument document = getDocument ("france.xml");
      String wkt_france = this.geocoder.computeWKT (document);
      Assert.assertNotNull (wkt_france, "France geometry not found.");

      document = getDocument ("paris.xml");
      String wkt_paris = this.geocoder.computeWKT (document);
      Assert.assertNotNull (wkt_paris, "Paris geometry not found.");


      WKTReader reader = new WKTReader ();
      Geometry france = reader.read (wkt_france);
      Geometry paris = reader.read (wkt_paris);

      // Test paris place is inside "France"
      Assert.assertTrue (france.contains (paris),
         "Paris geometry is not inside france");

      // Check Noumea france territory in also inside france
      document = getDocument ("noumea.xml");
      String wkt_noumea = this.geocoder.computeWKT (document);
      Assert.assertNotNull (wkt_noumea, "Nouméa geometry not found.");
      Geometry noumea = reader.read (wkt_noumea);
      Assert.assertTrue (france.contains (noumea),
         "Nouméa geometry is not inside france");

      // Check berlin is outside france
      document = getDocument ("berlin.xml");
      String wkt_berlin = this.geocoder.computeWKT (document);
      Assert.assertNotNull (wkt_berlin, "Berlin geometry not found.");

      Geometry berlin = reader.read (wkt_berlin);
      Assert.assertFalse (france.contains (berlin),
         "Berlin geometry is found inside france");
   }

   /**
    * At version 0.3.8-18, brussel location was not found in nominatim results
    * parsing. Checks that Brussels is still recognized.
    */
   @Test (groups={"non-regression"})
   public void getBoundariesWKTBrussels()
   {
      XmlDocument document = getDocument ("brussels.xml");
      String wkt_brussels = this.geocoder.computeWKT (document);
      Assert.assertNotNull (wkt_brussels, "Brussels geometry not found.");
   }

   /**
    * Ensure sentinel are not recognized as location.
    */
   @Test (groups={"non-regression"})
   public void getBoundariesWKTSentinel()
   {
      // Test "sentinel" address
      XmlDocument document = getDocument ("sentinel.xml");
      Assert.assertNull(this.geocoder.computeWKT (document),
         "\"sentinel\" address should not return any result");
   }

   /**
    * Ensure envisat are not recognized as location.
    */
   @Test (groups={"non-regression"})
   public void getBoundariesWKTEnvisat()
   {
      // Test "envisat" address
      XmlDocument document = getDocument ("envisat.xml");
      Assert.assertNull(this.geocoder.computeWKT (document),
         "\"sentinel\" address should not return any result");
   }

   private XmlDocument getDocument (String filename)
   {
      InputStream input = getClass ().getResourceAsStream (
         "/geocoder/nominatim/" + filename);
      XmlDocument document = new XmlDocument (input);
      try
      {
         input.close ();
      }
      catch (IOException e)
      {
         return null;
      }
      return document;
   }
} // End NominatimGeocoderTest class
