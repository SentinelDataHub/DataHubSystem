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
import fr.gael.dhus.olingo.v1.entitySet.CollectionEntitySet;

public class CollectionSQLVisitor extends OlingoSQLVisitor
{
   private String prefix = null;

   public CollectionSQLVisitor (String prefix)
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

      if (uriLiteral.equals (CollectionEntitySet.NAME))
         return prefix + ".name";

      if (uriLiteral.equals (CollectionEntitySet.DESCRIPTION))
         return prefix + ".description";

      throw new IllegalArgumentException ("Property not supported: " +
         uriLiteral);
   }
}
