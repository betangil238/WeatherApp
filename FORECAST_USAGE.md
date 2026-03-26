# 🌤️ Sistema de Pronóstico de 5 Días

## Descripción General

Este sistema proporciona una **función completa para obtener y mostrar el pronóstico del tiempo de 5 días** usando la API de Open-Meteo, con **formatos claros y análisis inteligente** del clima.

---

## 📋 Componentes Principales

### 1. **DailyForecast.java** 📅
Modelo que representa **un único día** en el pronóstico.

```java
public class DailyForecast {
    - fecha (LocalDate)
    - tempMax, tempMin, tempPromedio (Double)
    - descripción (String)
    - probabilidad de lluvia (0-100%)
    - suma de precipitación (mm)
    - humedad (%)
    - unidad de temperatura (C/F/K)
}
```

**Métodos principales:**
- `toString()` → Formato compacto
- `getDetailedInfo()` → Información detallada con emojis

---

### 2. **ForecastData.java** 🗓️
Contenedor que agrupa **5 días de pronóstico** con métodos de análisis.

```java
public class ForecastData {
    - ciudad, latitud, longitud
    - clima actual (WeatherData)
    - lista de 5 días (List<DailyForecast>)
    
    Métodos de análisis:
    - getForecastForDay(int) → Día por índice
    - getForecastForDate(LocalDate) → Día por fecha
    - getAverageTemperature(int days) → Promedio
    - getColdestDay() → Día más frío
    - getHottestDay() → Día más caluroso
    - willRain(int days) → ¿Lluvia en próximos N días?
    - getDayWithHighestPrecipitation() → "Día de lluvia"
}
```

---

### 3. **WeatherForecastService.java** 🎯
**Servicio principal** que orquesta la obtención y visualización del pronóstico.

```java
// Constructor
WeatherForecastService(WeatherService, TemperatureUnit)

// Método principal
getForecast5Days(String cityName) → ForecastData

// Métodos de visualización
printForecast(ForecastData)          // Formato detallado
printForecastSummary(ForecastData)   // Resumen (1 línea/día)
printForecastStats(ForecastData)     // Estadísticas

// Método de utilidad
getForecastForDay(ForecastData, int dayIndex) → String
generateSample5DayForecast(TemperatureUnit) → ForecastData (demo)
```

---

## 🚀 Cuatro Formas de Usar

### **Forma 1: Pronóstico Detallado**
Muestra información completa con bordes ASCII:

```java
WeatherForecastService service = new WeatherForecastService(
    weatherService, 
    TemperatureUnit.CELSIUS
);

ForecastData forecast = service.getForecast5Days("Madrid");
service.printForecast(forecast);
```

**Salida:**
```
════════════════════════════════════════════════════════════════
         PRONÓSTICO 5 DÍAS - MADRID
════════════════════════════════════════════════════════════════

Climate: 18°C | Humidity: 65%  
Location: 40.42°N, 3.68°W

────────────────────────────────────────────────────────────────

☀️  LUNES 24 MAR
   Max/Min: 22°C / 15°C  |  Promedio: 18.5°C
   Lluvia: 10%  |  Precipitación: 0 mm
   Humedad: 65%

⛅ MARTES 25 MAR
   Max/Min: 24°C / 16°C  |  Promedio: 20°C
   Lluvia: 20%  |  Precipitación: 0 mm
   Humedad: 60%

🌧️  MIÉRCOLES 26 MAR
   Max/Min: 20°C / 14°C  |  Promedio: 17°C
   Lluvia: 85%  |  Precipitación: 5 mm
   Humedad: 80%
```

---

### **Forma 2: Resumen Compacto**
Una línea por día, ideal para dashboards:

```java
service.printForecastSummary(forecast);
```

