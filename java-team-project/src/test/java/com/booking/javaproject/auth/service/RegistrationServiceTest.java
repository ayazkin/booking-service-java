package com.booking.javaproject.auth.service;

import com.booking.javaproject.auth.dto.RegisterRequest;
import com.booking.javaproject.auth.model.Role;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:registration-service;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registersUserWithProfileEncodedPasswordAndUserRole() {
        RegisterRequest request = registerRequest("Ivan@example.com", "password123");

        User registeredUser = registrationService.register(request);

        User savedUser = userRepository.findByEmail("ivan@example.com").orElseThrow();
        assertThat(savedUser.getId()).isEqualTo(registeredUser.getId());
        assertThat(savedUser.getUsername()).isEqualTo("ivan@example.com");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getProfile().getFirstName()).isEqualTo("Ivan");
        assertThat(savedUser.getProfile().getLastName()).isEqualTo("Petrov");
        assertThat(savedUser.getProfile().getPhone()).isEqualTo("+79990000000");
        assertThat(savedUser.getRoles())
                .extracting(Role::getName)
                .containsExactly(RegistrationService.USER_ROLE);
    }

    @Test
    void rejectsDuplicateEmail() {
        registrationService.register(registerRequest("ivan@example.com", "password123"));

        assertThatThrownBy(() -> registrationService.register(registerRequest("IVAN@example.com", "password456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with this email already exists");
    }

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setFirstName("Ivan");
        request.setLastName("Petrov");
        request.setPhone("+79990000000");
        request.setPassword(password);
        request.setConfirmPassword(password);
        return request;
    }
}
