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

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.v1.entity.Network;
import fr.gael.dhus.server.http.valve.AccessValve;

public class NetworkStatisticEntitySet extends AbstractEntitySet<Network>
{
   public static final String ENTITY_NAME = "NetworkStatistic";

   // Entity keys
   public static final String ID = "Id";
   public static final String ACTIVITYPERIOD = "ActivityPeriod";
   public static final String CONNECTIONNUMBER = "ConnectionNumber";

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      // Properties
      List<Property> properties = new ArrayList<Property> ();

      properties.add (new SimpleProperty ()
      .setName (ID)
      .setType (EdmSimpleTypeKind.Int64)
      .setFacets (new Facets ().setNullable (false))
      .setCustomizableFeedMappings (
         new CustomizableFeedMappings ()
            .setFcTargetPath (EdmTargetPath.SYNDICATION_TITLE)));

      properties.add (new SimpleProperty ()
         .setName (ACTIVITYPERIOD)
         .setType (EdmSimpleTypeKind.Int64));

      properties.add (new SimpleProperty ()
         .setName (CONNECTIONNUMBER)
         .setType (EdmSimpleTypeKind.Int64));
      // Key
      Key key =
         new Key ().setKeys (Collections.singletonList (new PropertyRef ()
            .setName (ID)));

      return new EntityType ().setName (ENTITY_NAME).setProperties (properties)
         .setKey (key);
   }

   @Override
   public int count ()
   {
      return AccessValve.getAccessInformationMap ().size ();
   }

   @Override
   public boolean isAuthorized (User user)
   {
      return user.getRoles ().contains (Role.STATISTICS);
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