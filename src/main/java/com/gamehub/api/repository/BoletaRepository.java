package com.gamehub.api.repository;

import com.gamehub.api.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findByUsuarioEmailOrderByFechaDesc(String email);
}
