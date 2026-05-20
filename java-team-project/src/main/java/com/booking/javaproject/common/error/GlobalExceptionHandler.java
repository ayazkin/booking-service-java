package com.booking.javaproject.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public Object handleResponseStatusException(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatusCode status = exception.getStatusCode();
        String message = resolveMessage(status, exception.getReason());
        logByStatus(status, request, exception);
        return buildErrorResponse(request, status, message);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Object handleNotFound(Exception exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("Resource not found for {} {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildErrorResponse(request, status, "Страница не найдена");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        log.warn("HTTP method is not supported for {} {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildErrorResponse(request, status, "Метод запроса не поддерживается");
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), exception);
        return buildErrorResponse(request, status, "Внутренняя ошибка сервера");
    }

    private Object buildErrorResponse(HttpServletRequest request, HttpStatusCode status, String message) {
        if (expectsJson(request)) {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ApiErrorResponse(false, message));
        }

        ModelAndView modelAndView = new ModelAndView(resolveErrorView(status));
        modelAndView.setStatus(resolveHttpStatus(status));
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("message", message);
        return modelAndView;
    }

    private boolean expectsJson(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader(HttpHeaders.ACCEPT);

        return uri.startsWith("/api/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));
    }

    private String resolveMessage(HttpStatusCode status, String reason) {
        if (reason != null && !reason.isBlank()) {
            return reason;
        }

        HttpStatus httpStatus = resolveHttpStatus(status);
        if (httpStatus != null) {
            return httpStatus.getReasonPhrase();
        }

        return "Ошибка запроса";
    }

    private String resolveErrorView(HttpStatusCode status) {
        if (status.value() == HttpStatus.FORBIDDEN.value()) {
            return "error/403";
        }

        return status.value() == HttpStatus.NOT_FOUND.value() ? "error/404" : "error/500";
    }

    private HttpStatus resolveHttpStatus(HttpStatusCode status) {
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        return httpStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : httpStatus;
    }

    private void logByStatus(HttpStatusCode status, HttpServletRequest request, Exception exception) {
        if (status.is5xxServerError()) {
            log.error("Server error for {} {}", request.getMethod(), request.getRequestURI(), exception);
            return;
        }

        log.warn("Request failed with status {} for {} {}: {}", status.value(), request.getMethod(), request.getRequestURI(), exception.getMessage());
    }
}
