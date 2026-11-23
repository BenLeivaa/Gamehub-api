package com.gamehub.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String marca;
    private String modelo;

    @Column(nullable = false)
    private Double precio;

    // El stock vive aquí (antes estaba en inventory-service)
    @Column(nullable = false)
    private Integer stock;

    @Column(length = 1000)
    private String descripcion;

    private String imagen;

    @Column(nullable = false)
    private Boolean estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}
