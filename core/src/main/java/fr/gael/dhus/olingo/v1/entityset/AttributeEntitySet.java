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
package fr.gael.dhus.olingo.v1.entityset;

import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;

import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entity.Attribute;
import org.apache.olingo.odata2.api.edm.provider.Facets;

public class AttributeEntitySet extends AbstractEntitySet<Attribute>
{
   public static final String ENTITY_NAME = "Attribute";

   // Entity keys
   public static final String VALUE = "Value";
   public static final String CATEGORY = "Category";

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      EntityType res = Model.ITEM.getEntityType();

      List<Property> properties = res.getProperties ();
      properties.add ((Property) new SimpleProperty ().setName (VALUE)
            .setType (EdmSimpleTypeKind.String));
      properties.add((Property) new SimpleProperty().setName(CATEGORY)
            .setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setDefaultValue(null)));

      return res.setName (ENTITY_NAME).setProperties (properties);
   }

   @Override
   public boolean isTopLevel()
   {
      return false;
   }

   @Override
   public boolean hasManyEntries()
   {
      return false;
   }
}
