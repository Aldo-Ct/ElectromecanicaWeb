package com.electromecanica.app.config;

import com.electromecanica.app.entity.*;
import com.electromecanica.app.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class InicializadorDatos {
    @Value("${ELECTROMECANICA_DEFAULT_PASSWORD:}")
    private String contrasenaInicial;

    @Bean
    CommandLineRunner inicializar(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                                  MarcaRepository marcaRepository, ProveedorRepository proveedorRepository,
                                  ClienteRepository clienteRepository, ProductoRepository productoRepository,
                                  LoteRepository loteRepository, MovimientoInventarioRepository movimientoRepository,
                                  PasswordEncoder codificadorContrasena) {
        return argumentos -> {
            String contrasena = contrasenaInicial == null ? "" : contrasenaInicial.trim();
            if (contrasena.isBlank() && usuarioRepository.count() == 0) {
                throw new IllegalStateException("Defina ELECTROMECANICA_DEFAULT_PASSWORD para crear los usuarios iniciales");
            }
            if (!contrasena.isBlank()) {
                crearUsuarioSiFalta(usuarioRepository, codificadorContrasena, "Administrador", "General",
                        "admin@electromecanica.pe", contrasena, Rol.ADMINISTRADOR);
                crearUsuarioSiFalta(usuarioRepository, codificadorContrasena, "Gerente", "General",
                        "gerente@electromecanica.pe", contrasena, Rol.GERENTE);
                crearUsuarioSiFalta(usuarioRepository, codificadorContrasena, "Asesor", "Ventas",
                        "ventas@electromecanica.pe", contrasena, Rol.VENTAS);
                crearUsuarioSiFalta(usuarioRepository, codificadorContrasena, "Encargado", "Almacén",
                        "almacen@electromecanica.pe", contrasena, Rol.ALMACEN);
                crearUsuarioSiFalta(usuarioRepository, codificadorContrasena, "Supervisor", "Inventario",
                        "inventario@electromecanica.pe", contrasena, Rol.INVENTARIO);
                crearUsuarioSiFalta(usuarioRepository, codificadorContrasena, "Especialista", "Soporte",
                        "soporte@electromecanica.pe", contrasena, Rol.SOPORTE);
                crearUsuarioSiFalta(usuarioRepository, codificadorContrasena, "Analista", "Datos",
                        "analista@electromecanica.pe", contrasena, Rol.ANALISTA);
            }

            List<String> categorias = List.of("Motores eléctricos", "Contactores", "Interruptores",
                    "Transformadores", "Variadores de frecuencia", "Rodamientos", "Bombas", "Reductores",
                    "Válvulas", "Herramientas");
            categorias.forEach(nombre -> categoriaRepository.findByNombreIgnoreCase(nombre).orElseGet(() ->
                    categoriaRepository.save(Categoria.builder().nombre(nombre)
                            .descripcion("Categoría para " + nombre.toLowerCase()).activo(true).build())));

            List<String> marcas = List.of("Siemens", "ABB", "Schneider Electric", "WEG", "SKF", "Bosch", "Festo");
            marcas.forEach(nombre -> marcaRepository.findByNombreIgnoreCase(nombre).orElseGet(() ->
                    marcaRepository.save(Marca.builder().nombre(nombre)
                            .descripcion("Fabricante de equipos y componentes industriales").activo(true).build())));

            Proveedor proveedor = proveedorRepository.findByRuc("20123456789").orElseGet(() ->
                    proveedorRepository.save(Proveedor.builder().ruc("20123456789")
                            .razonSocial("Suministros Electromecánicos del Perú S.A.C.")
                            .nombreContacto("Central de pedidos").correo("pedidos@suministros.pe")
                            .telefono("+51 999 111 222").direccion("Lima, Perú").activo(true).build()));

            clienteRepository.findByNumeroDocumento("20601234567").orElseGet(() ->
                    clienteRepository.save(Cliente.builder().tipoDocumento(TipoDocumento.RUC)
                            .numeroDocumento("20601234567").razonSocial("Industrias Andinas S.A.C.")
                            .correo("compras@industriasandinas.pe").telefono("+51 999 333 444")
                            .direccion("Lima, Perú").activo(true).build()));

            Usuario administrador = usuarioRepository.findByCorreoIgnoreCase("admin@electromecanica.pe").orElse(null);
            if (administrador != null && productoRepository.count() == 0) {
                crearProductoInicial(productoRepository, loteRepository, movimientoRepository, proveedor, administrador,
                        categoriaRepository, marcaRepository, "MOT-WEG-3HP", "Motor eléctrico trifásico 3 HP",
                        "Motor industrial de alta eficiencia para servicio continuo", "Motores eléctricos", "WEG",
                        "W22", TipoProducto.ELECTRICO, "220/380 V", "3 HP", null, new BigDecimal("1250.00"),
                        new BigDecimal("1690.00"), 12);
                crearProductoInicial(productoRepository, loteRepository, movimientoRepository, proveedor, administrador,
                        categoriaRepository, marcaRepository, "ROD-SKF-6205", "Rodamiento rígido 6205-2RSH",
                        "Rodamiento sellado para aplicaciones industriales", "Rodamientos", "SKF", "6205-2RSH",
                        TipoProducto.MECANICO, null, null, "25 mm", new BigDecimal("38.00"),
                        new BigDecimal("59.90"), 40);
                crearProductoInicial(productoRepository, loteRepository, movimientoRepository, proveedor, administrador,
                        categoriaRepository, marcaRepository, "CON-SIE-18A", "Contactor industrial 18 A",
                        "Contactor de potencia para maniobra de motores", "Contactores", "Siemens", "3RT2025",
                        TipoProducto.ELECTRICO, "220 V", null, null, new BigDecimal("145.00"),
                        new BigDecimal("215.00"), 20);
            }
        };
    }

    private void crearUsuarioSiFalta(UsuarioRepository repositorio, PasswordEncoder codificador, String nombre,
                                     String apellido, String correo, String contrasena, Rol rol) {
        repositorio.findByCorreoIgnoreCase(correo).orElseGet(() -> repositorio.save(Usuario.builder()
                .nombre(nombre).apellido(apellido).correo(correo)
                .contrasena(codificador.encode(contrasena)).rol(rol).activo(true).build()));
    }

    private void crearProductoInicial(ProductoRepository productoRepository, LoteRepository loteRepository,
                                      MovimientoInventarioRepository movimientoRepository, Proveedor proveedor,
                                      Usuario usuario, CategoriaRepository categoriaRepository, MarcaRepository marcaRepository,
                                      String sku, String nombre, String descripcion, String categoriaNombre, String marcaNombre,
                                      String modelo, TipoProducto tipo, String voltaje, String potencia, String diametro,
                                      BigDecimal precioCompra, BigDecimal precioVenta, int cantidad) {
        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(categoriaNombre).orElseThrow();
        Marca marca = marcaRepository.findByNombreIgnoreCase(marcaNombre).orElseThrow();
        Producto producto = Producto.builder().sku(sku).nombre(nombre).descripcion(descripcion).categoria(categoria)
                .marca(marca).modelo(modelo).tipoProducto(tipo).precioCompra(precioCompra).precioVenta(precioVenta)
                .stock(cantidad).stockMinimo(5).unidadMedida(UnidadMedida.UNIDAD).garantia("12 meses")
                .activo(true).publicadoVenta(true).build();
        EspecificacionProducto especificacion = EspecificacionProducto.builder().producto(producto).voltaje(voltaje)
                .potencia(potencia).diametro(diametro).build();
        producto.setEspecificacion(especificacion);
        productoRepository.save(producto);
        String codigoLote = "LOT-INICIAL-" + sku;
        loteRepository.save(Lote.builder().producto(producto).proveedor(proveedor).codigoLote(codigoLote)
                .cantidad(cantidad).precioCompra(precioCompra).observaciones("Ingreso inicial de demostración").build());
        movimientoRepository.save(MovimientoInventario.builder().producto(producto).tipo(TipoMovimientoInventario.ENTRADA)
                .cantidad(cantidad).referencia(codigoLote).usuario(usuario)
                .observaciones("Carga inicial del sistema").build());
    }
}
