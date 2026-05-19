package com.booking.javaproject.auth.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class OAuth2LoginModelAdvice {

    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

    public OAuth2LoginModelAdvice(ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @ModelAttribute("googleLoginEnabled")
    public boolean googleLoginEnabled() {
        ClientRegistrationRepository repository = clientRegistrationRepository.getIfAvailable();
        if (repository == null) {
            return false;
        }

        try {
            return repository.findByRegistrationId("google") != null;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
