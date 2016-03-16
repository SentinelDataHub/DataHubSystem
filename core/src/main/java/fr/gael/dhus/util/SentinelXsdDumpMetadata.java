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
/**
 * CLI Utility class to be removed once the Sentinel-1 Metadata
 * extraction has been fixed.
 */
package fr.gael.dhus.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;

import fr.gael.dhus.database.object.MetadataIndex;
import fr.gael.dhus.datastore.processing.ProcessingUtils;
import fr.gael.drb.DrbAttribute;
import fr.gael.drb.DrbNode;
import fr.gael.drb.DrbSequence;
import fr.gael.drb.impl.DrbFactoryResolver;
import fr.gael.drb.impl.xml.XmlWriter;
import fr.gael.drb.query.Query;
import fr.gael.drb.value.Value;
import fr.gael.drbx.cortex.DrbCortexItemClass;
import fr.gael.drbx.cortex.DrbCortexMetadataResolver;
import fr.gael.drbx.cortex.DrbCortexModel;

public class SentinelXsdDumpMetadata
{
   final public static String METADATA_NAMESPACE = "http://www.gael.fr/dhus#"; 
   final public static String PROPERTY = "metadataExtractor";
   final public static String MIME_PLAIN_TEXT = "plain/text";
   final public static String MIME_APPLICATION_GML = "application/gml+xml";

   /**
    * Hide utility class constructor
    */
   private SentinelXsdDumpMetadata ()
   {

   }

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      if (!args[0].equals("--dump"))
      {
         // Activates the resolver for Drb
         try
         {
            DrbFactoryResolver.setMetadataResolver (
                  new DrbCortexMetadataResolver (
               DrbCortexModel.getDefaultModel ()));
         }
         catch (IOException e)
         {
            System.err.println ("Resolver cannot be handled.");
         }
         process(args[0]);
      }
      else
      {
      System.out.println("declare function local:deduplicate($list) {");
      System.out.println("  if (fn:empty($list)) then ()");
      System.out.println("  else");
      System.out.println("    let $head := $list[1],");
      System.out.println("      $tail := $list[position() > 1]");
      System.out.println("    return");
      System.out
            .println("      if (fn:exists($tail[ . = $head ])) then " +
                  "local:deduplicate($tail)");
      System.out.println("      else ($head, local:deduplicate($tail))");
      System.out.println("};");
      System.out.println();
      System.out.println("declare function local:values($list) {");
      System.out
            .println("  fn:string-join(local:deduplicate(" +
                  "fn:data($list)), ' ')");
      System.out.println("};");
      System.out.println();

      System.out.println("let $doc := !!!! GIVE PATH HERE !!!!");
      System.out.println("return\n(\n");

      /**
       * Open XML Schema
       */
      final XSLoader loader = new XMLSchemaLoader();

      final XSModel model = loader.loadURI(args[1]);

      XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);

      if (map != null)
      {
         for (int j = 0; j < map.getLength(); j++)
         {
            dumpElement("", (XSElementDeclaration) map.item(j));
         }
      }

