package biz.cits.reactive.db;

import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DbConfig {

    private static Logger log = LoggerFactory.getLogger(DbConfig.class);

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
            stmt.execute("CREATE TABLE messages (ID UUID PRIMARY KEY, MESSAGE JSONB NOT NULL)");
            stmt.closeOnCompletion();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return dataSource;
    }
}
