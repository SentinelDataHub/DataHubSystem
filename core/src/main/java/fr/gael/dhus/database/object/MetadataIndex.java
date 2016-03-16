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
import javax.persistence.Embeddable;

/**
 * This class implements persistent metadata element list attached 
 * to each {@link Product}.
 *
 */
@Embeddable
public class MetadataIndex
{

   @Column (name = "NAME", nullable = false)
   private String name;
   
   @Column (name = "TYPE")
   private String type;
   
   @Column (name = "CATEGORY")
   private String category;
   
   @Column (name = "QUERYABLE")
   private String queryable;
   
   @Column (name = "VALUE", nullable = false, length=8192)
   private String value;

   public MetadataIndex ()
   {
   }

   public MetadataIndex (String name, String type, String category,
      String queryable, String value)
   {
      setName (name);
      setType (type);
      setCategory (category);
      setQueryable (queryable);
      setValue (value);
   }

   public MetadataIndex (MetadataIndex index)
   {
      setName (index.getName ());
      setType (index.getType ());
      setCategory (index.getCategory ());
      setQueryable (index.getQueryable ());
      setValue (index.getValue ());
   }

   /**
    * @param name the name to set
    */
   public void setName (String name)
   {
      this.name = name;
   }
   /**
    * @return the name
    */
   public String getName ()
   {
      return name;
   }
   /**
    * @return the type
    */
   public String getType ()
   {
      return type;
   }
   /**
    * @param type the type to set
    */
   public void setType (String type)
   {
      this.type = type;
   }
   /**
    * @return the value
    */
   public String getValue ()
   {
      return value;
   }
   /**
    * @param value the value to set
    */
   public void setValue (String value)
   {
      this.value = value;
   }
   /**
    * @param category the category to set
    */
   public void setCategory (String category)
   {
      this.category = category;
   }
   /**
    * @return the category
    */
   public String getCategory ()
   {
      return category;
   }
   /**
    * @param queryable the queryable to set
    */
   public void setQueryable (String queryable)
   {
      this.queryable = queryable;
   }
   /**
    * @return the queryable
    */
   public String getQueryable ()
   {
      return queryable;
   }

   @Override
   public int hashCode ()
   {
      final int prime = 31;
      int result = 1;
      result =
         prime * result + ( (category == null) ? 0 : category.hashCode ());
      result = prime * result + ( (name == null) ? 0 : name.hashCode ());
      result =
         prime * result + ( (queryable == null) ? 0 : queryable.hashCode ());
      result = prime * result + ( (type == null) ? 0 : type.hashCode ());
      result = prime * result + ( (value == null) ? 0 : value.hashCode ());
      return result;
   }

   @Override
   public boolean equals (Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass () != obj.getClass ()) return false;
      MetadataIndex other = (MetadataIndex) obj;
      if (category == null)
      {
         if (other.category != null) return false;
      }
      else
         if ( !category.equals (other.category)) return false;
      if (name == null)
      {
         if (other.name != null) return false;
      }
      else
         if ( !name.equals (other.name)) return false;
      if (queryable == null)
      {
         if (other.queryable != null) return false;
      }
      else
         if ( !queryable.equals (other.queryable)) return false;
      if (type == null)
      {
         if (other.type != null) return false;
      }
      else
         if ( !type.equals (other.type)) return false;
      if (value == null)
      {
         if (other.value != null) return false;
      }
      else
         if ( !value.equals (other.value)) return false;
      return true;
   }
}
