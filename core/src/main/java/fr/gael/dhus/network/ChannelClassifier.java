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
package fr.gael.dhus.network;

import java.util.Vector;

class ChannelClassifier
{
   private final Vector<ChannelClassifierRules> includes;
   private final Vector<ChannelClassifierRules> excludes;

   ChannelClassifier()
   {
      this.includes = new Vector<ChannelClassifierRules>();
      this.excludes = new Vector<ChannelClassifierRules>();
   }

   void addIncludeRules(final ChannelClassifierRules rule)
   {
      this.includes.add(rule);
   }

   void addExcludeRules(final ChannelClassifierRules rule)
   {
      this.excludes.add(rule);
   }

   boolean complyWith(ConnectionParameters parameters)
         throws IllegalArgumentException
   {
      // Check input parameter
      if (parameters == null)
      {
         throw new IllegalArgumentException("Cannot check classifier against"
               + " a null set of connextion parameters.");
      }

      // Return false immediately when parameters match any exclusion rule
      for (ChannelClassifierRules exclude_rules : this.excludes)
      {
         if (exclude_rules.complyWith(parameters))
         {
            return false;
         }
      }

      // Return true if no inclusion rules is defined
      if (this.includes.size() <= 0)
      {
         return true;
      }

      // Check that at least one inclusion set of rules if matched
      for (ChannelClassifierRules include_rules : this.includes)
      {
         if (include_rules.complyWith(parameters))
         {
            return true;
         }
      }

      // Return false
      return false;
   }
   
   @Override
   public String toString()
   {
      String message = "";
      
      if (this.includes.size() > 0)
      {
         message += "Includes";
      }

      for (ChannelClassifierRules rules: this.includes)
      {
         message += " {" + rules.toString() + "};";
      }

      if (this.excludes.size() > 0)
      {
         message += " Excludes";
      }

      for (ChannelClassifierRules rules: this.excludes)
      {
         message += " {" + rules.toString() + "};";
      }
      
      return message;
   }
}
