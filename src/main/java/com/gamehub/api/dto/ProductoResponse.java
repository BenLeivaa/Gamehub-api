package com.gamehub.api.dto;

import com.gamehub.api.model.Producto;

public record ProductoResponse(Long id, String nombre, String marca, String modelo, Double precio,
                               Integer stock, Long categoriaId, String categoriaNombre,
                               String descripcion, String imagen, Boolean estado) {

    public static ProductoResponse from(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getMarca(), p.getModelo(), p.getPrecio(), p.getStock(),
                p.getCategoria().getId(), p.getCategoria().getNombre(),
                p.getDescripcion(), p.getImagen(), p.getEstado());
    }
}
