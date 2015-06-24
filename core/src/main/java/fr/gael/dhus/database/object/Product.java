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
package fr.gael.dhus.database.object;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Product instance implements a product entry into the database. This product
 * reflects on product in the archive.
 */
@Entity
@Table (name = "PRODUCTS")
public class Product implements Serializable
{
   /**
    * serial id
    */
   private static final long serialVersionUID = -1837334601431802602L;

   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID", nullable = false)
   private Long id;

   @Column (name = "uuid", unique = true, nullable = false)
   private String uuid = UUID.randomUUID ().toString ();

   @Temporal (TemporalType.TIMESTAMP)
   @Column (name = "created", nullable = false)
   private Date created = new Date ();

   @Temporal (TemporalType.TIMESTAMP)
   @Column (name = "updated", nullable = false)
   private Date updated = new Date ();

   @Column (name = "PATH", nullable = false)
   private URL path;

   @Column (name = "IDENTIFIER", nullable = true)
   private String identifier;

   @ElementCollection (targetClass = MetadataIndex.class, fetch = FetchType.EAGER)
   @Cascade(value={CascadeType.ALL})
   @Fetch(value = FetchMode.SUBSELECT)
   @CollectionTable(name="METADATA_INDEXES", joinColumns=@JoinColumn(name="PRODUCT_ID"))
   private List<MetadataIndex> indexes=new ArrayList<MetadataIndex> ();

   @Column (name = "PROCESSED", nullable = false, columnDefinition = "boolean default false")
   private Boolean processed = false;

   @Column (name = "QUICKLOOK_SIZE")
   private Long quicklookSize;
   
   @Column (name = "THUMBNAIL_SIZE")
   private Long thumbnailSize;
   
   @Column (name = "QUICKLOOK_PATH")
   private String quicklookPath = null;

   @Column (name = "THUMBNAIL_PATH")
   private String thumbnailPath = null;

   /**
    * Locked flag used by eviction
    */
   @Column (name = "LOCKED", nullable = false, columnDefinition = "boolean default false")
   private Boolean locked = false;

   /**
    * GML footprint string if any
    */
   @Column (name = "FOOTPRINT", nullable = true, length = 4096)
   private String footPrint;

   @ManyToMany (fetch = FetchType.LAZY)
   @Cascade ({ CascadeType.SAVE_UPDATE })
   @JoinTable (
      name="PRODUCT_USER_AUTH",
      joinColumns={@JoinColumn(name="PRODUCTS_ID", table="PRODUCTS")},
      inverseJoinColumns={@JoinColumn(name="USERS_ID", table="USERS")})
   @OrderBy ("username")
   private Set<User> authorizedUsers = new HashSet<User> ();

   @Embedded
   @Cascade ({CascadeType.ALL})
   private Download download = new Download ();

   @Column (name = "ORIGIN")
   private String origin;

   @Column (name = "SIZE")
   private Long size;

   @Column (name = "ingestionDate")
   private Date ingestionDate;

   @ManyToOne (fetch = FetchType.LAZY)
   @JoinColumn (name = "OWNER_ID", nullable = true)
   private User owner;

   @Column (name = "contentStart")
   private Date contentStart;

   @Column (name = "contentEnd")
   private Date contentEnd;

   /**
    * @return the productId
    */
   public Long getId ()
   {
      return id;
   }

   public void setId (Long id)
   {
      this.id = id;
   }

   /**
    * @return the created
    */
   public Date getCreated ()
   {
      return created;
   }

   /**
    * @param created the created to set
    */
   public void setCreated (Date created)
   {
      this.created = created;
   }

   /**
    * @param path the path to set
    */
   public void setPath (URL path)
   {
      this.path = path;
   }

   /**
    * @return the path
    */
   public URL getPath ()
   {
      return path;
   }

   /**
    * @return the indexes
    */
   public List<MetadataIndex> getIndexes ()
   {
      return this.indexes;
   }

   /**
    * @param indexes the indexes to set
    */
   public void setIndexes (List<MetadataIndex> indexes)
   {
      this.indexes = indexes;
   }

   /**
    * @param locked the locked to set
    */
   public void setLocked (Boolean locked)
   {
      this.locked = locked;
   }

   /**
    * @return the locked
    */
   public Boolean getLocked ()
   {
      return locked;
   }

   /**
    * @param identifier the identifier to set
    */
   public void setIdentifier (String identifier)
   {
      this.identifier = identifier;
   }

   /**
    * @return the identifier
    */
   public String getIdentifier ()
   {
      return identifier;
   }

   /**
    * @param footPrint the footPrint to set
    */
   public void setFootPrint (String footPrint)
   {
      this.footPrint = footPrint;
   }

   /**
    * @return the footPrint
    */
   public String getFootPrint ()
   {
      return footPrint;
   }

   /**
    * @param authorizedUsers the authorizedUsers to set
    */
   public void setAuthorizedUsers (Set<User> authorizedUsers)
   {
      this.authorizedUsers = authorizedUsers;
   }

   /**
    * @return the authorizedUsers
    */
   public Set<User> getAuthorizedUsers ()
   {
      return authorizedUsers;
   }

