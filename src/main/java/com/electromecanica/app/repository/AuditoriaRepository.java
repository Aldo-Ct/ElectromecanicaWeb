package com.electromecanica.app.repository;

import com.electromecanica.app.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    @Query("select a from Auditoria a join fetch a.usuario order by a.fecha desc")
    List<Auditoria> listarCompletas();
}
