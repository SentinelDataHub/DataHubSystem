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
package fr.gael.dhus.database.object.statistic;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import fr.gael.dhus.database.object.AbstractTimestampEntity;
import fr.gael.dhus.database.object.User;

/**
 * @author moucha
 */

@MappedSuperclass
@Inheritance (strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ActionRecord extends AbstractTimestampEntity implements Serializable 
{
   private static final long serialVersionUID = -6258559874812820257L;
   
   public static String STATUS_STARTED = "STARTED";
   // never used? public static String STATUS_CLOSED = "CLOSED";
   public static String STATUS_SUCCEEDED = "SUCCEEDED";
   public static String STATUS_FAILED = "FAILED";
   
   public static String PERIODICITY_YEAR = "YEAR";
   public static String PERIODICITY_MONTH = "MONTH";
   public static String PERIODICITY_WEEK = "WEEK";
      
   @Id
   @GeneratedValue (strategy = GenerationType.AUTO)
   @Column (name = "ID", nullable = false)
   private Long id;

   /**
    * Action name
    */
   @Column (name = "NAME", nullable = true)
   private String name;

   /**
    * Action status. Possible values are 'STARTED', 'CLOSED', 'SUCCEEDED', 'FAILED'.
    */
   @Column (name = "STATUS", nullable = true)
   private String status;

//   /**
//    * User name
//    */
//   @Column (name = "USER", nullable = true)
//   private String username;

   /**
    * Foreign key USERS_ID
    */
   @ManyToOne
   @JoinColumn (name = "USERS_ID", nullable = true)
   private User user;

   public Long getId ()
   {
      return id;
   }

   public void setId (Long id)
   {
      this.id = id;
   }

   public String getName ()
   {
      return name;
   }

   public void setName (String name)
   {
      this.name = name;
   }

   public String getStatus ()
   {
      return status;
   }

   public void setStatus (String status)
   {
      this.status = status;
   }

//   public String getUsername ()
//   {
//      return username;
//   }
//
//   public void setUsername (String user)
//   {
//      this.username = user;
//   }

   public User getUser ()
   {
      return user;
   }

   public void setUser (User user)
   {
      this.user = user;
   }
}
