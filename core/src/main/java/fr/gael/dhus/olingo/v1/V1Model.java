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

import fr.gael.dhus.olingo.v1.entityset.RestrictionEntitySet;
import fr.gael.dhus.olingo.v1.entityset.SystemRoleEntitySet;
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

import fr.gael.dhus.olingo.v1.entityset.AttributeEntitySet;
import fr.gael.dhus.olingo.v1.entityset.ClassEntitySet;
import fr.gael.dhus.olingo.v1.entityset.CollectionEntitySet;
import fr.gael.dhus.olingo.v1.entityset.ConnectionEntitySet;
import fr.gael.dhus.olingo.v1.entityset.ItemEntitySet;
import fr.gael.dhus.olingo.v1.entityset.NetworkEntitySet;
import fr.gael.dhus.olingo.v1.entityset.NetworkStatisticEntitySet;
import fr.gael.dhus.olingo.v1.entityset.NodeEntitySet;
import fr.gael.dhus.olingo.v1.entityset.ProductEntitySet;
import fr.gael.dhus.olingo.v1.entityset.SynchronizerEntitySet;
import fr.gael.dhus.olingo.v1.entityset.UserEntitySet;
import fr.gael.dhus.olingo.v1.entityset.UserSynchronizerEntitySet;
import fr.gael.dhus.olingo.v1.entityset.V1EntitySet;

/**
 * Builds an Entity Data Model (schema).
 */
public class V1Model extends EdmProvider
{
   /** The namespace prefixes each entity in the Schema */
   public static final String NAMESPACE = "DHuS";

   /** An entity container contains EntitySets and AssociationSets */
   public static final String ENTITY_CONTAINER = "DHuSData";

   public static final AttributeEntitySet ATTRIBUTE = new AttributeEntitySet ();
   public static final CollectionEntitySet COLLECTION =
      new CollectionEntitySet ();
   public static final ItemEntitySet ITEM = new ItemEntitySet ();
   public static final NodeEntitySet NODE = new NodeEntitySet ();
   public static final ProductEntitySet PRODUCT = new ProductEntitySet ();
   public static final ClassEntitySet CLASS = new ClassEntitySet ();
   public static final SynchronizerEntitySet SYNCHRONIZER =
      new SynchronizerEntitySet ();
   public static final UserEntitySet USER = new UserEntitySet ();
   public static final ConnectionEntitySet CONNECTION = new ConnectionEntitySet ();
   public static final NetworkEntitySet NETWORK = new NetworkEntitySet ();
   public static final NetworkStatisticEntitySet NETWORKSTATISTIC = 
      new NetworkStatisticEntitySet ();
   public static final RestrictionEntitySet RESTRICTION =
         new RestrictionEntitySet ();
   public static final SystemRoleEntitySet SYSTEM_ROLE =
         new SystemRoleEntitySet ();
   public static final UserSynchronizerEntitySet USER_SYNCHRONIZER =
         new UserSynchronizerEntitySet();

   public static final FullQualifiedName TIME_RANGE = new FullQualifiedName (
      V1Model.NAMESPACE, "TimeRange");
   public static final String TIME_RANGE_START = "Start";
   public static final String TIME_RANGE_END = "End";

   public static final FullQualifiedName CHECKSUM = new FullQualifiedName (
      V1Model.NAMESPACE, "Checksum");
   public static final String ALGORITHM = "Algorithm";
   public static final String VALUE = "Value";

   private static final Map<String, V1EntitySet<?>> ENTITYSETS;
   private static final Map<String, ComplexType> COMPLEX_TYPES;

   static
   {
      COMPLEX_TYPES = new HashMap<> ();
      ENTITYSETS = new HashMap<> ();
      addEntitySet (ATTRIBUTE);
      addEntitySet (COLLECTION);
      addEntitySet (NODE);
      addEntitySet (PRODUCT);
      addEntitySet (CLASS);
      addEntitySet (SYNCHRONIZER);
      addEntitySet (USER);
      addEntitySet (CONNECTION);
      addEntitySet (NETWORK);
      addEntitySet (NETWORKSTATISTIC);
      addEntitySet (RESTRICTION);
      addEntitySet (SYSTEM_ROLE);
      addEntitySet(USER_SYNCHRONIZER);
      for (ComplexType ctype : getComplexTypes ())
      {
         COMPLEX_TYPES.put (ctype.getName (), ctype);
      }
   }

