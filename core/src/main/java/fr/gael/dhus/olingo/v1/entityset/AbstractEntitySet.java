/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
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
import fr.gael.dhus.olingo.v1.ExpectedException;
import fr.gael.dhus.olingo.v1.Navigator;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.info.GetComplexPropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetMediaResourceUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetSimplePropertyUriInfo;

/**
 * Base class to implement new Entity Sets.
 *
 * @param <T> the Entity Type contained in this ES.
 */
public abstract class AbstractEntitySet<T extends AbstractEntity>
{
   /**
    * @return the name of the Entity Type contained in this ES.
    */
   public abstract String getEntityName();

   /**
    * @return the Entity Type contained in this ES.
    */
   public abstract EntityType getEntityType();

   /**
    * Returns the name of this ES, defaults to {@code "`name of the ET` + (e)s"}.
    *
    * @return name of this ES.
    */
   public String getName()
   {
      return generateEntitySetName(getEntityName());
   }

   /**
    * @return the full qualified name of the Entity Type contained in this ES.
    */
   public FullQualifiedName getFullQualifiedName()
   {
      return new FullQualifiedName(Model.NAMESPACE, getEntityName());
   }

   /**
    * @return Olingo ES object for this ES.
    */
   public EntitySet getEntitySet()
   {
      EntitySet res = new EntitySet().setName(getName());
      res.setEntityType(getFullQualifiedName());
      return res;
   }

   /**
    * @return a list of association sets (defaults to empty list).
    */
   public List<AssociationSet> getAssociationSets()
   {
      return Collections.EMPTY_LIST;
   }

   /**
    * @return a list of associations (defaults to empty list).
    */
   public List<Association> getAssociations()
   {
      return Collections.EMPTY_LIST;
   }

   /**
    * Does the navigation and calls {@link AbstractEntity#getProperty(String)}.
    * @param uri_info contains the navigation segments and the name of the property to get.
    * @return the value of requested property (may be null).
    * @throws ODataException
    */
   public Object readPropertyValue(GetSimplePropertyUriInfo uri_info)
         throws ODataException
   {
      KeyPredicate startKP = uri_info.getKeyPredicates().get(0);
      EdmProperty target
            = uri_info.getPropertyPath()
            .get(uri_info.getPropertyPath().size() - 1);

      T t = Navigator.<T>navigate(uri_info.getStartEntitySet(), startKP,
            uri_info.getNavigationSegments(), null);

      // Case of complex property
      String propName = target.getName();
      if (uri_info.getPropertyPath().size() > 1)
      {
         return t.getComplexProperty(
               uri_info.getPropertyPath().get(0).getName()).get(propName);
      }
      return t.getProperty(propName);
   }

   public Map<String, Object> getComplexProperty(
         GetComplexPropertyUriInfo uri_info) throws ODataException
   {
      KeyPredicate startKP = uri_info.getKeyPredicates().get(0);
      EdmProperty target
            = uri_info.getPropertyPath()
            .get(uri_info.getPropertyPath().size() - 1);
      T t = Navigator.<T>navigate(uri_info.getStartEntitySet(), startKP,
            uri_info.getNavigationSegments(), null);
      return t.getComplexProperty(target.getName());
   }

   public ODataResponse getEntityMedia(GetMediaResourceUriInfo uri_info,
         ODataSingleProcessor processor) throws ODataException
   {
      KeyPredicate startKP = uri_info.getKeyPredicates().get(0);
      T t = Navigator.<T>navigate(uri_info.getStartEntitySet(), startKP,
            uri_info.getNavigationSegments(), null);
      ODataResponse resp = t.getEntityMedia(processor);
      if (resp == null)
      {
         throw new ExpectedException("No stream for entity " + getEntityName());
      }
      return resp;
   }

   public int count()
   {
      return 0;
   }

   public boolean isAuthorized(User user)
   {
      return true;
   }

   /**
    * Is an abstract ES? defaults to {@code false}.
    * @return true if this ES is abstract.
    */
   public boolean isAbstract()
   {
      return false;
   }

   /**
    * Return true if this ES is a top level ES (displayed in the service document and accessible
    * at the root of the service).
    *
    * @return true if is a top level ES.
    */
   public boolean isTopLevel()
   {
      return true;
   }

   /**
    * Return {@code true} to enable pagination.
    *
    * @return true if this ES typically has many entries
    */
   public boolean hasManyEntries()
   {
      return true;
   }

   /**
    * Returns the entities as a Map.<p>
    * Only top-level entity sets (accessible at the root of the OData service)
    * should implement this method.
    * The returned map may implement SubMap, to create a filtered map.
    *
    * @return an instance of Map, never null.
    */
   public Map<?, AbstractEntity> getEntities()
   {
      return Collections.EMPTY_MAP;
   }

   /**
    * Returns an Entity identified by the given KeyPredicate.<p>
    * Only top-level entity sets (accessible at the root of the OData service)
    * should implement this method.
    *
    * @param kp KeyPredicate.
    * @return an Entity or null if `kp` does not identify an Entity.
    */
   public AbstractEntity getEntity(KeyPredicate kp)
   {
      return null;
   }

   /**
    * Get the list of navigation links that are acceptable values for the $expand query parameter.
    *
    * @return a non null list that may be empty.
    */
   public List<String> getExpandableNavLinkNames()
   {
      return Collections.emptyList();
   }

   /**
    * Expand the given navigation link.
    *
    * @see fr.gael.dhus.olingo.v1.Expander#expandFeedSingletonKey(String, String, Map, Map, String)
    *
    * @param navlink_name navlink_name name of the navigation link to expand.
    * @param self_url the absolute url to address this entitySet.
    * @param entities that are part of the response and will hold the inlined entities returned by
    *                 this method.
    * @param key that identifies the Entity from the feed being served to the client.
    *
    * @return a non null list that may be empty.
    */
   public List<Map<String, Object>> expand(String navlink_name, String self_url,
         Map<?, AbstractEntity> entities, Map<String, Object> key)
   {
      throw new IllegalStateException("NavLink " + navlink_name + " cannot be expanded");
   }

   public static String generateEntitySetName(String entityName)
   {
      String suffix = "s";
      if (entityName.endsWith("s"))
      {
         suffix = "es";
      }
      return entityName + suffix;
   }
}
