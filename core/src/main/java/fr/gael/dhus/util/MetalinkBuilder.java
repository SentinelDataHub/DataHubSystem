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
package fr.gael.dhus.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A Metalink v4 builder.<br>
 * It relies on the Standard Java API v.6 and higher.<br>
 * It supports the whole Metalink v4 specifications except the
 * extensions elements.
 * <p>
 * Every method returns its instance for method chaining.
 * <p>
 * Names of fields in this class are the same as the names of elements in
 * the produced XML document.
 * <p>
 * <b>You MUST add at least one file and each file MUST contains
 * at least one url.</b>
 * <p>
 * See the <a href="http://tools.ietf.org/html/rfc5854">
 *    RFC for Metalink v4 (RFC5854)</a>
 * and the <a href="http://www.metalinker.org/">Official website</a>.
 * <p>
 * <b>Example:</b>
 * <pre>
 * SimpleDateFormat sdf = new SimpleDateFormat(
 * MetalinkBuilder.DATE_TIME_FORMAT);
 * sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
 * 
 * MetalinkBuilder mb = new MetalinkBuilder();
 * mb.setGenerator("DHuS/3.8.1")
 *   .setOrigin("http://dhus.gael.fr:8080/", true)
 *   .setPublished(sdf.format(new Date()))
 *   .addFile(
 *   "S1A_S3_SLC__1ASV_20140507T105003_20140507T105033_000490_0005F3_A57E.zip")
 *     .setHash("MD5", "324sdf65468G4EQ34H68QS4FGH3QS847H")
 *     .setPublisher("European Space Agency (ESA)", "http://esa.int/")
 *     .setSize(5000000)
 *     .addUrl("http://dhus.gael.fr:8080/odata/v1/
 *     Products('148539fa-18b6-11e4-a1de-b2227cce2b54')/$value", null, 0)
 * Document meta4 = mb.build();
 * 
 * Transformer transformer = TransformerFactory.newInstance().newTransformer();
 * transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 * transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
 * "2");
 * transformer.transform(new DOMSource(meta4), new StreamResult(System.out));
 * </pre>
 */
public class MetalinkBuilder
{
   /** The content type of the produced document. */
   public static final String CONTENT_TYPE = "application/metalink4+xml";
   
   /** The file extension for a Metalink v4 document is ".meta4" */
   public static final String FILE_EXTENSION = ".meta4";
   
   /** The "date_time" date format defined in the
    * <a href="http://tools.ietf.org/html/rfc3339">RFC3339</a>.
    */
   public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
   
   /** Single static instance of DocBuilder because ServiceLoader is slow. */
   private static final DocumentBuilder DOC_BUILDER;
   
   /** Single static instance of Transformer because ServiceLoader is slow. */
   private static final Transformer TRANSFORMER;
   
   private String generator = null;
   
   /** Contains an IRI and has a boolean attribute. */
   private BasicElement origin = null;
   
   /** Contains a date_time */
   private String published = null;
   
   /** Contains a date_time */
   private String updated = null;
   
   private final ArrayList<MetalinkFileBuilder> files =
         new ArrayList<MetalinkFileBuilder> ();
   
   static {
      try
      {
         DOC_BUILDER = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         TRANSFORMER = TransformerFactory.newInstance().newTransformer();
         TRANSFORMER.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      }
      catch (ParserConfigurationException ex)
      {
         throw new Error("Cannot instanciate DocBuilder with default configuration", ex);
      }
      catch (TransformerConfigurationException ex)
      {
         throw new Error("Cannot instanciate Transformer with default configuration", ex);
      }
   }
   
