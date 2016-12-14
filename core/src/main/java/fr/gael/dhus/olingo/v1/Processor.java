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
package fr.gael.dhus.olingo.v1;

import fr.gael.dhus.olingo.Security;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidKeyException;
import fr.gael.dhus.olingo.v1.ExpectedException.NotAllowedException;
import fr.gael.dhus.olingo.v1.ExpectedException.NotImplementedException;
import fr.gael.dhus.olingo.v1.entity.Ingest;
import fr.gael.dhus.olingo.v1.entity.Product;
import fr.gael.dhus.olingo.v1.entity.Synchronizer;
import fr.gael.dhus.olingo.v1.entity.User;
import fr.gael.dhus.olingo.v1.entity.UserSynchronizer;
import fr.gael.dhus.olingo.v1.entity.AbstractEntity;
import fr.gael.dhus.olingo.v1.entityset.AbstractEntitySet;
import fr.gael.dhus.olingo.v1.map.SubMap;
import fr.gael.dhus.olingo.v1.map.SubMapBuilder;
import fr.gael.dhus.olingo.v1.operations.AbstractOperation;
import fr.gael.dhus.service.SecurityService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;
import fr.gael.dhus.util.MetalinkBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties.ODataEntityProviderPropertiesBuilder;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataProcessor;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetComplexPropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetLinksUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetFunctionImportUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetMediaResourceUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetServiceDocumentUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetSimplePropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;

/**
 * Processes every resources request. Executes the CRUD commands. Each method is
 * prefixed by 'create', 'read', 'update' or 'delete'. URLs are validated by the
 * UriParser.
 */
public class Processor extends ODataSingleProcessor
{
   /** Extract the OData resource path from an URL. */
   private static final Pattern RESOURCE_PATH_EXTRACTOR = Pattern.compile("odata/v1(/.*)$");
   private static final Logger LOGGER = LogManager.getLogger(Processor.class);
   private static final ConfigurationManager CONFIGURATION_MANAGER =
         ApplicationContextProvider.getBean(ConfigurationManager.class);

   /* This OData service allows the metalink content type for the EntitySet Products. */
   @Override
   public List<String> getCustomContentTypes(Class<? extends ODataProcessor> processor_feature)
         throws ODataException
   {
      return Collections.singletonList(MetalinkBuilder.CONTENT_TYPE);
   }

   @Override
   public ODataResponse readServiceDocument(GetServiceDocumentUriInfo uri_info, String content_type)
         throws ODataException
   {
      Edm edm = new EdmWrapper(getContext().getService().getEntityDataModel());
      return EntityProvider.writeServiceDocument(content_type, edm, ServiceFactory.ROOT_URL);
   }

