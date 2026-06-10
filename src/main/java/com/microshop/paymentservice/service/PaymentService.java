package com.microshop.paymentservice.service;

import com.microshop.paymentservice.model.Payment;
import com.microshop.paymentservice.model.PaymentStatus;
import com.microshop.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Value("${notification.service.url:http://localhost:8007/api/notifications}")
    private String notificationServiceUrl;

    public PaymentService(PaymentRepository paymentRepository, RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
    }

    public Payment initiatePayment(String orderId, String email) {
        // Generar OTP de 6 dígitos
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // Si ya existe un pago PENDING para esta orden, reutilizarlo con nuevo OTP
        // Esto evita duplicados cuando el usuario reintenta
        Payment payment = paymentRepository
                .findLatestByOrderIdAndStatus(orderId, PaymentStatus.PENDING)
                .orElse(new Payment());

        payment.setOrderId(orderId);
        payment.setEmail(email);
        payment.setOtpCode(otpCode);
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);

        // Enviar OTP por email via Notification Service (Brevo)
        try {
            Map<String, String> request = new HashMap<>();
            request.put("to", email);
            request.put("subject", "Código de confirmación de pago - MicroShop");
            request.put("body",
                    "Tu código OTP para confirmar el pago del pedido <strong>" + orderId + "</strong> es:<br><br>" +
                    "<h2 style='letter-spacing: 8px; color: #10b981;'>" + otpCode + "</h2><br>" +
                    "Este código es válido para una sola transacción. No lo compartas con nadie.");

            restTemplate.postForEntity(notificationServiceUrl + "/send-payment-otp", request, String.class);
            log.info("OTP email sent successfully to {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage());
        }

        return savedPayment;
    }

    public boolean confirmPayment(String orderId, String otpCode) {
        Optional<Payment> optionalPayment = paymentRepository
                .findLatestByOrderIdAndStatus(orderId, PaymentStatus.PENDING);

        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();

            if (payment.getOtpCode().equals(otpCode)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);
                return true;
            }
        }

        return false;
    }
}
