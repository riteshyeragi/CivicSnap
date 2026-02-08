package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.example.dto.AuthResponse;
import com.example.service.SupabaseService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserAuthController {

    private final SupabaseService supabaseService;

    public UserAuthController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            SupabaseService.SupabaseAuthResponse result = supabaseService.signUp(email, password);
            return ResponseEntity.ok(new AuthResponse(result.getAccessToken(), result.getUserId(), result.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");	
        if (email == null || password == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            SupabaseService.SupabaseAuthResponse result = supabaseService.signIn(email, password);
            return ResponseEntity.ok(new AuthResponse(result.getAccessToken(), result.getUserId(), result.getEmail()));
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new AuthResponse(null, null, "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            supabaseService.recoverPassword(email);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
