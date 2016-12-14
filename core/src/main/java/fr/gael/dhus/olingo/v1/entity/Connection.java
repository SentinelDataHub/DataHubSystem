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
import fr.gael.dhus.olingo.v1.entityset.ConnectionEntitySet;
import fr.gael.dhus.olingo.v1.map.impl.UserMap;
import fr.gael.dhus.server.http.valve.AccessInformation;
import fr.gael.dhus.server.http.valve.AccessInformation.FailureConnectionStatus;
import fr.gael.dhus.server.http.valve.AccessInformation.Status;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.NavigationSegment;

/**
 * Connection Bean. Connection informations served by the DHuS.
 */
public class Connection extends AbstractEntity
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
   
   public String getConnectionStatus()
   {
      return accessInformation.getConnectionStatus()==null ? "UNKNOWN" :
         accessInformation.getConnectionStatus().getStatus().name();
   }
   public String getConnectionStatusMessage()
   {
      if ((accessInformation.getConnectionStatus()!=null) &&
          (accessInformation.getConnectionStatus().getStatus()==Status.FAILURE))
      {
         FailureConnectionStatus status = (FailureConnectionStatus)
            accessInformation.getConnectionStatus();
         if (status.getException()!=null)
            return status.getException().getMessage();
      }
      return null;
   }
   
   public long getContentLength ()
   {
      return accessInformation.getReponseSize();
   }
   
   public long getWrittenContentLength ()
   {
      return accessInformation.getWrittenResponseSize();
   }


   @Override
   public Map<String, Object> toEntityResponse(String root_url)
   {
      Map<String, Object>res = new HashMap<>();
      res.put(ConnectionEntitySet.ID,       uuid.toString());
      res.put(ConnectionEntitySet.DATE,     getStartDate());
      res.put(ConnectionEntitySet.REMOTEIP, getRemoteAddress());
      res.put(ConnectionEntitySet.REQUEST,  getRequest());
      res.put(ConnectionEntitySet.DURATION, getDurationMs());
      res.put(ConnectionEntitySet.CONTENT_LENGTH, getContentLength());
      res.put(ConnectionEntitySet.WRITTEN_CONTENT_LENGTH,
           getWrittenContentLength());
      res.put(ConnectionEntitySet.STATUS, getConnectionStatus());
      res.put(ConnectionEntitySet.STATUS_MESSAGE, getConnectionStatusMessage());
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      if (prop_name.equals (ConnectionEntitySet.ID)) return uuid;
      if (prop_name.equals (ConnectionEntitySet.DATE))
         return getStartDate ();
      if (prop_name.equals (ConnectionEntitySet.REMOTEIP))
         return getRemoteAddress ();
      if (prop_name.equals (ConnectionEntitySet.REQUEST))
         return getRequest ();
      if (prop_name.equals (ConnectionEntitySet.DURATION))
         return getDurationMs ();
      if (prop_name.equals (ConnectionEntitySet.CONTENT_LENGTH))
         return getContentLength();
      if (prop_name.equals (ConnectionEntitySet.WRITTEN_CONTENT_LENGTH))
         return getWrittenContentLength();
      if (prop_name.equals (ConnectionEntitySet.STATUS))
         return getConnectionStatus ();
      if (prop_name.equals (ConnectionEntitySet.STATUS_MESSAGE))
         return getConnectionStatusMessage ();

      throw new ODataException ("Property '" + prop_name + "' not found.");
   }

   @Override
   public Object navigate(NavigationSegment ns) throws ODataException
   {
      Object res;

      if (ns.getEntitySet().getName().equals(Model.USER.getName()))
      {
         res = new UserMap().get(getUsername());
      }
      else
      {
         throw new InvalidTargetException(this.getClass().getSimpleName(), ns.getEntitySet().getName());
      }

      return res;
   }

}
