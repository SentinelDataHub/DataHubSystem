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
package fr.gael.dhus.olingo.v1.map;

import java.util.Map;

import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;

/**
 * Builds a submap. Every method returns its instance for method chaining.
 * 
 * @param <K> Key
 * @param <V> Value
 */
public abstract class SubMapBuilder<K, V>
{
   protected int skip;
   protected int top;
   protected FilterExpression filter;
   protected OrderByExpression orderBy;

   protected SubMapBuilder ()
   {
      skip = 0;
      top = 0;
      filter = null;
      orderBy = null;
   }

   /**
    * Adds a filter. If you add several filters, they will be bound together
    * using the logical conjunction (AND operator).
    * 
    * @param filter a string which must conform to the syntax defined by the
    *           implementation, eg: SQL syntax.
    * @return this.
    */
   public SubMapBuilder<K, V> setFilter (FilterExpression filter)
   {
      this.filter = filter;
      return this;
   }

   /**
    * Sets the skip. Skip will skip the first results producted by
    * Map.values().iterator().
    * 
    * @param skip how many rows to skip.
    * @return this.
    */
   public SubMapBuilder<K, V> setSkip (int skip)
   {
      if (skip >= 0)
      {
         this.skip = skip;
      }
      return this;
   }

   public SubMapBuilder<K, V> setTop (int top)
   {
      if (top >= 0)
      {
         this.top = top;
      }
      return this;
   }

   /**
    * Adds a sort on the value set. Sorts are applied in the same order they are
    * given. Sorts will affect the order the results are produced by
    * Map.values().iterator().
    * 
    * @param sort the name of the field.
    * @param desc true if descending, ascending otherwise.
    * @return this
    */
   public SubMapBuilder<K, V> setOrderBy (OrderByExpression order)
   {
      this.orderBy = order;
      return this;
   }

   /**
    * Build a new SubMap with the given Filters, Sorts and Skip.
    * 
    * @return a new SubMap.
    */
   public abstract Map<K, V> build ();
}
