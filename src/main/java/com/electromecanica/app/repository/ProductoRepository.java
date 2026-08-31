package com.electromecanica.app.repository;

import com.electromecanica.app.entity.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySkuIgnoreCase(String sku);

    Optional<Producto> findByCodigoBarrasIgnoreCase(String codigoBarras);

    @Query("select p from Producto p join fetch p.categoria join fetch p.marca left join fetch p.especificacion order by p.nombre")
    List<Producto> listarCompletos();

    @Query("select p from Producto p join fetch p.categoria join fetch p.marca left join fetch p.especificacion " +
            "where p.activo = true and p.publicadoVenta = true and p.categoria.activo = true and p.marca.activo = true " +
            "order by p.nombre")
    List<Producto> listarPublicados();

    @Query("select distinct p from Producto p join fetch p.categoria join fetch p.marca left join fetch p.especificacion " +
            "where lower(p.nombre) like lower(concat('%', :termino, '%')) " +
            "or lower(p.sku) like lower(concat('%', :termino, '%')) " +
            "or lower(coalesce(p.codigoBarras, '')) like lower(concat('%', :termino, '%')) " +
            "or lower(p.modelo) like lower(concat('%', :termino, '%')) order by p.nombre")
    List<Producto> buscar(@Param("termino") String termino);

    @Query("select p from Producto p join fetch p.categoria join fetch p.marca left join fetch p.especificacion where p.id = :id")
    Optional<Producto> buscarCompletoPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> bloquearPorId(@Param("id") Long id);

    long countByActivoTrue();

    @Query("select count(p) from Producto p where p.activo = true and p.stock <= p.stockMinimo")
    long contarBajoMinimo();

    @Query("select coalesce(sum(p.stock), 0) from Producto p where p.activo = true")
    Integer sumarStockActivo();
}
