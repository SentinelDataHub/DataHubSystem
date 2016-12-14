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
package fr.gael.dhus.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import fr.gael.dhus.service.metadata.ItemClassMetadataTypes;
import fr.gael.dhus.service.metadata.MetadataType;
import fr.gael.dhus.service.metadata.MetadataTypeParser;
import fr.gael.dhus.service.metadata.SolrField;
import fr.gael.drbx.cortex.DrbCortexItemClass;
import fr.gael.drbx.cortex.DrbCortexModel;

/**
 * A Service for retrieving DHuS metadata types attached to the Item Classes
 * defined in the DRB Cortex Ontology.
 * <p>
 * At initialization (constructor), the service scans all the item classes
 * defined in the DRB Cortex Ontology model and parse any attached metadata type
 * definitions i.e. <code>metadataTypes</code> property. All metadata type
 * definitions are parsed including those of the super classes. For
 * initialization time performance and low runtime memory footprint, the
 * metadata types definitions are parsed only once and cached cf. documentation
 * of the constructor {@link #MetadataTypeService()}.
 * </p>
 * <p>
 * Once initialized, the Service can be called at any time for retrieving the
 * metadata types defined for a given item class identified by its URI. The
 * metadata type can be retrieved either by its identifier
 * {@link #getMetadataTypeById(String, String)} or by its name
 * {@link #getMetadataTypeByName(String, String)}. The implementation make use
 * of multiple indexes to maximize the retrieval performances either by type
 * identifier or name. Concurrent requests from different thread are allowed.
 * </p>
 * <p>
 * Example of definition in the DRB Cortex Ontology:
 * 
 * <pre>
 *    &lt;rdf:Description rdf:about=" <i> URI of the target item class </i> ">
 *       &lt;dhus:metadataTypes rdf:parseType="Literal">
 *          &lt;metadataType id="platformName"
 *                        name="Satellite name"
 *                        contentType="text/plain"
 *                        category="platform">
 *             &lt;solrField name="platformname"
 *                        type="text_general"/>
 *          &lt;/metadataType>
 * 
 *          [... truncated for brevity ...]
 * 
 *       &lt;/dhus:metadataTypes
 *    &lt;/rdf:Description
 * </pre>
 * 
 * </p>
 */
@Service
public class MetadataTypeService
{
   /**
    * A logger for this class.
    */
   private static final Logger LOGGER = LogManager.getLogger(MetadataTypeService.class);

   /**
    * The DHuS XML Namespace holding the property elements defining the metadata
    * types.
    */
   public final static String DHUS_NAMESPACE = "http://www.gael.fr/dhus#";

   /**
    * The name of the property elements defining the metadata types.
    */
   public final static String METADATA_TYPES_PROPERTY = "metadataTypes";

   /**
    * The name of the root element wrapping the metadata type definitions in the
    * metadata type XML Schema.
    */
   public final static String METADATA_TYPES_ELEMENT_NAME = "metadataTypes";

   /**
    * A Map of {#ItemClassMetadataTypes} instances denoting the DRB Cortex
    * classes and holding the list of associated DHuS metadata types.
    */
   private final Map<String, ItemClassMetadataTypes> mapItemClassByUri;

