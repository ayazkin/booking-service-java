package com.booking.javaproject.common.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class JsonSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonSecurityExceptionHandler.class);

    private final ObjectMapper objectMapper;
    private final LoginUrlAuthenticationEntryPoint loginEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");

    public JsonSecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        if (!expectsJson(request)) {
            loginEntryPoint.commence(request, response, authException);
            return;
        }

        log.warn("Unauthorized request for {} {}: {}", request.getMethod(), request.getRequestURI(), authException.getMessage());
        writeJson(response, HttpStatus.UNAUTHORIZED, "Требуется авторизация");
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        log.warn("Access denied for {} {}: {}", request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());

        if (!expectsJson(request)) {
            forwardToForbiddenPage(request, response);
            return;
        }

        writeJson(response, HttpStatus.FORBIDDEN, "Доступ запрещен");
    }

    private void forwardToForbiddenPage(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.FORBIDDEN.value());
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Доступ запрещен");
        request.getRequestDispatcher("/error/403").forward(request, response);
    }

    private boolean expectsJson(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader(HttpHeaders.ACCEPT);

        return uri.startsWith("/api/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));
    }

    private void writeJson(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ApiErrorResponse(false, message));
    }
}
