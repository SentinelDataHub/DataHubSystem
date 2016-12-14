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
package fr.gael.dhus.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.gael.dhus.DHuS;
import fr.gael.dhus.database.dao.SynchronizerDao;
import fr.gael.dhus.database.object.SynchronizerConf;
import fr.gael.dhus.database.object.config.system.ExecutorConfiguration;
import fr.gael.dhus.service.exception.InvokeSynchronizerException;
import fr.gael.dhus.sync.Executor;
import fr.gael.dhus.sync.MetaExecutor;
import fr.gael.dhus.sync.Synchronizer;
import fr.gael.dhus.sync.SynchronizerStatus;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * Manages the {@link Executor}, {@link Synchronizer} and
 * {@link SynchronizerConf}.
 */
@Service
public class SynchronizerService
      implements ISynchronizerService
{
   /** Log. */
   private static final Logger LOGGER = LogManager.getLogger(SynchronizerService.class);

   /** Manages {@link SynchronizerConf}. */
   @Autowired
   private SynchronizerDao synchronizerDao;

   /** Configuration (etc/dhus.xml). */
   @Autowired
   private ConfigurationManager cfgManager;

   @Autowired
   private SecurityService secu;

   /** An instance of {@link Executor}, running the synchronization. */
   private final Executor executor = MetaExecutor.getInstance();

   /**
    * Returns the status of the {@link Executor}.
    * @return {@link Status#RUNNING} or {@link Status#STOPPED}.
    */
   @Override
   public Status getStatus ()
   {
      return executor.isRunning ()? Status.RUNNING: Status.STOPPED;
   }

   /**
    * If the {@link Executor} is not started yet, add all the active
    * {@link Synchronizer} in the {@link Executor} and starts the
    * {@link Executor}.
    */
   @Override
   public void startSynchronization ()
   {
      if (!executor.isRunning ())
      {
         for (SynchronizerConf sc: synchronizerDao.getActiveSynchronizers ())
         {
            try
            {
               Synchronizer sync = instanciate (sc);
               executor.addSynchronizer (sync);
            }
            catch (InvokeSynchronizerException ex)
            {
               LOGGER.error ("Failed to invoke a Synchronizer", ex);
            }
         }
         executor.start (true); // FIXME: true or false?
      }
   }

   /**
    * Stops the {@link Executor}.
    */
   @Override
   public void stopSynchronization ()
   {
      executor.stop ();
      executor.removeAllSynchronizers ();
   }

   /**
    * Returns a {@link SynchronizerConf} by its identifier.
    * @param id {@link SynchronizerConf} identifier.
    * @return a {@link SynchronizerConf} or {@code null} if not found.
    */
   @Override
   public SynchronizerConf getSynchronizerConfById (long id)
   {
      return synchronizerDao.read (id);
   }

   /**
    * Returns a List of {@link SynchronizerConf}.
    * @return a List of {@link SynchronizerConf}.
    */
   @Override
   public Iterator<SynchronizerConf> getSynchronizerConfs ()
   {
      return synchronizerDao.getAllSynchronizerConfs (null);
   }

   @Override
   public Iterator<SynchronizerConf> getSynchronizerConfs (String type)
   {
      return synchronizerDao.getAllSynchronizerConfs (type);
   }

   /**
    * Returns how many {@link SynchronizerConf} exist in the database.
    * @return count.
    */
   @Override
   public int count ()
   {
      return synchronizerDao.count ();
   }

   /**
    * Creates a new {@link Synchronizer} from the given type and cron expression.
    * The newly created {@link Synchronizer} is flagged as not active and is not
    * run by the {@link Executor}.
    *
    * @see SynchronizerConf#setCronExpression(String)
    *
    * @param label see {@link SynchronizerConf#getLabel()}, can be null.
    * @param type see {@link SynchronizerConf#getType()}.
    * @param cron_exp the pace of the synchronization.
    *
    * @return the newly created {@link SynchronizerConf}.
    *
    * @throws ParseException failed to parse the given cron expression.
    */
   @Override
   public SynchronizerConf createSynchronizer (String label, String type,
         String cron_exp) throws ParseException
   {
      SynchronizerConf sc = new SynchronizerConf ();
      sc.setLabel (label);
      sc.setType (type);
      sc.setCronExpression (cron_exp);
      sc.setCreated (new Date());
      sc.setModified (sc.getCreated ());
      sc = synchronizerDao.create(sc);
      LOGGER.info("Synchronizer#" + sc.getId() +
            " created by user " + secu.getCurrentUser().getUsername());
      return sc;
   }

   /**
    * Removes a {@link Synchronizer} with the given identifier.
    * The removed {@link Synchronizer} won't be run any longer.
    * @param id {@link SynchronizerConf} identifier.
    */
   @Override
   public void removeSynchronizer (long id)
   {
      SynchronizerConf sc = synchronizerDao.read (id);
      if (sc != null)
      {
         try
         {
            if (sc.getActive ())
            {
               executor.removeSynchronizer (sc);
            }
         }
         finally
         {
            synchronizerDao.delete (sc);
            LOGGER.info("Synchronizer#" + sc.getId() +
                  " deleted by user " + secu.getCurrentUser().getUsername());
         }
      }
   }

   /**
    * Sets a {@link Synchronizer} active and adds it in the executor.
    * @param id {@link SynchronizerConf} identifier.
    * @throws InvokeSynchronizerException Synchronizer invocation failure.
    */
   @Transactional
   @Override
   public void activateSynchronizer (long id)
         throws InvokeSynchronizerException
   {
      SynchronizerConf sc = synchronizerDao.read (id);
      if (sc != null)
      {
         boolean wasActive = true;
         if (!sc.getActive ())
         {
            sc.setActive (true);
            wasActive = false;
         }
         Synchronizer s = instanciate (sc);
         executor.addSynchronizer (s);
         if (!wasActive)
         {
            synchronizerDao.update (sc);
            LOGGER.info("Synchronizer#" + sc.getId() +
                  " started by user " + secu.getCurrentUser().getUsername());
         }
      }
   }

   /**
    * Sets a {@link Synchronizer} inactive and removes it from the executor.
    * @param id {@link SynchronizerConf} identifier.
    */
   @Transactional
   @Override
   public void deactivateSynchronizer (long id)
   {
      SynchronizerConf sc = synchronizerDao.read (id);
      if (sc != null && sc.getActive ())
      {
         try {
            // Removes the synchronizer from the Executor
            Synchronizer s = executor.removeSynchronizer (sc);
            if (s != null && s.getSynchronizerConf () != null)
            {
               sc = s.getSynchronizerConf ();
            }
            LOGGER.info("Synchronizer#" + sc.getId() +
                  " stopped by user " + secu.getCurrentUser().getUsername());
         }
         finally
         {
            sc.setActive (false);
            synchronizerDao.update (sc);
         }
      }
   }

   /**
    * Enables the batch mode of the {@link Executor}.
    * @see Executor#enableBatchMode(boolean)
    * @param enable {@code true} to enable the batch mode.
    */
   @Override
   public void enableBatchMode (boolean enable)
   {
      executor.enableBatchMode (enable);
   }

   /**
    * Returns {@code true} if the batch mod is enabled.
    * @see Executor#isBatchModeEnabled()
    * @return {@code true} if the batch mod is enabled.
    */
   @Override
   public boolean isBatchModeEnabled ()
   {
      return executor.isBatchModeEnabled ();
   }

   /**
    * Saves the configuration of a {@link Synchronizer}.
    * First it deactivates the synchronizer with the given ID, then it saves its
    * configuration, then it reactivates the synchronizer if its {@code active}
    * field is set to {@code true}.
    * @param sc configuration.
    * @throws InvokeSynchronizerException if the given configuration does not
    *         allow instanciation of a {@link Synchronizer}.
    */
   @Override
   public void saveSynchronizerConf (SynchronizerConf sc)
         throws InvokeSynchronizerException
   {
      long id = sc.getId ();
      // the corresponding synchronizer must be removed from the Executor
      // before we save its configuration in the database.
      deactivateSynchronizer(id);
      sc.setModified (new Date());
      this.synchronizerDao.update (sc);
      if (sc.getActive ())
      {
         activateSynchronizer (id);
      }
      LOGGER.info("Synchronizer#" + sc.getId() +
            " modified by user " + secu.getCurrentUser().getUsername());
   }

   /**
    * Saves the given synchronizer's configuration back in the databse.
    * This method is intended to be used by Synchronizers themselves, this
    * method does no tests at all to avoid deadlocks.
    * <p>
    * This method is unsafe and you should probably be using
    * {@link #saveSynchronizerConf(SynchronizerConf)} instead.
    * @param s to save.
    */
   @Override
   @Transactional (propagation = Propagation.REQUIRES_NEW)
   public void saveSynchronizer (Synchronizer s)
   {
      this.synchronizerDao.update (s.getSynchronizerConf ());
   }

   /**
    * Returns the status of the given {@link Synchronizer}.
    * @param sc an instance of SynchronizerConf that exists in the database.
    * @return status.
    */
   @Override
   public SynchronizerStatus getStatus(SynchronizerConf sc)
   {
      if (!this.executor.isRunning ())
      {
         return new SynchronizerStatus (SynchronizerStatus.Status.STOPPED,
            new Date(0L), "Executor is not running");
      }

      if (!sc.getActive ())
      {
         return SynchronizerStatus.makeStoppedStatus (sc.getModified ());
      }

      SynchronizerStatus ss = this.executor.getSynchronizerStatus (sc);
      if (ss == null)
      {
         return SynchronizerStatus.makeUnknownStatus ();
      }

      return ss;
   }

   /**
    * Creates a new Synchronizer instance of the class returned from
    * {@link SynchronizerConf#getType()}.
    */
   private static Synchronizer instanciate (SynchronizerConf sc)
         throws InvokeSynchronizerException
   {
      String type = sc.getType ();
      if (!type.contains ("."))
      {
         type = "fr.gael.dhus.sync.impl." + type;
      }

      try
      {
         Class<?> type_class = Class.forName (type);

         if (!Synchronizer.class.isAssignableFrom (type_class))
         {
            throw new InvokeSynchronizerException (
                  "type " + type + " is not Synchronizer");
         }

         Class<Synchronizer> sync_impl = (Class<Synchronizer>) type_class;

         Constructor<Synchronizer> con =
               sync_impl.getConstructor (SynchronizerConf.class);
         return con.newInstance (sc);
      }
      // Only catch block below, until the end of the method.
      catch (ClassNotFoundException ex)
      {
         throw new InvokeSynchronizerException (
               "Cannot instanciate Synchronizer, type=" + type, ex);
      }
      catch (NoSuchMethodException ex)
      {
         throw new InvokeSynchronizerException (
               type + " has no constructor with a SynchronizerConf param",
               ex);
      }
      catch (InstantiationException ex)
      {
         throw new InvokeSynchronizerException (
               type + " is an abstract class", ex);
      }
      catch (IllegalAccessException ex)
      {
         throw new InvokeSynchronizerException (
               "Constructor is not public, type=" + type, ex);
      }
      catch (IllegalArgumentException ex)
      {
         throw new InvokeSynchronizerException (ex);
      }
      catch (InvocationTargetException ex)
      {
         String cause = ex.getCause().getMessage();
         if (cause == null || cause.isEmpty())
         {
            cause = ex.getCause().toString();
         }
         throw new InvokeSynchronizerException (
               type + " has thrown an exception while being invoked, cause: " + cause, ex);
      }
   }

   /**
    * Spring ContextClosedEvent listener to terminate the {@link Executor}.
    * <b>YOU MUST NOT CALL THIS METHOD!</b>
    */
   @Override
   public void onApplicationEvent (ContextClosedEvent event)
   {
      LOGGER.debug ("Synchronizer: event " + event + " received");
      if (event == null)
      {
         return;
      }
      // Terminates the Executor
      LOGGER.info ("Synchronization: Executor is terminating");
      executor.terminate ();
      executor.removeAllSynchronizers ();
   }

   /**
    * Starts the {@link Executor} after every webapps have been installed.
    * Called by main start in {@link DHuS}
    * <b>YOU MUST NOT CALL THIS METHOD!</b>
    */
   public void init()
   {
      // Starts the Executor if not started yet
      if (!this.executor.isRunning ())
      {
         ExecutorConfiguration cfg = this.cfgManager.getExecutorConfiguration ();
         if (cfg.isEnabled ())
         {
            this.executor.enableBatchMode (cfg.isBatchModeEnabled ());
            startSynchronization (); // Adds every active synchronizer and starts
            LOGGER.info ("Synchronization: Starting the Executor (batchmode: "
                  + (cfg.isBatchModeEnabled () ? "on": "off") + ')');
         }
      }
   }

}
