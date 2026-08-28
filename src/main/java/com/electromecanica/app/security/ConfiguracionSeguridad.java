package com.electromecanica.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class ConfiguracionSeguridad {
    private final ServicioDetallesUsuario servicioDetallesUsuario;
    private final FiltroAutenticacionJwt filtroAutenticacionJwt;
    private final PuntoEntradaAutenticacionJwt puntoEntrada;

    public ConfiguracionSeguridad(ServicioDetallesUsuario servicioDetallesUsuario,
                                  FiltroAutenticacionJwt filtroAutenticacionJwt,
                                  PuntoEntradaAutenticacionJwt puntoEntrada) {
        this.servicioDetallesUsuario = servicioDetallesUsuario;
        this.filtroAutenticacionJwt = filtroAutenticacionJwt;
        this.puntoEntrada = puntoEntrada;
    }

    @Bean
    public PasswordEncoder codificadorContrasena() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider proveedorAutenticacion() {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider();
        proveedor.setUserDetailsService(servicioDetallesUsuario);
        proveedor.setPasswordEncoder(codificadorContrasena());
        return proveedor;
    }

    @Bean
    public AuthenticationManager administradorAutenticacion(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource configuracionCors() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        configuracion.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(configuracionCors()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(excepciones -> excepciones.authenticationEntryPoint(puntoEntrada))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/api/autenticacion/acceso", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/auditoria/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/usuarios/contrasena").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/reportes/resumen").authenticated()
                        .requestMatchers("/api/reportes/**").hasAnyRole("ADMINISTRADOR", "GERENTE", "ANALISTA")
                        .requestMatchers(HttpMethod.POST, "/api/ventas/**").hasAnyRole("ADMINISTRADOR", "VENTAS")
                        .requestMatchers(HttpMethod.PUT, "/api/ventas/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/api/ventas/**").hasAnyRole("ADMINISTRADOR", "VENTAS", "GERENTE", "SOPORTE")
                        .requestMatchers(HttpMethod.POST, "/api/devoluciones/**").hasAnyRole("ADMINISTRADOR", "SOPORTE")
                        .requestMatchers(HttpMethod.GET, "/api/devoluciones/**").hasAnyRole("ADMINISTRADOR", "SOPORTE", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/api/inventario/**").hasAnyRole("ADMINISTRADOR", "ALMACEN", "INVENTARIO")
                        .requestMatchers(HttpMethod.GET, "/api/inventario/**").hasAnyRole("ADMINISTRADOR", "ALMACEN", "INVENTARIO", "VENTAS", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/categorias/**", "/api/marcas/**", "/api/proveedores/**")
                                .hasAnyRole("ADMINISTRADOR", "ALMACEN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**", "/api/categorias/**", "/api/marcas/**", "/api/proveedores/**")
                                .hasAnyRole("ADMINISTRADOR", "ALMACEN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/categorias/**", "/api/marcas/**", "/api/proveedores/**")
                                .hasAnyRole("ADMINISTRADOR", "ALMACEN")
                        .requestMatchers(HttpMethod.POST, "/api/clientes/**").hasAnyRole("ADMINISTRADOR", "VENTAS", "SOPORTE")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasAnyRole("ADMINISTRADOR", "VENTAS", "SOPORTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasAnyRole("ADMINISTRADOR", "VENTAS", "SOPORTE")
                        .anyRequest().authenticated())
                .authenticationProvider(proveedorAutenticacion())
                .addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
