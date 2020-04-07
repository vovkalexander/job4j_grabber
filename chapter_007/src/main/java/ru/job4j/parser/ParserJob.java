package ru.job4j.parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.Date;
/**
 * ParserJob.
 * @author Vovk Alexander  vovk.ag747@gmail.com
 * @version $Id$
 * @since 0.1
 */
public class ParserJob implements AutoCloseable {
    /**
     * Field - contains object of class SimpleDateFormat.
     */
    private  SimpleDateFormat sdf = new  SimpleDateFormat("dd MMMM yy", Locale.forLanguageTag("ru"));
    /**
     * Field - stores value of class Log.
     */
    private  Months months;
    /**
     * Field - stores value of class Log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ParserJob.class);
    /**
     * Field - stores link of class Connection.
     */
    private Connection connection;
    /**
     * Field - stores object of ArrayList.
     */
    private  List<Vacancy> vacancies = new ArrayList<>();
    /**
     * Field - stores value of name's vacancy.
     */
    private String name;
    /**
     * Field - stores value of hyperlink's vacancy .
     */
    private String link;
    /**
     * Field - stores value of text's vacancy.
     */
    private String text;
    /**
     * Field - stores date of create.
     */
    private String date;
    /**
     * Field - stores object of Properties.
     */
    private Properties config = new Properties();
    /**
     * Field - contains value of string which is name of website .
     */
    private String url = "https://www.sql.ru/forum/job-offers/";
    /**
     * Constructor for activation of connection.
     */
    public ParserJob(){
        this.months = new Months();
        init();
    }
    /**
     * Method  in which is accomplished connection to Postgres.
     */
    public void init() {
        try (InputStream in = ParserJob.class.getClassLoader().getResourceAsStream("app.properties")) {
            assert in != null;
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            this.connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    /**
     * Method  in which is fulfilled processing of data from URL and inputs into arraylist.
     */
    public void parseJob() throws IOException, SQLException {
        String newDate = null;
        int i = 1;
        long settingDate = this.setDate();
        long currentDate = 0;
        boolean result = true;
        do {
            Document doc = Jsoup.connect(url + i++).get();
            Elements tdElements = doc.getElementsByAttributeValue("class", "postslisttopic");
            for (Element tdElement : tdElements) {
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
                        newDate = this.putDate(date);
                        currentDate = this.conversion(newDate).getTime();
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    if (currentDate >= settingDate ) {
                        vacancies.add(new Vacancy(name, text, link, newDate));
                    } else {
                        result = false;
                    }
                }
            }
        } while (result);
    }
    /**
     * Method verifies available values in DB.
     */
    private boolean isEmpty() throws SQLException {
        boolean res = true;
        try (Statement st = this.connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM vacancy LIMIT 1")) {
            if (rs.next()) {
                res = false;
            }
        }
        return res;
    }
    /**
     * Method makes value from DB into string object.
     * @param data - value from DB.
     * @return String object.
     */
    private String putDate(String data)  {
        Calendar cal = Calendar.getInstance();
        if (data.contains("сегодня")) {
            data = sdf.format(cal.getTime());
        } else if (data.contains("вчера")) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            data = sdf.format(cal.getTime());
        } else {
            String[] array = data.split(" ");
            data = data.replaceAll(array[1], months.addMonths().get(array[1]));
        }
        return  data;
    }
    /**
     * Method makes value from DB into date object.
     * @param data - value from DB.
     * @return Date object.
     */
    private Date conversion(String data) throws ParseException {
        return sdf.parse(data);

    }
    /**
     * Method sets up search start date.
     * @return date to long.
     */
    public long setDate() throws SQLException {
        Calendar calc = Calendar.getInstance();
        if (this.isEmpty()) {
            calc.set(2020,Calendar.JANUARY, 1, 0, 0, 0);
        } else {
            calc.set(Calendar.DAY_OF_MONTH, calc.get(Calendar.DAY_OF_MONTH) - 2);
        }
        return calc.getTimeInMillis();
    }
    /**
     * Method  adds value from url to database.
     */
    public void addVacancyToDB() {
        try (PreparedStatement st = this.connection.prepareStatement("insert into vacancy (name, text, link, date) VALUES (?, ?, ?, ?)" +
                "ON CONFLICT (name) DO NOTHING;"))
        {
            for (Vacancy vacancy : vacancies) {
                st.setString(1, vacancy.getName());
                st.setString(2, vacancy.getText());
                st.setString(3, vacancy.getLink());
                st.setString(4, vacancy.getDate());
                st.executeUpdate();
            }
        }catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
