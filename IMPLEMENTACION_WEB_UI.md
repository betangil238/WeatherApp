# 🚀 Guía de Implementación - Weather App Web UI

## 📋 Tabla de Contenidos
1. [Requisitos Previos](#requisitos-previos)
2. [Instalación Rápida](#instalación-rápida)
3. [Configuración del Backend](#configuración-del-backend)
4. [Verificación de Endpoints](#verificación-de-endpoints)
5. [Personalización](#personalización)
6. [Deployment](#deployment)
7. [Solución de Problemas](#solución-de-problemas)

---

## 📦 Requisitos Previos

- Java 11+
- Maven 3.6+
- Spring Boot 2.5+
- Navegador moderno (Chrome, Firefox, Safari, Edge)
- Conexión a Internet (para API Open-Meteo)

---

## ⚡ Instalación Rápida

### Paso 1: Copiar Archivos Web

```bash
# Los archivos ya están en:
src/main/resources/
├── index.html
├── styles.css
├── script.js
└── README_WEB.md
```

### Paso 2: Verificar Dependencias en pom.xml

```xml
<!-- Asegurar que tiene Spring Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Para JSON/DTO -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>

<!-- Para Lombok (opcional, para DTOs más limpios) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### Paso 3: Compilar y Ejecutar

```bash
cd weather-app
mvn clean install
mvn spring-boot:run
```

### Paso 4: Acceder a la Aplicación

Abre tu navegador y ve a:
```
http://localhost:8080
```

---

## 🔧 Configuración del Backend

### 1. Archivo application.properties

Crea o edita `src/main/resources/application.properties`:

```properties
# Puerto del servidor
server.port=8080

# Logging
logging.level.root=INFO
logging.level.com.weatherapp=DEBUG

# Spring MVC
spring.mvc.static-path-pattern=/static/**

# Encoding
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.force=true
```

### 2. Controlador REST (Ya Creado)

El archivo `WeatherApiController.java` ya contiene:
- ✅ Endpoint para clima actual: `GET /api/weather`
- ✅ Endpoint para múltiples ciudades: `GET /api/weather/multiple`
- ✅ Endpoint para pronóstico: `GET /api/weather/forecast`
- ✅ Health check: `GET /api/weather/health`
- ✅ CORS configurado

### 3. Configuración Web MVC (Ya Creada)

El archivo `WebMvcConfig.java` ya incluye:
- ✅ CORS habilitado para todos los orígenes
- ✅ Servicio de archivos estáticos
- ✅ Configuración de métodos HTTP permitidos

### 4. Inyectar Servicios (IMPORTANTE)

Asegúrate que `WeatherService` y `WeatherForecastService` están anotados con `@Service`:

```java
@Service
public class WeatherService {
    // ...
}

@Service
public class WeatherForecastService {
    // ...
}
```

---

## ✅ Verificación de Endpoints

### Método 1: Con curl

```bash
# Probar endpoint de una ciudad
curl "http://localhost:8080/api/weather?city=Madrid&unit=celsius"

# Probar múltiples ciudades
curl "http://localhost:8080/api/weather/multiple?cities=Madrid,Barcelona,Valencia&unit=celsius"

# Probar health check
curl "http://localhost:8080/api/weather/health"

# Probar pronóstico
curl "http://localhost:8080/api/weather/forecast?city=Madrid&unit=celsius"
```

### Método 2: Con Postman

1. Abre Postman
2. Crea nueva colección "Weather App"
3. Agrega requestss para cada endpoint:

```
GET http://localhost:8080/api/weather?city=Madrid&unit=celsius
GET http://localhost:8080/api/weather/multiple?cities=Madrid,Barcelona&unit=celsius
GET http://localhost:8080/api/weather/forecast?city=Madrid&unit=celsius
GET http://localhost:8080/api/weather/health
```

### Método 3: Desde el Navegador

Abre la consola de desarrollador (F12) y ejecuta:

```javascript
// Probar fetch API
fetch('http://localhost:8080/api/weather?city=Madrid&unit=celsius')
  .then(r => r.json())
  .then(d => console.log(d))
  .catch(e => console.error(e));
```

---

## 🎨 Personalización

### Cambiar Paleta de Colores

Edita `styles.css`:

```css
:root {
    /* Cambiar colores principales */
    --primary-color: #667eea;      /* Azul */
    --secondary-color: #764ba2;    /* Púrpura */
    --accent-color: #f093fb;       /* Rosado */
    --dark-bg: #0a0e27;
    --light-bg: #16213e;
    --text-primary: #ffffff;
    --text-secondary: #cbd5e0;
}
```

### Cambiar Logo o Título

Edita `index.html`:

```html
<h1 class="title">🌍 Your App Name</h1>
<p class="subtitle">Your subtitle here</p>
```

### Cambiar Textos de Placeholder

Edita `index.html`:

```html
<input placeholder="Escribe aquí tu ciudad..." />
```

### Agregar Nuevo Modo o Funcionalidad

1. Agrega HTML en `index.html`
2. Agrega CSS en `styles.css`
3. Agrega JavaScript en `script.js`
4. Crea endpoint en `WeatherApiController.java`

---

## 📤 Deployment

### Opción 1: Servidor Linux/Cloud (Recomendado)

```bash
# 1. Buildear JAR
mvn clean package

# 2. Copiar a servidor
scp target/*.jar user@server:/opt/weather-app/

# 3. Ejecutar en servidor
ssh user@server
cd /opt/weather-app
java -jar weather-app-1.0.0.jar --server.port=8080

# 4. Para ejecución permanente, usar systemd
sudo nano /etc/systemd/system/weather-app.service
```

**Archivo systemd:**
```ini
[Unit]
Description=Weather App Service
After=network.target

[Service]
Type=simple
User=weather
WorkingDirectory=/opt/weather-app
ExecStart=/usr/bin/java -jar /opt/weather-app/weather-app-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable weather-app
sudo systemctl start weather-app
```

### Opción 2: Docker

Crea `Dockerfile`:

```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

```bash
# Build
docker build -t weather-app .

# Run
docker run -p 8080:8080 weather-app
```

### Opción 3: Docker Compose

Crea `docker-compose.yml`:

```yaml
version: '3'
services:
  weather-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xmx512m
    restart: always
```

```bash
docker-compose up -d
```

### Opción 4: Heroku

```bash
# 1. Login a Heroku
heroku login

# 2. Crear aplicación
heroku create weather-app-xyz

# 3. Agregar Procfile
echo "web: java -Dserver.port=\$PORT \$JAVA_OPTS -jar target/*.jar" > Procfile

# 4. Deployar
git push heroku main
```

---

## 🐛 Solución de Problemas

### Error: "Failed to fetch"

**Causa**: CORS bloqueado

**Solución**:
```java
// En WebMvcConfig.java, verificar:
registry.addMapping("/api/**")
        .allowedOrigins("*")  // O específicamente tu dominio
        .allowedMethods("*")
        .allowedHeaders("*");
```

### Error: 404 en index.html

**Causa**: Archivos estáticos no encontrados

**Solución**:
```
Mover archivos a: src/main/resources/public/
O verificar WebMvcConfig.java tiene resourceHandlers bien configurado
```

### Error: Timeouts en API

**Causa**: Servidor está lento o Open-Meteo no responde

**Solución**: Aumentar timeout en `script.js`:
```javascript
const controller = new AbortController();
const timeout = setTimeout(() => controller.abort(), 15000); // 15s
```

### Error: "CORS blocked"

**Causa**: El servidor no devuelve headers CORS correctos

**Solución**: Verificar application.properties:
```properties
server.servlet.encoding.charset=UTF-8
```

Y en WebMvcConfig:
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .maxAge(3600);
}
```

### Error: Datos no se cargan en tabla

**Causa**: Respuesta del API no coincide con estructura esperada

**Solución**: Verifica en consola (F12):
1. Abre Network tab
2. Verifica respuesta del endpoint
3. Compara con estructura esperada en comentarios de `script.js`

---

## 📊 Fórmulas de Conversión de Temperatura

Implementadas en `WeatherApiController.java`:

```
Celsius a Fahrenheit:  (°C × 9/5) + 32 = °F
Celsius a Kelvin:      °C + 273.15 = K
Fahrenheit a Celsius:  (°F - 32) × 5/9 = °C
Kelvin a Celsius:      K - 273.15 = °C
```

---

## 📈 Estadísticas del Proyecto

| Componente | Líneas | Descripción |
|-----------|--------|------------|
| index.html | 170 | Estructura completa con 3 modos |
| styles.css | 1050+ | Diseño moderno con 15+ animaciones |
| script.js | 650+ | Lógica y manejo de peticiones |
| WeatherApiController.java | 280 | 4 endpoints REST |
| WebMvcConfig.java | 35 | Configuración de CORS y static files |
| **Total** | **2,185+** | **Código de producción** |

---

## ✨ Características Principales Implementadas

✅ Interfaz web moderna y responsive  
✅ 3 modos de operación (una ciudad, múltiples, pronóstico)  
✅ Soporte para 3 unidades de temperatura (°C, °F, K)  
✅ Tabla comparativa con análisis inteligente  
✅ Pronóstico de 5 días  
✅ Indicadores de carga y mensajes de error  
✅ CORS habilitado para desarrollo/producción  
✅ DTOs para respuestas JSON limpias  
✅ Health check endpoint  
✅ Documentación completa  

---

## 📞 Soporte

Para problemas específicos, revisa:
- `README_WEB.md` - Documentación web
- `README.md` - Documentación general del proyecto
- Consola del navegador (F12) - Errores JavaScript
- Logs de Spring Boot - Errores del servidor

---

**Weather App © 2026**

Última actualización: Marzo 24, 2026  
Versión: 1.0.0  
Estado: ✅ Production Ready
