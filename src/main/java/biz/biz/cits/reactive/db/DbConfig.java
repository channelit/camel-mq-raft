package biz.biz.cits.reactive.db;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DbConfig {


    @Value("${postgres.server}")
    private String[] pgServer;

    @Value("${postgres.port}")
    private int[] pgPort;

    @Value("${postgres.user}")
    private String pgUser;

    @Value("${postgres.pswd}")
    private String pgPswd;

    @Value("${postgres.db}")
    private String pgDb;

    @Bean
    public DataSource dataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(pgServer);
        dataSource.setPortNumbers(pgPort);
        dataSource.setUser(pgUser);
        dataSource.setPassword(pgPswd);
        dataSource.setDatabaseName(pgDb);
        try {
            Statement stmt = dataSource.getConnection().createStatement();
            stmt.execute("CREATE TABLE messages (ID INT, MESSAGE VARCHAR(500))");
            stmt.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource;
    }
}