   /**
    * Builds the XML document.
    * @return A non-null instance of Document containing the whole XML tree.
    * @throws IllegalStateException if there is not file and/or no url in a
    * file.
    */
   public Document build ()
   {
      // Validating
      if (files.size () == 0)
         throw new IllegalStateException ("MetalinkBuilder has no file.");
      for (MetalinkFileBuilder fb: files)
         if (fb.url.size () == 0)
            throw new IllegalStateException ("MetalinkFileBuilder has no url.");
      
      Document doc = DOC_BUILDER.newDocument();
      
      // Root of the XML document
      Element rootElement = doc.createElement("metalink");
      rootElement.setAttribute ("xmlns", "urn:ietf:params:xml:ns:metalink");
      doc.appendChild (rootElement);
      
      // Children elements
      if (generator != null && !generator.isEmpty ())
         appendTextElement(doc, rootElement, "generator", generator);
      
      if (origin != null)
         origin.build (doc, rootElement);
      
      if (published != null)
         appendTextElement(doc, rootElement, "published", published);
      
      if (updated != null)
         appendTextElement(doc, rootElement, "updated", updated);
      
      for (MetalinkFileBuilder fb: files)
         fb.build (doc, rootElement);
      
      return doc;
   }
   
   /**
    * Builds and stringify this metalink document.
    * @param indent {@code true} if the returned XML doc must be indented.
    * @return an XML doc as string.
    * @throws TransformerException if the stringification failed.
    */
   public String buildToString(boolean indent) throws TransformerException
   {
      StringWriter sw = new StringWriter();
      TRANSFORMER.setOutputProperty(OutputKeys.INDENT, (indent)? "yes": "no");
      Document doc = build();
      TRANSFORMER.transform(new DOMSource(doc), new StreamResult(sw));
      return sw.toString();
   }
   
   /**
    * Utility method to create and append a new element in the XML tree.
    * Creates a new Element and appends it to the given parent node.
    * @param doc The XML DOM document.
    * @param parent The parent node in the XML tree.
    * @param name The name of the new element.
    * @param value The text value of the new element.
    */
   private static void appendTextElement (Document doc, Node parent,
         String name, String value)
   {
      Element e = doc.createElement(name);
      e.appendChild (doc.createTextNode (value));
      parent.appendChild (e);
   }

   /**
    * The `generator` element identifies the generating agent name and version
    * used to generate a Metalink Document.
    * @param generator "AgentName/AgentVersion" eg: "DHuS/3.8.1"
    * @return this.
    */
   public MetalinkBuilder setGenerator (String generator)
   {
      this.generator = generator;
      return this;
   }

   /**
    * The `origin` element is an IRI where the Metalink Document was
    * originally published.
    * @param origin An Internationalized Resource Identifiers which can
    *               be an URI.
    * @param dynamic If true, then updated versions of the Metalink can
    *                be found at this IRI.
    * @return this.
    */
   public MetalinkBuilder setOrigin (String origin, boolean dynamic)
   {
      this.origin = new BasicElement ("origin", origin);
      this.origin.addAttribute ("dynamic", dynamic);
      return this;
   }

   /**
    * The `published` element is a Date indicating the initial creation or
    * first availability of the resource.
    * @param published A date_time, see {@link #DATE_TIME_FORMAT}.
    * @return this.
    */
   public MetalinkBuilder setPublished (String published)
   {
      this.published = published;
      return this;
   }

   /**
    * The `updated` element is a Date indicating the most recent instant
    * in time when a Metalink was modified.
    * @param updated A date_time, see {@link #DATE_TIME_FORMAT}.
    * @return this.
    */
   public MetalinkBuilder setUpdated (String updated)
   {
      this.updated = updated;
      return this;
   }

   /**
    * Adds a file to this metalink document.
    * You MUST add at least one file.
    * @param name The local file name to which the downloaded file
    *             will be written.
    * @return A new builder for the new file element.
    * @see MetalinkFileBuilder
    */
   public MetalinkFileBuilder addFile (String name)
   {
      MetalinkFileBuilder newFile = new MetalinkFileBuilder(name);
      files.add(newFile);
      return newFile;
   }
   
   /**
    * File element.<br>
    * You MUST add at least one url.<br>
    * See the <a href="http://tools.ietf.org/html/rfc5854#section-4.1.2">
    *    RFC</a>.
    */
   public class MetalinkFileBuilder
   {
      /** An attribute of the `file` element. */
      private String name = null;
      
      private String copyright = null;
      
      private String description = null;
      
      /** Contains a String and has a `type` attribute. */
      private final HashMap<String, String> hash =
            new HashMap<String, String> ();
      
      private String identity = null;
      
      /** Constains a String. */
      private final ArrayList<String> language = new ArrayList<String> ();
      