   /**
    * @param downloadablePath the downloadablePath to set
    */
   public void setDownloadablePath (String downloadablePath)
   {
      getDownload ().setPath (downloadablePath);
   }

   /**
    * @return the downloadablePath
    */
   public String getDownloadablePath ()
   {
      return getDownload ().getPath ();
   }

   public long getDownloadableSize ()
   {
      return getDownload ().getSize ();
   }

   public void setDownloadableSize (long size)
   {
      getDownload ().setSize (size);
   }

   public String getDownloadableType ()
   {
      return getDownload ().getType ();
   }

   public void setDownloadableType (String type)
   {
      getDownload ().setType (type);
   }

   /**
    * @param processed the processed to set
    */
   public void setProcessed (Boolean processed)
   {
      this.processed = processed;
   }

   /**
    * @return the processed
    */
   public Boolean getProcessed ()
   {
      return processed;
   }

   /**
    * @param origin the origin to set
    */
   public void setOrigin (String origin)
   {
      this.origin = origin;
   }

   /**
    * @return the origin
    */
   public String getOrigin ()
   {
      return origin;
   }

   public Long getSize ()
   {
      return size;
   }

   public void setSize (Long size)
   {
      this.size = size;
   }

   /**
    * @return the quicklookPath
    */
   public String getQuicklookPath ()
   {
      return quicklookPath;
   }

   /**
    * @param quicklookPath the quicklookPath to set
    */
   public void setQuicklookPath (String quicklookPath)
   {
      this.quicklookPath = quicklookPath;
   }

   /**
    * @return true if quicklookPath exists
    */
   public Boolean getQuicklookFlag ()
   {
      return this.quicklookPath != null;
   }

   /**
    * @return the thumbnailPath
    */
   public String getThumbnailPath ()
   {
      return thumbnailPath;
   }

   /**
    * @param thumbnailPath the thumbnailPath to set
    */
   public void setThumbnailPath (String thumbnailPath)
   {
      this.thumbnailPath = thumbnailPath;
   }

   /**
    * @return if thumbnail exists
    */
   public Boolean getThumbnailFlag ()
   {
      return thumbnailPath != null;
   }
   
   public String getUuid ()
   {
      return uuid;
   }

   public void setUuid (String uuid)
   {
      this.uuid = uuid;
   }

   public Download getDownload ()
   {
      return download;
   }

   public void setDownload (Download download)
   {
      this.download = download;
   }

   public Date getIngestionDate ()
   {
      return ingestionDate;
   }

   public void setIngestionDate (Date ingestionDate)
   {
      this.ingestionDate = ingestionDate;
   }

   public User getOwner ()
   {
      return owner;
   }

   public void setOwner (User owner)
   {
      this.owner = owner;
   }

   public Date getContentStart ()
   {
      return contentStart;
   }

   public void setContentStart (Date contentStart)
   {
      this.contentStart = contentStart;
   }

   public Date getContentEnd ()
   {
      return contentEnd;
   }

   public void setContentEnd (Date contentEnd)
   {
      this.contentEnd = contentEnd;
   }
   
   public void setQuicklookSize (Long quicklookSize)
   {
      this.quicklookSize = quicklookSize;
   }

   public void setThumbnailSize (Long thumbnailSize)
   {
      this.thumbnailSize = thumbnailSize;
   }
   
   public Long getQuicklookSize ()
   {
      return quicklookSize;
   }

   public Long getThumbnailSize ()
   {
      return thumbnailSize;
   }

   @Override
   public boolean equals (Object o)
   {
      if (o == null) return false;
      if (this == o) return true;
      if ( ! (o instanceof Product)) return false;
      Product other = (Product) o;
      if (this.id == null) return false;
      return this.id.equals (other.id);
   }

   @Override
   public int hashCode ()
   {
      int hash = 7;
      hash = 67 * hash + (this.id != null ? this.id.hashCode () : 0);
      hash = 67 * hash + (this.uuid != null ? this.uuid.hashCode () : 0);
      return hash;
   }

   @Embeddable
   public static class Download
   {
      @Column (name = "DOWNLOAD_PATH")
      private String path;

      @Column (name = "DOWNLOAD_SIZE")
      private Long size = -1L;

      @Column (name = "DOWNLOAD_TYPE")
      private String type = "application/octet-stream";

      @Cascade(value={CascadeType.ALL})
      @ElementCollection (fetch = FetchType.EAGER)
      @MapKeyColumn (name = "DOWNLOAD_CHECKSUM_ALGORITHM")
      @Column (name = "DOWNLOAD_CHECKSUM_VALUE")
      @CollectionTable (name = "CHECKSUMS")
      private Map<String, String> checksums = new HashMap<String, String> ();

      public String getPath ()
      {
         return path;
      }

      public void setPath (String path)
      {
         this.path = path;
      }

      public Long getSize ()
      {
         return size;
      }

      public void setSize (Long size)
      {
         this.size = size;
      }

      public String getType ()
      {
         return type;
      }

      public void setType (String type)
      {
         this.type = type;
      }

      public Map<String, String> getChecksums ()
      {
         return checksums;
      }

      public void setChecksums (Map<String, String> checksums)
      {
         this.checksums = checksums;
      }
   }
}
