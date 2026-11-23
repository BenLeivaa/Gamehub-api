package com.gamehub.api.dto;

import com.gamehub.api.model.DetalleBoleta;

public record DetalleBoletaResponse(Long id, Long productoId, String productoNombre, Integer cantidad,
                                    Double precioUnitario, Double subtotal) {

    public static DetalleBoletaResponse from(DetalleBoleta d) {
        return new DetalleBoletaResponse(
                d.getId(), d.getProducto().getId(), d.getProducto().getNombre(),
                d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
    }
}
