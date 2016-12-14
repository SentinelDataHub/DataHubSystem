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
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.Security;
import fr.gael.dhus.olingo.v1.Expander;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;
import fr.gael.dhus.olingo.v1.entity.Collection;
import fr.gael.dhus.olingo.v1.map.impl.CollectionMap;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class CollectionEntitySet extends AbstractEntitySet<Collection>
{
   private static final Logger LOGGER = LogManager.getLogger (CollectionEntitySet.class);
   private static final CollectionService COLLECTION_SERVICE =
         ApplicationContextProvider.getBean(CollectionService.class);

   public static final String ENTITY_NAME = "Collection";

   // Entity keys
   public static final String UUID = "UUID";
   public static final String NAME = "Name";
   public static final String DESCRIPTION = "Description";

   public static final FullQualifiedName ASSO_COLLECTION_PRODUCT =
      new FullQualifiedName(Model.NAMESPACE, "Collection_Product");
   // many
   public static final String ROLE_COLLECTION_PRODUCTS = "Collection_Products";
   // many
   public static final String ROLE_PRODUCT_COLLECTIONS = "Product_Collections";

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      List<Property> properties = new ArrayList<>();
      properties.add (new SimpleProperty ()
         .setName (NAME)
         .setType (EdmSimpleTypeKind.String)
         .setFacets (new Facets ().setNullable (false))
         .setCustomizableFeedMappings (
            new CustomizableFeedMappings ()
               .setFcTargetPath (EdmTargetPath.SYNDICATION_TITLE)));
      properties.add (new SimpleProperty ().setName (DESCRIPTION).setType (
         EdmSimpleTypeKind.String));

      // Navigation Properties
      List<NavigationProperty> navigationProperties =
         Collections.singletonList(new NavigationProperty()
            .setName(Model.PRODUCT.getName())
            .setRelationship(ASSO_COLLECTION_PRODUCT)
            .setFromRole(ROLE_PRODUCT_COLLECTIONS)
            .setToRole(ROLE_COLLECTION_PRODUCTS));

      // Key
      Key key =
         new Key ().setKeys (Collections.singletonList (new PropertyRef ()
            .setName (NAME)));

      return new EntityType ().setName (ENTITY_NAME).setProperties (properties)
         .setKey (key).setNavigationProperties (navigationProperties);
   }

   @Override
   public List<AssociationSet> getAssociationSets ()
   {
      return Collections.singletonList(new AssociationSet()
         .setName (ASSO_COLLECTION_PRODUCT.getName ())
         .setAssociation (ASSO_COLLECTION_PRODUCT)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_PRODUCT_COLLECTIONS)
               .setEntitySet (getName ()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_COLLECTION_PRODUCTS)
               .setEntitySet(Model.PRODUCT.getName())));
   }

   @Override
   public List<Association> getAssociations ()
   {
      return Collections.singletonList(new Association()
         .setName (ASSO_COLLECTION_PRODUCT.getName ())
         .setEnd1 (
            new AssociationEnd ()
               .setType(Model.PRODUCT.getFullQualifiedName())
               .setRole (ROLE_COLLECTION_PRODUCTS)
               .setMultiplicity (EdmMultiplicity.MANY))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_PRODUCT_COLLECTIONS)
               .setMultiplicity (EdmMultiplicity.MANY)));
   }

   @Override
   public int count ()
   {
      User u = Security.getCurrentUser();
      return COLLECTION_SERVICE.countAuthorizedCollections(u);
   }

   @Override
   public Map getEntities()
   {
      return new CollectionMap();
   }

   @Override
   public AbstractEntity getEntity(KeyPredicate kp)
   {
      return new CollectionMap().get(kp.getLiteral());
   }

   @Override
   public List<String> getExpandableNavLinkNames()
   {
      return Collections.singletonList("Products");
   }

   @Override
   public List<Map<String, Object>> expand(String navlink_name, String self_url,
         Map<?, AbstractEntity> entities, Map<String, Object> key)
   {
      return Expander.expandFeedSingletonKey(navlink_name, self_url, entities, key, CollectionEntitySet.NAME);
   }
}
