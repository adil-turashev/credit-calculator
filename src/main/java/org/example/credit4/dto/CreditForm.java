package org.example.credit4.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditForm {

    @NotBlank(message = "Введите ФИО")
    private String fullName;

    @NotBlank(message = "Введите номер телефона")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Телефон должен содержать 10-15 цифр, можно с +"
    )
    private String phone;

    @NotNull(message = "Введите сумму кредита")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal principal;

    @NotNull(message = "Введите срок кредита")
    @Min(value = 1, message = "Срок должен быть минимум 1 месяц")
    private Integer months;
}