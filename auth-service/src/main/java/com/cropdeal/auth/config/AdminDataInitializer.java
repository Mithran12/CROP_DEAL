package com.cropdeal.auth.config;

import com.cropdeal.auth.entity.Role;
import com.cropdeal.auth.entity.User;
import com.cropdeal.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public AdminDataInitializer(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${cropdeal.admin.email:admincropdeal@gmail.com}") String email,
                                @Value("${cropdeal.admin.password:Admin@123}") String password,
                                @Value("${cropdeal.admin.name:CropDeal Admin}") String name) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase();
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(email)) {
            User admin = new User();
            admin.setName(name);
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
        }
    }
}
