package com.electromecanica.app.repository;

import com.electromecanica.app.entity.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    @Query("select distinct d from Devolucion d join fetch d.venta join fetch d.cliente join fetch d.usuario left join fetch d.detalles dd left join fetch dd.producto order by d.fecha desc")
    List<Devolucion> listarCompletas();

    @Query("select coalesce(sum(dd.cantidad), 0) from DetalleDevolucion dd where dd.devolucion.venta.id = :ventaId and dd.producto.id = :productoId and dd.devolucion.estado = com.electromecanica.app.entity.EstadoDevolucion.APLICADA")
    Long cantidadDevuelta(@Param("ventaId") Long ventaId, @Param("productoId") Long productoId);

    long countByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}
