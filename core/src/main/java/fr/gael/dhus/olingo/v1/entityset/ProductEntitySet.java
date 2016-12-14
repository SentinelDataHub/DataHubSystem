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
import fr.gael.dhus.olingo.Security;
import fr.gael.dhus.olingo.v1.Expander;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;
import fr.gael.dhus.olingo.v1.entity.Product;
import fr.gael.dhus.olingo.v1.map.impl.ProductsMap;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import java.util.ArrayList;
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
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.CustomizableFeedMappings;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.uri.KeyPredicate;

public class ProductEntitySet extends AbstractEntitySet<Product>
{
   public static final String ENTITY_NAME = "Product";

   // Entity keys
   public static final String CONTENT_DATE = "ContentDate";
   public static final String INGESTION_DATE = "IngestionDate";
   public static final String CREATION_DATE = "CreationDate";
   public static final String EVICTION_DATE = "EvictionDate";
   public static final String CONTENT_GEOMETRY = "ContentGeometry";
   public static final String METALINK = "Metalink";
   public static final String CHECKSUM = "Checksum";
   public static final String LOCAL_PATH = "LocalPath";

   public static final FullQualifiedName ASSO_PRODUCT_PRODUCT =
      new FullQualifiedName(Model.NAMESPACE, "Product_Product");
   public static final String ROLE_PRODUCT_PRODUCTS = "Product_Products";// many
   public static final String ROLE_PRODUCT_PRODUCT = "Product_Product";// 1

   public static final FullQualifiedName ASSO_PRODUCT_NODE =
      new FullQualifiedName(Model.NAMESPACE, "Product_Node");
   public static final String ROLE_PRODUCT_NODES = "Product_Nodes";// many
   public static final String ROLE_NODE_PRODUCT = "Node_Product";// 1

   public static final FullQualifiedName ASSO_PRODUCT_CLASS =
      new FullQualifiedName(Model.NAMESPACE, "Product_Class");
   public static final String ROLE_PRODUCT_CLASS = "Product_Class";
   public static final String ROLE_CLASS_PRODUCTS = "Class_Products";

   public static final FullQualifiedName ASSO_PRODUCT_ATTRIBUTE =
      new FullQualifiedName(Model.NAMESPACE, "Product_Attribute");
   public static final String ROLE_PRODUCT_ATTRIBUTES = "Product_Attributes";// many
   public static final String ROLE_ATTRIBUTE_PRODUCT = "Attribute_Product";// 1

   private static final ProductService PRODUCT_SERVICE =
         ApplicationContextProvider.getBean(ProductService.class);

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      EntityType res = Model.NODE.getEntityType();

      // Properties
      List<Property> properties = res.getProperties ();

      properties.add (new SimpleProperty ().setName (CREATION_DATE)
         .setType (EdmSimpleTypeKind.DateTime)
         .setFacets (new Facets ().setNullable (false)));
      properties.add (new SimpleProperty ()
         .setName (INGESTION_DATE)
         .setType (EdmSimpleTypeKind.DateTime)
         .setCustomizableFeedMappings (
            new CustomizableFeedMappings ()
               .setFcTargetPath (EdmTargetPath.SYNDICATION_UPDATED)));
      properties.add (new SimpleProperty ().setName (EVICTION_DATE).setType (
         EdmSimpleTypeKind.DateTime));
      properties.add(new ComplexProperty().setName(CONTENT_DATE).setType(Model.TIME_RANGE));
      properties.add(new ComplexProperty().setName(CHECKSUM).setType(Model.CHECKSUM));
      properties.add (new SimpleProperty ().setName (CONTENT_GEOMETRY).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (METALINK).setType (
         EdmSimpleTypeKind.String));

      if (Security.currentUserHasRole(Role.ARCHIVE_MANAGER))
      {
         properties.add (new SimpleProperty ().setName (LOCAL_PATH).setType (
            EdmSimpleTypeKind.String));
      }

      // Navigation Properties
      List<NavigationProperty> navigationProperties =
         new ArrayList<NavigationProperty> ();

      navigationProperties.add (new NavigationProperty ().setName (getName ())
         .setRelationship (ASSO_PRODUCT_PRODUCT)
         .setFromRole (ROLE_PRODUCT_PRODUCT).setToRole (ROLE_PRODUCT_PRODUCTS));
      navigationProperties.add (new NavigationProperty ()
         .setName(Model.NODE.getName()).setRelationship(ASSO_PRODUCT_NODE)
         .setFromRole (ROLE_NODE_PRODUCT).setToRole (ROLE_PRODUCT_NODES));
      navigationProperties.add (new NavigationProperty ()
         .setName(Model.ATTRIBUTE.getName())
         .setRelationship (ASSO_PRODUCT_ATTRIBUTE)
         .setFromRole (ROLE_ATTRIBUTE_PRODUCT)
         .setToRole (ROLE_PRODUCT_ATTRIBUTES));
      navigationProperties.add (new NavigationProperty ().setName ("Class")
         .setRelationship (ASSO_PRODUCT_CLASS)
         .setFromRole (ROLE_CLASS_PRODUCTS).setToRole (ROLE_PRODUCT_CLASS));

