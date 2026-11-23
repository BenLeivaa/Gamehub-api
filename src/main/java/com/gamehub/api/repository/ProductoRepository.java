package com.gamehub.api.repository;

import com.gamehub.api.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaId(Long categoriaId);

    boolean existsByCategoriaIdAndEstadoTrue(Long categoriaId);

    /** Serializa compras concurrentes del mismo producto para no vender más stock del disponible. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Producto p where p.id = :id")
    java.util.Optional<Producto> findByIdForUpdate(@Param("id") Long id);
}