package com.gamehub.api.controller;

import com.gamehub.api.dto.BoletaRequest;
import com.gamehub.api.dto.BoletaResponse;
import com.gamehub.api.security.AuthUtil;
import com.gamehub.api.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@RequiredArgsConstructor
@Tag(name = "Boletas (órdenes)", description = "Compras de la tienda")
@SecurityRequirement(name = "bearerAuth")
public class BoletaController {

    private final BoletaService boletaService;

    @Operation(summary = "Crear una compra",
            description = "Valida stock, calcula el total y descuenta el inventario. Respuestas: 201, 400, 401, 403, 404.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoletaResponse crear(@Valid @RequestBody BoletaRequest request, Authentication auth) {
        return boletaService.crear(auth.getName(), request);
    }

    @Operation(summary = "Listar todas las boletas", description = "ADMINISTRADOR y VENDEDOR. Respuestas: 200, 401, 403.")
    @GetMapping
    public List<BoletaResponse> listar() {
        return boletaService.listar();
    }

    @Operation(summary = "Listar mis compras", description = "Boletas del usuario autenticado. Respuestas: 200, 401, 403.")
    @GetMapping("/mis-compras")
    public List<BoletaResponse> misCompras(Authentication auth) {
        return boletaService.misCompras(auth.getName());
    }

    @Operation(summary = "Detalle de una boleta",
            description = "Admin y vendedor ven cualquiera; un cliente solo las suyas. Respuestas: 200, 401, 403, 404.")
    @GetMapping("/{id}")
    public BoletaResponse obtener(@PathVariable Long id, Authentication auth) {
        return boletaService.obtener(id, auth.getName(), AuthUtil.esPersonal(auth));
    }

    @Operation(summary = "Cancelar una boleta", description = "Devuelve el stock. Solo ADMINISTRADOR. Respuestas: 200, 400, 401, 403, 404.")
    @PatchMapping("/{id}/cancelar")
    public BoletaResponse cancelar(@PathVariable Long id) {
        return boletaService.cancelar(id);
    }

    @Operation(summary = "Marcar una boleta como entregada", description = "Solo ADMINISTRADOR o VENDEDOR.")
    @PostMapping("/{id}/entregar")
    public BoletaResponse marcarEntregada(@PathVariable Long id) {
        return boletaService.marcarEntregada(id);
    }
}
