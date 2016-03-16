/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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
package fr.gael.dhus.olingo.v1.map.impl;

import fr.gael.dhus.database.object.restriction.AccessRestriction;
import fr.gael.dhus.olingo.v1.entity.Restriction;
import fr.gael.dhus.olingo.v1.map.AbstractDelegatingMap;
import fr.gael.dhus.olingo.v1.map.SubMap;
import fr.gael.dhus.olingo.v1.map.SubMapBuilder;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestrictionMap extends AbstractDelegatingMap<Long, Restriction>
      implements SubMap<Long, Restriction>
{
   private final fr.gael.dhus.database.object.User dhus_user;

   private UserService userService =
         ApplicationContextProvider.getBean (UserService.class);

   public RestrictionMap (String username)
   {
      this.dhus_user = userService.getUserNoCheck (username);
   }

   @Override
   protected Restriction serviceGet (Long key)
   {
      Set<AccessRestriction> restrictions = dhus_user.getRestrictions ();
      for (AccessRestriction restriction : restrictions)
      {
         if (restriction.getId ().equals (key))
         {
            return new Restriction (restriction);
         }
      }
      return null;
   }

   @Override
   protected Iterator<Restriction> serviceIterator ()
   {
      List<Restriction> restrictions = new ArrayList<> ();
      List<AccessRestriction> access_restrictions =
            new ArrayList<> (dhus_user.getRestrictions ());

      for (AccessRestriction access_restriction : access_restrictions)
      {
         restrictions.add (new Restriction (access_restriction));
      }

      return restrictions.iterator ();
   }

   @Override
   protected int serviceCount ()
   {
      return dhus_user.getRestrictions ().size ();
   }

   @Override
   public SubMapBuilder<Long, Restriction> getSubMapBuilder ()
   {
      return new SubMapBuilder<Long, Restriction> ()
      {
         @Override
         public Map<Long, Restriction> build ()
         {
            return RestrictionMap.this;
         }
      };
   }

   static class RestrictionComparatorId
         implements Comparator<AccessRestriction>
   {
      @Override
      public int compare (AccessRestriction o1, AccessRestriction o2)
      {
         return (o1.getId ().intValue () - o2.getId ().intValue ());
      }
   }

   static class RestrictionComparatorReason
         implements Comparator<AccessRestriction>
   {
      @Override
      public int compare (AccessRestriction o1, AccessRestriction o2)
      {
         return o1.getBlockingReason ().compareTo (o2.getBlockingReason ());
      }
   }
}
