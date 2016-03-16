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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import fr.gael.dhus.datastore.processing.ProcessingUtils;
import fr.gael.dhus.olingo.v1.V1Model;
/**
 * This class denotes Drb Class description of a product.
 *
 */
public class ClassEntitySet extends 
   V1EntitySet<fr.gael.dhus.olingo.v1.entity.Class>
{
   public static final String ENTITY_NAME = "Class";

   // Entity keys
   public static final String ID = "Id";
   public static final String URI = "Uri";

   public static final FullQualifiedName ASSO_CLASS_CLASS =
         new FullQualifiedName (V1Model.NAMESPACE, "Class_Class");
   public static final String ROLE_CLASS_CLASSES = "Class_Classes"; // many
   public static final String ROLE_CLASS_PARENT = "Class_Classes"; // 1

   private static Logger logger = LogManager.getLogger (ClassEntitySet.class);

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
      .setName (ID)
      .setType (EdmSimpleTypeKind.String)
      .setFacets (new Facets ().setNullable (false))
      .setCustomizableFeedMappings (
         new CustomizableFeedMappings ()
            .setFcTargetPath (EdmTargetPath.SYNDICATION_TITLE)));
      
      properties.add (new SimpleProperty ().setName (URI).setType (
         EdmSimpleTypeKind.String));
     
      // Navigation Properties
      List<NavigationProperty> navigationProperties =
         new ArrayList<NavigationProperty> ();
      // TODO (OData v3) setContainsTarget(true)
      navigationProperties.add (new NavigationProperty ().setName (getName ())
         .setRelationship (ASSO_CLASS_CLASS).setFromRole (ROLE_CLASS_PARENT)
         .setToRole (ROLE_CLASS_CLASSES));

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
      associationSets.add (new AssociationSet ()
         .setName (ASSO_CLASS_CLASS.getName ())
         .setAssociation (ASSO_CLASS_CLASS)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_CLASS_CLASSES).setEntitySet (
               getName ()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_CLASS_PARENT).setEntitySet (
               getName ())));
      return associationSets;
   }

   @Override
   public List<Association> getAssociations ()
   {
      List<Association> associations = new ArrayList<Association> ();
      associations.add (new Association ()
         .setName (ASSO_CLASS_CLASS.getName ())
         .setEnd1 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_CLASS_CLASSES)
               .setMultiplicity (EdmMultiplicity.MANY))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_CLASS_PARENT)
               .setMultiplicity (EdmMultiplicity.MANY)));

      return associations;
   }

   @Override
   public Map<String, ODataCallback> getCallbacks (final URI lnk)
   {
      Map<String, ODataCallback> res = new HashMap<String, ODataCallback> ();
      // Expand Nodes
      res.put ("Classes", new OnWriteFeedContent ()
      {
         @Override
         public WriteFeedCallbackResult retrieveFeedResult (
            WriteFeedCallbackContext context) throws ODataApplicationException
         {
            WriteFeedCallbackResult result = new WriteFeedCallbackResult ();
            try
            {
               if (isNavigationFromTo (context, getName (),
                  V1Model.CLASS.getName ()))
               {
                  EntityProviderWriteProperties inlineProperties =
                     EntityProviderWriteProperties
                        .serviceRoot (lnk)
                        .expandSelectTree (
                           context.getCurrentExpandSelectTreeNode ())
                        .selfLink (context.getSelfLink ()).build ();

                  String uri = (String)context.getEntryData ().get (URI);
                  
                  List<Map<String, Object>> feedData =
                     new ArrayList<Map<String, Object>> ();
                  
                  List<String>super_classes=ProcessingUtils.getSuperClass(uri);
                  for (String cl: super_classes)
                  {
                     fr.gael.dhus.olingo.v1.entity.Class sclass = 
                        new  fr.gael.dhus.olingo.v1.entity.Class (cl);
                     feedData.add (sclass.toEntityResponse (lnk.toString ()));
                  }
                  result.setFeedData (feedData);
                  result.setInlineProperties (inlineProperties);
               }
            }
            catch (EdmException e)
            {
               logger.error ("Error when including Nodes in Node Response", e);
            }
            return result;
         }
      });
      return res;
   }
}
