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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import fr.gael.dhus.database.object.Role;
import fr.gael.dhus.service.SecurityService;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties.ODataEntityProviderPropertiesBuilder;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetComplexPropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetLinksUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetMediaResourceUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetServiceDocumentUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetSimplePropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.core.uri.ExpandSelectTreeCreator;

import org.w3c.dom.Document;

import fr.gael.dhus.olingo.v1.entity.Product;
import fr.gael.dhus.olingo.v1.entity.Synchronizer;
import fr.gael.dhus.olingo.v1.entity.User;
import fr.gael.dhus.olingo.v1.entity.UserSynchronizer;
import fr.gael.dhus.olingo.v1.entity.V1Entity;
import fr.gael.dhus.olingo.v1.map.SubMap;
import fr.gael.dhus.olingo.v1.map.SubMapBuilder;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.MetalinkBuilder;

/**
 * Processes every resources request. Executes the CRUD commands. Each method is
 * prefixed by 'create', 'read', 'update' or 'delete'. URLs are validated by the
 * UriParser.
 */
public class V1Processor extends ODataSingleProcessor
{
   private static Logger logger = LogManager.getLogger ();
   private static ConfigurationManager configurationManager = 
      ApplicationContextProvider.getBean (ConfigurationManager.class);
   
   /**
    * This OData service allows the metalink content type for the EntitySet
    * Products.
    */
   @Override
   public List<String> getCustomContentTypes (
      Class<? extends ODataProcessor> processor_feature) throws ODataException
   {
      return Collections.singletonList (MetalinkBuilder.CONTENT_TYPE);
   }

   @Override
   public ODataResponse readServiceDocument (GetServiceDocumentUriInfo uri_info,
      String content_type) throws ODataException
   {
      Edm edm =
         new V1EdmWrapper (getContext ().getService ().getEntityDataModel ());
      return EntityProvider.writeServiceDocument (content_type, edm,
         getServiceRoot ().toASCIIString ());
   }

