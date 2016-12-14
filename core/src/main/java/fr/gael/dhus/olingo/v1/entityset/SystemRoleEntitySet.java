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
package fr.gael.dhus.olingo.v1.entityset;

import fr.gael.dhus.olingo.v1.entity.SystemRole;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTargetPath;
import org.apache.olingo.odata2.api.edm.provider.CustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SystemRoleEntitySet extends AbstractEntitySet<SystemRole>
{
   public static final String ENTITY_NAME = "SystemRole";
   public static final String NAME = "Name";
   public static final String DESCRIPTION = "Description";

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      Key key = new Key ();
      List<PropertyRef> property_refs =
            Collections.singletonList (new PropertyRef ().setName (NAME));
      key.setKeys (property_refs);

      SimpleProperty name = new SimpleProperty ();
      name.setName (NAME);
      name.setType (EdmSimpleTypeKind.String);
      name.setFacets (new Facets ().setNullable (false));
      name.setCustomizableFeedMappings (new CustomizableFeedMappings ()
            .setFcTargetPath (EdmTargetPath.SYNDICATION_TITLE));

      SimpleProperty description = new SimpleProperty ();
      description.setName (DESCRIPTION);
      description.setType (EdmSimpleTypeKind.String);
      description.setFacets (new Facets ().setNullable (false));

      List<Property> properties = new ArrayList<> ();
      properties.add (name);
      properties.add (description);

      EntityType entityType = new EntityType ();
      entityType.setName (ENTITY_NAME);
      entityType.setProperties (properties);
      entityType.setKey (key);

      return entityType;
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
