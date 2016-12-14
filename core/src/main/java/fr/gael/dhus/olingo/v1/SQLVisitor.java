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
package fr.gael.dhus.olingo.v1;

import org.apache.olingo.odata2.api.edm.EdmLiteral;
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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Implements the ExpressionVisitor interface to build SQL expressions from
 * Olingo expression trees. visitFilterExpression builds the WHERE clause (not
 * prefixed with the WHERE statement). visitOrderByExpression builds the ORDER
 * BY clause (not prefixed with the ORDER BY statement). You must implement the
 * <code>visitProperty</code> method which highly depends on the EDM.
 *
 * @see http://olingo.apache.org/doc/tutorials/
 * Olingo_Tutorial_AdvancedRead_FilterVisitor.html
 */
public abstract class SQLVisitor implements ExpressionVisitor
{
   private final DetachedCriteria criteria;

   protected SQLVisitor(Class entity)
   {
      this.criteria = DetachedCriteria.forClass(entity);
   }

   /* Builds the WHERE clause (not prefixed with the WHERE statement). */
   @Override
   public Object visitFilterExpression(FilterExpression filter_expression,
         String expression_string, Object expression)
   {
      if (expression != null)
      {
         criteria.add((Criterion) expression);
      }
      return criteria;
   }

   /* Builds the ORDER BY clause (not prefixed with the ORDER BY statement). */
   @Override
   public Object visitOrderByExpression(OrderByExpression order_expression,
         String expression_string, List<Object> orders)
   {
      for (Object object: orders)
      {
         Order order = Order.class.cast(object);
         criteria.addOrder(order);
      }
      return criteria;
   }

   /* Called for each fields in the $orderby param. */
   @Override
   public Object visitOrder(OrderExpression order_expression,
         Object filter_result, SortOrder sort_order)
   {
      Order order;
      String property = ((Member) filter_result).getName();
      switch (sort_order)
      {
         case asc:
         {
            order = Order.asc(property);
            break;
         }
         case desc:
         {
            order = Order.desc(property);
            break;
         }
         default:
         {
            throw new UnsupportedOperationException("Unsupported order: " + sort_order);
         }
      }
      return order;
   }

   /* Binary Operators. */
   @Override
   public Object visitBinary(BinaryExpression binary_expression,
         BinaryOperator operator, Object left_side, Object right_side)
   {
      Criterion criterion;
      switch (operator)
      {
         case EQ:
         case NE:
         case GT:
         case GE:
         case LT:
         case LE:
         {
            criterion = getCriterionComparative(operator, left_side, right_side);
            break;
         }
         case AND:
         case OR:
         {
            Criterion left = (Criterion) left_side;
            Criterion right = (Criterion) right_side;
            criterion = getCriterionLogical(operator, left, right);
            break;
         }
         default:
            // Other operators are not supported for SQL Statements
            throw new UnsupportedOperationException("Unsupported operator: " +
                  operator.toUriLiteral());
      }
      // return the binary statement
      return criterion;
   }

   /* Unary operator. */
   @Override
   public Object visitUnary(UnaryExpression unary_expression,
         UnaryOperator operator, Object operand)
   {
      switch (operator)
      {
         case MINUS:
         {
            if (operand instanceof Long)
            {
               return -((Long) operand);
            }
            else if (operand instanceof Double)
            {
               return -((Double) operand);
            }
            else
            {
               throw new UnsupportedOperationException("Invalid expression: " +
                     unary_expression.getUriLiteral());
            }
         }
         case NOT:
         {
            return Restrictions.not((Criterion) operand);
         }
         default:
            break;
      }
      throw new UnsupportedOperationException("Unsupported operator: " +
            operator.toUriLiteral());
   }

