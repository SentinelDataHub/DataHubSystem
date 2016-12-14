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
package fr.gael.dhus.olingo.v1.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.exception.ODataException;

import fr.gael.dhus.olingo.v1.entityset.NetworkStatisticEntitySet;
import fr.gael.dhus.server.http.valve.AbuseMetrics;
import fr.gael.dhus.server.http.valve.AccessValve;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Product Bean. A product served by the DHuS.
 */
public class NetworkStatistic extends AbstractEntity
{
   protected final AbuseMetrics abuseMetrics;

   public NetworkStatistic ()
   {
      ConfigurationManager cfgManager =
               ApplicationContextProvider.getBean (ConfigurationManager.class);
      this.abuseMetrics = (AccessValve.getMetrics (0, 
         cfgManager.getOdataConfiguration ().getMaxRows ()));
   }

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      Map<String, Object> res = new HashMap<> ();
      res.put (NetworkStatisticEntitySet.ID, 0);
      res.put (NetworkStatisticEntitySet.ACTIVITYPERIOD, abuseMetrics.getPeriod ());
      res.put (NetworkStatisticEntitySet.CONNECTIONNUMBER, abuseMetrics.getCalls ());
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      if (prop_name.equals (NetworkStatisticEntitySet.ID)) return 0;
      if (prop_name.equals (NetworkStatisticEntitySet.ACTIVITYPERIOD)) 
         return abuseMetrics.getPeriod ();
      if (prop_name.equals (NetworkStatisticEntitySet.CONNECTIONNUMBER)) 
         return abuseMetrics.getCalls ();
      throw new ODataException ("Property '" + prop_name + "' not found.");
   }
}
