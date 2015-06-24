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
public class Quota
{
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
    * @param maxUpload the maxUpload to set
    */
   public void setMaxUpload (BigInteger maxUpload)
   {
      this.maxUpload = maxUpload;
   }
   /**
    * @return the maxDownload
    */
   public BigInteger getMaxDownload ()
   {
      return maxDownload;
   }
   /**
    * @param maxDownload the maxDownload to set
    */
   public void setMaxDownload (BigInteger maxDownload)
   {
      this.maxDownload = maxDownload;
   }
   /**
    * @param maxConnectionCount the maxConnectionCount to set
    */
   public void setMaxConnectionCount (BigInteger maxConnectionCount)
   {
      this.maxConnectionCount = maxConnectionCount;
   }
   /**
    * @return the maxConnectionCount
    */
   public BigInteger getMaxConnectionCount ()
   {
      return maxConnectionCount;
   }
   
}
