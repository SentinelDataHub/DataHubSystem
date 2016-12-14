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
package fr.gael.dhus.olingo.v1.entity;

import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.FILENAME;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.ID;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.MD5;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.STATUS;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.STATUS_DATE;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.STATUS_MESSAGE;
import static fr.gael.dhus.olingo.v1.entityset.IngestEntitySet.TARGET_COLLECTIONS;

import fr.gael.dhus.database.object.*;
import fr.gael.dhus.datastore.IncomingManager;
import fr.gael.dhus.olingo.v1.ExpectedException.IncompleteDocException;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidKeyException;
import fr.gael.dhus.olingo.v1.ExpectedException.InvalidTargetException;
import fr.gael.dhus.olingo.v1.Navigator;
import fr.gael.dhus.olingo.v1.Model;
import fr.gael.dhus.olingo.v1.map.FunctionalMap;
import fr.gael.dhus.olingo.v1.visitor.IngestFunctionalVisitor;
import fr.gael.dhus.service.CollectionService;
import fr.gael.dhus.service.ProductService;
import fr.gael.dhus.service.SecurityService;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.bind.DatatypeConverter;

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
 * Ingest entity to upload/ingest new products.
 */
public class Ingest extends AbstractEntity
{
   /** Log. */
   private static final Logger LOGGER = LogManager.getLogger(Ingest.class);

   /** Provides access to current user. */
   private static final SecurityService SECURITY_SERVICE =
         ApplicationContextProvider.getBean(SecurityService.class);

   /** Provides the temp dir. */
   private static final IncomingManager INCOMING_MANAGER =
         ApplicationContextProvider.getBean(IncomingManager.class);

   /** Provides the addProduct() method to add a product in the ingestion pipeline. */
   private static final ProductService PRODUCT_SERVICE =
         ApplicationContextProvider.getBean(ProductService.class);

   /** To get db collections from their IDs. */
   private static final CollectionService COLLECTION_SERVICE =
         ApplicationContextProvider.getBean(CollectionService.class);

   /** To auto-increment `id`, not the size of `UPLOAD` because delete is implemented. */
   private static final AtomicLong CURSOR = new AtomicLong(0L);
   /** Map of uploaded products, key is the Id. */
   private static final Map<Long, Ingest> UPLOADS =
         Collections.synchronizedMap(new HashMap<Long, Ingest>());

   /** Key. */
   private final Long id;
   /** MD5sum of the data received by the constructor. */
   private final String md5;
   /** TargetCollection for the new product. */
   private final Map<String, Collection> targetCollections = new HashMap<>();;
   /** User who uploaded this data. */
   private final fr.gael.dhus.database.object.User uploader;
   /** When this Ingest entered the current status. */
   private final Date statusDate = new Date();

   /** Filename, required to start ingestion of data. */
   private String filename;
   /** Current status. */
   private Status status;
   /** Message bound to the current status. */
   private String statusMessage;
   /** Path to temp file holding the data to be ingested. */
   private Path temp_file;

   /**
    * Creates a new Ingest with data to ingest.
    * Will read the stream, store it in a temp file.
    * @param in data to ingest.
    * @throws ODataException an error occured.
    */
   public Ingest(InputStream in) throws ODataException
   {
      uploader = SECURITY_SERVICE.getCurrentUser();

      // write file to temp file
      try
      {
         temp_file = Files.createTempFile(INCOMING_MANAGER.getTempDir().toPath(), null, ".ingest_data");
         LOGGER.info(String.format("User %s uploading data to %s",
               uploader.getUsername(), temp_file.toString()));
         try (OutputStream os = Files.newOutputStream(temp_file))
         {
            // Computes the MD5 hash of the uploaded file as it is written to disk
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestOutputStream md5_os = new DigestOutputStream(os, md);
            BufferedOutputStream bos = new BufferedOutputStream(md5_os);

            int byt3;
            while ((byt3 = in.read()) != -1)
            {
               bos.write(byt3);
            }
            bos.flush();
            this.md5 = DatatypeConverter.printHexBinary(md5_os.getMessageDigest().digest());
         }
      }
      catch (IOException | NoSuchAlgorithmException e)
      {
         LOGGER.fatal(e);
         throw new ODataException("A system error occured", e);
      }

      id = CURSOR.getAndIncrement();
      if (UPLOADS.put(id, this) != null)
      {
         LOGGER.fatal("Race condition!");
      }

      setStatus(Status.WAITING_FOR_METADATA);
      statusMessage = "Set the Filename property to insert the product in the ingestion pipeline";
   }

