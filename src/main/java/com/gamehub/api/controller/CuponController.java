package com.gamehub.api.controller;

import com.gamehub.api.dto.CuponRequest;
import com.gamehub.api.exception.BusinessException;
import com.gamehub.api.exception.ResourceNotFoundException;
import com.gamehub.api.model.Cupon;
import com.gamehub.api.repository.CuponRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** Administración y validación de cupones almacenados en la base de datos. */
@RestController
@RequestMapping("/api/v1/cupones")
@RequiredArgsConstructor
@Tag(name = "Cupones", description = "Cupones de descuento")
public class CuponController {

    private final CuponRepository cuponRepository;

    @Operation(summary = "Lista todos los cupones", description = "Solo administrador")
    @GetMapping
    public List<Cupon> listar() {
        return cuponRepository.findAll();
    }

    @Operation(summary = "Crea un cupón", description = "Solo administrador")
    @PostMapping
    public ResponseEntity<Cupon> crear(@Valid @RequestBody CuponRequest request) {
        String codigo = request.codigo().trim().toUpperCase();
        if (cuponRepository.findByCodigoIgnoreCase(codigo).isPresent()) {
            throw new BusinessException("Ya existe un cupón con ese código");
        }

        Cupon cupon = Cupon.builder()
                .codigo(codigo)
                .porcentaje(request.porcentaje())
                .tope(request.tope())
                .activo(request.activo() == null || request.activo())
                .expiraEn(request.expiraEn())
                .build();
        return ResponseEntity.status(201).body(cuponRepository.save(cupon));
    }

    @Operation(summary = "Valida un cupón", description = "Público. El total definitivo se calcula al crear la compra.")
    @GetMapping("/validar")
    public Cupon validar(@RequestParam String codigo) {
        Cupon cupon = cuponRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Ese código de cupón no existe"));
        if (!Boolean.TRUE.equals(cupon.getActivo()) || !cupon.getExpiraEn().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Ese cupón está vencido o inactivo");
        }
        return cupon;
    }

    @Operation(summary = "Activa o desactiva un cupón", description = "Solo administrador")
    @PatchMapping("/{id}/estado")
    public Cupon cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        Cupon cupon = buscar(id);
        cupon.setActivo(activo);
        return cuponRepository.save(cupon);
    }

    /** La interfaz llama a esta ruta como eliminar, pero se conserva el historial desactivándolo. */
    @Operation(summary = "Desactiva un cupón", description = "Solo administrador; no elimina registros históricos")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        Cupon cupon = buscar(id);
        cupon.setActivo(false);
        cuponRepository.save(cupon);
        return ResponseEntity.noContent().build();
    }

    private Cupon buscar(Long id) {
        return cuponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado con id=" + id));
    }
}
