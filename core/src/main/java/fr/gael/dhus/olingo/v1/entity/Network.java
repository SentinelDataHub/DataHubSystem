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

import fr.gael.dhus.olingo.v1.ExpectedException.InvalidTargetException;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entityset.NetworkEntitySet;

import java.util.HashMap;
import java.util.Map;

import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.NavigationSegment;

/**
 * Network Bean.
 */
public class Network extends AbstractEntity
{
   public Network ()
   {
   }

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      Map<String, Object> res = new HashMap<> ();
      res.put (NetworkEntitySet.ID, 0);
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      if (prop_name.equals (NetworkEntitySet.ID)) return 0;
      throw new ODataException ("Property '" + prop_name + "' not found.");
   }

   @Override
   public Object navigate(NavigationSegment ns) throws ODataException
   {
      Object res;

      if (ns.getEntitySet().getName().equals(Model.NETWORKSTATISTIC.getName()))
      {
         res = new NetworkStatistic();
      }
      else
      {
         throw new InvalidTargetException(this.getClass().getSimpleName(), ns.getEntitySet().getName());
      }

      return res;
   }

}
