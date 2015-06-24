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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.ODataCallback;
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
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.V1Util;
import fr.gael.dhus.olingo.v1.entity.Attribute;
import fr.gael.dhus.olingo.v1.entity.Node;
import fr.gael.dhus.olingo.v1.entity.Product;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class ProductEntitySet extends V1EntitySet<Product>
{
   private static Logger logger = LogManager.getLogger (ProductEntitySet.class);
   private static ProductService productManager = ApplicationContextProvider
      .getBean (ProductService.class);

   public static String ENTITY_NAME = "Product";

   // Entity keys
   public static String CONTENT_DATE = "ContentDate";
   public static String INGESTION_DATE = "IngestionDate";
   public static String CREATION_DATE = "CreationDate";
   public static String EVICTION_DATE = "EvictionDate";
   public static String CONTENT_GEOMETRY = "ContentGeometry";
   public static String METALINK = "Metalink";
   public static String CHECKSUM = "Checksum";

   public static final FullQualifiedName ASSO_PRODUCT_PRODUCT =
      new FullQualifiedName (V1Model.NAMESPACE, "Product_Product");
   public static final String ROLE_PRODUCT_PRODUCTS = "Product_Products"; // many
   public static final String ROLE_PRODUCT_PRODUCT = "Product_Product"; // 1

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      EntityType res = V1Model.NODE.getEntityType ();

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
      properties.add (new ComplexProperty ().setName (CONTENT_DATE).setType (
         V1Model.TIME_RANGE));
      properties.add (new ComplexProperty ().setName (CHECKSUM).setType (
         V1Model.CHECKSUM));
      properties.add (new SimpleProperty ().setName (CONTENT_GEOMETRY).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (METALINK).setType (
         EdmSimpleTypeKind.String));

      // Navigation Properties
      List<NavigationProperty> navigationProperties =
         res.getNavigationProperties (); // new ArrayList<NavigationProperty>
                                         // ();
      navigationProperties.add (new NavigationProperty ().setName (getName ())
         .setRelationship (ASSO_PRODUCT_PRODUCT)
         .setFromRole (ROLE_PRODUCT_PRODUCT).setToRole (ROLE_PRODUCT_PRODUCTS));

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
      return associations;
   }

   @Override
   public int count ()
   {
      User u = V1Util.getCurrentUser ();
      return productManager.countAuthorizedProducts (u);
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
               if (isNavigationFromTo (context, getName (), getName ()))
               {
                  EntityProviderWriteProperties inlineProperties =
                     EntityProviderWriteProperties
                        .serviceRoot (lnk)
                        .expandSelectTree (
                           context.getCurrentExpandSelectTreeNode ())
                        .selfLink (context.getSelfLink ()).build ();

                  User u = V1Util.getCurrentUser ();
                  fr.gael.dhus.database.object.Product product =
                     productManager.getProduct ((String) context
                        .getEntryData ().get (ItemEntitySet.ID), u);

                  List<Map<String, Object>> feedData =
                     new ArrayList<Map<String, Object>> ();
                  if (product != null)
                  {
                     Product p = Product.fromDatabase (product);
                     if (p != null)
                     {
                        for (Product subP : p.getProducts ().values ())
                        {
                           feedData.add (subP.toEntityResponse (lnk.toString ()));
                        }
                     }
                  }
                  result.setFeedData (feedData);
                  result.setInlineProperties (inlineProperties);
               }
            }
            catch (Exception e)
            {
               logger.error (
                  "Error when including Products in Product Response", e);
            }
            return result;
         }
      });
      // Expand Attributes
      res.put ("Attributes", new OnWriteFeedContent ()
      {
         @Override
         public WriteFeedCallbackResult retrieveFeedResult (
            WriteFeedCallbackContext context) throws ODataApplicationException
         {
            WriteFeedCallbackResult result = new WriteFeedCallbackResult ();
            try
            {
               if (isNavigationFromTo (context, getName (),
                  V1Model.ATTRIBUTE.getName ()))
               {
                  EntityProviderWriteProperties inlineProperties =
                     EntityProviderWriteProperties
                        .serviceRoot (lnk)
                        .expandSelectTree (
                           context.getCurrentExpandSelectTreeNode ())
                        .selfLink (context.getSelfLink ()).build ();

                  User u = V1Util.getCurrentUser ();
                  fr.gael.dhus.database.object.Product product =
                     productManager.getProduct ((String) context
                        .getEntryData ().get (ItemEntitySet.ID), u);

                  List<Map<String, Object>> feedData =
                     new ArrayList<Map<String, Object>> ();
                  if (product != null)
                  {
                     Product p = Product.fromDatabase (product);
                     if (p != null)
                     {
                        for (Attribute attr : p.getAttributes ().values ())
                        {
                           feedData.add (attr.toEntityResponse (lnk.toString ()));
                        }
                     }
                  }
                  result.setFeedData (feedData);
                  result.setInlineProperties (inlineProperties);
               }
            }
            catch (Exception e)
            {
               logger.error (
                  "Error when including Attributes in Product Response", e);
            }
            return result;
         }
      });
      // Expand Nodes
      res.put ("Nodes", new OnWriteFeedContent ()
      {
         @Override
         public WriteFeedCallbackResult retrieveFeedResult (
            WriteFeedCallbackContext context) throws ODataApplicationException
         {
            WriteFeedCallbackResult result = new WriteFeedCallbackResult ();
            try
            {
               if (isNavigationFromTo (context, getName (),
                  V1Model.NODE.getName ()))
               {
                  EntityProviderWriteProperties inlineProperties =
                     EntityProviderWriteProperties
                        .serviceRoot (lnk)
                        .expandSelectTree (
                           context.getCurrentExpandSelectTreeNode ())
                        .selfLink (context.getSelfLink ()).build ();

                  User u = V1Util.getCurrentUser ();
                  fr.gael.dhus.database.object.Product product =
                     productManager.getProduct ((String) context
                        .getEntryData ().get (ItemEntitySet.ID), u);

                  List<Map<String, Object>> feedData =
                     new ArrayList<Map<String, Object>> ();
                  if (product != null)
                  {
                     Product p = Product.fromDatabase (product);
                     if (p != null)
                     {
                        for (Node node : p.getNodes ().values ())
                        {
                           feedData.add (node.toEntityResponse (lnk.toString ()));
                        }
                     }
                  }
                  result.setFeedData (feedData);
                  result.setInlineProperties (inlineProperties);
               }
            }
            catch (Exception e)
            {
               logger.error ("Error when including Nodes in Product Response",
                  e);
            }
            return result;
         }
      });
      return res;
   }
}
