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

import fr.gael.dhus.olingo.v1.entity.Restriction;
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

public class RestrictionEntitySet extends AbstractEntitySet<Restriction>
{
   public static final String ENTITY_NAME = "Restriction";
   public static final String UUID = "UUID";
   public static final String RESTRICTION_TYPE = "RestrictionType";
   public static final String REASON = "Reason";

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      List<Property> properties = new ArrayList<> ();

      SimpleProperty uuid = new SimpleProperty ();
      uuid.setName (UUID);
      uuid.setType (EdmSimpleTypeKind.String);
      uuid.setFacets (new Facets ().setNullable (false));
      uuid.setCustomizableFeedMappings (new CustomizableFeedMappings ()
            .setFcTargetPath (EdmTargetPath.SYNDICATION_TITLE));
      properties.add (uuid);

      SimpleProperty restriction_type = new SimpleProperty ();
      restriction_type.setName (RESTRICTION_TYPE);
      restriction_type.setType (EdmSimpleTypeKind.String);
      restriction_type.setFacets (new Facets ().setNullable (false));
      properties.add (restriction_type);

      SimpleProperty reason = new SimpleProperty ();
      reason.setName (REASON);
      reason.setType (EdmSimpleTypeKind.String);
      reason.setFacets (new Facets ().setNullable (false));
      properties.add (reason);

      Key key = new Key ();
      List<PropertyRef> propertyRefs = Collections.singletonList (
            new PropertyRef ().setName (UUID));
      key.setKeys (propertyRefs);

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
