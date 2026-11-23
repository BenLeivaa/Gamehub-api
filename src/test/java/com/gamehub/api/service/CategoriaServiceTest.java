package com.gamehub.api.service;

import com.gamehub.api.dto.CategoriaRequest;
import com.gamehub.api.exception.BusinessException;
import com.gamehub.api.model.Categoria;
import com.gamehub.api.repository.CategoriaRepository;
import com.gamehub.api.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void crear_nombreDuplicado_lanzaBusinessException() {
        when(categoriaRepository.existsByNombreIgnoreCase("Consolas")).thenReturn(true);

        CategoriaRequest request = new CategoriaRequest("Consolas", null, null);

        assertThrows(BusinessException.class, () -> categoriaService.crear(request));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void desactivar_conProductosActivos_lanzaBusinessException() {
        Categoria categoria = Categoria.builder().id(1L).nombre("Consolas").estado(true).build();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoriaIdAndEstadoTrue(1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> categoriaService.desactivar(1L));
        assertTrue(categoria.getEstado());
    }

    @Test
    void desactivar_sinProductosActivos_laDesactiva() {
        Categoria categoria = Categoria.builder().id(1L).nombre("Consolas").estado(true).build();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.existsByCategoriaIdAndEstadoTrue(1L)).thenReturn(false);

        categoriaService.desactivar(1L);

        assertFalse(categoria.getEstado());
        verify(categoriaRepository).save(categoria);
    }
}
