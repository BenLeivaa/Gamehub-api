package com.gamehub.api.service;

import com.gamehub.api.dto.DireccionDto;
import com.gamehub.api.dto.UsuarioRequest;
import com.gamehub.api.dto.UsuarioResponse;
import com.gamehub.api.dto.UsuarioUpdateRequest;
import com.gamehub.api.exception.BusinessException;
import com.gamehub.api.exception.ResourceNotFoundException;
import com.gamehub.api.model.Rol;
import com.gamehub.api.model.Usuario;
import com.gamehub.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con el email '" + request.email() + "'");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .telefono(request.telefono())
                .rol(request.rol())
                .estado(true)
                .build();
        aplicarDireccion(usuario, request.direccion());

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario creado: {} ({})", usuario.getEmail(), usuario.getRol());
        return UsuarioResponse.from(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Rol rol) {
        List<Usuario> usuarios = (rol != null) ? usuarioRepository.findByRol(rol) : usuarioRepository.findAll();
        return usuarios.stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.from(buscar(id));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscar(id);

        if (request.nombre() != null && !request.nombre().isBlank()) {
            usuario.setNombre(request.nombre());
        }
        if (request.telefono() != null) {
            usuario.setTelefono(request.telefono());
        }
        if (request.rol() != null) {
            usuario.setRol(request.rol());
        }
        if (request.estado() != null) {
            usuario.setEstado(request.estado());
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        aplicarDireccion(usuario, request.direccion());

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario actualizado: {} (id={})", usuario.getEmail(), usuario.getId());
        return UsuarioResponse.from(usuario);
    }

    /** Baja lógica: el usuario queda inactivo (no se borra porque puede tener boletas). */
    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = buscar(id);
        if (Boolean.FALSE.equals(usuario.getEstado())) {
            throw new BusinessException("El usuario con id=" + id + " ya está inactivo");
        }
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
        log.warn("Usuario desactivado: {} (id={})", usuario.getEmail(), id);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id=" + id));
    }

    private void aplicarDireccion(Usuario usuario, DireccionDto dir) {
        if (dir == null) {
            return;
        }
        usuario.setComuna(dir.comuna());
        usuario.setCiudad(dir.ciudad());
        usuario.setCalle(dir.calle());
        usuario.setNumero(dir.numero());
    }
}