   /** Writes an EntitySet eg: http://dhus.gael.fr/odata/v1/Collections */
   @SuppressWarnings ({ "unchecked", "rawtypes" })
   @Override
   public ODataResponse readEntitySet (GetEntitySetUriInfo uri_info,
      String content_type) throws ODataException
   {
      int maxrows = configurationManager.getOdataConfiguration ().
         getMaxRows ();
      
      List<Map<String, Object>> building =
         new ArrayList<Map<String, Object>> ();
      boolean doPagination = false;
      
      
      // Gets values for `skip` and `top` (pagination).
      int skip = (uri_info.getSkip () == null) ? 0 : uri_info.getSkip ();
      int top = (uri_info.getTop () == null) ? maxrows : uri_info.getTop ();

      // Gets the `collection` part of the URI.
      EdmEntitySet targetES = uri_info.getTargetEntitySet ();
      KeyPredicate startKP =
         (uri_info.getKeyPredicates ().size () == 0) ? null : uri_info
            .getKeyPredicates ().get (0);

      Navigator<Map> navigator =
         new Navigator<Map> (uri_info.getStartEntitySet (), startKP,
            uri_info.getNavigationSegments (), Map.class);

      // Creates the EntitySetResponseBuilder.
      if (targetES.getName ().equals (V1Model.PRODUCT.getName ()) ||
         targetES.getName ().equals (V1Model.COLLECTION.getName ()) ||
         targetES.getName ().equals (V1Model.NODE.getName ()) ||
         targetES.getName ().equals (V1Model.USER.getName ()) ||
         targetES.getName ().equals (V1Model.CONNECTION.getName ()))
      {
         doPagination = true;
      }
      else if (!targetES.getName ().equals (V1Model.ATTRIBUTE.getName ()) &&
               !targetES.getName ().equals (V1Model.CLASS.getName ()) &&
               !targetES.getName ().equals (V1Model.SYNCHRONIZER.getName ()) &&
               !targetES.getName ().equals (V1Model.NETWORK.getName ()) &&
               !targetES.getName ().equals (V1Model.NETWORKSTATISTIC.getName ()) &&
               !targetES.getName ().equals (V1Model.RESTRICTION.getName ()) &&
               !targetES.getName ().equals (V1Model.SYSTEM_ROLE.getName ()) &&
               !targetES.getName ().equals (V1Model.USER_SYNCHRONIZER.getName()))
      {
         throw new ODataException ("Target EntitySet not allowed.");
      }

      // Builds the response.
      Map results = navigator.navigate ();
      int inlineCount = results.size ();
      FilterExpression filter = uri_info.getFilter ();
      OrderByExpression orderBy = uri_info.getOrderBy ();

      if (uri_info.getInlineCount () != null &&
         uri_info.getInlineCount ().equals (InlineCount.ALLPAGES) &&
         results instanceof SubMap && filter != null)
      {
         SubMapBuilder smb = ((SubMap) results).getSubMapBuilder ();
         smb.setFilter (filter);
         results = smb.build ();
         inlineCount = results.size ();
      }

      // Skip, Sort and Filter.
      if (results instanceof SubMap &&
         (filter != null || orderBy != null || skip != 0 || top != 0))
      {
         SubMapBuilder smb = ((SubMap) results).getSubMapBuilder ();
         smb.setFilter (filter).setOrderBy (orderBy);
         smb.setSkip (skip);
         smb.setTop (top);
         results = smb.build ();
      }
      try
      {
         if (uri_info.getFormat () != null &&
            uri_info.getFormat ().equals (MetalinkBuilder.CONTENT_TYPE) &&
            targetES.getName ().equals (V1Model.PRODUCT.getName ()))
         {
            List<Product> res = new ArrayList<Product> ();
            // Feeds the EntitySetResponseBuilder.
            Iterator it = results.values ().iterator ();
            while (it.hasNext ())
            {
               Object o = it.next ();
               res.add ((Product) o);
            }
            // Iterators may be scrolls on the database.
            if (it instanceof Closeable)
            {
               try
               {
                  ((Closeable) it).close ();
               }
               catch (IOException e)
               {
                  logger.warn ("Cannot close iterator:", e);
               }
            }

            return ODataResponse
               .fromResponse (
                  EntityProvider.writeBinary (MetalinkBuilder.CONTENT_TYPE,
                     makeMetalinkDocument (res).getBytes ("UTF-8")))
               .header ("Content-Disposition",
                  "inline; filename=products" + MetalinkBuilder.FILE_EXTENSION)
               .build ();
         }
      }
      catch (UnsupportedEncodingException e)
      {
         throw new ODataException (e);
      }

      // Feeds the EntitySetResponseBuilder.
      Iterator<V1Entity> it = results.values ().iterator ();
      int i;
      for (i = 0; it.hasNext (); i++)
      {
         V1Entity o = it.next ();
         building.add (o.toEntityResponse (makeLink ().toString ()));
      }
      // Iterators may be scrolls on the database.
      if (it instanceof Closeable)
      {
         try
         {
            ((Closeable) it).close ();
         }
         catch (IOException e)
         {
            logger.warn ("Cannot close iterator:", e);
         }
      }

      ODataEntityProviderPropertiesBuilder builder =
         EntityProviderWriteProperties.serviceRoot (makeLink ());

      // Creates the `next` link.
      if (doPagination && i == top && it.hasNext ())
      {
         i += skip;
         builder.nextLink (makeNextLink (i));
      }
      String targetName = uri_info.getTargetEntitySet ().getName ();

      ExpandSelectTreeCreator creator =
         new ExpandSelectTreeCreator (uri_info.getSelect (),
            uri_info.getExpand ());
      builder.expandSelectTree (creator.create ());
      builder.callbacks (V1Model.getEntitySet (targetName).getCallbacks (
         makeLink (false)));

      if (uri_info.getInlineCount () != null &&
         uri_info.getInlineCount ().equals (InlineCount.ALLPAGES))
      {
         builder.inlineCountType (uri_info.getInlineCount ());
         builder.inlineCount (inlineCount);
      }

      return EntityProvider.writeFeed (content_type, targetES, building,
         builder.build ());
   }

