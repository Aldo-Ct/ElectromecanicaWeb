package com.electromecanica.app.service;

import com.electromecanica.app.dto.MarcaDTO;
import com.electromecanica.app.entity.AccionAuditoria;
import com.electromecanica.app.entity.Marca;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.repository.MarcaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarcaService {
    private final MarcaRepository marcaRepository;
    private final AuditoriaService auditoriaService;

    public MarcaService(MarcaRepository marcaRepository, AuditoriaService auditoriaService) {
        this.marcaRepository = marcaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<MarcaDTO> listar() {
        return marcaRepository.findAll(Sort.by("nombre")).stream().map(this::convertir).toList();
    }

    @Transactional
    public MarcaDTO crear(MarcaDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        marcaRepository.findByNombreIgnoreCase(dto.nombre()).ifPresent(m -> {
            throw new IllegalArgumentException("Ya existe una marca con ese nombre");
        });
        Marca marca = Marca.builder().nombre(dto.nombre().trim()).descripcion(dto.descripcion()).logoUrl(dto.logoUrl())
                .activo(dto.activo() == null || dto.activo()).build();
        marcaRepository.save(marca);
        auditoriaService.registrar(AccionAuditoria.CREACION, usuario, "Marca", marca.getId(),
                "Marca creada: " + marca.getNombre(), null, marca.getNombre(), solicitudHttp);
        return convertir(marca);
    }

    @Transactional
    public MarcaDTO actualizar(Long id, MarcaDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        Marca marca = obtener(id);
        marcaRepository.findByNombreIgnoreCase(dto.nombre()).filter(m -> !m.getId().equals(id)).ifPresent(m -> {
            throw new IllegalArgumentException("Ya existe una marca con ese nombre");
        });
        String anterior = marca.getNombre();
        marca.setNombre(dto.nombre().trim());
        marca.setDescripcion(dto.descripcion());
        marca.setLogoUrl(dto.logoUrl());
        marca.setActivo(dto.activo() == null || dto.activo());
        auditoriaService.registrar(AccionAuditoria.ACTUALIZACION, usuario, "Marca", id,
                "Marca actualizada", anterior, marca.getNombre(), solicitudHttp);
        return convertir(marca);
    }

    @Transactional
    public void desactivar(Long id, Usuario usuario, HttpServletRequest solicitudHttp) {
        Marca marca = obtener(id);
        marca.setActivo(false);
        auditoriaService.registrar(AccionAuditoria.CAMBIO_ESTADO, usuario, "Marca", id,
                "Marca desactivada", "activo", "inactivo", solicitudHttp);
    }

    public Marca obtener(Long id) {
        return marcaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Marca no encontrada"));
    }

    private MarcaDTO convertir(Marca marca) {
        return new MarcaDTO(marca.getId(), marca.getNombre(), marca.getDescripcion(), marca.getLogoUrl(), marca.getActivo());
    }
}
