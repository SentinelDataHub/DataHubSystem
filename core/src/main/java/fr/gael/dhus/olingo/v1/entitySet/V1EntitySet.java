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

import fr.gael.dhus.olingo.v1.Navigator;
import fr.gael.dhus.olingo.v1.V1Model;
import fr.gael.dhus.olingo.v1.entity.V1Entity;

public abstract class V1EntitySet<T extends V1Entity>
{
   public abstract String getEntityName ();

   public abstract EntityType getEntityType ();

   public String getName ()
   {
      return getEntityName () + "s";
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
      return new ArrayList<AssociationSet> ();
   }

   public List<Association> getAssociations ()
   {
      return new ArrayList<Association> ();
   }

   public Object readPropertyValue (GetSimplePropertyUriInfo uriInfo)
      throws ODataException
   {
      KeyPredicate startKP = uriInfo.getKeyPredicates ().get (0);
      EdmProperty target =
         uriInfo.getPropertyPath ()
            .get (uriInfo.getPropertyPath ().size () - 1);

      Navigator<T> navigator =
         new Navigator<T> (uriInfo.getStartEntitySet (), startKP,
            uriInfo.getNavigationSegments ());
      T t = navigator.navigate ();

      // Case of complex property
      String propName = target.getName ();
      if (uriInfo.getPropertyPath ().size () > 1)
      {
         return t.getComplexProperty (
            uriInfo.getPropertyPath ().get (0).getName ()).get (propName);
      }
      return t.getProperty (propName);
   }

   public Map<String, Object> getComplexProperty (
      GetComplexPropertyUriInfo uriInfo) throws ODataException
   {
      KeyPredicate startKP = uriInfo.getKeyPredicates ().get (0);
      EdmProperty target =
         uriInfo.getPropertyPath ()
            .get (uriInfo.getPropertyPath ().size () - 1);
      T t =
         new Navigator<T> (uriInfo.getStartEntitySet (), startKP,
            uriInfo.getNavigationSegments ()).navigate ();
      return t.getComplexProperty (target.getName ());
   }

   public ODataResponse getEntityMedia (GetMediaResourceUriInfo uriInfo,
      ODataSingleProcessor processor) throws ODataException
   {
      KeyPredicate startKP = uriInfo.getKeyPredicates ().get (0);
      Navigator<T> nav =
         new Navigator<T> (uriInfo.getStartEntitySet (), startKP,
            uriInfo.getNavigationSegments ());
      T t = nav.navigate ();
      ODataResponse resp = t.getEntityMedia (processor);
      if (resp == null)
      {
         throw new ODataException ("No stream for entity " + getEntityName ());
      }
      return resp;
   }

   public Map<String, Object> getEntityResponse (GetEntityUriInfo uriInfo,
      String lnk) throws ODataException
   {
      KeyPredicate startKP = uriInfo.getKeyPredicates ().get (0);
      Navigator<T> navigator =
         new Navigator<T> (uriInfo.getStartEntitySet (), startKP,
            uriInfo.getNavigationSegments ());
      T t = navigator.navigate ();
      return t.toEntityResponse (lnk);
   }

   public Map<String, ODataCallback> getCallbacks (URI lnk)
   {
      return new HashMap<String, ODataCallback> ();
   }

   public int count ()
   {
      return 0;
   }

   protected static boolean isNavigationFromTo (WriteCallbackContext context,
      String entitySetName, String navigationPropertyName) throws EdmException
   {
      if (entitySetName == null || navigationPropertyName == null)
      {
         return false;
      }
      EdmEntitySet sourceEntitySet = context.getSourceEntitySet ();
      EdmNavigationProperty navigationProperty =
         context.getNavigationProperty ();
      return entitySetName.equals (sourceEntitySet.getName ()) &&
         navigationPropertyName.equals (navigationProperty.getName ());
   }
}
