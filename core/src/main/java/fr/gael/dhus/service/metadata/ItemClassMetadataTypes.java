package fr.gael.dhus.service.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemClassMetadataTypes
{
   private final String uri;
   private final String label;
   private final Map<String, MetadataType> mapTypesById;
   private final Map<String, MetadataType> mapTypesByName;

   /**
    * @param item_class_uri
    * @throws NullPointerException
    */
   public ItemClassMetadataTypes(final String item_class_uri, final String label)
         throws NullPointerException, IllegalArgumentException
   {
      // Check that input identifier is not a null reference
      if (item_class_uri == null)
      {
         throw new NullPointerException("Cannot create an instance of "
               + "ItemClassMetadataTypes without a non null URI");
      }

      // Check that input identifier contains at least one character
      if (item_class_uri.length() < 1)
      {
         throw new IllegalArgumentException("Cannot create an instance of "
               + "ItemClassMetadataTypes with a URI of less than a character");
      }

      // Assign item class URI
      this.uri = item_class_uri;

      // Assign item class label (maybe null)
      this.label = label;

      // Create the maps indexed according to metadata type identifiers and
      // metadata type names
      this.mapTypesById = new ConcurrentHashMap<String, MetadataType>();
      this.mapTypesByName = new ConcurrentHashMap<String, MetadataType>();

   } // End ItemClassMetadataTypes() constructor

   public String getUri()
   {
      return this.uri;
   }
   
   public String getLabel()
   {
      return this.label;
   }

   public void addMetadatType(final MetadataType metadata_type)
         throws NullPointerException
   {
      // Check that the input metadata type is not a null reference
      if (metadata_type == null)
      {
         throw new NullPointerException(
               "Cannot add null metadata type definition.");
      }

      this.mapTypesById.put(metadata_type.getId(), metadata_type);
      this.mapTypesByName.put(metadata_type.getName(), metadata_type);
   }

   public void addAllMetadataTypes(final List<MetadataType> metadata_types)
         throws NullPointerException
   {
      // Check that the input list of metadata types is not a null reference
      if (metadata_types == null)
      {
         throw new NullPointerException(
               "Cannot add all metadata type definitions from a null list.");
      }
      
      // Loop among the input list metadata type definitions
      for (MetadataType metadata_type: metadata_types)
      {
         this.addMetadatType(metadata_type);
      }
   }

   public MetadataType getTypeById(String type_identifier)
   {
      return this.mapTypesById.get(type_identifier);
   }

   public MetadataType getTypeByName(String type_name)
   {
      return this.mapTypesByName.get(type_name);
   }
   
   public Collection<MetadataType>getAllMetadataTypes ()
   {
      return this.mapTypesById.values();
   }

} // End ItemClassMetadataTypes class
