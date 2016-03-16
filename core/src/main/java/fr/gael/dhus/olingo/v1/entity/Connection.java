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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.olingo.odata2.api.exception.ODataException;

import fr.gael.dhus.olingo.v1.entityset.ConnectionEntitySet;
import fr.gael.dhus.server.http.valve.AccessInformation;

/**
 * Connection Bean. Connection informations served by the DHuS.
 */
public class Connection extends V1Entity
{
   protected final AccessInformation accessInformation;
   protected final UUID uuid;

   public Connection (UUID uuid, AccessInformation accessInformation)
   {
      this.accessInformation = accessInformation;
      this.uuid = uuid;
   }

   public String getUsername ()
   {
      return accessInformation.getUsername ();
   }

   public UUID getUUID()
   {
      return uuid;
   }

   public Date getStartDate()
   {
      return accessInformation.getStartDate();
   }

   public String getRemoteAddress()
   {
      return accessInformation.getRemoteAddress();
   }

   public String getRequest()
   {
      return accessInformation.getRequest();
   }

   public Double getDurationMs()
   {
      return accessInformation.getDurationMs();
   }

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      Map<String, Object> res = new HashMap<> ();
      res.put (ConnectionEntitySet.ID, uuid.toString ());
      res.put (ConnectionEntitySet.DATE, accessInformation.getStartDate ());
      res.put (ConnectionEntitySet.REMOTEIP,
         accessInformation.getRemoteAddress ());
      res.put (ConnectionEntitySet.REQUEST, accessInformation.getRequest ());
      res.put (ConnectionEntitySet.DURATION, accessInformation.getDurationMs ());
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      if (prop_name.equals (ConnectionEntitySet.ID)) return uuid;
      if (prop_name.equals (ConnectionEntitySet.DATE))
         return accessInformation.getStartDate ();
      if (prop_name.equals (ConnectionEntitySet.REMOTEIP))
         return accessInformation.getRemoteAddress ();
      if (prop_name.equals (ConnectionEntitySet.REQUEST))
         return accessInformation.getRequest ();
      if (prop_name.equals (ConnectionEntitySet.DURATION))
         return accessInformation.getDurationMs ();

      throw new ODataException ("Property '" + prop_name + "' not found.");
   }
}
