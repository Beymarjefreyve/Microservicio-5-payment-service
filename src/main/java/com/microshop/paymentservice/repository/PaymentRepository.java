package com.microshop.paymentservice.repository;

import com.microshop.paymentservice.model.Payment;
import com.microshop.paymentservice.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Trae el pago más reciente para un orderId — evita NonUniqueResultException
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Payment> findLatestByOrderId(@Param("orderId") String orderId);

    // Trae el pago PENDING más reciente — usado en confirmPayment
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.status = :status ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Payment> findLatestByOrderIdAndStatus(@Param("orderId") String orderId, @Param("status") PaymentStatus status);
}
