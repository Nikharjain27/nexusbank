package com.nexusbank.customer.repository;

import com.nexusbank.customer.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByCustomerNumber(String customerNumber);

    Optional<Customer> findByEmailIgnoreCase(String email);

    boolean existsByCustomerNumber(String customerNumber);

    boolean existsByEmailIgnoreCase(String email);
}