package com.electromecanica.app.repository;

import com.electromecanica.app.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    @Query("select m from MovimientoInventario m join fetch m.producto join fetch m.usuario order by m.fecha desc")
    List<MovimientoInventario> listarCompletos();

    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(Long productoId);
}