      /** Constains a IRI. */
      private String logo = null;
      
      /** Contains an IRI and has two String and one Int attributes. */
      private final ArrayList<BasicElement> metaUrls =
            new ArrayList<BasicElement> ();
      
      /** Contains a String. */
      private final ArrayList<String> os = new ArrayList<String> ();
      
      /** Contains two String attributes. */
      private BasicElement publisher = null;
      
      /** Contains a String and has a String attribute. */
      private BasicElement signature = null;
      
      /** Contains a positive Int. */
      private long size = -1;
      
      /** Contains a String and has one String and one Int attributes. */
      private final ArrayList<BasicElement> url =
            new ArrayList<BasicElement> ();
      
      private String version = null;
      
      private final HashSet<MetalinkFilePiecesBuilder> pieces =
            new HashSet<MetalinkFilePiecesBuilder>();
      
      /** @see MetalinkBuilder#addFile(String) */
      private MetalinkFileBuilder (String name)
      {
         this.name = name;
      }
      
      /**
       * Creates a new file Element and appends it to root node.
       * @param doc The XML DOM document.
       * @param root The root node in the XML tree.
       */
      private void build (Document doc, Node root)
      {
         Element file = doc.createElement ("file");
         file.setAttribute ("name", name);
         root.appendChild (file);
         
         if (copyright != null && !copyright.isEmpty ())
            appendTextElement(doc, file, "copyright", copyright);
         
         if (description != null && !description.isEmpty ())
            appendTextElement(doc, file, "description", description);
         
         for (Entry<String, String> e: hash.entrySet ())
         {
            Element hash = doc.createElement("hash");
            hash.setAttribute ("type", e.getKey ());
            hash.appendChild (doc.createTextNode (e.getValue ()));
            file.appendChild (hash);
         }
         
         if (identity != null && !identity.isEmpty ())
            appendTextElement(doc, file, "identity", identity);
         
         for (String lang: language)
            appendTextElement(doc, file, "language", lang);
         
         if (logo != null && !logo.isEmpty ())
            appendTextElement(doc, file, "logo", logo);
         
         for (BasicElement be: metaUrls)
            be.build (doc, file);
         
         for (String os: this.os)
            appendTextElement(doc, file, "os", os);
         
         if (publisher != null)
            publisher.build (doc, file);
         
         if (signature != null)
            signature.build (doc, file);
         
         if (size >= 0)
            appendTextElement(doc, file, "size", String.valueOf (size));
         
         for (BasicElement url: this.url)
            url.build (doc, file);
         
         if (version != null && !version.isEmpty ())
            appendTextElement(doc, file, "version", version);
         
         for (MetalinkFilePiecesBuilder pieces: this.pieces)
            pieces.build (doc, file);
      }
      
      /**
       * The `copyright` element is a Text that conveys the copyright
       * for this file.
       * @return this.
       */
      public MetalinkFileBuilder setCopyright (String copyright)
      {
         this.copyright = copyright;
         return this;
      }

      /**
       * The `description` element is a Text that describe this file.
       * @return this.
       */
      public MetalinkFileBuilder setDescription (String description)
      {
         this.description = description;
         return this;
      }

      /**
       * The `hash` element is a Text that conveys a cryptographic hash
       * for this file.
       * @param type The type of hash. eg: "SHA-1", "MD5", "SHA-256".
       * @param hash The hash for this file.
       * @return this.
       */
      public MetalinkFileBuilder setHash (String type, String hash)
      {
         this.hash.put (type, hash);
         return this;
      }

      /**
       * The `identity` element is a Text that conveys an identity
       * for this file.
       * @param identity eg: "EO Product".
       * @return this.
       */
      public MetalinkFileBuilder setIdentity (String identity)
      {
         this.identity = identity;
         return this;
      }

      /**
       * The `language` element is a Text that conveys a code for the
       * language of this file.
       * The String parameter MUST conform to the
       * <a href="http://tools.ietf.org/html/rfc5646">RFC5646</a>.
       * @param language A non-null language code. eg: "en-GB".
       * @return this.
       */
      public MetalinkFileBuilder addLanguage (String language)
      {
         if (language != null && !language.isEmpty ()) this.language.add (
               language);
         return this;
      }

