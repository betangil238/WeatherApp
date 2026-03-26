package com.weatherapp.client;

import com.weatherapp.config.AppConfig;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.exception.NetworkException;
import com.weatherapp.model.GeoLocation;
import com.weatherapp.model.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

/**
 * Cliente HTTP para la API de Open-Meteo.
 * Responsabilidad única: Realizar llamadas HTTP y orquestar validación/parsing.
 * 
 * Delegaciones:
 * - WeatherDataValidator: Validación de datos
 * - WeatherDataParser: Parsing de JSON a objetos
 * 
 * Incluye reintentos con backoff exponencial.
 */
public class OpenMeteoClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenMeteoClient.class);
    private final HttpClient httpClient;
    private final WeatherDataValidator validator;
    private final WeatherDataParser parser;

    /**
     * Constructor. Inicializa cliente HTTP, validador y parser.
     */
    public OpenMeteoClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(AppConfig.REQUEST_TIMEOUT_SECONDS))
                .build();
        this.validator = new WeatherDataValidator();
        this.parser = new WeatherDataParser(validator);
    }

    /**
     * Resuelve el nombre de una ciudad a coordenadas geográficas.
     * Utiliza la API de geocodificación de Open-Meteo con reintentos.
     *
     * @param cityName Nombre de la ciudad a buscar
     * @return GeoLocation con latitud, longitud y nombre resuelto
     * @throws com.weatherapp.exception.CityNotFoundException si la ciudad no se encuentra
     * @throws NetworkException si ocurre error de conectividad después de reintentos
     */
    public GeoLocation resolveCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de ciudad no puede estar vacío");
        }

        String encodedCity = URLEncoder.encode(cityName.trim(), StandardCharsets.UTF_8);
        String url = String.format("%s/search?name=%s&language=%s&limit=%d",
                                    AppConfig.GEOCODING_API_BASE_URL,
                                    encodedCity,
                                    AppConfig.LANGUAGE_CODE,
                                    AppConfig.GEOCODING_LIMIT);

        LOGGER.info("Resolviendo ciudad: {}", cityName);

        String response = executeRequestWithRetry(url);
        return parser.parseGeocodingResponse(cityName, response);
    }

    /**
     * Obtiene datos climáticos para una ubicación específica.
     * Utiliza la API de clima de Open-Meteo con reintentos.
     *
     * @param location Ubicación geográfica (latitud, longitud)
     * @param temperatureUnit Unidad de temperatura a usar
     * @return WeatherData con temperatura, humedad, descripción, etc.
     * @throws com.weatherapp.exception.WeatherDataException si datos inválidos o fuera de rango
     * @throws NetworkException si ocurre error de conectividad después de reintentos
     */
    public WeatherData getWeatherData(GeoLocation location, TemperatureUnit temperatureUnit) {
        if (location == null) {
            throw new IllegalArgumentException("❌ La ubicación no puede ser nula");
        }
        if (temperatureUnit == null) {
            throw new IllegalArgumentException("❌ La unidad de temperatura no puede ser nula");
        }

        // Validar que las coordenadas estén en rangos válidos ANTES de hacer la llamada HTTP
        validator.validateGeoLocation(location);

        String url = String.format(Locale.US,
                "%s/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,precipitation&temperature_unit=%s",
                AppConfig.WEATHER_API_BASE_URL,
                location.getLatitude(),
                location.getLongitude(),
                temperatureUnit.getApiParameter());

        LOGGER.info("Obteniendo datos climáticos para: {}", location.getCityName());

        String response = executeRequestWithRetry(url);
        return parser.parseWeatherResponse(location.getCityName(), response, temperatureUnit);
    }

    /**
     * Ejecuta una solicitud HTTP con reintentos automáticos ante fallos.
     * Implementa backoff exponencial para evitar sobrecargar el servidor.
     *
     * @param url URL a consultar
     * @return Cuerpo de la respuesta como String
     * @throws NetworkException si falla después de MAX_RETRIES intentos
     */
    private String executeRequestWithRetry(String url) {
        long delayMs = AppConfig.RETRY_DELAY_MS;

        for (int attempt = 1; attempt <= AppConfig.MAX_RETRIES; attempt++) {
            try {
                LOGGER.debug("Intento {}/{}: GET {}", attempt, AppConfig.MAX_RETRIES, url);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(AppConfig.REQUEST_TIMEOUT_SECONDS))
                        .header("User-Agent", "WeatherApp/1.0")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    LOGGER.debug("Respuesta exitosa (200 OK)");
                    return response.body();
                } else {
                    LOGGER.warn("Error HTTP {}", response.statusCode());
                    if (attempt < AppConfig.MAX_RETRIES) {
                        LOGGER.debug("Reintentando en {}ms...", delayMs);
                        Thread.sleep(delayMs);
                        delayMs *= AppConfig.RETRY_BACKOFF_FACTOR;
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.warn("Solicitud interrumpida");
                Thread.currentThread().interrupt();
                throw new NetworkException("Solicitud interrumpida", attempt, AppConfig.MAX_RETRIES, e);
            } catch (Exception e) {
                LOGGER.warn("Error de conexión: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                if (attempt < AppConfig.MAX_RETRIES) {
                    LOGGER.debug("Reintentando en {}ms...", delayMs);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new NetworkException("Reintento interrumpido", attempt, AppConfig.MAX_RETRIES, ie);
                    }
                    delayMs *= AppConfig.RETRY_BACKOFF_FACTOR;
                } else {
                    String message = String.format(
                            "Error de conexión después de %d intentos: %s",
                            AppConfig.MAX_RETRIES, e.getMessage());
                    LOGGER.error(message);
                    throw new NetworkException(message, attempt, AppConfig.MAX_RETRIES, e);
                }
            }
        }

        String message = "Error: No se pudo completar la solicitud después de " + AppConfig.MAX_RETRIES + " intentos";
        LOGGER.error(message);
        throw new NetworkException(message, AppConfig.MAX_RETRIES, AppConfig.MAX_RETRIES, null);
    }

    /**
     * Método delegador para mantener backward compatibility con tests.
     * Mapea un código de clima WMO a una descripción legible.
     * Este método está disponible públicamente por razones de testing.
     *
     * @param weatherCode Código meteorológico WMO (0-99)
     * @return Descripción con emoji del clima
     */
    public String parseWeatherCode(int weatherCode) {
        return parser.mapWeatherCode(weatherCode);
    }
}
