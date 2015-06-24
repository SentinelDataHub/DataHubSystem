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
package fr.gael.dhus.olingo.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.api.exception.ODataException;

import fr.gael.dhus.olingo.v1.entitySet.AttributeEntitySet;
import fr.gael.dhus.olingo.v1.entitySet.CollectionEntitySet;
import fr.gael.dhus.olingo.v1.entitySet.ItemEntitySet;
import fr.gael.dhus.olingo.v1.entitySet.NodeEntitySet;
import fr.gael.dhus.olingo.v1.entitySet.ProductEntitySet;
import fr.gael.dhus.olingo.v1.entitySet.V1EntitySet;

/**
 * Builds an Entity Data Model (schema).
 */
public class V1Model extends EdmProvider
{
   /** The namespace prefixes each entity in the Schema */
   public static final String NAMESPACE = "DHuS";

   /** An entity container contains EntitySets and AssociationSets */
   public static final String ENTITY_CONTAINER = "DHuSData";

   private static final Map<String, EntityType> entities;
   private static final Map<String, EntitySet> entitySets;
   private static final Map<String, AssociationSet> associationSets;
   private static final Map<String, Association> associations;
   private static final Map<String, ComplexType> complexTypes;
   private static final Map<String, V1EntitySet<?>> v1entitieSets;

   public static AttributeEntitySet ATTRIBUTE = new AttributeEntitySet ();
   public static CollectionEntitySet COLLECTION = new CollectionEntitySet ();
   public static ItemEntitySet ITEM = new ItemEntitySet ();
   public static NodeEntitySet NODE = new NodeEntitySet ();
   public static ProductEntitySet PRODUCT = new ProductEntitySet ();

   public static final FullQualifiedName TIME_RANGE = new FullQualifiedName (
      V1Model.NAMESPACE, "TimeRange");
   public static String TIME_RANGE_START = "Start";
   public static String TIME_RANGE_END = "End";

   public static final FullQualifiedName CHECKSUM = new FullQualifiedName (
      V1Model.NAMESPACE, "Checksum");
   public static String ALGORITHM = "Algorithm";
   public static String VALUE = "Value";

   static
   {
      entities = new HashMap<String, EntityType> ();
      entitySets = new HashMap<String, EntitySet> ();
      associationSets = new HashMap<String, AssociationSet> ();
      associations = new HashMap<String, Association> ();
      complexTypes = new HashMap<String, ComplexType> ();
      v1entitieSets = new HashMap<String, V1EntitySet<?>> ();
      addEntitySet (ATTRIBUTE);
      addEntitySet (COLLECTION);
      addEntitySet (NODE);
      addEntitySet (PRODUCT);
      for (ComplexType ctype : getComplexTypes ())
      {
         complexTypes.put (ctype.getName (), ctype);
      }
   }

   private static void addEntitySet (V1EntitySet<?> entitySet)
   {
      entities.put (entitySet.getEntityName (), entitySet.getEntityType ());
      entitySets.put (entitySet.getName (), entitySet.getEntitySet ());
      v1entitieSets.put (entitySet.getName (), entitySet);
      for (AssociationSet assoc : entitySet.getAssociationSets ())
      {
         associationSets.put (assoc.getName (), assoc);
      }
      for (Association assoc : entitySet.getAssociations ())
      {
         associations.put (assoc.getName (), assoc);
      }
   }

   public static V1EntitySet<?> getEntitySet (String name)
   {
      return v1entitieSets.get (name);
   }

