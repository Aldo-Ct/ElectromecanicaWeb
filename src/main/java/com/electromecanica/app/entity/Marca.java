package com.electromecanica.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marcas", indexes = @Index(name = "idx_marca_nombre", columnList = "nombre", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 500)
    private String logoUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
