package tn.esprit.espritconnect2.bootstrap;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Runs UUID schema repair before Hibernate tries to recreate foreign keys.
 */
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(HibernateJpaAutoConfiguration.class)
public class UsersDatabaseRepairConfiguration {

    public static final String REPAIR_BEAN_NAME = "usersDatabaseEarlyRepair";

    @Bean(name = REPAIR_BEAN_NAME)
    public Object usersDatabaseEarlyRepair(DataSource dataSource) {
        new UsersSchemaRepairService(new JdbcTemplate(dataSource), dataSource).repairIfNeeded();
        return new Object();
    }

    @Bean
    static BeanDefinitionRegistryPostProcessor entityManagerFactoryDependsOnUsersRepair() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
                if (!registry.containsBeanDefinition("entityManagerFactory")) {
                    return;
                }
                BeanDefinition definition = registry.getBeanDefinition("entityManagerFactory");
                Set<String> dependsOn = new LinkedHashSet<>();
                if (definition.getDependsOn() != null) {
                    dependsOn.addAll(Arrays.asList(definition.getDependsOn()));
                }
                dependsOn.add(REPAIR_BEAN_NAME);
                definition.setDependsOn(dependsOn.toArray(String[]::new));
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                // no-op
            }
        };
    }
}
