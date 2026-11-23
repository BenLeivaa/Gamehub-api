package com.gamehub.api.service;

import com.gamehub.api.dto.LoginRequest;
import com.gamehub.api.dto.LoginResponse;
import com.gamehub.api.dto.RegisterRequest;
import com.gamehub.api.dto.UsuarioResponse;
import com.gamehub.api.exception.BusinessException;
import com.gamehub.api.model.Rol;
import com.gamehub.api.model.Usuario;
import com.gamehub.api.repository.UsuarioRepository;
import com.gamehub.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Registro público: siempre crea un CLIENTE. */
    public UsuarioResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe una cuenta con el email '" + request.email() + "'");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .telefono(request.telefono())
                .rol(Rol.CLIENTE)
                .estado(true)
                .build();
        if (request.direccion() != null) {
            usuario.setComuna(request.direccion().comuna());
            usuario.setCiudad(request.direccion().ciudad());
            usuario.setCalle(request.direccion().calle());
            usuario.setNumero(request.direccion().numero());
        }

        usuario = usuarioRepository.save(usuario);
        log.info("Cliente registrado: {}", usuario.getEmail());
        return UsuarioResponse.from(usuario);
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        if (Boolean.FALSE.equals(usuario.getEstado())) {
            throw new DisabledException("La cuenta está inactiva");
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol().name());
        log.info("Login exitoso: {}", usuario.getEmail());

        return new LoginResponse(token, "Bearer", usuario.getId(), usuario.getNombre(),
                usuario.getEmail(), usuario.getRol());
    }
}
