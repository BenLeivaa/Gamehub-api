package com.gamehub.api.controller;

import com.gamehub.api.dto.LoginRequest;
import com.gamehub.api.dto.LoginResponse;
import com.gamehub.api.dto.RegisterRequest;
import com.gamehub.api.dto.UsuarioResponse;
import com.gamehub.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro e inicio de sesión (JWT)")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar un cliente", description = "Crea una cuenta con rol CLIENTE. Respuestas: 201, 400.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Iniciar sesión", description = "Devuelve el token JWT. Respuestas: 200, 400, 401.")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
