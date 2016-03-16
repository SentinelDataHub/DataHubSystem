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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import fr.gael.dhus.datastore.eviction.EvictionStrategy;

@Entity
@Table (name = "EVICTION")
public class Eviction implements Serializable
{
   private static final long serialVersionUID = -8274887879237178967L;

   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID")
   private Long id;

   @Column (name = "STRATEGY", nullable = false)
   private EvictionStrategy strategy;

   @Column (name = "MAX_DISK_USAGE", nullable = false)
   private int maxDiskUsage;

   @Column (name = "KEEP_PERIOD", nullable = false)
   private int keepPeriod;

   @Column (name = "EVICTION_MAX_PRODUCT_NUMBER", nullable = false)
   private int maxProductNumber;
   
   @ElementCollection (targetClass=Product.class, fetch=FetchType.EAGER)
   @JoinTable (name="PRODUCT_TO_EVICT",  
      joinColumns = {@JoinColumn(name="EVICTION_ID")}, 
      inverseJoinColumns = { @JoinColumn (name = "PRODUCT_ID") })
   private Set<Product>products=new HashSet<Product> ();
   
   public Long getId ()
   {
      return id;
   }

   public void setId (Long id)
   {
      this.id = id;
   }

   public EvictionStrategy getStrategy ()
   {
      return strategy;
   }

   public void setStrategy (EvictionStrategy strategy)
   {
      this.strategy = strategy;
   }

   public int getMaxDiskUsage ()
   {
      return maxDiskUsage;
   }

   public void setMaxDiskUsage (int max_disk_usage)
   {
      this.maxDiskUsage = max_disk_usage;
   }

   public int getKeepPeriod ()
   {
      return keepPeriod;
   }

   public void setKeepPeriod (int keep_period)
   {
      this.keepPeriod = keep_period;
   }

   public int getMaxProductNumber ()
   {
      return maxProductNumber;
   }

   public void setMaxProductNumber (int max_product_number)
   {
      this.maxProductNumber = max_product_number;
   }

   public Set<Product> getProducts ()
   {
      return products;
   }

   public void setProducts (Set<Product> products)
   {
      this.products = products;
   }
}
