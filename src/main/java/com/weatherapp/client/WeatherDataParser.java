package com.weatherapp.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.exception.WeatherDataException;
import com.weatherapp.model.GeoLocation;
import com.weatherapp.model.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parseador de respuestas JSON de Open-Meteo API.
 * Responsable de extraer datos de geocodificación y clima desde JSON.
 * NO realiza validaciones (ver WeatherDataValidator).
 */
public class WeatherDataParser {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherDataParser.class);
    private final Gson gson;
    private final WeatherDataValidator validator;

    /**
     * Constructor. Inyecta el validador para usarlo después del parsing.
     */
    public WeatherDataParser(WeatherDataValidator validator) {
        this.gson = new Gson();
        this.validator = validator;
    }

    /**
     * Parsea la respuesta JSON de la API de geocodificación.
     * Extrae el primer resultado y lo convierte a GeoLocation.
     *
     * @param originalCityName Nombre original buscado (para mensajes de error)
     * @param response JSON de respuesta de la API
     * @return GeoLocation con datos extraídos y validados
     * @throws CityNotFoundException si no se encuentra la ciudad
     * @throws WeatherDataException si hay error al parsear
     */
    public GeoLocation parseGeocodingResponse(String originalCityName, String response) {
        try {
            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

            if (!jsonObject.has("results") || jsonObject.get("results").getAsJsonArray().isEmpty()) {
                throw new CityNotFoundException(originalCityName);
            }

            JsonArray results = jsonObject.getAsJsonArray("results");
            JsonObject firstResult = results.get(0).getAsJsonObject();

            double lat = firstResult.get("latitude").getAsDouble();
            double lon = firstResult.get("longitude").getAsDouble();
            String resolvedName = firstResult.get("name").getAsString();

            // Crear ubicación temporal
            GeoLocation location = new GeoLocation(lat, lon, resolvedName);
            
            // Validar coordenadas
            validator.validateGeoLocation(location);

            // Agregar país si está disponible
            if (firstResult.has("country")) {
                resolvedName += ", " + firstResult.get("country").getAsString();
                location.setCityName(resolvedName);
            }

            LOGGER.debug("Ciudad resuelta: {} ({}, {})", resolvedName, lat, lon);
            return location;

        } catch (CityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            String message = "Error al parsear respuesta de geocodificación: " + e.getMessage();
            LOGGER.error(message, e);
            throw new WeatherDataException("Geocodificación", null, message);
        }
    }

    /**
     * Parsea la respuesta JSON de la API de clima.
     * Extrae temperatura, sensación térmica, humedad, velocidad del viento, precipitación y código climático.
     *
     * @param cityName Nombre de la ciudad
     * @param response JSON de respuesta de la API
     * @param temperatureUnit Unidad de temperatura
     * @return WeatherData con datos extraídos y validados
     * @throws WeatherDataException si hay error al parsear o validar
     */
    public WeatherData parseWeatherResponse(String cityName, String response, TemperatureUnit temperatureUnit) {
        try {
            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

            if (!jsonObject.has("current")) {
                throw new WeatherDataException("Respuesta", null, 
                        "Respuesta de API inválida: falta sección 'current'");
            }

            JsonObject current = jsonObject.getAsJsonObject("current");

            double temperature = current.get("temperature_2m").getAsDouble();
            double apparentTemperature = current.get("apparent_temperature").getAsDouble();
            int humidity = current.get("relative_humidity_2m").getAsInt();
            int weatherCode = current.get("weather_code").getAsInt();
            double windSpeed = current.has("wind_speed_10m") ? current.get("wind_speed_10m").getAsDouble() : 0.0;
            double precipitation = current.has("precipitation") ? current.get("precipitation").getAsDouble() : 0.0;

            // Crear objeto WeatherData temporal para validación
            WeatherData weatherData = new WeatherData(
                    cityName, temperature, apparentTemperature, 
                    humidity, windSpeed, precipitation, "", temperatureUnit);
            
            // Validar que los datos estén en rangos realistas
            validator.validateWeatherData(weatherData);

            // Mapear código climático a descripción
            String description = mapWeatherCode(weatherCode);

            LOGGER.debug("Clima obtenido: {} - {}", temperature, description);
            return new WeatherData(
                    cityName, temperature, apparentTemperature, 
                    humidity, windSpeed, precipitation, description, temperatureUnit);

        } catch (WeatherDataException e) {
            throw e;
        } catch (Exception e) {
            String message = "Error al parsear respuesta de clima: " + e.getMessage();
            LOGGER.error(message, e);
            throw new WeatherDataException("Clima", null, message);
        }
    }

    /**
     * Mapea códigos climáticos de Open-Meteo a descripciones en español con emojis.
     * Fuente: https://open-meteo.com/en/docs/weather-api
     *
     * @param weatherCode Código climático (0-99)
     * @return Descripción legible en español con emoji
     */
    public String mapWeatherCode(int weatherCode) {
        switch (weatherCode) {
            // Cielos despejados
            case 0: return "☀️ Despejado";
            case 1:
            case 2: return "🌤️ Mayormente despejado";
            case 3: return "☁️ Parcialmente nublado";

            // Niebla y polvo
            case 45:
            case 48: return "🌫️ Niebla";

            // Llovizna
            case 51:
            case 53:
            case 55: return "🌧️ Llovizna";

            // Lluvia
            case 61:
            case 63:
            case 65: return "🌧️ Lluvia";
            case 71:
            case 73:
            case 75: return "❄️ Nieve";
            case 77: return "❄️ Granizo";

            // Lluvia congelada
            case 80:
            case 81:
            case 82: return "🌧️ Chubascos";

            // Tormenta
            case 85:
            case 86: return "⛈️ Nieve con tormenta";
            case 95:
            case 96:
            case 99: return "⛈️ Tormenta";

            // Otros
            default: return "🌍 Desconocido (código: " + weatherCode + ")";
        }
    }
}
