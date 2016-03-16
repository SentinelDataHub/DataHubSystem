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

import fr.gael.dhus.olingo.OlingoFunctionalVisitor;
import fr.gael.dhus.olingo.v1.entity.Connection;
import fr.gael.dhus.olingo.v1.entityset.ConnectionEntitySet;
import fr.gael.dhus.util.functional.ComparatorTransformer;

import java.util.Date;

import org.apache.commons.collections4.Transformer;

import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.expression.OrderExpression;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;
import org.apache.olingo.odata2.api.uri.expression.SortOrder;

/**
 * Implements the `visitProperty` method of abstract class OlingoFunctionalVisitor.
 * Allows us to convert an OlingoExpressionTree to an ExecutableExpressionTree using the
 * OlingoFunctionalVisitor.
 */
public class ConnectionFunctionalVisitor extends OlingoFunctionalVisitor
{

   @Override
   public Object visitProperty(PropertyExpression pe, String uri_literal, EdmTyped edm_property)
   {
      // Returns Transformer<Connection, Object> to provide the requested property `uri_literal`.
      Transformer<Connection, ? extends Object> res;
      switch (uri_literal)
      {
         case ConnectionEntitySet.ID:       res = new IdProvider();       break;
         case ConnectionEntitySet.DATE:     res = new DateProvider();     break;
         case ConnectionEntitySet.REMOTEIP: res = new RemoteIpProvider(); break;
         case ConnectionEntitySet.REQUEST:  res = new RequestProvider();  break;
         case ConnectionEntitySet.DURATION: res = new DurationProvider(); break;

         default: throw new UnsupportedOperationException("Unknown property: " + uri_literal);
      }

      return ExecutableExpressionTree.Node.createLeave(res);
   }

   @Override
   public Object visitOrder(OrderExpression oe, Object filter, SortOrder sort_order)
   {
      ExecutableExpressionTree.Node param = ExecutableExpressionTree.Node.class.cast(filter);
      Transformer cmp = new ComparatorTransformer(sort_order == SortOrder.desc);

      return ExecutableExpressionTree.Node.createDuoNode(cmp, param, param);
   }

   // Connection to Id function
   private static class IdProvider implements Transformer<Connection, String>
   {
      @Override
      public String transform(Connection u)
      {
         return u.getUUID().toString();
      }
   }

   // Connection to Date function
   private static class DateProvider implements Transformer<Connection, Date>
   {
      @Override
      public Date transform(Connection u)
      {
         return u.getStartDate();
      }
   }

   // Connection to RemoteIp
   private static class RemoteIpProvider implements Transformer<Connection, String>
   {
      @Override
      public String transform(Connection u)
      {
         return u.getRemoteAddress();
      }
   }

   // Connection to Request
   private static class RequestProvider implements Transformer<Connection, String>
   {
      @Override
      public String transform(Connection u)
      {
         return u.getRequest();
      }
   }

   // Connection to Duration
   private static class DurationProvider implements Transformer<Connection, Double>
   {
      @Override
      public Double transform(Connection u)
      {
         return u.getDurationMs();
      }
   }
}
