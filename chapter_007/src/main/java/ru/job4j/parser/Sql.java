package ru.job4j.parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
/**
 * Vacancy.
 * @author Vovk Alexander  vovk.ag747@gmail.com
 * @version $Id$
 * @since 0.1
 */
public class Sql implements AutoCloseable, Job {
    /**
     * Field - stores value of class Log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Sql.class);
    /**
     * Field - stores link of class Connection.
     */
    private Connection connection;
    /**
     * Field - stores object of ArrayList.
     */
    List<Vacancy> vacancies = new ArrayList<>();
    /**
     * Field - stores value of name's vacancy.
     */
    String name;
    /**
     * Field - stores value of hyperlink's vacancy .
     */
    String link;
    /**
     * Field - stores value of text's vacancy.
     */
    String text;
    /**
     * Field - stores date of create.
     */
    String date;
    /**
     * Field - stores object of Properties.
     */
    Properties config = new Properties();
    /**
     * Constructor for activation of connection.
     */
    public Sql(){
        init();
    }
    /**
     * Method  in which is accomplished connection to Postgres.
     * @return logical value;
     */
    public boolean init() {
        try (InputStream in = Sql.class.getClassLoader().getResourceAsStream("app.properties")) {
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            this.connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return this.connection != null;
    }
    /**
     * Method  in which is fulfilled processing of data from URL and inputs into arraylist.
     */
    public void parseJob() throws IOException {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/").get();
        Elements tdElements = doc.getElementsByAttributeValue("class", "postslisttopic");
        tdElements.forEach(tdElement -> {
            Element aElement = tdElement.child(0);
            name = aElement.text();
            if (name.toLowerCase().contains("java")
                    && !name.toLowerCase().contains("закрыт")
                    && !name.toLowerCase().contains("javascript")
                    && !name.toLowerCase().contains("java script")) {
                link = aElement.attr("href");
                try {
                    Document doc1 = Jsoup.connect(link).get();
                    text = doc1.getElementsByAttributeValue("class", "msgBody").text();
                    int index = doc1.getElementsByAttributeValue("class", "msgFooter").text().indexOf(",");
                    date = doc1.getElementsByAttributeValue("class", "msgFooter").text().substring(0, index);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                vacancies.add(new Vacancy(name, text, link, date));
                System.out.println(vacancies);
            }
        });
    }
    /**
     * Method  adds value from url to database.
     */
    public void addVacancyToDB() {
        try (PreparedStatement st = this.connection.prepareStatement("insert into vacancy (name, text, link, date) VALUES (?, ?, ?, ?)" +
                "ON CONFLICT (name) DO NOTHING;"))
        {
            for (int index = 0; index < vacancies.size(); index++) {
                st.setString(1, vacancies.get(index).getName());
                st.setString(2, vacancies.get(index).getText());
                st.setString(3, vacancies.get(index).getLink());
                st.setString(4, vacancies.get(index).getDate());
                st.executeUpdate();
            }
        }catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext)  {
        try {
            this.parseJob();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.addVacancyToDB();
    }
    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    public static void main(String[] args) throws SchedulerException, IOException {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = sf.getScheduler();
        scheduler.start();
        Sql sql = new Sql();
        JobDetail job = newJob(Sql.class)
                .withIdentity("parse - vacancy", "group1")
                .build();
        CronTrigger trigger =  newTrigger().
                withIdentity("trigger", "group1" )
                .withSchedule(cronSchedule(sql.config.getProperty("cron.time")))
                .build();
        scheduler.scheduleJob(job, trigger);
    }
}
