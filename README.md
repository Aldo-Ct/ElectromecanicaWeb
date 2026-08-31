# Sistema Electromecánica

Plataforma web con un sitio público de presentación comercial y un área interna para gestionar productos, inventario, ventas, clientes, proveedores, devoluciones, usuarios, reportes y auditoría.

La solución está compuesta por:

- Backend REST con Java 17, Spring Boot 3.2, Spring Security y JWT.
- Base de datos PostgreSQL 16 administrada con migraciones Flyway.
- Frontend con Angular 17.
- Docker Compose para levantar PostgreSQL y el backend.

## Requisitos

Para la forma de ejecución recomendada se necesita:

- Git.
- Docker Desktop, o Docker Engine con Docker Compose v2.
- Node.js `18.13+` o `20.9+`. Se recomienda Node.js 20 LTS.
- npm 8 o superior.

Para ejecutar el backend fuera de Docker también se necesita:

- JDK 17.
- Maven 3.9 o superior.

> El repositorio no incluye Maven Wrapper (`mvnw`), por lo que la ejecución nativa requiere tener `mvn` instalado.

## Puertos utilizados

| Servicio | Dirección |
|---|---|
| Frontend Angular | <http://localhost:4200> |
| Backend REST | <http://localhost:8080> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| PostgreSQL | `localhost:5432` |

Antes de comenzar, verificar que los puertos `4200`, `8080` y `5432` estén disponibles.

## Rutas principales del frontend

| Área | Ruta | Acceso |
|---|---|---|
| Sitio web institucional | `/` | Público |
| Inicio de sesión | `/acceso` | Público |
| Panel principal | `/panel` | Personal autenticado |
| Módulos administrativos | `/productos`, `/inventario`, `/ventas`, entre otros | Según rol |

El botón **Acceso restringido** del sitio público dirige a `/acceso`. El dashboard y sus módulos mantienen las protecciones de autenticación y roles existentes.

## Inicio rápido recomendado

Esta modalidad ejecuta PostgreSQL y Spring Boot en Docker, mientras Angular se ejecuta localmente.

### 1. Ubicarse en la raíz del proyecto

```bash
cd ElectromecanicaWeb
```

Los siguientes comandos deben ejecutarse desde la carpeta que contiene `docker-compose.yml` y `pom.xml`.

### 2. Definir la contraseña de los usuarios iniciales

En macOS, Linux o Git Bash:

```bash
export ELECTROMECANICA_DEFAULT_PASSWORD='CambiarEstaClave123!'
```

Esta variable se usa cuando se crean los usuarios por primera vez. Si no se define, Docker Compose usa `password123` como valor de desarrollo.

### 3. Levantar PostgreSQL y el backend

Docker Desktop debe estar iniciado antes de ejecutar:

```bash
docker compose up -d --build
docker compose ps
```

Consultar el arranque del backend:

```bash
docker compose logs -f backend
```

El backend estará listo cuando el registro muestre que la aplicación inició. Se puede salir del seguimiento de logs con `Ctrl+C`; los contenedores continuarán ejecutándose.

Durante el primer arranque:

- Flyway crea y actualiza el esquema usando las migraciones de `src/main/resources/db/migration`.
- Se crean los usuarios, catálogos y datos de demostración iniciales.
- La información se conserva en el volumen Docker `electromecanica_postgres_data`.

### 4. Levantar el frontend

En otra terminal, desde la raíz del proyecto:

```bash
cd frontend
npm ci
npm start
```

Abrir <http://localhost:4200> en el navegador. El frontend de desarrollo ya está configurado para consumir `http://localhost:8080/api`.

## Accesos iniciales

Todos los usuarios creados en una base de datos nueva usan el valor definido en `ELECTROMECANICA_DEFAULT_PASSWORD`.

| Correo | Rol |
|---|---|
| `admin@electromecanica.pe` | ADMINISTRADOR |
| `gerente@electromecanica.pe` | GERENTE |
| `ventas@electromecanica.pe` | VENTAS |
| `almacen@electromecanica.pe` | ALMACEN |
| `inventario@electromecanica.pe` | INVENTARIO |
| `soporte@electromecanica.pe` | SOPORTE |
| `analista@electromecanica.pe` | ANALISTA |

Para comprobar el acceso sin usar el frontend:

```bash
curl -X POST http://localhost:8080/api/autenticacion/acceso \
  -H 'Content-Type: application/json' \
  -d '{"correo":"admin@electromecanica.pe","contrasena":"CambiarEstaClave123!"}'
```

La respuesta correcta contiene un token JWT y los datos del usuario.

