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
package fr.gael.dhus.system.init;

import fr.gael.dhus.service.MetadataTypeService;
import fr.gael.dhus.service.metadata.SolrField;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.schema.ManagedIndexSchema;
import org.apache.solr.schema.ManagedIndexSchemaFactory;
import org.apache.solr.schema.SchemaField;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.xml.sax.SAXException;

/**
 * This is a workaround to create a schema.xml at startup,
 * only compatible with Solr v4.10.
 *
 * Until Solr is updated to v5.3+, we won't benefint from
 * <a href="https://issues.apache.org/jira/browse/SOLR-7182">SOLR-7182</a>.
 */
@Component(value = "solrInitializer")
public class SolrInitializer
{
   private static final Logger LOGGER = LogManager.getLogger(SolrInitializer.class);

   private static final String SOLR_CONFIG_NAME = "solrconfig.xml";

   /* Field property flags, not public in Solr. */
   private static final int INDEXED     = 0x00000001;
   private static final int STORED      = 0x00000004;
   private static final int MULTIVALUED = 0x00000200;
   private static final int REQUIRED    = 0x00001000;
   // In Solr and DHuS, all Solr field types are indexed and stored by default.
   private static final int DEFAULT     = INDEXED | STORED;
   private static final int ALLOPTS     = INDEXED | STORED | MULTIVALUED | REQUIRED;

   /** The MetadataTypeService holds solr field informations. */
   @Autowired
   private MetadataTypeService metadataTypeService;

   /**
    * Opens and Parses `schema.xml` and adds every fields returned by
    * {@link MetadataTypeService#getSolrFields()}.
    *
    * @param path_to_coredir path to the core's instanceDir.
    * @param path_to_schema path to the schema.xml to edit.
    *
    * @throws ParserConfigurationException An error occured while parsing schema.xml
    * @throws IOException An error occured while accessing schema.xml
    * @throws SAXException An error occured while parsing schema.xml
    */
   public void createSchema(Path path_to_coredir, String path_to_schema)
         throws ParserConfigurationException, IOException, SAXException
   {
      SolrConfig sc = new SolrConfig(path_to_coredir, SOLR_CONFIG_NAME, null);
      ManagedIndexSchemaFactory misf = new ManagedIndexSchemaFactory();
      NamedList named_list = new NamedList();
      named_list.add("mutable", Boolean.TRUE);
      misf.init(named_list);
      ManagedIndexSchema schema = misf.create(path_to_schema, sc);

      Map<String, SolrField> solrfm = metadataTypeService.getSolrFields();

      List<SchemaField> schemafl = new ArrayList<>(solrfm.size() + 1);

      for (SolrField solrf: solrfm.values())
      {
         Map<String, Object> options = new HashMap<>();
         int metadata_properties = DEFAULT;

         if (solrf.isStored() != null)
         {
            if (solrf.isStored())
            {
               options.put("stored", true);
            }
            else
            {
               options.put("stored", false);
               metadata_properties &= ~STORED;
            }
         }

         if (solrf.isIndexed() != null)
         {
            if (solrf.isIndexed())
            {
               options.put("indexed", true);
            }
            else
            {
               options.put("indexed", false);
               metadata_properties &= ~INDEXED;
            }
         }

         if (solrf.isRequired() != null)
         {
            if (solrf.isRequired())
            {
               options.put("required", true);
               metadata_properties |= REQUIRED;
            }
            else
            {
               options.put("required", false);
            }
         }

         if (solrf.isMultiValued() != null)
         {
            if (solrf.isMultiValued())
            {
               options.put("multiValued", true);
               metadata_properties |= MULTIVALUED;
            }
            else
            {
               options.put("multiValued", false);
            }
         }

         if (LOGGER.isDebugEnabled())
         {
            StringBuilder sb = new StringBuilder("solr field: ").append(solrf.getName());
            sb.append(       "  type=").append(solrf.getType());
            sb.append(     "  stored=").append((metadata_properties & STORED) > 0);
            sb.append(    "  indexed=").append((metadata_properties & INDEXED) > 0);
            sb.append(   "  required=").append((metadata_properties & REQUIRED) > 0);
            sb.append("  multiValued=").append((metadata_properties & MULTIVALUED) > 0);
            LOGGER.debug(sb.toString());
         }

         SchemaField schemaf;
         if ((schemaf = schema.getFieldOrNull(solrf.getName())) != null)
         {
            // If already defined with a different type: fatal error!
            if (!schemaf.getType().getTypeName().equals(solrf.getType()))
            {
               String msg = new StringBuilder()
                     .append("Conflicting solr field '")
                     .append(solrf.getName())
                     .append("' defined twice with different types: ")
                     .append(schemaf.getType().getTypeName())
                     .append(" and ")
                     .append(solrf.getType())
                     .toString();
               throw new RuntimeException(msg);
            }

            int schema_properties = schemaf.getProperties();
            // If already defined with a different properties: fatal error!
            if ((schema_properties & ALLOPTS) != metadata_properties)
            {
               String msg = new StringBuilder()
                     .append("Conflicting solr field '")
                     .append(solrf.getName())
                     .append("' defined twice with different properties")
                     .toString();
               throw new RuntimeException(msg);
            }

            LOGGER.info(String.format("solr field '%s' already in schema", solrf.getName()));
            continue;
         }

         LOGGER.info(String.format("Adding solr field '%s' in schema", solrf.getName()));

         schemaf = schema.newField(solrf.getName(), solrf.getType(), options);

         schemafl.add(schemaf);
      }

      // Adds new fields and saves the schema.xml
      schema.addFields(schemafl);
   }
}