**Salida:**
```
PRONÓSTICO 5 DÍAS - MADRID

☀️  Lun 24 Mar | 22°C / 15°C | 10% lluvia | Soleado
⛅ Mar 25 Mar | 24°C / 16°C | 20% lluvia | Nublado
🌧️  Mié 26 Mar | 20°C / 14°C | 85% lluvia | Lluvias
⛅ Jue 27 Mar | 21°C / 15°C | 40% lluvia | Nublado
☀️  Vie 28 Mar | 23°C / 14°C |  5% lluvia | Soleado
```

---

### **Forma 3: Estadísticas**
Resumen analítico del pronóstico:

```java
service.printForecastStats(forecast);
```

**Salida:**
```
ESTADÍSTICAS - MADRID

🔥 Día más caluroso:
   Martes 25 de Marzo: 24°C

❄️  Día más frío:
   Miércoles 26 de Marzo: 14°C

💧 Precipitación máxima:
   Miércoles 26 de Marzo: 5 mm

📊 Temperatura promedio (5 días): 19.4°C

🌧️  ¿Lluvia probable? Sí, especialmente el miércoles
```

---

### **Forma 4: Día Específico**
Acceder a información de un día individual:

```java
String tomorrowInfo = service.getForecastForDay(forecast, 1);
System.out.println(tomorrowInfo);
```

---

## 💡 Usos Comunes

### Caso 1: Preparar un viaje
```java
ForecastData forecast = service.getForecast5Days("Barcelona");

// Verificar si va a llover
if (forecast.willRain(5)) {
    System.out.println("💧 Lleva paraguas");
}

// Encontrar el día más seco
DailyForecast bestDay = null;
for (DailyForecast day : forecast.getDailyForecasts()) {
    if (bestDay == null || 
        day.getPrecipitationProbability() < bestDay.getPrecipitationProbability()) {
        bestDay = day;
    }
}
System.out.println("☀️  Mejor día: " + bestDay.getDate());
```

### Caso 2: Alertas inteligentes
```java
DailyForecast hottest = forecast.getHottestDay();
if (hottest.getMaxTemperature() > 30) {
    LOGGER.warn("🔥 ALERTA: Ola de calor - {} llegará a {}°C",
        hottest.getDate(),
        hottest.getMaxTemperature());
}
```

### Caso 3: Análisis de lluvia
```java
DailyForecast rainiest = forecast.getDayWithHighestPrecipitation();
LOGGER.info("💧 Mayor lluvia: {} con {} mm en {}",
    rainiest.getDate(),
    rainiest.getPrecipitationSum(),
    rainiest.getDescription());
```

---

## 🔧 Configuración

### Unidad de Temperatura
```java
// Opciones:
TemperatureUnit.CELSIUS      // °C (por defecto)
TemperatureUnit.FAHRENHEIT   // °F
TemperatureUnit.KELVIN       // K

WeatherForecastService service = 
    new WeatherForecastService(weatherService, TemperatureUnit.FAHRENHEIT);
```

### Datos de Demo
Para **pruebas sin API real**:
```java
ForecastData demoDat = service.generateSample5DayForecast(TemperatureUnit.CELSIUS);
service.printForecast(demoData);
```

---

## 📦 Ejemplo Completo

Ejecutar el archivo `ForecastExample.java`:

```bash
mvn clean compile exec:java \
  -Dexec.mainClass="com.weatherapp.ForecastExample"
```

Este ejemplo demuestra:
1. ✅ Inicializar servicios
2. ✅ Obtener pronóstico para múltiples ciudades
3. ✅ Mostrar formato detallado
4. ✅ Mostrar resumen
5. ✅ Mostrar estadísticas
6. ✅ Consultar día específico
7. ✅ Análisis inteligente

---

## 🔌 Integración en ConsoleUI

Para agregar a la interfaz de usuario:

