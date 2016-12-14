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
package fr.gael.dhus.olingo.v1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationPropertySegment;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.SelectItem;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;

/**
 * Because every kind of UriInfo is a parent of class UriInfo...
 */
@SuppressWarnings("unchecked")
public class AdaptableUriInfo implements UriInfo
{
   /** Any kind of UriInfo. */
   private final Object o;

   /**
    * Creates an UriInfo from an instance of any of its super types.
    *
    * @param o Any kind of UriInfo.
    */
   public AdaptableUriInfo(Object o)
   {
      this.o = o;
   }

   /** Invokes the given method and returns the result. */
   private Object get(String method_name)
   {
      try {
         Method m = o.getClass().getDeclaredMethod(method_name);
         return m.invoke(o);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
         return null;
      }
   }

   @Override
   public EdmEntityContainer getEntityContainer()
   {
      return EdmEntityContainer.class.cast(get("getEntityContainer"));
   }

   @Override
   public EdmEntitySet getStartEntitySet()
   {
      return EdmEntitySet.class.cast(get("getStartEntitySet"));
   }

   @Override
   public EdmEntitySet getTargetEntitySet()
   {
      return EdmEntitySet.class.cast(get("getTargetEntitySet"));
   }

   @Override
   public EdmFunctionImport getFunctionImport()
   {
      return EdmFunctionImport.class.cast(get("getFunctionImport"));
   }

   @Override
   public EdmType getTargetType()
   {
      return EdmType.class.cast(get("getTargetType"));
   }

   @Override
   public List<KeyPredicate> getKeyPredicates()
   {
      return List.class.cast(get("getKeyPredicates"));
   }

   @Override
   public List<KeyPredicate> getTargetKeyPredicates()
   {
      return List.class.cast(get("getTargetKeyPredicates"));
   }

   @Override
   public List<NavigationSegment> getNavigationSegments()
   {
      return List.class.cast(get("getNavigationSegments"));
   }

   @Override
   public List<EdmProperty> getPropertyPath()
   {
      return List.class.cast(get("getPropertyPath"));
   }

   @Override
   public boolean isCount()
   {
      Boolean b = Boolean.class.cast(get("isCount"));
      return b == null? false: b;
   }

   @Override
   public boolean isValue()
   {
      Boolean b = Boolean.class.cast(get("isValue"));
      return b == null? false: b;
   }

   @Override
   public boolean isLinks()
   {
      Boolean b = Boolean.class.cast(get("isLinks"));
      return b == null? false: b;
   }

   @Override
   public String getFormat()
   {
      return String.class.cast(get("getFormat"));
   }

   @Override
   public FilterExpression getFilter()
   {
      return FilterExpression.class.cast(get("getFilter"));
   }

   @Override
   public InlineCount getInlineCount()
   {
      return InlineCount.class.cast(get("getInlineCount"));
   }

   @Override
   public OrderByExpression getOrderBy()
   {
      return OrderByExpression.class.cast(get("getOrderBy"));
   }

   @Override
   public String getSkipToken()
   {
      return String.class.cast(get("getSkipToken"));
   }

   @Override
   public Integer getSkip()
   {
      return Integer.class.cast(get("getSkip"));
   }

   @Override
   public Integer getTop()
   {
      return Integer.class.cast(get("getTop"));
   }

   @Override
   public List<ArrayList<NavigationPropertySegment>> getExpand()
   {
      return List.class.cast(get("getExpand"));
   }

   @Override
   public List<SelectItem> getSelect()
   {
      return List.class.cast(get("getSelect"));
   }

   @Override
   public Map<String, EdmLiteral> getFunctionImportParameters()
   {
      return Map.class.cast(get("getFunctionImportParameters"));
   }

   @Override
   public Map<String, String> getCustomQueryOptions()
   {
      return Map.class.cast(get("getCustomQueryOptions"));
   }

}
