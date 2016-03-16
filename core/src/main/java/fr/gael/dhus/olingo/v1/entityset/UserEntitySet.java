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

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.V1Util;
import fr.gael.dhus.olingo.v1.entity.User;
import fr.gael.dhus.service.UserService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

public class UserEntitySet extends V1EntitySet<User>
{
   public static final String ENTITY_NAME = "User";

   private final UserService userService = ApplicationContextProvider
      .getBean (UserService.class);

   // Entity keys
   public static final String USERNAME = "Username";
   public static final String EMAIL = "Email";
   public static final String FIRSTNAME = "FirstName";
   public static final String LASTNAME = "LastName";
   public static final String COUNTRY = "Country";
   public static final String DOMAIN = "Domain";
   public static final String SUBDOMAIN = "SubDomain";
   public static final String USAGE = "Usage";
   public static final String SUBUSAGE = "SubUsage";
   public static final String PHONE = "Phone";
   public static final String ADDRESS = "Address";
   public static final String HASH = "Hash";
   public static final String PASSWORD = "Password";
   public static final String CREATED = "Created";

   public static final FullQualifiedName ASSO_USER_CONNECTION =
      new FullQualifiedName (V1Model.NAMESPACE, "User_Connection");
   public static final String ROLE_USER_CONNECTIONS = "User_Connections";// many
   public static final String ROLE_CONNECTION_USER = "Connection_User";// 1

   public static final FullQualifiedName ASSO_USER_RESTRICTION =
      new FullQualifiedName (V1Model.NAMESPACE, "User_Restriction");
   public static final String ROLE_USER_RESTRICTIONS = "User_Restrictions";// many
   public static final String ROLE_RESTRICTION_USER = "Restriction_User";// 1

   public static final FullQualifiedName ASSO_USER_SYSTEMROLE =
         new FullQualifiedName (V1Model.NAMESPACE, "User_SystemRole");
   public static final String ROLE_USER_SYSTEMROLES = "User_SystemRoles";// many
   public static final String ROLE_SYSTEMROLE_USER = "SystemRole_User";// 1

   @Override
   public String getEntityName ()
   {
      return ENTITY_NAME;
   }

   @Override
   public EntityType getEntityType ()
   {
      // Properties
      List<Property> properties = new ArrayList<Property> ();

      properties.add (new SimpleProperty ()
         .setName (USERNAME)
         .setType (EdmSimpleTypeKind.String)
         .setFacets (new Facets ().setNullable (false))
         .setCustomizableFeedMappings (
            new CustomizableFeedMappings ()
               .setFcTargetPath (EdmTargetPath.SYNDICATION_TITLE)));

      properties.add (new SimpleProperty ().setName (EMAIL).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (FIRSTNAME).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (LASTNAME).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (COUNTRY).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (PHONE).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (ADDRESS).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (DOMAIN).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (SUBDOMAIN).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (USAGE).setType (
         EdmSimpleTypeKind.String));
      properties.add (new SimpleProperty ().setName (SUBUSAGE).setType (
         EdmSimpleTypeKind.String));
      properties.add(new SimpleProperty().setName(HASH)
            .setType(EdmSimpleTypeKind.String));
      properties.add(new SimpleProperty().setName(PASSWORD)
            .setType(EdmSimpleTypeKind.String));
      properties.add(new SimpleProperty().setName(CREATED)
            .setType(EdmSimpleTypeKind.DateTime));

      // Navigation Properties
      List<NavigationProperty> navigationProperties =
         new ArrayList<NavigationProperty> ();

      List<Role> roles = V1Util.getCurrentUser ().getRoles ();
      if (roles.contains (Role.SYSTEM_MANAGER) ||
               roles.contains (Role.STATISTICS))
      {
      navigationProperties.add (new NavigationProperty ()
         .setName (V1Model.CONNECTION.getName ()).setRelationship (ASSO_USER_CONNECTION)
         .setFromRole (ROLE_CONNECTION_USER).setToRole (ROLE_USER_CONNECTIONS));
      }

      // navigate to user restrictions
      navigationProperties.add (new NavigationProperty ()
            .setName (V1Model.RESTRICTION.getName ())
            .setRelationship (ASSO_USER_RESTRICTION)
            .setFromRole (ROLE_RESTRICTION_USER)
            .setToRole (ROLE_USER_RESTRICTIONS));

      // navigate to user roles
      navigationProperties.add (new NavigationProperty ()
            .setName (V1Model.SYSTEM_ROLE.getName ())
            .setRelationship (ASSO_USER_SYSTEMROLE)
            .setFromRole (ROLE_SYSTEMROLE_USER)
            .setToRole (ROLE_USER_SYSTEMROLES));

      // Key
      Key key =
         new Key ().setKeys (Collections.singletonList (new PropertyRef ()
            .setName (USERNAME)));

      return new EntityType ().setName (ENTITY_NAME).setProperties (properties)
         .setKey (key).setNavigationProperties (navigationProperties);
   }