   /* Writes an EntitySet eg: http://dhus.gael.fr/odata/v1/Collections */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   public ODataResponse readEntitySet(GetEntitySetUriInfo uri_info, String content_type)
         throws ODataException
   {
      // Gets values for `skip` and `top` (pagination).
      int maxrows = CONFIGURATION_MANAGER.getOdataConfiguration().getMaxRows();
      boolean doPagination = false;
      int skip = (uri_info.getSkip() == null) ?       0 : uri_info.getSkip();
      int top  = (uri_info.getTop()  == null) ? maxrows : uri_info.getTop();

      // Gets the `collection` part of the URI.
      EdmEntitySet targetES = uri_info.getTargetEntitySet();
      AbstractEntitySet target = Model.getEntitySet(targetES.getName());
      boolean is_navlink = !uri_info.getNavigationSegments().isEmpty();

      // Validity and security checks.
      if (!target.isAuthorized(Security.getCurrentUser()) || !is_navlink && !target.isTopLevel())
      {
         throw new NotAllowedException();
      }

      // Contained target workaround (non OData2: non standard!)
      if (is_navlink)
      {
         int last_id = getContext().getPathInfo().getODataSegments().size() - 1;
         String navlinkname = getContext().getPathInfo().getODataSegments().get(last_id).getPath();
         if (!navlinkname.equals(targetES.getName()))
         {
            targetES = new ContainedEntitySetDecorator(navlinkname, targetES);
         }
      }

      // Enables pagination.
      if (target.hasManyEntries())
      {
         doPagination = target.hasManyEntries();
      }

      // Builds the response.
      KeyPredicate startKP =
            (uri_info.getKeyPredicates().isEmpty()) ? null : uri_info.getKeyPredicates().get(0);

      Map results = Navigator.<Map>navigate(uri_info.getStartEntitySet(), startKP,
            uri_info.getNavigationSegments(), Map.class);
      //int inlineCount = results.size();
      int inlineCount = -1;
      FilterExpression filter = uri_info.getFilter();
      OrderByExpression orderBy = uri_info.getOrderBy();

      if (uri_info.getInlineCount() != null &&
            uri_info.getInlineCount().equals(InlineCount.ALLPAGES) &&
            results instanceof SubMap && filter != null)
      {
         SubMapBuilder smb = ((SubMap) results).getSubMapBuilder();
         smb.setFilter(filter);
         results = smb.build();
         inlineCount = results.size();
      }

      // Skip, Sort and Filter.
      if (results instanceof SubMap && (filter != null || orderBy != null || skip != 0 || top != 0))
      {
         SubMapBuilder smb = ((SubMap) results).getSubMapBuilder();
         smb.setFilter(filter).setOrderBy(orderBy);
         smb.setSkip(skip);
         smb.setTop(top);
         results = smb.build();
      }

      // Custom format (eg: metalink)
      if (uri_info.getFormat() != null)
      {
         if (uri_info.getFormat().equals(MetalinkBuilder.CONTENT_TYPE))
         {
            ArrayList<AbstractEntity> aslist = new ArrayList<>();
            Iterator<AbstractEntity> it = results.values().iterator();
            while (it.hasNext())
            {
               aslist.add(it.next());
            }
            return MetalinkFormatter.writeFeed(targetES, aslist, makeLink().toString());
         }
      }

      // Feeds the EntitySetResponseBuilder.
      List<Map<String, Object>> building = new ArrayList<>();
      Iterator<AbstractEntity> it = results.values().iterator();
      int i;
      for (i = 0; it.hasNext(); i++)
      {
         AbstractEntity o = it.next();
         building.add(o.toEntityResponse(makeLink().toString()));
         if ((!it.hasNext ()) && (o instanceof Closeable))
         {
            try
            {
               Closeable.class.cast (o).close ();
            }
            catch (IOException e)
            {
               LOGGER.warn ("Cannot close resource: " + o);
            }
         }
      }
      // Iterators may be scrolls on the database.
      if (it instanceof Closeable)
      {
         try
         {
            ((Closeable) it).close();
         }
         catch (IOException e)
         {
            LOGGER.warn("Cannot close iterator:", e);
         }
      }

      ODataEntityProviderPropertiesBuilder builder
            = EntityProviderWriteProperties.serviceRoot(makeLink());

      // Creates the `next` link.
      if (doPagination && i == top && it.hasNext())
      {
         i += skip;
         builder.nextLink(makeNextLink(i));
      }

      // $expand.
      ExpandSelectTreeNode expand_select_tree
            = UriParser.createExpandSelectTree(uri_info.getSelect(), uri_info.getExpand());

      builder.expandSelectTree(expand_select_tree)
             .callbacks(makeCallbacks(
                   target.getExpandableNavLinkNames(),
                   new Expander(makeLink(false), target, results)));

      // inlinecount.
      if (uri_info.getInlineCount() != null &&
          uri_info.getInlineCount().equals(InlineCount.ALLPAGES))
      {
         if (inlineCount == -1) inlineCount=results.size();
         builder.inlineCountType(uri_info.getInlineCount());
         builder.inlineCount(inlineCount);
      }

      return EntityProvider.writeFeed(content_type, targetES, building, builder.build());
   }

