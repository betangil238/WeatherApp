package com.weatherapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Gestor de configuración persistente de la aplicación.
 * Maneja la carga y almacenamiento de preferencias del usuario (unidad de temperatura, etc.)
 * en un archivo de configuración en el home del usuario: ~/.weatherapp/config.properties
 */
public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private Properties properties;
    private boolean isFirstRun;

    /**
     * Constructor que carga la configuración existente o crea una nueva.
     * Si es la primera ejecución, retorna la configuración por defecto.
     */
    public ConfigManager() {
        this.properties = new Properties();
        this.isFirstRun = false;

        try {
            File configFile = new File(AppConfig.CONFIG_FILE);
            
            if (configFile.exists()) {
                loadConfiguration();
            } else {
                this.isFirstRun = true;
                createDefaultConfiguration();
            }
        } catch (IOException e) {
            LOGGER.warn("Error loading config: {}", e.getMessage());
            LOGGER.warn("Using default configuration");
            this.isFirstRun = true;
            createDefaultConfiguration();
        }
    }

    /**
     * Carga la configuración desde el archivo de propiedades.
     *
     * @throws IOException si ocurre error al leer el archivo
     */
    private void loadConfiguration() throws IOException {
        try (FileInputStream fis = new FileInputStream(AppConfig.CONFIG_FILE)) {
            properties.load(fis);
            LOGGER.debug("Configuration loaded from: {}", AppConfig.CONFIG_FILE);
        }
    }

    /**
     * Crea la configuración por defecto sin guardar en archivo.
     * Se usa cuando es la primera ejecución.
     */
    private void createDefaultConfiguration() {
        properties.setProperty(AppConfig.CONFIG_PROPERTY_TEMPERATURE_UNIT,
                             AppConfig.DEFAULT_TEMPERATURE_UNIT.name());
        LOGGER.debug("Default configuration created");
    }

    /**
     * Obtiene la unidad de temperatura guardada en la configuración.
     * Si no existe preferencia guardada, retorna la unidad por defecto.
     *
     * @return TemperatureUnit guardada o DEFAULT_TEMPERATURE_UNIT
     */
    public TemperatureUnit getTemperatureUnit() {
        String unitName = properties.getProperty(AppConfig.CONFIG_PROPERTY_TEMPERATURE_UNIT,
                                                  AppConfig.DEFAULT_TEMPERATURE_UNIT.name());
        return TemperatureUnit.fromString(unitName);
    }

    /**
     * Guarda la unidad de temperatura seleccionada en la configuración
     * y persiste en el archivo de propiedades.
     *
     * @param unit TemperatureUnit a guardar
     */
    public void saveTemperatureUnit(TemperatureUnit unit) {
        properties.setProperty(AppConfig.CONFIG_PROPERTY_TEMPERATURE_UNIT, unit.name());
        saveConfiguration();
    }

    /**
     * Guarda todas las propiedades en el archivo de configuración.
     */
    private void saveConfiguration() {
        try {
            // Crear directorio si no existe
            File configDir = new File(AppConfig.CONFIG_DIR);
            if (!configDir.exists()) {
                if (!configDir.mkdirs()) {
                    LOGGER.warn("Failed to create config directory: {}", AppConfig.CONFIG_DIR);
                    return;
                }
            }

            // Guardar propiedades en archivo
            try (FileOutputStream fos = new FileOutputStream(AppConfig.CONFIG_FILE)) {
                properties.store(fos, "Weather App Configuration - Do not edit manually");
                LOGGER.debug("Configuration saved to: {}", AppConfig.CONFIG_FILE);
            }
        } catch (IOException e) {
            LOGGER.error("Error saving configuration: {}", e.getMessage());
        }
    }

    /**
     * Retorna true si es la primera ejecución de la aplicación.
     *
     * @return true si no existe archivo de configuración previo
     */
    public boolean isFirstRun() {
        return isFirstRun;
    }

    /**
     * Resetea la configuración a valores por defecto.
     * Útil para testing o reinicio.
     */
    public void resetToDefaults() {
        properties.clear();
        createDefaultConfiguration();
        isFirstRun = true;
    }

    /**
     * Retorna la unidad de temperatura actual como String.
     *
     * @return nombre de la unidad (CELSIUS, FAHRENHEIT, KELVIN)
     */
    @Override
    public String toString() {
        return String.format("ConfigManager{unit=%s, firstRun=%s}",
                           getTemperatureUnit().getName(), isFirstRun);
    }
}
