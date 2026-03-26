package com.weatherapp.client;

import com.weatherapp.config.AppConfig;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.exception.WeatherDataException;
import com.weatherapp.model.GeoLocation;
import com.weatherapp.model.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validador de datos climáticos y geográficos.
 * Responsable de verificar que los datos estén dentro de rangos realistas.
 */
public class WeatherDataValidator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherDataValidator.class);

    /**
     * Valida que una ubicación geográfica tenga coordenadas dentro de rangos válidos.
     * Latitud debe estar en [-90, 90] y longitud en [-180, 180].
     *
     * @param location Ubicación a validar
     * @throws WeatherDataException si las coordenadas están fuera de rango
     */
    public void validateGeoLocation(GeoLocation location) {
        if (location.getLatitude() < AppConfig.LATITUDE_MIN || 
            location.getLatitude() > AppConfig.LATITUDE_MAX) {
            String message = String.format(
                    "❌ Latitud inválida: %.4f. Debe estar entre %.1f y %.1f",
                    location.getLatitude(), 
                    AppConfig.LATITUDE_MIN, 
                    AppConfig.LATITUDE_MAX);
            LOGGER.warn("Validación de geolocalización fallida: {}", message);
            throw new WeatherDataException("Latitud", location.getLatitude(), message);
        }

        if (location.getLongitude() < AppConfig.LONGITUDE_MIN || 
            location.getLongitude() > AppConfig.LONGITUDE_MAX) {
            String message = String.format(
                    "❌ Longitud inválida: %.4f. Debe estar entre %.1f y %.1f",
                    location.getLongitude(), 
                    AppConfig.LONGITUDE_MIN, 
                    AppConfig.LONGITUDE_MAX);
            LOGGER.warn("Validación de geolocalización fallida: {}", message);
            throw new WeatherDataException("Longitud", location.getLongitude(), message);
        }
    }

    /**
     * Valida que los datos climáticos estén dentro de rangos realistas.
     * Verifica: humedad [0-100%], temperatura realista, temperatura aparente realista.
     *
     * @param weatherData Datos climáticos a validar
     * @throws WeatherDataException si algún valor está fuera de rango
     */
    public void validateWeatherData(WeatherData weatherData) {
        // Validar humedad
        if (weatherData.getHumidity() < AppConfig.HUMIDITY_MIN || 
            weatherData.getHumidity() > AppConfig.HUMIDITY_MAX) {
            String message = String.format(
                    "❌ Humedad inválida: %d%%. Debe estar entre %d%% y %d%%",
                    weatherData.getHumidity(), 
                    AppConfig.HUMIDITY_MIN, 
                    AppConfig.HUMIDITY_MAX);
            LOGGER.warn("Validación de humedad fallida: {}", message);
            throw new WeatherDataException("Humedad", weatherData.getHumidity(), message);
        }

        // Validar temperatura actual
        validateTemperature(weatherData, "Temperatura", weatherData.getTemperature());
        
        // Validar temperatura aparente
        validateTemperature(weatherData, "Temperatura aparente", weatherData.getApparentTemperature());
    }

    /**
     * Valida que una temperatura esté en rango realista.
     * Convierte a Celsius como punto de referencia universal.
     */
    private void validateTemperature(WeatherData weatherData, String fieldName, double tempValue) {
        double tempCelsius = convertToCelsius(tempValue, weatherData.getTemperatureUnit());
        
        if (tempCelsius < AppConfig.TEMPERATURE_MIN_CELSIUS || 
            tempCelsius > AppConfig.TEMPERATURE_MAX_CELSIUS) {
            String message = String.format(
                    "❌ %s irreal: %.1f %s (%.1f°C). Debe estar entre %.1f°C y %.1f°C",
                    fieldName,
                    tempValue,
                    weatherData.getTemperatureSymbol(),
                    tempCelsius,
                    AppConfig.TEMPERATURE_MIN_CELSIUS,
                    AppConfig.TEMPERATURE_MAX_CELSIUS);
            LOGGER.warn("Validación de temperatura fallida: {}", message);
            throw new WeatherDataException(fieldName, tempValue, message);
        }
    }

    /**
     * Convierte una temperatura a Celsius.
     */
    private double convertToCelsius(double temperature, TemperatureUnit unit) {
        if (unit == TemperatureUnit.FAHRENHEIT) {
            return (temperature - 32) * 5 / 9;
        } else if (unit == TemperatureUnit.KELVIN) {
            return temperature - 273.15;
        }
        return temperature; // Ya está en Celsius
    }
}
