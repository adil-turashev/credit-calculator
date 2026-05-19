package org.example.credit4.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_requests")
public class CreditRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String phone;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal principal;

    private Integer months;

    @Column(precision = 19, scale = 10)
    private BigDecimal monthlyRate;

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPaid;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @Column(name = "user_uuid")
    private String userUuid;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditRequestStatus status;
}