   private static void addEntitySet (V1EntitySet<?> entity_set)
   {
      ENTITYSETS.put (entity_set.getName (), entity_set);
   }

   public static V1EntitySet<?> getEntitySet (String name)
   {
      return ENTITYSETS.get (name);
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

      fr.gael.dhus.database.object.User u = V1Util.getCurrentUser ();

      ArrayList<EntityType> entities = new ArrayList<> ();
      ArrayList<EntitySet> entitysets = new ArrayList<> ();
      ArrayList<Association> associations = new ArrayList<> ();
      ArrayList<AssociationSet> association_sets = new ArrayList<> ();

      for (V1EntitySet<?> entitySet : ENTITYSETS.values ())
      {
         if (entitySet.isAuthorized (u))
         {
            entities.add(entitySet.getEntityType ());
            entitysets.add(entitySet.getEntitySet ());
            associations.addAll(entitySet.getAssociations ());
            association_sets.addAll (entitySet.getAssociationSets ());
         }
      }

      schema.setEntityTypes (entities);
      schema.setComplexTypes (new ArrayList<ComplexType> (COMPLEX_TYPES
         .values ()));
      schema.setAssociations (new ArrayList<Association> (associations));

      EntityContainer entityContainer = new EntityContainer ();
      entityContainer.setName (ENTITY_CONTAINER).setDefaultEntityContainer (
         true);

      entityContainer.setEntitySets (entitysets);
      entityContainer.setAssociationSets (new ArrayList<AssociationSet> (
         association_sets));

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
   public EntityType getEntityType (FullQualifiedName edm_fq_name)
      throws ODataException
   {
      if (edm_fq_name != null && edm_fq_name.getNamespace ().equals (NAMESPACE))
      {
         return ENTITYSETS.get (V1EntitySet
            .generateEntitySetName (edm_fq_name.getName ())).getEntityType ();
      }
      return null;
   }

   /**
    * Returns the description of a Complex Type.
    */
   @Override
   public ComplexType getComplexType (FullQualifiedName edm_fq_name)
      throws ODataException
   {
      if (edm_fq_name != null && edm_fq_name.getNamespace ().equals (NAMESPACE))
      {
         return COMPLEX_TYPES.get (edm_fq_name.getName ());
      }
      return null;
   }

   /**
    * Returns the description of an Association.
    */
   @Override
   public Association getAssociation (FullQualifiedName edm_fq_name)
      throws ODataException
   {
      if (edm_fq_name != null && edm_fq_name.getNamespace ().equals (NAMESPACE))
      {
         String assocName = edm_fq_name.getName ();
         String entity = assocName.substring (0, assocName.indexOf ("_"));
         for (Association assoc : ENTITYSETS.get (
            V1EntitySet.generateEntitySetName (entity)).getAssociations ())
         {
            if (assoc.getName ().equals (edm_fq_name.getName ()))
            {
               return assoc;
            }
         }
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
   public EntitySet getEntitySet (String entity_container, String name)
      throws ODataException
   {
      if (name == null) return null;
      if (entity_container == null ||
         entity_container.equals (ENTITY_CONTAINER))
      {
         return ENTITYSETS.get (name).getEntitySet ();
      }
      return null;
   }

   /**
    * Returns the description of an AssociationSet.
    */
   @Override
   public AssociationSet getAssociationSet (String entity_container,
      FullQualifiedName association, String source_entity_set_name,
      String source_entity_set_role) throws ODataException
   {
      if (association == null || association.getName () == null) return null;
      if (entity_container == null ||
         entity_container.equals (ENTITY_CONTAINER))
      {
         for (AssociationSet assoc : ENTITYSETS.get (
            source_entity_set_name).getAssociationSets ())
         {
            if (assoc.getName ().equals (association.getName ()))
            {
               return assoc;
            }
         }
      }
      return null;
   }
}
