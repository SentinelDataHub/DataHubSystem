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
package fr.gael.dhus.olingo.v1.entity;

import fr.gael.dhus.database.object.SynchronizerConf;
import fr.gael.dhus.olingo.v1.ExpectedException;
import fr.gael.dhus.olingo.v1.ExpectedException.IncompleteDocException;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidTargetException;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidValueException;
import fr.gael.dhus.olingo.v1.ExpectedException.NoTargetException;
import fr.gael.dhus.olingo.v1.Navigator;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.entityset.SynchronizerEntitySet;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.service.ISynchronizerService;
import fr.gael.dhus.service.exception.InvokeSynchronizerException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.sync.SynchronizerStatus;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.UriParser;

/**
 * Synchronizer OData Entity.
 */
public final class Synchronizer extends AbstractEntity
{
   /** Log. */
   private static final Logger LOGGER = LogManager.getLogger(Synchronizer.class);

   /** Database Object. */
   private final SynchronizerConf syncConf;

   /** Synchronizer Service, to create new {@link SynchronizerConf}. */
   private static final ISynchronizerService SYNCHRONIZER_SERVICE =
         ApplicationContextProvider.getBean (ISynchronizerService.class);

   /** Collection Service, for TargetCollection. */
   private static final CollectionService COLLECTION_SERVICE =
      ApplicationContextProvider.getBean (CollectionService.class);

   /**
    * Creates a new Synchronizer from its ID.
    * 
    * @param sync_id synchronizer's ID.
    * @throws NullPointerException if no synchronizer has the given ID.
    */
   public Synchronizer (long sync_id)
   {
      this (SYNCHRONIZER_SERVICE.getSynchronizerConfById (sync_id));
   }

   /**
    * Creates a new Synchronizer from a database object.
    * 
    * @param sync_conf database object.
    */
   public Synchronizer (SynchronizerConf sync_conf)
   {
      Objects.requireNonNull (sync_conf);
      this.syncConf = sync_conf;
   }

   /**
    * Creates an new Synchronizer from the given ODataEntry.
    * 
    * @param odata_entry created by a POST request on the OData interface.
    * @throws ODataException if the given entry is malformed.
    */
   public Synchronizer (ODataEntry odata_entry) throws ODataException
   {
      Map<String, Object> props = odata_entry.getProperties ();

      String label = (String) props.get(SynchronizerEntitySet.LABEL);
      String schedule = (String) props.get(SynchronizerEntitySet.SCHEDULE);
      String request = (String) props.get(SynchronizerEntitySet.REQUEST);
      String service_url = (String) props.get(SynchronizerEntitySet.SERVICE_URL);

      if (schedule == null || schedule.isEmpty () || service_url == null ||
         service_url.isEmpty ())
      {
         throw new IncompleteDocException();
      }

      if (request != null && !request.equals ("start") &&
         !request.equals ("stop"))
      {
         throw new InvalidValueException(SynchronizerEntitySet.REQUEST, request);
      }

      try
      {
         this.syncConf =
            SYNCHRONIZER_SERVICE.createSynchronizer (label,
               "ODataProductSynchronizer", schedule);
         updateFromEntry (odata_entry);
      }
      catch (ParseException e)
      {
         throw new ExpectedException(e.getMessage());
      }
   }

   /**
    * Returns the TargetCollection, or null if there is none.
    * 
    * @return the TargetCollection.
    */
   public Collection getTargetCollection () throws ODataException
   {
      String target = this.syncConf.getConfig ("target_collection");
      if (target == null)
      {
         return null;
      }

      fr.gael.dhus.database.object.Collection c =
         COLLECTION_SERVICE.getCollection (target);
      if (c == null)
      {
         throw new ODataException (
            "This synchronizer references a deleted collection");
      }

      return new Collection (c);
   }

   /**
    * Deletes a synchronizer having the given id.
    * 
    * @param sync_id ID of the synchronizer to delete.
    */
   public static void delete (long sync_id)
   {
      SYNCHRONIZER_SERVICE.removeSynchronizer (sync_id);
   }

