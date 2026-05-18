package org.example.credit4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.credit4.dto.ScheduleDto;
import org.example.credit4.entity.CreditRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultDto {
    private Long requestId;
    private String fullName;
    private String phone;
    private BigDecimal principal;
    private Integer months;
    private BigDecimal monthlyRate;
    private BigDecimal monthlyPayment;
    private BigDecimal totalPaid;
    private LocalDateTime requestedAt;
    private CreditRequestStatus status;
    private List<ScheduleDto> schedule;
}