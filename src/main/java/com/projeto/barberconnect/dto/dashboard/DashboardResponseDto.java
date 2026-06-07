package com.projeto.barberconnect.dto.dashboard;

import java.util.List;

public record DashboardResponseDto(
        Long barbershopId,
        String barbershopName,

        // Totais gerais
        Long totalAppointmentsToday,
        Long totalAppointmentsThisMonth,
        Long totalConfirmed,
        Long totalCancelled,
        Long totalCompleted,
        Long totalPending,

        // Barbeiros ativos na barbearia
        Integer totalActiveBarbers,

        // Serviços ativos na barbearia
        Integer totalActiveServices,

        // Próximos agendamentos do dia (ordenados por horário)
        List<AppointmentSummaryDto> upcomingToday
) {
}