   @SuppressWarnings ("rawtypes")
   @Override
   public ODataResponse countEntitySet (final GetEntitySetCountUriInfo uri_info,
      final String content_type) throws ODataException
   {
      // Gets the `collection` part of the URI.
      EdmEntitySet targetES = uri_info.getTargetEntitySet ();
      KeyPredicate startKP =
         (uri_info.getKeyPredicates ().size () == 0) ? null : uri_info
            .getKeyPredicates ().get (0);

      Navigator<Map> navigator =
         new Navigator<Map> (uri_info.getStartEntitySet (), startKP,
            uri_info.getNavigationSegments (), Map.class);

      // Creates the EntitySetResponseBuilder.
      if (!targetES.getName ().equals (V1Model.PRODUCT.getName ()) &&
          !targetES.getName ().equals (V1Model.COLLECTION.getName ()) &&
          !targetES.getName ().equals (V1Model.NODE.getName ()) &&
          !targetES.getName ().equals (V1Model.ATTRIBUTE.getName ()) &&
          !targetES.getName ().equals (V1Model.CLASS.getName ()) &&
          !targetES.getName ().equals (V1Model.SYNCHRONIZER.getName ()) &&
          !targetES.getName ().equals (V1Model.USER.getName ()) &&
          !targetES.getName ().equals (V1Model.CONNECTION.getName ()) &&
          !targetES.getName ().equals (V1Model.NETWORK.getName ()) &&
          !targetES.getName ().equals (V1Model.NETWORKSTATISTIC.getName ()) &&
          !targetES.getName ().equals (V1Model.RESTRICTION.getName ()) &&
          !targetES.getName ().equals (V1Model.SYSTEM_ROLE.getName ()) &&
          !targetES.getName ().equals (V1Model.USER_SYNCHRONIZER.getName ()))
      {
         throw new ODataException ("Target EntitySet not allowed.");
      }

      // Builds the response.
      Map<?, ?> results = navigator.navigate ();

      FilterExpression filter = uri_info.getFilter ();
      // Skip, Sort and Filter.
      if (results instanceof SubMap && (filter != null))
      {
         SubMapBuilder smb = ((SubMap) results).getSubMapBuilder ();
         smb.setFilter (filter);
         results = smb.build ();
      }

      return ODataResponse.entity (results.size ()).build ();
   }

   /** Writes an Entity eg: http://dhus.gael.fr/odata/v1/Collections(10) */
   @Override
   public ODataResponse readEntity (GetEntityUriInfo uri_info,
         String content_type) throws ODataException
   {
      ODataResponse rsp = null;
      String targetName = uri_info.getTargetEntitySet ().getName ();
      Map<String, Object> data =
         V1Model.getEntitySet (targetName).getEntityResponse (uri_info,
            makeLink ().toString ());

      ExpandSelectTreeCreator creator =
         new ExpandSelectTreeCreator (uri_info.getSelect (),
            uri_info.getExpand ());

      EntityProviderWriteProperties p =
         EntityProviderWriteProperties
            .serviceRoot (makeLink ())
            .expandSelectTree (creator.create ())
            .callbacks (
               V1Model.getEntitySet (targetName)
                  .getCallbacks (makeLink (false))).build ();
      rsp =
         EntityProvider.writeEntry (content_type,
               uri_info.getTargetEntitySet (), data, p);
      return rsp;
   }

   /** Writes a Stream eg: http://dhus.gael.fr/odata/v1/Products('8')/$value */
   @Override
   public ODataResponse readEntityMedia (GetMediaResourceUriInfo uri_info,
      String content_type) throws ODataException
   {
      String targetName = uri_info.getTargetEntitySet ().getName ();
      return V1Model.getEntitySet (targetName).getEntityMedia (uri_info, this);
   }

