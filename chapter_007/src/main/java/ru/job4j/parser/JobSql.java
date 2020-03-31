package ru.job4j.parser;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
/**
 * JobSql.
 * @author Vovk Alexander  vovk.ag747@gmail.com
 * @version $Id$
 * @since 0.1
 */
public class JobSql  {
    /**
     * Field - stores value of class SQL.
     */
    Sql sql;
    /**
     * Constructor for activation fields.
     */
    public JobSql(Sql sql) {
        this.sql = sql;
    }
    /**
     * Method launches connection with DB at a certain time.
     */
    public void startUp() throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = sf.getScheduler();
        scheduler.start();
        JobDetail job = newJob(Sql.class)
                .withIdentity("parse - vacancy", "group1")
                .build();
        CronTrigger trigger =  newTrigger()
                .withIdentity("trigger", "group1")
                .withSchedule(cronSchedule(sql.config.getProperty("cron.time")))
                .build();
        scheduler.scheduleJob(job, trigger);
    }
    public static void main(String[] args) throws SchedulerException {
        JobSql job = new JobSql(new Sql());
        job.startUp();
    }
}
