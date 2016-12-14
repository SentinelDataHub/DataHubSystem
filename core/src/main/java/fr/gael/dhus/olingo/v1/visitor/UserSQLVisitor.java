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

import fr.gael.dhus.database.object.User;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;

import fr.gael.dhus.olingo.v1.SQLVisitor;
import fr.gael.dhus.olingo.v1.entityset.UserEntitySet;

public class UserSQLVisitor extends SQLVisitor
{

   public UserSQLVisitor ()
   {
      super(User.class);
   }

   @Override
   public Object visitProperty (PropertyExpression property_expression,
      String uri_literal, EdmTyped edm_property)
   {
      if (edm_property == null)
         throw new IllegalArgumentException ("Property not found: " +
               uri_literal);

      if (uri_literal.equals (UserEntitySet.USERNAME)) 
         return new Member ("username");

      if (uri_literal.equals (UserEntitySet.COUNTRY))
         return new Member ("country");

      if (uri_literal.equals (UserEntitySet.EMAIL))
         return new Member ("email");

      if (uri_literal.equals (UserEntitySet.FIRSTNAME))
         return new Member ("firstname");

      if (uri_literal.equals (UserEntitySet.LASTNAME))
         return new Member ("lastname");

      if (uri_literal.equals (UserEntitySet.ADDRESS))
         return new Member ("address");

      if (uri_literal.equals (UserEntitySet.PHONE))
         return new Member ("phone");

      if (uri_literal.equals (UserEntitySet.DOMAIN))
         return new Member ("domain");

      if (uri_literal.equals (UserEntitySet.SUBDOMAIN))
         return new Member ("subDomain");

      if (uri_literal.equals (UserEntitySet.USAGE))
         return new Member ("usage");

      if (uri_literal.equals (UserEntitySet.SUBUSAGE))
         return new Member ("subUsage");

      if (uri_literal.equals(UserEntitySet.CREATED))
         return new Member ("created");

      throw new IllegalArgumentException ("Property not supported: " +
            uri_literal);
   }
}
