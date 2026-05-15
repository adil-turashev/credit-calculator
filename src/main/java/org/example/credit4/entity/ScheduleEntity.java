package org.example.credit4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_schedule")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private CreditRequestEntity request;

    @Column(name = "month_number")
    private Integer monthNumber;

    @Column(precision = 19, scale = 2)
    private BigDecimal interest;

    @Column(name = "principal_part", precision = 19, scale = 2)
    private BigDecimal principalPart;

    @Column(precision = 19, scale = 2)
    private BigDecimal payment;

    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;
}