   /* A constant. */
   @Override
   public Object visitLiteral(LiteralExpression literal, EdmLiteral edm_literal)
   {
      Object result;
      Class type = edm_literal.getType().getDefaultType();

      if (type.equals(Boolean.class))
      {
         result = Boolean.valueOf(edm_literal.getLiteral());
      }
      else if (type.equals(Byte.class)    || type.equals(Short.class) ||
               type.equals(Integer.class) || type.equals(Long.class))
      {
         result = Long.valueOf(edm_literal.getLiteral());
      }
      else if (type.equals(Double.class) || type.equals(BigDecimal.class))
      {
         result = Double.valueOf(edm_literal.getLiteral());
      }
      else if (type.equals(String.class))
      {
         result = edm_literal.getLiteral();
      }
      else if (type.equals(Calendar.class))
      {
         SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
         SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
         try
         {
            result = sdf1.parse(edm_literal.getLiteral());
         }
         catch (ParseException e)
         {
            try
            {
               result = sdf2.parse(edm_literal.getLiteral());
            }
            catch (ParseException e1)
            {
               throw new IllegalArgumentException("Invalid date format");
            }
         }
      }
      else
      {
         throw new IllegalArgumentException("Type " + edm_literal.getType() +
               " is not supported by the service");
      }

      return result;
   }

   /* Translates to an SQL function. */
   @Override
   public Object visitMethod(MethodExpression method_expression,
         MethodOperator method, List<Object> parameters)
   {
      Criterion criterion;
      switch (method)
      {
         // String functions
         case CONCAT:
         {
            criterion = Restrictions.sqlRestriction(
                  "CONCAT(?,?)",
                  new Object[]
                  {
                     parameters.get(0), parameters.get(1)
                  },
                  new Type[]
                  {
                     StandardBasicTypes.STRING, StandardBasicTypes.STRING
                  }
            );
            break;
         }
         case INDEXOF:
         {
            criterion = Restrictions.sqlRestriction(
                  "LOCATE(?,?)",
                  new Object[]
                  {
                     parameters.get(0), parameters.get(1)
                  },
                  new Type[]
                  {
                     StandardBasicTypes.STRING, StandardBasicTypes.STRING
                  }
            );
            break;
         }
         case LENGTH:
         {
            criterion = Restrictions.sqlRestriction(
                  "LENGTH(?)", parameters.get(0), StandardBasicTypes.STRING);
            break;
         }
         case SUBSTRING:
         {
            criterion = Restrictions.sqlRestriction(
                  "SUBSTR(?,?)",
                  new Object[]
                  {
                     parameters.get(0), parameters.get(1)
                  },
                  new Type[]
                  {
                     StandardBasicTypes.STRING, StandardBasicTypes.STRING
                  }
            );
            break;
         }
         case TOUPPER:
         {
            criterion = Restrictions.sqlRestriction(
                  "UPPER(?)", parameters.get(0), StandardBasicTypes.STRING);
            break;
         }
         case TOLOWER:
         {
            criterion = Restrictions.sqlRestriction(
                  "LOWER(?)", parameters.get(0), StandardBasicTypes.STRING);
            break;
         }
         case TRIM:
         {
            criterion = Restrictions.sqlRestriction(
                  "TRIM(?)", parameters.get(0), StandardBasicTypes.STRING);
            break;
         }
         case ENDSWITH:
         case STARTSWITH:
         {
            criterion = getCriterionFunction(method, parameters.get(0), parameters.get(1));
            break;
         }
         case SUBSTRINGOF:
         {
            criterion = getCriterionFunction(method, parameters.get(1), parameters.get(0));
            break;
         }

         // Date functions
         case DAY:
         {
            criterion = Restrictions.sqlRestriction("DAYOFMONTH(?)",
                  parameters.get(0), StandardBasicTypes.TIMESTAMP);
            break;
         }
         case HOUR:
         {
            criterion = Restrictions.sqlRestriction(
                  "HOUR(?)", parameters.get(0), StandardBasicTypes.TIMESTAMP);
            break;
         }
         case MINUTE:
         {
            criterion = Restrictions.sqlRestriction("MINUTE(?)",
                  parameters.get(0), StandardBasicTypes.TIMESTAMP);
            break;
         }
         case MONTH:
         {
            criterion = Restrictions.sqlRestriction("MONTH(?)",
                  parameters.get(0), StandardBasicTypes.TIMESTAMP);
            break;
         }
         case SECOND:
         {
            criterion = Restrictions.sqlRestriction("SECOND(?)",
                  parameters.get(0), StandardBasicTypes.TIMESTAMP);
            break;
         }
         case YEAR:
         {
            criterion = Restrictions.sqlRestriction(
                  "YEAR(?)", parameters.get(0), StandardBasicTypes.TIMESTAMP);
            break;
         }

         // Math functions
         case CEILING:
         {
            criterion = Restrictions.sqlRestriction(
                  "CEILING(?)", parameters.get(0), StandardBasicTypes.DOUBLE);
            break;
         }
         case FLOOR:
         {
            criterion = Restrictions.sqlRestriction(
                  "FLOOR (?)", parameters.get(0), StandardBasicTypes.DOUBLE);
            break;
         }
         case ROUND:
         {
            criterion = Restrictions.sqlRestriction(
                  "ROUND(?)", parameters.get(0), StandardBasicTypes.DOUBLE);
            break;
         }

         default:
            throw new UnsupportedOperationException("Unsupported method: " +
                  method.toUriLiteral());
      }

      return criterion;
   }

