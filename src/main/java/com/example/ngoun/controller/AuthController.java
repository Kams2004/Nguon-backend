package com.example.ngoun.controller;

import com.example.ngoun.config.JwtUtil;
import com.example.ngoun.model.User;
import com.example.ngoun.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        return userService.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .filter(User::getActive)
                .<ResponseEntity<?>>map(user -> {
                    String token = jwtUtil.generateToken(username);
                    Map<String, String> response = new HashMap<>();
                    response.put("token", token);
                    response.put("username", username);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }
}