   @Override
   public void updateFromEntry(ODataEntry entry) throws ODataException
   {
      Map<String, Object> props = entry.getProperties();

      String fname = (String)props.remove(FILENAME);
      if (this.filename == null && (fname == null || fname.isEmpty()))
      {
         throw new IncompleteDocException("Property filename required");
      }
      this.filename = fname;

      for (Map.Entry<String, Object> unkn: props.entrySet())
      {
         switch (unkn.getKey())
         {
            case ID:
            case STATUS:
            case STATUS_DATE:
            case STATUS_MESSAGE:
               LOGGER.warn("Property " + unkn.getKey() + " is read-only");
               break;
            default:
               LOGGER.warn("Unknown property " + unkn.getKey());
         }
      }

      List<String> target_collections = entry.getMetadata().getAssociationUris(TARGET_COLLECTIONS);
      if (target_collections.size() > 0)
      {
         Edm edm = RuntimeDelegate.createEdm(new Model());
         UriParser urip = RuntimeDelegate.getUriParser(edm);

         for (String target_collection: target_collections)
         {
            List<PathSegment> path_segments = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(target_collection, "/");
            while (st.hasMoreTokens ())
            {
               path_segments.add(UriParser.createPathSegment(st.nextToken(), null));
            }
            UriInfo uinfo = urip.parse(path_segments, Collections.EMPTY_MAP);

            EdmEntitySet sync_ees = uinfo.getStartEntitySet();
            KeyPredicate kp = uinfo.getKeyPredicates().get(0);
            List<NavigationSegment> ns_l = uinfo.getNavigationSegments();

            Collection coll = Navigator.<Collection>navigate(sync_ees, kp, ns_l, Collection.class);
            if (coll == null)
            {
               throw new ODataException("Target collection not found: " + target_collection);
            }

            targetCollections.put(coll.getName(), coll);
         }
      }

      // Ingesting ...
      LOGGER.info(String.format("Ingesting product %s (IngestId=%d md5=%s) uploaded by %s",
            filename, id, md5, uploader.getUsername()));

      List<fr.gael.dhus.database.object.Collection> collections =
            new ArrayList<>(targetCollections.size());
      for (Collection c: targetCollections.values())
      {
         collections.add(COLLECTION_SERVICE.getCollection(c.getUUID()));
      }

      Path pname = temp_file.resolveSibling(filename);
      try
      {
         Files.move(temp_file, pname);
         temp_file = pname;
         URL purl = pname.toUri().toURL();
         fr.gael.dhus.database.object.Product p = PRODUCT_SERVICE.addProduct (
               purl, uploader, purl.toString ());
         PRODUCT_SERVICE.processProduct (p, uploader, collections, null, null);
      }
      catch (IOException ex)
      {
         LOGGER.error("Cannot ingest product", ex);
         status = Status.ERROR;
         statusMessage = ex.getMessage();
         throw new ODataException("Cannot ingest product", ex);
      }
      setStatus(Status.INGESTED);
   }

   @Override
   public Map<String, Object> toEntityResponse(String root_url)
   {
      HashMap<String, Object> res = new HashMap<>();

      res.put(ID, id);
      res.put(STATUS, status.toString());
      res.put(STATUS_MESSAGE, statusMessage);
      res.put(STATUS_DATE, statusDate);
      res.put(MD5, md5);
      res.put(FILENAME, filename);

      return res;
   }

   @Override
   public Object getProperty(String prop_name) throws ODataException
   {
      Object res;
      switch (prop_name)
      {
         case ID:
            res = id; break;
         case STATUS:
            res = status.toString(); break;
         case STATUS_MESSAGE:
            res = statusMessage; break;
         case STATUS_DATE:
            res = statusDate; break;
         case MD5:
            res = md5; break;
         case FILENAME:
            res = filename; break;
         default:
            LOGGER.warn("Requested property " + prop_name + " does not exist");
            res = null;
      }
      return res;
   }

