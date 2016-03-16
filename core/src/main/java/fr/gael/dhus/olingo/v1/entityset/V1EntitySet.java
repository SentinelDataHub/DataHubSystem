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

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.olingo.v1.Navigator;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.entity.V1Entity;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.ep.callback.WriteCallbackContext;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.info.GetComplexPropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetMediaResourceUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetSimplePropertyUriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class V1EntitySet<T extends V1Entity>
{
   public abstract String getEntityName ();

   public abstract EntityType getEntityType ();

   public String getName ()
   {
      return generateEntitySetName(getEntityName ());
   }

   public FullQualifiedName getFullQualifiedName ()
   {
      return new FullQualifiedName (V1Model.NAMESPACE, getEntityName ());
   }

   public EntitySet getEntitySet ()
   {
      EntitySet res = new EntitySet ().setName (getName ());
      res.setEntityType (getFullQualifiedName ());
      return res;
   }

   public List<AssociationSet> getAssociationSets ()
   {
      return new ArrayList<> ();
   }

   public List<Association> getAssociations ()
   {
      return new ArrayList<> ();
   }

   public Object readPropertyValue (GetSimplePropertyUriInfo uri_info)
      throws ODataException
   {
      KeyPredicate startKP = uri_info.getKeyPredicates ().get (0);
      EdmProperty target =
         uri_info.getPropertyPath ()
            .get (uri_info.getPropertyPath ().size () - 1);

      Navigator<T> navigator =
         new Navigator<T> (uri_info.getStartEntitySet (), startKP,
            uri_info.getNavigationSegments ());
      T t = navigator.navigate ();

      // Case of complex property
      String propName = target.getName ();
      if (uri_info.getPropertyPath ().size () > 1)
      {
         return t.getComplexProperty (
            uri_info.getPropertyPath ().get (0).getName ()).get (propName);
      }
      return t.getProperty (propName);
   }

   public Map<String, Object> getComplexProperty (
      GetComplexPropertyUriInfo uri_info) throws ODataException
   {
      KeyPredicate startKP = uri_info.getKeyPredicates ().get (0);
      EdmProperty target =
         uri_info.getPropertyPath ()
            .get (uri_info.getPropertyPath ().size () - 1);
      T t =
         new Navigator<T> (uri_info.getStartEntitySet (), startKP,
            uri_info.getNavigationSegments ()).navigate ();
      return t.getComplexProperty (target.getName ());
   }

   public ODataResponse getEntityMedia (GetMediaResourceUriInfo uri_info,
      ODataSingleProcessor processor) throws ODataException
   {
      KeyPredicate startKP = uri_info.getKeyPredicates ().get (0);
      Navigator<T> nav =
         new Navigator<T> (uri_info.getStartEntitySet (), startKP,
            uri_info.getNavigationSegments ());
      T t = nav.navigate ();
      ODataResponse resp = t.getEntityMedia (processor);
      if (resp == null)
      {
         throw new ODataException ("No stream for entity " + getEntityName ());
      }
      return resp;
   }

   public Map<String, Object> getEntityResponse (GetEntityUriInfo uri_info,
      String lnk) throws ODataException
   {
      KeyPredicate startKP = uri_info.getKeyPredicates ().get (0);
      Navigator<T> navigator =
         new Navigator<T> (uri_info.getStartEntitySet (), startKP,
            uri_info.getNavigationSegments ());
      T t = navigator.navigate ();
      return t.toEntityResponse (lnk);
   }

   public Map<String, ODataCallback> getCallbacks (URI lnk)
   {
      return new HashMap<> ();
   }

   public int count ()
   {
      return 0;
   }

   public boolean isAuthorized (User user)
   {
      return true;
   }

   protected static boolean isNavigationFromTo (WriteCallbackContext context,
         String entity_set_name, String navigation_property_name)
         throws EdmException
   {
      if (entity_set_name == null || navigation_property_name == null)
      {
         return false;
      }
      EdmEntitySet sourceEntitySet = context.getSourceEntitySet ();
      EdmNavigationProperty navigationProperty =
         context.getNavigationProperty ();
      return entity_set_name.equals (sourceEntitySet.getName ()) &&
         navigation_property_name.equals (navigationProperty.getName ());
   }

   public static String generateEntitySetName (String entityName)
   {
      String suffix="s";
      if (entityName.endsWith("s")) suffix="es";
      return entityName + suffix;
   }
}
