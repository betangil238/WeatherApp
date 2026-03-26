# ⚡ Quick Start: Pronóstico 5 Días en 5 Minutos

## 🎯 En este archivo

Prueba el sistema en **menos de 5 minutos** sin leer documentación.

---

## Step 1: Compilar (1 minuto)

```bash
cd c:\Users\betan\weather-app
mvn clean compile
```

**Resultado esperado:** `BUILD SUCCESS`

---

## Step 2: Ejecutar Ejemplo (2 minutos)

```bash
mvn exec:java -Dexec.mainClass="com.weatherapp.ForecastExample"
```

**Verás:**
- ✅ Clima actual para Madrid, Barcelona, Nueva York
- ✅ Pronóstico detallado de 5 días
- ✅ Resumen compacto
- ✅ Estadísticas
- ✅ Análisis inteligente

---

## Step 3: Ver el Código (1 minuto)

**Archivo principal:** [src/main/java/com/weatherapp/ForecastExample.java](src/main/java/com/weatherapp/ForecastExample.java)

**Componentes creados:**
- `DailyForecast` - 1 día de pronóstico
- `ForecastData` - 5 días + análisis
- `WeatherForecastService` - Servicio

---

## Step 4: Usar en tu Código (1 minuto)

```java
// 1. Crear servicio
WeatherForecastService service = new WeatherForecastService(
    weatherService,
    TemperatureUnit.CELSIUS
);

// 2. Obtener pronóstico
ForecastData forecast = service.getForecast5Days("Madrid");

// 3. Mostrar - Elige uno:
service.printForecast(forecast);           // Detallado
service.printForecastSummary(forecast);    // Resumido
service.printForecastStats(forecast);      // Estadísticas
```

---

## 4 Formas de Mostrar

```java
// 1️⃣ DETALLADO - Bordes ASCII
service.printForecast(forecast);

// 2️⃣ RESUMEN - Una línea/día
service.printForecastSummary(forecast);

// 3️⃣ ESTADÍSTICAS - Análisis
service.printForecastStats(forecast);

// 4️⃣ DÍA ESPECÍFICO - Por índice
String day1 = service.getForecastForDay(forecast, 0);
System.out.println(day1);
```

---

## Análisis Inteligente

```java
// ¿Va a llover en próximos 5 días?
if (forecast.willRain(5)) {
    System.out.println("💧 Sí, llevar paraguas");
}

// Encontrar día más caluroso
DailyForecast hottest = forecast.getHottestDay();
System.out.println("🔥 " + hottest.getDate() + ": " + 
    hottest.getMaxTemperature() + "°C");

// Encontrar día más frío
DailyForecast coldest = forecast.getColdestDay();
System.out.println("❄️  " + coldest.getDate() + ": " + 
    coldest.getMinTemperature() + "°C");

// Temperatura promedio 5 días
double avg = forecast.getAverageTemperature(5);
System.out.println("📊 Promedio: " + avg + "°C");

// Día con más lluvia
DailyForecast rainiest = forecast.getDayWithHighestPrecipitation();
System.out.println("🌧️  " + rainiest.getDate() + ": " + 
    rainiest.getPrecipitationSum() + " mm");
```

---

## Para Integrar en ConsoleUI

Ver: [FORECAST_INTEGRATION.md](FORECAST_INTEGRATION.md)

Pasos de copy/paste para agregar opción 5 al menú.

---

## Para Entender Todo

Ver: [FORECAST_COMPLETED.md](FORECAST_COMPLETED.md) → Resumen completo

---

## Archivos Creados

```
src/main/java/com/weatherapp/
├── model/
│   ├── DailyForecast.java          ✅ 170+ líneas
│   └── ForecastData.java           ✅ 280+ líneas
├── service/
│   └── WeatherForecastService.java ✅ 250+ líneas
└── ForecastExample.java            ✅ 200+ líneas
```

---

## ✅ Control Final

- ✅ Compilación exitosa
- ✅ Ejemplo funcional
- ✅ 4 formatos de visualización
- ✅ Análisis inteligente
- ✅ Documentación completa

---

## 🎯 Siguientes 10 Minutos (Opcional)

1. Integrar en ConsoleUI (~5 min)
2. Ver documentación FORECAST_USAGE.md (~5 min)

---

**¡Listo! Ya tienes el sistema funcionando.** 🎉

Para más detalles: [FORECAST_COMPLETED.md](FORECAST_COMPLETED.md)

