package com.electromecanica.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedores", indexes = @Index(name = "idx_proveedor_ruc", columnList = "ruc", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(nullable = false, length = 180)
    private String razonSocial;

    @Column(length = 150)
    private String nombreContacto;

    @Column(length = 150)
    private String correo;

    @Column(length = 30)
    private String telefono;

    @Column(length = 300)
    private String direccion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
