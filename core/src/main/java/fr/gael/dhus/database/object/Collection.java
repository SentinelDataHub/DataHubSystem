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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * Collection is a set of products.
 */
@Entity
@Table (name = "COLLECTIONS")
public class Collection implements Serializable
{
   /**
    * serial ID
    */
   private static final long serialVersionUID = 6480328554272776667L;

   final public static String HIDDEN_PREFIX = "#.";
   final public static String ROOT_NAME = HIDDEN_PREFIX+"root";
   
   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID")
   private Long id;

   @Column (name = "NAME", nullable = false)
   private String name;

   @Column (name = "DESCRIPTION", nullable = true, length = 1024)
   private String description;

   @ManyToMany (fetch = FetchType.LAZY)
   @JoinTable(
      name="COLLECTION_PRODUCT",
      joinColumns={@JoinColumn(name="COLLECTIONS_ID", table="COLLECTIONS")}, 
      inverseJoinColumns={@JoinColumn(name="PRODUCTS_ID", table="PRODUCTS")})
   @OrderBy ("identifier")
   private Set<Product> products = new HashSet<> ();

   @ManyToOne (fetch = FetchType.EAGER)
   @JoinColumn (name = "PARENT_COLLECTION_ID")
   private Collection parent;

   @OneToMany(mappedBy="parent", fetch = FetchType.EAGER)
   @OrderBy ("name")
   private Set<Collection>subCollections;
   
   
   @ManyToMany (fetch = FetchType.LAZY)
   @JoinTable (
      name="COLLECTION_USER_AUTH",
      joinColumns={@JoinColumn(name="COLLECTIONS_ID", table="COLLECTIONS")},
      inverseJoinColumns={@JoinColumn(name="USERS_ID", table="USERS")})
   @OrderBy ("username")
   private Set<User> authorizedUsers = new HashSet<User> ();

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
    * @return the parent
    */
   public Collection getParent ()
   {
      return parent;
   }

   /**
    * @param parent the parent to set
    */
   public void setParent (Collection parent)
   {
      this.parent = parent;
   }

   /**
    * @return the subCollections
    */
   public Set<Collection> getSubCollections ()
   {
      return this.subCollections;
   }

   /**
    * @param authorizedUsers the authorizedUsers to set
    */
   public void setAuthorizedUsers (Set<User> authorizedUsers)
   {
      this.authorizedUsers = authorizedUsers;
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
      result = prime * result + ( (id == null) ? 0 : id.hashCode ());
      return result;
   }

   @Override
   public boolean equals (Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass () != obj.getClass ()) return false;
      Collection other = (Collection) obj;
      if (id == null)
      {
         if (other.id != null) return false;
      }
      else
         if ( !id.equals (other.id)) return false;
      return true;
   }
}
