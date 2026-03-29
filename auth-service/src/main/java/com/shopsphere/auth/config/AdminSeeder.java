package com.shopsphere.auth.config;

import com.shopsphere.auth.model.Role;
import com.shopsphere.auth.model.User;
import com.shopsphere.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    public CommandLineRunner createDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@shopsphere.com";

            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .name("ShopSphere Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ADMIN)
                        .build();

                userRepository.save(admin);
                System.out.println("Default admin created: " + adminEmail);
            }
        };
    }
}