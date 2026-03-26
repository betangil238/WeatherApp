# 🔌 Guía de Integración: Pronóstico 5 Días en ConsoleUI

## Objetivo
Integrar el sistema de pronóstico de 5 días en la interfaz de usuario de consola existente, proporcionando acceso fácil desde el menú principal.

---

## 📍 Paso 1: Actualizar Main.java

**Ubicación:** `src/main/java/com/weatherapp/Main.java`

```java
public class Main {
    public static void main(String[] args) {
        // ... código existente ...
        
        // AGREGAR ESTAS LÍNEAS:
        WeatherForecastService forecastService = 
            new WeatherForecastService(
                weatherService,
                TemperatureUnit.CELSIUS
            );
        
        // Pasar al handler
        ConsoleUI ui = new ConsoleUI(
            weatherService,
            weatherCache,
            forecastService  // ← NUEVO
        );
        
        ui.start();
    }
}
```

---

## 📍 Paso 2: Actualizar ConsoleUI.java

**Ubicación:** `src/main/java/com/weatherapp/presentation/ConsoleUI.java`

### 2a. Agregar campo de instancia

```java
public class ConsoleUI {
    private WeatherService weatherService;
    private WeatherCache weatherCache;
    private WeatherForecastService forecastService;  // ← AGREGAR
    
    // Constructor actualizado
    public ConsoleUI(
        WeatherService weatherService,
        WeatherCache weatherCache,
        WeatherForecastService forecastService  // ← NUEVO PARÁMETRO
    ) {
        this.weatherService = weatherService;
        this.weatherCache = weatherCache;
        this.forecastService = forecastService;  // ← ASIGNAR
    }
}
```

### 2b. Actualizar menú principal

```java
private void displayMainMenu() {
    System.out.println("\n" + "═".repeat(50));
    System.out.println("      ☀️  APLICACIÓN DEL TIEMPO");
    System.out.println("═".repeat(50));
    System.out.println("1. Clima actual");
    System.out.println("2. Clima en caché (si disponible)");
    System.out.println("3. Buscar ciudad");
    System.out.println("4. Convertir unidad de temperatura");
    System.out.println("5. 📊 PRONÓSTICO 5 DÍAS ← NUEVO");  // ← NUEVO
    System.out.println("6. Estadísticas de caché");
    System.out.println("7. Salir");
    System.out.println("═".repeat(50));
}

private void handleMenuChoice(String choice) {
    switch (choice.trim()) {
        case "1":
            handleCurrentWeather();
            break;
        case "2":
            handleCachedWeather();
            break;
        case "3":
            handleSearchCity();
            break;
        case "4":
            handleTemperatureConversion();
            break;
        case "5":  // ← NUEVO CASO
            handleForecast5Days();
            break;
        case "6":
            handleCacheStats();
            break;
        case "7":
            handleExit();
            break;
        default:
            System.out.println("❌ Opción no válida");
    }
}
```

### 2c. Agregar método de pronóstico

