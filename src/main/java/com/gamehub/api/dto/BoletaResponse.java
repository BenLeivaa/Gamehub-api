package com.gamehub.api.dto;

import com.gamehub.api.model.Boleta;
import com.gamehub.api.model.EstadoBoleta;

import java.time.LocalDateTime;
import java.util.List;

public record BoletaResponse(Long id, LocalDateTime fecha, Double total, EstadoBoleta estado,
                             Long usuarioId, String usuarioNombre, String usuarioEmail,
                             List<DetalleBoletaResponse> detalles) {

    public static BoletaResponse from(Boleta b) {
        return new BoletaResponse(
                b.getId(), b.getFecha(), b.getTotal(), b.getEstado(),
                b.getUsuario().getId(), b.getUsuario().getNombre(), b.getUsuario().getEmail(),
                b.getDetalles().stream().map(DetalleBoletaResponse::from).toList());
    }
}
