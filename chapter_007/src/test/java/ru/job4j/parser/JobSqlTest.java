package ru.job4j.parser;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
/**
 * JobSqlTest.
 * @author Vovk Alexander (vovk.ag747@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public class JobSqlTest {
    public Connection init() {
        try (InputStream input = JobSql.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties config = new Properties();
            config.load(input);
            Class.forName(config.getProperty("driver-class-name"));
            return DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    @Test
    public void receiveDataFromDB() throws SQLException, SchedulerException {
        Connection cn = this.init();
        JobSql job = new JobSql(new Sql());
        job.startUp();
        PreparedStatement st = cn.prepareStatement("SELECT  count(right(date, 2)), (select max(id) from vacancy)   from vacancy " +
                "where right(date, 2)= ?");
        st.setString(1, "20");
        ResultSet rs1 = st.executeQuery();
        assertThat(rs1.next(), is(true));
        assertThat(rs1.getInt(1), is(rs1.getInt(2)));
    }
}
