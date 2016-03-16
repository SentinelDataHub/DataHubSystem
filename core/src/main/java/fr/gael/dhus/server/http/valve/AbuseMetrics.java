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
package fr.gael.dhus.server.http.valve;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.UUID;

public class AbuseMetrics
{
   private Long period;
   private Long calls;
   private Map<String, Long>abusiveUsers;
   private Map<String, Long> abusiveIp;
   private Map<String, Long> abusiveRequests;
   /**
    * @return the period
    */
   public Long getPeriod()
   {
      return period;
   }
   /**
    * @param period the period to set
    */
   public void setPeriod(Long period)
   {
      this.period = period;
   }
   /**
    * @return the calls
    */
   public Long getCalls()
   {
      return calls;
   }
   /**
    * @param calls the calls to set
    */
   public void setCalls(Long calls)
   {
      this.calls = calls;
   }
   /**
    * @return the abusiveUsers
    */
   public Map<String, Long> getAbusiveUsers()
   {
      return abusiveUsers;
   }
   /**
    * @param abusiveUsers the abusiveUsers to set
    */
   public void setAbusiveUsers(Map<String, Long> abusiveUsers)
   {
      this.abusiveUsers = abusiveUsers;
   }
   /**
    * @return the abusiveIp
    */
   public Map<String, Long> getAbusiveIp()
   {
      return abusiveIp;
   }
   /**
    * @param abusiveIp the abusiveIp to set
    */
   public void setAbusiveIp(Map<String, Long> abusiveIp)
   {
      this.abusiveIp = abusiveIp;
   }
   
   public Map<String, Long> getAbusiveRequests()
   {
      return abusiveRequests;
   }
   
   public void setAbusiveRequests(Map<String, Long> abusiveRequests)
   {
      this.abusiveRequests = abusiveRequests;
   }

   private static String UNKNOWN_USER = "@unknown@";
   private static String UNKNOWN_IP="0.0.0.0";
   
   /**
    * Computes Abusive information metrics from collected access information.
    * @param requests access informations
    * @param top number of max values to be retained
    * @return the metrics of abusive accesses.
    */
   static AbuseMetrics computeAbuseMetricsFromAccess (
      Map<UUID,AccessInformation> requests, int skip, int top)
   {
      AbuseMetrics abuse = new AbuseMetrics();
      Map<String, Long> user_map=new TreeMap<String, Long>();
      Map<String, Long> ip_map=new TreeMap<String, Long>();
      Map<String, Long> requests_map=new TreeMap<String, Long>();
      
      SortedSet<Entry<UUID, AccessInformation>> accesses= 
         entriesSortedByValues(requests);
      Iterator<Map.Entry<UUID, AccessInformation>>iterator=accesses.iterator();
      
      long newest=Long.MIN_VALUE;
      long oldest=Long.MAX_VALUE;
      long longest_request = Long.MIN_VALUE;
      long count = 0;
      
      while (iterator.hasNext())
      {
         if (skip-->0) continue;
         
         Map.Entry<UUID, AccessInformation>entry = iterator.next();
         AccessInformation ai = entry.getValue();
         // Case of eviction happened.
         if (ai == null) continue;

         // Keep track of the delay
         Long timestamp = ai.getStartTimestamp();
         long delay = ai.getDurationNs();
         if (timestamp>newest) newest=timestamp;
         if (timestamp<oldest) oldest=timestamp;
         if (longest_request<delay) longest_request=delay; 
         // Keep track of the record number
         count++;

         // Keep track of the user list
         long user_count = 1;
         String username = ai.getUsername(); 
         if (username == null) username=UNKNOWN_USER;
         if (user_map.containsKey(username)) user_count+=user_map.get(username);
         user_map.put(username, user_count);
         
         // Keep track of the Ip List
         long ip_count = 1;
         String ip = ai.getRemoteAddress(); 
         if (ip == null) ip=UNKNOWN_IP;
         if (ip_map.containsKey(ip)) ip_count += ip_map.get(ip);
         ip_map.put(ip, ip_count);
         
         // Keep track of the request time spent.
         String request = ai.getRequest();
         Long old_request=Long.MIN_VALUE;
         if (request == null) continue;
         if (requests_map.containsKey(request))
            old_request = requests_map.get(request);
         if (delay>old_request)
            requests_map.put(request, delay);
      }

      // report the period nanoseconds
      abuse.setPeriod(newest-oldest);
      abuse.setCalls(count);
      
      
      // Sort Abusive users
      SortedSet<Map.Entry<String, Long>> sorted_users_calls = 
               entriesSortedByValues(user_map);
      Iterator<Map.Entry<String, Long>>user_it=sorted_users_calls.iterator();
      abuse.setAbusiveUsers(new LinkedHashMap<String, Long>());
      int top_count = 0;
      while (user_it.hasNext() && top_count++<top)
      {
         Map.Entry<String, Long>entry = user_it.next();
         abuse.getAbusiveUsers().put(entry.getKey(), entry.getValue());
      }
      // Sort Abusive IP usage
      SortedSet<Map.Entry<String, Long>> sorted_ip_calls = 
               entriesSortedByValues(ip_map);
      Iterator<Map.Entry<String, Long>>ip_it=sorted_ip_calls.iterator();
      abuse.setAbusiveIp(new LinkedHashMap<String, Long>());
      top_count = 0;
      while (ip_it.hasNext() && top_count++<top)
      {
         Map.Entry<String, Long>entry = ip_it.next();
         abuse.getAbusiveIp().put(entry.getKey(), entry.getValue());
      }
      
      // Sort Abusive IP usage
      SortedSet<Map.Entry<String, Long>> sorted_requests_calls = 
         entriesSortedByValues(requests_map);
      Iterator<Map.Entry<String, Long>>requests_it=
         sorted_requests_calls.iterator();
      abuse.setAbusiveRequests(new LinkedHashMap<String, Long>());
      top_count = 0;
      while (requests_it.hasNext() && top_count++<top)
      {
         Map.Entry<String, Long>entry = requests_it.next();
         abuse.getAbusiveRequests().put(entry.getKey(), entry.getValue());
      }
      
      return abuse;
   }
   
   static <K,V extends Comparable<? super V>> SortedSet <Map.Entry<K,V>> 
      entriesSortedByValues(Map<K,V> map)
   {
      SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
           new Comparator<Map.Entry<K,V>>()
           {
              @Override
              public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2)
              {
                 int res = e2.getValue().compareTo(e1.getValue());
                 return res != 0 ? res : 1;
              }
           });
      sortedEntries.addAll(map.entrySet());
      return sortedEntries;
   }


   @Override
   public String toString()
   {
      String user_list = "[";
      String separator="";
      for (String user:getAbusiveUsers().keySet())
      {
         user_list += separator + user + ":" + getAbusiveUsers().get(user);
         separator=",";
      }
      user_list+="]";
      
      String ip_list = "[";
      separator="";
      for (String ip:getAbusiveIp().keySet())
      {
         ip_list += separator + ip + ":" + getAbusiveIp().get(ip);
         separator=",";
      }
      ip_list+="]";
      
      String requests_list = "[";
      separator="";
      for (String request:getAbusiveRequests().keySet())
      {
         requests_list += separator + request + ":" + AccessValve.
            twoDigit(getAbusiveRequests().get(request)/1000000000.0) + "s";
         separator=",";
      }
      requests_list+="]";

      return "Calls=" + this.getCalls() + ", period=" + AccessValve.
         twoDigit(getPeriod()/1000000000.0).trim() + "s," + user_list + ", " + 
         ip_list + ", " + requests_list;
   }
}