   @SuppressWarnings("rawtypes")
   @Override
   public ODataResponse countEntitySet(final GetEntitySetCountUriInfo uri_info,
         final String content_type) throws ODataException
   {
      // Gets the `collection` part of the URI.
      EdmEntitySet targetES = uri_info.getTargetEntitySet();
      AbstractEntitySet entityset = Model.getEntitySet(targetES.getName());

      // Validity and security checks.
      if (!entityset.isAuthorized(Security.getCurrentUser()) ||
          uri_info.getNavigationSegments().isEmpty() && !entityset.isTopLevel())
      {
         throw new NotAllowedException();
      }

      // Builds the response.
      KeyPredicate startKP =
            (uri_info.getKeyPredicates().isEmpty()) ? null : uri_info.getKeyPredicates().get(0);

      Map<?, ?> results = Navigator.<Map>navigate(uri_info.getStartEntitySet(), startKP,
            uri_info.getNavigationSegments(), Map.class);

      FilterExpression filter = uri_info.getFilter();
      // Skip, Sort and Filter.
      if (results instanceof SubMap && (filter != null))
      {
         SubMapBuilder smb = ((SubMap) results).getSubMapBuilder();
         smb.setFilter(filter);
         results = smb.build();
      }

      return ODataResponse.entity(results.size()).build();
   }

   /* Writes an Entity eg: http://dhus.gael.fr/odata/v1/Collections(10) */
   @Override
   public ODataResponse readEntity(GetEntityUriInfo uri_info, String content_type)
         throws ODataException
   {
      EdmEntitySet targetES = uri_info.getTargetEntitySet();
      AbstractEntitySet target = Model.getEntitySet(targetES.getName());

      // Validity and security checks.
      if (!target.isAuthorized(Security.getCurrentUser()))
      {
         throw new NotAllowedException();
      }

      // Contained target workaround (non OData2: non standard!)
      int last_id = getContext().getPathInfo().getODataSegments().size() - 1;
      String navlinkname = getContext().getPathInfo().getODataSegments().get(last_id).getPath();
      // remove the key
      last_id = navlinkname.indexOf('(');
      if (last_id != -1)
      {
         navlinkname = navlinkname.substring(0, last_id);
      }
      if (!navlinkname.equals(targetES.getName()))
      {
         targetES = new ContainedEntitySetDecorator(navlinkname, targetES);
      }

      // Navigate to target Entity
      KeyPredicate startKP = uri_info.getKeyPredicates().get(0);
      AbstractEntity entity = Navigator.<AbstractEntity>navigate(
            uri_info.getStartEntitySet(), startKP, uri_info.getNavigationSegments(), null);
      Map<String, Object> data = entity.toEntityResponse(makeLink().toString());

      // $expand & $select
      ExpandSelectTreeNode expand_select_tree =
            UriParser.createExpandSelectTree(uri_info.getSelect(), uri_info.getExpand());

      EntityProviderWriteProperties prop = EntityProviderWriteProperties
            .serviceRoot(makeLink())
            .expandSelectTree(expand_select_tree)
            .callbacks(makeCallbacks(
                  entity.getExpandableNavLinkNames(),
                  new Expander(makeLink(false), entity)))
            .build();

      return EntityProvider.writeEntry(content_type, targetES, data, prop);
   }

   /* Writes a Stream eg: http://dhus.gael.fr/odata/v1/Products('8')/$value */
   @Override
   public ODataResponse readEntityMedia(GetMediaResourceUriInfo uri_info, String content_type)
         throws ODataException
   {
      String targetName = uri_info.getTargetEntitySet().getName();
      return Model.getEntitySet(targetName).getEntityMedia(uri_info, this);
   }

