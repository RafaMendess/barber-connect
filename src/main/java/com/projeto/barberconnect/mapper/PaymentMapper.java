package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.payment.PaymentResponseDto;
import com.projeto.barberconnect.entity.Payment;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentResponseDto toResponse(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                SummaryMapper.toAppointmentSummary(payment.getAppointment()),
                payment.getType(),
                payment.getStatus(),
                payment.getPaymentDate(),
                payment.getCreatedAt()
        );
    }
}
