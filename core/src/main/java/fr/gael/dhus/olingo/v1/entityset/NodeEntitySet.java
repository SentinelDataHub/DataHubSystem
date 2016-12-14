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

import fr.gael.dhus.olingo.v1.Expander;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;
import fr.gael.dhus.olingo.v1.entity.Node;

import java.util.ArrayList;
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
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;

public class NodeEntitySet extends AbstractEntitySet<Node>
{
   public static final String ENTITY_NAME = "Node";

   // Entity keys
   public static final String CHILDREN_NUMBER = "ChildrenNumber";
   public static final String VALUE = "Value";
   public static final String PATH = "Path";

   public static final FullQualifiedName ASSO_NODE_NODE =
         new FullQualifiedName(Model.NAMESPACE, "Node_Node");
   public static final String ROLE_NODE_NODES = "Node_Nodes"; // many
   public static final String ROLE_NODE_PARENT = "Node_Node"; // 1

   public static final FullQualifiedName ASSO_NODE_CLASS =
            new FullQualifiedName(Model.NAMESPACE, "Node_Class");
      public static final String ROLE_NODE_CLASS = "Node_Class"; // 1
      public static final String ROLE_CLASS_NODES = "Class_Nodes"; // 1

   public static final FullQualifiedName ASSO_NODE_ATTRIBUTE =
         new FullQualifiedName(Model.NAMESPACE, "Node_Attribute");
   public static final String ROLE_NODE_ATTRIBUTES = "Node_Attributes"; // many
   public static final String ROLE_ATTRIBUTE_NODE = "Attribute_Node"; // 1

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      EntityType res = Model.ITEM.getEntityType();

      res.getProperties ().add (
         new SimpleProperty ().setName (CHILDREN_NUMBER).setType (
            EdmSimpleTypeKind.Int64));
      res.getProperties ().add (
         new SimpleProperty ().setName (VALUE).setType (
            EdmSimpleTypeKind.String));

      // Navigation Properties
      List<NavigationProperty> navigationProperties =
         new ArrayList<NavigationProperty> ();
      // TODO (OData v3) setContainsTarget(true)
      navigationProperties.add (new NavigationProperty ().setName (getName ())
         .setRelationship (ASSO_NODE_NODE).setFromRole (ROLE_NODE_PARENT)
         .setToRole (ROLE_NODE_NODES));
      navigationProperties.add (new NavigationProperty ()
         .setName(Model.ATTRIBUTE.getName())
         .setRelationship (ASSO_NODE_ATTRIBUTE)
         .setFromRole (ROLE_ATTRIBUTE_NODE).setToRole (ROLE_NODE_ATTRIBUTES));
      navigationProperties.add (new NavigationProperty ()
         .setName ("Class")
         .setRelationship (ASSO_NODE_CLASS)
         .setFromRole (ROLE_CLASS_NODES)
         .setToRole (ROLE_NODE_CLASS));

      // TODO (OData v3) setAbstract(true) setBaseType(ENTITY_ITEM)
      return res.setName (ENTITY_NAME)
         .setNavigationProperties (navigationProperties).setHasStream (true);
   }

   @Override
   public List<AssociationSet> getAssociationSets ()
   {
      List<AssociationSet> associationSets = new ArrayList<AssociationSet> ();
      associationSets.add (new AssociationSet ()
         .setName (ASSO_NODE_NODE.getName ())
         .setAssociation (ASSO_NODE_NODE)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_NODE_NODES).setEntitySet (
               getName ()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_NODE_PARENT).setEntitySet (
               getName ())));
      associationSets.add (new AssociationSet ()
         .setName (ASSO_NODE_ATTRIBUTE.getName ())
         .setAssociation (ASSO_NODE_ATTRIBUTE)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_NODE_ATTRIBUTES)
               .setEntitySet(Model.ATTRIBUTE.getName()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_ATTRIBUTE_NODE)
               .setEntitySet (getName ())));
      associationSets.add (new AssociationSet ()
         .setName (ASSO_NODE_CLASS.getName ())
         .setAssociation (ASSO_NODE_CLASS)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_NODE_CLASS)
               .setEntitySet(Model.CLASS.getName()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_CLASS_NODES)
               .setEntitySet (getName ())));
      return associationSets;
   }

   @Override
   public List<Association> getAssociations ()
   {
      List<Association> associations = new ArrayList<Association> ();
      associations.add (new Association ()
         .setName (ASSO_NODE_NODE.getName ())
         .setEnd1 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_NODE_NODES)
               .setMultiplicity (EdmMultiplicity.MANY))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_NODE_PARENT)
               .setMultiplicity (EdmMultiplicity.ZERO_TO_ONE)));

      associations.add (new Association ()
      .setName (ASSO_NODE_ATTRIBUTE.getName ())
      .setEnd1 (
         new AssociationEnd ()
            .setType(Model.ATTRIBUTE.getFullQualifiedName())
            .setRole (ROLE_NODE_ATTRIBUTES)
            .setMultiplicity (EdmMultiplicity.MANY))
      .setEnd2 (
         new AssociationEnd ().setType (getFullQualifiedName ())
            .setRole (ROLE_ATTRIBUTE_NODE)
            .setMultiplicity (EdmMultiplicity.ONE)));

      associations.add (new Association ()
      .setName (ASSO_NODE_CLASS.getName ())
      .setEnd1 (
         new AssociationEnd ()
            .setType(Model.CLASS.getFullQualifiedName())
            .setRole (ROLE_NODE_CLASS)
            .setMultiplicity (EdmMultiplicity.ONE))
      .setEnd2 (
         new AssociationEnd ().setType (getFullQualifiedName ())
            .setRole (ROLE_CLASS_NODES)
            .setMultiplicity (EdmMultiplicity.MANY)));
      return associations;
   }

   @Override
   public boolean isTopLevel()
   {
      return false;
   }

   @Override
   public List<String> getExpandableNavLinkNames()
   {
      List<String> res = new ArrayList<>(super.getExpandableNavLinkNames());
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
