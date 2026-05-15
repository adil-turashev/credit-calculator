package org.example.credit4.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.credit4.dto.RegiterForm;
import org.example.credit4.entity.AppUser;
import org.example.credit4.entity.AppUserRole;
import org.example.credit4.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegiterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerForm") RegiterForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (appUserRepository.existsByUsername(form.getUsername())) {
            bindingResult.rejectValue("username", "duplicate", "Такой пользователь уже существует");
            return "register";
        }

        appUserRepository.save(
                AppUser.builder()
                        .username(form.getUsername().trim())
                        .password(passwordEncoder.encode(form.getPassword()))
                        .role(AppUserRole.valueOf(form.getRole()))
                        .build()
        );

        return "redirect:/login?registered";
    }
}