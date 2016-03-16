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

import org.apache.commons.collections4.Transformer;

/**
 * Usefull Utilities for the Java functional programming package.
 */
public class FunctionalTools
{
   /**
    * Compose 2 Transformers into 1.
    * @param <A> Input type.
    * @param <B> Transition type.
    * @param <C> Output type.
    * @param first firstly invoked Transformer.
    * @param second secondly invoked Transformer.
    * @return {@code second.transform(first.transform(A))}.
    */
   public static <A,B,C> Transformer<A,C> compose(Transformer<A,B> first,
         Transformer<? super B,C> second)
   {
      final Transformer<A,B> ffirst  = first;
      final Transformer<? super B,C> fsecond = second;
      return new Transformer<A,C>()
      {
         @Override
         public C transform(A u)
         {
            return fsecond.transform(ffirst.transform(u));
         }
      };
   }

}
