package com.projeto.barberconnect.dto.dashboard;

import java.util.List;

public record BarberDashboardResponseDto(
        Long barberId,
        String barberName,

        // Totais do barbeiro
        Long totalAppointmentsToday,
        Long totalAppointmentsThisMonth,
        Long totalConfirmed,
        Long totalCancelled,
        Long totalCompleted,
        Long totalPending,

        // Próximos agendamentos do dia
        List<AppointmentSummaryDto> upcomingToday
) {
}
