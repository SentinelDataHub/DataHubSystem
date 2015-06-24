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
package fr.gael.dhus.olingo.v1.entitySet;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmException;
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
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.V1Util;
import fr.gael.dhus.olingo.v1.entity.Collection;
import fr.gael.dhus.olingo.v1.entity.Product;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class CollectionEntitySet extends V1EntitySet<Collection>
{
   private static Logger logger = LogManager
      .getLogger (CollectionEntitySet.class);
   private static CollectionService collectionManager =
      ApplicationContextProvider.getBean (CollectionService.class);

   public static String ENTITY_NAME = "Collection";

   // Entity keys
   public static String ID = "Id";
   public static String NAME = "Name";
   public static String DESCRIPTION = "Description";

   public static final FullQualifiedName ASSO_COLLECTION_PRODUCT =
      new FullQualifiedName (V1Model.NAMESPACE, "Collection_Product");
   public static final String ROLE_COLLECTION_PRODUCTS = "Collection_Products"; // many
   public static final String ROLE_PRODUCT_COLLECTIONS = "Product_Collections"; // many

   public static final FullQualifiedName ASSO_COLLECTION_COLLECTION =
      new FullQualifiedName (V1Model.NAMESPACE, "Collection_Collection");
   public static final String ROLE_COLLECTION_COLLECTIONS =
      "Collection_Collections"; // many
   public static final String ROLE_COLLECTION_PARENT = "Collection_Parent"; // 1

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      List<Property> properties = new ArrayList<Property> ();
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
         new ArrayList<NavigationProperty> ();
      navigationProperties.add (new NavigationProperty ()
         .setName (V1Model.PRODUCT.getName ())
         .setRelationship (ASSO_COLLECTION_PRODUCT)
         .setFromRole (ROLE_PRODUCT_COLLECTIONS)
         .setToRole (ROLE_COLLECTION_PRODUCTS));
      // TODO (OData v3) setContainsTarget(true)
      navigationProperties.add (new NavigationProperty ().setName (getName ())
         .setRelationship (ASSO_COLLECTION_COLLECTION)
         .setFromRole (ROLE_COLLECTION_PARENT)
         .setToRole (ROLE_COLLECTION_COLLECTIONS));

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
      List<AssociationSet> associationSets = new ArrayList<AssociationSet> ();
      associationSets.add (new AssociationSet ()
         .setName (ASSO_COLLECTION_PRODUCT.getName ())
         .setAssociation (ASSO_COLLECTION_PRODUCT)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_PRODUCT_COLLECTIONS)
               .setEntitySet (getName ()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_COLLECTION_PRODUCTS)
               .setEntitySet (V1Model.PRODUCT.getName ())));
      associationSets.add (new AssociationSet ()
         .setName (ASSO_COLLECTION_COLLECTION.getName ())
         .setAssociation (ASSO_COLLECTION_COLLECTION)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_COLLECTION_COLLECTIONS)
               .setEntitySet (getName ()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_COLLECTION_PARENT)
               .setEntitySet (getName ())));
      return associationSets;
   }

   @Override
   public List<Association> getAssociations ()
   {
      List<Association> associations = new ArrayList<Association> ();
      associations.add (new Association ()
         .setName (ASSO_COLLECTION_PRODUCT.getName ())
         .setEnd1 (
            new AssociationEnd ()
               .setType (V1Model.PRODUCT.getFullQualifiedName ())
               .setRole (ROLE_COLLECTION_PRODUCTS)
               .setMultiplicity (EdmMultiplicity.MANY))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_PRODUCT_COLLECTIONS)
               .setMultiplicity (EdmMultiplicity.MANY)));

      associations.add (new Association ()
         .setName (ASSO_COLLECTION_COLLECTION.getName ())
         .setEnd1 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_COLLECTION_COLLECTIONS)
               .setMultiplicity (EdmMultiplicity.MANY))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_COLLECTION_PARENT)
               .setMultiplicity (EdmMultiplicity.ZERO_TO_ONE)));
      return associations;
   }

   @Override
   public Map<String, ODataCallback> getCallbacks (final URI lnk)
   {
      Map<String, ODataCallback> res = new HashMap<String, ODataCallback> ();
      // Expand Products
      res.put ("Products", new OnWriteFeedContent ()
      {
         @Override
         public WriteFeedCallbackResult retrieveFeedResult (
            WriteFeedCallbackContext context) throws ODataApplicationException
         {
            WriteFeedCallbackResult result = new WriteFeedCallbackResult ();
            try
            {
               if (isNavigationFromTo (context, getName (),
                  V1Model.PRODUCT.getName ()))
               {
                  EntityProviderWriteProperties inlineProperties =
                     EntityProviderWriteProperties
                        .serviceRoot (lnk)
                        .expandSelectTree (
                           context.getCurrentExpandSelectTreeNode ())
                        .selfLink (context.getSelfLink ()).build ();

                  User u = V1Util.getCurrentUser ();
                  List<fr.gael.dhus.database.object.Product> products =
                     new ArrayList<fr.gael.dhus.database.object.Product> ();
                  try
                  {
                     products =
                        collectionManager.getAuthorizedProducts (
                           ((Long) (context.getEntryData ().get (ID))),
                           (u == null) ? null : u);
                  }
                  catch (Exception e)
                  {
                  }
                  List<Map<String, Object>> feedData =
                     new ArrayList<Map<String, Object>> ();
                  for (fr.gael.dhus.database.object.Product product : products)
                  {
                     if (product == null) continue;
                     Product p = Product.fromDatabase (product);
                     if (p == null) continue;
                     feedData.add (p.toEntityResponse (lnk.toString ()));
                  }
                  result.setFeedData (feedData);
                  result.setInlineProperties (inlineProperties);
               }
            }
            catch (EdmException e)
            {
               logger.error (
                  "Error when including Products in Collection Response", e);
            }
            return result;
         }
      });
      // Expand Collections
      res.put ("Collections", new OnWriteFeedContent ()
      {

         @Override
         public WriteFeedCallbackResult retrieveFeedResult (
            WriteFeedCallbackContext context) throws ODataApplicationException
         {
            WriteFeedCallbackResult result = new WriteFeedCallbackResult ();
            try
            {
               if (isNavigationFromTo (context, getName (), getName ()))
               {
                  EntityProviderWriteProperties inlineProperties =
                     EntityProviderWriteProperties
                        .serviceRoot (lnk)
                        .expandSelectTree (
                           context.getCurrentExpandSelectTreeNode ())
                        .selfLink (context.getSelfLink ()).build ();

                  User u = V1Util.getCurrentUser ();
                  Set<fr.gael.dhus.database.object.Collection> collections =
                     new HashSet<fr.gael.dhus.database.object.Collection> ();
                  try
                  {
                     collections =
                        collectionManager.getAuthorizedSubCollections (
                           (Long) context.getEntryData ().get (ID),
                           (u == null ? null : u));
                  }
                  catch (Exception e)
                  {
                  }

                  List<Map<String, Object>> feedData =
                     new ArrayList<Map<String, Object>> ();
                  for (fr.gael.dhus.database.object.Collection collection : collections)
                  {
                     if (collection == null) continue;
                     Collection c = Collection.fromDatabase (collection);
                     if (c == null) continue;
                     feedData.add (c.toEntityResponse (lnk.toString ()));
                  }
                  result.setFeedData (feedData);
                  result.setInlineProperties (inlineProperties);
               }
            }
            catch (EdmException e)
            {
               logger.error (
                  "Error when including SubCollections in Collection Response",
                  e);
            }
            return result;
         }
      });
      return res;
   }

   @Override
   public int count ()
   {
      User u = V1Util.getCurrentUser ();
      return collectionManager.countAuthorizedSubCollections (null, u);
   }
}
