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
package fr.gael.dhus.database.object;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * User saved searches
 */
@Entity
@Table (name = "SEARCHES")
public class Search
{
   @Id
   @Column (name = "UUID", nullable = false)
   private String uuid = UUID.randomUUID ().toString ();

   @Column (name = "VALUE")
   private String value;
   @Column (name = "NOTIFY", columnDefinition = "BOOLEAN")
   private boolean notify;
   @Column (name = "FOOTPRINT")
   private String footprint;

   @ElementCollection(fetch=FetchType.EAGER)
   @Cascade ({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
   private Map<String, String> advanced = new HashMap<> ();

   @Column ( name="COMPLETE", length=5000)
   private String complete;

   /**
    * @return the uuid
    */
   public String getUUID ()
   {
      return uuid;
   }

   /**
    * @param uuid the uuid to set
    */
   public void setId (String uuid)
   {
      this.uuid = uuid;
   }

   public String getValue ()
   {
      return value;
   }

   public void setValue (String value)
   {
      this.value = value;
   }

   public boolean isNotify ()
   {
      return notify;
   }

   public void setNotify (boolean notify)
   {
      this.notify = notify;
   }

   public String getFootprint ()
   {
      return footprint;
   }

   public void setFootprint (String footprint)
   {
      this.footprint = footprint;
   }

   public String getComplete ()
   {
      return complete;
   }

   public void setComplete (String complete)
   {
      this.complete = complete;
   }

   public Map<String, String> getAdvanced ()
   {
      return advanced;
   }

   public void setAdvanced (Map<String, String> advanced)
   {
      this.advanced = advanced;
   }

   @Override
   public boolean equals (Object o)
   {
      if (this == o) return true;
      if (!(o instanceof Search)) return false;

      Search search = (Search) o;

      return !(uuid != null ? !uuid.equals (search.uuid) : search.uuid != null);

   }

   @Override
   public int hashCode ()
   {
      return uuid != null ? uuid.hashCode () : 0;
   }
}
