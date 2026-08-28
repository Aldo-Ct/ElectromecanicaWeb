package com.electromecanica.app.repository;

import com.electromecanica.app.entity.EstadoVenta;
import com.electromecanica.app.entity.Venta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByNumeroVenta(String numeroVenta);

    @Query("select distinct v from Venta v join fetch v.cliente join fetch v.vendedor left join fetch v.detalles d left join fetch d.producto left join fetch v.comprobante order by v.fecha desc")
    List<Venta> listarCompletas();

    @Query("select distinct v from Venta v join fetch v.cliente join fetch v.vendedor left join fetch v.detalles d left join fetch d.producto left join fetch v.comprobante where v.id = :id")
    Optional<Venta> buscarCompletaPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct v from Venta v left join fetch v.detalles d left join fetch d.producto join fetch v.cliente where v.id = :id")
    Optional<Venta> bloquearCompletaPorId(@Param("id") Long id);

    long countByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("select coalesce(sum(v.total), 0) from Venta v where v.fecha between :inicio and :fin and v.estado <> :estado")
    BigDecimal sumarIngresos(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("estado") EstadoVenta estado);
}
