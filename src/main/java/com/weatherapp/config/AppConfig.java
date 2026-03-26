package com.weatherapp.config;

/**
 * Configuración global de la aplicación.
 * Contiene constantes de URLs de API, timeouts, reintentos y unidades por defecto.
 */
public class AppConfig {

    // URLs de APIs
    /**
     * URL base de la API de geocodificación de Open-Meteo.
     * Utilizada para resolver coordenadas a partir del nombre de la ciudad.
     */
    public static final String GEOCODING_API_BASE_URL = "https://geocoding-api.open-meteo.com/v1";

    /**
     * URL base de la API de datos climáticos de Open-Meteo.
     * Utilizada para obtener temperatura, humedad y descripción del clima.
     */
    public static final String WEATHER_API_BASE_URL = "https://api.open-meteo.com/v1";

    // Configuración de HTTP
    /**
     * Timeout para conexiones HTTP en segundos.
     */
    public static final int REQUEST_TIMEOUT_SECONDS = 10;

    /**
     * Número máximo de reintentos ejecutivos ante fallos de conexión.
     */
    public static final int MAX_RETRIES = 3;

    /**
     * Delay inicial entre reintentos en milisegundos.
     * Se incrementa exponencialmente en cada reintento.
     */
    public static final long RETRY_DELAY_MS = 1000;

    /**
     * Factor multiplicativo para backoff exponencial.
     * Cada reintento espera: RETRY_DELAY_MS * (RETRY_BACKOFF_FACTOR ^ intento)
     */
    public static final double RETRY_BACKOFF_FACTOR = 2.0;

    // Unidad de temperatura por defecto
    /**
     * Unidad de temperatura por defecto al inicio (si no hay configuración guardada).
     */
    public static final TemperatureUnit DEFAULT_TEMPERATURE_UNIT = TemperatureUnit.CELSIUS;

    // Lenguaje de respuestas
    /**
     * Código de idioma para la API de geocodificación.
     */
    public static final String LANGUAGE_CODE = "es";

    // Límites de búsqueda de geocodificación
    /**
     * Número máximo de resultados de geocodificación a retornar.
     */
    public static final int GEOCODING_LIMIT = 1;

    // Configuración de salida
    /**
     * Indicador de si usar emojis en la salida de consola.
     */
    public static final boolean USE_EMOJIS = true;

    /**
     * Número de decimales para mostrar temperaturas.
     */
    public static final int TEMPERATURE_DECIMAL_PLACES = 1;

    /**
     * Número de decimales para mostrar coordenadas.
     */
    public static final int COORDINATE_DECIMAL_PLACES = 4;

    // Validación de rangos de datos climáticos
    /**
     * Humedad relativa mínima válida (porcentaje).
     */
    public static final int HUMIDITY_MIN = 0;

    /**
     * Humedad relativa máxima válida (porcentaje).
     */
    public static final int HUMIDITY_MAX = 100;

    /**
     * Temperatura mínima realista en Celsius.
     * Cubre casos extremos reales. Base: −89.2°C es la temperatura más baja registrada en tierra (Vostok, Antártida).
     * Se usa −90°C como límite seguro para ubicaciones polares.
     */
    public static final double TEMPERATURE_MIN_CELSIUS = -90.0;

    /**
     * Temperatura máxima realista en Celsius.
     * Cubre casos extremos reales. Base: +70°C es cercana a la temperatura máxima registrada en tierra.
     * Se usa +70°C como límite seguro para ubicaciones desérticas extremas.
     */
    public static final double TEMPERATURE_MAX_CELSIUS = 70.0;

    /**
     * Latitud mínima válida en coordenadas geográficas.
     */
    public static final double LATITUDE_MIN = -90.0;

    /**
     * Latitud máxima válida en coordenadas geográficas.
     */
    public static final double LATITUDE_MAX = 90.0;

    /**
     * Longitud mínima válida en coordenadas geográficas.
     */
    public static final double LONGITUDE_MIN = -180.0;

    /**
     * Longitud máxima válida en coordenadas geográficas.
     */
    public static final double LONGITUDE_MAX = 180.0;

    /**
     * Código climático mínimo válido en la API de Open-Meteo.
     */
    public static final int WEATHER_CODE_MIN = 0;

    /**
     * Código climático máximo válido en la API de Open-Meteo.
     */
    public static final int WEATHER_CODE_MAX = 99;

    // Rutas de configuración
    /**
     * Directorio de configuración de usuario.
     * Se crea en el home del usuario: ~/.weatherapp/
     */
    public static final String CONFIG_DIR = System.getProperty("user.home") + "/.weatherapp";

    /**
     * Archivo de configuración de usuario.
     * Contiene preferencias como la unidad de temperatura elegida.
     */
    public static final String CONFIG_FILE = CONFIG_DIR + "/config.properties";

    /**
     * Propiedad en el archivo de configuración para la unidad de temperatura.
     */
    public static final String CONFIG_PROPERTY_TEMPERATURE_UNIT = "temperature.unit";
}
