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
package fr.gael.dhus.olingo.v1.visitor;

import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;

import fr.gael.dhus.olingo.OlingoSQLVisitor;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.entitySet.ItemEntitySet;
import fr.gael.dhus.olingo.v1.entitySet.NodeEntitySet;
import fr.gael.dhus.olingo.v1.entitySet.ProductEntitySet;

public class ProductSQLVisitor extends OlingoSQLVisitor
{
   private String prefix = null;

   public ProductSQLVisitor (String prefix)
   {
      this.prefix = prefix;
   }

   @Override
   public Object visitProperty (PropertyExpression propertyExpression,
      String uriLiteral, EdmTyped edmProperty)
   {
      if (edmProperty == null)
         throw new IllegalArgumentException ("Property not found: " +
            uriLiteral);

      if (uriLiteral.equals (ItemEntitySet.ID)) return prefix + ".uuid";

      if (uriLiteral.equals (ItemEntitySet.NAME))
         return prefix + ".identifier";

      if (uriLiteral.equals (ItemEntitySet.CONTENT_LENGTH))
         return prefix + ".download.size";

      if (uriLiteral.equals (ProductEntitySet.CREATION_DATE))
         return prefix + ".created";

      if (uriLiteral.equals (ProductEntitySet.INGESTION_DATE))
         return prefix + ".ingestionDate";

      if (uriLiteral.equals (ProductEntitySet.CONTENT_GEOMETRY))
         return prefix + ".footPrint";

      // Case of complex property ContentDate
      // Not used really, but needed to be here.
      if (uriLiteral.equals (ProductEntitySet.CONTENT_DATE)) return "";
      if (uriLiteral.equals (V1Model.TIME_RANGE_START))
         return prefix + ".contentStart";
      if (uriLiteral.equals (V1Model.TIME_RANGE_END))
         return prefix + ".contentEnd";

      if (uriLiteral.equals (ProductEntitySet.EVICTION_DATE) ||
         uriLiteral.equals (ProductEntitySet.METALINK))
         throw new IllegalArgumentException ("Property \"" + uriLiteral +
            "\" is a dynamic data and is not filterable.");

      if (uriLiteral.equals (ItemEntitySet.CONTENT_TYPE) ||
         uriLiteral.equals (NodeEntitySet.VALUE) ||
         uriLiteral.equals (NodeEntitySet.CHILDREN_NUMBER))
         throw new IllegalArgumentException ("Property \"" + uriLiteral +
            "\" is not filterable.");

      throw new IllegalArgumentException ("Property not supported: " +
         uriLiteral);
   }
}
