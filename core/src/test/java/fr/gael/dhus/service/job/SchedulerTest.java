package fr.gael.dhus.service.job;

import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import fr.gael.dhus.service.job.JobScheduler;

public class SchedulerTest
{
   private static Logger logger = LogManager.getLogger();
   
   @Autowired
   JobScheduler scheduler;
   
  @Test
  public void testScheduler () throws SchedulerException
  {
     SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SS");
     logger.info ("Next execution of database save            : " + df.format (scheduler.getNextDumpDatabaseJobSchedule ()));
     logger.info ("Next execution of database history cleanup : " + df.format (scheduler.getNextCleanDatabaseDumpJobSchedule ()));
     logger.info ("Next execution of database cleanup         : " + df.format (scheduler.getNextCleanDatabaseJobSchedule ()));
     logger.info ("Next execution of emailing logs            : " + df.format (scheduler.getNextSendLogsJobSchedule ()));
     logger.info ("Next execution of saved search             : " + df.format (scheduler.getNextSearchesJobSchedule ()));
     logger.info ("Next execution of file scanner             : " + df.format (scheduler.getNextFileScannerJobSchedule ()));
  }
}