   /* Writes Links eg: http://dhus.gael.fr/odata/v1/Collections(10)/$links/Products */
   @Override
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public ODataResponse readEntityLinks(GetEntitySetLinksUriInfo uri_info, String content_type)
         throws ODataException
   {
      // Target of the link to create
      EdmEntitySet link_target_es = uri_info.getTargetEntitySet();

      // Gets the entityset containing the navigation link to `link_target_es`
      //EdmEntitySet  target_es = getLinkFromES(new AdaptableUriInfo(uri_info));
      //EdmEntityType target_et = target_es.getEntityType();

      // Gets the `collection` part of the URI.
      KeyPredicate start_kp =
         (uri_info.getKeyPredicates().isEmpty()) ? null : uri_info.getKeyPredicates().get(0);

      boolean do_pagination = false;
      // force pagination on products and when $skip and/or $top are provided
      if (link_target_es.getName().equals(Model.PRODUCT.getName()) ||
          uri_info.getSkip() != null || uri_info.getTop()!= null)
      {
         do_pagination = true;
      }

      Map results = Navigator.<Map>navigate(uri_info.getStartEntitySet(), start_kp,
            uri_info.getNavigationSegments(), Map.class);

      if (!(results instanceof SubMap))
      {
         do_pagination = false;
      }

      int maxrows = CONFIGURATION_MANAGER.getOdataConfiguration().getMaxRows();

      int skip = (uri_info.getSkip() == null) ? 0 : uri_info.getSkip();
      int top = (uri_info.getTop() == null) ? maxrows : uri_info.getTop();
      FilterExpression filter = uri_info.getFilter();
      if (do_pagination && (filter != null || skip != 0 || top != 0))
      {
         SubMapBuilder smb = ((SubMap) results).getSubMapBuilder();
         smb.setFilter(filter);
         smb.setSkip(skip);
         smb.setTop(top);
         results = smb.build();
      }

      // Feeds the EntitySetResponseBuilder.
      List<Map<String, Object>> building = new ArrayList<>();
      Iterator<AbstractEntity> it = results.values().iterator();
      int i;
      for (i = 0; it.hasNext() && (!do_pagination || i<top) ; i++)
      {
         building.add(it.next().toEntityResponse(makeLink().toString()));
      }

      ODataEntityProviderPropertiesBuilder builder
            = EntityProviderWriteProperties.serviceRoot(makeLink());

      // Creates the `next` link.
      if (do_pagination && i == top && it.hasNext())
      {
         i += skip;
         builder.nextLink(makeNextLink(i));
      }

      if (it instanceof Closeable)
      {
         try
         {
            ((Closeable) it).close();
         }
         catch (IOException e)
         {
            LOGGER.warn("Cannot close iterator:", e);
         }
      }

      return EntityProvider.writeLinks(content_type, link_target_es, building, builder.build());
   }

   /* Writes a Property eg: http://dhus.gael.fr/odata/v1/Products('8')/Name/ */
   @Override
   public ODataResponse readEntitySimpleProperty(GetSimplePropertyUriInfo uri_info, String content_type)
         throws ODataException
   {
      Object value = readPropertyValue(uri_info);
      EdmProperty target = uri_info.getPropertyPath().get(uri_info.getPropertyPath().size() - 1);
      return EntityProvider.writeProperty(content_type, target, value);
   }

   /* Writes a complex Property eg:
    * http://dhus.gael.fr/odata/v1/Products('8')/ContentDate/ */
   @Override
   public ODataResponse readEntityComplexProperty(GetComplexPropertyUriInfo uri_info, String content_type)
         throws ODataException
   {
      EdmProperty target = uri_info.getPropertyPath().get(uri_info.getPropertyPath().size() - 1);
      String entityTarget = uri_info.getTargetEntitySet().getName();
      Map<String, Object> values = Model.getEntitySet(entityTarget).getComplexProperty(uri_info);
      return EntityProvider.writeProperty(content_type, target, values);
   }

