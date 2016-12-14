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
package fr.gael.dhus.util.functional.collect;

import fr.gael.dhus.util.functional.tuple.Duo;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.Transformer;

/**
 * A read-only view on an input map with transformed entries.
 * @param <K> Key type.
 * @param <Vi> Input Value type.
 * @param <Vo> Output Value type.
 */
public class TransformedMap<K,Vi,Vo> extends AbstractMap<K,Vo>
{

   private final Map<K,Vi> input;
   private final Transformer<Duo<K,Vi>,Vo> transformer;

   public TransformedMap(Map<K,Vi> input, Transformer<Duo<K,Vi>,Vo> transformer)
   {
      Objects.requireNonNull(input, "input map must not be null");
      Objects.requireNonNull(transformer, "transformer must not be null");

      this.input = input;
      this.transformer = transformer;
   }

   @Override
   public Vo get(Object key)
   {
      return transformer.transform(new Duo<>((K)key, input.get(key)));
   }

   @Override
   public Set<Entry<K,Vo>> entrySet()
   {
      return new EntrySet();
   }

   private class EntrySet extends AbstractSet<Entry<K,Vo>>
   {

      @Override
      public Iterator<Entry<K,Vo>> iterator()
      {
         final Iterator<Entry<K,Vi>> it = input.entrySet().iterator();
         return new Iterator<Entry<K,Vo>>()
         {
            @Override
            public boolean hasNext()
            {
               return it.hasNext();
            }

            @Override
            public Entry<K, Vo> next()
            {
               Entry<K,Vi> in = it.next();
               Vo out = transformer.transform(new Duo<>(in.getKey(), in.getValue()));
               SimpleImmutableEntry<K,Vo> ent = new SimpleImmutableEntry<>(in.getKey(), out);
               return ent;
            }

            @Override
            public void remove()
            {
               throw new UnsupportedOperationException("Read only.");
            }
         };
      }

      @Override
      public int size()
      {
         return input.size();
      }
   }
}