   @Override
   public Object visitMember(MemberExpression member_expression, Object path, Object property)
   {
      /* The property shall be handled inside visitProperty method in
       * ODataSQLVisitor implementation */
      return property;
   }

   /* Returns the field name corresponding to the given EDM type. */
   @Override
   public abstract Object visitProperty(PropertyExpression property_expression,
         String uri_literal, EdmTyped edm_property);

   private Criterion getCriterionComparative(BinaryOperator operator, Object left, Object right)
   {
      Criterion criterion = null;
      if (left instanceof Member)
      {
         if (right instanceof Member)
         {
            // property <operator> property
            String lvalue = ((Member) left).getName();
            String rvalue = ((Member) right).getName();
            switch (operator)
            {
               case EQ:
               {
                  criterion = Restrictions.eqProperty(lvalue, rvalue);
                  break;
               }
               case NE:
               {
                  criterion = Restrictions.neProperty(lvalue, rvalue);
                  break;
               }
               case GT:
               {
                  criterion = Restrictions.gtProperty(lvalue, rvalue);
                  break;
               }
               case GE:
               {
                  criterion = Restrictions.geProperty(lvalue, rvalue);
                  break;
               }
               case LT:
               {
                  criterion = Restrictions.ltProperty(lvalue, rvalue);
                  break;
               }
               case LE:
               {
                  criterion = Restrictions.leProperty(lvalue, rvalue);
                  break;
               }
               default:
                  throw new UnsupportedOperationException(
                        "Unsupported operation: " + operator.toUriLiteral());
            }
         }
         else
         {
            // property <operator> literal
            String property = ((Member) left).getName();
            criterion = internalCriterionComparative(operator, property, right);
         }
      }
      else if (right instanceof Member)
      {
         // literal <operator> property
         String property = ((Member) right).getName();
         criterion = internalCriterionComparative(operator, property, left);
      }
      else if (left instanceof Comparable)
      {
         // literal <operator> literal
         Comparable comparable = (Comparable) left;
         boolean bool;
         int result = comparable.compareTo(right);
         switch (operator)
         {
            case EQ:
            {
               bool = result == 0;
               break;
            }
            case NE:
            {
               bool = result != 0;
               break;
            }
            case GT:
            {
               bool = result > 0;
               break;
            }
            case GE:
            {
               bool = result >= 0;
               break;
            }
            case LT:
            {
               bool = result < 0;
               break;
            }
            case LE:
            {
               bool = result <= 0;
               break;
            }
            default:
               throw new UnsupportedOperationException(
                     "Unsupported operation: " + operator.toUriLiteral());
         }
         if (bool)
         {
            criterion = Restrictions.sqlRestriction("0=0");
         }
         else
         {
            criterion = Restrictions.sqlRestriction("0<>0");
         }
      }
      return criterion;
   }

