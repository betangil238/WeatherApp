package com.weatherapp;

import com.weatherapp.client.OpenMeteoClient;
import com.weatherapp.config.ConfigManager;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.exception.NetworkException;
import com.weatherapp.exception.WeatherDataException;
import com.weatherapp.model.WeatherData;
import com.weatherapp.presentation.ConsoleUI;
import com.weatherapp.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Punto de entrada de la aplicación.
 * Orquesta la inicialización y el loop principal:
 * 1. Carga/selecciona unidad de temperatura
 * 2. Inicializa dependencias
 * 3. Loop interactivo: entrada usuario → búsqueda → visualización
 * 4. Manejo global de excepciones
 */
public class Main {

    
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ConsoleUI ui = null;

        try {
            // ====== FASE 1: INICIALIZACIÓN ======
            ui = new ConsoleUI();
            ui.showWelcomeMessage();

            LOGGER.info("Inicializando aplicación...");

            // Cargar o seleccionar unidad de temperatura
            ConfigManager configManager = new ConfigManager();
            TemperatureUnit selectedUnit;

            if (configManager.isFirstRun()) {
                // Primera ejecución: mostrar menú de selección
                LOGGER.info("Primera ejecución detectada");
                selectedUnit = ui.showTemperatureUnitMenu();
                configManager.saveTemperatureUnit(selectedUnit);
            } else {
                // Cargar unidad guardada
                selectedUnit = configManager.getTemperatureUnit();
                LOGGER.info("Unidad de temperatura cargada: {}", selectedUnit.getName());
            }

            // Inicializar dependencias
            OpenMeteoClient client = new OpenMeteoClient();
            WeatherService weatherService = new WeatherService(client, selectedUnit);

            LOGGER.info("✅ Aplicación inicializada correctamente");
            System.out.println();

            // ====== FASE 2: LOOP PRINCIPAL ======
            boolean continueRunning = true;

            while (continueRunning) {
                try {
                    // Mostrar menú principal
                    int choice = ui.showMainMenu();

                    if (choice == 2) {
                        // OPCIÓN 2: Múltiples ciudades con comparativo
                        handleMultipleCitiesMode(ui, weatherService);
                    } else {
                        // OPCIÓN 1: Una sola ciudad (modo clásico)
                        handleSingleCityMode(ui, weatherService);
                    }

                    // Preguntar si continuar
                    continueRunning = ui.askContinue();

                } catch (IllegalArgumentException e) {
                    // Error de validación de entrada del usuario
                    ui.showError("[ENTRADA INVÁLIDA] " + e.getMessage());
                    System.out.println("   Por favor, intenta nuevamente.");
                    // Continuar el loop para reintentar

                } catch (CityNotFoundException e) {
                    // Ciudad no encontrada - no es recuperable por reintentos de red
                    ui.showError("[CIUDAD NO ENCONTRADA] " + e.getMessage());
                    System.out.println("   Por favor, intenta con otro nombre de ciudad.");
                    // Continuar el loop para reintentar con otra ciudad

                } catch (WeatherDataException e) {
                    // Datos climáticos inválidos
                    ui.showError("[DATOS INVÁLIDOS] " + e.getMessage());
                    System.out.println("   Por favor, intenta con otro nombre de ciudad.");
                    // Continuar el loop para reintentar

                } catch (NetworkException e) {
                    // Error de red - potencialmente recuperable
                    ui.showError("[ERROR DE RED] " + e.getMessage());
                    System.out.println();

                    // Preguntar si reintentar o introducir otra ciudad
                    continueRunning = ui.askForRetry();
                }
            }

            // ====== FASE 3: LIMPIEZA Y DESPEDIDA ======
            ui.showGoodbyeMessage();
            LOGGER.info("Aplicación finalizada.");

        } catch (Exception e) {
            // Error fatal no capturado
            LOGGER.error("[FATAL] Error fatal: {}", e.getMessage(), e);
            System.exit(1);

        } finally {
            // Cerrar recursos
            if (ui != null) {
                ui.close();
            }
        }
    }

    /**
     * Maneja la consulta de clima para una sola ciudad.
     * Solicita al usuario un nombre de ciudad y muestra sus datos climáticos.
     *
     * @param ui Interfaz de usuario
     * @param weatherService Servicio de clima
     */
    private static void handleSingleCityMode(ConsoleUI ui, WeatherService weatherService) {
        // Solicitar ciudad al usuario
        String cityName = ui.getUserCity();

        // Obtener datos climáticos
        WeatherData weatherData = weatherService.getWeatherByCityName(cityName);

        // Mostrar resultados
        ui.displayWeather(weatherData);
    }

    /**
     * Maneja la consulta de clima para múltiples ciudades en modo comparativo.
     * Solicita al usuario varios nombres de ciudades y muestra sus datos en un formato comparativo.
     *
     * @param ui Interfaz de usuario
     * @param weatherService Servicio de clima
     */
    private static void handleMultipleCitiesMode(ConsoleUI ui, WeatherService weatherService) {
        // Solicitar múltiples ciudades al usuario
        List<String> cityNames = ui.getMultipleCities();

        // Obtener datos climáticos para todas las ciudades
        List<WeatherData> weatherDataList = weatherService.getWeatherByMultipleCities(cityNames);

        // Mostrar comparativo
        ui.displayWeatherComparative(weatherDataList);
    }
}
