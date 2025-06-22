package com.lukianchykov.bookingsystem.repository;

import com.lukianchykov.bookingsystem.domain.Payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}