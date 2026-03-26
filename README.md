# 🌍 Weather App - Aplicación de Clima en Consola

Una aplicación de consola en Java que consulta datos climáticos en tiempo real usando la API gratuita de **Open-Meteo**.

---

## 📋 Características

✅ **Búsqueda de clima por ciudad** - Ingresa el nombre de cualquier ciudad  
✅ **Múltiples unidades de temperatura** - Celsius (°C), Fahrenheit (°F), Kelvin (K)  
✅ **Consultas interactivas** - Loop continuo para multiple búsquedas  
✅ **Manejo robusto de errores** - Reintentos automáticos, validaciones  
✅ **Salida formateada** - Emojis y cuadros ASCII para mejor legibilidad  
✅ **Persistencia de configuración** - Guarda tu unidad de temperatura preferida  
✅ **Sin dependencias externas innecesarias** - Solo Gson + HttpClient nativo  

---

## 🏗️ Requisitos

- **Java 11+** - La aplicación requiere Java 11 o superior
- **Maven 3.6+** - Para compilar el proyecto
- **Conexión a Internet** - Para consultar las APIs de Open-Meteo

---

## 📦 Dependencias

```xml
<!-- JSON Parsing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
```

---

## 🚀 Instalación y Ejecución

### 1. Clonar o descargar el proyecto

```bash
cd weather-app
```

### 2. Compilar el proyecto

```bash
mvn clean compile
```

### 3. Ejecutar tests

```bash
mvn test
```

Deberías ver 10+ tests pasando ✅

### 4. Ejecutar la aplicación

#### Opción A: Desde Maven
```bash
mvn exec:java -Dexec.mainClass="com.weatherapp.Main"
```

#### Opción B: Crear JAR y ejecutar
```bash
# Compilar y crear JAR con dependencias
mvn clean package

# Ejecutar JAR
java -jar target/weather-app-jar-with-dependencies.jar
```

#### Opción C: Desde IDE (VS Code, IntelliJ, etc.)
Abre `src/main/java/com/weatherapp/Main.java` y ejecuta directamente.

---

## 📖 Guía de Uso

### Al iniciar la aplicación

```
╔═══════════════════════════════════════════════════════════╗
║           🌍 APLICACIÓN DE CLIMA EN TIEMPO REAL 🌦️       ║
║              Powered by Open-Meteo API                   ║
╚═══════════════════════════════════════════════════════════╝

[ConfigManager] Configuration loaded from: /home/user/.weatherapp/config.properties

Selecciona la unidad de temperatura para mostrar:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  1️⃣  Celsius (°C)       - Recomendado
  2️⃣  Fahrenheit (°F)   - EE.UU.
  3️⃣  Kelvin (K)         - Científica
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Ingresa tu opción (1-3) [Defecto: 1]: 1
✅ Unidad seleccionada: Celsius (°C)
```

### Buscar una ciudad

```
🔍 Ingresa el nombre de la ciudad (ej: Madrid, Nueva York): Madrid

[OpenMeteoClient] Resolviendo ciudad: Madrid
[OpenMeteoClient] ✓ Ciudad resuelta: Madrid, Spain (40.4168, -3.7038)
[OpenMeteoClient] Obteniendo datos climáticos para: Madrid
[OpenMeteoClient] ✓ Datos climáticos obtenidos: 25.5° - ☀️ Despejado

╔═══════════════════════════════════════════════════════════╗
║           🌍 INFORMACIÓN DEL CLIMA                        ║
╠═══════════════════════════════════════════════════════════╣
║ 📍 Ciudad: Madrid, Spain                                  ║
║ 🌡️  Temperatura:        25.5°C                             ║
║ 🤔 Sensación térmica:   23.0°C                             ║
║ 💧 Humedad:             60%                                ║
║ ☁️  Descripción:        ☀️ Despejado                       ║
╚═══════════════════════════════════════════════════════════╝

¿Deseas consultar otra ciudad? (S/N): S

🔍 Ingresa el nombre de la ciudad: Nueva York
...
```

### Cuando no se encuentra la ciudad

