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
package fr.gael.dhus.service.job;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler extends SchedulerFactoryBean implements
   ApplicationContextAware
{
   @Autowired
   private AutowiringJobFactory autowiringJobFactory;

   private ApplicationContext applicationContext;

   private HashMap<Class<? extends AbstractJob>, Trigger> triggers;
   
   @Override
   public void setApplicationContext (final ApplicationContext context)
   {
      this.applicationContext = context;
   }

   @Override
   public void afterPropertiesSet () throws Exception
   {
      Map<String, AbstractJob> webappBeanNames =
         applicationContext.getBeansOfType (AbstractJob.class);
      triggers = new HashMap<> ();
      for (String webappBeanName : webappBeanNames.keySet ())
      {
         AbstractJob cron = webappBeanNames.get (webappBeanName);
         CronTriggerFactoryBean trigger = new CronTriggerFactoryBean ();
         JobDetail job = JobBuilder.newJob (cron.getClass ()).
            storeDurably (true).build ();
         trigger.setJobDetail (job);
         trigger.setCronExpression (cron.getCronExpression ());
         trigger.setName (webappBeanName + "Trigger");
         trigger.afterPropertiesSet ();
         triggers.put (cron.getClass(), trigger.getObject ());
      }
      super.setTriggers (triggers.values ().toArray (
         new Trigger[triggers.size ()]));
      super.setJobFactory (autowiringJobFactory);
      super.afterPropertiesSet ();
   }

   public Date getNextSystemCheckJobSchedule () throws SchedulerException
   {
      
      return triggers.get (SystemCheckJob.class).getFireTimeAfter (new Date ());
   }

   public Date getNextFileScannerJobSchedule () throws SchedulerException
   {
      return triggers.get (FileScannersJob.class).getFireTimeAfter (new Date());
   }

   public Date getNextSearchesJobSchedule () throws SchedulerException
   {
      return triggers.get (SearchesJob.class).getFireTimeAfter (new Date ());
   }

   public Date getNextDumpDatabaseJobSchedule () throws SchedulerException
   {
      return triggers.get (DumpDatabaseJob.class).getFireTimeAfter (new Date());
   }

   public Date getNextCleanDatabaseDumpJobSchedule () throws SchedulerException
   {
      return triggers.get (CleanDatabaseDumpJob.class).getFireTimeAfter (
         new Date ());
   }

   public Date getNextCleanDatabaseJobSchedule () throws SchedulerException
   {
      return triggers.get (CleanDatabaseJob.class).getFireTimeAfter (
         new Date ());
   }

   public Date getNextSendLogsJobSchedule () throws SchedulerException
   {
      return triggers.get (SendLogsJob.class).getFireTimeAfter (
         new Date ());
   }

   public Date getNextSendEvictionListJobSchedule () throws SchedulerException
   {
      return triggers.get (SendEvictionListJob.class).getFireTimeAfter (
         new Date ());
   }

   public Date getNextEvictionJobSchedule () throws SchedulerException
   {
      return triggers.get (EvictionJob.class).getFireTimeAfter (new Date ());
   }

   public Date getNextScheduleArchiveSynchronization () throws
         SchedulerException
   {
      return triggers.get (ArchiveSynchronizationJob.class).getFireTimeAfter (
            new Date ());
   }

   public Date getNextScheduleSystemCheck () throws SchedulerException
   {
      return triggers.get (SystemCheckJob.class).getFireTimeAfter (new Date ());
   }

   public Date getNextLocalArchiveMonitorJobSchedule ()
      throws SchedulerException
   {
      return triggers.get (ArchiveSynchronizationJob.class).getFireTimeAfter(
         new Date ());
   }
}
