package com.projeto.barberconnect.service;

import com.projeto.barberconnect.dto.dashboard.AppointmentSummaryDto;
import com.projeto.barberconnect.dto.dashboard.BarberDashboardResponseDto;
import com.projeto.barberconnect.dto.dashboard.DashboardResponseDto;
import com.projeto.barberconnect.entity.Appointment;
import com.projeto.barberconnect.entity.AppointmentStatus;
import com.projeto.barberconnect.entity.Barber;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.exception.ResourceNotFoundException;
import com.projeto.barberconnect.repository.AppointmentRepository;
import com.projeto.barberconnect.repository.BarberRepository;
import com.projeto.barberconnect.repository.BarbershopRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final BarbershopRepository barbershopRepository;
    private final BarberRepository barberRepository;

    public DashboardService(AppointmentRepository appointmentRepository,
                            BarbershopRepository barbershopRepository,
                            BarberRepository barberRepository) {
        this.appointmentRepository = appointmentRepository;
        this.barbershopRepository = barbershopRepository;
        this.barberRepository = barberRepository;
    }

    // ----------------------------------------------------------------
    // Dashboard da Barbearia (para SHOP_OWNER)
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public DashboardResponseDto getBarbershopDashboard(Long barbershopId, Long currentUserId) {

        // findByIdAndActiveTrue — método existente em BarbershopRepository
        Barbershop barbershop = barbershopRepository.findByIdAndActiveTrue(barbershopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barbershop with id " + barbershopId + " not found"));

        // Apenas o dono pode ver o dashboard da barbearia
        if (!barbershop.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You are not the owner of this barbershop");
        }

        // Intervalos de tempo
        LocalDateTime startOfToday     = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow  = startOfToday.plusDays(1);
        LocalDateTime startOfMonth     = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        // Totais por status no mês
        Long totalConfirmed  = appointmentRepository.countByBarbershopAndPeriodAndStatus(
                barbershopId, startOfMonth, startOfNextMonth, AppointmentStatus.CONFIRMED);
        Long totalCancelled  = appointmentRepository.countByBarbershopAndPeriodAndStatus(
                barbershopId, startOfMonth, startOfNextMonth, AppointmentStatus.CANCELLED);
        Long totalCompleted  = appointmentRepository.countByBarbershopAndPeriodAndStatus(
                barbershopId, startOfMonth, startOfNextMonth, AppointmentStatus.COMPLETED);
        Long totalPending    = appointmentRepository.countByBarbershopAndPeriodAndStatus(
                barbershopId, startOfMonth, startOfNextMonth, AppointmentStatus.PENDING);

        Long totalThisMonth  = totalConfirmed + totalCancelled + totalCompleted + totalPending;

        // Total de hoje (soma dos status ativos)
        Long confirmedToday  = appointmentRepository.countByBarbershopAndPeriodAndStatus(
                barbershopId, startOfToday, startOfTomorrow, AppointmentStatus.CONFIRMED);
        Long pendingToday    = appointmentRepository.countByBarbershopAndPeriodAndStatus(
                barbershopId, startOfToday, startOfTomorrow, AppointmentStatus.PENDING);
        Long completedToday  = appointmentRepository.countByBarbershopAndPeriodAndStatus(
                barbershopId, startOfToday, startOfTomorrow, AppointmentStatus.COMPLETED);
        Long totalToday      = confirmedToday + pendingToday + completedToday;

        // Barbeiros e serviços ativos
        // findAllByBarbershopIdAndActiveTrue — método existente em BarberRepository
        int totalActiveBarbers = barberRepository
                .findAllByBarbershopIdAndActiveTrue(barbershopId).size();

        // Próximos agendamentos do dia (todos os barbeiros da barbearia)
        List<AppointmentSummaryDto> upcomingToday = barberRepository
                .findAllByBarbershopIdAndActiveTrue(barbershopId)
                .stream()
                .flatMap(barber -> appointmentRepository
                        .findAllByBarberIdAndDateRange(
                                barber.getId(), startOfToday, startOfTomorrow)
                        .stream())
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .sorted((a, b) -> a.getAppointmentDateTime()
                        .compareTo(b.getAppointmentDateTime()))
                .map(DashboardService::toSummary)
                .toList();

        // totalActiveServices não existe em OfferedServiceRepository nesta branch,
        // por isso usamos -1 como placeholder — será preenchido após merge com dev-services
        return new DashboardResponseDto(
                barbershopId,
                barbershop.getName(),
                totalToday,
                totalThisMonth,
                totalConfirmed,
                totalCancelled,
                totalCompleted,
                totalPending,
                totalActiveBarbers,
                -1, // placeholder: atualizar após merge com dev-services
                upcomingToday
        );
    }

    // ----------------------------------------------------------------
    // Dashboard do Barbeiro (para BARBER)
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public BarberDashboardResponseDto getBarberDashboard(Long barberId, Long currentUserId) {

        // findByIdAndActiveTrue — método existente em BarberRepository
        Barber barber = barberRepository.findByIdAndActiveTrue(barberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barber with id " + barberId + " not found"));

        // Apenas o próprio barbeiro ou o dono da barbearia podem ver
        boolean isBarberHimself = barber.getUser().getId().equals(currentUserId);
        boolean isOwner         = barber.getBarbershop().getOwner().getId().equals(currentUserId);

        if (!isBarberHimself && !isOwner) {
            throw new AccessDeniedException(
                    "You don't have permission to view this barber's dashboard");
        }

        LocalDateTime startOfToday     = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow  = startOfToday.plusDays(1);
        LocalDateTime startOfMonth     = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        // Totais do barbeiro no mês — reutiliza findAllByBarberIdAndStatus
        long totalConfirmed = appointmentRepository
                .findAllByBarberIdAndStatus(barberId, AppointmentStatus.CONFIRMED).size();
        long totalCancelled = appointmentRepository
                .findAllByBarberIdAndStatus(barberId, AppointmentStatus.CANCELLED).size();
        long totalCompleted = appointmentRepository
                .findAllByBarberIdAndStatus(barberId, AppointmentStatus.COMPLETED).size();
        long totalPending   = appointmentRepository
                .findAllByBarberIdAndStatus(barberId, AppointmentStatus.PENDING).size();

        // Total do mês via dateRange
        long totalThisMonth = appointmentRepository
                .findAllByBarberIdAndDateRange(barberId, startOfMonth, startOfNextMonth)
                .size();

        // Total de hoje
        long totalToday = appointmentRepository
                .findAllByBarberIdAndDateRange(barberId, startOfToday, startOfTomorrow)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .count();

        // Próximos do dia
        List<AppointmentSummaryDto> upcomingToday = appointmentRepository
                .findAllByBarberIdAndDateRange(barberId, startOfToday, startOfTomorrow)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(DashboardService::toSummary)
                .toList();

        return new BarberDashboardResponseDto(
                barberId,
                barber.getUser().getName(),
                totalToday,
                totalThisMonth,
                totalConfirmed,
                totalCancelled,
                totalCompleted,
                totalPending,
                upcomingToday
        );
    }

    // ----------------------------------------------------------------
    // Helper privado
    // ----------------------------------------------------------------

    private static AppointmentSummaryDto toSummary(Appointment appointment) {
        LocalDateTime endsAt = appointment.getAppointmentDateTime()
                .plusMinutes(appointment.getService().getEstimatedTime());

        return new AppointmentSummaryDto(
                appointment.getId(),
                appointment.getAppointmentDateTime(),
                endsAt,
                appointment.getStatus(),
                appointment.getClient().getName(),
                appointment.getBarber().getUser().getName(),
                appointment.getService().getName(),
                appointment.getService().getEstimatedTime()
        );
    }
}
