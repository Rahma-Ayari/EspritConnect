package tn.esprit.espritconnect2.bootstrap;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Runs UUID schema repair before Hibernate tries to recreate foreign keys.
 */
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(HibernateJpaAutoConfiguration.class)
public class UsersDatabaseRepairConfiguration {

    @Bean
    public Object usersDatabaseEarlyRepair(DataSource dataSource) {
        new UsersSchemaRepairService(new JdbcTemplate(dataSource), dataSource).repairIfNeeded();
        return new Object();
    }
}
