package com.gamehub.api.dto;

import com.gamehub.api.model.Categoria;

public record CategoriaResponse(Long id, String nombre, String descripcion, Boolean estado) {

    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getEstado());
    }
}
