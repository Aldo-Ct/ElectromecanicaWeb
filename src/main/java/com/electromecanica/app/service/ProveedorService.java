package com.electromecanica.app.service;

import com.electromecanica.app.dto.ProveedorDTO;
import com.electromecanica.app.entity.AccionAuditoria;
import com.electromecanica.app.entity.Proveedor;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.repository.ProveedorRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorService {
    private final ProveedorRepository proveedorRepository;
    private final AuditoriaService auditoriaService;

    public ProveedorService(ProveedorRepository proveedorRepository, AuditoriaService auditoriaService) {
        this.proveedorRepository = proveedorRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listar() {
        return proveedorRepository.findAll(Sort.by("razonSocial")).stream().map(this::convertir).toList();
    }

    @Transactional
    public ProveedorDTO crear(ProveedorDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        proveedorRepository.findByRuc(dto.ruc()).ifPresent(p -> {
            throw new IllegalArgumentException("Ya existe un proveedor con ese RUC");
        });
        Proveedor proveedor = new Proveedor();
        copiar(dto, proveedor);
        proveedorRepository.save(proveedor);
        auditoriaService.registrar(AccionAuditoria.CREACION, usuario, "Proveedor", proveedor.getId(),
                "Proveedor creado: " + proveedor.getRazonSocial(), null, proveedor.getRuc(), solicitudHttp);
        return convertir(proveedor);
    }

    @Transactional
    public ProveedorDTO actualizar(Long id, ProveedorDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        Proveedor proveedor = obtener(id);
        proveedorRepository.findByRuc(dto.ruc()).filter(p -> !p.getId().equals(id)).ifPresent(p -> {
            throw new IllegalArgumentException("Ya existe un proveedor con ese RUC");
        });
        String anterior = proveedor.getRuc();
        copiar(dto, proveedor);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZACION, usuario, "Proveedor", id,
                "Proveedor actualizado", anterior, proveedor.getRuc(), solicitudHttp);
        return convertir(proveedor);
    }

    @Transactional
    public void desactivar(Long id, Usuario usuario, HttpServletRequest solicitudHttp) {
        Proveedor proveedor = obtener(id);
        proveedor.setActivo(false);
        auditoriaService.registrar(AccionAuditoria.CAMBIO_ESTADO, usuario, "Proveedor", id,
                "Proveedor desactivado", "activo", "inactivo", solicitudHttp);
    }

    public Proveedor obtener(Long id) {
        return proveedorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
    }

    private void copiar(ProveedorDTO dto, Proveedor proveedor) {
        proveedor.setRuc(dto.ruc().trim());
        proveedor.setRazonSocial(dto.razonSocial().trim());
        proveedor.setNombreContacto(dto.nombreContacto());
        proveedor.setCorreo(dto.correo());
        proveedor.setTelefono(dto.telefono());
        proveedor.setDireccion(dto.direccion());
        proveedor.setActivo(dto.activo() == null || dto.activo());
    }

    private ProveedorDTO convertir(Proveedor proveedor) {
        return new ProveedorDTO(proveedor.getId(), proveedor.getRuc(), proveedor.getRazonSocial(),
                proveedor.getNombreContacto(), proveedor.getCorreo(), proveedor.getTelefono(), proveedor.getDireccion(), proveedor.getActivo());
    }
}
