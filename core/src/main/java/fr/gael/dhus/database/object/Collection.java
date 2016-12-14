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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * Collection is a set of products.
 */
@Entity
@Table (name = "COLLECTIONS")
public class Collection implements Serializable
{
   public static final String HIDDEN_PREFIX = "#.";
   public static final String ROOT_NAME = HIDDEN_PREFIX+"root";

   private static final long serialVersionUID = 6480328554272776667L;

   @Id
   @Column (name = "UUID", nullable = false)
   private String uuid = UUID.randomUUID ().toString ();

   @Column (name = "NAME", nullable = false, unique = true)
   private String name;

   @Column (name = "DESCRIPTION", length = 1024)
   private String description;

   @ManyToMany (fetch = FetchType.LAZY)
   @JoinTable(
      name="COLLECTION_PRODUCT",
      joinColumns={@JoinColumn(name="COLLECTIONS_UUID", table="COLLECTIONS")},
      inverseJoinColumns={@JoinColumn(name="PRODUCTS_ID", table="PRODUCTS")})
   @OrderBy ("identifier")
   private Set<Product> products = new HashSet<> ();

   @ManyToMany (fetch = FetchType.LAZY)
   @JoinTable (
      name="COLLECTION_USER_AUTH",
      joinColumns={@JoinColumn(name="COLLECTIONS_UUID", table="COLLECTIONS")},
      inverseJoinColumns={@JoinColumn(name="USERS_UUID", table="USERS")})
   @OrderBy ("username")
   private Set<User> authorizedUsers = new HashSet<> ();

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

   /**
    * @return the name
    */
   public String getName ()
   {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName (String name)
   {
      this.name = name;
   }

   /**
    * @return the description
    */
   public String getDescription ()
   {
      return description;
   }

   /**
    * @param description the description to set
    */
   public void setDescription (String description)
   {
      this.description = description;
   }

   /**
    * @return the products
    */
   public Set<Product> getProducts ()
   {
      return products;
   }

   /**
    * @param products the products to set
    */
   public void setProducts (Set<Product> products)
   {
      this.products = products;
   }

   /**
    * @param authorized_users the authorizedUsers to set
    */
   public void setAuthorizedUsers (Set<User> authorized_users)
   {
      this.authorizedUsers = authorized_users;
   }

   /**
    * @return the authorizedUsers
    */
   public Set<User> getAuthorizedUsers ()
   {
      return authorizedUsers;
   }

   @Override
   public String toString ()
   {
      return this.name;
   }

   @Override
   public int hashCode ()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( (uuid == null) ? 0 : uuid.hashCode ());
      return result;
   }

   @Override
   public boolean equals (Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass () != obj.getClass ()) return false;
      Collection other = (Collection) obj;
      if (uuid == null)
      {
         if (other.uuid != null) return false;
      }
      else
         if ( !uuid.equals (other.uuid)) return false;
      return true;
   }
}
