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
package fr.gael.dhus.sync;

import java.util.Date;

import org.quartz.CronExpression;

/** Contains the status of a synchronizer. */
public class SynchronizerStatus
{
   /**
    * Since when the this status is the current status.
    * May be {@code null} if the status is {@code UNKNOWN}.
    */
   public final Date since;
   /** Current status. */
   public final Status status;
   /**
    * Message that may come with the current status.
    * <p>if {@code status == RUNNING}, then is null.
    * <p>if {@code status == PENDING}, then contains the next running date.
    * <p>if {@code status == STOPPED}, then is null.
    * <p>if {@code status == ERROR}, then contains the error message.
    */
   public final String message;

   /**
    * Creates a new SynchronizerStatus.
    * @param status RUNNING, PENDING, STOPPED or ERROR.
    * @param since from what time this status is the current status.
    * @param message status message.
    */
   public SynchronizerStatus (Status status, Date since, String message)
   {
      this.message = message;
      this.since = since;
      this.status = status;
   }

   /** Statuses. */
   public static enum Status {
      /** This synchronizer is being synchronized at the moment. */
      RUNNING,
      /** This synchronizer is pending for synchronization. */
      PENDING,
      /** This synchronizer is stopped. */
      STOPPED,
      /** This synchronizer has thrown a exception. */
      ERROR,
      /** Failed to get the status of a synchronizer. */
      UNKNOWN;
   }

   /**
    * Makes a Pending status.
    * @param ce cronExpression scheduling the synchonizer
    * @return Pending.
    */
   public static SynchronizerStatus makePendingStatus (CronExpression ce)
   {
      Date now = new Date ();
      return new SynchronizerStatus (Status.PENDING, now,
            "Next activation: " + ce.getNextValidTimeAfter (now).toString ());
   }

   /**
    * Makes a Running status.
    * @return Running.
    */
   public static SynchronizerStatus makeRunningStatus ()
   {
      return new SynchronizerStatus (Status.RUNNING, new Date(), null);
   }

   /**
    * Makes a Stopped status.
    * @param since from what time the synchronizer has been stopped.
    * @return Stopped
    */
   public static SynchronizerStatus makeStoppedStatus (Date since)
   {
      return new SynchronizerStatus (Status.STOPPED, since, null);
   }

   /**
    * Makes an Error status.
    * @param message error message.
    * @return Error.
    */
   public static SynchronizerStatus makeErrorStatus (String message)
   {
      return new SynchronizerStatus (Status.ERROR, new Date(), message);
   }

   /**
    * Makes an Unknown status.
    * @return Unknown.
    */
   public static SynchronizerStatus makeUnknownStatus ()
   {
      return new SynchronizerStatus (Status.UNKNOWN, null, null);
   }
}
