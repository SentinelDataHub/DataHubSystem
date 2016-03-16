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
package fr.gael.dhus.service.metadata;

/**
 * A class denoting a field definition of Solr hold by a DHuS metadata type
 * definition in a DRB Cortex Ontology model.
 * <p>
 * TODO: this class is a first implementation that support only a subset
 * of the attributes available for the Solr fields. Once the interest of this
 * class confirmed, it should be upgraded with the remaining attributes.
 * </p>
 * <p>The definitions of the members of this class derive from the Solr
 * documentation available at <a href="https://cwiki.apache
 * .org/confluence/display/solr/Defining+Fields">https://cwiki.apache
 * .org/confluence/display/solr/Defining+Fields</a> and reported hereafter:
 * <table>
 *     <tr>
 *         <th>Property</th>
 *         <th>Description</th>
 *         <th>Values/Default</th>
 *     </tr>
 *     <tr>
 *         <td><b>name</b></td>
 *         <td>The name of the field. Field names should consist of
 *         alphanumeric or underscore characters only and not start with a
 *         digit. This is not currently strictly enforced, but other field
 *         names will not have first class support from all components and
 *         back compatibility is not guaranteed. Names with both leading and
 *         trailing underscores (e.g. _version_) are reserved. Every field
 *         must have a name</td>
 *         <td>String. No default value</td>
 *     </tr>
 *     <tr>
 *         <td><b>type</b></td>
 *         <td>The name of the field type for this field. This will be found
 *         in the "name" attribute on the field type definition. Every field
 *         must have a type.</td>
 *         <td>String. No default value</td>
 *     </tr>
 *     <tr>
 *         <td><b>index</b></td>
 *         <td>If true, the value of the field can be used in queries to
 *         retrieve matching documents</td>
 *         <td>true or false. Value inherited from field type that defaults
 *         to true if not specified.</td>
 *     </tr>
 *     <tr>
 *         <td><b>stored</b></td>
 *         <td>If true, the actual value of the field can be retrieved by
 *         queries</td>
 *         <td>true or false. Value inherited from field type that defaults
 *         to true if not specified.</td>
 *     </tr>
 *     <tr>
 *         <td><b>multiValued</b></td>
 *         <td>If true, indicates that a single document might contain
 *         multiple values for this field type</td>
 *         <td>true or false. Value inherited from field type that defaults
 *         to false if not specified.</td>
 *     </tr>
 *     <tr>
 *         <td><b>required</b></td>
 *         <td>Instructs Solr to reject any attempts to add a document which
 *         does not have a value for this field. This property defaults to
 *         false.</td>
 *         <td>true or false. Defaults to false.</td>
 *     </tr>
 * </table>
 * </p>
 */
public class SolrField
{
   /**
    * The name of the field. This member is mandatory and has to be provided at
    * construction cf. {@link #SolrField(String, String)}.
    */
   private final String name;

   /**
    * The name of the field type for this field. This will be found in the
    * "name" attribute on the field type definition. Every field must have a
    * type cf. {@link #SolrField(String, String)}.
    */
   private final String type;

   /**
    * Specifies whether the field should be indexed. If true, the value of the
    * field can be used in queries to retrieve matching documents.
    */
   private Boolean indexed = null;

   /**
    * Specifies whether the complete, unparsed value of the field should be
    * stored. If true, the actual value of the field can be retrieved by
    * queries.
    */
   private Boolean stored = null;

   /**
    * Specifies whether a single document might contain multiple values for this
    * field type.
    */
   private Boolean multiValued = null;

   /**
    * Instructs Solr to reject any attempts to add a document which does not
    * have a value for this field. This property defaults to false.
    */
   private Boolean required = null;

   /**
    * Builds an instance of SolrField.
    *
    * @param name the name of the field to be created.
    * @param type the type of the field to be created.
    * @throws NullPointerException if one of the name or type parameter is a
    *            null reference.
    */
   public SolrField(final String name, final String type)
         throws NullPointerException
   {
      // Check that field name is not a null reference
      if (name == null)
      {
         throw new NullPointerException("Cannot create a Solr field with a "
               + "null name.");
      }

      // Check that field type is not a null reference
      if (type == null)
      {
         throw new NullPointerException("Cannot create a Solr field with a "
               + "null type.");
      }

      // Assign field name and type
      this.name = name;
      this.type = type;

   } // End SolrField(String, String)

   /**
    * @return the name of the field (never null).
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * @return the type of the field (never null).
    */
   public String getType()
   {
      return this.type;
   }

   /**
    * @return a true Boolean whether this field is indexed, false otherwise and
    *         maybe null denoting that the default true value shall be
    *         considered.
    */
   public Boolean isIndexed()
   {
      return this.indexed;
   }

   /**
    * Specifies whether this field should be indexed or not. The indexed
    * parameter may be null denoting that the default "true" value shall be
    * considered.
    * 
    * @param indexed the Boolean value to be set or null for default value.
    */
   void setIndexed(Boolean indexed)
   {
      this.indexed = indexed;
   }

   /**
    * @return a true Boolean whether this field is stored, false otherwise and
    *         maybe null denoting that the default true value shall be
    *         considered.
    */
   public Boolean isStored()
   {
      return this.stored;
   }

   /**
    * Specifies whether this field should be stored or not. The stored parameter
    * may be null denoting that the default "true" value shall be considered.
    * 
    * @param indexed the Boolean value to be set or null for default value.
    */
   void setStored(Boolean stored)
   {
      this.stored = stored;
   }

   /**
    * @return a true Boolean whether this field is multi-valued, false otherwise
    *         and maybe null denoting that the default false value shall be
    *         considered.
    */
   public Boolean isMultiValued()
   {
      return this.multiValued;
   }

   /**
    * Specifies whether this field multi-valued or not. The multiValued
    * parameter may be null denoting that the default "false" value shall be
    * considered.
    * 
    * @param indexed the Boolean value to be set or null for default value.
    */
   void setMultiValued(Boolean multiValued)
   {
      this.multiValued = multiValued;
   }

   /**
    * @return a true Boolean whether this field is required, false otherwise and
    *         maybe null denoting that the default false value shall be
    *         considered.
    */
   public Boolean isRequired()
   {
      return this.required;
   }

   /**
    * Specifies whether this field multi-valued or not. The multiValued
    * parameter may be null denoting that the default "false" value shall be
    * considered.
    * 
    * @param indexed the Boolean value to be set or null for default value.
    */
   void setRequired(Boolean required)
   {
      this.required = required;
   }

   @Override
   public String toString()
   {
      return "SolrField [name=\"" + this.getName()
               + "\", type=\"" + this.getType()
               + "\", indexed=\"" + this.isIndexed()
               + "\", stored=\"" + this.isStored()
               + "\", multiValued=\"" + this.isMultiValued()
               + "\", required=\"" + this.isRequired() + "\"]";
   }

} // End SolrField class
