package fr.gael.dhus;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDate 
{
   // Drôle de test ...
   @Test
   public void testTimezone ()
   {
      Calendar now = Calendar.getInstance (TimeZone.getTimeZone ("UTC"));
      Assert.assertEquals (now.getTimeZone ().getID (), "UTC", 
         "UTC timezone is expected");
   }
   // Idem...
   @Test
   public void testDate ()
   {
      Calendar now = Calendar.getInstance();
      Date d_now = new Date ();
      
      double difference = d_now.getTime () - now.getTimeInMillis () ;
      
      Assert.assertEquals (difference, 0.0, 5.0);
   }

}