   /**
    * Writes Links eg:
    * http://dhus.gael.fr/odata/v1/Collections(10)/$links/Products
    */
   @Override
   @SuppressWarnings ({ "unchecked", "rawtypes" })
   public ODataResponse readEntityLinks (GetEntitySetLinksUriInfo uri_info,
      String content_type) throws ODataException
   {
      ODataResponse rsp = null;
      List<Map<String, Object>> building =
         new ArrayList<Map<String, Object>> ();

      // Gets the `collection` part of the URI.
      EdmEntitySet targetES = uri_info.getTargetEntitySet ();
      KeyPredicate startKP =
         (uri_info.getKeyPredicates ().size () == 0) ? null : uri_info
            .getKeyPredicates ().get (0);

      boolean doPagination = false;
      if (targetES.getName ().equals (V1Model.PRODUCT.getName ()) ||
         targetES.getName ().equals (V1Model.COLLECTION.getName ()))
      {
         doPagination = true;
      }
      else
         if ( !targetES.getName ().equals (V1Model.NODE.getName ()) &&
            !targetES.getName ().equals (V1Model.ATTRIBUTE.getName ())&&
            !targetES.getName ().equals (V1Model.CLASS.getName ()))
         {
            throw new ODataException ("Target EntitySet not allowed.");
         }

      Navigator<Map> navigator =
         new Navigator<Map> (uri_info.getStartEntitySet (), startKP,
            uri_info.getNavigationSegments (), Map.class);
      Map results = navigator.navigate ();

      int maxrows = configurationManager.getOdataConfiguration ().getMaxRows ();
      
      int skip = (uri_info.getSkip () == null) ? 0 : uri_info.getSkip ();
      int top = (uri_info.getTop () == null) ? maxrows : uri_info.getTop ();
      FilterExpression filter = uri_info.getFilter ();
      if (results instanceof SubMap &&
         (filter != null || skip != 0 || top != 0))
      {
         SubMapBuilder smb = ((SubMap) results).getSubMapBuilder ();
         smb.setFilter (filter);
         smb.setSkip (skip);
         smb.setTop (top);
         results = smb.build ();
      }

      // Feeds the EntitySetResponseBuilder.
      Iterator<V1Entity> it = results.values ().iterator ();
      int i;
      for (i = 0; it.hasNext (); i++)
      {
         building.add (it.next ().toEntityResponse (makeLink ().toString ()));
      }

      if (it instanceof Closeable)
      {
         try
         {
            ((Closeable) it).close ();
         }
         catch (IOException e)
         {
            logger.warn ("Cannot close iterator:", e);
         }
      }

      ODataEntityProviderPropertiesBuilder builder =
         EntityProviderWriteProperties.serviceRoot (makeLink ());

      // Creates the `next` link.
      if (doPagination && i == top && it.hasNext ())
      {
         i += skip;
         builder.nextLink (makeNextLink (i));
      }

      rsp =
         EntityProvider.writeLinks (content_type, targetES, building,
            builder.build ());
      return rsp;
   }

   /** Writes a Property eg: http://dhus.gael.fr/odata/v1/Products('8')/Name/ */
   @Override
   public ODataResponse readEntitySimpleProperty (
      GetSimplePropertyUriInfo uri_info, String content_type)
      throws ODataException
   {
      Object value = readPropertyValue (uri_info);
      EdmProperty target =
         uri_info.getPropertyPath ()
            .get (uri_info.getPropertyPath ().size () - 1);
      return EntityProvider.writeProperty (content_type, target, value);
   }

   /**
    * Writes a complex Property eg:
    * http://dhus.gael.fr/odata/v1/Products('8')/ContentDate/
    */
   @Override
   public ODataResponse readEntityComplexProperty (
      GetComplexPropertyUriInfo uri_info, String content_type)
      throws ODataException
   {
      EdmProperty target =
         uri_info.getPropertyPath ()
            .get (uri_info.getPropertyPath ().size () - 1);
      String entityTarget = uri_info.getTargetEntitySet ().getName ();
      Map<String, Object> values =
         V1Model.getEntitySet (entityTarget).getComplexProperty (uri_info);
      return EntityProvider.writeProperty (content_type, target, values);
   }