   @Override
   public void updateFromEntry (ODataEntry odata_entry) throws ODataException
   {
      Map<String, Object> props = odata_entry.getProperties ();

      String schedule = (String) props.remove(SynchronizerEntitySet.SCHEDULE);
      String request = (String) props.remove(SynchronizerEntitySet.REQUEST);
      String service_url = (String) props.remove(SynchronizerEntitySet.SERVICE_URL);
      Integer page_size = (Integer) props.remove(SynchronizerEntitySet.PAGE_SIZE);
      Boolean copy_product = (Boolean) props.remove(SynchronizerEntitySet.COPY_PRODUCT);

      // Nullable fields
      boolean has_label = props.containsKey(SynchronizerEntitySet.LABEL);
      boolean has_login = props.containsKey(SynchronizerEntitySet.SERVICE_LOGIN);
      boolean has_password = props.containsKey(SynchronizerEntitySet.SERVICE_PASSWORD);
      boolean has_incoming = props.containsKey(SynchronizerEntitySet.REMOTE_INCOMING);
      boolean has_filter = props.containsKey(SynchronizerEntitySet.FILTER_PARAM);
      boolean has_collec = props.containsKey(SynchronizerEntitySet.SOURCE_COLLECTION);
      boolean has_last_date = props.containsKey(SynchronizerEntitySet.LAST_INGESTION_DATE);
      boolean has_target_col = editsTargetCollection(odata_entry);

      String label = (String) props.remove(SynchronizerEntitySet.LABEL);
      String service_login = (String) props.remove(SynchronizerEntitySet.SERVICE_LOGIN);
      String service_password = (String) props.remove(SynchronizerEntitySet.SERVICE_PASSWORD);
      String remote_incoming = (String) props.remove(SynchronizerEntitySet.REMOTE_INCOMING);
      String filter_param = (String) props.remove(SynchronizerEntitySet.FILTER_PARAM);
      String source_collection = (String) props.remove(SynchronizerEntitySet.SOURCE_COLLECTION);
      GregorianCalendar last_ingestion_date =
            (GregorianCalendar) props.remove(SynchronizerEntitySet.LAST_INGESTION_DATE);

      // Navigation
      Collection target_collection = getTargetCollection(odata_entry);

      for (String pname : props.keySet ())
      {
         LOGGER.debug ("Unknown or ReadOnly property: " + pname);
      }

      if (request != null)
      {
         if (request.equals ("start"))
         {
            this.syncConf.setActive (true);
         }
         else
            if (request.equals ("stop"))
            {
               this.syncConf.setActive (false);
            }
            else
            {
               throw new InvalidValueException(SynchronizerEntitySet.SCHEDULE, request);
            }
      }

      if (schedule != null && !schedule.isEmpty ())
      {
         try
         {
            this.syncConf.setCronExpression (schedule);
         }
         catch (ParseException ex)
         {
            throw new ExpectedException(ex.getMessage());
         }
      }

      if (has_label)
      {
         this.syncConf.setLabel (label);
      }

      if (service_url != null && !service_url.isEmpty ())
      {
         this.syncConf.setConfig ("service_uri", service_url);
      }

      if (has_login)
      {
         updateNullableProperty("service_username", service_login);
      }

      if (has_password)
      {
         updateNullableProperty("service_password", service_password);
      }

      if (page_size != null)
      {
         this.syncConf.setConfig ("page_size", page_size.toString ());
      }

      if (has_incoming)
      {
         updateNullableProperty("remote_incoming_path", remote_incoming);
      }

      if (has_last_date)
      {
         String date = last_ingestion_date != null ?
               String.valueOf(last_ingestion_date.getTime().getTime())
               : null;
         updateNullableProperty("last_created", date);
      }

      if (copy_product != null)
      {
         this.syncConf.setConfig ("copy_product", copy_product.toString ());
      }

      if (has_target_col)
      {
         if (target_collection == null)
         {
            this.syncConf.removeConfig("target_collection");
         }
         else
         {
            this.syncConf.setConfig("target_collection", target_collection.getUUID());
         }
      }

      if (has_filter)
      {
         updateNullableProperty("filter_param", filter_param);
      }

      if (has_collec)
      {
         updateNullableProperty("source_collection", source_collection);
      }

      try
      {
         SYNCHRONIZER_SERVICE.saveSynchronizerConf (this.syncConf);
      }
      catch (InvokeSynchronizerException e)
      {
         throw new ODataException (e);
      }
   }

