package com.booking.javaproject.auth.service;

import com.booking.javaproject.auth.model.Role;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        String normalizedLogin = login.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedLogin)
                .or(() -> userRepository.findByUsername(normalizedLogin))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(user.getRoles().stream()
                        .map(Role::getName)
                        .map(SimpleGrantedAuthority::new)
                        .toList())
                .build();
    }
}