```
🔍 Ingresa el nombre de la ciudad: CiudadFantástica

[OpenMeteoClient] Resolviendo ciudad: CiudadFantástica
╔═══════════════════════════════════════════════════════════╗
║                     ❌ ERROR                             ║
╠═══════════════════════════════════════════════════════════╣
║ 🚫 Ciudad no encontrada: 'CiudadFantástica'              ║
╚═══════════════════════════════════════════════════════════╝

¿Deseas intentar con otra ciudad? (S/N): S
```

---

## 🏠 Estructura del Proyecto

```
weather-app/
├── src/
│   ├── main/java/com/weatherapp/
│   │   ├── Main.java                    ← Punto de entrada
│   │   ├── model/
│   │   │   ├── GeoLocation.java         ← POJO: latitud, longitud, ciudad
│   │   │   └── WeatherData.java         ← POJO: temperatura, humedad, clima
│   │   ├── config/
│   │   │   ├── AppConfig.java           ← Constantes de configuración
│   │   │   ├── TemperatureUnit.java     ← Enum: C, F, K
│   │   │   └── ConfigManager.java       ← Persistencia de configuración
│   │   ├── client/
│   │   │   └── OpenMeteoClient.java     ← Cliente HTTP (geocoding + weather)
│   │   ├── service/
│   │   │   └── WeatherService.java      ← Orquestación de lógica
│   │   └── presentation/
│   │       └── ConsoleUI.java           ← Interfaz de usuario
│   └── test/java/com/weatherapp/
       ├── client/
       │   ├── OpenMeteoClientErrorTest.java        ← Tests de errores (26 casos)
       │   ├── OpenMeteoClientGeocodeTest.java      ← Tests de geocodificación (21 casos)
       │   └── OpenMeteoClientWeatherTest.java      ← Tests de datos climáticos (33 casos)
       ├── service/
       │   ├── WeatherServiceTest.java              ← Tests unitarios (10+ casos)
       │   ├── WeatherServiceValidationTest.java    ← Tests de validación (10+ casos)
       │   ├── WeatherServiceIntegrationTest.java   ← Tests de integración (28 casos)
       │   └── WeatherServiceEdgeCasesTest.java     ← Tests de casos límite (27 casos)
       └── integration/
           └── WeatherAppEndToEndTest.java          ← Tests E2E (21 casos)
├── pom.xml                               ← Configuración Maven
└── README.md                             ← Este archivo

```

---

## 🔧 Configuración

La aplicación guarda tu preferencia de unidad de temperatura en:

```
~/.weatherapp/config.properties
```

Contenido del archivo:
```properties
temperature.unit=CELSIUS
```

### Cambiar la unidad de temperatura

**Opción 1:** Editar el archivo de configuración directamente
```bash
# Busca el archivo en tu home
~/.weatherapp/config.properties

# Cambia el valor:
temperature.unit=FAHRENHEIT  # o KELVIN
```

**Opción 2:** Borrar el archivo y ejecutar la app (elegirá nuevamente)
```bash
rm ~/.weatherapp/config.properties
java -jar target/weather-app-jar-with-dependencies.jar
```

---

## 📊 APIs Utilizadas

### Open-Meteo - Geocoding API
- **URL:** https://geocoding-api.open-meteo.com/v1/search
- **Propósito:** Resolver nombre de ciudad → latitud y longitud
- **Parámetros:** `name`, `language`, `limit`
- **Respuesta:** JSON con coincidencias (nombre, latitud, longitud, país)

### Open-Meteo - Weather API
- **URL:** https://api.open-meteo.com/v1/forecast
- **Propósito:** Obtener datos climáticos actuales
- **Parámetros:** `latitude`, `longitude`, `current`, `temperature_unit`
- **Respuesta:** JSON con temperatura, sensación térmica, humedad, código climático

**Ventajas:**
- ✅ API completamente gratuita (sin autenticación)
- ✅ Sin límite de rate (pero respeta límites razonables)
- ✅ Datos precisos y actualizados
- ✅ Cobertura mundial

---

## 🧪 Testing

### Ejecutar todos los tests

```bash
mvn test
```

### Ejecutar un test específico

```bash
mvn test -Dtest=WeatherServiceTest
```

### Tests incluidos

