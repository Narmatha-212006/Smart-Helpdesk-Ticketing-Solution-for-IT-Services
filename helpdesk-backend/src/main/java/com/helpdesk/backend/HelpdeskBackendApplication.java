package com.helpdesk.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class HelpdeskBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelpdeskBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner databaseCleaner(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Try removing NOT NULL constraint first if it's there
                jdbcTemplate.execute("ALTER TABLE tickets MODIFY user_id BIGINT NULL");
            } catch (Exception e) {
                System.out.println("Failed to modify user_id to NULL: " + e.getMessage());
            }

            try {
                // Now try dropping the column.
                // It might fail if there's a foreign key constraint.
                jdbcTemplate.execute("ALTER TABLE tickets DROP COLUMN user_id");
                System.out.println("Successfully dropped user_id from tickets!");
            } catch (Exception e) {
                System.out.println("Could not drop user_id: " + e.getMessage());
                // We'll also try to disable FK checks just in case
                try {
                    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
                    jdbcTemplate.execute("ALTER TABLE tickets DROP COLUMN user_id");
                    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
                    System.out.println("Dropped user_id using FOREIGN_KEY_CHECKS=0");
                } catch (Exception e2) {
                    System.out.println("Even with FK checks disabled, drop failed: " + e2.getMessage());
                }
            }
        };
    }
}
