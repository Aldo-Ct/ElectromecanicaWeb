package com.electromecanica.app.service;

import com.electromecanica.app.dto.AuditoriaDTO;
import com.electromecanica.app.entity.AccionAuditoria;
import com.electromecanica.app.entity.Auditoria;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditoriaService {
    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional
    public void registrar(AccionAuditoria accion, Usuario usuario, String entidad, Object entidadId,
                          String descripcion, String valorAnterior, String valorNuevo, HttpServletRequest solicitudHttp) {
        Auditoria auditoria = Auditoria.builder()
                .accion(accion)
                .usuario(usuario)
                .entidad(entidad)
                .entidadId(entidadId == null ? null : entidadId.toString())
                .descripcion(descripcion)
                .valorAnterior(valorAnterior)
                .valorNuevo(valorNuevo)
                .direccionIp(obtenerDireccionIp(solicitudHttp))
                .build();
        auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaDTO> listar() {
        return auditoriaRepository.listarCompletas().stream().map(this::convertir).toList();
    }

    private AuditoriaDTO convertir(Auditoria auditoria) {
        Usuario usuario = auditoria.getUsuario();
        return new AuditoriaDTO(auditoria.getId(), usuario.getId(), usuario.getNombre() + " " + usuario.getApellido(),
                auditoria.getAccion(), auditoria.getEntidad(), auditoria.getEntidadId(), auditoria.getDescripcion(),
                auditoria.getValorAnterior(), auditoria.getValorNuevo(), auditoria.getFecha(), auditoria.getDireccionIp());
    }

    private String obtenerDireccionIp(HttpServletRequest solicitudHttp) {
        if (solicitudHttp == null) {
            return null;
        }
        String reenviada = solicitudHttp.getHeader("X-Forwarded-For");
        return reenviada == null || reenviada.isBlank()
                ? solicitudHttp.getRemoteAddr()
                : reenviada.split(",")[0].trim();
    }
}
