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

/**
 * A MetadataType class representing a type definition for DHuS products
 * metadata.
 * <p>
 * The DHuS products metadata usually derive from the evaluation of so called
 * metadataExtractor XQuery scripts attached as properties of the product item
 * classes of the default DRB Cortex Ontology model. This metadata type
 * definition provides the static specification of the metadata in output of
 * these XQuery scripts without evaluation. It may also provide additional
 * properties that qualifies the type for other purpose as the Solr indexing or
 * end user label. TODO: internationalized labels should be specified and
 * implemented as for the RDFS classes e.g.
 * <code>&lt;label xml:lang="en">Platform Name&lt;/label>
 * </code>.
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
 * <p>
 * <p>
 * <b>COMPATIBILITY NOTE:</b> this metadata type uses an improved semantic with
 * respect to previous (and still in use) attributes in output of the
 * <code>metadataExtractor</code>s. As such, the former <code>queryable</code>
 * no longer exists and the <code>id</code> shall be used instead. Knowing
 * whether the field is to be index in the Solr index is now determined by the
 * presence of a SolrField attached to this class. Finally, the
 * <code>type</code> has been renamed <code>contentType</code>.
 * </p>
 */
public class MetadataType
{
   /**
    * A unique identifier of the type. This identifier shall be unique in the
    * context of a item class and all its ancestors. The case of collisions
    * between sub-classes is implementation dependent and may lead to
    * unspecified behavior. The control of collisions is not enforced even it
    * could in any further version of this class or any related one of the
    * containing package. Any metadata type shall have an identifier cf.
    * {@link #MetadataType(String)}.
    */
   private final String id;

   /**
    * The name of the metadata type. A metadata type may have no name i.e.
    * a null name.
    */
   private String name;

   /**
    * The MIME content type of the instances of this type. A metadata type may
    * have no content type i.e. a null member, denoting a default "text/plain"
    * should be considered.
    */
   private String contentType;

   /**
    * The category of this metadata type.
    *
    * TODO: this single member should be updated to allow multiple categories
    * and more, categories with internationalized labels, descriptions, etc.
    * A metadata type may have no category i.e. null member.
    */
   private String category;

   /**
    * An optional Solr field definition. This member defines how this metadata
    * should be handled for the DHuS Solr index. If not present i.e.null, this
    * metadata shouldn't be reported in the Solr index. Otherwise it should be
    * treated for Solr considering this definition with the same semantic as
    * in the Solr documentation and specifications.
    */
   private SolrField solrField;

   /**
    * Builds a metadata type with its immutable identifier.
    *
    * @param id the identifier of the metadata type that shall not be null or
    *        the empty string even if this last state is not enforced.
    * @throws NullPointerException if the identifier is null.
    */
   public MetadataType(final String id) throws NullPointerException
   {
      // Check that input identifier is not a null reference
      if (id == null)
      {
         throw new NullPointerException("Cannot build a metadata type with "
               + "a null identifier.");
      }

      // Assign input identifier
      this.id = id;

   } // End MetadataType(String)

   /**
    * @return the non-null identifier of this metadata type.
    */
   public String getId()
   {
      return this.id;
   }

   /**
    * @return the name, maybe null, of this metadata type.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Assigns a name, maybe null, to this metadata type.
    *
    * @param name the name of this metadata type.
    */
   void setName(String name)
   {
      this.name = name;
   }

   /**
    * @return the MIME content type, maybe null, of this matadata type.
    */
   public String getContentType()
   {
      return this.contentType;
   }

   /**
    * Assigns a MIME content type, maybe null, to this metadata type.
    *
    * @param contentType the MIM content type to be assigned.
    */
   void setContentType(String contentType)
   {
      this.contentType = contentType;
   }

   /**
    * @return the category holding this metadata type, maybe null.
    */
   public String getCategory()
   {
      return this.category;
   }

   /**
    * Assigns the category, maybe null, holding this metadata type.
    *
    * @param category the categrory to be assigned.
    */
   void setCategory(String category)
   {
      this.category = category;
   }

   /**
    * @return the Solr field definition to be considered for this type, or null
    *         if no Solr indexing should be considered
    */
   public SolrField getSolrField()
   {
      return this.solrField;
   }

   /**
    * Assigns the definition to be considered for Solr indexing.
    *
    * @param solrType the Solr field definition to be considered.
    */
   void setSolrField(SolrField solrType)
   {
      this.solrField = solrType;
   }

   @Override
   public String toString()
   {
      // Prepare general definition
      String definition =
         "MetadataType [id=\"" + this.getId() + "\", name=\"" + this.getName()
               + "\", contentType=\"" + this.getContentType()
               + "\", category=\"" + this.getCategory() + "\", ";

      // Add Solr field type, if any
      SolrField solr_field = this.getSolrField();

      if (solr_field != null)
      {
         definition += solr_field;
      }
      else
      {
         definition += "SolrField [none]";
      }

      // Close definition
      definition += "]";

      // Return definition string
      return definition;

   } // End toString()

} // End MetadataTypeclass