   @Override
   public List<AssociationSet> getAssociationSets ()
   {
      List<AssociationSet> associationSets = new ArrayList<AssociationSet> ();

      List<Role> roles = V1Util.getCurrentUser ().getRoles ();
      if (roles.contains (Role.SYSTEM_MANAGER) ||
               roles.contains (Role.STATISTICS))
      {
      associationSets.add (new AssociationSet ()
         .setName (ASSO_USER_CONNECTION.getName ())
         .setAssociation (ASSO_USER_CONNECTION)
         .setEnd1 (
            new AssociationSetEnd ().setRole (ROLE_USER_CONNECTIONS).setEntitySet (
               V1Model.CONNECTION.getName ()))
         .setEnd2 (
            new AssociationSetEnd ().setRole (ROLE_CONNECTION_USER).setEntitySet (
               getName ())));
      }

      // User restriction association set
      AssociationSet user_restriction = new AssociationSet ();
      user_restriction.setName (ASSO_USER_RESTRICTION.getName ());
      user_restriction.setAssociation (ASSO_USER_RESTRICTION);
      user_restriction.setEnd1 (new AssociationSetEnd ()
            .setRole (ROLE_USER_RESTRICTIONS)
            .setEntitySet (V1Model.RESTRICTION.getName ()));
      user_restriction.setEnd2 (new AssociationSetEnd ()
            .setRole (ROLE_RESTRICTION_USER)
            .setEntitySet (getName ()));
      associationSets.add (user_restriction);

      // User system role association set
      AssociationSet user_role = new AssociationSet ();
      user_role.setName (ASSO_USER_SYSTEMROLE.getName ());
      user_role.setAssociation (ASSO_USER_SYSTEMROLE);
      user_role.setEnd1 (new AssociationSetEnd ()
            .setRole (ROLE_USER_SYSTEMROLES)
            .setEntitySet (V1Model.SYSTEM_ROLE.getName ()));
      user_role.setEnd2 (new AssociationSetEnd ()
            .setRole (ROLE_SYSTEMROLE_USER)
            .setEntitySet (getName ()));
      associationSets.add (user_role);

      return associationSets;
   }

   @Override
   public List<Association> getAssociations ()
   {
      List<Association> associations = new ArrayList<Association> ();
      List<Role> roles = V1Util.getCurrentUser ().getRoles ();
      if (roles.contains (Role.SYSTEM_MANAGER) ||
               roles.contains (Role.STATISTICS))
      {
         associations.add (new Association ()
            .setName (ASSO_USER_CONNECTION.getName ())
            .setEnd1 (
               new AssociationEnd ()
                  .setType (V1Model.CONNECTION.getFullQualifiedName ())
                  .setRole (ROLE_USER_CONNECTIONS)
                  .setMultiplicity (EdmMultiplicity.MANY))
            .setEnd2 (
               new AssociationEnd ().setType (getFullQualifiedName ())
                  .setRole (ROLE_CONNECTION_USER)
                  .setMultiplicity (EdmMultiplicity.ONE)));
      }

      // User restriction association
      Association user_restriction = new Association ();
      user_restriction.setName (ASSO_USER_RESTRICTION.getName ());
      user_restriction.setEnd1 (new AssociationEnd ()
            .setType (V1Model.RESTRICTION.getFullQualifiedName ())
            .setRole (ROLE_USER_RESTRICTIONS)
            .setMultiplicity (EdmMultiplicity.MANY));
      user_restriction.setEnd2 (new AssociationEnd ()
            .setType (getFullQualifiedName ())
            .setRole (ROLE_RESTRICTION_USER)
            .setMultiplicity (EdmMultiplicity.ONE));
      associations.add (user_restriction);

      // User system role association
      Association user_role = new Association ();
      user_role.setName (ASSO_USER_SYSTEMROLE.getName ());
      user_role.setEnd1 (new AssociationEnd ()
            .setType (V1Model.SYSTEM_ROLE.getFullQualifiedName ())
            .setRole (ROLE_USER_SYSTEMROLES)
            .setMultiplicity (EdmMultiplicity.MANY));
      user_role.setEnd2 (new AssociationEnd ()
            .setType (getFullQualifiedName ())
            .setRole (ROLE_SYSTEMROLE_USER)
            .setMultiplicity (EdmMultiplicity.ONE));
      associations.add (user_role);

      return associations;
   }

   @Override
   public int count ()
   {
      return userService.count ("");
   }
}