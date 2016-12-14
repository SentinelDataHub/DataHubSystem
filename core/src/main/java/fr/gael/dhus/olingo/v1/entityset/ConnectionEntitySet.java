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

import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTargetPath;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;
import org.apache.olingo.odata2.api.edm.provider.CustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.Security;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entity.Connection;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;
import fr.gael.dhus.olingo.v1.map.impl.ConnectionMap;
import fr.gael.dhus.server.http.valve.AccessValve;
import java.util.Map;
import java.util.UUID;
import org.apache.olingo.odata2.api.uri.KeyPredicate;

public class ConnectionEntitySet extends AbstractEntitySet<Connection>
{
   public static final String ENTITY_NAME = "Connection";

   // Entity keys
   public static final String ID = "Id";
   public static final String DATE = "Date";
   public static final String REMOTEIP = "RemoteIp";
   public static final String REQUEST = "Request";
   public static final String DURATION = "Duration";
   public static final String CONTENT_LENGTH = "ContentLength";
   public static final String WRITTEN_CONTENT_LENGTH = "WrittenContentLength";
   public static final String STATUS = "Status";
   public static final String STATUS_MESSAGE = "Message";

   public static final FullQualifiedName ASSO_CONNECTION_USER =
      new FullQualifiedName(Model.NAMESPACE, "Connection_User");
   public static final String ROLE_CONNECTION_USER = "Connection_User";
   public static final String ROLE_USER_CONNECTIONS = "User_Connections";

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
         .setType (EdmSimpleTypeKind.String)
         .setFacets (new Facets ().setNullable (false))
         .setCustomizableFeedMappings (
            new CustomizableFeedMappings ()
               .setFcTargetPath (EdmTargetPath.SYNDICATION_TITLE)));

      properties.add (new SimpleProperty ().setName (DATE).setType (
         EdmSimpleTypeKind.DateTime));
      properties.add (new SimpleProperty ().setName (REMOTEIP).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (REQUEST).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (DURATION).setType (
         EdmSimpleTypeKind.Double));
      properties.add (new SimpleProperty ().setName (CONTENT_LENGTH).setType (
         EdmSimpleTypeKind.Int64));
      properties.add (new SimpleProperty ().setName (WRITTEN_CONTENT_LENGTH).
         setType (EdmSimpleTypeKind.Int64));
      properties.add (new SimpleProperty ().setName (STATUS).setType (
            EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (STATUS_MESSAGE).setType (
            EdmSimpleTypeKind.String).
            setFacets (new Facets ().setNullable (true)));

      // Navigation Properties
      List<NavigationProperty> navigationProperties =
         new ArrayList<NavigationProperty> ();

      if (Security.currentUserHasRole(Role.SYSTEM_MANAGER))
      {
      navigationProperties.add (new NavigationProperty ().setName ("User")
         .setRelationship (ASSO_CONNECTION_USER)
         .setFromRole (ROLE_USER_CONNECTIONS).setToRole (ROLE_CONNECTION_USER));
      }

      // Key
      Key key =
         new Key ().setKeys (Collections.singletonList (new PropertyRef ()
            .setName (ID)));

      return new EntityType ().setName (ENTITY_NAME).setProperties (properties)
         .setKey (key).setNavigationProperties (navigationProperties);
   }

   @Override
   public List<AssociationSet> getAssociationSets ()
   {
      List<AssociationSet> associationSets = new ArrayList<AssociationSet> ();

      if (Security.currentUserHasRole(Role.SYSTEM_MANAGER))
      {
      associationSets.add (new AssociationSet ()
         .setName (ASSO_CONNECTION_USER.getName ())
         .setAssociation (ASSO_CONNECTION_USER)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_CONNECTION_USER)
               .setEntitySet(Model.USER.getName()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_USER_CONNECTIONS)
               .setEntitySet (getName ())));
      }
      return associationSets;
   }

   @Override
   public List<Association> getAssociations ()
   {
      List<Association> associations = new ArrayList<Association> ();

      if (Security.currentUserHasRole(Role.SYSTEM_MANAGER))
      {
      associations.add (new Association ()
         .setName (ASSO_CONNECTION_USER.getName ())
         .setEnd1 (
            new AssociationEnd ()
               .setType(Model.USER.getFullQualifiedName())
               .setRole (ROLE_CONNECTION_USER)
               .setMultiplicity (EdmMultiplicity.ONE))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_USER_CONNECTIONS)
               .setMultiplicity (EdmMultiplicity.MANY)));
      }
      return associations;
   }

   @Override
   public int count ()
   {
      return AccessValve.getAccessInformationMap ().size ();
   }

   @Override
   public boolean isAuthorized (User user)
   {
      return user.getRoles ().contains (Role.SYSTEM_MANAGER) ||
             user.getRoles ().contains (Role.STATISTICS);
   }

   @Override
   public Map getEntities()
   {
      return new ConnectionMap();
   }

   @Override
   public AbstractEntity getEntity(KeyPredicate kp)
   {
      UUID key = UUID.fromString(kp.getLiteral());
      return (new ConnectionMap()).get(key);
   }
}