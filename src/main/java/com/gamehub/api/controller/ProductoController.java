package com.gamehub.api.controller;

import com.gamehub.api.dto.ProductoRequest;
import com.gamehub.api.dto.ProductoResponse;
import com.gamehub.api.security.AuthUtil;
import com.gamehub.api.service.ProductoService;
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
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Lectura pública; crear/editar/desactivar solo ADMINISTRADOR")
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Listar productos",
            description = "Público. Filtros opcionales: categoriaId y marca. Visitantes y clientes ven solo los activos; "
                    + "admin y vendedor ven todos. Respuestas: 200.")
    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(required = false) Long categoriaId,
                                         @RequestParam(required = false) String marca,
                                         Authentication auth) {
        return productoService.listar(categoriaId, marca, AuthUtil.esPersonal(auth));
    }

    @Operation(summary = "Obtener el detalle de un producto", description = "Público. Respuestas: 200, 404.")
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @Operation(summary = "Crear un producto", description = "Respuestas: 201, 400, 401, 403, 404 (categoría inexistente).")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        return productoService.crear(request);
    }

    @Operation(summary = "Actualizar un producto", description = "Respuestas: 200, 400, 401, 403, 404.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @Operation(summary = "Desactivar un producto (baja lógica)", description = "Respuestas: 204, 400, 401, 403, 404.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        productoService.desactivar(id);
    }
}
