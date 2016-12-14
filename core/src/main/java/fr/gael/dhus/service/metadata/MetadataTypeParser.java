/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2015,2016 GAEL Systems
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
package fr.gael.dhus.service.metadata;

import fr.gael.dhus.messaging.jms.Message;
import fr.gael.dhus.messaging.jms.Message.MessageType;
import fr.gael.dhus.service.metadata.xml.MetadataTypes;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MetadataTypeParser
{
   /**
    * A logger for this class.
    */
   private static final Logger LOGGER = LogManager.getLogger(MetadataTypeParser.class);
   private final Unmarshaller unmarshaller;

   public MetadataTypeParser() throws JAXBException
   {
      final JAXBContext jaxb_context =
         JAXBContext.newInstance("fr.gael.dhus.service.metadata.xml");

      this.unmarshaller = jaxb_context.createUnmarshaller();

      // Configure the unmarshaller for XML Schema validation
      this.unmarshaller.setEventHandler(new ValidationEventHandler()
      {
         @Override
         public boolean handleEvent(final ValidationEvent event)
         {
            switch (event.getSeverity())
            {
               case ValidationEvent.WARNING:
               case ValidationEvent.ERROR:
               case ValidationEvent.FATAL_ERROR:
                  LOGGER.error(new Message(MessageType.SYSTEM,
                        "XML Matadata Type parsing failure at line "
                              + event.getLocator().getLineNumber() + ", column "
                              + event.getLocator().getColumnNumber() + ": "
                              + event.getMessage()));
                  break;
               default:
                  LOGGER.warn("Invalid configuration validation event!");
                  break;
            }
            return false;
         }
      });
   }

   public List<MetadataType> parse(final String xml_metadata_types_string)
         throws NullPointerException, IllegalStateException, JAXBException
   {
      // Check that input XML string is not a null reference
      if (xml_metadata_types_string == null)
      {
         throw new NullPointerException("Cannot parse a null XML string.");
      }

      // Check configured unmarshaller
      if (this.unmarshaller == null)
      {
         throw new IllegalStateException(
               "Cannot parse types with a null unmarshaller.");
      }

      // Parse input XML string
      final MetadataTypes xml_metadata_types =
         (MetadataTypes) this.unmarshaller.unmarshal(new StringReader(
               xml_metadata_types_string));

      // Build output list of metadata types
      final List<MetadataType> parsed_metadata_types = new LinkedList<>();

      for (final MetadataTypes.MetadataType xml_metadata_type : xml_metadata_types
            .getMetadataType())
      {
         final MetadataType metadata_type =
            new MetadataType(xml_metadata_type.getId());
         metadata_type.setName(xml_metadata_type.getName());
         metadata_type.setContentType(xml_metadata_type.getContentType());
         metadata_type.setCategory(xml_metadata_type.getCategory());

         final MetadataTypes.MetadataType.SolrField xml_solr_field =
            xml_metadata_type.getSolrField();

         if (xml_solr_field != null)
         {
            final SolrField solr_field =
               new SolrField(xml_solr_field.getName(), xml_solr_field.getType());
            solr_field.setIndexed(xml_solr_field.isIndexed());
            solr_field.setStored(xml_solr_field.isStored());
            solr_field.setMultiValued(xml_solr_field.isMultiValued());
            solr_field.setRequired(xml_solr_field.isRequired());

            metadata_type.setSolrField(solr_field);
         }

         parsed_metadata_types.add(metadata_type);

         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Parsed \"" + metadata_type.getName() + "\" ("
                  + metadata_type.getId() + ") metadata type.");
         }
      }

      // Return parsed metadata types
      return parsed_metadata_types;
   }
}
