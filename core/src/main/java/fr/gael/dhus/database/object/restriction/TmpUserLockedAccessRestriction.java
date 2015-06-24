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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author pidancier
 */
@Entity
@DiscriminatorValue ("temporary")
public class TmpUserLockedAccessRestriction extends LockedAccessRestriction
{
   @Column (name = "CREATION")
   private Date lockDate;

   public TmpUserLockedAccessRestriction ()
   {
      lockDate = new Date ();
      SimpleDateFormat df = new SimpleDateFormat ("dd MMMM yyyy", Locale.US);
      super
         .setBlockingReason (new String (
            "Your account is pending, you need to validate it " +
               "by clicking on the link contained in the mail that was sent " +
               "to you on " +df.format(lockDate)+"."));
   }

   /**
    * @return the lockDate
    */
   public Date getLockDate ()
   {
      return lockDate;
   }
}
