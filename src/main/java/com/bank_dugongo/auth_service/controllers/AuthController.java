package com.bank_dugongo.auth_service.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank_dugongo.auth_service.dto.AuthResponseDTO;
import com.bank_dugongo.auth_service.dto.LoginRequestDTO;
import com.bank_dugongo.auth_service.dto.PatchUserRequestDTO;
import com.bank_dugongo.auth_service.dto.RegisterRequestDTO;
import com.bank_dugongo.auth_service.dto.UserInfoDTO;
import com.bank_dugongo.auth_service.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping
    public ResponseEntity<AuthResponseDTO> register(
        @Valid @RequestBody RegisterRequestDTO request
    ) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
        @Valid @RequestBody LoginRequestDTO request
    ) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        boolean isValid = authService.validateToken(token);

        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> getUserInfo(
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        UserInfoDTO userInfo = authService.getUserInfo(token);
        return ResponseEntity.ok(userInfo);
    }

    @PatchMapping
    public ResponseEntity<UserInfoDTO> patchUser(
        @RequestHeader("Authorization") String authHeader,
        @Valid @RequestBody PatchUserRequestDTO request
    ) {
        String token = authHeader.substring(7);
        UserInfoDTO updatedUser = authService.patchUser(token, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping
    public ResponseEntity<Void> softDeleteUser(
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        authService.softDeleteUser(token);
        return ResponseEntity.noContent().build();
    }
}