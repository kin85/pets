package com.project.pets.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(
                "ALTER TABLE app.users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT TRUE"
        );
        jdbcTemplate.execute(
                """
                CREATE TABLE IF NOT EXISTS app.user_tokens (
                    id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    token VARCHAR(120) NOT NULL,
                    type VARCHAR(40) NOT NULL,
                    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                    CONSTRAINT pk_user_tokens PRIMARY KEY (id),
                    CONSTRAINT fk_user_tokens_user FOREIGN KEY (user_id)
                        REFERENCES app.users (id) ON DELETE CASCADE
                )
                """
        );
        jdbcTemplate.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS uq_user_tokens_token ON app.user_tokens (token)"
        );
        jdbcTemplate.execute(
                "CREATE INDEX IF NOT EXISTS idx_user_tokens_user_type ON app.user_tokens (user_id, type)"
        );
    }
}