      System.out.println("\n) ^--- REMOVE LAST COMMA !!!");
      }
   }

   private static void dumpElement(String path, XSElementDeclaration element)
   {
      String output_path = "" + path + "/" + element.getName();

      XSTypeDefinition type = element.getTypeDefinition();

      if (type.getName().endsWith("Array"))
      {
//         System.err.println(element.getName() + " - " + type.getName()
//            + " SKIPPED !");
         return;
      }

      if (((type.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE)
            || ((XSComplexTypeDefinition) type)
            .getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE))
      {
         if (includesBaseType(type, "string")
               || includesBaseType(type, "integer")
               || includesBaseType(type, "boolean"))
         {
//            System.out.println("         <metadata name=\"" +
// element.getName() + "\" type=\"text/plain\" category=\"\">");
//            System.out.println("            " +
// indexedName(element.getName())
//               + " { local:values($doc" + output_path + ") }");
//            System.out.println("         </metadata>,");

            System.out.println("         local:getMetadata('" +
                  element.getName() + "', '" +
                  indexedName(element.getName()) + "',");
            System.out.println("            $doc" + output_path + "),");
         }
      }
      else
      {
         dumpParticle(output_path,
               ((XSComplexTypeDefinition)type).getParticle());
      }

   }

   private static void dumpParticle(String path, XSParticle particle)
   {
      XSTerm term = particle.getTerm();

      switch (term.getType())
      {
         case XSConstants.ELEMENT_DECLARATION:
            dumpElement(path, (XSElementDeclaration) term);
            break;
         case XSConstants.MODEL_GROUP:
            XSModelGroup model_group = (XSModelGroup) term;
            final XSObjectList particles = model_group.getParticles();

            for (int ipar = 0; ipar < particles.getLength(); ipar++)
            {
               dumpParticle(path, (XSParticle) particles.item(ipar));
            }
            break;
         default:
            System.err.println(path + " - UNKNOWN");
      }

   }

   @SuppressWarnings ("unused")
   private static XSTypeDefinition getBaseType(XSTypeDefinition type)
   {
      if ("http://www.w3.org/2001/XMLSchema".equals(type.getNamespace()))
         return type;

      XSTypeDefinition base_type = type.getBaseType();

      if (base_type != null)
      {
         return getBaseType(base_type);
      }
      else
         return type;
   }

   private static boolean includesBaseType(XSTypeDefinition type,
         String type_name)
   {
      XSTypeDefinition base_type = type.getBaseType();

      if (base_type != null)
      {
         if (base_type.getName().equals(type_name))
            return true;
         else
            return includesBaseType(base_type, type_name);
      }
      else
         return false;
   }

   private static String indexedName(String name)
   {
      String output = "";

      for (char ch :name.toCharArray())
      {
         if (Character.isUpperCase(ch))
         {
            output += " " + Character.toLowerCase(ch);
         }
         else
            output += ch;
      }
      
      return output;
   }
   
   
   private static void process (String url)
   {
      Collection<String> properties=null;
      DrbNode node=null;
      DrbCortexItemClass cl=null;
      
      // Prepare the index structure.
      Set<MetadataIndex>indexes = new HashSet<MetadataIndex> ();
      
      // Prepare the DRb node to be processed
      try
      {
         // First : force loading the model before accessing items.
         node = ProcessingUtils.getNodeFromPath(url);
         cl = ProcessingUtils.getClassFromNode(node);
         
         System.err.println ("Class \"" + cl.getLabel () + "\" for product " +
               node.getName ());
System.err.println("First child: " + node.getNamedChild("manifest.safe", 1)
      .getFirstChild().getName());
         // Get all values of the metadata properties attached to the item
         // class or any of its super-classes
         properties =
            cl.listPropertyStrings (METADATA_NAMESPACE+PROPERTY, false);
         
         // Return immediately if no property value were found
         if (properties == null)
         {
            System.err.println ("WARN - Item \"" + cl.getLabel()
                  + "\" has no metadata defined.");
            return;
         }
      }
      catch (IOException e)
      {
         throw new UnsupportedOperationException (
            "Error While decoding drb node", e); 
      }
      
      // Loop among retrieved property values
      for (String property : properties)
      {
         // Filter possible XML markup brackets that could have been encoded
         // in a CDATA section
         property = property.replaceAll("&lt;", "<");
         property = property.replaceAll("&gt;", ">");
         /*
         property = property.replaceAll("\n", " "); // Replace eol by
         blank space
         property = property.replaceAll(" +", " "); // Remove contiguous
         blank spaces
         */

         // Create a query for the current metadata extractor
         Query metadataQuery = new Query(property);
//System.err.println("Query= \"" + property + "\"");
         // Evaluate the XQuery
         DrbSequence metadataSequence = metadataQuery.evaluate(node);

         // Check that something results from the evaluation: jump to next
         // value otherwise
         if ((metadataSequence == null)
               || (metadataSequence.getLength() < 1))
         {
            continue;
         }

         // Loop among results
         for (int iitem = 0; iitem < metadataSequence.getLength(); iitem++)
         {
            // Get current metadata node
            DrbNode n = (DrbNode) metadataSequence.getItem(iitem);
            
            // Get name
            DrbAttribute name_att = n.getAttribute ("name");
            Value name_v = null;
            if (name_att != null) name_v = name_att.getValue ();
            String name = null;
            if (name_v != null)
               name=name_v.convertTo (Value.STRING_ID).toString ();
            
            // get type
            DrbAttribute type_att = n.getAttribute ("type");
            Value type_v = null;
            if (type_att != null) type_v = type_att.getValue ();
            else type_v = new fr.gael.drb.value.String (MIME_PLAIN_TEXT);
            String type = type_v.convertTo (Value.STRING_ID).toString ();
            
            // get category
            DrbAttribute cat_att = n.getAttribute ("category");
            Value cat_v = null;
            if (cat_att != null) cat_v = cat_att.getValue ();
            else cat_v = new fr.gael.drb.value.String ("product");
            String category = cat_v.convertTo (Value.STRING_ID).toString ();
            
         // get category
            DrbAttribute qry_att = n.getAttribute ("queryable");
            String queryable = null;
            if (qry_att != null) 
            {
                Value qry_v = qry_att.getValue ();
                if (qry_v != null)
                   queryable = qry_v.convertTo (Value.STRING_ID).toString ();
            }
            
            
            // Get value
            String value = null;
            if (MIME_APPLICATION_GML.equals (type) && n.hasChild ())
            {
               ByteArrayOutputStream out = new ByteArrayOutputStream();
               XmlWriter.writeXML(n.getFirstChild (), out);
               value = out.toString ();
            }
            else // Case of "text/plain"
            {
               Value value_v = n.getValue ();
               if (value_v != null)
               {
                  value = value_v.convertTo (Value.STRING_ID).toString ();
                  value = value.trim();
               }
            }
            
            if ((name != null) && (value != null))
            {
               MetadataIndex index = new MetadataIndex ();
               index.setName (name);
               try
               {
                  index.setType (new MimeType (type).toString ());
               }
               catch (MimeTypeParseException e)
               {
                  System.err.println (
                     "Wrong metatdata extractor mime type in class \"" +
                     cl.getLabel () + "\" for metadata called \"" + name + 
                     "\".");
               }
               index.setCategory (category);
               index.setValue (value);
               index.setQueryable (queryable);
System.err.println("Index: name=\"" + index.getName() + "\", value=\"" +
      index.getValue() + "\", category=\"" + index.getCategory() + "\"");
               indexes.add (index);
            }
            else
            {
               String field_name="";
               if (name != null) field_name = name;
               else if (queryable != null) field_name = queryable;
               else if (category != null) field_name = "of category " +
                     category ;
               
               System.err.println("Nothing extracted for field " + field_name);
            }
         }
      }
      // Add ingestion date entry
      MetadataIndex index = new MetadataIndex ();
      index.setCategory ("product");
      index.setName ("Ingestion Date");
      index.setQueryable ("ingestionDate");
      SimpleDateFormat df = new SimpleDateFormat (
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      index.setValue (df.format (new Date()));
      indexes.add (index);
      
      return;
   }
}