## Desarrollo del backend fuera de Docker

Esta alternativa es útil para depurar o reiniciar Spring Boot rápidamente. PostgreSQL permanece en Docker.

### 1. Levantar únicamente PostgreSQL

```bash
docker compose up -d postgres
docker compose ps
```

### 2. Configurar las variables locales

El valor predeterminado de `SPRING_DATASOURCE_URL` usa el host `postgres`, que solo existe dentro de la red de Docker. Para ejecutar Java en el equipo se debe usar `localhost`:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/electromecanica_db'
export SPRING_DATASOURCE_USERNAME='postgres'
export SPRING_DATASOURCE_PASSWORD='postgres'
export ELECTROMECANICA_DEFAULT_PASSWORD='CambiarEstaClave123!'
```

### 3. Ejecutar Spring Boot

```bash
mvn spring-boot:run
```

No se debe ejecutar simultáneamente el backend de Docker y el backend de Maven, porque ambos intentarán usar el puerto `8080`.

## Comprobaciones y pruebas

Comprobar que la API responde:

```bash
curl --fail http://localhost:8080/v3/api-docs
```

Ejecutar las pruebas del backend, con JDK 17 y Maven instalados:

```bash
mvn test
```

Validar la compilación del frontend:

```bash
cd frontend
npm ci
npm run build
```

Validar la imagen del backend sin instalar Maven localmente:

```bash
docker compose build backend
```

## Detener o reiniciar el entorno

Detener los contenedores conservando la base de datos:

```bash
docker compose down
```

Volver a iniciarlos:

```bash
docker compose up -d
```

Eliminar los contenedores y reiniciar completamente la base de datos:

```bash
docker compose down -v
```

> `docker compose down -v` elimina todos los datos locales almacenados en PostgreSQL. En el siguiente arranque se ejecutarán nuevamente las migraciones y la carga inicial.

## Variables de entorno principales

| Variable | Uso | Valor local de Docker Compose |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Conexión JDBC a PostgreSQL | `jdbc:postgresql://postgres:5432/electromecanica_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de PostgreSQL | `postgres` |
| `ELECTROMECANICA_DEFAULT_PASSWORD` | Contraseña para crear usuarios iniciales | `password123` si no se define |
| `JWT_SECRET` | Firma de los tokens JWT | Valor de desarrollo de `application.yml` |
| `PORT` | Puerto HTTP del backend | `8080` |

Los valores predeterminados son únicamente para desarrollo local. No deben reutilizarse en producción.

## Solución de problemas

### Docker no responde

Si aparece un error similar a `Cannot connect to the Docker daemon`, iniciar Docker Desktop y volver a ejecutar `docker compose up -d --build`.

### El backend no conecta a PostgreSQL

- Dentro de Docker, la URL debe usar `postgres:5432`.
- Al ejecutar el backend con Maven, la URL debe usar `localhost:5432`.
- Revisar el estado y los logs con `docker compose ps` y `docker compose logs postgres`.

### No se puede iniciar sesión después de cambiar la contraseña inicial

La variable `ELECTROMECANICA_DEFAULT_PASSWORD` solo asigna la contraseña cuando cada usuario es creado; no modifica usuarios existentes. Para empezar otra vez con una base local vacía se puede usar `docker compose down -v`, teniendo presente que elimina todos los datos.

### Un puerto ya está ocupado

Detener la aplicación que usa `4200`, `8080` o `5432`. Si ya existe un PostgreSQL local en `5432`, debe detenerse o cambiarse el puerto publicado en `docker-compose.yml` y ajustarse la URL JDBC del backend nativo.

### El navegador bloquea solicitudes por CORS

El backend permite por defecto los orígenes `http://localhost:4200` y `http://127.0.0.1:4200`. Ejecutar Angular en uno de esos hosts y puertos.

## Estructura principal

```text
.
├── src/main/java/          # Backend Spring Boot
├── src/main/resources/     # Configuración y migraciones Flyway
├── src/test/               # Pruebas del backend
├── frontend/               # Aplicación Angular
├── docker-compose.yml      # PostgreSQL y backend local
├── Dockerfile              # Construcción del backend
└── pom.xml                 # Dependencias Maven
```

## Consideraciones de base de datos y seguridad

- Flyway es responsable de los cambios de esquema.
- Hibernate usa `ddl-auto: validate`; no crea ni altera tablas automáticamente.
- Las contraseñas se almacenan como hashes BCrypt.
- La API usa autenticación JWT y sesiones sin estado.
- Swagger y el endpoint de acceso son públicos; los demás endpoints requieren autenticación y permisos según el rol.
