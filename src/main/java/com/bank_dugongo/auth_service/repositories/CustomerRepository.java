package com.bank_dugongo.auth_service.repositories;

import com.bank_dugongo.auth_service.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    boolean existsByEmail(String email);
    
    boolean existsByDocumentNumber(String documentNumber);
}