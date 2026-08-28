package com.electromecanica.app.service;

import com.electromecanica.app.dto.CrearVentaDTO;
import com.electromecanica.app.dto.SolicitudDetalleVentaDTO;
import com.electromecanica.app.entity.*;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {
    @Mock private VentaRepository ventaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ClienteService clienteService;
    @Mock private InventarioService inventarioService;
    @Mock private AuditoriaService auditoriaService;
    @Mock private ComprobanteVentaService comprobanteVentaService;
    @Mock private HttpServletRequest solicitudHttp;

    private VentaService ventaService;
    private Producto producto;
    private Cliente cliente;
    private Usuario vendedor;

    @BeforeEach
    void preparar() {
        ventaService = new VentaService(ventaRepository, productoRepository, clienteService,
                inventarioService, auditoriaService, comprobanteVentaService);
        producto = Producto.builder().id(1L).sku("MOT-PRUEBA").nombre("Motor de prueba")
                .precioVenta(new BigDecimal("590.00")).precioCompra(new BigDecimal("420.00"))
                .stock(8).stockMinimo(2).activo(true).build();
        cliente = Cliente.builder().id(1L).tipoDocumento(TipoDocumento.DNI).numeroDocumento("12345678")
                .nombres("Cliente").apellidos("Prueba").activo(true).build();
        vendedor = Usuario.builder().id(1L).nombre("Usuario").apellido("Ventas")
                .correo("ventas@prueba.pe").contrasena("cifrada").rol(Rol.VENTAS).activo(true).build();
    }

    @Test
    void venderSieteUnidadesReduceStockDeOchoAUno() {
        when(clienteService.obtener(1L)).thenReturn(cliente);
        when(productoRepository.bloquearPorId(1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.saveAndFlush(any(Venta.class))).thenAnswer(invocacion -> {
            Venta venta = invocacion.getArgument(0);
            venta.setId(10L);
            return venta;
        });
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        CrearVentaDTO solicitud = new CrearVentaDTO(1L, null, BigDecimal.ZERO, MetodoPago.EFECTIVO,
                new BigDecimal("4200.00"), TipoComprobante.BOLETA, null,
                List.of(new SolicitudDetalleVentaDTO(1L, 7, new BigDecimal("590.00"))));

        ventaService.crear(solicitud, vendedor, solicitudHttp);

        assertEquals(1, producto.getStock());
        verify(inventarioService).registrarMovimiento(eq(producto), eq(TipoMovimientoInventario.VENTA),
                eq(-7), anyString(), eq(vendedor), eq("Salida por venta"));
    }

    @Test
    void rechazarVentaSinStockNoModificaExistencias() {
        when(clienteService.obtener(1L)).thenReturn(cliente);
        when(productoRepository.bloquearPorId(1L)).thenReturn(Optional.of(producto));
        CrearVentaDTO solicitud = new CrearVentaDTO(1L, null, BigDecimal.ZERO, MetodoPago.EFECTIVO,
                new BigDecimal("6000.00"), TipoComprobante.BOLETA, null,
                List.of(new SolicitudDetalleVentaDTO(1L, 9, new BigDecimal("590.00"))));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ventaService.crear(solicitud, vendedor, solicitudHttp));

        assertTrue(error.getMessage().contains("Stock insuficiente"));
        assertEquals(8, producto.getStock());
        verify(inventarioService, never()).registrarMovimiento(any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void anularVentaReponeElStockVendido() {
        producto.setStock(1);
        Venta venta = ventaCompletada(producto, 7);
        when(ventaRepository.bloquearCompletaPorId(10L)).thenReturn(Optional.of(venta));
        when(productoRepository.bloquearPorId(1L)).thenReturn(Optional.of(producto));

        ventaService.anular(10L, vendedor, "Error de registro", solicitudHttp);

        assertEquals(8, producto.getStock());
        assertEquals(EstadoVenta.ANULADA, venta.getEstado());
        verify(inventarioService).registrarMovimiento(eq(producto), eq(TipoMovimientoInventario.AJUSTE),
                eq(7), eq(venta.getNumeroVenta()), eq(vendedor), contains("Reposición por anulación"));
    }

    private Venta ventaCompletada(Producto productoVenta, int cantidad) {
        Venta venta = Venta.builder().id(10L).numeroVenta("VEN-PRUEBA").cliente(cliente).vendedor(vendedor)
                .fecha(LocalDateTime.now()).subtotal(new BigDecimal("3500.00")).impuesto(new BigDecimal("630.00"))
                .descuento(BigDecimal.ZERO).total(new BigDecimal("4130.00"))
                .montoRecibido(new BigDecimal("4200.00")).vuelto(new BigDecimal("70.00"))
                .metodoPago(MetodoPago.EFECTIVO).estado(EstadoVenta.COMPLETADA).detalles(new ArrayList<>()).build();
        venta.agregarDetalle(DetalleVenta.builder().id(1L).producto(productoVenta).cantidad(cantidad)
                .precioLista(new BigDecimal("590.00")).precioUnitario(new BigDecimal("590.00"))
                .subtotal(new BigDecimal("4130.00")).build());
        return venta;
    }
}
