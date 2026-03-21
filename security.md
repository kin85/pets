# 🔐 SEGURIDAD DE LA APLICACIÓN - EXPLICACIÓN SENCILLA

## 📚 ÍNDICE
1. [Las 5 Clases de Seguridad y su Función](#clases)
2. [Flujo Completo: Registro](#flujo-registro)
3. [Flujo Completo: Login](#flujo-login)
4. [Flujo Completo: Acceso a Endpoint Protegido](#flujo-protegido)
5. [Explicación de DDL-AUTO](#ddl-auto)

---

<a name="clases"></a>
## 🎭 LAS 5 CLASES DE SEGURIDAD Y SU FUNCIÓN

### **1. SecurityConfig.java** - 🏛️ "El Arquitecto del Edificio"

**¿Qué hace?**
Es la clase que **diseña y configura todas las reglas de seguridad** de tu aplicación. Define:
- Qué rutas son públicas y cuáles requieren autenticación
- Cómo se encriptan las contraseñas
- Cómo se manejan los permisos CORS (permitir acceso desde otros dominios)

**Analogía:** Es como el arquitecto que diseña un edificio y dice:
- "La recepción (login/register) es pública, cualquiera puede entrar"
- "Las oficinas del piso 2 (/api/**) necesitan tarjeta de acceso"
- "Las personas del edificio de al lado (localhost:4200) pueden visitarnos"

**Configuraciones importantes:**

```java
// 1. ENCRIPTADOR DE CONTRASEÑAS
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
📝 **Explicación:** BCrypt toma tu contraseña (ej: "mipass123") y la convierte en algo como:
`$2a$10$N9qo8uLOickgx2ZMRZoMye...` (irreversible, muy seguro)

```java
// 2. REGLAS DE ACCESO
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**", "/error").permitAll()  // ✅ Público
    .anyRequest().authenticated()                        // 🔒 Todo lo demás requiere login
)
```
📝 **Explicación:**
- `/auth/login` y `/auth/register` → Cualquiera puede acceder (sin token)
- `/api/owners/123` → Necesitas token JWT válido

```java
// 3. SESIONES DESHABILITADAS (STATELESS)
.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```
📝 **Explicación:** El servidor NO guarda información de quién está logueado. Cada petición debe traer su "tarjeta de acceso" (token JWT). Esto hace que sea escalable (puedes tener 100 servidores y todos funcionan igual).

```java
// 4. CORS - Permitir acceso desde Angular
config.setAllowedOrigins(List.of("http://localhost:4200"));
```
📝 **Explicación:** Por defecto, los navegadores bloquean peticiones desde un dominio a otro (ej: localhost:4200 → localhost:8080). Esto lo permite explícitamente.

---

### **2. JwtService.java** - 🎫 "El Fabricante de Tarjetas de Acceso"

**¿Qué hace?**
Crea y valida los **tokens JWT** (las tarjetas de acceso digitales).

**Analogía:** Es como la máquina que:
- **Imprime** tarjetas de acceso cuando te logueas
- **Verifica** que la tarjeta sea válida y no esté vencida cuando entras

**Métodos principales:**

```java
// CREAR TOKEN
public String generateToken(UserDetails user) {
    Instant now = Instant.now();
    Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES); // +120 minutos
    
    return Jwts.builder()
        .subject(user.getUsername())           // 👤 "joaquin"
        .issuedAt(Date.from(now))              // 📅 Fecha de creación
        .expiration(Date.from(exp))            // ⏰ Fecha de expiración
        .claim("roles", [...])                 // 🎭 Roles del usuario
        .signWith(key(), Jwts.SIG.HS256)       // 🔐 Firma con clave secreta
        .compact();
}
```

**¿Qué es un token JWT?**
Es un texto largo que contiene información **encriptada** y **firmada**:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FxdWluIiwiaWF0IjoxNzA5MDAwMDAwLCJleHAiOjE3MDkwMDcyMDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

Si alguien lo modifica, la firma no coincidirá y será rechazado.

```java
// VALIDAR TOKEN
public boolean isTokenValid(String token, UserDetails user) {
    String username = extractUsername(token);
    return username.equals(user.getUsername()) && !isExpired(token);
}
```

📝 **Verificaciones:**
1. ¿El usuario del token coincide con el usuario que dice ser?
2. ¿El token sigue vigente (no expiró)?

---

### **3. JwtAuthenticationFilter.java** - 🚪 "El Guardia de Seguridad"

**¿Qué hace?**
Es un **filtro** que intercepta **TODAS** las peticiones HTTP antes de que lleguen a tus controladores.

**Analogía:** Es el guardia de seguridad en la entrada que:
1. Te pide tu tarjeta de acceso
2. La verifica
3. Si es válida, te deja pasar
4. Si no, te echa

**Flujo de ejecución:**

```java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    
    // 1️⃣ BUSCAR EL TOKEN EN LOS HEADERS
    String authHeader = request.getHeader("Authorization");
    // Ejemplo: "Bearer eyJhbGciOiJIUzI1NiIsInR5..."
    
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        // ❌ No hay token → seguir sin autenticar
        filterChain.doFilter(request, response);
        return;
    }
    
    // 2️⃣ EXTRAER EL TOKEN (quitar "Bearer ")
    String token = authHeader.substring(7);
    
    // 3️⃣ EXTRAER EL USERNAME DEL TOKEN
    String username = jwtService.extractUsername(token); // "joaquin"
    
    // 4️⃣ CARGAR EL USUARIO DE LA BASE DE DATOS
    UserDetails user = userDetailsService.loadUserByUsername(username);
    
    // 5️⃣ VALIDAR EL TOKEN
    if (jwtService.isTokenValid(token, user)) {
        // ✅ Token válido → AUTENTICAR AL USUARIO
        var auth = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        // Ahora Spring sabe quién eres y qué permisos tienes
    }
    
    // 6️⃣ CONTINUAR CON LA PETICIÓN
    filterChain.doFilter(request, response);
}
```

**Importante:** Este filtro se ejecuta **ANTES** de que llegue a tu controlador. Si el token es inválido, Spring Security automáticamente responde con `401 Unauthorized`.

---

### **4. CustomUserDetailService.java** - 📖 "El Directorio de Empleados"

**¿Qué hace?**
Busca la información del usuario en la **base de datos** cuando Spring Security necesita verificar credenciales.

**Analogía:** Es como el sistema de recursos humanos que tiene la lista de todos los empleados y sus permisos.

```java
@Override
public UserDetails loadUserByUsername(String username) {
    
    // 1️⃣ BUSCAR USUARIO EN BD
    var user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    // 2️⃣ CONVERTIR ROLES A FORMATO DE SPRING SECURITY
    var authorities = user.getRoles().stream()
        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
        .toList();
    // Ejemplo: Role{name="USER"} → "ROLE_USER"
    
    // 3️⃣ RETORNAR OBJETO UserDetails DE SPRING
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),    // "joaquin"
        user.getPassword(),    // "$2a$10$N9qo..." (encriptada)
        authorities            // ["ROLE_USER"]
    );
}
```

**¿Cuándo se usa?**
- En el **login**: Para verificar que username y password coincidan
- En cada **petición con token**: Para cargar permisos del usuario

---

### **5. AuthServiceImpl.java** - 🎬 "El Coordinador de Operaciones"

**¿Qué hace?**
Coordina los procesos de **registro** y **login**, usando todas las clases anteriores.

**Analogía:** Es como el gerente que coordina:
- Recursos humanos (base de datos)
- El fabricante de tarjetas (JwtService)
- El encriptador de contraseñas (PasswordEncoder)
- El sistema de autenticación (AuthenticationManager)

#### **REGISTRO (método `save`)**

```java
@Override
public Long save(UserRegisterDto userRegisterDto) {
    
    // ✅ 1. VALIDAR QUE NO EXISTA
    if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }
    
    // ✅ 2. BUSCAR ROL "USER"
    Role userRole = roleRepository.findByName("USER")
        .orElseThrow(...);
    
    // ✅ 3. CREAR USUARIO CON CONTRASEÑA ENCRIPTADA
    User user = new User();
    user.setUsername(dto.getUsername());
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword())); // 🔐 BCrypt
    user.getRoles().add(userRole);
    user = userRepository.save(user);
    
    // ✅ 4. CREAR OWNER VINCULADO
    Owner owner = new Owner();
    owner.setUser(user);
    owner.setName(dto.getName());
    owner.setAddress(dto.getAddress());
    owner.setPhone(dto.getPhone());
    ownerRepository.save(owner);
    
    return user.getId();
}
```

#### **LOGIN (método `login`)**

```java
@Override
public LoginResponse login(LoginRequest req) {
    
    // ✅ 1. AUTENTICAR CON SPRING SECURITY
    var auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            req.username(), 
            req.password()
        )
    );
    // Aquí Spring verifica automáticamente:
    // - Que el usuario exista (llama a CustomUserDetailService)
    // - Que la contraseña coincida (compara con BCrypt)
    
    // ✅ 2. OBTENER USUARIO AUTENTICADO
    var springUser = (UserDetails) auth.getPrincipal();
    
    // ✅ 3. BUSCAR USER Y OWNER EN BD
    User user = userRepository.findByUsername(springUser.getUsername())
        .orElseThrow(...);
    
    Owner owner = ownerRepository.findByUserId(user.getId())
        .orElseThrow(...);
    
    // ✅ 4. GENERAR TOKEN JWT
    String token = jwtService.generateToken(springUser);
    
    // ✅ 5. RETORNAR TOKEN + ID DEL OWNER
    return new LoginResponse(token, owner.getId());
}
```

---

<a name="flujo-registro"></a>
## 🔄 FLUJO COMPLETO: REGISTRO DE USUARIO

```
FRONTEND (Angular)
    │
    │ POST /auth/register
    │ Body: {
    │   username: "joaquin",
    │   email: "joaquin@mail.com",
    │   password: "mipass123",
    │   name: "Joaquín Pérez",
    │   address: "Calle 123",
    │   phone: "555-1234"
    │ }
    ▼
