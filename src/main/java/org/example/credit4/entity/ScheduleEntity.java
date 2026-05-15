package org.example.credit4.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    @ToString.Exclude
    private CreditRequestEntity request;

    private Integer monthNumber;

    @Column(precision = 19, scale = 2)
    private BigDecimal interest;

    @Column(precision = 19, scale = 2)
    private BigDecimal principalPart;

    @Column(precision = 19, scale = 2)
    private BigDecimal payment;

    @Column(precision = 19, scale = 2)
    private BigDecimal balanceAfter;
}

