package org.example.credit4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class ScheduleDto {
        private Integer monthNumber;
        private BigDecimal interest;
        private BigDecimal principalPart;
        private BigDecimal payment;
        private BigDecimal balanceAfter;
    }

