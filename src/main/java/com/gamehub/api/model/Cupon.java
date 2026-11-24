package com.gamehub.api.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "cupones") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cupon {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(nullable = false, unique = true) private String codigo;
 @Column(nullable = false) private Integer porcentaje;
 private Double tope;
 @Column(nullable = false) private Boolean activo;
 @Column(nullable = false, updatable = false) private LocalDateTime creadoEn;
 @Column(nullable = false) private LocalDateTime expiraEn;
 @PrePersist void crear(){ if(creadoEn==null) creadoEn=LocalDateTime.now(); }
}