   /**
    * Default constructor loading metadata types associated to all item classes
    * known by the current DRB Cortex model.
    * <p>
    * The access to the default DRB Cortex model is required during this
    * initialization. Otherwise, the initialization is aborted and the Service
    * will not return any metadata type.
    * </p>
    * <p>
    * The initialization loops among all item class of the DRB Cortex model. The
    * classes with no attached metadata type definitions i.e.
    * <code>metadataTypes</code> property, are discarded to same memory
    * footprint. For the others, the metadata type definitions in XML are parsed
    * and the resulting classes are stored in an instance of the multi-indexed
    * {@link ItemClassMetadataTypes} according to the type identifier and the
    * type name for performance purpose. Also for performance purpose, the
    * metadata type XML definitions are parsed only once and the result are
    * shared between {@link ItemClassMetadataTypes} thanks to a cache based on a
    * UUID hash.
    * </p>
    */
   public MetadataTypeService()
   {
      // Log initialization start
      LOGGER.info("Initializing Metadata Typing Service...");

      // Get a time stamps of the initialization start
      final Date start_time = new Date();

      // Initialize an empty map
      this.mapItemClassByUri =
         new ConcurrentHashMap<String, ItemClassMetadataTypes>();

      // Get current DRB Cortex Model
      DrbCortexModel model = null;

      try
      {
         model = DrbCortexModel.getDefaultModel();
      }
      catch (final IOException exception)
      {
         LOGGER.error("Error while getting current DRB Cortex model "
               + "(aborting Metadata Typing service).", exception);
         LOGGER.info("Aborting MetadataTypeService inititialization "
               + "with no metadata type defined!");
         return;
      }

      // Get an iterator over all DRB Cortex classes
      final ExtendedIterator ont_class_iterator =
         model.getCortexModel().getOntModel().listClasses();

      // Prepare a map of parsed metadata type declarations
      final Map<UUID, List<MetadataType>> cached_metadata_types =
         new ConcurrentHashMap<UUID, List<MetadataType>>();

      // Prepare a parser for XML definitions of metadata types
      MetadataTypeParser metadata_type_parser;

      try
      {
         metadata_type_parser = new MetadataTypeParser();
      }
      catch (final JAXBException exception)
      {
         LOGGER.error("Cannot create a parser for the XML definitions of "
               + "metadata types.", exception);
         LOGGER.info("Aborting MetadataTypeService inititialization "
               + "with no metadata type defined!");
         return;
      }

      // Loop among Ontology classes
      while (ont_class_iterator.hasNext())
      {
         // Extracts current class from the iterator
         final OntClass ont_class = (OntClass) ont_class_iterator.next();

         // Get current class URI
         final String uri = ont_class.getURI();

         // Skip classes without URI - case unforeseen by theory
         if (uri == null)
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("Skipping current Ontology class that has no URI "
                     + "(local name = \"" + ont_class.getLocalName() + "\").");
            }
            continue;
         }

         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Processing Ontology class \"" + uri + "\"");
         }

         // Get corresponding DRB Cortex class
         final DrbCortexItemClass item_class =
            DrbCortexItemClass.getCortexItemClassByName(uri);

         // Check resulting item class
         if (item_class == null)
         {
            LOGGER.error("Cannot derive DRB Cortex item class from URI " + "\""
                  + uri + "\".");
            continue;
         }

         // Prepare a container for the current item class metadata types
         final ItemClassMetadataTypes class_metadata_types =
            new ItemClassMetadataTypes(uri, item_class.getLabel());

         // Get all URIs of matadataTypes property describing the present item
         // class or any of its ancestors
         final Collection<String> xml_metadata_types_list =
            item_class.listPropertyStrings(DHUS_NAMESPACE
                  + METADATA_TYPES_PROPERTY, false);

         // Loop immediately if the current item class has no attached
         // metadataTypes
         if ((xml_metadata_types_list == null)
               || xml_metadata_types_list.isEmpty())
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("Item class \"" + item_class.getLabel() + "\" ("
                     + uri + ") has no attached metadata type definition "
                     + "(skipped).");
            }
            continue;
         }

         // Prepare the list of metadata types to be parsed or retrieved from
         // the cache
         final List<MetadataType> metadata_types = new ArrayList<>();

         // Loop among XML definitions of metadata types
         for (final String xml_metadata_types : xml_metadata_types_list)
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("Found metadataTypes string \""
                     + xml_metadata_types.substring(0, 30) + "...\"");
            }

            // Get a unique identifier of the current xml content
            final UUID uuid =
               UUID.nameUUIDFromBytes(xml_metadata_types.getBytes());

            // Get the already parsed types if cached
            if (cached_metadata_types.containsKey(uuid))
            {
               LOGGER.debug("Metadata Types already parsed (copied from cache)");
               metadata_types.addAll(cached_metadata_types.get(uuid));
            }
            else
            {
               LOGGER.debug("Parsing XML definition of Metadata Types...");
               List<MetadataType> parsed_metadata_types = null;

               final String rooted_xml_metadata_types =
                  "<" + METADATA_TYPES_ELEMENT_NAME + ">" + xml_metadata_types
                        + "</" + METADATA_TYPES_ELEMENT_NAME + ">";

               try
               {
                  parsed_metadata_types =
                     metadata_type_parser.parse(rooted_xml_metadata_types);
               }
               catch (NullPointerException | IllegalStateException
                     | JAXBException exception)
               {
                  LOGGER.error("Cannot parse XML metadata type definition of "
                        + "class \"" + uri + "\" (" + rooted_xml_metadata_types
                        + ") - skipped.", exception);
                  continue;
               }

               if (parsed_metadata_types != null)
               {
                  if (LOGGER.isDebugEnabled())
                  {
                     LOGGER.debug("Parsed " + parsed_metadata_types.size()
                           + " metadata types.");
                  }
                  metadata_types.addAll(parsed_metadata_types);
                  cached_metadata_types.put(uuid, parsed_metadata_types);
               }

            }

         } // Loop among XML definitions of metadata types

         // Index metadata types in the item class representative
         if (!metadata_types.isEmpty())
         {
            class_metadata_types.addAllMetadataTypes(metadata_types);

            LOGGER.info("Item class \"" + class_metadata_types.getLabel()
                  + "\" (" + class_metadata_types.getUri() + ") has "
                  + metadata_types.size() + " metadata types.");

            this.mapItemClassByUri.put(uri, class_metadata_types);
         }

      } // Loop among Ontology classes

      LOGGER.info("Metadata Typing Service initialized in "
            + (new Date().getTime() - start_time.getTime()) + " ms with "
            + this.mapItemClassByUri.size() + " item class(es).");

   } // End MetadataTypeService() constructor

   /**
    * Return the metadata type definition attached to an item class identified
    * by its URI and according to the type identifier.
    * <p>
    * This operation cannot retrieve a metadata type if one of the item class
    * URI or the metadata type identifier is null. If one or both are null, the
    * a null reference is returned. The operation may also return a null
    * reference if the item class did not exist at the initialization time, if
    * no metadata type is attached to the item class or if none of the attached
    * metadata type has the requested identifier.
    * </p>
    * <p>
    * Concurrent access from different threads are allowed for this operation.
    * </p>
    * 
    * @param item_class_uri the URI of the item class to be searched.
    * @param type_identifier the identifier of the metadata type to be
    *           retrieved.
    * @return the metadata type or null if none could be retrieved or if one of
    *         the input parameter is null.
    */
   public MetadataType getMetadataTypeById(final String item_class_uri,
         final String type_identifier)
   {
      // Return nothing if the input item class URI is a null reference
      if (item_class_uri == null)
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Cannot get a metadata type with a null item class"
                  + " URI.");
         }
         return null;
      }

      // Return nothing if the input item class URI is a null reference
      if (type_identifier == null)
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Cannot get a metadata type with a null type"
                  + " identifier.");
         }
         return null;
      }

      // Retrieve the metadata types attached to the item class
      final ItemClassMetadataTypes item_class_index =
         this.mapItemClassByUri.get(item_class_uri);

      // Return immediately if no metadata type is attached to the item class
      if (item_class_index == null)
      {
         return null;
      }

      // Return the requested metadata type, if any
      return item_class_index.getTypeById(type_identifier);

   } // End getTypeById(String, String)

   /**
    * Return the metadata type definition attached to an item class identified
    * by its URI and according to the type name.
    * <p>
    * This operation cannot retrieve a metadata type if one of the item class
    * URI or the metadata type name is null. If one or both are null, the a null
    * reference is returned. The operation may also return a null reference if
    * the item class did not exist at the initialization time, if no metadata
    * type is attached to the item class or if none of the attached metadata
    * type has the requested name.
    * </p>
    * <p>
    * Concurrent access from different threads are allowed for this operation.
    * </p>
    * 
    * @param item_class_uri the URI of the item class to be searched.
    * @param type_name the name of the metadata type to be retrieved.
    * @return the metadata type or null if none could be retrieved or if one of
    *         the input parameter is null.
    */
   public MetadataType getMetadataTypeByName(final String item_class_uri,
         final String type_name)
   {
      // Return nothing if the input item class URI is a null reference
      if (item_class_uri == null)
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Cannot get a metadata type with a null item class"
                  + " URI.");
         }
         return null;
      }

      // Return nothing if the input item class URI is a null reference
      if (type_name == null)
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Cannot get a metadata type with a null type"
                  + " identifier.");
         }
         return null;
      }

      // Retrieve the metadata types attached to the item class
      final ItemClassMetadataTypes item_class_index =
         this.mapItemClassByUri.get(item_class_uri);

      // Return immediately if no metadata type is attached to the item class
      if (item_class_index == null)
      {
         return null;
      }

      // Return the requested metadata type, if any
      return item_class_index.getTypeByName(type_name);

   } // End getTypeById(String, String)

   /**
    * Retrieve the list of Solr fields declared into the metadataField
    * ontology description. If a field is declared twice or more,
    * only the last field is considered from the list.
    * @return a Map of solr fields indexed by their name.
    */
   public Map<String, SolrField> getSolrFields()
   {
      Map<String, SolrField>fields = new HashMap<>();
      for (ItemClassMetadataTypes icmt: this.mapItemClassByUri.values())
      {
         Collection<MetadataType> mts = icmt.getAllMetadataTypes();
         if (mts == null)
         {
            continue;
         }

         for (MetadataType mt: mts)
         {
            SolrField sf = mt.getSolrField();
            if (sf == null)
            {
               continue;
            }

            fields.put(sf.getName(), sf);
         }
      }
      return fields;
   }

} // End MetadataTypeService class
