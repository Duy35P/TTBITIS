package com.bitis.luckydraw.repository;

import com.bitis.luckydraw.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByZaloId(String zaloId);
}
