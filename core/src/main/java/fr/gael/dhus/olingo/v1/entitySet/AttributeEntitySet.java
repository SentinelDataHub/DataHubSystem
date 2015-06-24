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
package fr.gael.dhus.olingo.v1.entitySet;

import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;

import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.entity.Attribute;

public class AttributeEntitySet extends V1EntitySet<Attribute>
{
   public static String ENTITY_NAME = "Attribute";

   // Entity keys
   public static String VALUE = "Value";

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      EntityType res = V1Model.ITEM.getEntityType ();

      List<Property> properties = res.getProperties (); // Collections.singletonList(
      properties.add ((Property) new SimpleProperty ().setName (VALUE).setType (
         EdmSimpleTypeKind.String));

      return res.setName (ENTITY_NAME).setProperties (properties);
   }
}
