package org.example.credit4.service;

import lombok.RequiredArgsConstructor;
import org.example.credit4.dto.CreditForm;
import org.example.credit4.dto.ResultDto;
import org.example.credit4.dto.ScheduleDto;
import org.example.credit4.entity.CreditRequestEntity;
import org.example.credit4.entity.CreditRequestStatus;
import org.example.credit4.entity.ScheduleEntity;
import org.example.credit4.repository.CreditRequestRepository;
import org.example.credit4.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditService {

    private static final BigDecimal ANNUAL_RATE = new BigDecimal("0.4999");
    private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

    private final CreditRequestRepository requestRepository;
    private final ScheduleRepository scheduleRepository;
    private final TelegramBotService telegramBotService;

    @Transactional
    public ResultDto calculateAndSave(CreditForm form, String ownerKey) {
        BigDecimal principal = form.getPrincipal().setScale(2, RoundingMode.HALF_UP);
        int months = form.getMonths();

        BigDecimal monthlyRate = calculateMonthlyRate();
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, monthlyRate, months);
        LocalDateTime requestedAt = LocalDateTime.now();

        CreditRequestEntity request = CreditRequestEntity.builder()
                .fullName(form.getFullName().trim())
                .phone(form.getPhone().trim())
                .telegramChatId(form.getTelegramChatId().trim())
                .principal(principal)
                .months(months)
                .monthlyRate(monthlyRate)
                .monthlyPayment(monthlyPayment)
                .totalPaid(BigDecimal.ZERO)
                .requestedAt(requestedAt)
                .userUuid(ownerKey)
                .status(CreditRequestStatus.PENDING)
                .build();

        request = requestRepository.save(request);

        List<ScheduleEntity> scheduleEntities = new ArrayList<>();
        List<ScheduleDto> scheduleDtos = new ArrayList<>();

        BigDecimal balance = principal;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (int month = 1; month <= months; month++) {
            BigDecimal interest = balance.multiply(monthlyRate, MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP);
            BigDecimal payment = monthlyPayment;
            BigDecimal principalPart = payment.subtract(interest).setScale(2, RoundingMode.HALF_UP);

            if (month == months) {
                principalPart = balance.setScale(2, RoundingMode.HALF_UP);
                payment = interest.add(principalPart).setScale(2, RoundingMode.HALF_UP);
            }

            balance = balance.subtract(principalPart).setScale(2, RoundingMode.HALF_UP);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            totalPaid = totalPaid.add(payment).setScale(2, RoundingMode.HALF_UP);

            ScheduleEntity scheduleEntity = ScheduleEntity.builder()
                    .request(request)
                    .monthNumber(month)
                    .interest(interest)
                    .principalPart(principalPart)
                    .payment(payment)
                    .balanceAfter(balance)
                    .build();

            scheduleEntities.add(scheduleEntity);

            scheduleDtos.add(ScheduleDto.builder()
                    .monthNumber(month)
                    .interest(interest)
                    .principalPart(principalPart)
                    .payment(payment)
                    .balanceAfter(balance)
                    .build());
        }

        scheduleRepository.saveAll(scheduleEntities);

        request.setTotalPaid(totalPaid);
        requestRepository.save(request);

        return ResultDto.builder()
                .requestId(request.getId())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .principal(request.getPrincipal())
                .months(request.getMonths())
                .monthlyRate(monthlyRate)
                .monthlyPayment(monthlyPayment)
                .totalPaid(totalPaid)
                .requestedAt(requestedAt)
                .status(request.getStatus())
                .schedule(scheduleDtos)
                .build();
    }

    @Transactional
    public void approveRequest(Long id) {
        CreditRequestEntity request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        request.setStatus(CreditRequestStatus.APPROVED);
        requestRepository.save(request);
        telegramBotService.sendNotification(request);
    }

    @Transactional
    public void cancelRequest(Long id) {
        CreditRequestEntity request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        request.setStatus(CreditRequestStatus.CANCELLED);
        requestRepository.save(request);
        telegramBotService.sendNotification(request);
    }

    public long getPendingRequestsCount() {
        return requestRepository.countByStatus(CreditRequestStatus.PENDING);
    }

    public List<CreditRequestEntity> getAllRequest() {
        return requestRepository.findAllByOrderByRequestedAtDesc();
    }

    public List<CreditRequestEntity> getRequestsByOwnerKey(String ownerKey) {
        return requestRepository.findByUserUuidOrderByRequestedAtDesc(ownerKey);
    }

    private BigDecimal calculateMonthlyRate() {
        double monthlyRateDouble = Math.pow(1.0 + ANNUAL_RATE.doubleValue(), 1.0 / 12.0) - 1.0;
        return BigDecimal.valueOf(monthlyRateDouble).setScale(10, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int months) {
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, MATH_CONTEXT);
        BigDecimal pow = onePlusR.pow(months, MATH_CONTEXT);

        return principal.multiply(monthlyRate, MATH_CONTEXT)
                .multiply(pow, MATH_CONTEXT)
                .divide(pow.subtract(BigDecimal.ONE, MATH_CONTEXT), 2, RoundingMode.HALF_UP);
    }
}