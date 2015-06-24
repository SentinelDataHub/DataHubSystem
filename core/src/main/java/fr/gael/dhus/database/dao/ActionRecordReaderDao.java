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
package fr.gael.dhus.database.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import fr.gael.dhus.database.dao.interfaces.HibernateDao;
import fr.gael.dhus.database.object.statistic.ActionRecord;
import fr.gael.dhus.database.object.statistic.ActionRecordDownload;
import fr.gael.dhus.database.object.statistic.ActionRecordLogon;
import fr.gael.dhus.database.object.statistic.ActionRecordSearch;
import fr.gael.dhus.database.object.statistic.ActionRecordUpload;

@Repository
/**
 * Class for computing statistics.
 * This class allows computing:
 * - the total user count (deleted not included) at current time;
 * - the total deleted user count at current time;
 * - the total user with access restrictions count at current time;
 * - the user with access restrictions list with details at current time;
 * - the user count per domain/usage at current time;
 * - the total active user count for a time interval;
 * - the active user count per domain/usage for a time interval;
 * - the total connection count for a time interval;
 * - the connection count per domain/usage for a time interval;
 * - the total search count for a time interval;
 * - the search count per user for a time interval;
 * - the search count per domain/usage for a time interval;
 * - the total download count for a time interval;
 * - the volume of downloads for a time interval;
 * - the download count per user for a time interval;
 * - the volume of downloads per user for a time interval;
 * - the download count per domain/usage for a time interval;
 * - the download count per product for a time interval;
 * - the volume of downloads per product for a time interval;
 * - the total upload count for a time interval;
 * - the volume of uploads for a time interval;
 * - the upload count per user for a time interval;
 * - the upload count per domain/usage for a time interval;
 * 
 */
public class ActionRecordReaderDao extends HibernateDao<ActionRecord, Long>
{
   @Autowired
   private UserDao userDao;
   
   /**
    * Returns the result of a SQL query.
    * 
    * @param sql
    *           The sql string.
    * @param periodicity
    *           A list of two Date or null if not applicable.
    * @return ReturnValue A list of Object[]. Each object tab contains the
    *         result of a row of the SELECT. The list is used for multiple row
    *         results.
    */
   @SuppressWarnings("unchecked")
   private List<Object[]> getReturnValue(final String sql,
         final Date start, final Date end)
   {
      boolean newSession = false;
      Session session;

      try
      {
         session = getSessionFactory ().getCurrentSession ();
      }
      catch (HibernateException e)
      {
         session =  getSessionFactory ().openSession ();
         newSession = true;
      }
      SQLQuery query = session.createSQLQuery (sql);
      if (start != null)
         query.setDate (0, start);
      if (end != null)
         query.setDate (1, end);
      List<Object[]> result = query.list ();

      if (newSession)
         session.disconnect ();
      return result;
   }
   
   private List<Object[]> getReturnValue(final String sql)
   {
      return getReturnValue(sql, null, null);
   }

   /**
    * Returns the result of a SQL query.
    * 
    * @param sql
    *           The sql string.
    * @param periodicity
    *           A list of two Date or null if not applicable.
    * @return number of elements returned by the passed query. Is something is
    *    wrong with the query result, 0 is returned
    */
   private int getCountValue(final String sql, final Date start, final Date end)
   {

      boolean newSession = false;
      Session session;
      try
      {
         session = getSessionFactory ().getCurrentSession ();
      }
      catch (HibernateException e)
      {
         session = getSessionFactory ().openSession ();
         newSession = true;
      }

      SQLQuery query = session.createSQLQuery (sql);
      if (start != null)
       query.setDate (0, start);
      if (end != null)
       query.setDate (1, end);
      BigInteger result = (BigInteger) query.uniqueResult ();

      if (newSession)
         session.disconnect ();

      return result.intValue ();
   }
   
   public int getCountValue(String sql)
   {
      return getCountValue(sql, null, null);
   }

   // Users ==================================================================

   /**
    * Retrieve the user count (deleted ones not included).
    * 
    * @return count The user count.
    */
   public int getTotalUsers()
   {
      final String sql = "SELECT COUNT(DISTINCT USERS.ID) FROM USERS "
            + " WHERE USERS.DELETED = FALSE AND NOT USERS.LOGIN = '"
            + userDao.getPublicDataName () + "'";

      return getCountValue(sql);
   }

   /**
    * Retrieve the deleted user count.
    * 
    * @return count The deleted user count.
    */
   public int getTotalDeletedUsers()
   {
      final String sql = "SELECT COUNT(DISTINCT USERS.ID) FROM USERS "
            + " WHERE USERS.DELETED = TRUE";

      return getCountValue(sql);
   }

   /**
    * Retrieve the user with access restricted count.
    * 
    * @return count The user with access restricted count.
    */
   public int getTotalRestrictedUsers()
   {
      final String sql = "SELECT COUNT(DISTINCT USER_RESTRICTIONS.USER_ID) "
            + " FROM USER_RESTRICTIONS INNER JOIN ACCESS_RESTRICTION "
            + " ON USER_RESTRICTIONS.RESTRICTION_ID = ACCESS_RESTRICTION.ID "
            + " INNER JOIN USERS ON USER_RESTRICTIONS.USER_ID = USERS.ID "
            + " WHERE USERS.DELETED = FALSE";

      return getCountValue(sql);
   }