   /**
    * Writes a Property eg:
    * http://dhus.gael.fr/odata/v1/Products('8')/Name/$value
    */
   @Override
   public ODataResponse readEntitySimplePropertyValue (
      GetSimplePropertyUriInfo uri_info, String content_type)
      throws ODataException
   {
      try
      {
         Object value = readPropertyValue (uri_info);
         EdmProperty target =
            uri_info.getPropertyPath ().get (
               uri_info.getPropertyPath ().size () - 1);

         if (target.getName ().equals ("Metalink")) // Metalink/$value
         {
            return ODataResponse
               .fromResponse (
                  EntityProvider.writeBinary (MetalinkBuilder.CONTENT_TYPE,
                     value.toString ().getBytes ("UTF-8")))
               .header ("Content-Disposition",
                  "inline; filename=product" + MetalinkBuilder.FILE_EXTENSION)
               .build ();
         }
         else
         {
            return EntityProvider.writePropertyValue (target, value);
         }
      }
      catch (UnsupportedEncodingException e)
      {
         throw new ODataException (e);
      }
   }

   @Override
   public ODataResponse createEntity (PostUriInfo uri_info, InputStream content,
         String rq_content_type, String content_type) throws ODataException
   {
      if (uri_info.getNavigationSegments ().size () > 0)
      {
         throw new ODataException ("No support for linking a new entry");
      }

      if (uri_info.getStartEntitySet().getEntityType().hasStream())
      {
         throw new ODataException ("No support for media resources");
      }

      // Merge semantics is set to FALSE because this is `create` (POST)
      EntityProviderReadProperties properties =
            EntityProviderReadProperties.init ().mergeSemantic (false).build ();
      ODataEntry entry = EntityProvider.readEntry(rq_content_type,
            uri_info.getStartEntitySet(), content, properties);

      EdmEntityType target_et = uri_info.getTargetEntitySet ().getEntityType ();

      Map<String, Object> res = null;
      fr.gael.dhus.database.object.User current_user =
            ApplicationContextProvider.getBean (SecurityService.class)
                  .getCurrentUser ();
      if (target_et.getName ().equals (V1Model.SYNCHRONIZER.getEntityName ()))
      {
         if (V1Model.SYNCHRONIZER.isAuthorized (current_user))
         {
            Synchronizer sync = new Synchronizer (entry);
            res = sync.toEntityResponse (makeLink ().toString ());
         }
         else
         {
            throw new ODataException (
                  "You are authorized to create a product synchronizer");
         }
      }
      else if (target_et.getName().equals(V1Model.USER_SYNCHRONIZER.getEntityName()))
      {
         if (V1Model.USER_SYNCHRONIZER.isAuthorized (current_user))
         {
            UserSynchronizer sync = new UserSynchronizer (entry);
            res = sync.toEntityResponse (makeLink ().toString ());
         }
         else
         {
            throw new ODataException (
                  "You are authorized to create a user synchronizer");
         }
      }
      else
      {
         throw new ODataException ("Given EntitySet is not writable");
      }

      return EntityProvider.writeEntry (content_type,
            uri_info.getStartEntitySet (), res,
            EntityProviderWriteProperties
                  .serviceRoot (getContext ().getPathInfo ().getServiceRoot ())
                  .build ()
      );
   }

