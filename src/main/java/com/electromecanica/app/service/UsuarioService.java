package com.electromecanica.app.service;

import com.electromecanica.app.dto.CambiarContrasenaDTO;
import com.electromecanica.app.dto.CrearUsuarioDTO;
import com.electromecanica.app.dto.UsuarioDTO;
import com.electromecanica.app.entity.AccionAuditoria;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorContrasena;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder codificadorContrasena,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorContrasena = codificadorContrasena;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar() {
        return usuarioRepository.findAll(Sort.by("nombre", "apellido")).stream().map(this::convertir).toList();
    }

    @Transactional
    public UsuarioDTO crear(CrearUsuarioDTO dto, Usuario actor, HttpServletRequest solicitudHttp) {
        String correo = dto.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }
        Usuario usuario = Usuario.builder()
                .nombre(dto.nombre().trim())
                .apellido(dto.apellido().trim())
                .correo(correo)
                .contrasena(codificadorContrasena.encode(dto.contrasena()))
                .rol(dto.rol())
                .activo(true)
                .build();
        usuarioRepository.save(usuario);
        auditoriaService.registrar(AccionAuditoria.CREACION, actor, "Usuario", usuario.getId(),
                "Usuario creado: " + correo, null, dto.rol().name(), solicitudHttp);
        return convertir(usuario);
    }

    @Transactional
    public UsuarioDTO cambiarEstado(Long id, boolean activo, Usuario actor, HttpServletRequest solicitudHttp) {
        Usuario usuario = obtener(id);
        if (usuario.getId().equals(actor.getId()) && !activo) {
            throw new IllegalArgumentException("No puede desactivar su propio usuario");
        }
        usuario.setActivo(activo);
        auditoriaService.registrar(AccionAuditoria.CAMBIO_ESTADO, actor, "Usuario", id,
                activo ? "Usuario activado" : "Usuario desactivado", null, Boolean.toString(activo), solicitudHttp);
        return convertir(usuario);
    }

    @Transactional
    public void cambiarContrasena(CambiarContrasenaDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        if (!codificadorContrasena.matches(dto.contrasenaActual(), usuario.getContrasena())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        usuario.setContrasena(codificadorContrasena.encode(dto.contrasenaNueva()));
        auditoriaService.registrar(AccionAuditoria.CAMBIO_CONTRASENA, usuario, "Usuario", usuario.getId(),
                "Contraseña actualizada", null, null, solicitudHttp);
    }

    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private UsuarioDTO convertir(Usuario usuario) {
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getApellido(), usuario.getCorreo(),
                usuario.getRol(), usuario.getActivo(), usuario.getFechaCreacion());
    }
}
