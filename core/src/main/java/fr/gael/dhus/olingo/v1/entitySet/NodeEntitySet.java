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
import org.apache.olingo.odata2.api.edm.EdmException;
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
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;

import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.entity.Attribute;
import fr.gael.dhus.olingo.v1.entity.Node;
import fr.gael.drb.DrbNode;

public class NodeEntitySet extends V1EntitySet<Node>
{
   private static Logger logger = LogManager.getLogger (NodeEntitySet.class);
   public static String ENTITY_NAME = "Node";

   // Entity keys
   public static String CHILDREN_NUMBER = "ChildrenNumber";
   public static String VALUE = "Value";
   public static String PATH = "Path";

   public static final FullQualifiedName ASSO_NODE_NODE =
      new FullQualifiedName (V1Model.NAMESPACE, "Node_Node");
   public static final String ROLE_NODE_NODES = "Node_Nodes"; // many
   public static final String ROLE_NODE_PARENT = "Node_Node"; // 1

   public static final FullQualifiedName ASSO_NODE_ATTRIBUTE =
      new FullQualifiedName (V1Model.NAMESPACE, "Node_Attribute");
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
      EntityType res = V1Model.ITEM.getEntityType ();

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
         .setName (V1Model.ATTRIBUTE.getName ())
         .setRelationship (ASSO_NODE_ATTRIBUTE)
         .setFromRole (ROLE_ATTRIBUTE_NODE).setToRole (ROLE_NODE_ATTRIBUTES));

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
               .setEntitySet (V1Model.ATTRIBUTE.getName ()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_ATTRIBUTE_NODE)
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
               .setType (V1Model.ATTRIBUTE.getFullQualifiedName ())
               .setRole (ROLE_NODE_ATTRIBUTES)
               .setMultiplicity (EdmMultiplicity.MANY))
         .setEnd2 (
            new AssociationEnd ().setType (getFullQualifiedName ())
               .setRole (ROLE_ATTRIBUTE_NODE)
               .setMultiplicity (EdmMultiplicity.ONE)));
      return associations;
   }

   @Override
   public Map<String, ODataCallback> getCallbacks (final URI lnk)
   {
      Map<String, ODataCallback> res = new HashMap<String, ODataCallback> ();
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

                  DrbNode drbnode =
                     (DrbNode) context.getEntryData ().get (PATH);
                  List<Map<String, Object>> feedData =
                     new ArrayList<Map<String, Object>> ();
                  if (drbnode != null)
                  {
                     Node node = new Node (drbnode);
                     if (node != null && node.getAttributes () != null)
                     {
                        for (Attribute attr : node.getAttributes ().values ())
                        {
                           feedData.add (attr.toEntityResponse (lnk.toString ()));
                        }
                     }
                  }
                  result.setFeedData (feedData);
                  result.setInlineProperties (inlineProperties);
               }
            }
            catch (EdmException e)
            {
               logger.error (
                  "Error when including Attributes in Node Response", e);
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

                  DrbNode drbnode =
                     (DrbNode) context.getEntryData ().get (PATH);
                  List<Map<String, Object>> feedData =
                     new ArrayList<Map<String, Object>> ();
                  if (drbnode != null)
                  {
                     Node node = new Node (drbnode);
                     if (node != null)
                     {
                        for (Node snode : node.getNodes ().values ())
                        {
                           feedData.add (snode.toEntityResponse (lnk
                              .toString ()));
                        }
                     }
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
