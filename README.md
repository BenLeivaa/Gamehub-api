# GameHub API

API REST del backend de **GameHub Store**, una tienda de videojuegos y accesorios. Desarrollada con Spring Boot para la Evaluación Parcial 3 de Desarrollo Fullstack II (DSY1104). Gestiona productos, categorías, usuarios y boletas (órdenes de compra), con autenticación JWT y control de acceso por roles.

- **Frontend (React):** https://github.com/MilyQuintana/Proyecto_FullStack_2_React.git
- **Documentación Swagger (local):** http://localhost:8080/swagger-ui.html

## Integrantes: 

Emily Pacheco / Benjamin Leiva


## Tecnologías

- Java 25 y Spring Boot 4.0.10
- Spring Web MVC, Spring Data JPA, Spring Security y Bean Validation
- PostgreSQL (Supabase)
- JWT con jjwt 0.12.6 (HS256)
- Swagger / OpenAPI con springdoc 3.0.3
- Lombok
- JUnit 5 y Mockito para las pruebas
- Maven (se incluye el wrapper `mvnw`)

## Requisitos

- JDK instalado (la versión indicada en `pom.xml`)
- Una base de datos PostgreSQL accesible (por ejemplo, un proyecto de Supabase)

No necesitas instalar Maven: el proyecto trae `mvnw` (Linux/macOS) y `mvnw.cmd` (Windows).

## Variables de entorno

| Variable | Obligatoria | Descripción | Valor por defecto |
|----------|-------------|-------------|-------------------|
| `DB_USUARIO` | Sí | Usuario de la base de datos | — |
| `DB_CLAVE` | Sí | Contraseña de la base de datos | — |
| `DB_URL` | No | URL JDBC de PostgreSQL | Pooler de Supabase (ver `application.properties`) |
| `JWT_SECRETO` | En producción, sí | Clave para firmar los tokens (mínimo 32 caracteres) | Clave de desarrollo |
| `CORS_ORIGENES` | No | Orígenes permitidos del frontend, separados por coma | `http://localhost:5173` |
| `PORT` | No | Puerto del servidor | `8080` |

> En producción define siempre `JWT_SECRETO` con una clave larga y aleatoria. La clave por defecto es solo para desarrollo.

## Cómo ejecutar

1. Clona el repositorio y entra a la carpeta del proyecto.
2. Define las variables de entorno.

   Linux / macOS:
   ```bash
   export DB_USUARIO="tu_usuario"
   export DB_CLAVE="tu_clave"
   export CORS_ORIGENES="http://localhost:5173"
   ```

   Windows (PowerShell):
   ```powershell
   $env:DB_USUARIO="tu_usuario"
   $env:DB_CLAVE="tu_clave"
   $env:CORS_ORIGENES="http://localhost:5173"
   ```

3. Inicia la API:
   ```bash
   ./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
   ```
4. Abre http://localhost:8080/swagger-ui.html para probar los endpoints.

Al arrancar por primera vez, si las tablas están vacías, se cargan datos de prueba (usuarios, categorías y productos). Las tablas se crean automáticamente (`spring.jpa.hibernate.ddl-auto=update`).

## Usuarios de prueba

| Rol | Email | Contraseña |
|-----|-------|------------|
| Administrador | admin@gamehub.cl | Admin1234 |
| Vendedor | vendedor@gamehub.cl | Vendedor1234 |
| Cliente | cliente@gamehub.cl | Cliente1234 |

## Pruebas

Las pruebas unitarias cubren la lógica de negocio de los servicios (compras, stock, login, registro, productos y categorías). Usan Mockito, por lo que no necesitan base de datos.

```bash
./mvnw test        # Windows: mvnw.cmd test
```

## Autenticación y roles

La API usa JWT. El flujo es:

1. `POST /api/v1/auth/login` con email y contraseña devuelve un token.
2. En las siguientes peticiones se envía el header `Authorization: Bearer <token>`.
3. El token dura 1 hora. Sin token o con token inválido, la API responde `401`; con un rol sin permiso, responde `403`.

En Swagger, usa el botón **Authorize** y pega el token para probar los endpoints protegidos.

| Rol | Permisos |
|-----|----------|
| **ADMINISTRADOR** | Acceso total: usuarios, productos, categorías y todas las boletas. Puede cancelar boletas. |
| **VENDEDOR** | Solo ve la lista de productos y su detalle, y la lista de boletas y su detalle. |
| **CLIENTE** | Solo accede a la tienda: ver productos y categorías, comprar y ver sus propias compras. |

