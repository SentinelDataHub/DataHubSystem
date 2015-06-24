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
package fr.gael.dhus.database.object.statistic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table (name = "ACTION_RECORD_DOWNLOADS")
public class ActionRecordDownload extends ActionRecord
{
   private static final long serialVersionUID = -917918363678621946L;

   /**
    * Product identifier. This field is a simple String (and not a Product since
    * products can be removed from the database.
    */
   @Column (name = "PRODUCT_IDENTIFIER", nullable = false)
   private String productIdentifier;

   /**
    * Product size.
    */
   @Column (name = "PRODUCT_SIZE", nullable = true)
   private Long productSize;

   public String getProductIdentifier ()
   {
      return productIdentifier;
   }

   public void setProductIdentifier (String productIdentifier)
   {
      this.productIdentifier = productIdentifier;
   }

   public Long getProductSize ()
   {
      return productSize;
   }

   public void setProductSize (Long productSize)
   {
      this.productSize = productSize;
   }
}
