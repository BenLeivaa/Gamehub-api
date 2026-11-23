package com.gamehub.api.dto;

import com.gamehub.api.model.Rol;
import com.gamehub.api.model.Usuario;

public record UsuarioResponse(Long id, String nombre, String email, String telefono, Rol rol,
                              Boolean estado, DireccionDto direccion) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNombre(), u.getEmail(), u.getTelefono(), u.getRol(), u.getEstado(),
                new DireccionDto(u.getComuna(), u.getCiudad(), u.getCalle(), u.getNumero()));
    }
}
