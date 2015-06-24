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
package fr.gael.dhus.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parse Solr query class
 * This class was initially designed to Wrap Standard Query parser used by Solr.
 *
 */
public class SolrQueryParser
{
   private static Log logger = LogFactory.getLog (SolrQueryParser.class);
   
   public final static int INDEX_ALL     = 0;
   public final static int INDEX_FOOTER  = 1;
   public final static int INDEX_FIELD   = 2;
   public final static int INDEX_VALUE   = 3;
   public final static int INDEX_TRAILER = 4;
   
   
   private static String pfooter     = "([+-]?)";
   private static String pfield      = "(?:([\\w+\\*\\?]+):)?"; 
   private static String pkey_value  = "[\\w\\*\\?@/<>\\-\\.]+";
   private static String pkey_range  = "[\\[\\{][[\\S ]&&[^\\]^\\}]]+[\\]\\}]";
   private static String pkey_block  = "\\([[\\S ]&&[^\\)]]+\\)]";
   private static String pkey_string = "\"[[\\S ]&&[^\"]]+\"";
   
   private static String pkey = "(" +
      pkey_value     + "|" +
      pkey_range     + "|" +
      pkey_block     + "|" +
      pkey_string    + ")";
   
   private static String ptrail = "((?:\\~|\\^)[\\d\\.]*)?";

   private final static Pattern pattern = Pattern.compile(pfooter+pfield+pkey+ptrail);
   
   public static List<String[]>parse (String query)
   {
      logger.debug ("Matching query : \"" + query + "\"");
      
      List<String[]>list = new ArrayList<String[]>();
      Matcher matcher = pattern.matcher(query.trim());
      
      while (matcher.find())
      {
         String[] strs = new String[5];
         strs[INDEX_ALL]     = neverNull(matcher.group(0)).trim();
         strs[INDEX_FOOTER]  = neverNull(matcher.group(1)).trim();
         strs[INDEX_FIELD]   = neverNull(matcher.group(2)).trim();
         strs[INDEX_VALUE]   = neverNull(matcher.group(3)).trim();
         strs[INDEX_TRAILER] = neverNull(matcher.group(4)).trim();
         list.add(strs);
         
         if (logger.isDebugEnabled())
         {
            logger.debug ("   \"" + strs[INDEX_ALL] + "\" is descabled as");
            logger.debug ("      Footer  \"" + strs[INDEX_FOOTER]+ "\"");
            logger.debug ("      Field   \"" + strs[INDEX_FIELD]+ "\"");
            logger.debug ("      Value   \"" + strs[INDEX_VALUE]+ "\"");
            logger.debug ("      Trailer \"" + strs[INDEX_TRAILER]+ "\"");
         }
      }
      
      return list;
   }
   
   private static String neverNull(String str)
   {
      if (str==null)
         return "";
      return str;
   }
}
