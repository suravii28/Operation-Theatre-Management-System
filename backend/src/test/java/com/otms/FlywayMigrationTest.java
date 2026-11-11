package com.otms;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlywayMigrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void appliesAndValidatesVersionOneSchema() {
        flyway.validate();
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
    }
}
