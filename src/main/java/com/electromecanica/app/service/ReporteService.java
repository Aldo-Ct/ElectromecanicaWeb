package com.electromecanica.app.service;

import com.electromecanica.app.dto.ResumenReporteDTO;
import com.electromecanica.app.entity.EstadoVenta;
import com.electromecanica.app.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReporteService {
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProveedorRepository proveedorRepository;
    private final DevolucionRepository devolucionRepository;

    public ReporteService(ProductoRepository productoRepository, VentaRepository ventaRepository,
                          ClienteRepository clienteRepository, ProveedorRepository proveedorRepository,
                          DevolucionRepository devolucionRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.proveedorRepository = proveedorRepository;
        this.devolucionRepository = devolucionRepository;
    }

    @Transactional(readOnly = true)
    public ResumenReporteDTO resumen() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1).minusNanos(1);
        BigDecimal ingresos = ventaRepository.sumarIngresos(inicio, fin, EstadoVenta.ANULADA);
        return new ResumenReporteDTO(productoRepository.countByActivoTrue(), productoRepository.contarBajoMinimo(),
                productoRepository.sumarStockActivo(), ventaRepository.countByFechaBetween(inicio, fin), ingresos,
                clienteRepository.countByActivoTrue(), proveedorRepository.countByActivoTrue(),
                devolucionRepository.countByFechaBetween(inicio, fin));
    }
}