   /* Writes a Property eg:
    * http://dhus.gael.fr/odata/v1/Products('8')/Name/$value */
   @Override
   public ODataResponse readEntitySimplePropertyValue(GetSimplePropertyUriInfo uri_info, String content_type)
         throws ODataException
   {
      try
      {
         Object value = readPropertyValue(uri_info);
         EdmProperty target = uri_info.getPropertyPath().get(uri_info.getPropertyPath().size() - 1);

         if (target.getName().equals("Metalink")) // Metalink/$value
         {
            return ODataResponse
                  .fromResponse(
                        EntityProvider.writeBinary(MetalinkBuilder.CONTENT_TYPE,
                        value.toString().getBytes("UTF-8")))
                  .header("Content-Disposition",
                        "inline; filename=product" + MetalinkBuilder.FILE_EXTENSION)
                  .build();
         }
         else
         {
            return EntityProvider.writePropertyValue(target, value);
         }
      }
      catch (UnsupportedEncodingException e)
      {
         throw new ExpectedException(e.getMessage());
      }
   }

   @Override
   public ODataResponse createEntity(PostUriInfo uri_info, InputStream content,
         String rq_content_type, String content_type) throws ODataException
   {
      if (uri_info.getNavigationSegments().size() > 0)
      {
         throw new ODataException("No support for linking a new entry");
      }

      Map<String, Object> res = null;
      EdmEntityType target_et = uri_info.getTargetEntitySet().getEntityType();
      fr.gael.dhus.database.object.User current_user = Security.getCurrentUser();

      if (uri_info.getStartEntitySet().getEntityType().hasStream())
      {
         // When creating Media Entity, `content` contains the data, the OData document
         // needs to be sent with an update (PUT) command later.
         // See [MS-ODATA].pdf chapt. 2.2.7.1.3 (p197).
         if (target_et.getName().equals(Model.INGEST.getEntityName()))
         {
            Ingest ingest = new Ingest(content);
            res = ingest.toEntityResponse(makeLink().toString());
         }
         else
         {
            throw new NotImplementedException();
         }
      }
      else
      {
         // Merge semantics is set to FALSE because this is `create` (POST)
         EntityProviderReadProperties properties
               = EntityProviderReadProperties.init().mergeSemantic(false).build();
         ODataEntry entry = EntityProvider.readEntry(rq_content_type,
               uri_info.getStartEntitySet(), content, properties);

         if (target_et.getName().equals(Model.SYNCHRONIZER.getEntityName()))
         {
            if (Model.SYNCHRONIZER.isAuthorized(current_user))
            {
               Synchronizer sync = new Synchronizer(entry);
               res = sync.toEntityResponse(makeLink().toString());
            }
            else
            {
               throw new NotAllowedException();
            }
         }
         else if (target_et.getName().equals(Model.USER_SYNCHRONIZER.getEntityName()))
         {
            if (Model.USER_SYNCHRONIZER.isAuthorized(current_user))
            {
               UserSynchronizer sync = new UserSynchronizer(entry);
               res = sync.toEntityResponse(makeLink().toString());
            }
            else
            {
               throw new NotAllowedException();
            }
         }
         else
         {
            throw new NotImplementedException();
         }
      }

      return EntityProvider.writeEntry(content_type,
               uri_info.getStartEntitySet(), res,
               EntityProviderWriteProperties
               .serviceRoot(getContext().getPathInfo().getServiceRoot())
            .build()
      );
   }

