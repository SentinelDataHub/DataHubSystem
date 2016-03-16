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
package fr.gael.dhus.olingo.v1.visitor.functors;

import fr.gael.dhus.util.functional.FunctionalTools;
import fr.gael.dhus.util.functional.tuple.Duo;
import fr.gael.dhus.util.functional.tuple.Trio;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.collections4.Transformer;

/**
 * OData operators and functions.
 *
 * See http://www.odata.org/documentation/odata-version-2-0/uri-conventions/#FilterSystemQueryOption
 *
 * @see fr.gael.dhus.util.functional
 */
public class Transformers
{
   // Logical Operators

   /** eq, equals. */
   public static Transformer<Duo<Object, Object>, Boolean> eq()
   {
      return new Transformer<Duo<Object, Object>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Object, Object> d)
         {
            if (d.getA() == null || d.getB() == null)
            {
               return d.getA() == d.getB();
            }
            return d.getA().equals(d.getB());
         }
      };
   }

   /** ne, not equals. */
   public static Transformer<Duo<Object, Object>, Boolean> ne()
   {
      return FunctionalTools.compose(eq(), not());
   }

   /** gt, greater than. */
   public static Transformer<Duo<Number, Number>, Boolean> gt()
   {
      return new Transformer<Duo<Number, Number>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Number, Number> d)
         {
            return d.getA().doubleValue() > d.getB().doubleValue();
         }
      };
   }

   /** ge, greater or equals than. */
   public static Transformer<Duo<Number, Number>, Boolean> ge()
   {
      return new Transformer<Duo<Number, Number>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Number, Number> u)
         {
            return u.getA().doubleValue() >= u.getB().doubleValue();
         }
      };
   }

   /** lt, lower than. */
   public static <T> Transformer<Duo<Number, Number>, Boolean> lt()
   {
      return new Transformer<Duo<Number, Number>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Number, Number> u)
         {
            return u.getA().doubleValue() < u.getB().doubleValue();
         }
      };
   }

   /** le, lower or equals than. */
   public static <T> Transformer<Duo<Number, Number>, Boolean> le()
   {
      return new Transformer<Duo<Number, Number>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Number, Number> u)
         {
            return u.getA().doubleValue() <= u.getB().doubleValue();
         }
      };
   }

   /** and. */
   public static Transformer<Duo<Boolean, Boolean>, Boolean> and()
   {
      return new Transformer<Duo<Boolean, Boolean>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Boolean, Boolean> u)
         {
            return u.getA() && u.getB();
         }
      };
   }

   /** or. */
   public static Transformer<Duo<Boolean, Boolean>, Boolean> or()
   {
      return new Transformer<Duo<Boolean, Boolean>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Boolean, Boolean> u)
         {
            return u.getA() || u.getB();
         }
      };
   }

   /** not. */
   public static Transformer<Boolean, Boolean> not()
   {
      return new Transformer<Boolean, Boolean>()
      {
         @Override
         public Boolean transform(Boolean u)
         {
            return !u;
         }
      };
   }

   // Arithmetic operators

   /** unary minus. */
   public static Transformer<Number, Number> minus()
   {
      return new Transformer<Number, Number>()
      {
         @Override
         public Number transform(Number u)
         {
            return -(u.doubleValue());
         }
      };
   }

   /** add. */
   public static Transformer<Duo<Number, Number>, Number> add()
   {
      return new Transformer<Duo<Number, Number>, Number>()
      {

         @Override
         public Number transform(Duo<Number, Number> u)
         {
            return u.getA().doubleValue() + u.getB().doubleValue();
         }
      };
   }

   /** sub. */
   public static Transformer<Duo<Number, Number>, Number> sub()
   {
      return new Transformer<Duo<Number, Number>, Number>()
      {

         @Override
         public Number transform(Duo<Number, Number> u)
         {
            return u.getA().doubleValue() - u.getB().doubleValue();
         }
      };
   }

   /** mul. */
   public static Transformer<Duo<Number, Number>, Number> mul()
   {
      return new Transformer<Duo<Number, Number>, Number>()
      {

         @Override
         public Number transform(Duo<Number, Number> u)
         {
            return u.getA().doubleValue() * u.getB().doubleValue();
         }
      };
   }

   /** div. */
   public static Transformer<Duo<Number, Number>, Number> div()
   {
      return new Transformer<Duo<Number, Number>, Number>()
      {

         @Override
         public Number transform(Duo<Number, Number> u)
         {
            return u.getA().doubleValue() / u.getB().doubleValue();
         }
      };
   }

   /** mod. */
   public static Transformer<Duo<Long, Long>, Long> mod()
   {
      return new Transformer<Duo<Long, Long>, Long>()
      {
         @Override
         public Long transform(Duo<Long, Long> u)
         {
            return u.getA() % u.getB();
         }
      };
   }

   // String Functions

   /** substringof. */
   public static Transformer<Duo<String, String>, Boolean> substringof()
   {
      return new Transformer<Duo<String, String>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<String, String> u)
         {
            return u.getA().contains(u.getB());
         }
      };
   }

   /** endswith. */
   public static Transformer<Duo<String, String>, Boolean> endswith()
   {
      return new Transformer<Duo<String, String>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<String, String> u)
         {
            return u.getA().endsWith(u.getB());
         }
      };
   }

   /** startswith. */
   public static Transformer<Duo<String, String>, Boolean> startswith()
   {
      return new Transformer<Duo<String, String>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<String, String> u)
         {
            return u.getA().startsWith(u.getB());
         }
      };
   }

   /** length. */
   public static Transformer<String, Integer> length()
   {
      return new Transformer<String, Integer>()
      {
         @Override
         public Integer transform(String u)
         {
            return u.length();
         }
      };
   }

   /** indexof. */
   public static Transformer<Duo<String, String>, Integer> indexof()
   {
      return new Transformer<Duo<String, String>, Integer>()
      {
         @Override
         public Integer transform(Duo<String, String> u)
         {
            return u.getA().indexOf(u.getB());
         }
      };
   }

   /** replace. */
   public static Transformer<Trio<String, String, String>, String> replace()
   {
      return new Transformer<Trio<String, String, String>, String>()
      {
         @Override
         public String transform(Trio<String, String, String> u)
         {
            return u.getA().replaceFirst(u.getB(), u.getC());
         }
      };
   }

   /** substring(string, int).  */
   public static Transformer<Duo<String, Integer>, String> substring()
   {
      return new Transformer<Duo<String, Integer>, String>()
      {
         @Override
         public String transform(Duo<String, Integer> u)
         {
            return u.getA().substring(u.getB());
         }
      };
   }

   /** substring(string, int, int). */
   public static Transformer<Trio<String, Integer, Integer>, String> substring2()
   {
      return new Transformer<Trio<String, Integer, Integer>, String>()
      {

         @Override
         public String transform(Trio<String, Integer, Integer> u)
         {
            return u.getA().substring(u.getB(), u.getC() - u.getB());
         }
      };
   }

   /** tolower. */
   public static Transformer<String, String> tolower()
   {
      return new Transformer<String, String>()
      {
         @Override
         public String transform(String u)
         {
            return u.toLowerCase();
         }
      };
   }

   /** toupper. */
   public static Transformer<String, String> toupper()
   {
      return new Transformer<String, String>()
      {
         @Override
         public String transform(String u)
         {
            return u.toUpperCase();
         }
      };
   }

   /** trim. */
   public static Transformer<String, String> trim()
   {
      return new Transformer<String, String>()
      {
         @Override
         public String transform(String u)
         {
            return u.trim();
         }
      };
   }

   /** concat. */
   public static Transformer<Duo<String, String>, String> concat()
   {
      return new Transformer<Duo<String, String>, String>()
      {
         @Override
         public String transform(Duo<String , String> v)
         {
            return v.getA()+v.getB();
         }
      };
   }

   // Date Functions

   /** day. */
   public static Transformer<Date, Integer> day()
   {
      return new Transformer<Date, Integer>()
      {
         @Override
         public Integer transform(Date u)
         {
            Calendar c = Calendar.getInstance();
            c.setTime(u);
            return c.get(Calendar.DAY_OF_MONTH);
         }
      };
   }

   /** hour. */
   public static Transformer<Date, Integer> hour()
   {
      return new Transformer<Date, Integer>()
      {
         @Override
         public Integer transform(Date u)
         {
            Calendar c = Calendar.getInstance();
            c.setTime(u);
            return c.get(Calendar.HOUR_OF_DAY);
         }
      };
   }

   /** minute. */
   public static Transformer<Date, Integer> minute()
   {
      return new Transformer<Date, Integer>()
      {
         @Override
         public Integer transform(Date u)
         {
            Calendar c = Calendar.getInstance();
            c.setTime(u);
            return c.get(Calendar.MINUTE);
         }
      };
   }

   /** month. */
   public static Transformer<Date, Integer> month()
   {
      return new Transformer<Date, Integer>()
      {
         @Override
         public Integer transform(Date u)
         {
            Calendar c = Calendar.getInstance();
            c.setTime(u);
            return c.get(Calendar.MONTH);
         }
      };
   }

   /** second. */
   public static Transformer<Date, Integer> second()
   {
      return new Transformer<Date, Integer>()
      {
         @Override
         public Integer transform(Date u)
         {
            Calendar c = Calendar.getInstance();
            c.setTime(u);
            return c.get(Calendar.SECOND);
         }
      };
   }

   /** year. */
   public static Transformer<Date, Integer> year()
   {
      return new Transformer<Date, Integer>()
      {
         @Override
         public Integer transform(Date u)
         {
            Calendar c = Calendar.getInstance();
            c.setTime(u);
            return c.get(Calendar.YEAR);
         }
      };
   }

   // Math functions

   /** round. */
   public static Transformer<Double, Double> round()
   {
      return new Transformer<Double, Double>()
      {
         @Override
         public Double transform(Double u)
         {
            return (double)Math.round(u);
         }
      };
   }

   /** floor. */
   public static Transformer<Double, Double> floor()
   {
      return new Transformer<Double, Double>()
      {
         @Override
         public Double transform(Double u)
         {
            return (double)Math.floor(u);
         }
      };
   }

   /** ceiling. */
   public static Transformer<Double, Double> ceiling()
   {
      return new Transformer<Double, Double>()
      {
         @Override
         public Double transform(Double u)
         {
            return (double)Math.ceil(u);
         }
      };
   }

   // IsOf functions are not yet implemented in Olingo2
}
