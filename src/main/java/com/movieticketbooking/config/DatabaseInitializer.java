package com.movieticketbooking.config;

import com.movieticketbooking.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * Ensures the database schema and sample data are automatically initialized
 * when the application boots up.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final SeatRepository seatRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate, SeatRepository seatRepository, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.seatRepository = seatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            logger.info("Checking database initialization...");
            // Check if movies table exists and has data
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'movies'",
                    Integer.class
            );

            if (count == null || count == 0) {
                logger.info("Initializing schema from classpath:database/schema.sql...");
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("database/schema.sql"));
                populator.execute(dataSource);
                logger.info("Schema initialized successfully.");
            }

            Integer movieCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM movies", Integer.class);
            if (movieCount == null || movieCount == 0) {
                logger.info("Populating sample data from classpath:database/data.sql...");
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("database/data.sql"));
                populator.execute(dataSource);
                logger.info("Sample data inserted successfully.");
            } else {
                logger.info("Database already initialized with {} movies.", movieCount);
            }

            ensureCustomerTableColumns();
            ensureAdminUser();
            ensureSeatsForExistingShows();
        } catch (Exception e) {
            logger.warn("Database initialization notice: {}", e.getMessage());
        }
    }

    private void ensureCustomerTableColumns() {
        try {
            Integer columnExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'password'",
                    Integer.class
            );
            if (columnExists == null || columnExists == 0) {
                logger.info("Adding password column to customers table...");
                jdbcTemplate.execute("ALTER TABLE customers ADD COLUMN password VARCHAR(255) DEFAULT 'password123'");
                jdbcTemplate.execute("UPDATE customers SET password = 'password123' WHERE password IS NULL");
                logger.info("Password column added and initialized.");
            }

            Integer roleExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'customers' AND column_name = 'role'",
                    Integer.class
            );
            if (roleExists == null || roleExists == 0) {
                logger.info("Adding role column to customers table...");
                jdbcTemplate.execute("ALTER TABLE customers ADD COLUMN role VARCHAR(50) DEFAULT 'CUSTOMER'");
                jdbcTemplate.execute("UPDATE customers SET role = 'CUSTOMER' WHERE role IS NULL");
                logger.info("Role column added and initialized.");
            }
        } catch (Exception e) {
            logger.warn("Customer table column check notice: {}", e.getMessage());
        }
    }

    private void ensureAdminUser() {
        try {
            Integer adminExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM customers WHERE LOWER(email) = 'admin@cinepass.com'",
                    Integer.class
            );
            if (adminExists == null || adminExists == 0) {
                logger.info("Seeding system administrator account (admin@cinepass.com)...");
                String adminHashedPw = passwordEncoder.encode("admin123");
                jdbcTemplate.update(
                        "INSERT INTO customers (name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)",
                        "System Administrator", "admin@cinepass.com", "9999999999", adminHashedPw, "ADMIN"
                );
                logger.info("Administrator account seeded successfully.");
            }
        } catch (Exception e) {
            logger.warn("Admin user seeding notice: {}", e.getMessage());
        }
    }

    private void ensureSeatsForExistingShows() {
        List<Long> showIdsWithoutSeats = jdbcTemplate.queryForList(
                "SELECT s.show_id FROM shows s LEFT JOIN seats seat ON s.show_id = seat.show_id " +
                        "WHERE seat.show_id IS NULL GROUP BY s.show_id ORDER BY s.show_id",
                Long.class
        );

        if (showIdsWithoutSeats.isEmpty()) {
            logger.info("All shows already have seat layouts.");
            return;
        }

        logger.info("Creating missing seat layouts for {} show(s)...", showIdsWithoutSeats.size());
        for (Long showId : showIdsWithoutSeats) {
            int created = seatRepository.createSeatsForShow(showId);
            logger.info("Created {} seats for show ID {}.", created, showId);
        }
    }
}
