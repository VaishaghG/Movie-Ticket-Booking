package com.movieticketbooking.repository;

import com.movieticketbooking.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CustomerRepository using Spring JdbcTemplate.
 */
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    public CustomerRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Customer> customerRowMapper = (rs, rowNum) -> {
        Customer customer = new Customer();
        customer.setId(rs.getLong("customer_id"));
        customer.setName(rs.getString("name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        try {
            customer.setPassword(rs.getString("password"));
        } catch (Exception ignored) {
            // Column might not exist in old table variants
        }
        try {
            String role = rs.getString("role");
            customer.setRole(role != null && !role.isBlank() ? role : "CUSTOMER");
        } catch (Exception ignored) {
            customer.setRole("CUSTOMER");
        }
        if (rs.getTimestamp("created_at") != null) {
            customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return customer;
    };

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers ORDER BY name ASC";
        return jdbcTemplate.query(sql, customerRowMapper);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        List<Customer> results = jdbcTemplate.query(sql, customerRowMapper, id);
        return results.stream().findFirst();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE LOWER(email) = LOWER(?)";
        List<Customer> results = jdbcTemplate.query(sql, customerRowMapper, email);
        return results.stream().findFirst();
    }

    @Override
    public Customer save(Customer customer) {
        String password = customer.getPassword() != null && !customer.getPassword().isBlank() 
                ? customer.getPassword() : null;
        String role = customer.getRole() != null && !customer.getRole().isBlank()
                ? customer.getRole().toUpperCase() : "CUSTOMER";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            String sql = "INSERT INTO customers (name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, customer.getName());
                ps.setString(2, customer.getEmail());
                ps.setString(3, customer.getPhone());
                ps.setString(4, password);
                ps.setString(5, role);
                return ps;
            }, keyHolder);
        } catch (Exception e1) {
            try {
                // Fallback for schema with password but without role column
                String fallbackWithPassword = "INSERT INTO customers (name, email, phone, password) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(fallbackWithPassword, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, customer.getName());
                    ps.setString(2, customer.getEmail());
                    ps.setString(3, customer.getPhone());
                    ps.setString(4, password);
                    return ps;
                }, keyHolder);
            } catch (Exception e2) {
                // Fallback for schema without password column
                String fallbackSql = "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)";
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(fallbackSql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, customer.getName());
                    ps.setString(2, customer.getEmail());
                    ps.setString(3, customer.getPhone());
                    return ps;
                }, keyHolder);
            }
        }

        Number key = keyHolder.getKey();
        if (key != null) {
            customer.setId(key.longValue());
        }
        return customer;
    }

    @Override
    public int update(Customer customer) {
        String role = customer.getRole() != null && !customer.getRole().isBlank() ? customer.getRole() : "CUSTOMER";
        if (customer.getPassword() != null && !customer.getPassword().isBlank()) {
            try {
                String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, password = ?, role = ? WHERE customer_id = ?";
                return jdbcTemplate.update(sql, customer.getName(), customer.getEmail(), customer.getPhone(), customer.getPassword(), role, customer.getId());
            } catch (Exception ignored) {}
            try {
                String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, password = ? WHERE customer_id = ?";
                return jdbcTemplate.update(sql, customer.getName(), customer.getEmail(), customer.getPhone(), customer.getPassword(), customer.getId());
            } catch (Exception ignored) {}
        }
        try {
            String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, role = ? WHERE customer_id = ?";
            return jdbcTemplate.update(sql, customer.getName(), customer.getEmail(), customer.getPhone(), role, customer.getId());
        } catch (Exception ignored) {}
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE customer_id = ?";
        return jdbcTemplate.update(sql, customer.getName(), customer.getEmail(), customer.getPhone(), customer.getId());
    }
}
