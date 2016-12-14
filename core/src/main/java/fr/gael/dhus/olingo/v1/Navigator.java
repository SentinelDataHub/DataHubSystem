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

import fr.gael.dhus.olingo.v1.ExpectedException.InvalidKeyException;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;
import fr.gael.dhus.olingo.v1.entityset.AbstractEntitySet;

import java.util.Iterator;
import java.util.List;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;

/**
 * This class is a utility class to navigate through the datastore. This class
 * is used by OData Processors implementations. It uses the deta defined in
 * uriInfo. All KeyPredicates are singles, because every Key is mono-valued in
 * the Schema. Olingo's uriInfo data structure depicts the uri convention from
 * the OData v2 specification. Uri Convention example:
 *
 * <pre>
 *  odata/v1/Category(1)/Products
 *  \______/\___________________/
 *    ROOT      RESOURCE PATH
 *
 * Resource Path:
 *  Category(1)/Category(34)/Product(5)/Price/$value
 *  \_________/\______________________/\___________/
 *  Collection        Navigation          Resource
 * </pre>
 *
 * See the <a href=
 * "http://www.odata.org/documentation/odata-version-2-0/uri-conventions
 * #ResourcePath">Uri conventions</a>.
 */
public final class Navigator
{
   /** Do not allow creation of instances. */
   private Navigator() {}

   /**
    * Reads the navigation segments and recursively digs deeper in the data. Can
    * return a Map<Key, Product|Collection|Node|Attribute> for EntitySets, a
    * Product, Collection, Node, Attribute for Entities, a Int, Long, String,
    * Date, Float, Double, ... for Properties.
    *
    * @param <E> return type.
    *
    * @param collection the first segment (as defined in the OData v2 spec).
    * @param collec_kp the KeyPredicate for the collection param.
    * @param nav_segments A list of NavigationSegment.
    * @param return_type the type of returned object (may be null).
    *
    * @return a non-null instance of the returnType.
    *
    * @throws IllegalArgumentException if one or more arg is null.
    * @throws ODataException if not able to return an instance.
    */
   @SuppressWarnings("unchecked")
   public static <E> E navigate(EdmEntitySet collection, KeyPredicate collec_kp,
         List<NavigationSegment> nav_segments, Class<? extends E> return_type) throws ODataException
   {
      Object result;

      AbstractEntitySet entity_set = Model.getEntitySet(collection.getName());
      if (collec_kp != null) // Start is an Entity
      {
         AbstractEntity entity = entity_set.getEntity(collec_kp);
         if (entity == null)
         {
            throw new InvalidKeyException(collec_kp.getLiteral(), entity_set.getName());
         }

         result = entity;
         Iterator<NavigationSegment> it = nav_segments.iterator();
         while (it.hasNext())
         {
            result = entity.navigate(it.next());
            if (result instanceof AbstractEntity)
            {
               entity = AbstractEntity.class.cast(result);
            }
            else if (it.hasNext())
            {
               throw new ExpectedException("Unexpected nav segment " + it.next().toString());
            }
         }
      }
      else // Start is an EntitySet (no further navigation required)
      {
         result = entity_set.getEntities();
      }

      // Returns the result casted into a returnType object
      if (result == null)
      {
         throw new ExpectedException("Navigation failed (result is null)");
      }

      try
      {
         if (return_type != null)
         {
            return return_type.cast(result);
         }
         return (E) result;
      }
      catch (ClassCastException e)
      {
         throw new ODataException(e);
      }

   }
}
