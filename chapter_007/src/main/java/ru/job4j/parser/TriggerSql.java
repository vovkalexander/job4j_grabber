package ru.job4j.parser;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.Properties;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
/**
 * JobSql.
 * @author Vovk Alexander  vovk.ag747@gmail.com
 * @version $Id$
 * @since 0.1
 */
public class TriggerSql {
    /**
     * Field - stores value of class Log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ParserJob.class);
    /**
     * Method launches connection with DB at a certain time.
     */
    public void startUp() {
        try (InputStream in = TriggerSql.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties config = new Properties();
            config.load(in);
            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler scheduler = sf.getScheduler();
            scheduler.start();
            JobDetail job = newJob(JobSql.class)
                    .withIdentity("parse - vacancy", "group1")
                    .build();
            CronTrigger trigger =  newTrigger()
                    .withIdentity("trigger", "group1")
                    .withSchedule(cronSchedule(config.getProperty("cron.time")))
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    public static void main(String[] args)  {
        TriggerSql job = new TriggerSql();
        job.startUp();
    }
}
