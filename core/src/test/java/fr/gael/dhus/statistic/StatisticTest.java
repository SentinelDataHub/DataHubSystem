package fr.gael.dhus.statistic;

import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import fr.gael.dhus.database.dao.ActionRecordReaderDao;
import fr.gael.dhus.database.object.statistic.ActionRecord;

@ContextConfiguration(locations = "classpath:spring/common.xml")
public class StatisticTest
{
   private static Log logger = LogFactory.getLog (StatisticTest.class);
   
   @Autowired
   ActionRecordReaderDao actionRecordReaderDao;
   
   @Test
   public void testStatistics () 
   {
      GregorianCalendar cal = new GregorianCalendar ();
      cal.set (2013, 1, 1, 0, 0, 0);
      Date start = cal.getTime ();
      cal.set (2014, 1, 31, 0, 0, 0);
      Date end = cal.getTime ();
      
      HashMap<String, BigInteger> map;
      
      // Total users (and deleted, access restricted)

      logger.info ("Total users: " +
           actionRecordReaderDao.getTotalUsers ());

      logger.info ("Total deleted users: " +
           actionRecordReaderDao.getTotalDeletedUsers());
      
      logger.info ("Total access restricted users: " +
    		  actionRecordReaderDao.getTotalRestrictedUsers() + "\n");

//      Vector vRows = actionRecordReaderDao.getDetailedAccessRestrictedUsers();
//      
//      for (int i = 0; i < vRows.size(); i++)
//      {
//    	  logger.info ("User name: " + ((Vector)(vRows.get(i))).get(0) +
//    		  "; Access restriction: " + ((Vector)(vRows.get(i))).get(1) +
//    		  "; Blocking reason: " + ((Vector)(vRows.get(i))).get(2));
//      }
      
      // Total users per company

//      map = actionRecordReaderDao.getTotalUsersPerCompany ();
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("Company: " + e.getKey () + " / User count: " +
//            e.getValue ().intValue ());
//      }

      // Total active users
      
      logger.info ("Total active users: " +
         actionRecordReaderDao.getActiveUsers (start, end));
      
      // Total active users per company
      
//      map = actionRecordReaderDao.getActiveUsersPerCompany (start, end);
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("Company: " + e.getKey () +
//            " / Active user count: " + e.getValue ().intValue ());
//      }

      // Total connections
      
      logger.info ("Total connections: " +
         actionRecordReaderDao.getTotalConnections (start, end));

      // Total connections per user

//      map = actionRecordReaderDao.getConnectionsPerUser (start, end);
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("User: " + e.getKey () +
//            " / Connection count: " + e.getValue ().intValue ());
//      }

      // Total connections per company
      
//      map = actionRecordReaderDao.getConnectionsPerCompany (start, end);
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("Company: " + e.getKey () +
//            " / Connection count: " + e.getValue ().intValue ());
//      }

      // Total searches
      
//      logger.info ("Total searches: " +
//         actionRecordReaderDao.getTotalSearches (start, end));

     // Total searches per user

//      map = actionRecordReaderDao.getSearchesPerUser (start, end);
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("User: " + e.getKey () +
//            " / Search count: " + e.getValue ().intValue ());
//      }

      // Total searches per company
      
//      map = actionRecordReaderDao.getSearchesPerCompany (start, end);

//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("Company: " + e.getKey () +
//            " / Search count: " + e.getValue ().intValue ());
//      }

      // Total downloads
      
//      logger.info ("Total downloads: " +
//         actionRecordReaderDao.getTotalDownloads (start, end));

      // Volume downloads
      
      logger.info ("Volume downloads: " +
         actionRecordReaderDao.getVolumeDownloads (start, end));

      // Total downloads per user

//      map = actionRecordReaderDao.getDownloadsPerUser (start, end);
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("User: " + e.getKey () +
//            " / Download count: " + e.getValue ().intValue ());
//      }

      // Volume downloads per user
      
      map = actionRecordReaderDao.getVolumeDownloadsPerUser (start, end);

      for (Entry<String, BigInteger> e : map.entrySet ())
      {
         logger.info ("User: " + e.getKey () +
            " / Download volume: " + e.getValue ().intValue ());
      }

      // Total downloads per company
      
//      map = actionRecordReaderDao.getDownloadsPerCompany (start, end);
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("Company: " + e.getKey () +
//            " / Download count: " + e.getValue ().intValue ());
//      }

      // Total downloads per product
      
//      map = actionRecordReaderDao.getDownloadsPerProduct (start, end);
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("Product: " + e.getKey () +
//            " / Download count: " + e.getValue ().intValue ());
//      }

      // Volume downloads per product
      
      map = actionRecordReaderDao.getVolumeDownloadsPerProduct (start, end);

      for (Entry<String, BigInteger> e : map.entrySet ())
      {
         logger.info ("Product: " + e.getKey () +
            " / Download volume: " + e.getValue ().intValue ());
      }

      // Total succeeded uploads

      logger.info ("Total succeeded uploads: " +
         actionRecordReaderDao.getTotalUploads (start, end,
            ActionRecord.STATUS_SUCCEEDED));

      // Volume succeeded uploads

      logger.info ("Volume succeeded uploads: " +
         actionRecordReaderDao.getVolumeUploads (start, end,
            ActionRecord.STATUS_SUCCEEDED));

      // Total failed uploads

      logger.info ("Total failed uploads: " +
         actionRecordReaderDao.getTotalUploads (start, end,
            ActionRecord.STATUS_FAILED));

      // Total succeeded uploads per user
      
      map = actionRecordReaderDao.getUploadsPerUser (start, end,
            ActionRecord.STATUS_SUCCEEDED);

      for (Entry<String, BigInteger> e : map.entrySet ())
      {
         logger.info ("User: " + e.getKey () +
            " / Succeeded upload count: " +
            e.getValue ().intValue ());
      }

      // Total failed uploads per user

      map = actionRecordReaderDao.getUploadsPerUser (start, end,
            ActionRecord.STATUS_FAILED);

      for (Entry<String, BigInteger> e : map.entrySet ())
      {
         logger.info ("User: " + e.getKey () +
            " / Failed upload count: " + e.getValue ().intValue ());
      }

      // Total succeeded uploads per company
//      
//      map = actionRecordReaderDao.getUploadsPerCompany (start, end,
//            ActionRecord.STATUS_SUCCEEDED);
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("Company: " + e.getKey () +
//            " / Succeeded upload count: " +
//            e.getValue ().intValue ());
//      }

      // Total failed uploads per company
      
//      map = actionRecordReaderDao.getUploadsPerCompany (start, end,
//            ActionRecord.STATUS_FAILED);
//
//      for (Entry<String, BigInteger> e : map.entrySet ())
//      {
//         logger.info ("Company: " + e.getKey () +
//            " / Failed upload count: " + e.getValue ().intValue ());
//      }

      
  }
}
