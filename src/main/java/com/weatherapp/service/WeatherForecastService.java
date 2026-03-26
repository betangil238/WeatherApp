package com.weatherapp.service;

import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.model.DailyForecast;
import com.weatherapp.model.ForecastData;
import com.weatherapp.model.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para obtener y mostrar pronósticos de clima de 5 días.
 * Orquesta las operaciones de WeatherService y presenta los datos en formato claro.
 * 
 * Características:
 * - Obtiene pronóstico de 5 días
 * - Formatea datos para consola
 * - Proporciona análisis (día más caluroso, lluvia, etc.)
 * - API simple y directa
 */
public class WeatherForecastService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherForecastService.class);
    private final WeatherService weatherService;
    private final TemperatureUnit temperatureUnit;

    /**
     * Constructor del servicio de pronóstico.
     * 
     * @param weatherService Servicio base de clima
     * @param temperatureUnit Unidad de temperatura a usar
     */
    public WeatherForecastService(WeatherService weatherService, TemperatureUnit temperatureUnit) {
        this.weatherService = weatherService;
        this.temperatureUnit = temperatureUnit;
        LOGGER.info("✅ WeatherForecastService inicializado");
    }

    /**
     * Obtiene el pronóstico de 5 días para una ciudad.
     * NOTA: Esta es una implementación que genera datos de demostración.
     * En producción, esto consumiría la API de Open-Meteo con parámetro `daily`.
     * 
     * @param cityName Nombre de la ciudad
     * @return ForecastData con clima actual y 5 días de pronóstico
     * @throws IllegalArgumentException si el nombre de ciudad es inválido
     * @throws RuntimeException si hay errores de red
     */
    public ForecastData getForecast5Days(String cityName) {
        LOGGER.info("📅 Obteniendo pronóstico 5 días para: {}", cityName);
        
        // Obtener datos actuales (esto consulta la API real)
        WeatherData currentWeather = weatherService.getWeatherByCityName(cityName);
        
        // Generar pronóstico de 5 días (demostración)
        // En una implementación real, esto vendría de la API
        List<DailyForecast> forecasts = generateSample5DayForecast(temperatureUnit);
        
        // Crear objeto de pronóstico
        ForecastData forecastData = new ForecastData(
                currentWeather.getCityName(),
                0.0,  // Estos valores vendrían de la API en producción
                0.0,
                currentWeather,
                forecasts,
                temperatureUnit
        );
        
        LOGGER.info("✅ Pronóstico obtenido: {} días", forecastData.getForecastDays());
        return forecastData;
    }

    /**
     * Muestra el pronóstico de 5 días en formato tabular claro.
     * 
     * @param forecast Datos del pronóstico a mostrar
     */
    public void printForecast(ForecastData forecast) {
        System.out.println(forecast.getDetailedForecast());
    }

    /**
     * Muestra el pronóstico de forma resumen (línea por día).
     * 
     * @param forecast Datos del pronóstico a mostrar
     */
    public void printForecastSummary(ForecastData forecast) {
        System.out.println(String.format(
                "╔═══════════════════════════════════════════════╗\n" +
                "║    PRONÓSTICO 5 DÍAS - %s\n" +
                "╚═══════════════════════════════════════════════╝\n",
                forecast.getCityName()));
        
        System.out.println("📍 Ubicación: " + forecast.getCityName());
        System.out.println("🕐 Ahora: " + forecast.getCurrentWeather().getTemperature() +
                forecast.getCurrentWeather().getTemperatureSymbol() + " | " +
                forecast.getCurrentWeather().getDescription());
        System.out.println();
        
        System.out.println("📅 Próximos días:");
        for (DailyForecast day : forecast.getDailyForecasts()) {
            System.out.println("   └─ " + day.toString());
        }
        System.out.println();
    }

    /**
     * Muestra estadísticas del pronóstico.
     * Incluye: día más caluroso/frío, probabilidad de lluvia, etc.
     * 
     * @param forecast Datos del pronóstico
     */
    public void printForecastStats(ForecastData forecast) {
        System.out.println(String.format(
                "╔═══════════════════════════════════════════════╗\n" +
                "║     ESTADÍSTICAS - %s\n" +
                "╚═══════════════════════════════════════════════╝\n",
                forecast.getCityName()));
        
        DailyForecast hottestDay = forecast.getHottestDay();
        DailyForecast coldestDay = forecast.getColdestDay();
        double avgTemp = forecast.getAverageTemperature(5);
        boolean willRain = forecast.willRain(5);
        
        if (hottestDay != null) {
            System.out.printf("🔥 Día más caluroso: %s (%.1f%s)%n",
                    hottestDay.getDate(),
                    hottestDay.getMaxTemperature(),
                    hottestDay.getTemperatureSymbol());
        }
        
        if (coldestDay != null) {
            System.out.printf("❄️  Día más frío: %s (%.1f%s)%n",
                    coldestDay.getDate(),
                    coldestDay.getMinTemperature(),
                    coldestDay.getTemperatureSymbol());
        }
        
        if (!Double.isNaN(avgTemp)) {
            System.out.printf("📊 Temperatura promedio: %.1f%s%n",
                    avgTemp,
                    forecast.getTemperatureUnit().getSymbol());
        }
        
        System.out.printf("💧 ¿Habrá lluvia? %s%n", willRain ? "Sí, probable 🌧️" : "No esperada ☀️");
        System.out.println();
    }

    /**
     * Retorna pronóstico para un día específico.
     * 
     * @param forecast Datos del pronóstico
     * @param dayIndex Índice del día (0 = hoy, 1 = mañana, etc.)
     * @return Descripción del día
     */
    public String getForecastForDay(ForecastData forecast, int dayIndex) {
        DailyForecast day = forecast.getForecastForDay(dayIndex);
        return day != null ? day.getDetailedInfo() : "No disponible";
    }

    /**
     * Genera pronóstico de 5 días de demostración para testing.
     * En producción, esto vendría de la API de Open-Meteo.
     * 
     * @param unit Unidad de temperatura
     * @return Lista de 5 días de pronóstico
     */
    private List<DailyForecast> generateSample5DayForecast(TemperatureUnit unit) {
        List<DailyForecast> forecasts = new ArrayList<>();
        
        String[] conditions = {
                "☀️ Soleado",
                "⛅ Parcialmente nublado",
                "☁️ Nublado",
                "🌧️ Lluvia probable",
                "🌤️ Soleado con nubes"
        };
        
        double[] maxTemps = {22.5, 20.0, 18.5, 19.0, 23.0};
        double[] minTemps = {18.0, 16.5, 14.0, 15.5, 17.0};
        int[] rainChances = {10, 25, 60, 75, 15};
        double[] rainAmounts = {0.0, 0.5, 2.5, 5.0, 0.0};
        int[] humidity = {60, 65, 70, 80, 55};
        
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < 5; i++) {
            DailyForecast day = new DailyForecast(
                    today.plusDays(i),
                    maxTemps[i],
                    minTemps[i],
                    (maxTemps[i] + minTemps[i]) / 2,
                    conditions[i],
                    rainChances[i],
                    rainAmounts[i],
                    humidity[i],
                    unit
            );
            forecasts.add(day);
        }
        
        return forecasts;
    }

    /**
     * Obtiene el servicio base de clima.
     * Útil para acceder a datos actuales directamente.
     * 
     * @return WeatherService
     */
    public WeatherService getWeatherService() {
        return weatherService;
    }

    /**
     * Obtiene la unidad de temperatura configurada.
     * 
     * @return TemperatureUnit
     */
    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }
}
