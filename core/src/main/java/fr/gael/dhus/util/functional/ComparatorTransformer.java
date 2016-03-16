/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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
package fr.gael.dhus.util.functional;

import fr.gael.dhus.util.functional.tuple.Duo;

import org.apache.commons.collections4.Transformer;

/**
 * Comparator as a Transformer accepting a Duo<? extends Comparable, ? extends Comparable>
 * as input.
 */
public class ComparatorTransformer
      implements Transformer<Duo<? extends Comparable, ? extends Comparable>, Integer>
{
   private final boolean reverse;

   /**
    * Creates a new ComparatorTransformer in ascending order.
    */
   public ComparatorTransformer()
   {
      this.reverse = false;
   }

   /**
    * Create a new ComparatorTransformer.
    * @param reverse if `true`, reverse parameters (descending order).
    */
   public ComparatorTransformer(boolean reverse)
   {
      this.reverse = reverse;
   }

   @Override
   public Integer transform(Duo<? extends Comparable, ? extends Comparable> params)
   {
      if (params.getA() == null && params.getB() == null)
      {
         return 0;
      }

      if (this.reverse)
      {
         if (params.getB() == null)
         {
            return - params.getA().compareTo(null);
         }
         return params.getB().compareTo(params.getA());
      }

      if (params.getA() == null)
      {
         return - params.getB().compareTo(null);
      }
      return params.getA().compareTo(params.getB());
   }
}
