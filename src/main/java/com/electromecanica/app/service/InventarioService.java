package com.electromecanica.app.service;

import com.electromecanica.app.dto.AjusteInventarioDTO;
import com.electromecanica.app.dto.LoteDTO;
import com.electromecanica.app.dto.MovimientoInventarioDTO;
import com.electromecanica.app.entity.*;
import com.electromecanica.app.repository.LoteRepository;
import com.electromecanica.app.repository.MovimientoInventarioRepository;
import com.electromecanica.app.repository.ProductoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioService {
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorService proveedorService;
    private final AuditoriaService auditoriaService;

    public InventarioService(LoteRepository loteRepository, MovimientoInventarioRepository movimientoRepository,
                             ProductoRepository productoRepository, ProveedorService proveedorService,
                             AuditoriaService auditoriaService) {
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.proveedorService = proveedorService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<LoteDTO> listarLotes() {
        return loteRepository.listarCompletos().stream().map(this::convertirLote).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> listarMovimientos(Long productoId) {
        List<MovimientoInventario> movimientos = productoId == null
                ? movimientoRepository.listarCompletos()
                : movimientoRepository.findByProductoIdOrderByFechaDesc(productoId);
        return movimientos.stream().map(this::convertirMovimiento).toList();
    }

    @Transactional
    public LoteDTO registrarIngreso(LoteDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        loteRepository.findByCodigoLoteIgnoreCase(dto.codigoLote()).ifPresent(l -> {
            throw new IllegalArgumentException("Ya existe un lote con ese código");
        });
        Producto producto = productoRepository.bloquearPorId(dto.productoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        Proveedor proveedor = proveedorService.obtener(dto.proveedorId());
        if (!producto.getActivo() || !proveedor.getActivo()) {
            throw new IllegalArgumentException("El producto y el proveedor deben estar activos");
        }
        Lote lote = Lote.builder()
                .producto(producto)
                .proveedor(proveedor)
                .codigoLote(dto.codigoLote().trim().toUpperCase())
                .cantidad(dto.cantidad())
                .precioCompra(dto.precioCompra())
                .fechaIngreso(dto.fechaIngreso() == null ? LocalDateTime.now() : dto.fechaIngreso())
                .observaciones(dto.observaciones())
                .build();
        loteRepository.save(lote);
        producto.setStock(producto.getStock() + dto.cantidad());
        producto.setPrecioCompra(dto.precioCompra());
        registrarMovimiento(producto, TipoMovimientoInventario.ENTRADA, dto.cantidad(),
                lote.getCodigoLote(), usuario, dto.observaciones());
        auditoriaService.registrar(AccionAuditoria.MOVIMIENTO_INVENTARIO, usuario, "Lote", lote.getId(),
                "Ingreso de " + dto.cantidad() + " unidades de " + producto.getNombre(), null,
                lote.getCodigoLote(), solicitudHttp);
        return convertirLote(lote);
    }

    @Transactional
    public MovimientoInventarioDTO ajustar(AjusteInventarioDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        if (dto.cantidad() == 0) {
            throw new IllegalArgumentException("La cantidad del ajuste no puede ser cero");
        }
        Producto producto = productoRepository.bloquearPorId(dto.productoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        int nuevoStock = producto.getStock() + dto.cantidad();
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El ajuste dejaría el stock en negativo");
        }
        producto.setStock(nuevoStock);
        MovimientoInventario movimiento = registrarMovimiento(producto, TipoMovimientoInventario.AJUSTE,
                dto.cantidad(), dto.referencia(), usuario, dto.observaciones());
        auditoriaService.registrar(AccionAuditoria.MOVIMIENTO_INVENTARIO, usuario, "Producto", producto.getId(),
                "Ajuste de inventario. Nuevo stock: " + nuevoStock, null, dto.cantidad().toString(), solicitudHttp);
        return convertirMovimiento(movimiento);
    }

    @Transactional
    public MovimientoInventario registrarMovimiento(Producto producto, TipoMovimientoInventario tipo, Integer cantidad,
                                                     String referencia, Usuario usuario, String observaciones) {
        return movimientoRepository.save(MovimientoInventario.builder()
                .producto(producto)
                .tipo(tipo)
                .cantidad(cantidad)
                .referencia(referencia)
                .usuario(usuario)
                .observaciones(observaciones)
                .build());
    }

    private LoteDTO convertirLote(Lote lote) {
        return new LoteDTO(lote.getId(), lote.getProducto().getId(), lote.getProducto().getNombre(),
                lote.getProducto().getSku(), lote.getCodigoLote(), lote.getCantidad(), lote.getProducto().getStock(), lote.getPrecioCompra(),
                lote.getFechaIngreso(), lote.getProveedor().getId(), lote.getProveedor().getRazonSocial(), lote.getObservaciones());
    }

    private MovimientoInventarioDTO convertirMovimiento(MovimientoInventario movimiento) {
        Usuario usuario = movimiento.getUsuario();
        return new MovimientoInventarioDTO(movimiento.getId(), movimiento.getProducto().getId(),
                movimiento.getProducto().getNombre(), movimiento.getProducto().getSku(), movimiento.getTipo(),
                movimiento.getCantidad(), movimiento.getFecha(), movimiento.getReferencia(), usuario.getId(),
                usuario.getNombre() + " " + usuario.getApellido(), movimiento.getObservaciones());
    }
}
