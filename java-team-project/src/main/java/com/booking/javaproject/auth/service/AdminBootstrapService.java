package com.booking.javaproject.auth.service;

import com.booking.javaproject.auth.config.AdminBootstrapProperties;
import com.booking.javaproject.auth.model.Role;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.model.UserProfile;
import com.booking.javaproject.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AdminBootstrapService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapService(
            AdminBootstrapProperties properties,
            UserRepository userRepository,
            RoleService roleService,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        roleService.findOrCreateUserRole();

        if (!properties.isEnabled()) {
            return;
        }

        String email = normalizeEmail(properties.getEmail());
        if (email == null || isBlank(properties.getPassword())) {
            log.warn("Admin bootstrap is enabled, but admin email or password is empty");
            return;
        }

        Role adminRole = roleService.findOrCreateAdminRole();
        User admin = userRepository.findByEmail(email)
                .orElseGet(() -> createAdmin(email));

        boolean alreadyAdmin = admin.getRoles().stream()
                .anyMatch(role -> RoleService.ADMIN_ROLE.equals(role.getName()));
        if (!alreadyAdmin) {
            admin.addRole(adminRole);
            userRepository.save(admin);
            log.info("Assigned {} to user {}", RoleService.ADMIN_ROLE, email);
        }
    }

    private User createAdmin(String email) {
        User admin = new User(email, email, passwordEncoder.encode(properties.getPassword().trim()));
        admin.setProfile(new UserProfile(
                normalizeOrDefault(properties.getFirstName(), "System"),
                normalizeOrDefault(properties.getLastName(), "Admin"),
                normalizeOptional(properties.getPhone())
        ));
        admin.addRole(roleService.findOrCreateUserRole());
        return userRepository.save(admin);
    }

    private String normalizeEmail(String email) {
        String normalized = normalizeOptional(email);
        if (normalized == null) {
            return null;
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        String normalized = normalizeOptional(value);
        return normalized == null ? defaultValue : normalized;
    }

    private String normalizeOptional(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
