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
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Per users quotas definition
 *
 */
@Entity
@Table (name = "QUOTAS")
public class Quota implements Serializable
{
   private static final long serialVersionUID = 160128668451266745L;

   @Id
   @GeneratedValue(strategy=GenerationType.AUTO)
   @Column ( name="ID", nullable = false )
   private Long id;
   
   @Column ( name="UPLOADS")
   private BigInteger maxUpload;
   @Column ( name="DOWNLOADS")
   private BigInteger maxDownload;
   @Column ( name="CONNECTIONS")
   private BigInteger maxConnectionCount;
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
    * @return the maxUpload
    */
   public BigInteger getMaxUpload ()
   {
      return maxUpload;
   }
   /**
    * @param max_upload the maxUpload to set
    */
   public void setMaxUpload (BigInteger max_upload)
   {
      this.maxUpload = max_upload;
   }
   /**
    * @return the maxDownload
    */
   public BigInteger getMaxDownload ()
   {
      return maxDownload;
   }
   /**
    * @param max_download the maxDownload to set
    */
   public void setMaxDownload (BigInteger max_download)
   {
      this.maxDownload = max_download;
   }
   /**
    * @param max_connection_count the maxConnectionCount to set
    */
   public void setMaxConnectionCount (BigInteger max_connection_count)
   {
      this.maxConnectionCount = max_connection_count;
   }
   /**
    * @return the maxConnectionCount
    */
   public BigInteger getMaxConnectionCount ()
   {
      return maxConnectionCount;
   }
   
}
