package org.example.credit4.config;

import lombok.RequiredArgsConstructor;
import org.example.credit4.entity.AppUser;
import org.example.credit4.entity.AppUserRole;
import org.example.credit4.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!appUserRepository.existsByUsername("manager")) {
            appUserRepository.save(AppUser.builder()
                    .username("manager")
                    .password(passwordEncoder.encode("manager123"))
                    .role(AppUserRole.MANAGER)
                    .build());
        }

        if (!appUserRepository.existsByUsername("user")) {
            appUserRepository.save(AppUser.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role(AppUserRole.USER)
                    .build());
        }
    }
}