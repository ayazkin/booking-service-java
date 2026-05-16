package com.booking.javaproject.user.model;

import com.booking.javaproject.auth.model.Role;
import com.booking.javaproject.auth.repository.RoleRepository;
import com.booking.javaproject.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-relationships;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class UserRelationshipsTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void savesUserWithProfileAndRoles() {
        Role admin = roleRepository.save(new Role("ROLE_ADMIN"));
        Role manager = roleRepository.save(new Role("ROLE_MANAGER"));

        User user = new User("ivan", "ivan@example.com", "{noop}password");
        user.setProfile(new UserProfile("Ivan", "Petrov", "+79990000000"));
        user.addRole(admin);
        user.addRole(manager);

        Long userId = userRepository.saveAndFlush(user).getId();

        User savedUser = userRepository.findById(userId).orElseThrow();

        assertThat(savedUser.getProfile()).isNotNull();
        assertThat(savedUser.getProfile().getUser()).isSameAs(savedUser);
        assertThat(savedUser.getProfile().getFirstName()).isEqualTo("Ivan");
        assertThat(savedUser.getProfile().getLastName()).isEqualTo("Petrov");
        assertThat(savedUser.getRoles())
                .extracting(Role::getName)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_MANAGER");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        Set<String> adminUsers = admin.getUsers().stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());
        assertThat(adminUsers).contains("ivan");
    }
}