      /**
       * The `logo` element's content is an IRI to an image that provides
       * visual identification for a file.
       * @return this.
       */
      public MetalinkFileBuilder setLogo (String logo)
      {
         this.logo = logo;
         return this;
      }

      /**
       * The `metaurl` element contains the IRI of a metadata file
       * (aka a metainfo file), about a resource to download.
       * This could be the IRI of a BitTorrent .torrent file, a
       * Metalink Document, or other type of metadata file.
       * @param url A non-null URL to the metadata file.
       * @param mediatype A non-null type for the referenced document.
       *                  eg: "torrent".
       * @param name The name of the file in the referenced document
       *             (can be null).
       * @param priority A number between 1 and 999999 (inclusive).
       *                 Lower values indicate a higher priority.
       * @return this.
       */
      public MetalinkFileBuilder addMetaUrl (String url, String mediatype,
            String name, int priority)
      {
         BasicElement metaUrl = new BasicElement ("metaurl", url);
         metaUrl.addAttribute ("mediatype", mediatype);
         if (name != null) metaUrl.addAttribute ("mediatype", mediatype);
         if (priority > 1 && priority < 1000000) metaUrl.addAttribute (
               "priority", priority);
         this.metaUrls.add (metaUrl);
         return this;
      }

      /**
       * The `os` element is a Text that conveys an Operating System that
       * this file is suitable for.
       * @param os A non-null IANA "Operating System Name" eg: "WIN32",
       *           "LINUX", "OSX".
       * @return this.
       */
      public MetalinkFileBuilder addOs (String os)
      {
         if (os != null && !os.isEmpty ()) this.os.add (os);
         return this;
      }

      /**
       * The `publisher` element contains the name of entity that has
       * published the file described in the
       * Metalink Document and an IRI for more information.
       * @param name A non-null name of the publisher.
       * @param url An URL to the site of the publisher (can be null).
       * @return this.
       */
      public MetalinkFileBuilder setPublisher (String name, String url)
      {
         this.publisher = new BasicElement ("publisher", null);
         this.publisher.addAttribute ("name", name);
         if (url != null) this.publisher.addAttribute ("url", url);
         return this;
      }

      /**
       * The `signature` element is a Text that conveys a digital signature
       * for this file.
       * @param signature The signature of this file.
       * @param mediatype The type of the signature.
       *                  eg: "application/pgp-signature".
       * @return this.
       */
      public MetalinkFileBuilder setSignature (String signature,
            String mediatype)
      {
         this.signature = new BasicElement ("signature", signature);
         this.signature.addAttribute ("mediatype", mediatype);
         return this;
      }

      /**
       * The `size` element indicates the length of this file in octets.
       * @param size A non-negative Integer.
       * @return this.
       */
      public MetalinkFileBuilder setSize (long size)
      {
         this.size = size;
         return this;
      }

      /**
       * The `url` element contains a file IRI.
       * @param url An url to this file.
       * @param location An [ISO3166-1] 2 letters country code, eg: "gb",
       *                 "us" (Can be null).
       * @param priority A number between 1 and 999999 (inclusive).
       *                 Lower values indicate a higher priority.
       * @return this.
       */
      public MetalinkFileBuilder addUrl (String url, String location,
            int priority)
      {
         BasicElement be = new BasicElement ("url", url);
         if (location != null && location.length () == 2) be.addAttribute (
               "location", location);
         if (priority > 0 && priority < 1000000) be.addAttribute (
               "priority", priority);
         this.url.add (be);
         return this;
      }

      /**
       * The `version` element is a Text that conveys a version for this file.
       * @param version eg: "3.8"
       * @return this.
       */
      public MetalinkFileBuilder setVersion (String version)
      {
         this.version = version;
         return this;
      }
      
      /**
       * A container for a list of cryptographic hashes of contiguous,
       * non-overlapping pieces of this file.
       * @param type The type of hash. eg: "SHA-1", "MD5", "SHA-256".
       * @param length The length of a piece of the file.
       * @return A new builder for the new pieces element.
       * @see MetalinkFilePiecesBuilder
       */
      public MetalinkFilePiecesBuilder setPieces (String type, long length)
      {
         MetalinkFilePiecesBuilder pieces = new MetalinkFilePiecesBuilder (
               type, length);
         this.pieces.add (pieces);
         return pieces;
      }
      
