package com.booking.javaproject.auth.service;

import com.booking.javaproject.auth.model.Role;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-bootstrap;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.admin.enabled=true",
        "app.admin.email=admin@test.local",
        "app.admin.password=adminPassword123",
        "app.admin.first-name=Test",
        "app.admin.last-name=Admin"
})
@Transactional
class AdminBootstrapServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsAdminUserWithUserAndAdminRoles() {
        User admin = userRepository.findByEmail("admin@test.local").orElseThrow();

        assertThat(admin.getUsername()).isEqualTo("admin@test.local");
        assertThat(admin.isEnabled()).isTrue();
        assertThat(passwordEncoder.matches("adminPassword123", admin.getPassword())).isTrue();
        assertThat(admin.getProfile().getFirstName()).isEqualTo("Test");
        assertThat(admin.getProfile().getLastName()).isEqualTo("Admin");
        assertThat(admin.getRoles())
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleService.USER_ROLE, RoleService.ADMIN_ROLE);
    }
}