   /**
    * Retrieve the user name, the access restriction and the blocking reason of
    * users with access restrictions.
    * 
    * @return vRows A vector containing vectors of three elements (user name,
    *         access restriction and blocking reason).
    */
   public String[][] getRestrictedUsers()
   {
      final String sql = "SELECT ACCESS_RESTRICTION.ACCESS_RESTRICTION, "
            + " COUNT(USERS.ID) "
            + " FROM ACCESS_RESTRICTION INNER JOIN USER_RESTRICTIONS "
            + " ON ACCESS_RESTRICTION.ID = USER_RESTRICTIONS.RESTRICTION_ID "
            + " INNER JOIN USERS ON USER_RESTRICTIONS.USER_ID = USERS.ID "
            + " WHERE USERS.DELETED = FALSE GROUP BY ACCESS_RESTRICTION.ACCESS_RESTRICTION";

      List<Object[]> value = getReturnValue(sql);
      
      String[][] array = new String[value.size ()][2];
      int i = 0;
      for (Object[] line : value)
      {         
         array[i] = new String[2];
         array[i][0] = line[0].toString().substring (0,1).toUpperCase ()+
            line[0].toString ().substring (1).toLowerCase () + " ("+line[1].toString()+")";
         array[i][1] = line[1].toString();
         i++;
      }
      return array;
   }

   /*
    * Retrieve the user count grouped by group.
    * 
    * @return map A hashmap containing the group name as key and the user count
    * as value.
    */
   /*
    * @SuppressWarnings ({ "unchecked" }) public HashMap<String, Integer>
    * getTotalUsersPerGroup () { final String sql =
    * "SELECT GROUPS.NAME, COUNT(DISTINCT USERS.ID) FROM GROUPS " +
    * " INNER JOIN USERS ON GROUPS.ID = USERS.USER_GROUPS " +
    * " GROUP BY GROUPS.ID ";
    * 
    * List<Object[]> value = getReturnValue (sql); HashMap map = new HashMap ();
    * 
    * for (Object[] line : value) { map.put ((String) (line[0]), (Integer)
    * (line[1])); } return map; }
    */

   /**
    * Retrieve the user count grouped by domain.
    */
   public String[][] getUsersPerDomain()
   {
      final String sql = 
         "SELECT USERS.domain as domain, COUNT(DISTINCT USERS.ID) " +
            "FROM USERS " +
            "WHERE USERS.DELETED = FALSE AND " +
            "NOT USERS.LOGIN='"+ userDao.getPublicDataName () +"' " +
            "GROUP BY domain";
      
      List<Object[]> value = getReturnValue(sql);  
      HashMap<String, String> values = new HashMap<String, String>();  
      for (Object[] line : value)
      {
         values.put ((String) line[0], line[1].toString());
      }
      
      String[][] res = new String[values.size ()][2];
      int i = 0;
      for (String key : values.keySet ())
      {
         res[i] = new String[2];
         res[i][0] = key + " ("+values.get(key)+")";
         res[i][1] = values.get(key);
         i++;
      }
      return res;
   }

   /**
    * Retrieve the user count grouped by usage.
    */  
   public String[][] getUsersPerUsage()
   {
      final String sql = "SELECT USERS.usage as usage"
            + ", COUNT(DISTINCT USERS.ID) FROM USERS WHERE USERS.DELETED = FALSE AND NOT USERS.LOGIN = '"
            + userDao.getPublicDataName () + "' GROUP BY usage";
      
      List<Object[]> value = getReturnValue(sql);  
      HashMap<String, String> values = new HashMap<String, String>();  
      for (Object[] line : value)
      {
         values.put ((String) line[0], line[1].toString());
      }
      
      String[][] res = new String[values.size ()][2];
      int i = 0;
      for (String key : values.keySet ())
      {
         res[i] = new String[2];
         res[i][0] = key + " ("+values.get(key)+")";
         res[i][1] = values.get(key);
         i++;
      }
      return res;
   }

   /**
    * Retrieve the active user count.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return count The active user count.
    */
   public int getActiveUsers(Date start, Date end)
   {
      final String sql = "SELECT COUNT(DISTINCT ACTION_RECORD_LOGONS.USERS_ID) "
            + " FROM ACTION_RECORD_LOGONS "
            + " WHERE ACTION_RECORD_LOGONS.CREATED BETWEEN ? AND ? "
            + " AND ACTION_RECORD_LOGONS.STATUS = '"
            + ActionRecord.STATUS_SUCCEEDED + "' ";

      return getCountValue(sql, start, end);
   }

   /*
    * Retrieve the active user count grouped by group.
    * 
    * @param periodicity The periodicity (YEAR, MONTH or WEEK)
    * 
    * @return map A hashmap containing the group name as key and the active user
    * count as value.
    */
   /*
    * public HashMap<String, Integer> getActiveUsersPerGroup ( final String
    * periodicity) { final String sql =
    * "SELECT GROUPS.NAME, COUNT(DISTINCT USERS.ID) FROM GROUPS " +
    * " INNER JOIN USERS ON GROUPS.ID = USERS.USER_GROUPS " +
    * " INNER JOIN ACTION_RECORD_LOGONS " +
    * " ON USERS.LOGIN = ACTION_RECORD_LOGONS.USER " +
    * " WHERE ACTION_RECORD_LOGONS.CREATED >= (current_timestamp() - 1 " +
    * periodicity + " AND ACTION_RECORD_LOGONS.STATUS = '" +
    * ActionRecord.STATUS_SUCCEEDED + "' " + " GROUP BY GROUPS.ID ";
    * 
    * List<Object[]> value = getReturnValue (sql); HashMap<String, Integer> map
    * = new HashMap<String, Integer> ();
    * 
    * for (Object[] line : value) { map.put ((String) (line[0]), (Integer)
    * (line[1])); } return map; }
    */

