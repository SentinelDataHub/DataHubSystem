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
package fr.gael.dhus.gwt.share;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FileScannerData implements IsSerializable
{

   private Long id;
   private String url;
   private String username;
   private String password;
   private String status;
   private String statusMessage;
   private String pattern;
   private List<Long> collections;
   private boolean active;

   public FileScannerData ()
   {
   }
   
   public FileScannerData(Long id, String url, String username, String password, String pattern, List<Long> collections, 
      String status, String statusMessage, boolean active)
   {
      this.url = url;
      this.id = id;
      this.username = username;
      this.password = password;
      this.pattern = pattern;
      this.collections = collections;
      this.status = status;
      this.statusMessage = statusMessage;
      this.active = active;
   }

   public String getUrl ()
   {
      return url;
   }

   public void setUrl (String url)
   {
      this.url = url;
   }

   public Long getId ()
   {
      return id;
   }

   public void setId (Long id)
   {
      this.id = id;
   }

   public String getUsername ()
   {
      return username;
   }

   public void setUsername (String username)
   {
      this.username = username;
   }

   public List<Long> getCollections ()
   {
      return collections;
   }

   public void setCollections (List<Long> collections)
   {
      this.collections = collections;
   }

   public String getStatus ()
   {
      return status;
   }

   public void setStatus (String status)
   {
      this.status = status;
   }

   public String getPassword ()
   {
      return password;
   }

   public void setPassword (String password)
   {
      this.password = password;
   }

   public String getStatusMessage ()
   {
      return statusMessage;
   }

   public void setStatusMessage (String statusMessage)
   {
      this.statusMessage = statusMessage;
   }

   public boolean isActive ()
   {
      return active;
   }

   public void setActive (boolean active)
   {
      this.active = active;
   }      
   
   public String getPattern()
   {
      return this.pattern;
   }
   
   public void setPattern(String pattern)
   {
      this.pattern = pattern;
   }
}
