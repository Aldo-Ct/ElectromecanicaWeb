package com.electromecanica.app.service;

import com.electromecanica.app.dto.ProductoPublicoDTO;
import com.electromecanica.app.dto.PublicacionProductoDTO;
import com.electromecanica.app.entity.*;
import com.electromecanica.app.repository.MovimientoInventarioRepository;
import com.electromecanica.app.repository.ProductoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
    @Mock private ProductoRepository productoRepository;
    @Mock private CategoriaService categoriaService;
    @Mock private MarcaService marcaService;
    @Mock private MovimientoInventarioRepository movimientoRepository;
    @Mock private AuditoriaService auditoriaService;
    @Mock private HttpServletRequest solicitudHttp;

    private ProductoService productoService;
    private Producto producto;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        productoService = new ProductoService(productoRepository, categoriaService, marcaService,
                movimientoRepository, auditoriaService);
        Categoria categoria = Categoria.builder().id(4L).nombre("Motores eléctricos").activo(true).build();
        Marca marca = Marca.builder().id(3L).nombre("WEG").activo(true).build();
        producto = Producto.builder().id(10L).sku("MOT-WEG-3HP").nombre("Motor trifásico 3 HP")
                .descripcion("Motor industrial").categoria(categoria).marca(marca).modelo("W22")
                .tipoProducto(TipoProducto.ELECTRICO).precioCompra(new BigDecimal("1200.00"))
                .precioVenta(new BigDecimal("1690.00")).stock(8).stockMinimo(2)
                .unidadMedida(UnidadMedida.UNIDAD).activo(true).publicadoVenta(true).build();
        usuario = Usuario.builder().id(1L).nombre("Administrador").apellido("General")
                .correo("admin@electromecanica.pe").contrasena("cifrada").rol(Rol.ADMINISTRADOR).activo(true).build();
    }

    @Test
    void listarPublicadosDevuelveInformacionComercialYDisponibilidad() {
        when(productoRepository.listarPublicados()).thenReturn(List.of(producto));

        List<ProductoPublicoDTO> resultado = productoService.listarPublicados();

        assertEquals(1, resultado.size());
        assertEquals("MOT-WEG-3HP", resultado.get(0).sku());
        assertEquals(new BigDecimal("1690.00"), resultado.get(0).precioVenta());
        assertEquals(8, resultado.get(0).stockDisponible());
        assertTrue(resultado.get(0).disponible());
    }

    @Test
    void ocultarProductoMantieneActivoElRegistro() {
        when(productoRepository.buscarCompletoPorId(10L)).thenReturn(Optional.of(producto));

        productoService.actualizarPublicacion(10L, new PublicacionProductoDTO(false), usuario, solicitudHttp);

        assertFalse(producto.getPublicadoVenta());
        assertTrue(producto.getActivo());
        verify(auditoriaService).registrar(eq(AccionAuditoria.CAMBIO_ESTADO), eq(usuario), eq("Producto"),
                eq(10L), contains("ocultado"), eq("publicado"), eq("oculto"), eq(solicitudHttp));
    }

    @Test
    void noPermitePublicarUnProductoInactivo() {
        producto.setActivo(false);
        producto.setPublicadoVenta(false);
        when(productoRepository.buscarCompletoPorId(10L)).thenReturn(Optional.of(producto));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> productoService.actualizarPublicacion(10L, new PublicacionProductoDTO(true), usuario, solicitudHttp));

        assertTrue(error.getMessage().contains("inactivo"));
        verifyNoInteractions(auditoriaService);
    }
}
