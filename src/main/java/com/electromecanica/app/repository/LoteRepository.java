package com.electromecanica.app.repository;

import com.electromecanica.app.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    Optional<Lote> findByCodigoLoteIgnoreCase(String codigoLote);

    @Query("select l from Lote l join fetch l.producto join fetch l.proveedor order by l.fechaIngreso desc")
    List<Lote> listarCompletos();

    List<Lote> findByProductoIdOrderByFechaIngresoDesc(Long productoId);
}
