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
package fr.gael.dhus.olingo.v1.entity;

import java.util.Map;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;

/**
 * Nutshell for entities. An entity must at least contain a few properties and
 * be readable. Methods related to writing data, complex types and media default
 * to the raise of an @{link ODataException}.
 */
public abstract class V1Entity
{
   /**
    * Makes a {@code Map<Property_name, Property_value>}.
    * 
    * @param root_url the absolute url to address this entity.
    * @return Properties and their values for this entity.
    */
   public abstract Map<String, Object> toEntityResponse (String root_url);

   /**
    * Returns the property value for the given property name.
    * 
    * @param prop_name Property name.
    * @return Property value.
    * @throws ODataException if no property has the given name.
    */
   public abstract Object getProperty (String prop_name) throws ODataException;

   /**
    * Updates this entity with the given entry.
    * 
    * @param entry contains the properties/complex types/medias to update.
    * @throws ODataException if this entity is not updatable or the entry data
    *            is not valid.
    */
   public void updateFromEntry (ODataEntry entry) throws ODataException
   {
      throw new ODataException ("Entity not updatable");
   }

   /**
    * Returns a complex type.
    * 
    * @param prop_name Complex type property name.
    * @return Properties and their values for the required complex type.
    * @throws ODataException if no complex types or no complex type with the
    *            given name.
    */
   public Map<String, Object> getComplexProperty (String prop_name)
      throws ODataException
   {
      throw new ODataException ("Entity has no complex type");
   }

   /**
    * Returns the EntityMedia for this entity ({@code /$value}).
    * 
    * @param processor to create the {@link ODataResponse}.
    * @return the response.
    * @throws ODataException if this entity has no media.
    */
   public ODataResponse getEntityMedia (ODataSingleProcessor processor)
      throws ODataException
   {
      throw new ODataException ("Entity has no media");
   }
}
