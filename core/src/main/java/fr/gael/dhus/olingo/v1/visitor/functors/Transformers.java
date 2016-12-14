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
            // subclasses of Number reimplement #equals(o) to compare instance types
            if (involvesNumbers(d))
            {
               return Number.class.cast(d.getA()).doubleValue() ==
                      Number.class.cast(d.getB()).doubleValue();
            }
            // dates can be instances of Date or Calendar
            if (involvesDates(d))
            {
               Duo<Calendar, Calendar> duo = asDuoOfCalendar(d);
               return duo.getA().equals(duo.getB());
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
   public static Transformer<Duo<Object, Object>, Boolean> gt()
   {
      return new Transformer<Duo<Object, Object>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Object, Object> d)
         {
            if (involvesNumbers(d))
            {
               return Number.class.cast(d.getA()).doubleValue() >
                      Number.class.cast(d.getB()).doubleValue();
            }
            if (involvesDates(d))
            {
               Duo<Calendar, Calendar> duo = asDuoOfCalendar(d);
               return duo.getA().after(duo.getB());
            }
            throw new IllegalStateException(notComparableMsg(d));
         }
      };
   }

   /** ge, greater or equals than. */
   public static Transformer<Duo<Object, Object>, Boolean> ge()
   {
      return new Transformer<Duo<Object, Object>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Object, Object> u)
         {
            if (involvesNumbers(u))
            {
            return Number.class.cast(u.getA()).doubleValue() >=
                   Number.class.cast(u.getB()).doubleValue();
            }
            if (involvesDates(u))
            {
               Duo<Calendar, Calendar> duo = asDuoOfCalendar(u);
               return duo.getA().after(duo.getB()) || duo.getA().equals(duo.getB());
            }
            throw new IllegalStateException(notComparableMsg(u));
         }
      };
   }

   /** lt, lower than. */
   public static <T> Transformer<Duo<Object, Object>, Boolean> lt()
   {
      return new Transformer<Duo<Object, Object>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Object, Object> u)
         {
            if (involvesNumbers(u))
            {
            return Number.class.cast(u.getA()).doubleValue() <
                   Number.class.cast(u.getB()).doubleValue();
            }
            if (involvesDates(u))
            {
               Duo<Calendar, Calendar> duo = asDuoOfCalendar(u);
               return duo.getA().before(duo.getB());
            }
            throw new IllegalStateException(notComparableMsg(u));
         }
      };
   }

   /** le, lower or equals than. */
   public static <T> Transformer<Duo<Object, Object>, Boolean> le()
   {
      return new Transformer<Duo<Object, Object>, Boolean>()
      {
         @Override
         public Boolean transform(Duo<Object, Object> u)
         {
            if (involvesNumbers(u))
            {
            return Number.class.cast(u.getA()).doubleValue() <=
                   Number.class.cast(u.getB()).doubleValue();
            }
            if (involvesDates(u))
            {
               Duo<Calendar, Calendar> duo = asDuoOfCalendar(u);
               return duo.getA().before(duo.getB()) || duo.getA().equals(duo.getB());
            }
            throw new IllegalStateException(notComparableMsg(u));
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
            if (u == null || u.getA() == null)
            {
               return false;
            }
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
            if (u == null || u.getA() == null)
            {
               return false;
            }
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
            if (u == null || u.getA() == null)
            {
               return false;
            }
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
            if (u == null)
            {
               return 0;
            }
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
            if (u == null || u.getA() == null)
            {
               return -1;
            }
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
            if (u == null || u.getA() == null)
            {
               return null;
            }
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
            if (u == null || u.getA() == null)
            {
               return null;
            }
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
            if (u == null || u.getA() == null)
            {
               return null;
            }
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
            if (u == null)
            {
               return null;
            }
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
            if (u == null)
            {
               return null;
            }
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
            if (u == null)
            {
               return null;
            }
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

   /**
    * Return true if <strong>BOTH</strong> A and B are instances of {@link Number}.
    * @param d to test.
    * @return true if A and B can be cast to Number.
    */
   private static boolean involvesNumbers(Duo<?, ?> d)
   {
      return Number.class.isAssignableFrom(d.getA().getClass()) &&
             Number.class.isAssignableFrom(d.getB().getClass());
   }

   /**
    * Returns true if A or B is a {@link Calendar} or a {@link Date}.
    * @param d to test.
    * @return true if the given Duo involves a date type.
    */
   private static boolean involvesDates(Duo<?, ?> d)
   {
      return Calendar.class.isAssignableFrom(d.getA().getClass()) ||
             Calendar.class.isAssignableFrom(d.getB().getClass()) ||

                 Date.class.isAssignableFrom(d.getA().getClass()) ||
                 Date.class.isAssignableFrom(d.getB().getClass());
   }

   /**
    * Converts the given object to a {@link Calendar}.
    * @param o to convert to Calendar.
    * @throws IllegalArgumentException parameter `o` cannot be converted into a Calendar.
    */
   private static Calendar castToCalendar(Object o) throws IllegalArgumentException
   {
      Calendar res;

      Class<?> ocls = o.getClass();
      if (Calendar.class.isAssignableFrom(ocls))
      {
         res = Calendar.class.cast(o);
      }
      else if (Date.class.isAssignableFrom(ocls))
      {
         res = Calendar.getInstance();
         res.setTime(Date.class.cast(o));
      }
      else if (Long.class.isAnnotation())
      {
         res = Calendar.getInstance();
         res.setTimeInMillis(Long.class.cast(o));
      }
      else
      {
         throw new IllegalArgumentException("Cannot create a Calendar from an instance of " + ocls);
      }

      return res;
   }

   /**
    * Returns a {@link Duo} of {@link Calendar}s from the given Duo.
    * @param d to convert.
    * @return a Duo of Calendars.
    */
   private static Duo<Calendar, Calendar> asDuoOfCalendar(Duo<?, ?> d)
   {
      return new Duo<>(castToCalendar(d.getA()), castToCalendar(d.getB()));
   }

   private static String notComparableMsg(Duo<?, ?> d)
   {
      return String.format("Instances of %s and %s are not comparable",
            d.getA().getClass(), d.getB().getClass());
   }
}
