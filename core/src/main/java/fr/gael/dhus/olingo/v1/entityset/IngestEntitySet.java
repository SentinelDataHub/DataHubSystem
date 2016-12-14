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
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entity.Ingest;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import org.apache.olingo.odata2.api.uri.KeyPredicate;

/**
 * An OData entity set for {@link Ingest}.
 */
public class IngestEntitySet extends AbstractEntitySet<Ingest>
{
   public static final String ENTITY_NAME = "Ingest";

   // Property names
   public static final String ID             = "Id";
   public static final String STATUS         = "Status";
   public static final String STATUS_DATE    = "StatusDate";
   public static final String STATUS_MESSAGE = "StatusMessage";

   public static final String FILENAME  = "Filename";
   public static final String MD5       = "MD5";

   // Association (Navigation link)
   public static final String TARGET_COLLECTIONS = "TargetCollections";
   public static final String UPLOADER = "Uploader";

   public static final FullQualifiedName ASSO_INGEST_COLLECTION =
         new FullQualifiedName(Model.NAMESPACE, "Ingest_Collection");
   //   ToRole: Ingest->Collections (many)
   public static final String ROLE_INGEST_COLLECTIONS = "Ingest_Collections";
   // FromRole: Collection->Ingests (many)
   public static final String ROLE_COLLECTION_INGESTS = "Collection_Ingests";

   public static final FullQualifiedName ASSO_INGEST_USER =
         new FullQualifiedName(Model.NAMESPACE, "Ingest_User");
   //   ToRole: Ingest->User (one)
   public static final String ROLE_INGEST_USER = "Ingest_User";
   // FromRole: User->Ingest (one)
   public static final String ROLE_USER_INGEST = "User_Ingest";

   @Override
   public String getEntityName()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType()
   {
      // Properties
      List<Property> properties = new ArrayList<>();
      properties.add(new SimpleProperty().setName(ID)
            .setType(EdmSimpleTypeKind.Int64)
            .setFacets(new Facets().setNullable(false))
            .setCustomizableFeedMappings(
                  new CustomizableFeedMappings().setFcTargetPath(EdmTargetPath.SYNDICATION_TITLE)));

      properties.add(new SimpleProperty().setName(STATUS)
            .setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false)));

      properties.add(new SimpleProperty().setName(STATUS_DATE)
            .setType(EdmSimpleTypeKind.DateTime)
            .setCustomizableFeedMappings(
                  new CustomizableFeedMappings().setFcTargetPath(EdmTargetPath.SYNDICATION_UPDATED)));

      properties.add(new SimpleProperty().setName(STATUS_MESSAGE)
            .setType(EdmSimpleTypeKind.String));


      properties.add(new SimpleProperty().setName(FILENAME)
            .setType(EdmSimpleTypeKind.String));

      properties.add(new SimpleProperty().setName(MD5)
            .setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false)));

      // Key
      Key key = new Key().setKeys(
            Collections.singletonList(new PropertyRef().setName(ID)));

      // Navigation Properties
      List<NavigationProperty> navigationProperties = new ArrayList<>(2);
      navigationProperties.add(
            new NavigationProperty()
            .setName(TARGET_COLLECTIONS)
            .setRelationship(ASSO_INGEST_COLLECTION)
            .setToRole(ROLE_INGEST_COLLECTIONS)
            .setFromRole(ROLE_COLLECTION_INGESTS)
      );
      navigationProperties.add(
            new NavigationProperty()
            .setName(UPLOADER)
            .setRelationship(ASSO_INGEST_USER)
            .setToRole(ROLE_INGEST_USER)
            .setFromRole(ROLE_USER_INGEST)
      );

      return new EntityType()
            .setName(ENTITY_NAME)
            .setProperties(properties)
            .setKey(key)
            .setNavigationProperties(navigationProperties)
            .setHasStream(true);
   }

   @Override
   public List<AssociationSet> getAssociationSets()
   {
      List<AssociationSet> res = new ArrayList<>(2);

      res.add(
            new AssociationSet()
            .setName(ASSO_INGEST_COLLECTION.getName())
            .setAssociation(ASSO_INGEST_COLLECTION)
            .setEnd1(
                  new AssociationSetEnd().setRole(ROLE_INGEST_COLLECTIONS)
                        .setEntitySet(Model.COLLECTION.getName()))
            .setEnd2(
                  new AssociationSetEnd().setRole(ROLE_COLLECTION_INGESTS)
                        .setEntitySet(getName()))
      );

      res.add(
            new AssociationSet()
            .setName(ASSO_INGEST_USER.getName())
            .setAssociation(ASSO_INGEST_USER)
            .setEnd1(
                  new AssociationSetEnd().setRole(ROLE_INGEST_USER)
                        .setEntitySet(Model.USER.getName()))
            .setEnd2(
                  new AssociationSetEnd().setRole(ROLE_USER_INGEST)
                        .setEntitySet(getName()))
      );

      return res;
   }

   @Override
   public List<Association> getAssociations()
   {
      List<Association> res = new ArrayList<>(2);

      res.add(
            new Association()
            .setName(ASSO_INGEST_COLLECTION.getName())
            .setEnd1(
                  new AssociationEnd()
                        .setType(Model.COLLECTION.getFullQualifiedName())
                        .setRole(ROLE_INGEST_COLLECTIONS)
                        .setMultiplicity(EdmMultiplicity.MANY))
            .setEnd2(
                  new AssociationEnd()
                        .setType(getFullQualifiedName())
                        .setRole(ROLE_COLLECTION_INGESTS)
                        .setMultiplicity(EdmMultiplicity.MANY))
      );

      res.add(
            new Association()
            .setName(ASSO_INGEST_USER.getName())
            .setEnd1(
                  new AssociationEnd()
                        .setType(Model.USER.getFullQualifiedName())
                        .setRole(ROLE_INGEST_USER)
                        .setMultiplicity(EdmMultiplicity.ONE))
            .setEnd2(
                  new AssociationEnd()
                        .setType(getFullQualifiedName())
                        .setRole(ROLE_USER_INGEST)
                        .setMultiplicity(EdmMultiplicity.ONE))
      );

      return res;
   }

   @Override
   public boolean isAuthorized(User user)
   {
      return user.getRoles().contains(Role.UPLOAD);
   }

   @Override
   public Map getEntities()
   {
      return Ingest.getMappable();
   }

   @Override
   public AbstractEntity getEntity(KeyPredicate kp)
   {
      Long key = Long.parseLong(kp.getLiteral());
      return Ingest.get(key);
   }

   @Override
   public boolean hasManyEntries()
   {
      return false;
   }
}
