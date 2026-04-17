package com.marketplace.user_service.config;

import com.marketplace.user_service.entity.Role;
import com.marketplace.user_service.entity.User;
import com.marketplace.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
// @Profile("!test") → este seeder NO corre en tests
// En tests usamos TestContainers con BD limpia
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Users already seeded, skipping.");
            return;
        }

        List<User> users = List.of(
                User.builder()
                        .name("Admin Sistema")
                        .email("admin@marketplace.com")
                        .password(passwordEncoder.encode("admin1234"))
                        .role(Role.ADMIN)
                        .isActive(true)
                        .build(),
                User.builder()
                        .name("Carlos Vendedor")
                        .email("vendedor@marketplace.com")
                        .password(passwordEncoder.encode("vendedor1234"))
                        .role(Role.SELLER)
                        .isActive(true)
                        .build(),
                User.builder()
                        .name("María Compradora")
                        .email("comprador@marketplace.com")
                        .password(passwordEncoder.encode("comprador1234"))
                        .role(Role.CUSTOMER)
                        .isActive(true)
                        .build(),
                User.builder()
                        .name("Demo User")
                        .email("demo@marketplace.com")
                        .password(passwordEncoder.encode("demo1234"))
                        .role(Role.CUSTOMER)
                        .isActive(true)
                        .build());

        userRepository.saveAll(users);
        log.info("✅ Seeded {} demo users.", users.size());
        log.info("   Admin:     admin@marketplace.com / admin1234");
        log.info("   Vendedor:  vendedor@marketplace.com / vendedor1234");
        log.info("   Comprador: comprador@marketplace.com / comprador1234");
    }
}