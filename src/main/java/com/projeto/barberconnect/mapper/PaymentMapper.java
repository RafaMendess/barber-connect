package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.payment.PaymentResponseDto;
import com.projeto.barberconnect.entity.Payment;

public final class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentResponseDto toResponse(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getAppointment().getId(),
                payment.getAppointment().getClient().getName(),
                payment.getAppointment().getService().getName(),
                payment.getAppointment().getBarber().getUser().getName(),
                payment.getType(),
                payment.getStatus(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
