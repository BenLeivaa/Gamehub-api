package com.gamehub.api.service;

import com.gamehub.api.dto.CategoriaRequest;
import com.gamehub.api.dto.CategoriaResponse;
import com.gamehub.api.exception.BusinessException;
import com.gamehub.api.exception.ResourceNotFoundException;
import com.gamehub.api.model.Categoria;
import com.gamehub.api.repository.CategoriaRepository;
import com.gamehub.api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new BusinessException("Ya existe una categoría con el nombre '" + request.nombre() + "'");
        }
        Categoria categoria = Categoria.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .estado(request.estado() != null ? request.estado() : true)
                .build();
        categoria = categoriaRepository.save(categoria);
        log.info("Categoría creada: {}", categoria.getNombre());
        return CategoriaResponse.from(categoria);
    }

    /** Si incluirInactivas es false (clientes y visitantes), solo devuelve las activas. */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(boolean incluirInactivas) {
        List<Categoria> categorias = incluirInactivas
                ? categoriaRepository.findAll()
                : categoriaRepository.findByEstado(true);
        return categorias.stream().map(CategoriaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return CategoriaResponse.from(buscar(id));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscar(id);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id)) {
            throw new BusinessException("Ya existe una categoría con el nombre '" + request.nombre() + "'");
        }
        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());
        if (request.estado() != null) {
            categoria.setEstado(request.estado());
        }
        categoria = categoriaRepository.save(categoria);
        log.info("Categoría actualizada: {} (id={})", categoria.getNombre(), id);
        return CategoriaResponse.from(categoria);
    }

    /** Baja lógica. No se puede desactivar una categoría que aún tiene productos activos. */
    @Transactional
    public void desactivar(Long id) {
        Categoria categoria = buscar(id);
        if (Boolean.FALSE.equals(categoria.getEstado())) {
            throw new BusinessException("La categoría ya está desactivada");
        }
        if (productoRepository.existsByCategoriaIdAndEstadoTrue(id)) {
            throw new BusinessException("No se puede desactivar: la categoría tiene productos activos");
        }
        categoria.setEstado(false);
        categoriaRepository.save(categoria);
        log.info("Categoría desactivada: {} (id={})", categoria.getNombre(), id);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id=" + id));
    }
}
