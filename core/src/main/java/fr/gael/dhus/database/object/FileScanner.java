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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * @author pidancier
 */
@Entity
@Table (name = "FILE_SCANNER")
public class FileScanner
{
   public static final String STATUS_ADDED = "added";
   public static final String STATUS_RUNNING = "running";
   public static final String STATUS_OK = "ok";
   public static final String STATUS_ERROR = "error";

   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID", nullable = false)
   private Long id;

   @Column (name = "URL")
   private String url;
   @Column (name = "USERNAME")
   private String username;
   @Column (name = "PASSWORD")
   private String password;
   @Column (name = "CRON_SCHEDULE")
   private String cronSchedule;
   @Column (name = "STATUS", nullable = false)
   private String status;
   @Column (name = "STATUS_MESSAGE", nullable = false)
   private String statusMessage;
   @Column (name = "PATTERN")
   private String pattern;

   @ManyToMany (fetch = FetchType.LAZY)
   @JoinTable(
      name="FILESCANNER_COLLECTIONS",
      joinColumns={@JoinColumn(name="FILE_SCANNER_ID", table="FILE_SCANNER")}, 
      inverseJoinColumns={@JoinColumn(name="COLLECTIONS_UUID",
                                      table="COLLECTIONS")})
   private Set<Collection> collections;
   
   @Column(name="ACTIVE", columnDefinition = "BOOLEAN", nullable = false)
   private boolean active=false;
   
   /**
    * @param id the id to set
    */
   public void setId (Long id)
   {
      this.id = id;
   }

   /**
    * @return the id
    */
   public Long getId ()
   {
      return id;
   }

   /**
    * @param url the url to set
    */
   public void setUrl (String url)
   {
      this.url = url;
   }

   /**
    * @return the url
    */
   public String getUrl ()
   {
      return url;
   }

   /**
    * @param username the username to set
    */
   public void setUsername (String username)
   {
      this.username = username;
   }

   /**
    * @return the username
    */
   public String getUsername ()
   {
      return username;
   }

   /**
    * @param password the password to set
    */
   public void setPassword (String password)
   {
      this.password = password;
   }

   /**
    * @return the password
    */
   public String getPassword ()
   {
      return password;
   }

   /**
    * @param password the pattern to set
    */
   public void setPattern (String pattern)
   {
      this.pattern = pattern;
   }

   /**
    * @return the pattern
    */
   public String getPattern ()
   {
      return pattern;
   }

   /**
    * @param active the status to set
    */
   public void setStatus (String status)
   {
      this.status = status;
   }

   /**
    * @return the status
    */
   public String getStatus ()
   {
      return status;
   }

   /**
    * @param active the statusMessage to set
    */
   public void setStatusMessage (String status_message)
   {
      this.statusMessage = status_message;
   }

   /**
    * @return the statusMessage
    */
   public String getStatusMessage ()
   {
      return statusMessage;
   }

   /**
    * @param cron_schedule the cronSchedule to set
    */
   public void setCronSchedule (String cron_schedule)
   {
      this.cronSchedule = cron_schedule;
   }

   /**
    * @return the cronSchedule
    */
   public String getCronSchedule ()
   {
      return cronSchedule;
   }

   public Set<Collection> getCollections ()
   {
      return collections;
   }

   public void setCollections (Set<Collection> collections)
   {
      this.collections = collections;
   }

   /**
    * @param active the active to set
    */
   public void setActive (boolean active)
   {
      this.active = active;
   }

   /**
    * @return the active
    */
   public boolean isActive ()
   {
      return active;
   }
}
