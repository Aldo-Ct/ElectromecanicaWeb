package com.electromecanica.app.service;

import com.electromecanica.app.dto.EspecificacionProductoDTO;
import com.electromecanica.app.dto.ProductoDTO;
import com.electromecanica.app.entity.*;
import com.electromecanica.app.repository.MovimientoInventarioRepository;
import com.electromecanica.app.repository.ProductoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final MarcaService marcaService;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AuditoriaService auditoriaService;

    public ProductoService(ProductoRepository productoRepository, CategoriaService categoriaService,
                           MarcaService marcaService, MovimientoInventarioRepository movimientoRepository,
                           AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
        this.marcaService = marcaService;
        this.movimientoRepository = movimientoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listar(String termino) {
        List<Producto> productos = termino == null || termino.isBlank()
                ? productoRepository.listarCompletos()
                : productoRepository.buscar(termino.trim());
        return productos.stream().map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtenerDTO(Long id) {
        return convertir(productoRepository.buscarCompletoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado")));
    }

    @Transactional
    public ProductoDTO crear(ProductoDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        productoRepository.findBySkuIgnoreCase(dto.sku()).ifPresent(p -> {
            throw new IllegalArgumentException("Ya existe un producto con ese SKU");
        });
        String codigoBarras = normalizarCodigoBarras(dto.codigoBarras());
        validarCodigoBarrasDisponible(codigoBarras, null);
        Categoria categoria = categoriaService.obtener(dto.categoriaId());
        Marca marca = marcaService.obtener(dto.marcaId());
        if (!categoria.getActivo() || !marca.getActivo()) {
            throw new IllegalArgumentException("La categoría y la marca deben estar activas");
        }
        int stockInicial = dto.stock() == null ? 0 : dto.stock();
        Producto producto = Producto.builder()
                .sku(dto.sku().trim().toUpperCase())
                .codigoBarras(codigoBarras)
                .nombre(dto.nombre().trim())
                .descripcion(dto.descripcion())
                .categoria(categoria)
                .marca(marca)
                .modelo(dto.modelo())
                .tipoProducto(dto.tipoProducto())
                .precioCompra(dto.precioCompra())
                .precioVenta(dto.precioVenta())
                .stock(stockInicial)
                .stockMinimo(dto.stockMinimo() == null ? 0 : dto.stockMinimo())
                .unidadMedida(dto.unidadMedida())
                .imagenUrl(dto.imagenUrl())
                .fichaTecnicaUrl(dto.fichaTecnicaUrl())
                .garantia(dto.garantia())
                .activo(dto.activo() == null || dto.activo())
                .build();
        aplicarEspecificacion(producto, dto.especificacion());
        productoRepository.save(producto);
        if (stockInicial > 0) {
            movimientoRepository.save(MovimientoInventario.builder()
                    .producto(producto).tipo(TipoMovimientoInventario.ENTRADA).cantidad(stockInicial)
                    .referencia("STOCK-INICIAL-" + producto.getSku()).usuario(usuario)
                    .observaciones("Stock inicial al crear el producto").build());
        }
        auditoriaService.registrar(AccionAuditoria.CREACION, usuario, "Producto", producto.getId(),
                "Producto creado: " + producto.getNombre(), null, producto.getSku(), solicitudHttp);
        return convertir(producto);
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        Producto producto = productoRepository.buscarCompletoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        productoRepository.findBySkuIgnoreCase(dto.sku()).filter(p -> !p.getId().equals(id)).ifPresent(p -> {
            throw new IllegalArgumentException("Ya existe un producto con ese SKU");
        });
        String codigoBarras = normalizarCodigoBarras(dto.codigoBarras());
        validarCodigoBarrasDisponible(codigoBarras, id);
        String anterior = producto.toString();
        producto.setSku(dto.sku().trim().toUpperCase());
        producto.setCodigoBarras(codigoBarras);
        producto.setNombre(dto.nombre().trim());
        producto.setDescripcion(dto.descripcion());
        producto.setCategoria(categoriaService.obtener(dto.categoriaId()));
        producto.setMarca(marcaService.obtener(dto.marcaId()));
        producto.setModelo(dto.modelo());
        producto.setTipoProducto(dto.tipoProducto());
        producto.setPrecioCompra(dto.precioCompra());
        producto.setPrecioVenta(dto.precioVenta());
        producto.setStockMinimo(dto.stockMinimo() == null ? 0 : dto.stockMinimo());
        producto.setUnidadMedida(dto.unidadMedida());
        producto.setImagenUrl(dto.imagenUrl());
        producto.setFichaTecnicaUrl(dto.fichaTecnicaUrl());
        producto.setGarantia(dto.garantia());
        producto.setActivo(dto.activo() == null || dto.activo());
        aplicarEspecificacion(producto, dto.especificacion());
        auditoriaService.registrar(AccionAuditoria.ACTUALIZACION, usuario, "Producto", id,
                "Producto actualizado: " + producto.getNombre(), anterior, producto.toString(), solicitudHttp);
        return convertir(producto);
    }

    @Transactional
    public void desactivar(Long id, Usuario usuario, HttpServletRequest solicitudHttp) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        producto.setActivo(false);
        auditoriaService.registrar(AccionAuditoria.CAMBIO_ESTADO, usuario, "Producto", id,
                "Producto desactivado", "activo", "inactivo", solicitudHttp);
    }

    public Producto obtener(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    public ProductoDTO convertir(Producto producto) {
        EspecificacionProducto especificacion = producto.getEspecificacion();
        EspecificacionProductoDTO especificacionDTO = especificacion == null ? null : new EspecificacionProductoDTO(
                especificacion.getVoltaje(), especificacion.getCorriente(), especificacion.getPotencia(),
                especificacion.getFrecuencia(), especificacion.getFases(), especificacion.getGradoProteccion(),
                especificacion.getMaterial(), especificacion.getDimensiones(), especificacion.getPeso(),
                especificacion.getDiametro(), especificacion.getCapacidad(), especificacion.getVelocidad());
        return new ProductoDTO(producto.getId(), producto.getSku(), producto.getCodigoBarras(), producto.getNombre(), producto.getDescripcion(),
                producto.getCategoria().getId(), producto.getCategoria().getNombre(), producto.getMarca().getId(),
                producto.getMarca().getNombre(), producto.getModelo(), producto.getTipoProducto(),
                producto.getPrecioCompra(), producto.getPrecioVenta(), producto.getStock(), producto.getStockMinimo(),
                producto.getUnidadMedida(), producto.getImagenUrl(), producto.getFichaTecnicaUrl(), producto.getGarantia(),
                producto.getActivo(), producto.getFechaCreacion(), producto.getFechaActualizacion(), especificacionDTO);
    }

    private String normalizarCodigoBarras(String codigoBarras) {
        return codigoBarras == null || codigoBarras.isBlank() ? null : codigoBarras.trim();
    }

    private void validarCodigoBarrasDisponible(String codigoBarras, Long productoIdActual) {
        if (codigoBarras == null) return;
        productoRepository.findByCodigoBarrasIgnoreCase(codigoBarras)
                .filter(producto -> productoIdActual == null || !producto.getId().equals(productoIdActual))
                .ifPresent(producto -> {
                    throw new IllegalArgumentException("Ya existe un producto con ese código de barras");
                });
    }

    private void aplicarEspecificacion(Producto producto, EspecificacionProductoDTO dto) {
        if (dto == null) {
            producto.setEspecificacion(null);
            return;
        }
        EspecificacionProducto especificacion = producto.getEspecificacion();
        if (especificacion == null) {
            especificacion = new EspecificacionProducto();
            especificacion.setProducto(producto);
            producto.setEspecificacion(especificacion);
        }
        especificacion.setVoltaje(dto.voltaje());
        especificacion.setCorriente(dto.corriente());
        especificacion.setPotencia(dto.potencia());
        especificacion.setFrecuencia(dto.frecuencia());
        especificacion.setFases(dto.fases());
        especificacion.setGradoProteccion(dto.gradoProteccion());
        especificacion.setMaterial(dto.material());
        especificacion.setDimensiones(dto.dimensiones());
        especificacion.setPeso(dto.peso());
        especificacion.setDiametro(dto.diametro());
        especificacion.setCapacidad(dto.capacidad());
        especificacion.setVelocidad(dto.velocidad());
    }
}
