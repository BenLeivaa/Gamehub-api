package com.gamehub.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BoletaRequest(
        @NotEmpty(message = "La compra debe tener al menos un producto")
        @Valid
        List<ItemBoletaRequest> items,
        String codigoCupon) {
    public BoletaRequest(List<ItemBoletaRequest> items) { this(items, null); }
}
