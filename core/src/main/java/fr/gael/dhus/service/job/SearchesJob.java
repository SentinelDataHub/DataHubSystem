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
package fr.gael.dhus.service.job;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.database.dao.UserDao;
import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.database.object.Product;
import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.Search;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.messaging.mail.MailServer;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.SearchService;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Autowired by {@link AutowiringJobFactory}
 */
@Component
public class SearchesJob extends AbstractJob
{
   private static final Logger LOGGER = LogManager.getLogger(SearchesJob.class);
      
   @Autowired
   private UserDao userDao;
   
   @Autowired
   private ProductService productService;
   
   @Autowired
   private MailServer mailServer;
   
   @Autowired
   private SearchService searchService;

   @Autowired
   private ConfigurationManager configurationManager;
   
   @Override
   public String getCronExpression ()
   {
      return configurationManager.getSearchesCronConfiguration ().getSchedule();
   }

   @Override
   protected void executeInternal (JobExecutionContext arg0)
      throws JobExecutionException
   {
      if (!configurationManager.getSearchesCronConfiguration ().isActive ())
         return;
      long time_start = System.currentTimeMillis ();
      LOGGER.info("SCHEDULER : User searches mailings.");
      if (!DHuS.isStarted ())
      {
         LOGGER.warn("SCHEDULER : Not run while system not fully initialized.");
         return;
      }
      Map <String,String>cids= new HashMap<String, String> ();
      for (User user:userDao.readNotDeleted ())
      {
         List<Search>searches = userDao.getUserSearches (user);
         if (searches == null || (searches.size ()==0)) 
         {
            LOGGER.debug("No saved search for user \"" + user.getUsername () +
                  "\".");
            continue;
         }
         
         if (user.getEmail () == null)
         {
            LOGGER.error("User \"" + user.getUsername () +
               "\" email not configured to send search notifications.");
            continue;
         }
         
         HtmlEmail he = new HtmlEmail();
         cids.clear ();

         int maxResult = searches.size () >= 10 ? 5 : 10;
         String message = "<html><style>" +
               "a { font-weight: bold; color: #205887; " +
               "text-decoration: none; }\n" +
               "a:hover { font-weight:bold; color: #FF790B" +
               "; text-decoration: none; }\na img { border-style: none; }\n" +
               "</style><body style=\"font-family: Trebuchet MS, Helvetica, " +
               "sans-serif; font-size: 14px;\">Dear " + getUserWelcome (user) +
               ",<p/>\n\n";
         message += "You requested periodic notification for the following " +
               "searches. Here are the top "+maxResult+" results for " +
               "each search:<p/>";
         message +="<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" " +
               "style=\"width: 100%;font-family: Trebuchet MS, Helvetica, " +
               "sans-serif; font-size: 14px;\"><tbody>";

         boolean atLeastOneResult = false;
         for (Search search:searches)
         {
            if (search.isNotify ())
            {
               message += "<tr><td colspan=\"3\" style=\"font-size: 13px; " +
                     "font-weight: bold; color: white; background-color: " +
                     "#205887; text-align: center;\"><b>";
               message += search.getValue ();
               message += "</b></td></tr>\n";

               Map<String, String> advanceds = search.getAdvanced ();     
               if (advanceds != null && !advanceds.isEmpty ())
               {
                  message +="<tr><td style=\"font-size: 13px; padding:0px; " +
                        "margin:0px; font-weight:normal; background-color: " +
                        "#799BB7; text-align: center; border-left: 1px solid " +
                        "#205887; border-right: 1px solid #205887; " +
                        "border-bottom: 1px solid #205887;\">";
                  boolean first = true;
                  List<String> keys = new ArrayList<String> (
                        advanceds.keySet ());
                  Collections.sort (keys);        
                  String lastKey = "";
                  for (String key : keys)
                  {
                     if ((lastKey+"End").equals(key))
                     {
                        message += " to "+advanceds.get (key);
                     }  
                     else
                     {
                        if (key.endsWith ("End"))
                        {
                           message += (first?"":", ") +
                                 key.substring (0, key.length ()-3) +
                                 ": * to "+advanceds.get (key);
                        }
                        else
                        {
                           message += (first?"":", ") + key+": " +
                                 advanceds.get (key);
                        }
                     }
                     first = false;
                     lastKey = key;
                  }
                  message +="</td></tr>";
               }
               
               Iterator<Product> results;
               try
               {
                  results = searchService.search(search.getComplete());
               }
               catch (Exception e)
               {
                  message += "<tr><td colspan=\"3\" style=\"" +
                        "text-align: center; border-left: 1px solid #205887; " +
                        "border-right: 1px solid #205887;\">" +
                        "No result found</td></tr>";
                  LOGGER.debug("There was an error when executing query : \"" +
                        e.getMessage () + "\"");
                  continue;
               }
                              
               if (!results.hasNext())
               {
                  message += "<tr><td colspan=\"3\" style=\"" +
                        "text-align: center; border-left: 1px solid #205887; " +
                        "border-right: 1px solid #205887;\">" +
                        "No result found</td></tr>";
                  LOGGER.debug("No result matches query : \"" +
                        search.getComplete () + "\"");
               }
               
               boolean first = true;   
               int searchIndex = 0;
               while (results.hasNext () && searchIndex < maxResult)
               {
                  
                  if (!first)
                  {
                     message += "<tr><td colspan=\"3\" style=\"" +
                           "background-color: #205887; height:1px;\" /></tr>";
                  }
                  first = false;
                  
                  Product product = results.next();
                  // WARNING : must implement to schedule fix of this issue...
                  if (product==null) continue;
                  
                  atLeastOneResult = true;
                  searchIndex++;
                  
                  LOGGER.debug("Result found: " + product.getIdentifier());
                  
                  String purl = configurationManager.getServerConfiguration ()
                        .getExternalUrl () + "odata/v1/Products('" +
                        product.getUuid () + "')";
                  
                  // EMBEDED THUMBNAIL
                  String cid=null;
                  if (product.getThumbnailFlag ())
                  {
                     File thumbnail = new File (product.getThumbnailPath ());
                     String thumbname = thumbnail.getName ();
                     if (cids.containsKey (thumbname))
                     {
                        cid=cids.get (thumbname);
                     }
                     else
                     {
                        try
                        {
                           cid = he.embed (thumbnail);
                           cids.put (thumbname, cid);
                        }
                        catch (Exception e)
                        {
                           LOGGER.warn("Cannot embed image \"" + purl +
                                 "/Products('Quicklook')/$value\" :"+
                                 e.getMessage ());
                           cid=null;
                        }
                     }
                  }
                  boolean downloadRight = user.getRoles ().contains (
                        Role.DOWNLOAD);
                  String link = downloadRight?"(<a target=\"_blank\" href=\"" +
                        purl + "/$value\">download</a>)" : "";
                  message += "   <tr><td colspan=\"3\" style=\"" +
                        "font-size: 14px; text-align: center; " +
                        "border-left: 1px solid #205887; border-right: 1px " +
                        "solid #205887;\"><a target=\"_blank\" href=\"" +
                        purl + "/$value\">" + product.getIdentifier () +
                        "</a> "+link+"</td>\n</tr>\n";
                  if (cid != null)
                  {
                     message += "   <tr><td rowspan=\"8\" style=\"" +
                           "text-align: center; vertical-align: middle;" +
                           " border-left: 1px solid #205887;\">" +
                           "<a target=\"_blank\" href=\"" + purl +
                           "/Products('Quicklook')/$value\"><img src=cid:" +
                           cid  + " style=\"max-height: 64px; max-width:" +
                           " 64px;\"></a></td>\n";
                  }
                                    
                  // Displays metadata
                  List<MetadataIndex>indexes = new ArrayList<> (
                        productService.getIndexes (product.getId ()));
                  Collections.sort (indexes, new Comparator<MetadataIndex>()
                  {
                     @Override
                     public int compare (MetadataIndex o1, MetadataIndex o2)
                     {
                        if ((o1.getCategory () == null) || 
                             o1.getCategory ().equals (o2.getCategory ()))
                           return o1.getName ().compareTo (o2.getName ());
                        return o1.getCategory ().compareTo (o2.getCategory ());
                     }
                  });   
                  int i = 0;
                  for (MetadataIndex index:indexes)
                  {
                     String queryable = index.getQueryable ();
                     String name = index.getName ();
                     String value = index.getValue ();
                     
                     if (value.length ()>50) continue;
                     
                     if (queryable != null) name += "(" + queryable + ")";
                     
                     if (i != 0)
                     {
                        message += "<tr>";
                     }
                     String start = "<td";
                     if (cid == null || i >= 8)
                     {
                        start += " style=\"width: 120px;" +
                              " border-left: 1px solid #205887;\"><td";
                     }
                     i++;
                     message += start+">" + name + "</td>" +
                           "<td style=\"border-right: 1px solid #205887;\">" +
                           value + "</td>";
                     message += "</tr>";
                  }
                  if (indexes == null || indexes.size () == 0)
                  {
                     message += "</tr>";
                  }
               }
            }
         }
         // No result: next user, no mail.
         if (!atLeastOneResult) continue;
         message += "<tr><td colspan=\"3\" style=\"background-color: #205887;" +
               " height:1px;\" /></tr>";
         message +="</tbody></table><p/>\n";
         message +="You can configure which searches are sent by mail in the "+
            "<i>saved searches</i> tab in "+ configurationManager
               .getNameConfiguration ().getShortName () +
            " system at <a target=\"_blank\" href=\"" +
            configurationManager.getServerConfiguration ().getExternalUrl () +
               "\">" +
            configurationManager.getServerConfiguration ().getExternalUrl () +
            "</a><br/>To stop receiving this message, just disable " +
            "all searches.<p/>";
         
         message += "Thanks for using " +
               configurationManager.getNameConfiguration ().getShortName () +
               ",<br/>" +
               configurationManager.getSupportConfiguration ().getName ();
         message += "</body></html>";
         
         LOGGER.info("Sending search results to " + user.getEmail());
         LOGGER.debug(message);
         
         try
         {
            he.setHtmlMsg (message);
            mailServer.send (he, user.getEmail (), null, null,
                  "Saved searches notifications");
         }
         catch (EmailException e)
         {
            LOGGER.error("Cannot send mail to \"" +
               user.getEmail () + "\" :" + e.getMessage ());
         }
      }
      LOGGER.info("SCHEDULER : User searches mailings done - " +
          (System.currentTimeMillis ()-time_start) + "ms");
   }
   
   private String getUserWelcome (User u)
   {
      String firstname = u.getUsername ();
      String lastname = "";
      if (u.getFirstname () != null && !u.getFirstname().trim ().isEmpty ())
      {
         firstname = u.getFirstname ();
         if (u.getLastname () != null && !u.getLastname().trim ().isEmpty ())
            lastname = " " + u.getLastname ();
      }
      return firstname + lastname;
   }
}