```java
private void handleForecast5Days() {
    System.out.println("\n" + "═".repeat(50));
    System.out.println("📊 PRONÓSTICO DEL TIEMPO 5 DÍAS");
    System.out.println("═".repeat(50));
    
    try {
        // Obtener ciudad
        String city = getUserInput("Ingrese nombre de la ciudad: ");
        if (city.isBlank()) {
            System.out.println("❌ Ciudad no puede estar vacía");
            return;
        }
        
        System.out.println("\n⏳ Obteniendo pronóstico...\n");
        
        // Obtener datos
        ForecastData forecast = forecastService.getForecast5Days(city);
        
        // Menú de opciones de visualización
        boolean showingOptions = true;
        while (showingOptions) {
            System.out.println("\n┌─ OPCIONES DE VISUALIZACIÓN ─────────┐");
            System.out.println("│ 1. Vista detallada                  │");
            System.out.println("│ 2. Resumen (1 línea/día)           │");
            System.out.println("│ 3. Estadísticas                    │");
            System.out.println("│ 4. Día específico                  │");
            System.out.println("│ 5. Ver análisis completamente      │");
            System.out.println("│ 6. Volver al menú principal        │");
            System.out.println("└────────────────────────────────────┘");
            
            String option = getUserInput("Seleccione opción: ");
            
            switch (option) {
                case "1":
                    System.out.println();
                    forecastService.printForecast(forecast);
                    break;
                    
                case "2":
                    System.out.println();
                    forecastService.printForecastSummary(forecast);
                    break;
                    
                case "3":
                    System.out.println();
                    forecastService.printForecastStats(forecast);
                    break;
                    
                case "4": {
                    String dayInput = getUserInput(
                        "¿Qué día? (0=hoy, 1=mañana, ..., 4=en 5 días): ");
                    try {
                        int dayIndex = Integer.parseInt(dayInput);
                        if (dayIndex < 0 || dayIndex >= 5) {
                            System.out.println("❌ Ingrese número entre 0 y 4");
                        } else {
                            String dayInfo = forecastService
                                .getForecastForDay(forecast, dayIndex);
                            System.out.println();
                            System.out.println(dayInfo);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Ingrese un número válido");
                    }
                    break;
                }
                
                case "5":
                    System.out.println("\n" + "═".repeat(50));
                    System.out.println("ANÁLISIS COMPLETO - " + 
                        forecast.getCityName().toUpperCase());
                    System.out.println("═".repeat(50));
                    
                    // Clima actual
                    System.out.println("\n🌍 CONDICIONES ACTUALES:");
                    System.out.println("   Temperatura: " + 
                        forecast.getCurrentWeather().getTemperature() + 
                        "°" + 
                        forecast.getTemperatureUnit().getSymbol());
                    System.out.println("   Humedad: " + 
                        forecast.getCurrentWeather().getHumidity() + "%");
                    System.out.println("   Descripción: " + 
                        forecast.getCurrentWeather().getDescription());
                    
                    // Detalles completos
                    System.out.println("\n📅 PRONÓSTICO DETALLADO:");
                    forecastService.printForecast(forecast);
                    
                    // Estadísticas
                    System.out.println("\n📊 ESTADÍSTICAS:");
                    forecastService.printForecastStats(forecast);
                    break;
                    
                case "6":
                    showingOptions = false;
                    break;
                    
                default:
                    System.out.println("❌ Opción no válida");
            }
        }
        
    } catch (CityNotFoundException e) {
        System.out.println("❌ Ciudad no encontrada: " + e.getMessage());
    } catch (NetworkException e) {
        System.out.println("❌ Error de conexión: " + e.getMessage());
    } catch (WeatherAppException e) {
        System.out.println("❌ Error: " + e.getMessage());
    } catch (Exception e) {
        System.out.println("❌ Error inesperado: " + e.getMessage());
        LOGGER.error("Error al obtener pronóstico", e);
    }
}
```

---

## 📍 Paso 3: Actualizar imports

En `ConsoleUI.java`, agregar estos imports:

```java
import com.weatherapp.model.ForecastData;
import com.weatherapp.model.DailyForecast;
import com.weatherapp.service.WeatherForecastService;
import com.weatherapp.exception.*;
```

---

## 📍 Paso 4: Actualizar pom.xml (si es necesario)

Verificar que estas dependencias existan:

```xml
<dependencies>
    <!-- Logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.0</version>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.9.2</version>
        <scope>test</scope>
    </dependency>
    <!-- ... resto de dependencias ... -->
</dependencies>
```

---

## ✅ Prueba de Integración

### Opción 1: Compilar y ejecutar

```bash
cd c:\Users\betan\weather-app

# Limpiar y compilar
mvn clean compile

# Ejecutar aplicación principal
mvn exec:java -Dexec.mainClass="com.weatherapp.Main"
```

### Opción 2: Compilar y ejecutar desde IDE

1. Abrir Main.java en VS Code
2. Agregar las líneas del Paso 1
3. Presionar F5 (Debug) o Ctrl+F5 (Run)
4. Esperar que la aplicación inicie
5. Seleccionar opción "5" del menú
6. Ingresar nombre de ciudad (ej: "Madrid")

