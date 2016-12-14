/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
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

import fr.gael.dhus.olingo.v1.entity.AbstractEntity;
import fr.gael.dhus.olingo.v1.entityset.AbstractEntitySet;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteEntryContent;
import org.apache.olingo.odata2.api.ep.callback.OnWriteFeedContent;
import org.apache.olingo.odata2.api.ep.callback.WriteCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackResult;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteFeedCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataMessageException;

/**
 * This class handles $expand requests, it deleguates to the Entity or the EntitySet if the
 * navigation link to be expanded is an Entity (*_TO_ONE) or an EntitySet (*_TO_MANY).
 * <p>
 * To enable support for the `$expand` parameter, please override the following methods:
 * <ul>
 * <li>{@link AbstractEntity#getExpandableNavLinkNames()}
 * <li>{@link AbstractEntity#expand(String, String)}
 * <li>{@link AbstractEntitySet#getExpandableNavLinkNames()}
 * <li>{@link AbstractEntitySet#expand(String, String, Map, Map)}
 * </ul>
 */
public class Expander implements OnWriteEntryContent, OnWriteFeedContent
{
   /** The root par of the URI to this OData service. */
   private final URI serviceRoot;

   /** Feed expended with retrieveFeedResult(). */
   private final Map<?, AbstractEntity> feed;

   /** EntitySet of feed to expand. */
   private final AbstractEntitySet entitySet;

   /** Entity expanded with retrieveEntryResult(). */
   private final AbstractEntity entity;

   /**
    * Expand a feed.
    * @param service_root the root par of the URI to this OData service.
    * @param entity_set of feed to expand.
    * @param feed to expand.
    */
   public Expander(URI service_root, AbstractEntitySet entity_set, Map<?, AbstractEntity> feed)
   {
      Objects.requireNonNull(service_root);
      Objects.requireNonNull(entity_set);
      Objects.requireNonNull(feed);
      this.serviceRoot = service_root;
      this.feed = feed;
      this.entity = null;
      this.entitySet = entity_set;
   }

   /**
    * Expand an entity.
    * @param service_root the root par of the URI to this OData service.
    * @param entity to expand.
    */
   public Expander(URI service_root, AbstractEntity entity)
   {
      Objects.requireNonNull(service_root);
      Objects.requireNonNull(entity);
      this.serviceRoot = service_root;
      this.feed = null;
      this.entity = entity;
      this.entitySet = null;
   }

   /**
    * Deleguates to the expand() method of AbstractEntitySet or AbstractEntity.
    * @param context of query to expand.
    * @return data.
    * @throws ODataMessageException Olingo exception occured.
    */
   private List<Map<String, Object>> getData(WriteCallbackContext context)
   {
      try
      {
         String navlink_name = context.getNavigationProperty().getName();
         if (this.entity != null)
         {
            return this.entity.expand(navlink_name, this.serviceRoot.toString());
         }
         else
         {
            Map<String, Object> key = context.extractKeyFromEntryData();
            return this.entitySet.expand(navlink_name, this.serviceRoot.toString(), feed, key);
         }
      }
      catch(ODataMessageException ex)
      {
         // rethrow programmatic exception
         throw new RuntimeException(ex);
      }
   }

   @Override
   public WriteEntryCallbackResult retrieveEntryResult(WriteEntryCallbackContext context)
         throws ODataApplicationException
   {
      EntityProviderWriteProperties inlineProperties = EntityProviderWriteProperties
            .serviceRoot(this.serviceRoot)
            .expandSelectTree(context.getCurrentExpandSelectTreeNode())
            .build();

      WriteEntryCallbackResult result = new WriteEntryCallbackResult();
      result.setInlineProperties(inlineProperties);

      List<Map<String, Object>> data = getData(context);
      if (data.size() > 1)
      {
         throw new IllegalStateException("cannot expand a feed as an entity");
      }
      if (data.size() == 1)
      {
         result.setEntryData(data.get(0));
      }

      return result;
   }

   @Override
   public WriteFeedCallbackResult retrieveFeedResult(WriteFeedCallbackContext context)
         throws ODataApplicationException
   {
      EntityProviderWriteProperties inlineProperties = EntityProviderWriteProperties
            .serviceRoot(this.serviceRoot)
            .expandSelectTree(context.getCurrentExpandSelectTreeNode())
            .selfLink(context.getSelfLink())
            .build();

      WriteFeedCallbackResult result = new WriteFeedCallbackResult();
      result.setInlineProperties(inlineProperties);

      result.setFeedData(getData(context));

      return result;
   }

   /**
    * Helper function to create the data for inlined feed from a Map data source.
    * @param map a map<Key, Entity> expanded.
    * @param self_url the absolute url to address the owning entity.
    * @return the expanded feed data (non null, may be empty).
    */
   public static List<Map<String, Object>> mapToData(Map<?, ? extends AbstractEntity> map, String self_url)
   {
      if (map.isEmpty())
      {
         return Collections.emptyList();
      }
      List<Map<String, Object>> res = new ArrayList<>(map.size());
      for (AbstractEntity feed_entry: map.values())
      {
         res.add(feed_entry.toEntityResponse(self_url));
      }
      return res;
   }

   /**
    * Helper function to create the data for inlined entity from an Entity.
    * @param entity expanded.
    * @param self_url the absolute url to address the owning entity.
    * @return the expanded data (non null, may be empty).
    */
   public static List<Map<String, Object>> entityToData(AbstractEntity entity, String self_url)
   {
      if (entity == null)
      {
         return Collections.emptyList();
      }
      return Collections.singletonList(entity.toEntityResponse(self_url));
   }

   /**
    * Helper function to delegate to the right Entity from a singleton key.
    * @param navlink_name navlink_name name of the navigation link to expand.
    * @param self_url the absolute url to address the owning entity.
    * @param entities that are part of the response and will hold the inlined entities returned by
    *                 this method.
    * @param key that identifies the Entity from the feed being served to the client.
    * @param keykey key to get the single key from the `key` map.
    * @return the expanded data (non null, may be empty).
    */
   public static List<Map<String, Object>> expandFeedSingletonKey(String navlink_name,
         String self_url, Map<?, AbstractEntity> entities, Map<String, Object> key, String keykey)
   {
      if (entities == null || entities.isEmpty())
      {
         return Collections.emptyList();
      }

      Object keypo = key.get(keykey);
      return entities.get(keypo).expand(navlink_name, self_url);
   }
}
