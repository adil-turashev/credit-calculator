package org.example.credit4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegiterForm {
    @NotBlank(message = "Введите логин")
    private String username;

    @NotBlank(message = "Введите пароль ")
    @Size(min = 4, message = "Пароль должен быть минимум 4 символа")
    private String password;

    @NotBlank(message = "Выберите роль")
    @Pattern(regexp = "USER|MANAGER", message = "Роль должна быть USER или MANAGER")
    private String role;
}