   private Criterion internalCriterionComparative(
         BinaryOperator operator, String property, Object value)
   {
      Criterion criterion;
      switch (operator)
      {
         case EQ:
         {
            criterion = Restrictions.eq(property, value);
            break;
         }
         case NE:
         {
            criterion = Restrictions.ne(property, value);
            break;
         }
         case GT:
         {
            criterion = Restrictions.gt(property, value);
            break;
         }
         case GE:
         {
            criterion = Restrictions.ge(property, value);
            break;
         }
         case LT:
         {
            criterion = Restrictions.lt(property, value);
            break;
         }
         case LE:
         {
            criterion = Restrictions.le(property, value);
            break;
         }
         default:
            throw new UnsupportedOperationException(
                  "Unsupported operation: " + operator.toUriLiteral());
      }
      return criterion;
   }

   private Criterion getCriterionLogical(BinaryOperator operator,
         Criterion left, Criterion right)
   {
      Criterion criterion;
      if (left == null && right == null)
      {
         criterion = null;
      }
      else if (left != null && right != null)
      {
         switch (operator)
         {
            case AND:
            {
               criterion = Restrictions.and(left, right);
               break;
            }
            case OR:
            {
               criterion = Restrictions.or(left, right);
               break;
            }
            default:
            {
               throw new UnsupportedOperationException(
                     "Unsupported operator: " + operator.toUriLiteral());
            }
         }
      }
      else if (left == null)
      {
         criterion = right;
      }
      else
      {
         criterion = left;
      }
      return criterion;
   }

   // WARNING: args[0] can be a Member BUT args[1] cannot be a Member!
   private Criterion getCriterionFunction(MethodOperator method, Object... args)
   {
      Criterion criterion;
      if (args[0] instanceof Member)
      {
         String property = ((Member) args[0]).getName();
         switch (method)
         {
            case ENDSWITH:
            {
               String pattern = "%" + args[1];
               criterion = Restrictions.like(property, pattern);
               break;
            }
            case STARTSWITH:
            {
               String pattern = args[1] + "%";
               criterion = Restrictions.like(property, pattern);
               break;
            }
            case SUBSTRINGOF:
            {
               String pattern = "%" + args[1] + "%";
               criterion = Restrictions.like(property, pattern);
               break;
            }
            default:
            {
               throw new UnsupportedOperationException("Unsupported method: " +
                     method.toUriLiteral());
            }
         }
      }
      else
      {
         Type[] types =
         {
            StandardBasicTypes.STRING, StandardBasicTypes.STRING
         };
         switch (method)
         {
            case ENDSWITH:
            {
               Object[] parameters =
               {
                  args[0], ("%" + args[1])
               };
               criterion = Restrictions.sqlRestriction("? LIKE ?", parameters, types);
               break;
            }
            case STARTSWITH:
            {
               Object[] parameters =
               {
                  args[0], (args[1] + "%")
               };
               criterion = Restrictions.sqlRestriction("? LIKE ?", parameters, types);
               break;
            }
            case SUBSTRINGOF:
            {
               Object[] parameters =
               {
                  args[0], ("%" + args[1] + "%")
               };
               criterion = Restrictions.sqlRestriction("? LIKE ?", parameters, types);
               break;
            }
            default:
            {
               throw new UnsupportedOperationException("Unsupported method: " +
                     method.toUriLiteral());
            }
         }
      }
      return criterion;
   }

   protected static class Member
   {
      private final String name;

      public Member(String name)
      {
         this.name = Objects.requireNonNull(name);
      }

      public String getName()
      {
         return name;
      }
   }
}
