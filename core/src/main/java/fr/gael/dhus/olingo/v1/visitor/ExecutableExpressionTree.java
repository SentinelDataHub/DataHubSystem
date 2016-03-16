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
package fr.gael.dhus.olingo.v1.visitor;

import fr.gael.dhus.util.functional.tuple.Duo;
import fr.gael.dhus.util.functional.tuple.Trio;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.Transformer;

/**
 * An executable expression tree to filter Collections.
 *
 * @param <T> type of elements from the collection being filtered.
 */
public class ExecutableExpressionTree<T>
{
   private final Node<T,Boolean> rootNode;

   public ExecutableExpressionTree(Node<T,Boolean> root_node)
   {
      this.rootNode = root_node;
   }

   public Object exec(T t)
   {
      return rootNode.exec(t);
   }

   /**
    * A node of the Expression Tree.
    * @param <T> accepted type of exec(T), type of the Collection entry.
    * @param <R> returned type of exec(T).
    */
   public static abstract class Node<T,R>
   {
      /**
       * Executes this Node with the given data.
       * @param element input.
       * @return output.
       */
      public abstract R exec(T element);

      /**
       * Creates a dynamic leave from a Transformer.
       * @param <T> Type of the Collection entry.
       * @param <R> Return type.
       * @param p Transformer that provides us data from the collection entry.
       * @return a new Node.
       */
      public static <T,R> Node<T,R> createLeave(final Transformer<T,R> p)
      {
         return new Node<T,R>()
         {
            @Override
            public R exec(T element)
            {
               return p.transform(element);
            }
         };
      }

      /**
       * Creates a constant leave from a Factory.
       * @param <T> Type of the Collection entry.
       * @param <R> Return type.
       * @param p a Provider.
       * @return a new Node.
       */
      public static <T,R> Node<T,R> createLeave(final Factory<R> p)
      {
         return new Node<T,R>()
         {
            @Override
            public R exec(T element)
            {
               return p.create();
            }
         };
      }

      /**
       * Creates a Node from a Transformer.
       * @param <T> Type of the Collection entry.
       * @param <R> Return type.
       * @param t a transformer that accepts a <?> and returns a <?>.
       * @param sub Node at `depth+1` in the tree.
       * @return a new Node.
       */
      public static <T,R> Node<T,R> createNode(final Transformer<Object,R> t, final Node<T,?> sub)
      {
         return new Node<T,R>()
         {
            @Override
            public R exec(T element)
            {
               return t.transform(sub.exec(element));
            }
         };
      }

      /**
       * Creates a Node from a Transformer<Duo<?,?>,?>.
       * @param <T> Type of the Collection entry.
       * @param <R> Return type.
       * @param t a bi-transformer that accepts a <?,?> and returns a <?>.
       * @param sub1 first  Node at `depth+1` in the tree, serves as  first parameter for `t`.
       * @param sub2 second Node at `depth+1` in the tree. serves as second parameter for `t`.
       * @return a new Node.
       */
      public static <T,R> Node<T,R> createNode(final Transformer<Duo<Object,Object>,R> t,
            final Node<T,?> sub1, final Node<T,?> sub2)
      {
         return new Node<T,R>()
         {
            @Override
            public R exec(T element)
            {
               Object o1 = sub1.exec(element);
               Object o2 = sub2.exec(element);
               return t.transform(new Duo<>(o1, o2));
            }
         };
      }

      /**
       * Creates a Duo Node, it is a Node that takes a Duo<T,T> as input,
       * transforms `A` with `sub1`, `B` with `sub2` and return their
       * tranformation through given transformer `t`.
       *
       * @param <T> input type.
       * @param <R> return type.
       * @param t transformer for this Node.
       * @param sub1 first  Node at `depth+1` in the tree, serves as first  parameter for `t`.
       * @param sub2 second Node at `depth+1` in the tree, serves as second parameter for `t`.
       * @return a new Node.
       */
      public static <T,R> Node<Duo<T,T>,R> createDuoNode(final Transformer<Duo<Object,Object>,R> t,
            final Node<T,?> sub1, final Node<T,?> sub2)
      {
         return new Node<Duo<T,T>,R>()
         {
            @Override
            public R exec(Duo<T, T> element)
            {
               Object o1 = sub1.exec(element.getA());
               Object o2 = sub2.exec(element.getB());
               return t.transform(new Duo<>(o1, o2));
            }
         };
      }

      /**
       * Creates a Node from a Transformer<Trio<?,?,?>,?>.
       * @param <T> Type of the Collection entry.
       * @param <R> Return type.
       * @param t a tri-transformer that accepts a <?,?,?> and returns a <?>.
       * @param sub1 first  Node at `depth+1` in the tree.
       * @param sub2 second Node at `depth+1` in the tree.
       * @param sub3 third  Node at `depth+1` in the tree.
       * @return a new Node.
       */
      public static <T,R> Node<T,R> createNode(final Transformer<Trio<Object,Object,Object>,R> t,
            final Node<T,?> sub1, final Node<T,?> sub2, final Node<T,?> sub3)
      {
         return new Node<T,R>()
         {
            @Override
            public R exec(T element)
            {
               Object o1 = sub1.exec(element);
               Object o2 = sub2.exec(element);
               Object o3 = sub3.exec(element);
               return t.transform(new Trio<>(o1, o2, o3));
            }
         };
      }
   }
}
