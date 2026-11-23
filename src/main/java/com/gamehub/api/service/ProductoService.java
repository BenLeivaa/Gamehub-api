package com.gamehub.api.service;

import com.gamehub.api.dto.ProductoRequest;
import com.gamehub.api.dto.ProductoResponse;
import com.gamehub.api.exception.BusinessException;
import com.gamehub.api.exception.ResourceNotFoundException;
import com.gamehub.api.model.Categoria;
import com.gamehub.api.model.Producto;
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
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Categoria categoria = buscarCategoria(request.categoriaId());

        Producto producto = Producto.builder()
                .nombre(request.nombre())
                .marca(request.marca())
                .modelo(request.modelo())
                .precio(request.precio())
                .stock(request.stock())
                .descripcion(request.descripcion())
                .imagen(request.imagen())
                .estado(request.estado() != null ? request.estado() : true)
                .categoria(categoria)
                .build();

        producto = productoRepository.save(producto);
        log.info("Producto creado: {} (id={})", producto.getNombre(), producto.getId());
        return ProductoResponse.from(producto);
    }

    /** Si incluirInactivos es false (clientes y visitantes), solo devuelve los productos activos. */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId, String marca, boolean incluirInactivos) {
        List<Producto> productos = (categoriaId != null)
                ? productoRepository.findByCategoriaId(categoriaId)
                : productoRepository.findAll();

        return productos.stream()
                .filter(p -> incluirInactivos || Boolean.TRUE.equals(p.getEstado()))
                .filter(p -> marca == null || marca.isBlank() || marca.equalsIgnoreCase(p.getMarca()))
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.from(buscar(id));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscar(id);
        Categoria categoria = buscarCategoria(request.categoriaId());

        producto.setNombre(request.nombre());
        producto.setMarca(request.marca());
        producto.setModelo(request.modelo());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setDescripcion(request.descripcion());
        producto.setImagen(request.imagen());
        producto.setCategoria(categoria);
        if (request.estado() != null) {
            producto.setEstado(request.estado());
        }

        producto = productoRepository.save(producto);
        log.info("Producto actualizado: {} (id={})", producto.getNombre(), id);
        return ProductoResponse.from(producto);
    }

    /** Baja lógica: no se borra porque puede estar en boletas ya emitidas. */
    @Transactional
    public void desactivar(Long id) {
        Producto producto = buscar(id);
        if (Boolean.FALSE.equals(producto.getEstado())) {
            throw new BusinessException("El producto con id=" + id + " ya está desactivado");
        }
        producto.setEstado(false);
        productoRepository.save(producto);
        log.info("Producto desactivado: {} (id={})", producto.getNombre(), id);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id=" + id));
    }

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id=" + categoriaId));
    }
}