   @Override
   public Map<String, Object> toEntityResponse (String root_url)
   {
      SynchronizerStatus ss = SYNCHRONIZER_SERVICE.getStatus (this.syncConf);
      Map<String, Object> res = new HashMap<> ();
      res.put(SynchronizerEntitySet.ID, this.syncConf.getId());
      res.put(SynchronizerEntitySet.LABEL, this.syncConf.getLabel());
      res.put(SynchronizerEntitySet.SCHEDULE, this.syncConf.getCronExpression());
      res.put(SynchronizerEntitySet.REQUEST, this.syncConf.getActive() ? "start" : "stop");
      res.put(SynchronizerEntitySet.STATUS, ss.status.toString());
      res.put(SynchronizerEntitySet.STATUS_DATE, ss.since);
      res.put(SynchronizerEntitySet.STATUS_MESSAGE, ss.message);
      res.put(SynchronizerEntitySet.CREATION_DATE, this.syncConf.getCreated());
      res.put(SynchronizerEntitySet.MODIFICATION_DATE, this.syncConf.getModified());
      res.put(SynchronizerEntitySet.SERVICE_URL, this.syncConf.getConfig("service_uri"));
      res.put(SynchronizerEntitySet.SERVICE_LOGIN, this.syncConf.getConfig("service_username"));
      res.put(SynchronizerEntitySet.SERVICE_PASSWORD,
            this.syncConf.getConfig("service_password") != null? "***": null);
      res.put(SynchronizerEntitySet.REMOTE_INCOMING,
            this.syncConf.getConfig("remote_incoming_path"));
      String page_size = this.syncConf.getConfig("page_size");
      res.put(SynchronizerEntitySet.PAGE_SIZE, page_size != null? Integer.decode(page_size): 30);
      String copy_prod = this.syncConf.getConfig("copy_product");
      res.put(SynchronizerEntitySet.COPY_PRODUCT,
            copy_prod != null ? Boolean.valueOf(copy_prod): false);
      res.put(SynchronizerEntitySet.FILTER_PARAM, this.syncConf.getConfig("filter_param"));
      res.put(SynchronizerEntitySet.SOURCE_COLLECTION, this.syncConf.getConfig("source_collection"));

      String last_created = this.syncConf.getConfig ("last_created");
      if (last_created != null)
      {
         res.put(SynchronizerEntitySet.LAST_INGESTION_DATE, new Date (Long.decode (last_created)));
      }
      return res;
   }

