package com.electromecanica.app.service;

import com.electromecanica.app.dto.CrearDevolucionDTO;
import com.electromecanica.app.dto.SolicitudDetalleDevolucionDTO;
import com.electromecanica.app.entity.*;
import com.electromecanica.app.repository.DevolucionRepository;
import com.electromecanica.app.repository.ProductoRepository;
import com.electromecanica.app.repository.VentaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevolucionServiceTest {
    @Mock private DevolucionRepository devolucionRepository;
    @Mock private VentaRepository ventaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioService inventarioService;
    @Mock private AuditoriaService auditoriaService;
    @Mock private HttpServletRequest solicitudHttp;

    private DevolucionService devolucionService;
    private Producto producto;
    private Usuario usuario;
    private Venta venta;

    @BeforeEach
    void preparar() {
        devolucionService = new DevolucionService(devolucionRepository, ventaRepository, productoRepository,
                inventarioService, auditoriaService);
        producto = Producto.builder().id(1L).sku("MOT-PRUEBA").nombre("Motor de prueba")
                .precioVenta(new BigDecimal("590.00")).stock(1).activo(true).build();
        Cliente cliente = Cliente.builder().id(1L).tipoDocumento(TipoDocumento.DNI).numeroDocumento("12345678")
                .nombres("Cliente").apellidos("Prueba").activo(true).build();
        usuario = Usuario.builder().id(2L).nombre("Usuario").apellido("Soporte")
                .correo("soporte@prueba.pe").contrasena("cifrada").rol(Rol.SOPORTE).activo(true).build();
        venta = Venta.builder().id(10L).numeroVenta("VEN-PRUEBA").cliente(cliente).vendedor(usuario)
                .fecha(LocalDateTime.now()).subtotal(new BigDecimal("3500.00")).impuesto(new BigDecimal("630.00"))
                .descuento(BigDecimal.ZERO).total(new BigDecimal("4130.00"))
                .montoRecibido(new BigDecimal("4200.00")).vuelto(new BigDecimal("70.00"))
                .metodoPago(MetodoPago.EFECTIVO).estado(EstadoVenta.COMPLETADA).detalles(new ArrayList<>()).build();
        venta.agregarDetalle(DetalleVenta.builder().id(1L).producto(producto).cantidad(7)
                .precioLista(new BigDecimal("590.00")).precioUnitario(new BigDecimal("590.00"))
                .subtotal(new BigDecimal("4130.00")).build());
    }

    @Test
    void devolverDosUnidadesReponeStockYRegistraMovimiento() {
        when(ventaRepository.bloquearCompletaPorId(10L)).thenReturn(Optional.of(venta));
        when(productoRepository.bloquearPorId(1L)).thenReturn(Optional.of(producto));
        when(devolucionRepository.cantidadDevuelta(10L, 1L)).thenReturn(0L);
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(invocacion -> {
            Devolucion devolucion = invocacion.getArgument(0);
            devolucion.setId(20L);
            return devolucion;
        });
        CrearDevolucionDTO solicitud = new CrearDevolucionDTO(10L, "Cambio solicitado",
                List.of(new SolicitudDetalleDevolucionDTO(1L, 2)));

        devolucionService.crear(solicitud, usuario, solicitudHttp);

        assertEquals(3, producto.getStock());
        assertEquals(EstadoVenta.DEVUELTA_PARCIAL, venta.getEstado());
        verify(inventarioService).registrarMovimiento(eq(producto), eq(TipoMovimientoInventario.DEVOLUCION),
                eq(2), anyString(), eq(usuario), contains("Reposición por devolución"));
    }

    @Test
    void rechazarDevolucionCuandoLaVentaSuperaSieteDias() {
        venta.setFecha(LocalDateTime.now().minusDays(8));
        when(ventaRepository.bloquearCompletaPorId(10L)).thenReturn(Optional.of(venta));
        CrearDevolucionDTO solicitud = new CrearDevolucionDTO(10L, "Solicitud fuera de plazo",
                List.of(new SolicitudDetalleDevolucionDTO(1L, 1)));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> devolucionService.crear(solicitud, usuario, solicitudHttp));

        assertTrue(error.getMessage().contains("7 días"));
        assertEquals(1, producto.getStock());
        verifyNoInteractions(productoRepository, inventarioService);
        verify(devolucionRepository, never()).save(any(Devolucion.class));
    }
}
