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
package fr.gael.dhus.olingo;

import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.expression.BinaryExpression;
import org.apache.olingo.odata2.api.uri.expression.BinaryOperator;
import org.apache.olingo.odata2.api.uri.expression.ExpressionVisitor;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.LiteralExpression;
import org.apache.olingo.odata2.api.uri.expression.MemberExpression;
import org.apache.olingo.odata2.api.uri.expression.MethodExpression;
import org.apache.olingo.odata2.api.uri.expression.MethodOperator;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderExpression;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;
import org.apache.olingo.odata2.api.uri.expression.SortOrder;
import org.apache.olingo.odata2.api.uri.expression.UnaryExpression;
import org.apache.olingo.odata2.api.uri.expression.UnaryOperator;

/**
 * Implements the ExpressionVisitor interface to build SQL expressions from
 * Olingo expression trees. visitFilterExpression builds the WHERE clause (not
 * prefixed with the WHERE statement). visitOrderByExpression builds the ORDER
 * BY clause (not prefixed with the ORDER BY statement). You must implement the
 * <code>visitProperty</code> method which highly depends on the EDM.
 * 
 * @see http://olingo.apache.org/doc/tutorials/
 *      Olingo_Tutorial_AdvancedRead_FilterVisitor.html
 */
public abstract class OlingoSQLVisitor implements ExpressionVisitor
{

   /** Builds the WHERE clause (not prefixed with the WHERE statement). */
   @Override
   public Object visitFilterExpression (FilterExpression filterExpression,
      String expressionString, Object expression)
   {
      return expression;
   }

   /** Builds the ORDER BY clause (not prefixed with the ORDER BY statement). */
   @Override
   public Object visitOrderByExpression (OrderByExpression orderByExpression,
      String expressionString, List<Object> orders)
   {
      StringBuilder sb = new StringBuilder ();

      sb.append (orders.get (0));

      for (int i = 1; i < orders.size (); i++)
      {
         sb.append (", ");
         sb.append (orders.get (i));
      }

      return sb.toString ();
   }

   /** Called for each fields in the $orderby param. */
   @Override
   public Object visitOrder (OrderExpression orderExpression,
      Object filterResult, SortOrder sortOrder)
   {
      if (sortOrder == SortOrder.desc)
      {
         return filterResult + " DESC";
      }

      return filterResult;
   }

   /** Binary Operators. */
   @Override
   public Object visitBinary (BinaryExpression binaryExpression,
      BinaryOperator operator, Object leftSide, Object rightSide)
   {
      String sqlOperator = null;
      switch (operator)
      {
         case EQ:
            sqlOperator = "=";
            break;
         case NE:
            sqlOperator = "<>";
            break;
         case GT:
            sqlOperator = ">";
            break;
         case GE:
            sqlOperator = ">=";
            break;
         case LT:
            sqlOperator = "<";
            break;
         case LE:
            sqlOperator = "<=";
            break;
         case AND:
            sqlOperator = "AND";
            break;
         case OR:
            sqlOperator = "OR";
            break;
         default:
            // Other operators are not supported for SQL Statements
            throw new UnsupportedOperationException ("Unsupported operator: " +
               operator.toUriLiteral ());
      }
      // return the binary statement
      return "(" + leftSide + " " + sqlOperator + " " + rightSide + ")";
   }

   /** Unary operator. */
   @Override
   public Object visitUnary (UnaryExpression unaryExpression,
      UnaryOperator operator, Object operand)
   {
      switch (operator)
      {
         case MINUS:
            return "-" + operand;
         case NOT:
            return "NOT " + operand;
      }
      throw new UnsupportedOperationException ("Unsupported operator: " +
         operator.toUriLiteral ());
   }

