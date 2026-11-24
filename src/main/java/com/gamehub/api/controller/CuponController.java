package com.gamehub.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cupones")
@Tag(name = "Cupones", description = "Validación pública de códigos de descuento")
public class CuponController {

    private record Cupon(String codigo, int porcentaje, int tope, LocalDate desde, LocalDate hasta) {}

    // Mismos cupones que usaba el frontend en utils/coupons.js
    private static final List<Cupon> CUPONES = List.of(
            new Cupon("GAMER10", 10, 50000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
    );

    @Operation(summary = "Validar un cupón",
            description = "Público. Respuestas: 200 cupón válido; 404 no existe; 400 vencido o fuera de vigencia.")
    @GetMapping("/validar")
    public ResponseEntity<Map<String, Object>> validar(@RequestParam String codigo) {
        Cupon cupon = CUPONES.stream()
                .filter(c -> c.codigo().equalsIgnoreCase(codigo.trim()))
                .findFirst()
                .orElse(null);

        if (cupon == null) {
            return ResponseEntity.status(404).body(respuestaError("Ese código de cupón no existe."));
        }

        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(cupon.desde()) || hoy.isAfter(cupon.hasta())) {
            return ResponseEntity.badRequest().body(respuestaError("Ese cupón está vencido o aún no está vigente."));
        }

        // Se incluyen nombres en español e inglés para ser compatible con el frontend
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("valid", true);
        body.put("codigo", cupon.codigo());
        body.put("code", cupon.codigo());
        body.put("porcentaje", cupon.porcentaje());
        body.put("percentage", cupon.porcentaje());
        body.put("tope", cupon.tope());
        body.put("cap", cupon.tope());
        return ResponseEntity.ok(body);
    }

    private Map<String, Object> respuestaError(String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("valid", false);
        body.put("error", mensaje);
        body.put("message", mensaje);
        return body;
    }
}
