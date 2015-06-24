package fr.gael.dhus.util;

import java.util.Iterator;

public class CheckIterator
{

   /**
    * Checks number of elements within a iterator.
    * @param it iterator to check
    * @param n number of elements found
    * @return true if n equals elements number in iterator, otherwise false.
    */
   public static boolean checkElementNumber (Iterator<?> it, int n)
   {
      int i = 0;      
      while (it.hasNext ())
      {
         if (it.next () == null)
         {
            return false;
         }
         i++;
      }
      return (i == n);
   }
}
