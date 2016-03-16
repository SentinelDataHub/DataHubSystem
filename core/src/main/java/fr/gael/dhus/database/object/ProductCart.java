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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * User product cart
 */
@Entity
@Table (name = "PRODUCTCARTS")
public class ProductCart
{
   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID", nullable = false)
   private Long id;

   @OneToOne (fetch=FetchType.LAZY)
   private User user;
   
   @ManyToMany (fetch = FetchType.LAZY)
   @JoinTable (name = "CART_PRODUCTS",
               joinColumns = { @JoinColumn (name = "CART_ID") },
               inverseJoinColumns = { @JoinColumn (name = "PRODUCT_ID") })
   private Set<Product> products;

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

   public User getUser ()
   {
      return user;
   }

   public void setUser (User user)
   {
      this.user = user;
   }

   public Set<Product> getProducts ()
   {
      return products;
   }

   public void setProducts (Set<Product> products)
   {
      this.products = products;
   }

   @Override
   public boolean equals (Object o)
   {
      if (this == o) return true;
      if (!(o instanceof ProductCart)) return false;

      ProductCart that = (ProductCart) o;

      if (id != null ? !id.equals (that.id) : that.id != null) return false;
      return !(user != null ? !user.equals (that.user) : that.user != null);

   }

   @Override
   public int hashCode ()
   {
      int result = id != null ? id.hashCode () : 0;
      result = 31 * result + (user != null ? user.hashCode () : 0);
      return result;
   }
}
