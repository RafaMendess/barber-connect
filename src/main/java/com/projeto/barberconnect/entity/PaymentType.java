package com.projeto.barberconnect.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de pagamento.")
public enum PaymentType {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    PIX
}
