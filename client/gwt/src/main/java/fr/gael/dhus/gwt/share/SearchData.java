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

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SearchData implements IsSerializable
{
   private Long id;
   private boolean notify;
   private String value;
   private String complete;
   private Double[][] footprint;
   private Map<String, String> advanced;

   public SearchData ()
   {
   }
   
   public SearchData (Long id, String value, String complete, Map<String, String> advanced, Double[][] footprint, boolean notify)
   {
      this.id = id;
      this.value = value;
      this.complete = complete;
      this.notify = notify;
      this.advanced = advanced;
      this.footprint = footprint;
   }
   
   public Long getId ()
   {
      return id;
   }  

   public boolean isNotify ()
   {
      return notify;
   }

   public void setNotify (boolean notify)
   {
      this.notify = notify;
   }

   public String getValue ()
   {
      return value;
   }

   public void setValue (String value)
   {
      this.value = value;
   }

   public Double[][] getFootprint ()
   {
      return footprint;
   }

   public void setFootprint (Double[][] footprint)
   {
      this.footprint = footprint;
   }

   public Map<String, String> getAdvanced ()
   {
      return advanced;
   }

   public void setAdvanced (Map<String, String> advanced)
   {
      this.advanced = advanced;
   }
   
   public String getComplete ()
   {
      return complete;
   }

   public void setComplete (String complete)
   {
      this.complete = complete;
   }

   @Override
   public boolean equals (Object o)
   {
      return o instanceof SearchData && ((SearchData) o).id == this.id;
   }
}
