package com.gamehub.api.repository;

import com.gamehub.api.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaId(Long categoriaId);

    boolean existsByCategoriaIdAndEstadoTrue(Long categoriaId);
}