El registro público (`/api/v1/auth/register`) siempre crea una cuenta con rol CLIENTE. Solo el administrador puede crear usuarios con otros roles.

## Endpoints

Todas las rutas llevan el prefijo de versión `/api/v1`.

### Autenticación

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/auth/register` | Público | Registra un cliente |
| POST | `/auth/login` | Público | Inicia sesión y devuelve el token |

### Productos

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/productos` | Público | Lista productos (filtros opcionales `categoriaId` y `marca`) |
| GET | `/productos/{id}` | Público | Detalle de un producto |
| POST | `/productos` | Administrador | Crea un producto |
| PUT | `/productos/{id}` | Administrador | Actualiza un producto |
| DELETE | `/productos/{id}` | Administrador | Desactiva un producto (baja lógica) |

Visitantes y clientes ven solo los productos activos; administrador y vendedor ven todos.

### Categorías

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/categorias` | Público | Lista categorías |
| GET | `/categorias/{id}` | Público | Detalle de una categoría |
| POST | `/categorias` | Administrador | Crea una categoría |
| PUT | `/categorias/{id}` | Administrador | Actualiza una categoría |
| DELETE | `/categorias/{id}` | Administrador | Desactiva una categoría (no permite si tiene productos activos) |

### Usuarios

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/usuarios` | Administrador | Lista usuarios (filtro opcional `rol`) |
| GET | `/usuarios/{id}` | Administrador | Detalle de un usuario |
| POST | `/usuarios` | Administrador | Crea un usuario con cualquier rol |
| PUT | `/usuarios/{id}` | Administrador | Actualiza un usuario |
| DELETE | `/usuarios/{id}` | Administrador | Desactiva un usuario (baja lógica) |

### Boletas (órdenes)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/boletas` | Cliente, Administrador | Crea una compra: valida stock, calcula el total y descuenta el inventario |
| GET | `/boletas` | Administrador, Vendedor | Lista todas las boletas |
| GET | `/boletas/mis-compras` | Cliente, Administrador | Boletas del usuario autenticado |
| GET | `/boletas/{id}` | Autenticado | Detalle; el cliente solo ve las suyas |
| PATCH | `/boletas/{id}/cancelar` | Administrador | Cancela la boleta y devuelve el stock |

### Códigos de respuesta

| Código | Significado |
|--------|-------------|
| 200 / 201 / 204 | Éxito, creado, sin contenido |
| 400 | Datos inválidos o regla de negocio incumplida (p. ej. stock insuficiente) |
| 401 | Sin token, token inválido o credenciales incorrectas |
| 403 | El rol no tiene permiso |
| 404 | Recurso no encontrado |

Los errores se devuelven en JSON con `timestamp`, `status` y `error` (y `detalles` cuando hay errores de validación).

## Modelo de datos

Cinco tablas relacionales:

```
usuarios ──< boletas ──< detalles_boleta >── productos >── categorias
```

| Tabla | Descripción |
|-------|-------------|
| `usuarios` | Datos de la cuenta, rol (ADMINISTRADOR, VENDEDOR, CLIENTE), estado y dirección |
| `categorias` | Categorías de productos (nombre único) |
| `productos` | Productos con precio, stock, marca, modelo, imagen y categoría |
| `boletas` | Compra de un usuario: fecha, total y estado (PENDIENTE, PAGADA, CANCELADA) |
| `detalles_boleta` | Líneas de cada boleta: producto, cantidad, precio unitario y subtotal |

El precio de cada producto se guarda en el detalle al momento de la compra, y las bajas son lógicas (campo `estado`) para no perder el historial de boletas.

## Estructura del proyecto

```
src/main/java/com/gamehub/api/
├── config/       Seguridad, CORS, Swagger y datos iniciales
├── controller/   Controladores REST
├── dto/          Objetos de entrada y salida (con validaciones)
├── exception/    Excepciones y manejo global de errores
├── model/        Entidades JPA y enums
├── repository/   Repositorios Spring Data
├── security/     Generación y validación de JWT
└── service/      Lógica de negocio
src/test/         Pruebas unitarias de los servicios
```

## Flujo de trabajo en Git

- La rama `main` contiene la versión estable.
- Los cambios se hacen en ramas de trabajo y se integran a `main` con mensajes de commit claros.
- Las tareas se reparten entre los integrantes (ver la tabla de integrantes).
