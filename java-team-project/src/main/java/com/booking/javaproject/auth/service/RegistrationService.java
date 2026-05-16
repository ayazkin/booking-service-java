package com.booking.javaproject.auth.service;

import com.booking.javaproject.auth.dto.RegisterRequest;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.model.UserProfile;
import com.booking.javaproject.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

    public static final String USER_ROLE = RoleService.USER_ROLE;

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            RoleService roleService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        if (!request.isPasswordConfirmed()) {
            throw new IllegalArgumentException("Passwords must match");
        }

        User user = new User(email, email, passwordEncoder.encode(request.getPassword()));
        user.setProfile(new UserProfile(
                normalizeRequired(request.getFirstName()),
                normalizeRequired(request.getLastName()),
                normalizeOptional(request.getPhone())
        ));
        user.addRole(roleService.findOrCreateUserRole());

        return userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return normalizeRequired(email).toLowerCase(Locale.ROOT);
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
