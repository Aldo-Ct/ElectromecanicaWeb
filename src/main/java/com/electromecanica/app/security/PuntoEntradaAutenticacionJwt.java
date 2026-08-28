package com.electromecanica.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PuntoEntradaAutenticacionJwt implements AuthenticationEntryPoint {
    private final ObjectMapper conversorJson;

    public PuntoEntradaAutenticacionJwt(ObjectMapper conversorJson) {
        this.conversorJson = conversorJson;
    }

    @Override
    public void commence(HttpServletRequest solicitud, HttpServletResponse respuesta,
                         AuthenticationException excepcion) throws IOException, ServletException {
        respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("fecha", LocalDateTime.now().toString());
        cuerpo.put("estado", 401);
        cuerpo.put("mensaje", "Debe iniciar sesión para acceder a este recurso");
        cuerpo.put("ruta", solicitud.getRequestURI());
        conversorJson.writeValue(respuesta.getOutputStream(), cuerpo);
    }
}