   @Override
   public Object navigate(NavigationSegment ns) throws ODataException
   {
      Object res;

      EdmEntitySet es = ns.getEntitySet();
      if (es.getName().equals(Model.USER.getName()))
      {
         res = new User(uploader); // one to one
      }
      else if (es.getName().equals(Model.COLLECTION.getName()))
      {
         if (ns.getKeyPredicates().isEmpty())
         {
            res = Collections.unmodifiableMap(targetCollections);
         }
         else
         {
            KeyPredicate kp = ns.getKeyPredicates().get(0);
            res = this.targetCollections.get(kp.getLiteral());
            if (res == null)
            {
               throw new InvalidKeyException(kp.getLiteral(), ns.getEntitySet().getName());
            }
         }
      }
      else
      {
         throw new InvalidTargetException(this.getClass().getSimpleName(), ns.getEntitySet().getName());
      }
      return res;
   }

   /**
    * Returns the requested Ingest, or {@code null} if no Ingest has such id.
    * @param id unique identifier (key).
    * @return an instance of Ingest or {@code null}.
    */
   public static Ingest get(long id)
   {
      return UPLOADS.get(id);
   }

   /**
    * Delete an instance of Ingest whose id is `id`.
    * @param id of the Ingest instance to delete.
    * @throws ODataException no Ingest was found for the given id.
    */
   public static void delete(long id) throws ODataException
   {
      Ingest ingest;
      if ((ingest = UPLOADS.remove(id)) == null)
      {
         throw new InvalidKeyException(String.valueOf(id), Ingest.class.getSimpleName());
      }
      else
      {
         try
         {
            Files.delete(ingest.temp_file);
         }
         catch (IOException ex)
         {
            LOGGER.error("Cannot delete ingest temp file " + ingest.temp_file, ex);
         }
      }
   }

   /**
    * Returns an unmodifiable map on Ingests.
    * The returned map is a shallow copy of the working map, thus it should not throw a
    * ConcurrentModificationException when iterating over it.
    * <p>The returned map implements SubMap.
    * @return Ingests.
    */
   public static Map<Long, Ingest> getMappable()
   {
      Map<Long, Ingest> res = new HashMap<>();
      synchronized(UPLOADS)
      {
         res.putAll(UPLOADS);
      }
      return new FunctionalMap<>(res, new IngestFunctionalVisitor());
   }

   /**
    * Navigate to linked entity User (navlink=`uploader`).
    * @return linked entity User.
    */
   public User navigateUploader()
   {
      return new User(this.uploader);
   }

   /**
    * Unique identifier, Key.
    * @return Id.
    */
   public Long getId()
   {
      return id;
   }

   /**
    * Filename, defaults to {@code null}. Must be set before the product is sent to ingestion.
    * @return Filename.
    */
   public String getFilename()
   {
      return filename;
   }

   /**
    * MD5, as specified in the supplied OData entity at creation.
    * @return MD5 Hash as string.
    */
   public String getMd5()
   {
      return md5;
   }

   /**
    * Status tells you if the uploaded data is being ingested or is already ingested.
    * @return Status enum entry.
    */
   public Status getStatus()
   {
      return status;
   }

   /**
    * Since when the current status has been active.
    * @return Status Date.
    */
   public Date getStatusDate()
   {
      return statusDate;
   }

   /**
    * Message associated with the current status, useful when the current status is ERROR.
    * @return Status message.
    */
   public String getStatusMessage()
   {
      return statusMessage;
   }

   /** Statuses. */
   public static enum Status
   {
      /** Product has been upload, waiting for the user to set properties. */
      WAITING_FOR_METADATA,
      /** An error occured. */
      ERROR,
      /** Product has passed through the ingestion process, was it successful? nobody knows. */
      INGESTED
   }

   private void setStatus(Status status)
   {
      this.status = status;
      this.statusDate.setTime(System.currentTimeMillis());
   }
}
