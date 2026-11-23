package com.gamehub.api.dto;

import com.gamehub.api.model.Rol;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/** Todos los campos son opcionales: solo se actualizan los que vienen. */
public record UsuarioUpdateRequest(
        String nombre,
        String telefono,
        Rol rol,
        Boolean estado,

        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @Valid
        DireccionDto direccion) {
}