Tu proyecto ahora contiene **170+ casos de prueba** completamente implementados:

#### 1️⃣ OpenMeteoClientErrorTest (26 tests)
Validación exhaustiva de parámetros y manejo de errores
- ✅ Validación de coordenadas (latitud/longitud fuera de rango)
- ✅ Validación de parámetros nulos
- ✅ Validación de humedad (negativa, mayor a 100%)
- ✅ Validación de rangos extremos

#### 2️⃣ OpenMeteoClientGeocodeTest (21 tests)
Resolución de ciudades a coordenadas geográficas
- ✅ Ciudades comunes (Madrid, Londres, París, Nueva York, Tokio)
- ✅ Ciudades con acentos (São Paulo, Zürich, México)
- ✅ Ciudades con espacios y guiones (Nueva York, Rio de Janeiro, Los Angeles)
- ✅ Validación de coordenadas retornadas
- ✅ Trimming de espacios en entrada
- ✅ Manejo de ciudades no encontradas
- ✅ Múltiples resultados
- ✅ Consistencia entre llamadas

#### 3️⃣ OpenMeteoClientWeatherTest (33 tests)
Obtención y validación de datos climáticos
- ✅ Validación de datos retornados (no nulos, válidos)
- ✅ Rango de temperaturas (-90°C a +60°C)
- ✅ Rango de humedad (0% a 100%)
- ✅ Soporte de unidades (Celsius, Fahrenheit, Kelvin)
- ✅ Conversiones de temperatura correctas
- ✅ Ubicaciones extremas (Polo Sur, desiertos, trópicos)
- ✅ Coordenadas límite (±90°, ±180°)
- ✅ Consistencia entre llamadas

#### 4️⃣ WeatherServiceTest (10+ tests)
Orquestación básica del servicio
- ✅ Validación de entrada (nula, vacía, espacios)
- ✅ Procesamiento exitoso
- ✅ Preservación de unidades de temperatura
- ✅ Manejo de errores de red
- ✅ Características especiales

#### 5️⃣ WeatherServiceValidationTest (10+ tests)
Validación específica de datos climáticos
- ✅ Validación de humedad (0%, 50%, 100%)
- ✅ Validación de temperatura realista
- ✅ Validación de coordenadas geográficas

#### 6️⃣ WeatherServiceIntegrationTest (28 tests)
Flujo completo: entrada → geocodificación → clima
- ✅ Flujo completo exitoso con múltiples ciudades
- ✅ Diferentes unidades de temperatura (C, F, K)
- ✅ Orden correcto de llamadas al cliente (con Mockito)
- ✅ Manejo de errores en diferentes fases
- ✅ Búsquedas secuenciales
- ✅ Trimming y normalización de entrada
- ✅ Mensajes de error con contexto
- ✅ Consistencia entre unidades

#### 7️⃣ WeatherServiceEdgeCasesTest (27 tests)
Casos límite y caracteres especiales
- ✅ Ciudades con acentos y Unicode (Cirílico, Chino, Árabe)
- ✅ Espacios múltiples y guiones
- ✅ Temperaturas extremas (-89°C, +60°C, cercana a cero)
- ✅ Humedad en límites (0%, 100%)
- ✅ Diferencias entre temperatura real y aparente
- ✅ Descripciones largas del clima
- ✅ Comparación entre ciudades (frías vs cálidas)
- ✅ Temperaturas con decimales

#### 8️⃣ WeatherAppEndToEndTest (21 tests)
Pruebas end-to-end con APIs reales de Open-Meteo
- ✅ Flujo E2E completo (Madrid, Londres, París, Nueva York, Tokio)
- ✅ E2E con diferentes unidades (Fahrenheit, Kelvin)
- ✅ Ciudades con caracteres especiales
- ✅ Validación exhaustiva de datos retornados
- ✅ Búsquedas secuenciales y consistencia
- ✅ Manejo de errores (ciudad inexistente, nula, vacía)
- ✅ Casos extremos y calidad de descripción

### Cobertura de Testing

