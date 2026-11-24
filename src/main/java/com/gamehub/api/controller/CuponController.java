package com.gamehub.api.controller;
import com.gamehub.api.dto.CuponRequest; import com.gamehub.api.model.Cupon; import com.gamehub.api.repository.CuponRepository;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import com.gamehub.api.exception.BusinessException;
@RestController @RequestMapping("/api/v1/cupones") @RequiredArgsConstructor
public class CuponController { private final CuponRepository repo;
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Cupon crear(@Valid @RequestBody CuponRequest r) { String codigo=r.codigo().trim().toUpperCase(); if(repo.findByCodigoIgnoreCase(codigo).isPresent()) throw new IllegalArgumentException("El código ya existe"); return repo.save(Cupon.builder().codigo(codigo).porcentaje(r.porcentaje()).tope(r.tope()).activo(r.activo()==null||r.activo()).expiraEn(r.expiraEn()).build()); }
 @GetMapping public java.util.List<Cupon> listar(){return repo.findAll();}
 @GetMapping("/validar") public Cupon validar(@RequestParam String codigo) { Cupon cupon=repo.findByCodigoIgnoreCase(codigo.trim()).orElseThrow(()->new BusinessException("Cupón no válido")); if(!Boolean.TRUE.equals(cupon.getActivo()) || !cupon.getExpiraEn().isAfter(LocalDateTime.now())) throw new BusinessException("Cupón vencido o no está activo"); return cupon; }
 @PatchMapping("/{id}/estado") public Cupon cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) { Cupon cupon=repo.findById(id).orElseThrow(()->new com.gamehub.api.exception.ResourceNotFoundException("Cupón no encontrado")); cupon.setActivo(activo); return repo.save(cupon); }
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void desactivar(@PathVariable Long id) { Cupon cupon=repo.findById(id).orElseThrow(()->new com.gamehub.api.exception.ResourceNotFoundException("Cupón no encontrado")); cupon.setActivo(false); repo.save(cupon); }
}