   @Override
   public ODataResponse updateEntity (PutMergePatchUriInfo uri_info,
         InputStream content, String rq_content_type, boolean merge,
         String content_type) throws ODataException
   {
      EntityProviderReadProperties properties =
            EntityProviderReadProperties.init().mergeSemantic(merge).build();
      ODataEntry entry = EntityProvider.readEntry(rq_content_type,
            uri_info.getStartEntitySet(), content, properties);

      fr.gael.dhus.database.object.User current_user =
            ApplicationContextProvider.getBean (SecurityService.class)
                  .getCurrentUser ();

      EdmEntityType target_et = uri_info.getTargetEntitySet ().getEntityType ();
      try
      {
         String target_entity = target_et.getName ();
         if (target_entity.equals (V1Model.SYNCHRONIZER.getEntityName ()))
         {
            if (V1Model.SYNCHRONIZER.isAuthorized (current_user))
            {
               long key = Long.decode (
                     uri_info.getKeyPredicates ().get (0).getLiteral ());
               Synchronizer s = new Synchronizer (key);
               s.updateFromEntry (entry);
            }
            else
            {
               throw new ODataException (
                     "You are not authorized to update a product synchronizer");
            }
         }
         else if (target_entity.equals (V1Model.USER.getEntityName ()))
         {
            String key = uri_info.getKeyPredicates ().get (0).getLiteral ();
            User u = new User (key);
            if (u.isAuthorize (current_user))
            {
               u.updateFromEntry (entry);
            }
            else
            {
               throw new ODataException (
                     "You are not authorized to update this user");
            }
         }
         else if (target_entity.equals(V1Model.USER_SYNCHRONIZER.getEntityName()))
         {
            if (V1Model.USER_SYNCHRONIZER.isAuthorized (current_user))
            {
               long key = Long.decode (
                     uri_info.getKeyPredicates ().get (0).getLiteral ());
               UserSynchronizer s = new UserSynchronizer (key);
               s.updateFromEntry (entry);
            }
            else
            {
               throw new ODataException (
                     "You are not authorized to update a user synchronizer");
            }
         }
         else
         {
            throw new ODataException ("Given EntitySet is not writable");
         }
      }
      catch (NullPointerException e)
      {
         return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
      }

      return ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
   }

   @Override
   public ODataResponse deleteEntity (DeleteUriInfo uri_info,
         String content_type) throws ODataException
   {
      fr.gael.dhus.database.object.User current_user =
            ApplicationContextProvider.getBean (SecurityService.class)
                  .getCurrentUser ();

      EdmEntityType target_et = uri_info.getTargetEntitySet ().getEntityType ();
      String target_name = target_et.getName ();
      if (target_name.equals (V1Model.SYNCHRONIZER.getEntityName ()))
      {
         if (V1Model.SYNCHRONIZER.isAuthorized (current_user))
         {
            long key = Long.decode (
                  uri_info.getKeyPredicates ().get (0).getLiteral ());
            Synchronizer.delete (key);
         }
         else
            {
               throw new ODataException (
                     "You are not authorized to delete a product synchronizer");
            }
      }
      else if (target_name.equals (V1Model.USER_SYNCHRONIZER.getEntityName ()))
      {
         if (V1Model.USER_SYNCHRONIZER.isAuthorized (current_user))
         {
            long key = Long.decode (
                  uri_info.getKeyPredicates ().get (0).getLiteral ());
            Synchronizer.delete (key); // using the same method to delete
         }
         else
         {
            throw new ODataException (
                     "You are not authorized to delete a user synchronizer");
         }
      }
      else
      {
         throw new ODataException ("Given EntitySet is not writable");
      }

      return ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
   }

   /** Returns the value of the given Property. */
   private Object readPropertyValue (GetSimplePropertyUriInfo uri_info)
      throws ODataException
   {
      String targetESName = uri_info.getTargetEntitySet ().getName ();
      EdmProperty target =
         uri_info.getPropertyPath ()
            .get (uri_info.getPropertyPath ().size () - 1);
      String propName = target.getName ();
      // Particular case of Metalink
      if (targetESName.equals (V1Model.PRODUCT.getName ()) &&
         propName.equals ("Metalink"))
      {
         KeyPredicate startKP = uri_info.getKeyPredicates ().get (0);

         Navigator<Product> navigator =
            new Navigator<Product> (uri_info.getStartEntitySet (), startKP,
               uri_info.getNavigationSegments (), Product.class);
         Product p = navigator.navigate ();

         return makeMetalinkDocument (Collections.singletonList (p));
      }
      return V1Model.getEntitySet (targetESName).readPropertyValue (uri_info);
   }