| Categoría | Tests | Cobertura |
|-----------|-------|----------|
| Validación de entrada | 15+ | ✅ Completa |
| Geocodificación | 21 | ✅ Completa |
| Datos climáticos | 56+ | ✅ Completa |
| Integración | 28 | ✅ Completa |
| Casos límite | 27 | ✅ Completa |
| End-to-End | 21 | ✅ Completa |
| **TOTAL** | **170+** | **✅ Excelente** |

### Ejecutar Tests Específicos

```bash
# Todos los tests
mvn test

# Tests de geocodificación
mvn test -Dtest=OpenMeteoClientGeocodeTest

# Tests de datos climáticos
mvn test -Dtest=OpenMeteoClientWeatherTest

# Tests de integración
mvn test -Dtest=WeatherServiceIntegrationTest

# Tests de edge cases
mvn test -Dtest=WeatherServiceEdgeCasesTest

# Tests end-to-end (requieren Internet)
mvn test -Dtest=WeatherAppEndToEndTest
```

### Estrategia de Testing

Los tests utilizan diferentes estrategias según el nivel:

- **Tests unitarios:** Mockito para simular cliente HTTP sin dependencias externas
- **Tests de integración:** Mocks con respuestas predefinidas para validar orquestación
- **Tests E2E:** Llamadas reales a las APIs de Open-Meteo (requieren conexión a Internet)

El `MockOpenMeteoClient` proporciona:
- Respuestas predefinidas para ciudades conocidas
- Simulación de errores de red y ciudades no encontradas
- Sin dependencias de conectividad externa para tests rápidos

---

## 🛠️ Desarrollo

### Compilar sin ejecutar

```bash
mvn compile
```

### Limpiar archivos compilados

```bash
mvn clean
```

### Ver dependencias

```bash
mvn dependency:tree
```

### Generar reportes

```bash
mvn site
```

---

## 🌤️ Pronóstico de 5 Días ✨ NUEVO

Además del clima actual, ahora la aplicación incluye un **sistema completo de pronóstico del tiempo para 5 días**.

### Características

✅ **Pronóstico de 5 días** - Predicciones completas para una semana  
✅ **4 formatos de visualización** - Detallado, resumen, estadísticas, día específico  
✅ **Análisis inteligente** - Identifica días calurosos/fríos, probabilidad de lluvia  
✅ **Datos de demo** - Para testing sin API real  
✅ **Ejemplo funcional** - Código ejecutable inmediatamente  

### Uso Rápido (5 minutos)

```bash
cd weather-app
mvn clean compile exec:java -Dexec.mainClass="com.weatherapp.ForecastExample"
```

### Usar en tu Código

```java
// Crear servicio
WeatherForecastService service = new WeatherForecastService(
    weatherService,
    TemperatureUnit.CELSIUS
);

// Obtener pronóstico
ForecastData forecast = service.getForecast5Days("Madrid");

// Mostrar - Elige un formato:
service.printForecast(forecast);           // Detallado 📊
service.printForecastSummary(forecast);    // Resumen 📋
service.printForecastStats(forecast);      // Estadísticas 📈
```

### Análisis Inteligente

```java
// ¿Va a lluver?
if (forecast.willRain(5)) {
    System.out.println("💧 Llevar paraguas");
}

// Día más caluroso
DailyForecast hottest = forecast.getHottestDay();
System.out.println("🔥 " + hottest.getDate() + ": " + 
                   hottest.getMaxTemperature() + "°C");

// Temperatura promedio
double avg = forecast.getAverageTemperature(5);
System.out.println("📊 Promedio: " + avg + "°C");
```

### Documentación del Pronóstico

Archivos de referencia:
- **[FORECAST_QUICKSTART.md](FORECAST_QUICKSTART.md)** - Inicio en 5 minutos
- **[FORECAST_USAGE.md](FORECAST_USAGE.md)** - Guía completa de uso
- **[FORECAST_INTEGRATION.md](FORECAST_INTEGRATION.md)** - Integración en ConsoleUI
- **[FORECAST_START.md](FORECAST_START.md)** - Página de bienvenida

### Archivos Creados

```
src/main/java/com/weatherapp/
├── model/
│   ├── DailyForecast.java           ← Modelo para 1 día
│   └── ForecastData.java            ← Contenedor para 5 días
├── service/
│   └── WeatherForecastService.java  ← Servicio de orquestación
└── ForecastExample.java             ← Ejemplo ejecutable
```

