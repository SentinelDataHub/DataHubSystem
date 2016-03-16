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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name = "COUNTRIES")
public class Country
{
   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID", nullable = false)
   private Long id;

   @Column (name = "ALPHA2", nullable = false, unique = true)
   private String alpha2;

   @Column (name = "ALPHA3", nullable = false, unique = true)
   private String alpha3;
   
   @Column (name = "NUMERIC", nullable = false, unique = true)
   private Integer numeric;
   
   @Column (name = "NAME", nullable = false, unique = true)
   private String name;
   
   public Long getId ()
   {
      return id;
   }

   public String getName ()
   {
      return name;
   }

   public void setName (String name)
   {
      this.name = name;
   }

   public String getAlpha2 ()
   {
      return alpha2;
   }

   public void setAlpha2 (String alpha2)
   {
      this.alpha2 = alpha2;
   }

   public String getAlpha3 ()
   {
      return alpha3;
   }

   public void setAlpha3 (String alpha3)
   {
      this.alpha3 = alpha3;
   }

   public Integer getNumeric ()
   {
      return numeric;
   }

   public void setNumeric (Integer numeric)
   {
      this.numeric = numeric;
   }
}
