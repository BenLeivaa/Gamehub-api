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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio principal: crear una compra (boleta) validando stock,
 * calculando el total y descontando el inventario, todo en una sola transacción.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public BoletaResponse crear(String emailUsuario, BoletaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        Boleta boleta = Boleta.builder()
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .estado(EstadoBoleta.PENDIENTE)
                .total(0.0)
                .build();

        double total = 0;
        for (ItemBoletaRequest item : request.items()) {
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con id=" + item.productoId()));

            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new BusinessException("El producto '" + producto.getNombre() + "' no está disponible");
            }
            if (producto.getStock() < item.cantidad()) {
                throw new BusinessException("Stock insuficiente para '" + producto.getNombre()
                        + "'. Disponible: " + producto.getStock());
            }

            producto.setStock(producto.getStock() - item.cantidad());

            double subtotal = producto.getPrecio() * item.cantidad();
            boleta.getDetalles().add(DetalleBoleta.builder()
                    .boleta(boleta)
                    .producto(producto)
                    .cantidad(item.cantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .build());
            total += subtotal;
        }
        boleta.setTotal(total);

        Boleta guardada = boletaRepository.save(boleta);
        log.info("Boleta creada (id={}) para {} por un total de {}", guardada.getId(), emailUsuario, total);
        return BoletaResponse.from(guardada);
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listar() {
        return boletaRepository.findAllByOrderByFechaDesc().stream().map(BoletaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> misCompras(String emailUsuario) {
        return boletaRepository.findByUsuarioEmailOrderByFechaDesc(emailUsuario)
                .stream().map(BoletaResponse::from).toList();
    }

    /** El personal (admin/vendedor) ve cualquier boleta; un cliente solo las suyas. */
    @Transactional(readOnly = true)
    public BoletaResponse obtener(Long id, String emailSolicitante, boolean esPersonal) {
        Boleta boleta = buscar(id);
        if (!esPersonal && !boleta.getUsuario().getEmail().equals(emailSolicitante)) {
            throw new AccessDeniedException("No puedes ver boletas de otros usuarios");
        }
        return BoletaResponse.from(boleta);
    }

    /** Cancela la boleta y devuelve el stock de cada producto. */
    @Transactional
    public BoletaResponse cancelar(Long id) {
        Boleta boleta = buscar(id);
        if (boleta.getEstado() == EstadoBoleta.CANCELADA) {
            throw new BusinessException("La boleta ya está cancelada");
        }
        for (DetalleBoleta detalle : boleta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
        }
        boleta.setEstado(EstadoBoleta.CANCELADA);
        log.warn("Boleta cancelada (id={}): stock devuelto", id);
        return BoletaResponse.from(boletaRepository.save(boleta));
    }

    private Boleta buscar(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Boleta no encontrada con id=" + id));
    }
}
