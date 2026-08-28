package com.electromecanica.app.service;

import com.electromecanica.app.dto.AccesoDTO;
import com.electromecanica.app.dto.RespuestaAutenticacionDTO;
import com.electromecanica.app.entity.AccionAuditoria;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.repository.UsuarioRepository;
import com.electromecanica.app.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionService {
    private final AuthenticationManager administradorAutenticacion;
    private final JwtTokenProvider proveedorToken;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public AutenticacionService(AuthenticationManager administradorAutenticacion, JwtTokenProvider proveedorToken,
                                UsuarioRepository usuarioRepository, AuditoriaService auditoriaService) {
        this.administradorAutenticacion = administradorAutenticacion;
        this.proveedorToken = proveedorToken;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public RespuestaAutenticacionDTO acceder(AccesoDTO dto, HttpServletRequest solicitudHttp) {
        String correo = dto.correo().trim().toLowerCase();
        Authentication autenticacion = administradorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(correo, dto.contrasena()));
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        String token = proveedorToken.generateToken(autenticacion);
        auditoriaService.registrar(AccionAuditoria.ACCESO, usuario, "Usuario", usuario.getId(),
                "Acceso correcto al sistema", null, null, solicitudHttp);
        return new RespuestaAutenticacionDTO(token, "Bearer", usuario.getId(), usuario.getNombre(),
                usuario.getApellido(), usuario.getCorreo(), usuario.getRol());
    }

    @Transactional
    public void salir(Usuario usuario, HttpServletRequest solicitudHttp) {
        auditoriaService.registrar(AccionAuditoria.SALIDA, usuario, "Usuario", usuario.getId(),
                "Sesión cerrada", null, null, solicitudHttp);
    }
}