---

## ❓ Troubleshooting

### Error: "No se puede encontrar o no se puede cargar la clase principal"

**Solución:** Asegúrate de compilar primero:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.weatherapp.Main"
```

### Error: "java.net.ConnectException: Connection refused"

**Causa:** Sin conexión a internet o las APIs de Open-Meteo no responden  
**Solución:** Verifica tu conexión y reinicia la aplicación

### Error: "gradle/wrapper/gradle-wrapper.jar"

**Causa:** El proyecto usa Maven, no Gradle  
**Solución:** Usa `mvn` en lugar de `gradle`

### Tests fallan con "JsonSyntaxException"

**Causa:** Problema con parsing de JSON  
**Solución:** Verifica que Gson esté en el classpath:
```bash
mvn clean package
```

---

## 📝 Notas de Implementación

### Testing Comprehensivo (v1.1.0)

Tu aplicación fue actualizada con una suite de testing completa que cubre:

**Niveles de Testing:**
1. **Unit Tests** - Validación de métodos individuales
2. **Integration Tests** - Flujo completo con mocks
3. **End-to-End Tests** - Pruebas con APIs reales

**Categorías de Cobertura:**
- Validación de entrada del usuario (null, empty, whitespace)
- Geocodificación de ciudades (comunes, especiales, no encontradas)
- Obtención de datos climáticos (temperaturas, humedad, validaciones)
- Unidades de temperatura (Celsius, Fahrenheit, Kelvin)
- Casos extremos (Polo Norte/Sur, desiertos, trópicos)
- Caracteres especiales y Unicode (acentos, alfabetos diferentes)
- Error handling y mensajes contextuales
- Consistencia de datos y reintentos

**Herramientas:**
- JUnit 5 para el framework de testing
- Mockito para mocks y stubs
- Assertions exhaustivos para validación

### Diseño de Arquitectura

```
Main
  ↓
ConsoleUI (Presentación)
  ↓
WeatherService (Orquestación)
  ↓
OpenMeteoClient (HTTP)
  ↓
Model + Config (Datos)
```

### Manejo de Errores

- **IllegalArgumentException** → Errores de validación (entrada del usuario)
- **RuntimeException** → Errores de red, IO, parsing
- **InterruptedException** → Thread interrumpido

Cada capa maneja sus propias excepciones y propaga al siguiente nivel con contexto adicional.

### Reintentos

- **Max intentos:** 3
- **Delay inicial:** 1000ms
- **Backoff:** Exponencial 2x (1s → 2s → 4s)

### Performance

- Tiempo de respuesta típico: 2-3 segundos (incluyendo network latency)
- Memory: ~50MB en ejecución
- No usa caché (cada búsqueda es fresca)

---

## 🚀 Mejoras Futuras

**Tests implementados recientemente (v1.1.0):**
- ✅ 170+ casos de prueba (unitarios, integración, E2E)
- ✅ Cobertura completa de geocodificación
- ✅ Cobertura completa de validación de datos
- ✅ Tests de edge cases con caracteres Unicode
- ✅ Tests de integración con Mockito
- ✅ Tests E2E con APIs reales

**Próximas mejoras:**

- [ ] Caché de consultas recientes
- [ ] Pronóstico de 7 días
- [ ] Búsqueda de múltiples ciudades simultáneamente
- [ ] Interfaz gráfica (Swing/JavaFX)
- [ ] Exportar a CSV/JSON
- [ ] Notificaciones de alerta (lluvia, frio, etc.)
- [ ] Historial de búsquedas
- [ ] Geolocalización automática

---

## 📄 Licencia

Este proyecto es de código abierto. Siéntete libre de usar, modificar y distribuir.

---

## 🤝 Contribuciones

¡Las contribuciones son bienvenidas! Por favor:
1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📞 Soporte

Si encuentras problemas:
1. Verifica los logs en consola (prefijos [TAG])
2. Consulta la sección Troubleshooting
3. Abre un issue en el repositorio

---

**¡Disfruta consultando el clima! 🌤️**