   /* Creates an EntityLink, eg: http://dhus.gael.fr/odata/v1/Collections(10)/$links/Products */
   @Override
   public ODataResponse createEntityLink(PostUriInfo uri_info, InputStream content,
         String request_content_type, String content_type) throws ODataException
   {
      // Target of the link to create
      //EdmEntitySet link_target_es = uri_info.getTargetEntitySet();

      // Gets the entityset containing the navigation link to `link_target_es`
      EdmEntitySet  target_es = getLinkFromES(new AdaptableUriInfo(uri_info));
      EdmEntityType target_et = target_es.getEntityType();

      // Check abilities and permissions
      if (!target_es.getName().equals(Model.USER.getName()))
      {
         throw new ODataException("EntitySet " + target_et.getName() + " cannot create links");
      }

      fr.gael.dhus.database.object.User current_user = Security.getCurrentUser();

      AbstractEntitySet es = Model.getEntitySet(target_es.getName());
      if (!es.isAuthorized(current_user))
      {
         throw new NotAllowedException();
      }

      // Gets the affected entity
      String key = uri_info.getKeyPredicates().get(0).getLiteral();
      User user = new User(key);

      // Reads and parses the link
      String link = EntityProvider.readLink(content_type, target_es, content);
      link = link.trim(); // Olingo does not trim... resulting in a parse exception
      try
      {
         link = (new URI(link)).getPath();
         if (link == null || link.isEmpty())
         {
            throw new ExpectedException("Invalid link, path is empty");
         }
         // Gets the OData resource path
         Matcher matcher = RESOURCE_PATH_EXTRACTOR.matcher(link);
         if (matcher.find())
         {
            link = matcher.group(1);
         }
         else
         {
            throw new ExpectedException("Invalid link, path is malformed");
         }
      }
      catch (URISyntaxException e)
      {
         throw new ExpectedException(e.getMessage());
      }

      // Use Olingo's UriParser
      UriParser urip = RuntimeDelegate.getUriParser(getContext().getService().getEntityDataModel());
      List<PathSegment> path_segments = new ArrayList<>();
      StringTokenizer st = new StringTokenizer(link, "/");
      while (st.hasMoreTokens())
      {
         path_segments.add(UriParser.createPathSegment(st.nextToken(), null));
      }
      @SuppressWarnings("unchecked")
      UriInfo uilink = urip.parse(path_segments, Collections.EMPTY_MAP);

      // Creates link
      user.createLink(uilink);

      // Empty answer with HTTP code 204: no content
      return ODataResponse.newBuilder().build();
   }

   @Override
   public ODataResponse updateEntity(PutMergePatchUriInfo uri_info, InputStream content,
         String rq_content_type, boolean merge, String content_type) throws ODataException
   {
      EntityProviderReadProperties properties =
            EntityProviderReadProperties.init().mergeSemantic(merge).build();
      ODataEntry entry =
            EntityProvider.readEntry(rq_content_type, uri_info.getStartEntitySet(), content, properties);

      fr.gael.dhus.database.object.User current_user = Security.getCurrentUser();

      EdmEntityType target_et = uri_info.getTargetEntitySet().getEntityType();
      try
      {
         String target_entity = target_et.getName();
         if (target_entity.equals(Model.SYNCHRONIZER.getEntityName()))
         {
            if (Model.SYNCHRONIZER.isAuthorized(current_user))
            {
               long key = Long.decode(uri_info.getKeyPredicates().get(0).getLiteral());
               Synchronizer s = new Synchronizer(key);
               s.updateFromEntry(entry);
            }
            else
            {
               throw new NotAllowedException();
            }
         }
         else if (target_entity.equals(Model.USER.getEntityName()))
         {
            String key = uri_info.getKeyPredicates().get(0).getLiteral();
            User u = new User(key);
            if (u.isAuthorize(current_user))
            {
               u.updateFromEntry(entry);
            }
            else
            {
               throw new NotAllowedException();
            }
         }
         else if (target_entity.equals(Model.INGEST.getEntityName()))
         {
            long key = Long.decode(uri_info.getKeyPredicates().get(0).getLiteral());
            Ingest i = Ingest.get(key);
            if (i == null)
            {
               throw new InvalidKeyException(String.valueOf(key), target_entity);
            }
            i.updateFromEntry(entry);
         }
         else if (target_entity.equals(Model.USER_SYNCHRONIZER.getEntityName()))
         {
            if (Model.USER_SYNCHRONIZER.isAuthorized(current_user))
            {
               long key = Long.decode(uri_info.getKeyPredicates().get(0).getLiteral());
               UserSynchronizer s = new UserSynchronizer(key);
               s.updateFromEntry(entry);
            }
            else
            {
               throw new NotAllowedException();
            }
         }
         else
         {
            throw new NotImplementedException();
         }
      }
      catch (NullPointerException e)
      {
         return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
      }

      return ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
   }

