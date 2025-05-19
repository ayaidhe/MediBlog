package com.example.MediBlog.controller;


import ch.qos.logback.classic.Logger;
import com.example.MediBlog.model.User;
import com.example.MediBlog.repository.UserRepository;
import com.example.MediBlog.services.CustomUserDetailsService;
import java.nio.file.Path;

import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    @Getter
    private final CustomUserDetailsService customUserDetailsService;

    public UserController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                          CustomUserDetailsService customUserDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("user") User user,
                                  BindingResult result,
                                  Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("emailError", "Cet email est déjà utilisé.");
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return "redirect:/login";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam("image") MultipartFile image,
                                Model model) {
        User user = CustomUserDetailsService.findByEmail(userDetails.getUsername());

        if (image != null && !image.isEmpty()) {
            try {
                // Chemin du dossier de destination
                String uploadsDir = "src/main/resources/static/uploads/";
                String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path path = Paths.get(uploadsDir + filename);
                Files.createDirectories(path.getParent());
                Files.write(path, image.getBytes());

                assert user != null;
                user.setProfilePicture("/uploads/" + filename);
                userRepository.save(user);

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Erreur lors de l'upload de l'image.");
            }
        }

        return "redirect:/profile";
    }
    @GetMapping("/profile")
    public String userProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = customUserDetailsService.findByEmail(userDetails.getUsername());

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile"; // Le template profile.html
    }


}