      /**
       * Pieces element.
       * A container for a list of cryptographic hashes of contiguous,
       * non-overlapping pieces of a file.
       */
      public class MetalinkFilePiecesBuilder
      {
         private String type;
         
         private long length;
         
         private final ArrayList<String> hash = new ArrayList<String> ();
         
         /** @see MetalinkFileBuilder#setPieces */
         private MetalinkFilePiecesBuilder (String type, long length)
         {
            if (type.isEmpty () || length < 1)
               throw new IllegalArgumentException (
                     "Bad param for MetalinkFilePiecesBuilder");
            this.type = type;
            this.length = length;
         }
         
         /**
          * Creates a new pieces Element and appends it to a file node.
          * @param doc The XML DOM document.
          * @param file A file node in the XML tree.
          */
         private void build (Document doc, Node file)
         {
            Element pieces = doc.createElement ("pieces");
            pieces.setAttribute ("type", type);
            pieces.setAttribute ("length", String.valueOf (length));
            file.appendChild (pieces);
            
            for (String hash: this.hash)
               appendTextElement (doc, pieces, "hash", hash);
         }
         
         /**
          * The `hash` element is a Text that conveys a cryptographic hash
          * for a piece of this file.
          * Hashes MUST be added in the same order as the corresponding
          * pieces appear in the file.
          * @return this.
          */
         public MetalinkFilePiecesBuilder addHash (String hash)
         {
            if (hash.isEmpty ())
               throw new IllegalArgumentException ("Empty hash not allowed");
            this.hash.add (hash);
            return this;
         }
         
         // Overrides equals and hashCode because instances of this
         // class are stored in a HashSet.
         @Override
         public int hashCode ()
         {
            return type.hashCode ();
         }
         
         @Override
         public boolean equals (Object obj)
         {
            if (obj == null) return false;
            if (this == obj) return true;
            if (getClass () != obj.getClass ()) return false;
            MetalinkFilePiecesBuilder other = (MetalinkFilePiecesBuilder) obj;
            if (type == null && other.type != null) return false;
            return type.equals (other.type);
         }
      }
   }
   
   /**
    * Basic Element
    * Elements with no child
    */
   private class BasicElement
   {
      private final String name;
      private final ArrayList<String> attributesNames  =
            new ArrayList<String> ();
      private final ArrayList<Object> attributesValues =
            new ArrayList<Object> ();
      private final String value;
      
      /** 
       * Creates a new BasicElement.
       * @param name The name of the element.
       * @param value The value of the element.
       * @throws NullPointerException if name is null.
       * @throws IllegalArgumentException if name is empty.
       */
      BasicElement (String name, String value)
      {
         if (name.isEmpty ())
            throw new IllegalArgumentException ("Empty name not allowed.");
         this.name = name;
         this.value = value;
      }

      /**
       * Add an attribute.
       * @param name The name of the attribute.
       * @param value The value of the attribute (its toString method will be
       *              used to write the document).
       * @throws NullPointerException if name or value is null.
       * @throws IllegalArgumentException if name is empty.
       */
      void addAttribute(String name, Object value)
      {
         if (name.isEmpty ())
            throw new IllegalArgumentException ("Empty name not allowed.");
         if (value == null)
            throw new NullPointerException ("Null value not allowed.");
         
         this.attributesNames.add (name);
         this.attributesValues.add (value);
      }
      
      /**
       * Creates a new Element and appends it to the given parent node.
       * @param doc The XML DOM document.
       * @param parent The parent node in the XML tree.
       */
      private void build (Document doc, Node parent)
      {
         Element e = doc.createElement (name);
         
         if (value != null && !value.isEmpty ()) e.appendChild (
               doc.createTextNode (value));
         
         for (int i=0; i<attributesNames.size (); i++)
            e.setAttribute (attributesNames.get (i),
                  attributesValues.get (i).toString ());
         
         parent.appendChild (e);
      }
   }
}
