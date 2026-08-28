package com.electromecanica.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes", indexes = @Index(name = "idx_cliente_documento", columnList = "numero_documento", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @Column(length = 180)
    private String razonSocial;

    @Column(length = 100)
    private String nombres;

    @Column(length = 100)
    private String apellidos;

    @Column(length = 150)
    private String correo;

    @Column(length = 30)
    private String telefono;

    @Column(length = 300)
    private String direccion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    public String getNombreCompleto() {
        if (razonSocial != null && !razonSocial.isBlank()) {
            return razonSocial;
        }
        return ((nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos)).trim();
    }
}