   /** A constant. */
   @Override
   public Object visitLiteral (LiteralExpression literal, EdmLiteral edmLiteral)
   {
      if (edmLiteral.getType ().equals (
         EdmSimpleTypeKind.String.getEdmSimpleTypeInstance ()))
      {
         // Prevent sql injection
         if (edmLiteral.getLiteral ().contains ("'"))
         {
            throw new IllegalArgumentException (
               "SQL injection through the OData API is not allowed !");
         }
         return "'" + edmLiteral.getLiteral () + "'";
      }
      else
         if (edmLiteral.getType ().equals (
            EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance ()))
         {
            return "'" + edmLiteral.getLiteral ().replace ("T", " ") + "'";
         }
         else
         {
            return "'" + edmLiteral.getLiteral () + "'";
         }
   }

   /** Translates to an SQL function. */
   @Override
   public Object visitMethod (MethodExpression methodExpression,
      MethodOperator method, List<Object> parameters)
   {
      StringBuilder sb = new StringBuilder ();

      // One parameter methods
      if (parameters.size () == 1)
      {
         switch (method)
         {
            case CEILING:
               sb.append ("CEILING(");
               break;
            case DAY:
               sb.append ("DAYOFMONTH(");
               break;
            case FLOOR:
               sb.append ("FLOOR(");
               break;
            case HOUR:
               sb.append ("HOUR(");
               break;
            case LENGTH:
               sb.append ("LENGTH(");
               break;
            case MINUTE:
               sb.append ("MINUTE(");
               break;
            case MONTH:
               sb.append ("MONTH(");
               break;
            case ROUND:
               sb.append ("ROUND(");
               break;
            case SECOND:
               sb.append ("SECOND(");
               break;
            case TOUPPER:
               sb.append ("UPPER(");
               break;
            case TOLOWER:
               sb.append ("LOWER(");
               break;
            case TRIM:
               sb.append ("TRIM(");
               break;
            case YEAR:
               sb.append ("YEAR(");
               break;
            default:
               throw new UnsupportedOperationException ("Unsupported method: " +
                  method.toUriLiteral ());
         }
         sb.append (parameters.get (0));
         sb.append (')');
      }

      // Other methods
      else
         if (method == MethodOperator.INDEXOF ||
            method == MethodOperator.CONCAT ||
            method == MethodOperator.SUBSTRING)
         {
            switch (method)
            {
               case INDEXOF:
                  sb.append ("LOCATE(");
                  break;
               case CONCAT:
                  sb.append ("CONCAT(");
                  break;
               case SUBSTRING:
                  sb.append ("SUBSTR(");
                  break;
               default:
            }

            sb.append (parameters.get (0));
            sb.append (',');
            sb.append (parameters.get (1));
            sb.append (')');
         }

         else
         {
            switch (method)
            {
               case ENDSWITH:
                  sb.append (parameters.get (0));
                  sb.append (" LIKE '%");
                  sb.append (removeApostrophes (parameters.get (1)));
                  sb.append ("'");
                  break;
               case STARTSWITH:
                  sb.append (parameters.get (0));
                  sb.append (" LIKE '");
                  sb.append (removeApostrophes (parameters.get (1)));
                  sb.append ("%'");
                  break;
               case SUBSTRINGOF:
                  sb.append (parameters.get (1));
                  sb.append (" LIKE '%");
                  sb.append (removeApostrophes (parameters.get (0)));
                  sb.append ("%'");
                  break;
               default:
                  throw new UnsupportedOperationException (
                     "Unsupported method: " + method.toUriLiteral ());
            }
         }

      return sb.toString ();
   }

   @Override
   public Object visitMember (MemberExpression memberExpression, Object path,
      Object property)
   {
      /**
       * the property shall be handled inside visitProperty method in
       * ODataSQLVisitor implementation
       **/
      return property;
   }

   /** Returns the field name corresponding to the given EDM type. */
   @Override
   public abstract Object visitProperty (PropertyExpression propertyExpression,
      String uriLiteral, EdmTyped edmProperty);

   /** Removes the first and last character if they are apostrophes. */
   private static String removeApostrophes (Object o)
   {
      String s = String.class.cast (o);
      if (s.charAt (0) == '\'' && s.charAt (s.length () - 1) == '\'')
         return s.substring (1, s.length () - 1);
      return s;
   }
}
