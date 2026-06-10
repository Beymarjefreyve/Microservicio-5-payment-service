package com.microshop.paymentservice.controller;

import com.microshop.paymentservice.dto.PaymentDTO;
import com.microshop.paymentservice.model.Payment;
import com.microshop.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentDTO.InitiateRequest request) {
        Payment payment = paymentService.initiatePayment(request.getOrderId(), request.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payment initiated. Email with OTP sent.");
        response.put("paymentId", payment.getId());
        response.put("status", payment.getStatus());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody PaymentDTO.ConfirmRequest request) {
        boolean isConfirmed = paymentService.confirmPayment(request.getOrderId(), request.getOtpCode());
        
        if (isConfirmed) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Payment confirmed successfully.");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid OTP or payment not found.");
            response.put("status", "FAILED");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
