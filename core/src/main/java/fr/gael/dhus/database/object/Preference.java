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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


/**
 * Persistent User preferences
 */
@Entity
@Table (name = "PREFERENCES")
public class Preference implements Serializable
{
   private static final long serialVersionUID = 3943794837766136598L;

   @Id
   @Column (name = "UUID", nullable = false)
   private String uuid = UUID.randomUUID ().toString ();
   
   @OneToMany (fetch = FetchType.EAGER)
   @CollectionTable (name="SEARCH_PREFERENCES", 
      joinColumns = @JoinColumn (name="PREFERENCE_UUID"))
   @Cascade ({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
   private Set<Search> searches = new HashSet<Search> ();
   
   @OneToMany (fetch=FetchType.EAGER)
   @JoinTable (name="FILE_SCANNER_PREFERENCES",  
      joinColumns = {@JoinColumn(name="PREFERENCE_UUID")}, 
      inverseJoinColumns = { @JoinColumn (name = "FILE_SCANNER_ID") })
   @Cascade ({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
   private Set<FileScanner> fileScanners = new HashSet<FileScanner> ();

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
   public void setUUID (String uuid)
   {
      this.uuid = uuid;
   }

   /**
    * @param search the search to set
    */
   public void setSearches (Set<Search> searches)
   {
      this.searches = searches;
   }

   /**
    * @return the search
    */
   public Set<Search> getSearches ()
   {
      return searches;
   }

   /**
    * @param file_scanners the fileScanners to set
    */
   public void setFileScanners (Set<FileScanner> file_scanners)
   {
      this.fileScanners = file_scanners;
   }

   /**
    * @return the fileScanners
    */
   public Set<FileScanner> getFileScanners ()
   {
      return fileScanners;
   }
}