      // TODO (OData v3) setContainsTarget(true) setBaseType(ENTITY_ITEM)
      return res.setName (ENTITY_NAME).setProperties (properties)
         .setHasStream (true).setNavigationProperties (navigationProperties);
   }

   @Override
   public List<AssociationSet> getAssociationSets ()
   {
      List<AssociationSet> associationSets = new ArrayList<AssociationSet> ();

      associationSets.add (new AssociationSet ()
         .setName (ASSO_PRODUCT_PRODUCT.getName ())
         .setAssociation (ASSO_PRODUCT_PRODUCT)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_PRODUCT_PRODUCTS)
               .setEntitySet (getName ()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_PRODUCT_PRODUCT)
               .setEntitySet (getName ())));

      associationSets.add (new AssociationSet ()
         .setName (ASSO_PRODUCT_NODE.getName ())
         .setAssociation (ASSO_PRODUCT_NODE)
         .setEnd1 (
            new AssociationSetEnd().setRole(ROLE_PRODUCT_NODES)
               .setEntitySet(Model.NODE.getName()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_NODE_PRODUCT).setEntitySet (
               getName ())));

      associationSets.add (new AssociationSet ()
         .setName (ASSO_PRODUCT_ATTRIBUTE.getName ())
         .setAssociation (ASSO_PRODUCT_ATTRIBUTE)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_PRODUCT_ATTRIBUTES)
               .setEntitySet(Model.ATTRIBUTE.getName()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_ATTRIBUTE_PRODUCT)
               .setEntitySet (getName ())));

      associationSets.add (new AssociationSet ()
         .setName (ASSO_PRODUCT_CLASS.getName ())
         .setAssociation (ASSO_PRODUCT_CLASS)
         .setEnd1 (
            new AssociationSetEnd().setRole(ROLE_PRODUCT_CLASS)
                  .setEntitySet(Model.CLASS.getName()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_CLASS_PRODUCTS)
               .setEntitySet (getName ())));
      return associationSets;
   }

   @Override
   public List<Association> getAssociations ()
   {
      List<Association> associations = new ArrayList<Association> ();

      associations.add (new Association ()
         .setName (ASSO_PRODUCT_PRODUCT.getName ())
         .setEnd1 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_PRODUCT_PRODUCTS)
               .setMultiplicity (EdmMultiplicity.MANY))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_PRODUCT_PRODUCT)
               .setMultiplicity (EdmMultiplicity.ZERO_TO_ONE)));

      associations.add (new Association ()
         .setName (ASSO_PRODUCT_NODE.getName ())
         .setEnd1 (
            new AssociationEnd ()
               .setType(Model.NODE.getFullQualifiedName())
               .setRole (ROLE_PRODUCT_NODES)
               .setMultiplicity (EdmMultiplicity.MANY))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_NODE_PRODUCT)
               .setMultiplicity (EdmMultiplicity.ONE)));

      associations.add (new Association ()
         .setName (ASSO_PRODUCT_ATTRIBUTE.getName ())
         .setEnd1 (
            new AssociationEnd ()
               .setType(Model.ATTRIBUTE.getFullQualifiedName())
               .setRole (ROLE_PRODUCT_ATTRIBUTES)
               .setMultiplicity (EdmMultiplicity.MANY))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_ATTRIBUTE_PRODUCT)
               .setMultiplicity (EdmMultiplicity.ONE)));

      associations.add (new Association ()
         .setName (ASSO_PRODUCT_CLASS.getName ())
         .setEnd1 (
            new AssociationEnd ()
               .setType(Model.CLASS.getFullQualifiedName())
               .setRole (ROLE_PRODUCT_CLASS)
               .setMultiplicity (EdmMultiplicity.ONE))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_CLASS_PRODUCTS)
               .setMultiplicity (EdmMultiplicity.MANY)));

      return associations;
   }

   @Override
   public int count ()
   {
      return PRODUCT_SERVICE.count ();
   }

   @Override
   public Map getEntities()
   {
      return new ProductsMap();
   }

   @Override
   public AbstractEntity getEntity(KeyPredicate kp)
   {
      return (new ProductsMap()).get(kp.getLiteral());
   }

   @Override
   public List<String> getExpandableNavLinkNames()
   {
      List<String> res = new ArrayList<>(super.getExpandableNavLinkNames());
      res.add("Products");
      res.add("Class");
      res.add("Attributes");
      res.add("Nodes");
      return res;
   }

   @Override
   public List<Map<String, Object>> expand(String navlink_name, String self_url,
         Map<?, AbstractEntity> entities, Map<String, Object> key)
   {
      return Expander.expandFeedSingletonKey(navlink_name, self_url, entities, key, ItemEntitySet.ID);
   }

}
