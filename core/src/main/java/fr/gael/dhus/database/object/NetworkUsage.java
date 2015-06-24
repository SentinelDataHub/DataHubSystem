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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * This class denotes an download or upload transfer that a given user has
 * experienced at a given date.
 * 
 * TODO This class should be moved to fr.gael.dhus.database.object package
 */
@Entity
@Table (name = "NETWORK_USAGE")
public class NetworkUsage
{
   /**
    * The identifier of this network usage record.
    */
   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID", nullable = false)
   private Long id;

   /**
    * The user associated to this network usage record.
    */
   @OneToOne (fetch=FetchType.EAGER)
   private User user;

   /**
    * The date at witch the network usage occurred.
    */
   @Temporal (TemporalType.TIMESTAMP)
   @Column (name = "DATE", nullable = false)
   private Date date;

   /**
    * True if the transfer denoted by this class is a download (from server
    * to client). False otherwise.
    */
   @Column (name = "IS_DOWNLOAD", nullable = false, columnDefinition = "BOOLEAN")
   private boolean isDownload;

   /**
    * The size of the transfer in bytes. The size shall correspond to the
    * number of bytes actually transferred and not the one that were
    * intended to be transferred e.g. an interrupted download should record
    * the bytes transferred and not the total size of the object of he
    * transfer.
    */
   @Column (name = "SIZE", nullable = false)
   private Long size;

   /**
    * @return the id
    */
   public Long getId ()
   {
      return id;
   }

   /**
    * @param id the id to set
    */
   public void setId (Long id)
   {
      this.id = id;
   }

   /**
    * @return the user
    */
   public User getUser ()
   {
      return user;
   }

   /**
    * @param user the user to set
    */
   public void setUser (User user)
   {
      this.user = user;
   }

   /**
    * @return the date
    */
   public Date getDate ()
   {
      return date;
   }

   /**
    * @param date the date to set
    */
   public void setDate (Date date)
   {
      this.date = date;
   }

   /**
    * @return the isDownload
    */
   public Boolean getIsDownload ()
   {
      return isDownload;
   }

   /**
    * @param isDownload the isDownload to set
    */
   public void setIsDownload (Boolean isDownload)
   {
      this.isDownload = isDownload;
   }

   /**
    * @return the size
    */
   public Long getSize ()
   {
      return size;
   }

   /**
    * @param size the size to set
    */
   public void setSize (Long size)
   {
      this.size = size;
   }

} // End NetworkUsage class
