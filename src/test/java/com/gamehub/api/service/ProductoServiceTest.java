package com.gamehub.api.service;

import com.gamehub.api.dto.ProductoRequest;
import com.gamehub.api.dto.ProductoResponse;
import com.gamehub.api.exception.BusinessException;
import com.gamehub.api.exception.ResourceNotFoundException;
import com.gamehub.api.model.Categoria;
import com.gamehub.api.model.Producto;
import com.gamehub.api.repository.CategoriaRepository;
import com.gamehub.api.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nombre("Consolas").estado(true).build();
    }

    private ProductoRequest request(Long categoriaId) {
        return new ProductoRequest("PS5", "Sony", "Slim", 549990.0, 10, categoriaId, "desc", null, null);
    }

    @Test
    void crear_guardaElProductoActivoPorDefecto() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse respuesta = productoService.crear(request(1L));

        assertEquals("PS5", respuesta.nombre());
        assertTrue(respuesta.estado());
        assertEquals("Consolas", respuesta.categoriaNombre());
    }

    @Test
    void crear_categoriaInexistente_lanzaResourceNotFound() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        ProductoRequest request = request(99L);

        assertThrows(ResourceNotFoundException.class, () -> productoService.crear(request));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void listar_clienteNoVeProductosInactivos() {
        Producto activo = Producto.builder().id(1L).nombre("A").precio(1.0).stock(1).estado(true).categoria(categoria).build();
        Producto inactivo = Producto.builder().id(2L).nombre("B").precio(1.0).stock(1).estado(false).categoria(categoria).build();
        when(productoRepository.findAll()).thenReturn(List.of(activo, inactivo));

        assertEquals(1, productoService.listar(null, null, false).size());
        assertEquals(2, productoService.listar(null, null, true).size());
    }

    @Test
    void desactivar_productoYaInactivo_lanzaBusinessException() {
        Producto inactivo = Producto.builder().id(2L).nombre("B").precio(1.0).stock(1).estado(false).categoria(categoria).build();
        when(productoRepository.findById(2L)).thenReturn(Optional.of(inactivo));

        assertThrows(BusinessException.class, () -> productoService.desactivar(2L));
    }

    @Test
    void obtener_productoInexistente_lanzaResourceNotFound() {
        when(productoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productoService.obtener(5L));
    }
}