┌─────────────────────────────────────────────┐
│  JwtAuthenticationFilter (GUARDIA)          │
│  - Verifica token en header                 │
│  - No hay token → OK (ruta pública)         │
└─────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────┐
│  AuthController.register()                  │
│  - Recibe el DTO                            │
│  - Llama a authService.save(dto)            │
└─────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────┐
│  AuthServiceImpl.save()                     │
│                                             │
│  1. ¿Username existe?                       │
│     → userRepository.findByUsername()       │
│     → Si existe: ERROR 409 CONFLICT         │
│                                             │
│  2. ¿Email existe?                          │
│     → userRepository.findByEmail()          │
│     → Si existe: ERROR 409 CONFLICT         │
│                                             │
│  3. Buscar rol "USER"                       │
│     → roleRepository.findByName("USER")     │
│                                             │
│  4. Encriptar contraseña                    │
│     → passwordEncoder.encode("mipass123")   │
│     → Resultado: "$2a$10$N9qo8uLO..."      │
│                                             │
│  5. Crear User en BD                        │
│     → userRepository.save(user)             │
│                                             │
│  6. Crear Owner en BD (vinculado a User)   │
│     → ownerRepository.save(owner)           │
│                                             │
│  7. Retornar ID del usuario                 │
└─────────────────────────────────────────────┘
    │
    ▼
