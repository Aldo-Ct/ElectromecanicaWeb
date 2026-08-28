# Sistema Electromecánica

Sistema web interno para la venta, el inventario y la gestión de equipos eléctricos y mecánicos. La solución integra Angular, Spring Boot, Spring Security, JWT, PostgreSQL, generación de comprobantes PDF, auditoría y Docker.

## Módulos internos

- Panel principal con indicadores operativos.
- Productos con categorías, marcas y especificaciones eléctricas o mecánicas.
- Inventario con ingresos por lote, ajustes y trazabilidad de movimientos.
- Almacén y proveedores.
- Ventas, clientes y comprobantes PDF.
- Devoluciones con reposición automática de stock.
- Usuarios y roles.
- Reportes y auditoría.

Esta fase no incluye sitio web público, catálogo público, contacto ni páginas institucionales.

## Tecnologías

- Java 17 y Spring Boot 3.2
- Spring Data JPA e Hibernate
- Spring Security, JWT y BCrypt
- PostgreSQL 16 y Flyway
- Angular 17
- Docker Compose
- iText para PDF

## Ejecución con Docker

```bash
docker compose down
docker compose up -d --build
docker compose ps
docker compose logs --tail=150 backend
```

Servicios esperados:

- `electromecanica-db`: PostgreSQL, puerto `5432`, estado saludable.
- `electromecanica-backend`: REST API, puerto `8080`.

La contraseña inicial se configura mediante `ELECTROMECANICA_DEFAULT_PASSWORD`. El valor predeterminado de desarrollo en `docker-compose.yml` es `password123`; debe reemplazarse en un entorno real.

## Accesos iniciales

| Correo | Rol |
|---|---|
| `admin@electromecanica.pe` | ADMINISTRADOR |
| `gerente@electromecanica.pe` | GERENTE |
| `ventas@electromecanica.pe` | VENTAS |
| `almacen@electromecanica.pe` | ALMACEN |
| `inventario@electromecanica.pe` | INVENTARIO |
| `soporte@electromecanica.pe` | SOPORTE |
| `analista@electromecanica.pe` | ANALISTA |

## Endpoints principales

| Área | Ruta base |
|---|---|
| Autenticación | `/api/autenticacion` |
| Productos | `/api/productos` |
| Categorías | `/api/categorias` |
| Marcas | `/api/marcas` |
| Clientes | `/api/clientes` |
| Proveedores | `/api/proveedores` |
| Inventario | `/api/inventario` |
| Movimientos | `/api/inventario/movimientos` |
| Lotes | `/api/inventario/lotes` |
| Ventas | `/api/ventas` |
| Devoluciones | `/api/devoluciones` |
| Usuarios | `/api/usuarios` |
| Reportes | `/api/reportes` |
| Auditoría | `/api/auditoria` |

La documentación OpenAPI está disponible en `/swagger-ui.html`.

## Frontend Angular

```bash
cd frontend
npm install
npm run build
npm start
```

Rutas internas: `/acceso`, `/panel`, `/productos`, `/categorias`, `/marcas`, `/inventario`, `/almacen`, `/ventas`, `/clientes`, `/proveedores`, `/devoluciones`, `/usuarios`, `/reportes` y `/auditoria`.

## Base de datos

Flyway ejecuta la migración `V1__dominio_electromecanica.sql`. El esquema utiliza nombres funcionales en español: `productos`, `categorias`, `marcas`, `clientes`, `proveedores`, `lotes`, `movimientos_inventario`, `ventas`, `detalles_venta`, `comprobantes_venta`, `devoluciones`, `detalles_devolucion`, `usuarios` y `auditorias`.

Hibernate funciona en modo `validate`; los cambios estructurales deben agregarse mediante nuevas migraciones Flyway.

## Seguridad

- El acceso del personal utiliza correo y contraseña.
- Las contraseñas se persisten únicamente como hashes BCrypt.
- La REST API utiliza JWT y sesiones sin estado.
- Los permisos se aplican según los roles en español.
- Las operaciones relevantes producen registros de auditoría.