### Opción 3: Ejecutar ejemplo independiente

```bash
mvn clean compile exec:java \
  -Dexec.mainClass="com.weatherapp.ForecastExample"
```

---

## 🎯 Flujo de Usuario

```
MENÚ PRINCIPAL
    ↓
[Seleccionar opción 5: Pronóstico 5 Días]
    ↓
Ingresar ciudad (ej: "Madrid")
    ↓
MENÚ DE VISUALIZACIÓN
    ├─ 1: Vista detallada (bordes ASCII)
    ├─ 2: Resumen (1 línea × día)
    ├─ 3: Estadísticas (hottest, coldest, lluvia)
    ├─ 4: Día específico (0=hoy...4=en 5 días)
    ├─ 5: Análisis completo (todo junto)
    └─ 6: Volver al menú principal
```

---

## 🔧 Personalización Opcional

### Cambiar unidad de temperatura

En `Main.java`:
```java
// De:
TemperatureUnit unit = TemperatureUnit.CELSIUS;

// A:
TemperatureUnit unit = TemperatureUnit.FAHRENHEIT;  // Para °F
TemperatureUnit unit = TemperatureUnit.KELVIN;      // Para K
```

### Agregar pronóstico en caché

```java
// En Main.java, crear servicio caché para pronósticos
CachedWeatherService cachedService = new CachedWeatherService(
    weatherService,
    weatherCache
);

// Luego pasar a ForecastService si lo deseamos
WeatherForecastService forecastService = 
    new WeatherForecastService(
        cachedService,  // Usar el servicio caché
        TemperatureUnit.CELSIUS
    );
```

---

## 📋 Checklist de Implementación

- [ ] Añadir `WeatherForecastService` a Main.java
- [ ] Importar en Main.java
- [ ] Actualizar constructor de ConsoleUI
- [ ] Actualizar Main.java para crear ConsoleUI con WeatherForecastService
- [ ] Agregar opción "5" al menú principal
- [ ] Agregar imports en ConsoleUI
- [ ] Implementar método `handleForecast5Days()`
- [ ] Compilar: `mvn clean compile`
- [ ] Ejecutar: `mvn exec:java -Dexec.mainClass="com.weatherapp.Main"`
- [ ] Probar opción 5 con varias ciudades
- [ ] Verificar que todas las visualizaciones funcionan

---

## 🐛 Solución de Problemas

### Problema: "Cannot find symbol: class WeatherForecastService"
**Solución:** Verificar que el archivo `WeatherForecastService.java` existe en `src/main/java/com/weatherapp/service/`

### Problema: "Constructor not found: ConsoleUI(...)"
**Solución:** Actualizar Main.java para que pase los 3 parámetros al constructor

### Problema: "City not found" para ciudades válidas
**Solución:** Verificar conexión a internet y que OpenMeteoClient funcione (prueba con option "1" del menú)

### Problema: Caracteres especiales no se ven bien
**Solución:** Asegurarse de que la consola está configurada para UTF-8:
```bash
# En Windows PowerShell:
chcp 65001
```

---

## 📊 Resultado Final Esperado

Después de la integración, el menú principal se vería así:

```
══════════════════════════════════════════════════
      ☀️  APLICACIÓN DEL TIEMPO
══════════════════════════════════════════════════
1. Clima actual
2. Clima en caché (si disponible)
3. Buscar ciudad
4. Convertir unidad de temperatura
5. 📊 PRONÓSTICO 5 DÍAS ← NUEVO
6. Estadísticas de caché
7. Salir
══════════════════════════════════════════════════
```

Y seleccionar opción "5" muestra un submenú interactivo para ver el pronóstico en diferentes formatos.

---

**Tiempo estimado de implementación:** 10-15 minutos
**Dificultad:** Fácil (solo copiar/pegar código)
**Dependencias nuevas:** Ninguna (usa componentes existentes)