RESPUESTA AL FRONTEND
    200 OK
    Body: { id: 1 }
```

---

<a name="flujo-login"></a>
## 🔄 FLUJO COMPLETO: LOGIN

```
FRONTEND (Angular)
    │
    │ POST /auth/login
    │ Body: {
    │   username: "joaquin",
    │   password: "mipass123"
    │ }
    ▼
┌─────────────────────────────────────────────┐
│  JwtAuthenticationFilter (GUARDIA)          │
│  - Verifica token en header                 │
│  - No hay token → OK (ruta pública)         │
└─────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────┐
│  AuthController.login()                     │
│  - Recibe LoginRequest                      │
│  - Llama a authService.login(req)           │
└─────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────┐
│  AuthServiceImpl.login()                    │
│                                             │
│  1. AUTENTICAR                              │
│     authenticationManager.authenticate()    │
│         │                                   │
│         ├─→ Llama a CustomUserDetailService │
│         │   → loadUserByUsername()          │
│         │   → Busca en BD                   │
│         │   → Retorna UserDetails con      │
│         │     password encriptada           │
│         │                                   │
│         └─→ Compara passwords con BCrypt    │
│             • Entrada: "mipass123"          │
│             • BD: "$2a$10$N9qo8uLO..."     │
│             • BCrypt verifica: ✅ MATCH     │
│                                             │
│  2. OBTENER USUARIO AUTENTICADO             │
│     springUser = auth.getPrincipal()        │
│                                             │
│  3. BUSCAR USER Y OWNER EN BD               │
│     user = userRepository.findByUsername()  │
│     owner = ownerRepository.findByUserId()  │
│                                             │
│  4. GENERAR TOKEN JWT                       │
│     token = jwtService.generateToken()      │
│     → Crea token firmado con:               │
│       • username: "joaquin"                 │
│       • roles: ["ROLE_USER"]                │
│       • expiration: ahora + 120 minutos     │
│     Resultado:                              │
│     "eyJhbGciOiJIUzI1NiIsInR5cCI..."       │
│                                             │
│  5. RETORNAR TOKEN + ID OWNER               │
└─────────────────────────────────────────────┘
    │
    ▼