   @Override
   public ODataResponse deleteEntity(DeleteUriInfo uri_info, String content_type)
         throws ODataException
   {
      fr.gael.dhus.database.object.User current_user = Security.getCurrentUser();

      EdmEntityType target_et = uri_info.getTargetEntitySet().getEntityType();
      String target_name = target_et.getName();
      if (target_name.equals(Model.SYNCHRONIZER.getEntityName()))
      {
         if (Model.SYNCHRONIZER.isAuthorized(current_user))
         {
            long key = Long.decode(uri_info.getKeyPredicates().get(0).getLiteral());
            Synchronizer.delete(key);
         }
         else
         {
            throw new NotAllowedException();
         }
      }
      else if (target_name.equals(Model.USER_SYNCHRONIZER.getEntityName()))
      {
         if (Model.USER_SYNCHRONIZER.isAuthorized(current_user))
         {
            long key = Long.decode(uri_info.getKeyPredicates().get(0).getLiteral());
            Synchronizer.delete(key); // using the same method to delete
         }
         else
         {
            throw new NotAllowedException();
         }
      }
      else if (target_et.getName().equals(Model.PRODUCT.getEntityName()))
      {
         String uuid = uri_info.getKeyPredicates().get(0).getLiteral();
         Product.delete(uuid);
      }
      else if (target_et.getName().equals(Model.INGEST.getEntityName()))
      {
         long key = Long.decode(uri_info.getKeyPredicates().get(0).getLiteral());
         Ingest.delete(key);
      }
      else
      {
         throw new NotImplementedException();
      }

      return ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
   }

   @Override
   public ODataResponse deleteEntityLink(DeleteUriInfo uri_info, String content_type)
         throws ODataException
   {
      // uriInfo#getNavigationSegments() does not return a shallow copy.
      List<NavigationSegment> lns = new ArrayList<>(uri_info.getNavigationSegments().size());
      lns.addAll(uri_info.getNavigationSegments());

      // Removes the target for navigation purposes.
      if (!lns.isEmpty())
      {
         lns.remove(lns.size()-1);
      }
      AbstractEntity entity = Navigator.<AbstractEntity>navigate(uri_info.getStartEntitySet(),
            uri_info.getKeyPredicates().get(0), lns, AbstractEntity.class);

      // Deletes.
      entity.deleteLink(uri_info);

      return ODataResponse.newBuilder().build();
   }

   @Override
   public ODataResponse executeFunctionImport(GetFunctionImportUriInfo uri_info, String content_type)
         throws ODataException
   {
      EdmFunctionImport function_import = uri_info.getFunctionImport();
      Map<String, EdmLiteral> params = uri_info.getFunctionImportParameters();
      EntityProviderWriteProperties entry_props =
            EntityProviderWriteProperties.serviceRoot(makeLink()).build();

      AbstractOperation op = Model.getServiceOperation(function_import.getName());

      fr.gael.dhus.database.object.User current_user =
            ApplicationContextProvider.getBean(SecurityService.class).getCurrentUser();
      if (!op.canExecute(current_user))
      {
         throw new NotAllowedException();
      }

      Object res = op.execute(params);

      return EntityProvider.writeFunctionImport(content_type, function_import, res, entry_props);
   }

   @Override
   public ODataResponse executeFunctionImportValue(GetFunctionImportUriInfo uri_info, String content_type)
         throws ODataException
   {
      EdmFunctionImport function_import = uri_info.getFunctionImport();
      Map<String, EdmLiteral> params = uri_info.getFunctionImportParameters();

      // FIXME: returned type might not be a simple type ...
      EdmSimpleType type = (EdmSimpleType) function_import.getReturnType().getType();

      AbstractOperation op = Model.getServiceOperation(function_import.getName());

      fr.gael.dhus.database.object.User current_user =
            ApplicationContextProvider.getBean(SecurityService.class).getCurrentUser();
      if (!op.canExecute(current_user))
      {
         throw new NotAllowedException();
      }

      Object res = op.execute(params);

      /* To handle binary results (NYI):
      if (type == EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance()) {
         response = EntityProvider.writeBinary(
            ((BinaryData) data).getMimeType(),    // BinaryData is an meta-object holding the data
            ((BinaryData) data).getData()         // and its mime type
          );
      }//*/
      final String value = type.valueToString(res, EdmLiteralKind.DEFAULT, null);

      return EntityProvider.writeText(value == null ? "" : value);
   }