   /**
    * Retrieve the active user count grouped by domain.
    */
   public String[][] getActiveUsersPerDomain(Date start, Date end, boolean perHour)
   {
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_LOGONS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_LOGONS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_LOGONS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_LOGONS.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.domain as domain, "
               + " COUNT(DISTINCT ACTION_RECORD_LOGONS.USERS_ID) FROM USERS "
               + " INNER JOIN ACTION_RECORD_LOGONS "
               + " ON USERS.ID = ACTION_RECORD_LOGONS.USERS_ID "
               + " WHERE ACTION_RECORD_LOGONS.CREATED BETWEEN ? AND ? "
               + " AND ACTION_RECORD_LOGONS.STATUS = '"
               + ActionRecord.STATUS_SUCCEEDED + "' GROUP BY date, domain ORDER BY date, domain";
      
      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> domains = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      domains.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String domain = (String) line[1];         
         if (!domains.contains (domain))
         {
            domains.add (domain);
         }
         int id = domains.indexOf (domain);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][domains.size ()];
      res[0] = domains.toArray (new String[domains.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[domains.size()];
         for (int j = 0; j < domains.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   /**
    * Retrieve the active user count grouped by usage.
    */
   public String[][] getActiveUsersPerUsage(Date start, Date end, boolean perHour)
   {
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_LOGONS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_LOGONS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_LOGONS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_LOGONS.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.usage as usage, "
               + " COUNT(DISTINCT ACTION_RECORD_LOGONS.USERS_ID) FROM USERS "
               + " INNER JOIN ACTION_RECORD_LOGONS "
               + " ON USERS.ID = ACTION_RECORD_LOGONS.USERS_ID "
               + " WHERE ACTION_RECORD_LOGONS.CREATED BETWEEN ? AND ? "
               + " AND ACTION_RECORD_LOGONS.STATUS = '"
               + ActionRecord.STATUS_SUCCEEDED + "' GROUP BY date, usage ORDER BY date, usage";
      
      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> usages = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      usages.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String usage = (String) line[1];         
         if (!usages.contains (usage))
         {
            usages.add (usage);
         }
         int id = usages.indexOf (usage);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][usages.size ()];
      res[0] = usages.toArray (new String[usages.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[usages.size()];
         for (int j = 0; j < usages.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   // Connections ============================================================

   /**
    * Retrieve the connection count.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return count The connection count.
    */
   public int getTotalConnections(Date start, Date end)
   {
      final String sql = "SELECT COUNT(ACTION_RECORD_LOGONS.ID) FROM ACTION_RECORD_LOGONS "
            + " WHERE ACTION_RECORD_LOGONS.CREATED BETWEEN ? AND ? "
            + " AND ACTION_RECORD_LOGONS.STATUS = '"
            + ActionRecord.STATUS_SUCCEEDED + "' ";

      return getCountValue(sql, start, end);
   }

   /**
    * Retrieve the connection count grouped by user.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return map A hashmap containing the user name as key and the connection
    *         count as value.
    */
   public String[][] getConnectionsPerUser(Date start, Date end, List<String> requestedUsers, boolean perHour)
   {
      String usersStr = "(null)";
      String legend = "Total";
      if (requestedUsers != null && !requestedUsers.isEmpty ())
      {
         legend = "Others";
         usersStr = "(";
         for (String user : requestedUsers)
         {
            usersStr += "'"+user+"',";
         }
         usersStr = usersStr.substring (0,usersStr.length ()-1);
         usersStr += ")";
      }
      String userId = "(case when USERS.LOGIN in "+usersStr+" then USERS.LOGIN else '"+legend+"' end)";
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_LOGONS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_LOGONS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_LOGONS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_LOGONS.CREATED),2),':00:00'":"")+")";
      
      final String sql = "SELECT "+dateSQL+" as date,"
            + " "+userId+" as userId, "
            + " COUNT(ACTION_RECORD_LOGONS.ID) FROM USERS "
            + " INNER JOIN ACTION_RECORD_LOGONS "
            + " ON USERS.ID = ACTION_RECORD_LOGONS.USERS_ID "
            + " WHERE ACTION_RECORD_LOGONS.CREATED BETWEEN ? AND ? "
            + " AND ACTION_RECORD_LOGONS.STATUS = '"
            + ActionRecord.STATUS_SUCCEEDED + "' GROUP BY date, userId ORDER BY date, userId";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> foundUsers = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      foundUsers.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         if (!foundUsers.contains (line[1]))
         {
            foundUsers.add ((String) line[1]);
         }
         int id = foundUsers.indexOf (line[1]);
         String date = line[0].toString();
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][foundUsers.size ()];
      res[0] = foundUsers.toArray (new String[foundUsers.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[foundUsers.size()];
         for (int j = 0; j < foundUsers.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   /*
    * Retrieve the connection count grouped by group.
    * 
    * @param periodicity The periodicity (YEAR, MONTH or WEEK)
    * 
    * @return map A hashmap containing the group name as key and the connection
    * count as value.
    */
   /*
    * public HashMap<String, Integer> getConnectionsPerGroup ( final String
    * periodicity) { final String sql =
    * "SELECT GROUPS.NAME, COUNT(ACTION_RECORD_LOGONS.ID) " +
    * " FROM GROUPS INNER JOIN USERS ON GROUPS.ID = USERS.USER_GROUPS " +
    * " INNER JOIN ACTION_RECORD_LOGONS " +
    * " ON USERS.LOGIN = ACTION_RECORD_LOGONS.USER " +
    * " WHERE ACTION_RECORD_LOGONS.CREATED >= (current_timestamp() - 1 " +
    * periodicity + " AND ACTION_RECORD_LOGONS.STATUS = '" +
    * ActionRecord.STATUS_SUCCEEDED + "' GROUP BY GROUPS.ID ";
    * 
    * List<Object[]> value = getReturnValue (sql); HashMap<String, Integer> map
    * = new HashMap<String, Integer> ();
    * 
    * for (Object[] line : value) { map.put ((String) (line[0]), (Integer)
    * (line[1])); } return map; }
    */

   /**
    * Retrieve the connection count grouped by domain.
    */
   public String[][] getConnectionsPerDomain(Date start, Date end, boolean perHour)
   {      
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_LOGONS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_LOGONS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_LOGONS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_LOGONS.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.domain as domain, "
               + " COUNT(ACTION_RECORD_LOGONS.ID) FROM USERS "
               + " INNER JOIN ACTION_RECORD_LOGONS "
               + " ON USERS.ID = ACTION_RECORD_LOGONS.USERS_ID "
               + " WHERE ACTION_RECORD_LOGONS.CREATED BETWEEN ? AND ? "
               + " AND ACTION_RECORD_LOGONS.STATUS = '"
               + ActionRecord.STATUS_SUCCEEDED + "' GROUP BY date, domain ORDER BY date, domain";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> domains = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      domains.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String domain = (String) line[1];
         if (!domains.contains (domain))
         {
            domains.add (domain);
         }
         int id = domains.indexOf (domain);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][domains.size ()];
      res[0] = domains.toArray (new String[domains.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[domains.size()];
         for (int j = 0; j < domains.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   /**
    * Retrieve the connection count grouped by usage.
    */
   public String[][] getConnectionsPerUsage(Date start, Date end, boolean perHour)
   {      
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_LOGONS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_LOGONS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_LOGONS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_LOGONS.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.usage as usage, "
               + " COUNT(ACTION_RECORD_LOGONS.ID) FROM USERS "
               + " INNER JOIN ACTION_RECORD_LOGONS "
               + " ON USERS.ID = ACTION_RECORD_LOGONS.USERS_ID "
               + " WHERE ACTION_RECORD_LOGONS.CREATED BETWEEN ? AND ? "
               + " AND ACTION_RECORD_LOGONS.STATUS = '"
               + ActionRecord.STATUS_SUCCEEDED + "' GROUP BY date, usage ORDER BY date, usage";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> usages = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      usages.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String usage = (String) line[1];
         if (!usages.contains (usage))
         {
            usages.add (usage);
         }
         int id = usages.indexOf (usage);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][usages.size ()];
      res[0] = usages.toArray (new String[usages.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[usages.size()];
         for (int j = 0; j < usages.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   // Searches ===============================================================

   /**
    * Retrieve the search count.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return count The search count.
    */
   public int getTotalSearches()
   {
      final String sql = "SELECT COUNT(ACTION_RECORD_SEARCHES.ID) "
            + " FROM ACTION_RECORD_SEARCHES";

      return getCountValue(sql);
   }

   /**
    * Retrieve the search count grouped by user.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return map A hashmap containing the user name as key and the search count
    *         as value.
    */
   public String[][] getSearchesPerUser(
         Date start, Date end, List<String> requestedUsers, boolean perHour)
      {
      String usersStr = "(null)";
      String legend = "Total";
      if (requestedUsers != null && !requestedUsers.isEmpty ())
      {
         legend = "Others";
         usersStr = "(";
         for (String user : requestedUsers)
         {
            usersStr += "'"+user+"',";
         }
         usersStr = usersStr.substring (0,usersStr.length ()-1);
         usersStr += ")";
      }
      String userId = "(case when USERS.LOGIN in "+usersStr+" then USERS.LOGIN else '"+legend+"' end)";
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_SEARCHES.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_SEARCHES.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_SEARCHES.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_SEARCHES.CREATED),2),':00:00'":"")+")";
      
      final String sql = "SELECT "+dateSQL+" as date,"
            + " "+userId+" as userId, "
            + " COUNT(ACTION_RECORD_SEARCHES.ID) FROM USERS "
            + " INNER JOIN ACTION_RECORD_SEARCHES "
            + " ON USERS.ID = ACTION_RECORD_SEARCHES.USERS_ID "
            + " WHERE ACTION_RECORD_SEARCHES.CREATED BETWEEN ? AND ? "
            + " GROUP BY date, userId ORDER BY date, userId";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> foundUsers = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      foundUsers.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         if (!foundUsers.contains (line[1]))
         {
            foundUsers.add ((String) line[1]);
         }
         int id = foundUsers.indexOf (line[1]);
         String date = line[0].toString();
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][foundUsers.size ()];
      res[0] = foundUsers.toArray (new String[foundUsers.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[foundUsers.size()];
         for (int j = 0; j < foundUsers.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   /*
    * Retrieve the search count grouped by group.
    * 
    * @param periodicity The periodicity (YEAR, MONTH or WEEK)
    * 
    * @return map A hashmap containing the group name as key and the search
    * count as value.
    */
   /*
    * public HashMap<String, Integer> getSearchesPerGroup (final String
    * periodicity) { final String sql =
    * "SELECT GROUPS.NAME, COUNT(ACTION_RECORD_SEARCHES.ID) " +
    * " FROM GROUPS INNER JOIN USERS ON GROUPS.ID = USERS.USER_GROUPS " +
    * " INNER JOIN ACTION_RECORD_SEARCHES " +
    * " ON USERS.LOGIN = ACTION_RECORD_SEARCHES.USER " +
    * " WHERE ACTION_RECORD_SEARCHES.CREATED >= (current_timestamp() - 1 " +
    * periodicity + " GROUP BY GROUPS.ID ";
    * 
    * List<Object[]> value = getReturnValue (sql); HashMap<String, Integer> map
    * = new HashMap<String, Integer> ();
    * 
    * for (Object[] line : value) { map.put ((String) (line[0]), (Integer)
    * (line[1])); } return map; }
    */

   /**
    * Retrieve the search count grouped by domain.
    */
   public String[][] getSearchesPerDomain(Date start, Date end, boolean perHour)
   {      
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_SEARCHES.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_SEARCHES.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_SEARCHES.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_SEARCHES.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.domain as domain, "
               + " COUNT(ACTION_RECORD_SEARCHES.ID) FROM USERS "
               + " INNER JOIN ACTION_RECORD_SEARCHES "
               + " ON USERS.ID = ACTION_RECORD_SEARCHES.USERS_ID "
               + " WHERE ACTION_RECORD_SEARCHES.CREATED BETWEEN ? AND ? "
               + " GROUP BY date, domain ORDER BY date, domain";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> domains = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      domains.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String domain = (String) line[1];
         if (!domains.contains (domain))
         {
            domains.add (domain);
         }
         int id = domains.indexOf (domain);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][domains.size ()];
      res[0] = domains.toArray (new String[domains.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[domains.size()];
         for (int j = 0; j < domains.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   /**
    * Retrieve the search count grouped by usage.
    */
   public String[][] getSearchesPerUsage(Date start, Date end, boolean perHour)
   {      
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_SEARCHES.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_SEARCHES.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_SEARCHES.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_SEARCHES.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.usage as usage, "
               + " COUNT(ACTION_RECORD_SEARCHES.ID) FROM USERS "
               + " INNER JOIN ACTION_RECORD_SEARCHES "
               + " ON USERS.ID = ACTION_RECORD_SEARCHES.USERS_ID "
               + " WHERE ACTION_RECORD_SEARCHES.CREATED BETWEEN ? AND ? "
               + " GROUP BY date, usage ORDER BY date, usage";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> usages = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      usages.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String usage = (String) line[1];
         if (!usages.contains (usage))
         {
            usages.add (usage);
         }
         int id = usages.indexOf (usage);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][usages.size ()];
      res[0] = usages.toArray (new String[usages.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[usages.size()];
         for (int j = 0; j < usages.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   // Downloads ==============================================================

   /**
    * Retrieve the download count.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return count The download count.
    */
   public int getTotalDownloads()
   {
      final String sql = "SELECT COUNT (ACTION_RECORD_DOWNLOADS.ID) "
            + " FROM ACTION_RECORD_DOWNLOADS WHERE STATUS='SUCCEEDED'";

      return getCountValue(sql);
   }

   /**
    * Retrieve the download volume.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return volume The download volume (in bytes TBC).
    */
   public int getVolumeDownloads(Date start, Date end)
   {
      final String sql = "SELECT SUM(ACTION_RECORD_DOWNLOADS.PRODUCT_SIZE) "
            + " FROM ACTION_RECORD_DOWNLOADS "
            + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? ";

      return getCountValue(sql, start, end);
   }

   /**
    * Retrieve the download count grouped by user.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return map A hashmap containing the user name as key and the download
    *         count as value.
    */
   public String[][] getDownloadsPerUser(
            Date start, Date end, List<String> requestedUsers, boolean perHour)
         {
         String usersStr = "(null)";
         String legend = "Total";
         if (requestedUsers != null && !requestedUsers.isEmpty ())
         {
            legend = "Others";
            usersStr = "(";
            for (String user : requestedUsers)
            {
               usersStr += "'"+user+"',";
            }
            usersStr = usersStr.substring (0,usersStr.length ()-1);
            usersStr += ")";
         }
         String userId = "(case when USERS.LOGIN in "+usersStr+" then USERS.LOGIN else '"+legend+"' end)";
         String dateSQL = "CONCAT(YEAR(ACTION_RECORD_DOWNLOADS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_DOWNLOADS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_DOWNLOADS.CREATED),2)"
            + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_DOWNLOADS.CREATED),2),':00:00'":"")+")";
         
         final String sql = "SELECT "+dateSQL+" as date,"
               + " "+userId+" as userId, "
               + " COUNT(ACTION_RECORD_DOWNLOADS.ID) FROM USERS "
               + " INNER JOIN ACTION_RECORD_DOWNLOADS "
               + " ON USERS.ID = ACTION_RECORD_DOWNLOADS.USERS_ID "
               + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
               + "    AND STATUS='SUCCEEDED'"
               + " GROUP BY date, userId ORDER BY date, userId";
         
         List<Object[]> value = getReturnValue(sql, start, end);
         List<String> foundUsers = new ArrayList<String>();  
         List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
         foundUsers.add ("date");
         String previousDate = "";
         HashMap<Integer, String> counts = null;
         for (Object[] line : value)
         {
            if (!foundUsers.contains (line[1]))
            {
               foundUsers.add ((String) line[1]);
            }
            int id = foundUsers.indexOf (line[1]);
            String date = line[0].toString();
            if (!previousDate.equals(date))
            {
               if (counts != null)
               {
                  dates.add(counts);
               }
               counts = new HashMap<Integer, String> ();
               counts.put (0, date);
               previousDate = date;
            }
            counts.put (id,line[2].toString ());
         }
         if (counts != null)
         {
            dates.add(counts);
         }
         String[][] res = new String[dates.size ()+1][foundUsers.size ()];
         res[0] = foundUsers.toArray (new String[foundUsers.size()]);
         int i = 1;
         for (HashMap<Integer, String> list : dates)
         {
            res[i] = new String[foundUsers.size()];
            for (int j = 0; j < foundUsers.size(); j++)
            {            
               res[i][j] = list.containsKey (j) ? list.get(j) : "0";
            }
            i++;
         }
         return res;      
   }
   
   public String[][] getDownloadsSizePerUser(
      Date start, Date end, List<String> requestedUsers, boolean perHour)
   {
      String usersStr = "(null)";
      String legend = "Total";
      if (requestedUsers != null && !requestedUsers.isEmpty ())
      {
         legend = "Others";
         usersStr = "(";
         for (String user : requestedUsers)
         {
            usersStr += "'"+user+"',";
         }
         usersStr = usersStr.substring (0,usersStr.length ()-1);
         usersStr += ")";
      }
      String userId = "(case when USERS.LOGIN in "+usersStr+" then USERS.LOGIN else '"+legend+"' end)";
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_DOWNLOADS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_DOWNLOADS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_DOWNLOADS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_DOWNLOADS.CREATED),2),':00:00'":"")+")";
      
      final String sql = "SELECT "+dateSQL+" as date,"
            + " "+userId+" as userId, "
            + " SUM(ACTION_RECORD_DOWNLOADS.PRODUCT_SIZE) FROM USERS "
            + " INNER JOIN ACTION_RECORD_DOWNLOADS "
            + " ON USERS.ID = ACTION_RECORD_DOWNLOADS.USERS_ID "
            + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
            + "    AND STATUS='SUCCEEDED'"
            + " GROUP BY date, userId ORDER BY date, userId";
      
      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> foundUsers = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      foundUsers.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         if (!foundUsers.contains (line[1]))
         {
            foundUsers.add ((String) line[1]);
         }
         int id = foundUsers.indexOf (line[1]);
         String date = line[0].toString();
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][foundUsers.size ()];
      res[0] = foundUsers.toArray (new String[foundUsers.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[foundUsers.size()];
         for (int j = 0; j < foundUsers.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;      
   }

   /**
    * Retrieve the download volume grouped by user.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return map A hashmap containing the user name as key and the download
    *         volume as value.
    */
   public HashMap<String, BigInteger> getVolumeDownloadsPerUser(
         Date start, Date end)
   {
      final String sql = "SELECT USERS.LOGIN, SUM(ACTION_RECORD_DOWNLOADS.PRODUCT_SIZE) "
            + " FROM USERS "
            + " INNER JOIN ACTION_RECORD_DOWNLOADS "
            + " ON USERS.ID = ACTION_RECORD_DOWNLOADS.USERS_ID "
            + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
            + "    AND STATUS='SUCCEEDED'"
            + " GROUP BY USERS.LOGIN ";

      List<Object[]> value = getReturnValue(sql, start, end);
      HashMap<String, BigInteger> map = new HashMap<String, BigInteger>();

      for (Object[] line : value)
      {
         map.put((String) (line[0]), ((BigDecimal) (line[1])).toBigInteger());
      }
      return map;
   }

   /*
    * Retrieve the download count grouped by group.
    * 
    * @param periodicity The periodicity (YEAR, MONTH or WEEK)
    * 
    * @return map A hashmap containing the group name as key and the download
    * count as value.
    */
   /*
    * public HashMap<String, Integer> getDownloadsPerGroup ( final String
    * periodicity) { final String sql =
    * "SELECT GROUPS.NAME, COUNT(ACTION_RECORD_DOWNLOADS.ID) " +
    * " FROM GROUPS INNER JOIN USERS " + " ON GROUPS.ID = USERS.USER_GROUPS " +
    * " INNER JOIN ACTION_RECORD_DOWNLOADS " +
    * " ON USERS.LOGIN = ACTION_RECORD_DOWNLOADS.USER "
    * " WHERE ACTION_RECORD_DOWNLOADS.CREATED >= (current_timestamp() - 1 " +
    * periodicity + " GROUP BY GROUPS.NAME ";
    * 
    * List<Object[]> value = getReturnValue (sql); HashMap<String, Integer> map
    * = new HashMap<String, Integer> ();
    * 
    * for (Object[] line : value) { map.put ((String) (line[0]), (Integer)
    * (line[1])); } return map; }
    */

   /**
    * Retrieve the download count grouped by domain.
    */
   public String[][] getDownloadsPerDomain(Date start, Date end, boolean perHour)
   {      
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_DOWNLOADS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_DOWNLOADS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_DOWNLOADS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_DOWNLOADS.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.domain as domain, "
               + " COUNT(ACTION_RECORD_DOWNLOADS.ID) FROM USERS "
               + " INNER JOIN ACTION_RECORD_DOWNLOADS "
               + " ON USERS.ID = ACTION_RECORD_DOWNLOADS.USERS_ID "
               + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
               + "    AND STATUS='SUCCEEDED'"
               + " GROUP BY date, domain ORDER BY date, domain";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> domains = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      domains.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String domain = (String) line[1];
         if (!domains.contains (domain))
         {
            domains.add (domain);
         }
         int id = domains.indexOf (domain);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][domains.size ()];
      res[0] = domains.toArray (new String[domains.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[domains.size()];
         for (int j = 0; j < domains.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }
   
   public String[][] getDownloadsSizePerDomain(Date start, Date end, boolean perHour)
   {      
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_DOWNLOADS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_DOWNLOADS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_DOWNLOADS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_DOWNLOADS.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.domain as domain, "
               + " SUM(ACTION_RECORD_DOWNLOADS.PRODUCT_SIZE) FROM USERS "
               + " INNER JOIN ACTION_RECORD_DOWNLOADS "
               + " ON USERS.ID = ACTION_RECORD_DOWNLOADS.USERS_ID "
               + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
               + "    AND STATUS='SUCCEEDED'"
               + " GROUP BY date, domain ORDER BY date, domain";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> domains = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      domains.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String domain = (String) line[1];
         if (!domains.contains (domain))
         {
            domains.add (domain);
         }
         int id = domains.indexOf (domain);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][domains.size ()];
      res[0] = domains.toArray (new String[domains.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[domains.size()];
         for (int j = 0; j < domains.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   /**
    * Retrieve the download count grouped by usage.
    */
   public String[][] getDownloadsPerUsage(Date start, Date end, boolean perHour)
   {      
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_DOWNLOADS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_DOWNLOADS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_DOWNLOADS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_DOWNLOADS.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.usage as usage, "
               + " COUNT(ACTION_RECORD_DOWNLOADS.ID) FROM USERS "
               + " INNER JOIN ACTION_RECORD_DOWNLOADS "
               + " ON USERS.ID = ACTION_RECORD_DOWNLOADS.USERS_ID "
               + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
               + "    AND STATUS='SUCCEEDED'"
               + " GROUP BY date, usage ORDER BY date, usage";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> usages = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      usages.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String usage = (String) line[1];
         if (!usages.contains (usage))
         {
            usages.add (usage);
         }
         int id = usages.indexOf (usage);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][usages.size ()];
      res[0] = usages.toArray (new String[usages.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[usages.size()];
         for (int j = 0; j < usages.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }
   
   public String[][] getDownloadsSizePerUsage(Date start, Date end, boolean perHour)
   {      
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_DOWNLOADS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_DOWNLOADS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_DOWNLOADS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_DOWNLOADS.CREATED),2),':00:00'":"")+")";
            
      final String sql = "SELECT "+dateSQL+" as date, USERS.usage as usage, "
               + " SUM(ACTION_RECORD_DOWNLOADS.PRODUCT_SIZE) FROM USERS "
               + " INNER JOIN ACTION_RECORD_DOWNLOADS "
               + " ON USERS.ID = ACTION_RECORD_DOWNLOADS.USERS_ID "
               + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
               + "    AND STATUS='SUCCEEDED'"
               + " GROUP BY date, usage ORDER BY date, usage";

      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> usages = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      usages.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         String usage = (String) line[1];
         if (!usages.contains (usage))
         {
            usages.add (usage);
         }
         int id = usages.indexOf (usage);
         String date = (String) line[0];
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][usages.size ()];
      res[0] = usages.toArray (new String[usages.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[usages.size()];
         for (int j = 0; j < usages.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;
   }

   /**
    * Retrieve the download count grouped by product.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return map A hashmap containing the product identifier as key and the
    *         download count as value.
    */
   public String[][] getDownloadsPerProduct(
         Date start, Date end, List<Long> requestProducts, boolean perHour)
   {
      String productStr = "(null)";
      String legend = "Total";
      if (requestProducts != null && !requestProducts.isEmpty ())
      {
         legend = "Others";
         productStr = "(";
         for (Long product : requestProducts)
         {
            productStr += "'"+product+"',";
         }
         productStr = productStr.substring (0,productStr.length ()-1);
         productStr += ")";
      }
      String userId = "(case when USERS.LOGIN in "+productStr+" then USERS.LOGIN else '"+legend+"' end)";
      String dateSQL = "CONCAT(YEAR(ACTION_RECORD_DOWNLOADS.CREATED),'-',RIGHT('00'+MONTH(ACTION_RECORD_DOWNLOADS.CREATED),2),'-',RIGHT('00'+DAY(ACTION_RECORD_DOWNLOADS.CREATED),2)"
         + (perHour ? ",'T',RIGHT('00'+HOUR(ACTION_RECORD_DOWNLOADS.CREATED),2),':00:00'":"")+")";
      
      final String sql = "SELECT "+dateSQL+" as date,"
            + " "+userId+" as userId, "
            + " COUNT(ACTION_RECORD_DOWNLOADS.ID) FROM USERS "
            + " INNER JOIN ACTION_RECORD_DOWNLOADS "
            + " ON USERS.ID = ACTION_RECORD_DOWNLOADS.USERS_ID "
            + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
            + "    AND STATUS='SUCCEEDED'"
            + " GROUP BY date, userId ORDER BY date, userId";
//      final String sql = "SELECT ACTION_RECORD_DOWNLOADS.PRODUCT_IDENTIFIER, "
//               + " COUNT(DISTINCT ACTION_RECORD_DOWNLOADS.ID) "
//               + " FROM ACTION_RECORD_DOWNLOADS "
//               + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
//               + " GROUP BY ACTION_RECORD_DOWNLOADS.PRODUCT_IDENTIFIER ";
      
      List<Object[]> value = getReturnValue(sql, start, end);
      List<String> foundUsers = new ArrayList<String>();  
      List<HashMap<Integer, String>> dates = new ArrayList<HashMap<Integer, String>>();  
      foundUsers.add ("date");
      String previousDate = "";
      HashMap<Integer, String> counts = null;
      for (Object[] line : value)
      {
         if (!foundUsers.contains (line[1]))
         {
            foundUsers.add ((String) line[1]);
         }
         int id = foundUsers.indexOf (line[1]);
         String date = line[0].toString();
         if (!previousDate.equals(date))
         {
            if (counts != null)
            {
               dates.add(counts);
            }
            counts = new HashMap<Integer, String> ();
            counts.put (0, date);
            previousDate = date;
         }
         counts.put (id,line[2].toString ());
      }
      if (counts != null)
      {
         dates.add(counts);
      }
      String[][] res = new String[dates.size ()+1][foundUsers.size ()];
      res[0] = foundUsers.toArray (new String[foundUsers.size()]);
      int i = 1;
      for (HashMap<Integer, String> list : dates)
      {
         res[i] = new String[foundUsers.size()];
         for (int j = 0; j < foundUsers.size(); j++)
         {            
            res[i][j] = list.containsKey (j) ? list.get(j) : "0";
         }
         i++;
      }
      return res;      
   }
   /**
    * Retrieve the download volume grouped by product.
    * 
    * @param periodicity
    *           A list of two Date.
    * @return map A hashmap containing the product identifier as key and the
    *         download volume as value.
    */
   public HashMap<String, BigInteger> getVolumeDownloadsPerProduct(
         Date start, Date end)
   {
      final String sql = "SELECT ACTION_RECORD_DOWNLOADS.PRODUCT_IDENTIFIER, "
            + " SUM(ACTION_RECORD_DOWNLOADS.PRODUCT_SIZE) "
            + " FROM ACTION_RECORD_DOWNLOADS "
            + " WHERE ACTION_RECORD_DOWNLOADS.CREATED BETWEEN ? AND ? "
            + "    AND STATUS='SUCCEEDED'"
            + " GROUP BY ACTION_RECORD_DOWNLOADS.PRODUCT_IDENTIFIER ";

      List<Object[]> value = getReturnValue(sql, start, end);
      HashMap<String, BigInteger> map = new HashMap<String, BigInteger>();

      for (Object[] line : value)
      {
         map.put((String) (line[0]), ((BigDecimal) (line[1])).toBigInteger());
      }
      return map;
   }

   // Uploads ================================================================

   /**
    * Retrieve the upload count.
    * 
    * @param periodicity
    *           The periodicity (YEAR, MONTH or WEEK)
    * @param status
    *           The status (STARTED, SUCCEEDED, FAILED)
    * @return count The upload count.
    */
   public int getTotalUploads(Date start, Date end, String status)
   {
      final String sql = "SELECT COUNT(ACTION_RECORD_UPLOADS.ID) "
            + " FROM ACTION_RECORD_UPLOADS "
            + " WHERE ACTION_RECORD_UPLOADS.STATUS = '" + status + "' "
            + " AND ACTION_RECORD_UPLOADS.CREATED BETWEEN ? AND ? ";

      return getCountValue(sql, start, end);
   }

   /**
    * Retrieve the upload volume.
    * 
    * @param periodicity
    *           A list of two Date.
    * @param status
    *           The status (STARTED, SUCCEEDED, FAILED)
    * @return count The upload volume.
    */
   public int getVolumeUploads(Date start, Date end, String status)
   {
      final String sql = "SELECT SUM(ACTION_RECORD_UPLOADS.PRODUCT_SIZE) "
            + " FROM ACTION_RECORD_UPLOADS "
            + " WHERE ACTION_RECORD_UPLOADS.STATUS = '" + status + "' "
            + " AND ACTION_RECORD_UPLOADS.CREATED BETWEEN ? AND ? ";

      return getCountValue(sql, start, end);
   }

   /**
    * Retrieve the upload count grouped by user.
    * 
    * @param periodicity
    *           A list of two Date.
    * @param status
    *           The status (STARTED, SUCCEEDED, FAILED)
    * @return map A hashmap containing the user name as key and the upload count
    *         as value.
    */
   public HashMap<String, BigInteger> getUploadsPerUser(
         Date start, Date end, String status)
   {
      final String sql = "SELECT USERS.LOGIN, "
            + " COUNT(ACTION_RECORD_UPLOADS.ID) FROM USERS "
            + " INNER JOIN ACTION_RECORD_UPLOADS "
            + " ON USERS.ID = ACTION_RECORD_UPLOADS.USERS_ID "
            + " WHERE ACTION_RECORD_UPLOADS.STATUS = '" + status + "' "
            + " AND ACTION_RECORD_UPLOADS.CREATED BETWEEN ? AND ? "
            + " GROUP BY USERS.LOGIN ";

      List<Object[]> value = getReturnValue(sql, start, end);
      HashMap<String, BigInteger> map = new HashMap<String, BigInteger>();

      for (Object[] line : value)
      {
         map.put((String) (line[0]), (BigInteger) (line[1]));
      }
      return map;
   }

   /*
    * Retrieve the upload count grouped by group.
    * 
    * @param periodicity The periodicity (YEAR, MONTH or WEEK)
    * 
    * @param status The status (STARTED, SUCCEEDED, FAILED)
    * 
    * @return map A hashmap containing the group name as key and the upload
    * count as value.
    */
   /*
    * public HashMap<String, Integer> getUploadsPerGroup ( final String
    * periodicity, String status) { final String sql =
    * "SELECT GROUPS.NAME, COUNT(ACTION_RECORD_UPLOADS.ID) FROM GROUPS " +
    * " INNER JOIN USERS ON GROUPS.ID = USERS.USER_GROUPS " +
    * " INNER JOIN ACTION_RECORD_UPLOADS " +
    * " ON USERS.LOGIN = ACTION_RECORD_UPLOADS.USER " +
    * " WHERE ACTION_RECORD_UPLOADS.STATUS = '" + status + "' " +
    * " AND ACTION_RECORD_UPLOADS.CREATED >= (current_timestamp() - 1 " +
    * periodicity + " GROUP BY GROUPS.NAME ";
    * 
    * List<Object[]> value = getReturnValue (sql); HashMap<String, Integer> map
    * = new HashMap<String, Integer> ();
    * 
    * for (Object[] line : value) { map.put ((String) (line[0]), (Integer)
    * (line[1])); } return map; }
    */

   /**
    * Retrieve the upload count grouped by usage.
    */
   public HashMap<String, BigInteger> getUploadsPerUsage(
         Date start, Date end, String status)
   {
      final String sql = "SELECT USERS.usage, COUNT(DISTINCT ACTION_RECORD_UPLOADS.USERS_ID) "
            + " FROM USERS INNER JOIN ACTION_RECORD_UPLOADS "
            + " ON USERS.ID = ACTION_RECORD_UPLOADS.USERS_ID "
            + " WHERE ACTION_RECORD_UPLOADS.STATUS = '"
            + status
            + "' "
            + " AND ACTION_RECORD_UPLOADS.CREATED BETWEEN ? AND ? "
            + " GROUP BY USERS.usage ";

      List<Object[]> value = getReturnValue(sql, start, end);
      HashMap<String, BigInteger> map = new HashMap<String, BigInteger>();

      for (Object[] line : value)
      {
         map.put((String) (line[0]), (BigInteger) (line[1]));
      }
      return map;
   }

   /**
    * Retrieve the upload count grouped by domain.
    */
   public HashMap<String, BigInteger> getUploadsPerDomain(
         Date start, Date end, String status)
   {
      final String sql = "SELECT USERS.domain, COUNT(DISTINCT ACTION_RECORD_UPLOADS.USERS_ID) "
            + " FROM USERS INNER JOIN ACTION_RECORD_UPLOADS "
            + " ON USERS.ID = ACTION_RECORD_UPLOADS.USERS_ID "
            + " WHERE ACTION_RECORD_UPLOADS.STATUS = '"
            + status
            + "' "
            + " AND ACTION_RECORD_UPLOADS.CREATED BETWEEN ? AND ? "
            + " GROUP BY USERS.domain ";

      List<Object[]> value = getReturnValue(sql, start, end);
      HashMap<String, BigInteger> map = new HashMap<String, BigInteger>();

      for (Object[] line : value)
      {
         map.put((String) (line[0]), (BigInteger) (line[1]));
      }
      return map;
   }
   
   public int countDownloads ()
   {
      return DataAccessUtils.intResult (find (
         "select count(*) FROM " + ActionRecordDownload.class.getName ()));
   }
   
   public int countLogons ()
   {
      return DataAccessUtils.intResult (find (
         "select count(*) FROM " + ActionRecordLogon.class.getName ()));
   }

   public int countSearches ()
   {
      return DataAccessUtils.intResult (find (
         "select count(*) FROM " + ActionRecordSearch.class.getName ()));
   }
   public int countUploads()
   {
      return DataAccessUtils.intResult (find (
         "select count(*) FROM " + ActionRecordUpload.class.getName ()));
   }
}
