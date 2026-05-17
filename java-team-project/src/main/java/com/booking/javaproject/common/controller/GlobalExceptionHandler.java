package com.booking.javaproject.common.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@ControllerAdvice
public class GlobalExceptionHandler implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public Object handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() == null ? status.getReasonPhrase() : exception.getReason();

        if (isApiRequest(request)) {
            return buildJsonError(status, message, request.getRequestURI());
        }

        return buildErrorView(status, message, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled request error: {}", request.getRequestURI(), exception);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal server error";

        if (isApiRequest(request)) {
            return buildJsonError(status, message, request.getRequestURI());
        }

        return buildErrorView(status, message, request.getRequestURI());
    }

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request) {
        HttpStatus status = resolveErrorStatus(request);
        String path = resolveErrorPath(request);
        String message = status == HttpStatus.NOT_FOUND ? "Page not found" : status.getReasonPhrase();

        if (isApiPath(path)) {
            return buildJsonError(status, message, path);
        }

        return buildErrorView(status, message, path);
    }

    private HttpStatus resolveErrorStatus(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode instanceof Integer code) {
            HttpStatus status = HttpStatus.resolve(code);
            return status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveErrorPath(HttpServletRequest request) {
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (path == null) {
            return request.getRequestURI();
        }
        return path.toString();
    }

    private ResponseEntity<Map<String, Object>> buildJsonError(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", path
        ));
    }

    private ModelAndView buildErrorView(HttpStatus status, String message, String path) {
        ModelAndView modelAndView = new ModelAndView(status == HttpStatus.NOT_FOUND ? "error/404" : "error/500");
        modelAndView.setStatus(status);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("error", status.getReasonPhrase());
        modelAndView.addObject("message", message);
        modelAndView.addObject("path", path);
        return modelAndView;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return isApiPath(request.getRequestURI());
    }

    private boolean isApiPath(String path) {
        return path != null && path.startsWith("/api/");
    }
}
