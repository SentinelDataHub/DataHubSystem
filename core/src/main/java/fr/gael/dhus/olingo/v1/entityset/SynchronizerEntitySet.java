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

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entity.Synchronizer;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;
import fr.gael.dhus.olingo.v1.map.impl.SynchronizerMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.uri.KeyPredicate;

/**
 * An OData entity set for {@link Synchronizer}.
 */
public class SynchronizerEntitySet extends AbstractEntitySet<Synchronizer>
{
   public static final String ENTITY_NAME = "Synchronizer";

   // Entity keys
   public static final String ID = "Id";
   public static final String LABEL = "Label";
   public static final String SCHEDULE = "Schedule";
   public static final String REQUEST = "Request";
   public static final String STATUS = "Status";
   public static final String STATUS_DATE = "StatusDate";
   public static final String STATUS_MESSAGE = "StatusMessage";
   public static final String CREATION_DATE = "CreationDate";
   public static final String MODIFICATION_DATE = "ModificationDate";
   public static final String SERVICE_URL = "ServiceUrl";
   public static final String SERVICE_LOGIN = "ServiceLogin";
   public static final String SERVICE_PASSWORD = "ServicePassword";
   public static final String REMOTE_INCOMING = "RemoteIncoming";
   public static final String PAGE_SIZE = "PageSize";
   public static final String LAST_INGESTION_DATE = "LastIngestionDate";
   public static final String TARGET_COLLECTION = "TargetCollection";
   public static final String COPY_PRODUCT = "CopyProduct";
   public static final String FILTER_PARAM = "FilterParam";
   public static final String SOURCE_COLLECTION = "SourceCollection";

   // Association (Navigation link)
   public static final FullQualifiedName ASSO_SYNC_COLLECTION =
         new FullQualifiedName(Model.NAMESPACE, "Synchronizer_Collection");

   // one
   public static final String ROLE_SYNC_COLLECTION = "Synchronizer_Collection";
   // many
   public static final String ROLE_COLLECTION_SYNCS = "Collection_Synchronizers";

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      List<Property> properties = new ArrayList<> ();
      properties.add (new SimpleProperty ().setName (ID)
            .setType (EdmSimpleTypeKind.Int64)
            .setFacets (new Facets ().setNullable (false)));

      properties.add (new SimpleProperty ().setName (LABEL)
            .setType (EdmSimpleTypeKind.String));

      properties.add (new SimpleProperty ().setName (CREATION_DATE)
            .setType (EdmSimpleTypeKind.DateTime)
            .setFacets (new Facets ().setNullable (false)));

      properties.add (new SimpleProperty ().setName (MODIFICATION_DATE)
            .setType (EdmSimpleTypeKind.DateTime)
            .setFacets (new Facets ().setNullable (false)));

      properties.add (new SimpleProperty ().setName (SERVICE_URL)
            .setType (EdmSimpleTypeKind.String)
            .setFacets (new Facets ().setNullable (false)));

      properties.add (new SimpleProperty ().setName (SERVICE_LOGIN)
            .setType (EdmSimpleTypeKind.String));

      properties.add (new SimpleProperty ().setName (SERVICE_PASSWORD)
            .setType (EdmSimpleTypeKind.String));

      properties.add (new SimpleProperty ().setName (REMOTE_INCOMING)
            .setType (EdmSimpleTypeKind.String));

      properties.add (new SimpleProperty ().setName (SCHEDULE)
            .setType (EdmSimpleTypeKind.String)
            .setFacets (new Facets ().setNullable (false)));

      properties.add (new SimpleProperty ().setName (PAGE_SIZE)
            .setType (EdmSimpleTypeKind.Int32)
            .setFacets (
                  new Facets ()
                        .setNullable (false)
                        .setDefaultValue ("30")
            )
      );

      properties.add(new SimpleProperty ().setName(COPY_PRODUCT)
            .setType (EdmSimpleTypeKind.Boolean)
            .setFacets (
                  new Facets ()
                     .setNullable (false)
                     .setDefaultValue ("false")
            )
      );

      properties.add (new SimpleProperty ().setName (FILTER_PARAM)
            .setType (EdmSimpleTypeKind.String));

      properties.add (new SimpleProperty ().setName (SOURCE_COLLECTION)
            .setType (EdmSimpleTypeKind.String));

      properties.add (new SimpleProperty ().setName (LAST_INGESTION_DATE)
            .setType (EdmSimpleTypeKind.DateTime));

      properties.add (new SimpleProperty ().setName (REQUEST)
            .setType (EdmSimpleTypeKind.String)
            .setFacets (
               new Facets ()
                  .setNullable (false)
                  .setDefaultValue ("stop")
            )
      );

      properties.add (new SimpleProperty ().setName (STATUS)
            .setType (EdmSimpleTypeKind.String)
            .setFacets (new Facets ().setNullable (false)));

      properties.add (new SimpleProperty ().setName (STATUS_DATE)
            .setType (EdmSimpleTypeKind.DateTime));

      properties.add (new SimpleProperty ().setName (STATUS_MESSAGE)
            .setType (EdmSimpleTypeKind.String));

      // Navigation Properties
      List<NavigationProperty> navigationProperties = Collections.singletonList (
         new NavigationProperty ()
            .setName (TARGET_COLLECTION)
            .setRelationship (ASSO_SYNC_COLLECTION)
            .setFromRole (ROLE_COLLECTION_SYNCS)
            .setToRole (ROLE_SYNC_COLLECTION)
      );

      // Key
      Key key = new Key ().setKeys (
            Collections.singletonList (new PropertyRef ().setName (ID)));

      return new EntityType ().setName (ENTITY_NAME).setProperties (properties)
            .setNavigationProperties (navigationProperties).setKey (key);
   }

   @Override
   public List<AssociationSet> getAssociationSets ()
   {
      return Collections.singletonList (new AssociationSet ()
            .setName (ASSO_SYNC_COLLECTION.getName ())
            .setAssociation (ASSO_SYNC_COLLECTION)
            .setEnd1 (
               new AssociationSetEnd ().setRole (ROLE_COLLECTION_SYNCS)
                  .setEntitySet (getName ()))
            .setEnd2 (
               new AssociationSetEnd ().setRole (ROLE_SYNC_COLLECTION)
                  .setEntitySet(Model.COLLECTION.getName()))
      );
   }

   @Override
   public List<Association> getAssociations ()
   {
      return Collections.singletonList (
         new Association ()
            .setName (ASSO_SYNC_COLLECTION.getName ())
            .setEnd1 (
               new AssociationEnd ()
                  .setType(Model.COLLECTION.getFullQualifiedName())
                  .setRole (ROLE_SYNC_COLLECTION)
                  .setMultiplicity (EdmMultiplicity.ZERO_TO_ONE))
            .setEnd2 (
               new AssociationEnd ().setType (getFullQualifiedName ())
                  .setRole (ROLE_COLLECTION_SYNCS)
                  .setMultiplicity (EdmMultiplicity.MANY))
      );
   }

   @Override
   public boolean isAuthorized (User user)
   {
      return user.getRoles ().contains (Role.SYSTEM_MANAGER);
   }

   @Override
   public Map getEntities()
   {
      return new SynchronizerMap();
   }

   @Override
   public AbstractEntity getEntity(KeyPredicate kp)
   {
      Long key = Long.parseLong(kp.getLiteral());
      return (new SynchronizerMap()).get(key);
   }

   @Override
   public boolean hasManyEntries()
   {
      return false;
   }
}
