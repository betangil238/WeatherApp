package com.weatherapp.service;

import com.weatherapp.client.OpenMeteoClient;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.exception.NetworkException;
import com.weatherapp.exception.WeatherAppException;
import com.weatherapp.exception.WeatherDataException;
import com.weatherapp.model.GeoLocation;
import com.weatherapp.model.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de lógica de negocio para obtener datos climáticos.
 * Orquesta las operaciones del cliente HTTP para:
 * 1. Resolver el nombre de una ciudad a coordenadas
 * 2. Obtener datos climáticos de esa ubicación
 * 3. Validar entrada del usuario
 *
 * Esta es la capa intermedia entre la UI y el cliente HTTP.
 */
public class WeatherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherService.class);
    private final OpenMeteoClient openMeteoClient;
    private final TemperatureUnit temperatureUnit;

    /**
     * Constructor inyectando el cliente HTTP y la unidad de temperatura.
     *
     * @param openMeteoClient Cliente HTTP para llamadas a Open-Meteo
     * @param temperatureUnit Unidad de temperatura a usar (C, F, K)
     */
    public WeatherService(OpenMeteoClient openMeteoClient, TemperatureUnit temperatureUnit) {
        this.openMeteoClient = openMeteoClient;
        this.temperatureUnit = temperatureUnit;
    }

    /**
     * Obtiene datos climáticos para una ciudad especificada por nombre.
     * Orquesta: validación → geocodificación → obtención de datos climáticos.
     *
     * @param cityName Nombre de la ciudad a buscar
     * @return WeatherData con información completa del clima
     * @throws IllegalArgumentException si el nombre de ciudad es inválido o no encontrado
     * @throws RuntimeException si ocurren errores de red o parsing
     */
    public WeatherData getWeatherByCityName(String cityName) {
        String normalizedCityName = validateAndNormalizeCityName(cityName);
        
        try {
            LOGGER.info("Buscando clima para: '{}'", normalizedCityName);
            
            GeoLocation location = openMeteoClient.resolveCity(normalizedCityName);
            LOGGER.debug("Ubicación resuelta: {}", location);
            
            WeatherData weatherData = openMeteoClient.getWeatherData(location, temperatureUnit);
            
            LOGGER.info("✅ Datos climáticos obtenidos exitosamente para: {}", location.getCityName());
            return weatherData;

        } catch (CityNotFoundException e) {
            // Ciudad no encontrada - no es recuperable
            LOGGER.warn("Ciudad no encontrada: '{}'", normalizedCityName, e);
            throw e;
        } catch (WeatherDataException e) {
            // Error de validación de datos
            LOGGER.warn("Validación fallida para '{}': {}", normalizedCityName, e.getMessage(), e);
            throw e;
        } catch (WeatherAppException e) {
            // Otras excepciones del dominio
            LOGGER.warn("Error del dominio para '{}': {}", normalizedCityName, e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            // Error de red, timeout, parsing JSON, u otros errores de runtime no esperados
            LOGGER.error("Error de conectividad para '{}'", normalizedCityName, e);
            throw new NetworkException(
                    "Imposible obtener datos climáticos para '" + normalizedCityName + 
                    "'. Verifica tu conexión de red.", e);
        }
    }

    /**
     * Valida y normaliza el nombre de la ciudad (trimming).
     * 
     * @param cityName Nombre de ciudad a validar
     * @return Nombre de ciudad normalizado (sin espacios en blanco)
     * @throws IllegalArgumentException si el nombre es nulo o vacío
     */
    private String validateAndNormalizeCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ El nombre de la ciudad no puede estar vacío");
        }
        return cityName.trim();
    }

    /**
     * Retorna la unidad de temperatura actual.
     *
     * @return TemperatureUnit configurada
     */
    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    /**
     * Obtiene datos climáticos para múltiples ciudades.
     * Realiza consultas individuales para cada ciudad y agrupa los resultados.
     * Las ciudades que no se encuentren o causen errores se excluyen de los resultados
     * con un mensaje de advertencia en los logs.
     *
     * @param cityNames Lista de nombres de ciudades a buscar
     * @return Lista de WeatherData para las ciudades encontradas exitosamente
     * @throws IllegalArgumentException si la lista de ciudades está vacía
     */
    public List<WeatherData> getWeatherByMultipleCities(List<String> cityNames) {
        if (cityNames == null || cityNames.isEmpty()) {
            throw new IllegalArgumentException("❌ Debes proporcionar al menos una ciudad");
        }

        LOGGER.info("Obteniendo clima para {} ciudades", cityNames.size());

        List<WeatherData> results = new ArrayList<>();

        for (String cityName : cityNames) {
            try {
                WeatherData weatherData = getWeatherByCityName(cityName);
                results.add(weatherData);
            } catch (CityNotFoundException e) {
                LOGGER.warn("Ciudad no encontrada: '{}'", cityName);
                // Continuar con la próxima ciudad
            } catch (NetworkException e) {
                LOGGER.warn("Error de red para '{}': {}", cityName, e.getMessage());
                // Continuar con la próxima ciudad
            } catch (WeatherAppException e) {
                LOGGER.warn("Error de datos para '{}': {}", cityName, e.getMessage());
                // Continuar con la próxima ciudad
            } catch (RuntimeException e) {
                LOGGER.warn("Error inesperado para '{}': {}", cityName, e.getMessage());
                // Continuar con la próxima ciudad
            }
        }

        if (results.isEmpty()) {
            throw new IllegalArgumentException("❌ No se encontraron datos climáticos para ninguna de las ciudades proporcionadas");
        }

        LOGGER.info("✅ Datos climáticos obtenidos para {} de {} ciudades", results.size(), cityNames.size());
        return results;
    }

    @Override
    public String toString() {
        return String.format("WeatherService{unit=%s}", temperatureUnit.getName());
    }
}
