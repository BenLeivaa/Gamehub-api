package com.gamehub.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoría no puede estar vacío")
        String nombre,
        String descripcion,
        Boolean estado) {
}
