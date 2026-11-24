package com.gamehub.api.repository;
import com.gamehub.api.model.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CuponRepository extends JpaRepository<Cupon, Long> { Optional<Cupon> findByCodigoIgnoreCase(String codigo); }
