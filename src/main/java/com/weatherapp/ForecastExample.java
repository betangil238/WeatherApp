package com.weatherapp;

import com.weatherapp.client.OpenMeteoClient;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.model.ForecastData;
import com.weatherapp.service.WeatherForecastService;
import com.weatherapp.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Ejemplo completo: Cómo obtener y mostrar pronóstico de 5 días
 * 
 * Este programa demuestra:
 * 1. Solicitar ciudad al usuario
 * 2. Obtener datos climáticos actuales
 * 3. Generar pronóstico de 5 días
 * 4. Mostrar en diferentes formatos (detallado, resumen, estadísticas)
 * 5. Realizar análisis del pronóstico
 * 
 * Uso:
 *   java com.weatherapp.ForecastExample
 * 
 * El programa solicitará un nombre de ciudad y mostrará:
 * - Condiciones actuales
 * - Pronóstico completo de 5 días
 * - Estadísticas (día más caluroso, frío, lluvia, etc.)
 */
public class ForecastExample {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ForecastExample.class);
    
    public static void main(String[] args) {
        LOGGER.info("════════════════════════════════════════════════════════════════");
        LOGGER.info("🌤️  EJEMPLO: PRONÓSTICO DEL TIEMPO 5 DÍAS");
        LOGGER.info("════════════════════════════════════════════════════════════════");
        LOGGER.info("");
        
        try {
            // ============================================================
            // 1. INICIALIZAR SERVICIOS
            // ============================================================
            LOGGER.info("1️⃣  INICIALIZANDO SERVICIOS");
            
            OpenMeteoClient client = new OpenMeteoClient();
            TemperatureUnit unit = TemperatureUnit.CELSIUS;
            
            WeatherService weatherService = new WeatherService(client, unit);
            WeatherForecastService forecastService = new WeatherForecastService(
                    weatherService,
                    unit
            );
            
            LOGGER.info("✅ Servicios inicializados correctamente\n");
            
            // ============================================================
            // 2. SOLICITAR CIUDAD AL USUARIO
            // ============================================================
            LOGGER.info("2️⃣  INGRESA LA CIUDAD PARA EL PRONÓSTICO");
            LOGGER.info("");
            
            Scanner scanner = new Scanner(System.in);
            System.out.print("🔍 Ingresa el nombre de la ciudad (ej: Madrid, Barcelona): ");
            String cityName = scanner.nextLine().trim();
            
            if (cityName.isEmpty()) {
                LOGGER.error("❌ ERROR: Debes ingresar una ciudad válida.");
                LOGGER.info("");
                scanner.close();
                return;
            }
            
            LOGGER.info("✅ Ciudad seleccionada: {}\n", cityName);
            
            // ============================================================
            // 3. OBTENER PRONÓSTICO PARA LA CIUDAD
            // ============================================================
            LOGGER.info("3️⃣  OBTENIENDO PRONÓSTICO 5 DÍAS PARA {}", cityName.toUpperCase());
            LOGGER.info("");
            
            try {
                ForecastData forecast = forecastService.getForecast5Days(cityName);
                
                // Ver un dato rápido
                LOGGER.info("📍 Ciudad: {}", cityName);
                LOGGER.info("🌡️  Ahora: {}°C", 
                        forecast.getCurrentWeather().getTemperature());
                LOGGER.info("📅 Pronóstico: {} días\n", 
                        forecast.getForecastDays());
                
                // ============================================================
                // 4. MOSTRAR PRONÓSTICO EN DETALLE
                // ============================================================
                LOGGER.info("4️⃣  FORMATO DETALLADO");
                LOGGER.info("");
                
                forecastService.printForecast(forecast);
                
                // ============================================================
                // 5. MOSTRAR RESUMEN
                // ============================================================
                System.out.println("\n");
                LOGGER.info("5️⃣  FORMATO RESUMEN");
                LOGGER.info("");
                
                forecastService.printForecastSummary(forecast);
                
                // ============================================================
                // 6. MOSTRAR ESTADÍSTICAS
                // ============================================================
                System.out.println("\n");
                LOGGER.info("6️⃣  ESTADÍSTICAS DEL PRONÓSTICO");
                LOGGER.info("");
                
                forecastService.printForecastStats(forecast);
                
                // ============================================================
                // 7. CONSULTAR DÍA ESPECÍFICO
                // ============================================================
                System.out.println("\n");
                LOGGER.info("7️⃣  DATOS PARA UN DÍA ESPECÍFICO");
                LOGGER.info("");
                
                LOGGER.info("Pronóstico para mañana (índice 1):");
                String tomorrowInfo = forecastService.getForecastForDay(forecast, 1);
                System.out.println(tomorrowInfo);
                
                // ============================================================
                // 8. ANÁLISIS INTELIGENTE
                // ============================================================
                System.out.println("\n");
                LOGGER.info("8️⃣  ANÁLISIS INTELIGENTE");
                LOGGER.info("");
                
                String daysText = forecast.willRain(5) ? "próximos días" : "próximos días";
                LOGGER.info("💧 Pronóstico de lluvia en los {}: {}", 
                        daysText,
                        forecast.willRain(5) ? "Probable 🌧️" : "No esperada ☀️");
                
                if (forecast.getHottestDay() != null) {
                    LOGGER.info("🔥 Día más caluroso: {} ({:.1f}°C)",
                            forecast.getHottestDay().getDate(),
                            forecast.getHottestDay().getMaxTemperature());
                }
                
                if (forecast.getColdestDay() != null) {
                    LOGGER.info("❄️  Día más frío: {} ({:.1f}°C)",
                            forecast.getColdestDay().getDate(),
                            forecast.getColdestDay().getMinTemperature());
                }
                
                double avgTemp = forecast.getAverageTemperature(5);
                if (!Double.isNaN(avgTemp)) {
                    LOGGER.info("📊 Temperatura promedio 5 días: {:.1f}°C", avgTemp);
                }
                
            } catch (Exception e) {
                LOGGER.error("❌ Error al obtener pronóstico para {}: {}", cityName, e.getMessage());
                LOGGER.info("");
            }
            
            LOGGER.info("");
            LOGGER.info("════════════════════════════════════════════════════════════════");
            LOGGER.info("✅ Pronóstico completado");
            LOGGER.info("════════════════════════════════════════════════════════════════");
            
            scanner.close();
            
        } catch (Exception e) {
            LOGGER.error("❌ ERROR FATAL: {}", e.getMessage(), e);
            System.err.println("\n⚠️  Error al ejecutar el ejemplo:");
            e.printStackTrace();
        }
    }
}
