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
package fr.gael.dhus.database.object.restriction;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * @author pidancier
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ACCESS_RESTRICTION")
@Table (name = "ACCESS_RESTRICTION")
public abstract class AccessRestriction
{
   @Column (name = "BLOCKING_REASON")
   protected String blockingReason;

   /**
    * inheritance management within hibernate.
    */
   @Column(name="ACCESS_RESTRICTION",insertable=false,updatable=false)
   String discriminator;

   @Id
   @Column (name = "UUID", nullable = false)
   private String uuid = UUID.randomUUID ().toString ();

   /**
    * @param blocking_reason the blockingReason to set
    */
   public void setBlockingReason (String blocking_reason)
   {
      this.blockingReason = blocking_reason;
   }

   /**
    * @return the blockingReason
    */
   public String getBlockingReason ()
   {
      return blockingReason;
   }

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

   public abstract boolean isBlocked();
}
