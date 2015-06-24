package fr.gael.dhus.search.geocoder.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import fr.gael.dhus.search.geocoder.impl.NominatimGeocoder;

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
      this.geocoder = new NominatimGeocoder();
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
      String wkt_france = this.geocoder.getBoundariesWKT("france");
      Assert.assertNotNull (wkt_france, "France geometry not found.");
      
      String wkt_paris = this.geocoder.getBoundariesWKT("paris");
      Assert.assertNotNull (wkt_paris, "Paris geometry not found.");

      
      WKTReader reader = new WKTReader ();
      Geometry france = reader.read (wkt_france);
      Geometry paris = reader.read (wkt_paris);
      
      // Test paris place is inside "France"
      Assert.assertTrue (france.contains (paris), 
         "Paris geometry is not inside france");

      // Check Noumea france territory in also inside france
      String wkt_noumea = this.geocoder.getBoundariesWKT("nouméa");
      Assert.assertNotNull (wkt_noumea, "Nouméa geometry not found.");
      Geometry noumea = reader.read (wkt_noumea);
      Assert.assertTrue (france.contains (noumea),
         "Nouméa geometry is not inside france");

      // Check berlin is outside france
      String wkt_berlin = this.geocoder.getBoundariesWKT("berlin");
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
      String wkt_brussels = this.geocoder.getBoundariesWKT("brussels");
      Assert.assertNotNull (wkt_brussels, "Brussels geometry not found.");
   }

   /**
    * Ensure sentinel are not recognized as location. 
    */
   @Test (groups={"non-regression"})
   public void getBoundariesWKTSentinel()
   {
      // Test "sentinel" address
      Assert.assertNull(this.geocoder.getBoundariesWKT("sentinel"),
         "\"sentinel\" address should not return any result");
   }
   
   /**
    * Ensure envisat are not recognized as location.
    */
   @Test (groups={"non-regression"})
   public void getBoundariesWKTEnvisat()
   {
      // Test "sentinel" address
      Assert.assertNull(this.geocoder.getBoundariesWKT("envisat"),
         "\"sentinel\" address should not return any result");
   }


} // End NominatimGeocoderTest class
