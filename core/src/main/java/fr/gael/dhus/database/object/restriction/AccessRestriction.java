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

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
   @Id
   @GeneratedValue(strategy=GenerationType.AUTO)
   @Column ( name="ID", nullable = false )
   private Long id;
   
   @Column (name = "BLOCKING_REASON")
   protected String blockingReason;

   /**
    * @param blockingReason the blockingReason to set
    */
   public void setBlockingReason (String blockingReason)
   {
      this.blockingReason = blockingReason;
   }

   /**
    * @return the blockingReason
    */
   public String getBlockingReason ()
   {
      return blockingReason;
   }
   
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
    * inheritance management within hibernate. 
    */
   @Column(name="ACCESS_RESTRICTION",insertable=false,updatable=false)
   String discriminator;
   
   public abstract boolean isBlocked();
}
