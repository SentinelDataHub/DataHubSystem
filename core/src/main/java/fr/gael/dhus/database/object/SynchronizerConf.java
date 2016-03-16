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
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.quartz.CronExpression;

/**
 * A database object to store synchronizers configuration.
 */
@Entity
@Table (name = "SYNCHRONIZERS")
public class SynchronizerConf implements Serializable
{
   /** Class SynchronizerConf v1. */
   private static final long serialVersionUID = 1L;

   /** Table row index. */
   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID", nullable = false)
   private Long id;

   /**
    * Absolute path to an implementation of
    * {@link fr.gael.dhus.datastore.sync.Synchronizer},
    * 
    * or a relative path if the implementation is in the package
    * {@link fr.gael.dhus.datastore.sync.impl}.
    */
   @Column (name = "TYPE", nullable = false)
   private String type;

   /**
    * Synchronizer's label, easier to remember than an ID.
    */
   @Column (name = "LABEL")
   private String label;

   /** The pace of the synchronization. */
   @Column (name = "CRON_EXP", nullable = false)
   private String cronExpression;

   /** If this synchronizer is active (run by the system). */
   @Column (name = "ACTIVE", nullable = false ,
           columnDefinition = "boolean default false")
   private boolean active = false;

   @Temporal (TemporalType.TIMESTAMP)
   @Column (name = "CREATED", nullable = false)
   private Date created;

   @Temporal (TemporalType.TIMESTAMP)
   @Column (name = "MODIFIED", nullable = false)
   private Date modified;

   /** Configuration in a Map. */
   @Cascade (CascadeType.DELETE)
   @ElementCollection (fetch = FetchType.EAGER)
   @MapKeyColumn(name="CONFIG_KEY", nullable = false)
   @Column (name = "CONFIG_VALUE", nullable = false)
   @CollectionTable(name="SYNCHRONIZERS_CONFIG",
         joinColumns=@JoinColumn(name="SYNC_ID"))
   private final Map<String, String> config = new HashMap<> ();

   /**
    * Returns the table row index for this {@link SynchronizerConf}.
    * @return the table row index.
    */
   public long getId ()
   {
      return this.id;
   }

    /**
    * @return this Synchronizer's label.
    */
   public String getLabel()
   {
      return this.label;
   }

   /**
    * @param label for this synchronizer.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * Returns the pace of the synchronization.
    * @return a cron expression.
    */
   public String getCronExpression ()
   {
      return this.cronExpression;
   }

   /**
    * Sets the pace of the synchronization.
    * <pre>
    *   * * * * * *
    *   | | | | | |
    *   | | | | | `----- day of week (* 0-6 ?)
    *   | | | | `---------- month (* 1-12)
    *   | | | `--------------- day of month (* 1-31 ?)
    *   | | `-------------------- hour (* 0-23)
    *   | `------------------------- min (* 0-59)
    *   `------------------------------ seconds (* 0-59)
    * </pre>
    * @param cronExpression a cron Expression
    * @throws ParseException failed to parse the given cron expression.
    */
   public void setCronExpression (String cronExpression) throws ParseException
   {
      CronExpression.validateExpression (cronExpression);
      this.cronExpression = cronExpression;
   }

   /**
    * Returns if the synchronizer is flagged as active in the database.
    * @return {@code true} if active.
    */
   public boolean getActive ()
   {
      return this.active;
   }

   /**
    * Sets the active flag for this synchronizer.
    * @param active {@code true} if active.
    */
   public void setActive (boolean active)
   {
      this.active = active;
   }

   /**
    * Returns the path to the implementation of Synchronizer.
    * @see #setType()
    * @return the path to the implementation of Synchronizer.
    */
   public String getType ()
   {
      return type;
   }

   /**
    * Sets the path to the implementation of Synchronizer.
    * This path must be absolute, but can be relative if the implementation is
    * in the package {@link fr.gael.dhus.datastore.sync.impl}.
    * <p>
    * Valid type examples:
    * {@code "fr.gael.dhus.datastore.sync.impl.ODataSynchronizer"},
    * {@code "ODataSynchronizer"}, {@code "com.group.syncimpl.MySynchronizer"}.
    * @param type a path to an implementation of Synchronizer.
    */
   public void setType (String type)
   {
      this.type = type;
   }

   /**
    * @return this Synchronizer's creation Date (informative).
    */
   public Date getCreated ()
   {
      return created;
   }

   /**
    * @param created creation time for this Synchronizer.
    */
   public void setCreated (Date created)
   {
      this.created = created;
   }

   /**
    * @return this Synchronizer's last modification Date (informative).
    */
   public Date getModified ()
   {
      return modified;
   }

   /**
    * Last modification by an Admin (not a synchronizer itself).
    * @param modified last modification time for this Synchronizer.
    */
   public void setModified (Date modified)
   {
      this.modified = modified;
   }

   /**
    * Returns the configuration as a key,value map.
    * @return the configuration.
    */
   public Map<String, String> getConfig ()
   {
      return this.config;
   }

   /**
    * Gets the value associated with the given key in the configuration.
    * @param key the key.
    * @return a value.
    */
   public String getConfig (String key)
   {
      return this.config.get (key);
   }

   /**
    * Sets a value for the given key in the configuration.
    * @param key   key configuration to set (not null and length<255).
    * @param value value (not null and length<2048).
    * @return the replaced value (or {@code null})
    */
   public String setConfig (String key, String value)
   {
      Objects.requireNonNull (key, "Null `key` param not allowed");
      Objects.requireNonNull (value, "Null `value` param not allowed");

      return this.config.put (key, value);
   }

   /**
    * Put all the key,value pairs from the given map into this object's
    * configuration.
    * @param config a key,value configuration.
    */
   public void setConfig (Map<String, String> config)
   {
      this.config.putAll (config);
   }

   /**
    * Removes the key-value association from the database.
    * @param key key configuration to remote (not null).
    * @return the removed value.
    */
   public String removeConfig (String key)
   {
      return this.config.remove (key);
   }
}
