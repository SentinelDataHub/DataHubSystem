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
package fr.gael.dhus.olingo.v1.entityset;

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.v1.entity.UserSynchronizer;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;
import fr.gael.dhus.olingo.v1.map.impl.UserSynchronizerMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTargetPath;
import org.apache.olingo.odata2.api.edm.provider.CustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.uri.KeyPredicate;

/**
 * UserSynchronizer OData EntitySet.
 */
public class UserSynchronizerEntitySet extends AbstractEntitySet<UserSynchronizer>
{
   public static final String ENTITY_NAME = "UserSynchronizer";

   // Entity keys
   public static final String ID                = "Id";
   public static final String LABEL             = "Label";
   public static final String SCHEDULE          = "Schedule";
   public static final String REQUEST           = "Request";
   public static final String STATUS            = "Status";
   public static final String STATUS_DATE       = "StatusDate";
   public static final String STATUS_MESSAGE    = "StatusMessage";
   public static final String CREATION_DATE     = "CreationDate";
   public static final String MODIFICATION_DATE = "ModificationDate";
   public static final String SERVICE_URL       = "ServiceUrl";
   public static final String SERVICE_LOGIN     = "ServiceLogin";
   public static final String SERVICE_PASSWORD  = "ServicePassword";
   public static final String CURSOR            = "Cursor";
   public static final String PAGE_SIZE         = "PageSize";
   public static final String FORCE             = "Force";

   @Override
   public String getEntityName()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType()
   {
      List<Property> properties = new ArrayList<>();

      properties.add(new SimpleProperty().setName(ID)
            .setType(EdmSimpleTypeKind.Int64)
            .setFacets(new Facets().setNullable(false))
            .setCustomizableFeedMappings(
                  new CustomizableFeedMappings().setFcTargetPath(EdmTargetPath.SYNDICATION_TITLE)));

      properties.add(new SimpleProperty().setName(LABEL)
            .setType(EdmSimpleTypeKind.String));

      properties.add(new SimpleProperty().setName(CREATION_DATE)
            .setType(EdmSimpleTypeKind.DateTime)
            .setFacets(new Facets().setNullable(false)));

      properties.add(new SimpleProperty().setName(MODIFICATION_DATE)
            .setType(EdmSimpleTypeKind.DateTime)
            .setFacets(new Facets().setNullable(false))
            .setCustomizableFeedMappings(
                  new CustomizableFeedMappings().setFcTargetPath(EdmTargetPath.SYNDICATION_UPDATED)));

      properties.add(new SimpleProperty().setName(REQUEST)
            .setType(EdmSimpleTypeKind.String)
            .setFacets(
               new Facets()
                  .setNullable(false)
                  .setDefaultValue("stop")
            )
      );

      properties.add(new SimpleProperty().setName(SCHEDULE)
            .setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false)));

      properties.add(new SimpleProperty().setName(STATUS)
            .setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false)));

      properties.add(new SimpleProperty().setName(STATUS_DATE)
            .setType(EdmSimpleTypeKind.DateTime));

      properties.add(new SimpleProperty().setName(STATUS_MESSAGE)
            .setType(EdmSimpleTypeKind.String));

      properties.add(new SimpleProperty().setName(SERVICE_URL)
            .setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false)));

      properties.add(new SimpleProperty().setName(SERVICE_LOGIN)
            .setType(EdmSimpleTypeKind.String));

      properties.add(new SimpleProperty().setName(SERVICE_PASSWORD)
            .setType(EdmSimpleTypeKind.String));

      properties.add(new SimpleProperty().setName(CURSOR)
            .setType(EdmSimpleTypeKind.Int64)
            .setFacets(
                  new Facets()
                        .setNullable(false)
                        .setDefaultValue("0")
            )
      );

      properties.add(new SimpleProperty().setName(PAGE_SIZE)
            .setType(EdmSimpleTypeKind.Int64)
            .setFacets(
                  new Facets()
                        .setNullable(false)
                        .setDefaultValue("500")
            )
      );

      properties.add(new SimpleProperty().setName(FORCE)
            .setType(EdmSimpleTypeKind.Boolean)
            .setFacets(
                  new Facets()
                        .setDefaultValue("false")
            )
      );

      // Key
      Key key = new Key().setKeys(Collections.singletonList(new PropertyRef().setName(ID)));

      return new EntityType().setName(ENTITY_NAME).setProperties(properties).setKey(key);
   }

   @Override
   public Map getEntities()
   {
      return new UserSynchronizerMap();
   }

   @Override
   public AbstractEntity getEntity(KeyPredicate kp)
   {
      Long key = Long.parseLong(kp.getLiteral());
      return (new UserSynchronizerMap()).get(key);
   }

   @Override
   public boolean hasManyEntries()
   {
      return false;
   }

   @Override
   public boolean isAuthorized(User user)
   {
      return user.getRoles().contains(Role.USER_MANAGER);
   }
}
