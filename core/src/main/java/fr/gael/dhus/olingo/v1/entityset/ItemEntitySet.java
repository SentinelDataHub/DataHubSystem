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

import fr.gael.dhus.database.object.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTargetPath;
import org.apache.olingo.odata2.api.edm.provider.CustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;

import fr.gael.dhus.olingo.v1.entity.Item;

public class ItemEntitySet extends AbstractEntitySet<Item>
{
   public static final String ENTITY_NAME = "Item";

   // Entity keys
   public static final String ID = "Id";
   public static final String NAME = "Name";
   public static final String CONTENT_TYPE = "ContentType";
   public static final String CONTENT_LENGTH = "ContentLength";

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      List<Property> properties = new ArrayList<Property> ();
      properties.add (new SimpleProperty ().setName (ID)
         .setType (EdmSimpleTypeKind.String)
         .setFacets (new Facets ().setNullable (false)));
      properties.add (new SimpleProperty ()
         .setName (NAME)
         .setType (EdmSimpleTypeKind.String)
         .setCustomizableFeedMappings (
            new CustomizableFeedMappings ()
               .setFcTargetPath (EdmTargetPath.SYNDICATION_TITLE)));
      properties.add (new SimpleProperty ().setName (CONTENT_TYPE).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (CONTENT_LENGTH).setType (
         EdmSimpleTypeKind.Int64));

      // Key
      Key key =
         new Key ().setKeys (Collections.singletonList (new PropertyRef ()
            .setName (ID)));

      // TODO (OData v3) setOpenType(true) setAbstract(true)
      return new EntityType ().setName (ENTITY_NAME).setProperties (properties)
         .setKey (key);
   }

   @Override
   public boolean isTopLevel()
   {
      return false;
   }

   @Override
   public boolean isAuthorized(User user)
   {
      return false;
   }

   @Override
   public boolean isAbstract()
   {
      return true;
   }
}