   /**
    * Extract the 'From' ES of an EntityLink.
    * @param uri_info path to resource, eg: /Collections(10)/$links/Products
    * @returns the EntitySet of the nav segment before the "$link" segment.
    */
   private EdmEntitySet getLinkFromES(UriInfo uri_info)
   {
      EdmEntitySet res;
      /* `uri_info`:
       * StartEntitySet/Foo/bar/baz/$links/TargetEntitySet
       *               \__________/       \______________/
       *                      Navigation Segments          */

      List<NavigationSegment> navsegs = uri_info.getNavigationSegments();

      if (navsegs.size() >= 2) // `navsegs` contains at least the target segment
      {
          res = navsegs.get(navsegs.size()-1).getEntitySet();
      }
      else
      {
         res = uri_info.getStartEntitySet();
      }
      return res;
   }

   /** Returns the value of the given Property. */
   private Object readPropertyValue(GetSimplePropertyUriInfo uri_info) throws ODataException
   {
      String targetESName = uri_info.getTargetEntitySet().getName();
      return Model.getEntitySet(targetESName).readPropertyValue(uri_info);
   }

   /** Makes the `next` link for navigation purposes. */
   private String makeNextLink(int skip) throws ODataException
   {
      try
      {
         String selfLnk = ServiceFactory.ROOT_URL;
         URIBuilder ub = new URIBuilder(selfLnk);
         ub.setParameter("$skip", String.valueOf(skip));
         return ub.toString();
      }
      catch (URISyntaxException ex)
      {
         throw new ODataException("Cannot make next link", ex);
      }
   }

   private URI makeLink() throws ODataException
   {
      return makeLink(true);
   }

   private URI makeLink(boolean remove_last_segment) throws ODataException
   {
      try
      {
         URIBuilder ub = new URIBuilder(ServiceFactory.EXTERNAL_URL);
         StringBuilder sb = new StringBuilder();

         String prefix = ub.getPath();
         String path = getContext().getPathInfo().getRequestUri().getPath();
         if (path == null || path.isEmpty() ||
               prefix != null && !prefix.isEmpty() && !path.startsWith(ub.getPath()))
         {
            sb.append(prefix);

            if (path != null)
            {
               if (prefix.endsWith("/") && path.startsWith("/"))
               {
                  sb.deleteCharAt(sb.length() - 1);
               }
               if (!prefix.endsWith("/") && !path.startsWith("/"))
               {
                  sb.append('/');
               }
            }
         }
         sb.append(path);

         if (remove_last_segment)
         {
            // Removes the last segment.
            int lio = sb.lastIndexOf("/");
            while (lio != -1 && lio == sb.length() - 1)
            {
               sb.deleteCharAt(lio);
               lio = sb.lastIndexOf("/");
            }
            if (lio != -1)
            {
               sb.delete(lio + 1, sb.length());
            }

            // Removes the `$links` segment.
            lio = sb.lastIndexOf("$links/");
            if (lio != -1)
            {
               sb.delete(lio, lio + 7);
            }
         }
         else if (!sb.toString().endsWith("/") && !sb.toString().endsWith("\\"))
         {
            sb.append("/");
         }
         ub.setPath(sb.toString());
         return ub.build();
      }
      catch (NullPointerException | URISyntaxException e)
      {
         throw new ODataException(e);
      }
   }

   private Map<String, ODataCallback> makeCallbacks(List<String> expandables, Expander expander)
   {
      Objects.requireNonNull(expandables);
      Objects.requireNonNull(expander);
      if (expandables.isEmpty())
      {
         return Collections.emptyMap();
      }

      Map<String, ODataCallback> res = new HashMap<>(expandables.size());
      for (String navlink_name: expandables)
      {
         res.put(navlink_name, expander);
      }
      return res;
   }

}
