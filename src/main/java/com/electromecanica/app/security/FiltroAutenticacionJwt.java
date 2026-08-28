package com.electromecanica.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroAutenticacionJwt extends OncePerRequestFilter {
    private final JwtTokenProvider proveedorToken;
    private final ServicioDetallesUsuario servicioDetallesUsuario;

    public FiltroAutenticacionJwt(JwtTokenProvider proveedorToken, ServicioDetallesUsuario servicioDetallesUsuario) {
        this.proveedorToken = proveedorToken;
        this.servicioDetallesUsuario = servicioDetallesUsuario;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        try {
            String token = obtenerToken(solicitud);
            if (StringUtils.hasText(token) && proveedorToken.validateToken(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                String correo = proveedorToken.getUsernameFromToken(token);
                UserDetails detalles = servicioDetallesUsuario.loadUserByUsername(correo);
                if (detalles.isEnabled()) {
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(detalles, null, detalles.getAuthorities());
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(solicitud));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        } catch (Exception excepcion) {
            logger.warn("No se pudo establecer la autenticación JWT", excepcion);
        }
        cadena.doFilter(solicitud, respuesta);
    }

    private String obtenerToken(HttpServletRequest solicitud) {
        String cabecera = solicitud.getHeader("Authorization");
        return StringUtils.hasText(cabecera) && cabecera.startsWith("Bearer ") ? cabecera.substring(7) : null;
    }
}
