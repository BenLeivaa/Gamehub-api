package com.gamehub.api.service;

import com.gamehub.api.dto.BoletaRequest;
import com.gamehub.api.dto.BoletaResponse;
import com.gamehub.api.dto.ItemBoletaRequest;
import com.gamehub.api.exception.BusinessException;
import com.gamehub.api.exception.ResourceNotFoundException;
import com.gamehub.api.model.*;
import com.gamehub.api.repository.BoletaRepository;
import com.gamehub.api.repository.ProductoRepository;
import com.gamehub.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private BoletaService boletaService;

    private Usuario cliente;
    private Producto consola;

    @BeforeEach
    void setUp() {
        cliente = Usuario.builder().id(1L).nombre("Cliente").email("cliente@gamehub.cl")
                .rol(Rol.CLIENTE).estado(true).build();
        Categoria categoria = Categoria.builder().id(1L).nombre("Consolas").estado(true).build();
        consola = Producto.builder().id(10L).nombre("PS5").precio(500000.0).stock(5)
                .estado(true).categoria(categoria).build();
    }

    @Test
    void crear_calculaTotalYDescuentaStock() {
        when(usuarioRepository.findByEmail("cliente@gamehub.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(consola));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletaResponse respuesta = boletaService.crear("cliente@gamehub.cl",
                new BoletaRequest(List.of(new ItemBoletaRequest(10L, 2))));

        assertEquals(1_000_000.0, respuesta.total());
        assertEquals(EstadoBoleta.PENDIENTE, respuesta.estado());
        assertEquals(1, respuesta.detalles().size());
        assertEquals(3, consola.getStock()); // 5 - 2
    }

    @Test
    void crear_stockInsuficiente_lanzaBusinessException() {
        when(usuarioRepository.findByEmail("cliente@gamehub.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(consola));

        BoletaRequest request = new BoletaRequest(List.of(new ItemBoletaRequest(10L, 99)));

        assertThrows(BusinessException.class, () -> boletaService.crear("cliente@gamehub.cl", request));
        assertEquals(5, consola.getStock()); // el stock no cambió
        verify(boletaRepository, never()).save(any());
    }

    @Test
    void crear_productoInactivo_lanzaBusinessException() {
        consola.setEstado(false);
        when(usuarioRepository.findByEmail("cliente@gamehub.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(consola));

        BoletaRequest request = new BoletaRequest(List.of(new ItemBoletaRequest(10L, 1)));

        assertThrows(BusinessException.class, () -> boletaService.crear("cliente@gamehub.cl", request));
    }

    @Test
    void crear_productoInexistente_lanzaResourceNotFound() {
        when(usuarioRepository.findByEmail("cliente@gamehub.cl")).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        BoletaRequest request = new BoletaRequest(List.of(new ItemBoletaRequest(99L, 1)));

        assertThrows(ResourceNotFoundException.class, () -> boletaService.crear("cliente@gamehub.cl", request));
    }

    @Test
    void cancelar_devuelveElStock() {
        Boleta boleta = boletaConUnDetalle(EstadoBoleta.PENDIENTE, 2);
        consola.setStock(3);
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletaResponse respuesta = boletaService.cancelar(1L);

        assertEquals(EstadoBoleta.CANCELADA, respuesta.estado());
        assertEquals(5, consola.getStock()); // 3 + 2
    }

    @Test
    void cancelar_boletaYaCancelada_lanzaBusinessException() {
        Boleta boleta = boletaConUnDetalle(EstadoBoleta.CANCELADA, 2);
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));

        assertThrows(BusinessException.class, () -> boletaService.cancelar(1L));
    }

    @Test
    void marcarEntregada_actualizaEstado() {
        Boleta boleta = boletaConUnDetalle(EstadoBoleta.PENDIENTE, 1);
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        BoletaResponse respuesta = boletaService.marcarEntregada(1L);

        assertEquals(EstadoBoleta.ENTREGADA, respuesta.estado());
    }

    @Test
    void marcarEntregada_canceladaLanzaBusinessException() {
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boletaConUnDetalle(EstadoBoleta.CANCELADA, 1)));
        assertThrows(BusinessException.class, () -> boletaService.marcarEntregada(1L));
    }

    @Test
    void obtener_clienteNoPuedeVerBoletaAjena() {
        Boleta boleta = boletaConUnDetalle(EstadoBoleta.PENDIENTE, 1);
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));

        assertThrows(AccessDeniedException.class,
                () -> boletaService.obtener(1L, "otro@gamehub.cl", false));
    }

    @Test
    void obtener_personalPuedeVerCualquierBoleta() {
        Boleta boleta = boletaConUnDetalle(EstadoBoleta.PENDIENTE, 1);
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));

        BoletaResponse respuesta = boletaService.obtener(1L, "vendedor@gamehub.cl", true);

        assertEquals(1L, respuesta.id());
    }

    private Boleta boletaConUnDetalle(EstadoBoleta estado, int cantidad) {
        Boleta boleta = Boleta.builder().id(1L).fecha(LocalDateTime.now()).total(0.0)
                .estado(estado).usuario(cliente).build();
        boleta.getDetalles().add(DetalleBoleta.builder().id(1L).boleta(boleta).producto(consola)
                .cantidad(cantidad).precioUnitario(500000.0).subtotal(500000.0 * cantidad).build());
        return boleta;
    }
}
