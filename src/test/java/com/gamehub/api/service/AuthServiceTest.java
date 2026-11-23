package com.gamehub.api.service;

import com.gamehub.api.dto.LoginRequest;
import com.gamehub.api.dto.LoginResponse;
import com.gamehub.api.dto.RegisterRequest;
import com.gamehub.api.exception.BusinessException;
import com.gamehub.api.model.Rol;
import com.gamehub.api.model.Usuario;
import com.gamehub.api.repository.UsuarioRepository;
import com.gamehub.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario admin() {
        return Usuario.builder().id(1L).nombre("Admin").email("admin@gamehub.cl")
                .passwordHash("hash").rol(Rol.ADMINISTRADOR).estado(true).build();
    }

    @Test
    void login_correcto_devuelveTokenYRol() {
        when(usuarioRepository.findByEmail("admin@gamehub.cl")).thenReturn(Optional.of(admin()));
        when(passwordEncoder.matches("Admin1234", "hash")).thenReturn(true);
        when(jwtService.generarToken("admin@gamehub.cl", "ADMINISTRADOR")).thenReturn("token-falso");

        LoginResponse respuesta = authService.login(new LoginRequest("admin@gamehub.cl", "Admin1234"));

        assertEquals("token-falso", respuesta.token());
        assertEquals(Rol.ADMINISTRADOR, respuesta.rol());
    }

    @Test
    void login_claveIncorrecta_lanzaBadCredentials() {
        when(usuarioRepository.findByEmail("admin@gamehub.cl")).thenReturn(Optional.of(admin()));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("admin@gamehub.cl", "mala")));
    }

    @Test
    void login_usuarioInexistente_lanzaBadCredentials() {
        when(usuarioRepository.findByEmail("nadie@gamehub.cl")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("nadie@gamehub.cl", "Clave1234")));
    }

    @Test
    void login_cuentaInactiva_lanzaDisabled() {
        Usuario inactivo = admin();
        inactivo.setEstado(false);
        when(usuarioRepository.findByEmail("admin@gamehub.cl")).thenReturn(Optional.of(inactivo));
        when(passwordEncoder.matches("Admin1234", "hash")).thenReturn(true);

        assertThrows(DisabledException.class,
                () -> authService.login(new LoginRequest("admin@gamehub.cl", "Admin1234")));
    }

    @Test
    void register_siempreCreaUnCliente() {
        when(usuarioRepository.existsByEmail("nuevo@gamehub.cl")).thenReturn(false);
        when(passwordEncoder.encode("Clave1234")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(new RegisterRequest("Nuevo", "nuevo@gamehub.cl", "Clave1234", null, null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(Rol.CLIENTE, captor.getValue().getRol());
        assertEquals("hash-nuevo", captor.getValue().getPasswordHash());
    }

    @Test
    void register_emailDuplicado_lanzaBusinessException() {
        when(usuarioRepository.existsByEmail("admin@gamehub.cl")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("Otro", "admin@gamehub.cl", "Clave1234", null, null);

        assertThrows(BusinessException.class, () -> authService.register(request));
        verify(usuarioRepository, never()).save(any());
    }
}
