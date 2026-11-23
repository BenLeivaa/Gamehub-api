package com.gamehub.api.dto;

import com.gamehub.api.model.Rol;

public record LoginResponse(String token, String tipo, Long id, String nombre, String email, Rol rol) {
}
