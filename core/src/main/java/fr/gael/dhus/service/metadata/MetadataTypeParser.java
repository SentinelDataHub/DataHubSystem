package fr.gael.dhus.service.metadata;

import java.io.StringReader;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.gael.dhus.messaging.jms.Message;
import fr.gael.dhus.messaging.jms.Message.MessageType;
import fr.gael.dhus.service.metadata.xml.MetadataTypes;

public class MetadataTypeParser
{
   /**
    * A logger for this class.
    */
   private static Log logger = LogFactory.getLog(MetadataTypeParser.class);
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
                  logger.error(new Message(MessageType.SYSTEM,
                        "XML Matadata Type parsing failure at line "
                              + event.getLocator().getLineNumber() + ", column "
                              + event.getLocator().getColumnNumber() + ": "
                              + event.getMessage()));
                  break;
               default:
                  logger.warn("Invalid configuration validation event!");
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
      final List<MetadataType> parsed_metadata_types =
         new Vector<MetadataType>();

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

         if (logger.isDebugEnabled())
         {
            logger.debug("Parsed \"" + metadata_type.getName() + "\" ("
                  + metadata_type.getId() + ") metadata type.");
         }
      }

      // Return parsed metadata types
      return parsed_metadata_types;
   }
}
