package com.electromecanica.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "especificaciones_producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "producto")
public class EspecificacionProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_especificacion_producto"))
    private Producto producto;

    @Column(length = 80)
    private String voltaje;
    @Column(length = 80)
    private String corriente;
    @Column(length = 80)
    private String potencia;
    @Column(length = 80)
    private String frecuencia;
    private Integer fases;
    @Column(length = 40)
    private String gradoProteccion;
    @Column(length = 120)
    private String material;
    @Column(length = 180)
    private String dimensiones;
    @Column(precision = 12, scale = 3)
    private BigDecimal peso;
    @Column(length = 80)
    private String diametro;
    @Column(length = 100)
    private String capacidad;
    @Column(length = 100)
    private String velocidad;
}
