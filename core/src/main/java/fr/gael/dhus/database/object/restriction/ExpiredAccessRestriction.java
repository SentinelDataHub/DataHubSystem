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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author pidancier
 *
 */
@Entity
@DiscriminatorValue("expired")
public class ExpiredAccessRestriction extends AccessRestriction
{
   @Column (name = "EXPIRED", columnDefinition = "BOOLEAN")
   private boolean expired=false;
   
   @Column (name = "REGISTRATION")
   private Date registration=new Date();
   
   public ExpiredAccessRestriction (Date registration)
   {
      this();
      this.setRegistration (registration);
   }
   
   public ExpiredAccessRestriction ()
   {
      super.setBlockingReason (
         new String ("Your account has been expired."));
   }
   
   /**
    * @param registration the registration to set
    */
   public void setRegistration (Date registration)
   {
      this.registration = registration;
   }

   /**
    * @return the registration
    */
   public Date getRegistration ()
   {
      return registration;
   }
   
   @Override
   public boolean isBlocked()
   {
      return expired;
   }
}