   private static List<ComplexType> getComplexTypes ()
   {
      List<ComplexType> complexTypeList = new ArrayList<ComplexType> ();

      List<Property> timeRangeProperties = new ArrayList<Property> ();
      timeRangeProperties.add (new SimpleProperty ().setName (TIME_RANGE_START)
         .setType (EdmSimpleTypeKind.DateTime));
      timeRangeProperties.add (new SimpleProperty ().setName (TIME_RANGE_END)
         .setType (EdmSimpleTypeKind.DateTime));
      complexTypeList.add (new ComplexType ().setName (TIME_RANGE.getName ())
         .setProperties (timeRangeProperties));

      List<Property> checksumProperties = new ArrayList<Property> ();
      checksumProperties.add (new SimpleProperty ().setName (ALGORITHM)
         .setType (EdmSimpleTypeKind.String));
      checksumProperties.add (new SimpleProperty ().setName (VALUE).setType (
         EdmSimpleTypeKind.String));
      complexTypeList.add (new ComplexType ().setName (CHECKSUM.getName ())
         .setProperties (checksumProperties));

      return complexTypeList;
   }

   /**
    * Answers the $metadata request. Returns the complete structural information
    * in order to build the metadata document and the service document
    */
   @Override
   public List<Schema> getSchemas () throws ODataException
   {
      List<Schema> schemas = new ArrayList<Schema> ();

      Schema schema = new Schema ();
      schema.setNamespace (NAMESPACE);

      schema.setEntityTypes (new ArrayList<EntityType> (entities.values ()));
      schema.setComplexTypes (new ArrayList<ComplexType> (complexTypes
         .values ()));
      schema.setAssociations (new ArrayList<Association> (associations
         .values ()));

      EntityContainer entityContainer = new EntityContainer ();
      entityContainer.setName (ENTITY_CONTAINER).setDefaultEntityContainer (
         true);
      entityContainer.setEntitySets (new ArrayList<EntitySet> (entitySets
         .values ()));
      entityContainer.setAssociationSets (new ArrayList<AssociationSet> (
         associationSets.values ()));

      schema.setEntityContainers (Collections.singletonList (entityContainer));

      schemas.add (schema);

      return schemas;
   }

   /**
    * Returns an Entity Type according to the full qualified name specified. The
    * Entity Type holds all information about its structure like simple
    * properties, complex properties, navigation properties and the definition
    * of its key property (or properties).
    */
   @Override
   public EntityType getEntityType (FullQualifiedName edmFQName)
      throws ODataException
   {
      if (edmFQName != null && edmFQName.getNamespace ().equals (NAMESPACE))
      {
         return entities.get (edmFQName.getName ());
      }
      return null;
   }

   /**
    * Returns the description of a Complex Type.
    */
   @Override
   public ComplexType getComplexType (FullQualifiedName edmFQName)
      throws ODataException
   {
      if (edmFQName != null && edmFQName.getNamespace ().equals (NAMESPACE))
      {
         return complexTypes.get (edmFQName.getName ());
      }
      return null;
   }

   /**
    * Returns the description of an Association.
    */
   @Override
   public Association getAssociation (FullQualifiedName edmFQName)
      throws ODataException
   {
      if (edmFQName != null && edmFQName.getNamespace ().equals (NAMESPACE))
      {
         return associations.get (edmFQName.getName ());
      }
      return null;
   }

   /**
    * Returns the description of an EntityContainer.
    */
   @Override
   public EntityContainerInfo getEntityContainerInfo (String name)
      throws ODataException
   {
      if (name == null || name.equals (ENTITY_CONTAINER))
      {
         return new EntityContainerInfo ().setName (ENTITY_CONTAINER)
            .setDefaultEntityContainer (true);
      }
      return null;
   }

   /**
    * Returns the description of an EntitySet.
    */
   @Override
   public EntitySet getEntitySet (String entityContainer, String name)
      throws ODataException
   {
      if (name == null) return null;
      if (entityContainer == null || entityContainer.equals (ENTITY_CONTAINER))
      {
         return entitySets.get (name);
      }
      return null;
   }

   /**
    * Returns the description of an AssociationSet.
    */
   @Override
   public AssociationSet getAssociationSet (String entityContainer,
      FullQualifiedName association, String sourceEntitySetName,
      String sourceEntitySetRole) throws ODataException
   {
      if (association == null || association.getName () == null) return null;
      if (entityContainer == null || entityContainer.equals (ENTITY_CONTAINER))
      {
         return associationSets.get (association.getName ());
      }
      return null;
   }
}
