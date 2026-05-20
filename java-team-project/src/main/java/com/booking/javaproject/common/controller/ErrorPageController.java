package com.booking.javaproject.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorPageController {

    @RequestMapping("/error/403")
    public ModelAndView forbidden() {
        ModelAndView modelAndView = new ModelAndView("error/403");
        modelAndView.setStatus(HttpStatus.FORBIDDEN);
        modelAndView.addObject("status", HttpStatus.FORBIDDEN.value());
        modelAndView.addObject("message", "Доступ запрещен");
        return modelAndView;
    }
}
