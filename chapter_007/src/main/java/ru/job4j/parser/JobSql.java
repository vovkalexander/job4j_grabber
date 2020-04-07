package ru.job4j.parser;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSql implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(ParserJob.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try (ParserJob parser = new ParserJob()) {
            parser.parseJob();
            parser.addVacancyToDB();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

    }
}