   @Override
   public Object getProperty (String prop_name) throws ODataException
   {
      Objects.requireNonNull (prop_name);

      SynchronizerStatus ss = SYNCHRONIZER_SERVICE.getStatus (this.syncConf);

      if (prop_name.equals(SynchronizerEntitySet.ID))
      {
         return this.syncConf.getId ();
      }
      if (prop_name.equals(SynchronizerEntitySet.LABEL))
      {
         return this.syncConf.getLabel ();
      }
      if (prop_name.equals(SynchronizerEntitySet.SCHEDULE))
      {
         return this.syncConf.getCronExpression ();
      }
      if (prop_name.equals(SynchronizerEntitySet.REQUEST))
      {
         return this.syncConf.getActive () ? "start" : "stop";
      }
      if (prop_name.equals(SynchronizerEntitySet.STATUS))
      {
         return ss.status.toString ();
      }
      if (prop_name.equals(SynchronizerEntitySet.STATUS_DATE))
      {
         return ss.since;
      }
      if (prop_name.equals(SynchronizerEntitySet.STATUS_MESSAGE))
      {
         return ss.message;
      }
      if (prop_name.equals(SynchronizerEntitySet.CREATION_DATE))
      {
         return this.syncConf.getCreated ();
      }
      if (prop_name.equals(SynchronizerEntitySet.MODIFICATION_DATE))
      {
         return this.syncConf.getModified ();
      }
      if (prop_name.equals(SynchronizerEntitySet.SERVICE_URL))
      {
         return this.syncConf.getConfig ("service_uri");
      }
      if (prop_name.equals(SynchronizerEntitySet.SERVICE_LOGIN))
      {
         return this.syncConf.getConfig ("service_username");
      }
      if (prop_name.equals(SynchronizerEntitySet.SERVICE_PASSWORD))
      {
         return this.syncConf.getConfig ("service_password") != null ? "***"
            : null;
      }
      if (prop_name.equals(SynchronizerEntitySet.REMOTE_INCOMING))
      {
         return this.syncConf.getConfig ("remote_incoming_path");
      }
      if (prop_name.equals(SynchronizerEntitySet.PAGE_SIZE))
      {
         String val = this.syncConf.getConfig("page_size");
         return val != null ? Integer.decode(val): 30;
      }
      if (prop_name.equals(SynchronizerEntitySet.LAST_INGESTION_DATE))
      {
         return new Date (
            Long.decode (this.syncConf.getConfig ("last_created")));
      }
      if (prop_name.equals(SynchronizerEntitySet.COPY_PRODUCT))
      {
         String val = this.syncConf.getConfig("copy_product");
         return val != null ? Boolean.parseBoolean(val): false;
      }
      if (prop_name.equals(SynchronizerEntitySet.FILTER_PARAM))
      {
         return this.syncConf.getConfig("filter_param");
      }
      if (prop_name.equals(SynchronizerEntitySet.SOURCE_COLLECTION))
      {
         return this.syncConf.getConfig("source_collection");
      }

      throw new ODataException ("Unknown property " + prop_name);
   }

   @Override
   public Object navigate(NavigationSegment ns) throws ODataException
   {
      Object res;

      if (ns.getEntitySet().getName().equals(Model.COLLECTION.getName()))
      {
         res = getTargetCollection();
         if (res == null)
         {
            throw new NoTargetException(SynchronizerEntitySet.TARGET_COLLECTION);
         }
      }
      else
      {
         throw new InvalidTargetException(this.getClass().getSimpleName(), ns.getEntitySet().getName());
      }

      return res;
   }

   private static boolean editsTargetCollection(ODataEntry entry) throws ODataException
   {
      List<String> nll = entry.getMetadata().getAssociationUris(SynchronizerEntitySet.TARGET_COLLECTION);
      return !(nll == null || nll.isEmpty());
   }

   private static Collection getTargetCollection(ODataEntry entry)
      throws ODataException
   {
      String navLinkName = SynchronizerEntitySet.TARGET_COLLECTION;
      List<String> nll = entry.getMetadata ().getAssociationUris (navLinkName);

      if (nll != null && !nll.isEmpty ())
      {
         if (nll.size () > 1)
         {
            throw new ODataException (
               "A synchronizer accepts only one collection");
         }
         String uri = nll.get(0);
         // Nullifying
         if (uri == null || uri.isEmpty())
         {
            return null;
         }

         Edm edm = RuntimeDelegate.createEdm(new Model());
         UriParser urip = RuntimeDelegate.getUriParser (edm);

         List<PathSegment> path_segments = new ArrayList<> ();

         StringTokenizer st = new StringTokenizer (uri, "/");

         while (st.hasMoreTokens ())
         {
            path_segments.add (UriParser.createPathSegment (st.nextToken (),
               null));
         }

         UriInfo uinfo =
            urip
               .parse (path_segments, Collections.<String, String> emptyMap ());

         EdmEntitySet sync_ees = uinfo.getStartEntitySet ();
         KeyPredicate kp = uinfo.getKeyPredicates ().get (0);
         List<NavigationSegment> ns_l = uinfo.getNavigationSegments ();

         Collection c = Navigator.<Collection>navigate(sync_ees, kp, ns_l, Collection.class);

         return c;
      }

      return null;
   }

   /**
    * If `value` is null or empty, remove `key` from the configuration.
    * @param key of config entry to update.
    * @param value to set, if null its entry will be removed from the config.
    */
   private void updateNullableProperty(final String key, final String value)
   {
      if (value == null || value.isEmpty())
      {
         this.syncConf.removeConfig(key);
      }
      else
      {
         this.syncConf.setConfig(key, value);
      }
   }

}
