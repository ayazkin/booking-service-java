package com.booking.javaproject.auth.service;

import com.booking.javaproject.auth.model.Role;
import com.booking.javaproject.auth.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    public static final String USER_ROLE = "ROLE_USER";
    public static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public Role findOrCreateUserRole() {
        return findOrCreate(USER_ROLE);
    }

    @Transactional
    public Role findOrCreateAdminRole() {
        return findOrCreate(ADMIN_ROLE);
    }

    private Role findOrCreate(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}
