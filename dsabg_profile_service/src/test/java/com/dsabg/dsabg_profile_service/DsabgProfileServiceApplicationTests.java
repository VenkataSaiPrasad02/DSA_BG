package com.dsabg.dsabg_profile_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=" +
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.orm.jpa.JpaRepositoriesAutoConfiguration"
})
class DsabgProfileServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context starts without a live database.
        // Add datasource config in application.properties when the database is ready.
    }
}