   /** Makes the metalink XML Document for a given list of products. */
   private String makeMetalinkDocument (Iterable<Product> lp)
      throws ODataException
   {
      try
      {
         MetalinkBuilder mb = new MetalinkBuilder ();

         for (Product p : lp)
         {
            String serviceRoot = makeLink ().toString ();
            String product_entity = "";
            if ( !serviceRoot.contains (V1Model.PRODUCT.getName ()))
               product_entity =
                  V1Model.PRODUCT.getName () + "('" + p.getId () + "')/";

            String url = serviceRoot + product_entity + "$value";
            mb.addFile (p.getName () + ".zip").addUrl (url, null, 0);
         }
         StringWriter sw = new StringWriter ();

         Document doc = mb.build ();
         Transformer transformer =
            TransformerFactory.newInstance ().newTransformer ();
         transformer.setOutputProperty (OutputKeys.INDENT, "yes");
         transformer.setOutputProperty (
            "{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.transform (new DOMSource (doc), new StreamResult (sw));
         return sw.toString ();
      }
      catch (ParserConfigurationException e)
      {
         throw new ODataException (e);
      }
      catch (TransformerException e)
      {
         throw new ODataException (e);
      }
   }

   /** Makes the `next` link for navigation purposes. */
   private String makeNextLink (int skip) throws ODataException
   {
      URI selfLnk = getServiceRoot ();
      URIBuilder ub = new URIBuilder (selfLnk);
      ub.setParameter ("$skip", String.valueOf (skip));
      return ub.toString ();
   }

   private URI makeLink () throws ODataException
   {
      return makeLink (true);
   }

   private URI makeLink (boolean remove_last_segment) throws ODataException
   {
      URI selfLnk = getServiceRoot ();
      StringBuilder sb = new StringBuilder (selfLnk.getPath ());

      if (remove_last_segment)
      {
         // Removes the last segment.
         int lio = sb.lastIndexOf ("/");
         while (lio != -1 && lio == sb.length () - 1)
         {
            sb.deleteCharAt (lio);
            lio = sb.lastIndexOf ("/");
         }
         if (lio != -1) sb.delete (lio + 1, sb.length ());

         // Removes the `$links` segment.
         lio = sb.lastIndexOf ("$links/");
         if (lio != -1) sb.delete (lio, lio + 7);
      }
      else
      {
         if ( !sb.toString ().endsWith ("/") && !sb.toString ().endsWith ("\\"))
         {
            sb.append ("/");
         }
      }
      try
      {
         URI res =
            new URI (selfLnk.getScheme (), selfLnk.getUserInfo (),
               selfLnk.getHost (), selfLnk.getPort (), sb.toString (), null,
               selfLnk.getFragment ());
         return res;
      }
      catch (Exception e)
      {
         throw new ODataException (e);
      }
   }

   /**
    * Originally, service root is extracted from http header with URI
    * serviceRoot = getContext ().getPathInfo ().getServiceRoot (); In case of
    * proxied http service without "Preserve" settings local uri is returned.
    * Dhus allow the user to configure the url from which the system can be
    * visible on the internet.
    * 
    * @return the base URI for service root
    * @throws ODataException if URI is wrong
    */
   private URI getServiceRoot () throws ODataException
   {
      String odata_header_url =
         getContext ().getPathInfo ().getRequestUri ().toString ();
      String path =
         odata_header_url.substring (odata_header_url.indexOf (V1Util
            .getBasePath ()));
      
      String url = ApplicationContextProvider.getBean (
            ConfigurationManager.class)
            .getServerConfiguration ().getExternalUrl ();
      try
      {
         return new URI (url + path);
      }
      catch (URISyntaxException e)
      {
         throw new ODataException ("Cannot compute serive root URI.", e);
      }
   }
}
