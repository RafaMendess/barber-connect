package com.projeto.barberconnect.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status do pagamento.")
public enum PaymentStatus {
    PENDING,
    PAID,
    REFUNDED,
    FAILED
}
