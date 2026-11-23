package com.gamehub.api.controller;

import com.gamehub.api.dto.CategoriaRequest;
import com.gamehub.api.dto.CategoriaResponse;
import com.gamehub.api.security.AuthUtil;
import com.gamehub.api.service.CategoriaService;
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
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Lectura pública; crear/editar/desactivar solo ADMINISTRADOR")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Listar categorías",
            description = "Público. Visitantes y clientes ven solo las activas; admin y vendedor ven todas. Respuestas: 200.")
    @GetMapping
    public List<CategoriaResponse> listar(Authentication auth) {
        return categoriaService.listar(AuthUtil.esPersonal(auth));
    }

    @Operation(summary = "Obtener una categoría", description = "Público. Respuestas: 200, 404.")
    @GetMapping("/{id}")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return categoriaService.obtener(id);
    }

    @Operation(summary = "Crear una categoría", description = "Respuestas: 201, 400, 401, 403.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest request) {
        return categoriaService.crear(request);
    }

    @Operation(summary = "Actualizar una categoría", description = "Respuestas: 200, 400, 401, 403, 404.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return categoriaService.actualizar(id, request);
    }

    @Operation(summary = "Desactivar una categoría (baja lógica)", description = "Respuestas: 204, 400, 401, 403, 404.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        categoriaService.desactivar(id);
    }
}