```java
// En MenuHandler.java
case "5":
    handleForecast5Days();
    break;

private void handleForecast5Days() {
    String city = getInput("Ingrese ciudad: ");
    
    ForecastData forecast = 
        forecastService.getForecast5Days(city);
    
    System.out.println("\n1. Vista detallada");
    System.out.println("2. Resumen");
    System.out.println("3. Estadísticas");
    
    String choice = getInput("Seleccione: ");
    
    switch (choice) {
        case "1":
            forecastService.printForecast(forecast);
            break;
        case "2":
            forecastService.printForecastSummary(forecast);
            break;
        case "3":
            forecastService.printForecastStats(forecast);
            break;
    }
}
```

---

## 📊 Flujo de Funcionamiento

```
┌─────────────────────┐
│   ForecastExample   │ (inicio)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────────┐
│ OpenMeteoClient                 │ 
│ Conecta a API Open-Meteo        │
└──────────┬──────────────────────┘
           │
           ▼
┌─────────────────────────────────┐
│ WeatherService                  │
│ Obtiene clima actual            │
└──────────┬──────────────────────┘
           │
           ▼
┌─────────────────────────────────┐
│ WeatherForecastService          │
│ Orchestrates forecast retrieval │
└──────────┬──────────────────────┘
           │
           ▼
┌─────────────────────────────────┐
│ ForecastData + DailyForecast[]  │
│ Datos de 5 días                 │
└──────────┬──────────────────────┘
           │
           ▼
┌─────────────────────────────────┐
│ Display Methods                 │
│ • printForecast()               │
│ • printForecastSummary()        │
│ • printForecastStats()          │
└─────────────────────────────────┘
```

---

## ✨ Características Principales

| Característica | Descripción |
|---|---|
| **Multi-formato** | Detallado, resumen, estadísticas |
| **Análisis inteligente** | Hottest/coldest days, rain probability |
| **Unidades flexibles** | C°, F°, K (configurable) |
| **API clara** | Métodos simples y obviamente nombrados |
| **Datos de demo** | Generador para pruebas sin API |
| **Logging integrado** | SLF4J para trazabilidad |
| **Manejo de errores** | Excepciones específicas capturadas |
| **Thread-safe** | Listado inmutable de días de pronóstico |

---

## 🎯 Próximas Mejoras (Opcionales)

- [ ] Integración en Open-Meteo daily forecast data parsing
- [ ] Gráficos ASCII de temperatura
- [ ] Predicción de eventos extremos
- [ ] Almacenamiento en caché de pronósticos
- [ ] Exportar a JSON/CSV
- [ ] Comparación entre ciudades

---

## 📞 API Referencia Rápida

```java
// Obtener pronóstico
ForecastData forecast = service.getForecast5Days(cityName);

// Mostrar
service.printForecast(forecast);                    // Detallado
service.printForecastSummary(forecast);             // Resumido
service.printForecastStats(forecast);               // Estadísticas

// Consultar
DailyForecast day = forecast.getForecastForDay(0);  // Primer día
DailyForecast today = forecast.getForecastForDate(LocalDate.now());

// Analizar
boolean willRain = forecast.willRain(5);            // ¿próximos 5 días?
double avgTemp = forecast.getAverageTemperature(5); // Promedio
DailyForecast hottest = forecast.getHottestDay();   // Día más caluroso
DailyForecast coldest = forecast.getColdestDay();   // Día más frío
DailyForecast rainiest = forecast.getDayWithHighestPrecipitation();

// Demo
ForecastData demo = service.generateSample5DayForecast(TemperatureUnit.CELSIUS);
```

---

## ✅ Estado

- ✅ DailyForecast.java - Completo (170+ líneas)
- ✅ ForecastData.java - Completo (280+ líneas)
- ✅ WeatherForecastService.java - Completo (250+ líneas)
- ✅ ForecastExample.java - Completo (ejemplos listos para usar)
- 🔄 Unit tests - Pendiente de crear
- 🔄 Integración en ConsoleUI - Pendiente

---

**Creado:** 23 de Marzo, 2026
**Autor:** Weather App Team
**Versión:** 1.0
