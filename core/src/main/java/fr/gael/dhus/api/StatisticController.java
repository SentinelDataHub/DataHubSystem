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
package fr.gael.dhus.api;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.gael.dhus.service.StatisticsService;

@Controller
@RequestMapping (value = "/statistic")
public class StatisticController
{
   @Autowired // ActionRecordReaderService
   private StatisticsService statisticsService;
   
   @PreAuthorize ("hasRole('ROLE_UPLOAD')")
   @RequestMapping (value = "/download/get")
   public void search (@RequestParam(value="type", defaultValue="number") String type, 
      @RequestParam(value="periodScale", defaultValue="day") String periodScale,
      @RequestParam(value="per", defaultValue="") String per,
      @RequestParam(value="period", defaultValue="") String period,
      HttpServletResponse res) throws IOException, ParseException
   {
      type = type.toLowerCase ();
      periodScale = periodScale.toLowerCase ();
      per = per.toLowerCase ();

      SimpleDateFormat day = new SimpleDateFormat ("yyyyMMdd'T'");
      SimpleDateFormat df = new SimpleDateFormat ("yyyyMMdd'T'HHmmss");
      Date now = new Date ();
      Date end = df.parse (day.format (now) + "235959");

      Calendar cal = Calendar.getInstance ();
      cal.setTime (now);
      cal.add (Calendar.DATE, -30);
      Date dateBefore30Days = cal.getTime ();
      Date start = df.parse (day.format (dateBefore30Days) + "000000");         
      if (period != null && !period.isEmpty ())
      {
         String[] dates = period.split ("-");
         if (dates.length == 2)
         {
            start = df.parse (dates[0]);
            end = df.parse (dates[1]);
         }
      }

      String[][] stats;
      if ("volume".equals (type))
      {
         if ("domain".equals (per))
         {
            stats = statisticsService.getDownloadsSizePerDomain (start, end,
               "hour".equals (periodScale));
         }
         else if ("usage".equals (per))
         {
            stats = statisticsService.getDownloadsSizePerUsage (start, end,
               "hour".equals (periodScale));
         }
         else
         {
            stats = statisticsService.getDownloadsSizePerUser (start, end,
               new ArrayList<String> (), "hour".equals (periodScale));
         }
      }
      else if ("number".equals (type))
      {
         if ("domain".equals (per))
         {
            stats = statisticsService.getDownloadsPerDomain (start, end,
               "hour".equals (periodScale));
         }
         else if ("usage".equals (per))
         {
            stats = statisticsService.getDownloadsPerUsage (start, end,
               "hour".equals (periodScale));
         }
         else
         {
            stats = statisticsService.getDownloadsPerUser (start, end,
               new ArrayList<String> (), "hour".equals (periodScale));
         }
      }
      else
      {
         // should not happen
         res.sendError (HttpServletResponse.SC_BAD_REQUEST,
            "'type' parameter shall be 'number' or 'volume' only. Default value is 'number'.");
         return;
      }

      res.setStatus (HttpServletResponse.SC_OK);
      res.setHeader ("Content-Disposition", "inline;filename=downloads.tsv");
      res.setContentType ("text/tab-separated-values");
      for (String[] stat : stats)
      {
         String line = "";
         for (String s : stat)
         {
            line += s + "\t";
         }
         res.getWriter ().println (line.substring (0, line.length () - 1));
      }
      res.flushBuffer ();
   }
}