RESPUESTA AL FRONTEND
    200 OK
    Body: {
      token: "eyJhbGciOiJIUzI1NiIsInR5cCI...",
      id: 1
    }

FRONTEND guarda el token en:
    - localStorage
    - sessionStorage
    - memoria
```

---

<a name="flujo-protegido"></a>
## 🔄 FLUJO COMPLETO: ACCESO A ENDPOINT PROTEGIDO

```
FRONTEND (Angular)
    │
    │ GET /api/owners/1/home
    │ Headers: {
    │   Authorization: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI..."
    │ }
    ▼
┌──────────────────────────────────────────────────────────┐
│  JwtAuthenticationFilter (GUARDIA) 🚪                     │
│                                                           │
│  1. Extraer header "Authorization"                       │
│     → "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI..."           │
│                                                           │
│  2. Verificar formato                                    │
│     → ¿Empieza con "Bearer "? ✅                         │
│                                                           │
│  3. Extraer token (quitar "Bearer ")                     │
│     → token = "eyJhbGciOiJIUzI1NiIsInR5cCI..."          │
│                                                           │
│  4. Decodificar token                                    │
│     jwtService.extractUsername(token)                    │
│     → Lee el token y extrae: username = "joaquin"        │
│                                                           │
│  5. Cargar usuario desde BD                              │
│     userDetailsService.loadUserByUsername("joaquin")     │
│         │                                                │
│         └─→ CustomUserDetailService                      │
│             → userRepository.findByUsername("joaquin")   │
│             → Retorna UserDetails con roles              │
│                                                           │
│  6. Validar token                                        │
│     jwtService.isTokenValid(token, user)                 │
│     → ¿Username coincide? ✅                             │
│     → ¿Token NO expiró? ✅                               │
│                                                           │
│  7. AUTENTICAR AL USUARIO EN EL CONTEXTO                 │
│     SecurityContextHolder.getContext()                   │
│       .setAuthentication(auth)                           │
│     → Ahora Spring sabe:                                 │
│       • Quién eres: "joaquin"                            │
│       • Qué permisos tienes: ["ROLE_USER"]               │
│                                                           │
│  8. Continuar con la petición                            │
└──────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────┐
│  SecurityConfig verifica permisos            │
│  → /api/** requiere .authenticated()        │
│  → Usuario está autenticado ✅              │
│  → PERMITIR ACCESO                          │
└─────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────┐
│  OwnerController.getOwnerHome(id)           │
│  - Recibe el ID: 1                          │
│  - Llama a ownerService.getOwnerHome(1)     │
└─────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────┐
│  OwnerServiceImpl.getOwnerHome()            │
│  - Busca Owner en BD                        │
│  - Busca Dogs del Owner                     │
│  - Retorna OwnerHomeDto                     │
└─────────────────────────────────────────────┘
    │
    ▼
RESPUESTA AL FRONTEND
    200 OK
    Body: {
      name: "Joaquín Pérez",
      dogs: [
        { id: 1, name: "Rex" },
        { id: 2, name: "Max" }
      ]
    }

---

SI EL TOKEN ES INVÁLIDO O EXPIRÓ:
    │
    ▼
JwtAuthenticationFilter detecta token inválido
    │
    ▼
SecurityConfig retorna error automáticamente
    │
    ▼
RESPUESTA AL FRONTEND
    401 UNAUTHORIZED
    { error: "Unauthorized" }
```

---

<a name="ddl-auto"></a>
## 🗄️ EXPLICACIÓN: spring.jpa.hibernate.ddl-auto=none

### **¿Qué es DDL-AUTO?**

DDL significa **"Data Definition Language"** (Lenguaje de Definición de Datos). Es el conjunto de comandos SQL que **crea, modifica o elimina tablas** en la base de datos.

Ejemplo de DDL:
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);
```

### **¿Qué hace `spring.jpa.hibernate.ddl-auto`?**

Le dice a **Hibernate** (el ORM de Spring) qué hacer con las tablas de la base de datos cuando arranca la aplicación.

### **OPCIONES DISPONIBLES**

| Valor | ¿Qué hace? | Cuándo usarlo |
|-------|------------|---------------|
| **none** | ❌ NO hace nada. Hibernate NO toca la BD | **Producción** - Tienes control total |
| **validate** | ✅ Solo valida que las tablas coincidan con las entidades (NO crea/modifica) | **Producción** - Valida sin modificar |
| **update** | 🔄 Actualiza las tablas si cambias una entidad (agrega columnas, NO elimina) | **Desarrollo** - Cómodo pero peligroso |
| **create** | 🔥 **BORRA todas las tablas y las crea de nuevo** cada vez que arrancas | **Tests** - Pierdes todos los datos |
| **create-drop** | 🔥🔥 Crea al iniciar y **BORRA TODO al cerrar** la aplicación | **Tests temporales** |

---

### **TU CONFIGURACIÓN: `ddl-auto=none`**

```properties
spring.jpa.hibernate.ddl-auto=none
```

**¿Qué significa?**
Hibernate **NO creará ni modificará las tablas** automáticamente. Tú eres responsable de crearlas manualmente.

**¿Cómo se crean las tablas entonces?**

Tienes 2 opciones:

#### **OPCIÓN 1: Scripts SQL manuales** (Lo que tienes ahora)

Debes crear un archivo SQL con todas las tablas y ejecutarlo en tu base de datos PostgreSQL.

**Ejemplo de script:** `create-schema.sql`
```sql
-- Crear secuencia compartida
CREATE SEQUENCE app.hibernate_sequence START WITH 1 INCREMENT BY 1;

-- Tabla de roles
CREATE TABLE app.roles (
    id BIGINT PRIMARY KEY DEFAULT nextval('app.hibernate_sequence'),
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Tabla de usuarios
CREATE TABLE app.users (
    id BIGINT PRIMARY KEY DEFAULT nextval('app.hibernate_sequence'),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Tabla intermedia user_roles
CREATE TABLE app.user_roles (
    user_id BIGINT REFERENCES app.users(id),
    role_id BIGINT REFERENCES app.roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Tabla de propietarios
CREATE TABLE app.owners (
    id BIGINT PRIMARY KEY DEFAULT nextval('app.hibernate_sequence'),
    user_id BIGINT NOT NULL UNIQUE REFERENCES app.users(id),
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL
);

-- Tabla de perros
CREATE TABLE app.dogs (
    id BIGINT PRIMARY KEY DEFAULT nextval('app.hibernate_sequence'),
    name VARCHAR(255) NOT NULL,
    breed VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    microchip VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES app.owners(id),
    photo_path VARCHAR(500)
);

-- Tabla de vacunas
CREATE TABLE app.vaccines (
    id BIGINT PRIMARY KEY DEFAULT nextval('app.hibernate_sequence'),
    name VARCHAR(100) NOT NULL
);

-- Tabla intermedia dog_vaccine
CREATE TABLE app.dog_vaccine (
    id BIGINT PRIMARY KEY DEFAULT nextval('app.hibernate_sequence'),
    dog_id BIGINT NOT NULL REFERENCES app.dogs(id),
    vaccine_id BIGINT NOT NULL REFERENCES app.vaccines(id),
    applied_date DATE NOT NULL,
    UNIQUE (dog_id, vaccine_id, applied_date)
);

-- Tabla de notas
CREATE TABLE app.notes (
    id BIGINT PRIMARY KEY DEFAULT nextval('app.hibernate_sequence'),
    note_date DATE NOT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    dog_id BIGINT NOT NULL REFERENCES app.dogs(id)
);

-- Insertar roles por defecto
INSERT INTO app.roles (name) VALUES ('USER');
INSERT INTO app.roles (name) VALUES ('ADMIN');
```

Luego lo ejecutas en tu base de datos Neon con:
```bash
psql -h ep-shy-sky-agfv3s9a-pooler.c-2.eu-central-1.aws.neon.tech \
     -U app \
     -d pets \
     -f create-schema.sql
```

#### **OPCIÓN 2: Usar herramientas de migración** (Flyway o Liquibase)

Son librerías que gestionan las migraciones de forma automática y versionada.

**Ventaja:** Puedes hacer cambios incrementales sin perder datos.

Ejemplo con **Flyway**:
```
src/main/resources/db/migration/
    ├── V1__create_users_and_roles.sql
    ├── V2__create_owners.sql
    ├── V3__create_dogs.sql
    └── V4__create_vaccines_and_notes.sql
```

Flyway ejecuta los scripts en orden y guarda un historial de qué se ejecutó.

---

### **¿POR QUÉ USAR `none` ES MEJOR?**

#### ✅ **VENTAJAS**

1. **Control total:** Tú decides cuándo y cómo cambiar la BD
2. **Seguridad:** Hibernate no puede borrar datos accidentalmente
3. **Versionado:** Puedes usar Git para versionar tus scripts SQL
4. **Producción:** Es la única opción segura en ambientes productivos
5. **Equipo:** Todos usan los mismos scripts, evita inconsistencias

#### ❌ **DESVENTAJAS DE `update` (lo más usado en desarrollo)**

1. **Peligroso:** Puede eliminar columnas si las quitas de una entidad
2. **No detecta renombres:** Si renombras `name` a `fullName`, crea una nueva columna (pierdes datos)
3. **No elimina tablas:** Si borras una entidad, la tabla queda huérfana
4. **Sincronización:** Cada desarrollador puede tener un estado diferente de la BD

---

### **RESUMEN SIMPLE**

**`ddl-auto=none`** significa:

> "Hibernate, tú NO toques la base de datos. Yo la gestiono manualmente con scripts SQL"

**Consecuencia:**

Antes de ejecutar tu aplicación Spring Boot por primera vez, **DEBES crear todas las tablas manualmente** en PostgreSQL, o la aplicación no funcionará porque intentará acceder a tablas que no existen.

---

## 🎓 CONCEPTOS CLAVE RESUMIDOS

### **BCrypt**
Algoritmo de encriptación de contraseñas **irreversible**. No puedes "desencriptar", solo comparar:
```java
String hash = bcrypt.encode("mipass");  // "$2a$10$N9qo..."
bcrypt.matches("mipass", hash);         // true
bcrypt.matches("otra", hash);           // false
```

### **JWT (JSON Web Token)**
Token firmado que contiene información del usuario. Se envía en cada petición:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI...
```

### **STATELESS**
El servidor NO guarda sesiones. Cada petición es independiente y debe traer su token.

### **Filter (Filtro)**
Código que se ejecuta **antes** de llegar al controlador. Sirve para:
- Validar tokens
- Logging
- Modificar headers

### **UserDetails**
Interfaz de Spring Security que representa un usuario autenticado con:
- Username
- Password (encriptada)
- Authorities (permisos/roles)

### **SecurityContext**
"Memoria temporal" donde Spring guarda la información del usuario autenticado durante la petición.

---

## 🔑 SEGURIDAD EN UNA ORACIÓN

**"Cada petición trae un token JWT firmado que el filtro valida antes de permitir el acceso, sin guardar sesiones en el servidor"**

---

## ✅ CHECKLIST DE SEGURIDAD

- [x] Contraseñas encriptadas con BCrypt
- [x] Tokens JWT con expiración (120 minutos)
- [x] Filtro que valida tokens en cada petición
- [x] Rutas públicas solo para login/register
- [x] CORS configurado para frontend específico
- [x] Sesiones deshabilitadas (STATELESS)
- [ ] ⚠️ Secret de JWT en variable de entorno (ahora es "CHANGE_ME")
- [ ] ⚠️ Credenciales de BD en variables de entorno
- [ ] ⚠️ HTTPS en producción
- [ ] ⚠️ Rate limiting (limitar intentos de login)

---

¿Necesitas que profundice en algún punto específico? 🤓