package com.example.MediBlog.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login"; // template Thymeleaf src/main/resources/templates/auth/login.html
    }
}
