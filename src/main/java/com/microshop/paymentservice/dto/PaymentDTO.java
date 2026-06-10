package com.microshop.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentDTO {

    public static class InitiateRequest {
        @NotBlank(message = "OrderId is required")
        private String orderId;

        @NotBlank(message = "Email is required")
        private String email;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ConfirmRequest {
        @NotBlank(message = "OrderId is required")
        private String orderId;

        @NotBlank(message = "OTP code is required")
        private String otpCode;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    }
}
