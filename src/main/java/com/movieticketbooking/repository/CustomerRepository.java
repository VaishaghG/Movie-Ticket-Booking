package com.movieticketbooking.repository;

import com.movieticketbooking.model.Customer;
import java.util.List;
import java.util.Optional;

/**
 * Interface demonstrating Abstraction in Customer data access.
 */
public interface CustomerRepository {

    List<Customer> findAll();

    Optional<Customer> findById(Long id);

    Optional<Customer> findByEmail(String email);

    Customer save(Customer customer);

    int update(Customer customer);
}
