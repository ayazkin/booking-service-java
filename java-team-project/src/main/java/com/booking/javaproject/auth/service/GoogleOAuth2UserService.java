package com.booking.javaproject.auth.service;

import com.booking.javaproject.auth.model.Role;
import com.booking.javaproject.user.model.User;
import com.booking.javaproject.user.model.UserProfile;
import com.booking.javaproject.user.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private static final String EMAIL_ATTRIBUTE = "email";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    public GoogleOAuth2UserService(
            UserRepository userRepository,
            RoleService roleService,
            PasswordEncoder passwordEncoder,
            PlatformTransactionManager transactionManager
    ) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);
        return transactionTemplate.execute(status -> syncUser(oauthUser));
    }

    private OAuth2User syncUser(OAuth2User oauthUser) {
        String email = normalizeEmail(oauthUser.getAttribute(EMAIL_ATTRIBUTE));
        if (email == null) {
            throw oauthException("Google account did not provide an email");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email, oauthUser.getAttributes()));

        if (!user.isEnabled()) {
            throw oauthException("User account is disabled");
        }

        ensureUserRole(user);

        return new DefaultOAuth2User(
                authoritiesFor(user),
                oauthUser.getAttributes(),
                EMAIL_ATTRIBUTE
        );
    }

    private User createUser(String email, Map<String, Object> attributes) {
        User user = new User(email, email, passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setProfile(new UserProfile(
                normalizeName(attributes.get("given_name"), "Google"),
                normalizeName(attributes.get("family_name"), "User"),
                null
        ));
        user.addRole(roleService.findOrCreateUserRole());
        return userRepository.save(user);
    }

    private void ensureUserRole(User user) {
        boolean hasUserRole = user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(RoleService.USER_ROLE::equals);
        if (!hasUserRole) {
            user.addRole(roleService.findOrCreateUserRole());
            userRepository.save(user);
        }
    }

    private Collection<GrantedAuthority> authoritiesFor(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private String normalizeEmail(Object value) {
        if (!(value instanceof String email) || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(Object value, String fallback) {
        if (!(value instanceof String name) || name.isBlank()) {
            return fallback;
        }
        return name.trim();
    }

    private OAuth2AuthenticationException oauthException(String message) {
        return new OAuth2AuthenticationException(
                new OAuth2Error("invalid_google_user"),
                message
        );
    }
}
