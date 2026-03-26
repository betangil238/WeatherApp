package com.weatherapp.cache;

/**
 * Configuración centralizada para el caché de clima.
 * Modifica los valores aquí para customizar el comportamiento del caché.
 * 
 * Ejemplo:
 * - Desarrollo: 5 minutos (actualizaciones frecuentes)
 * - Producción: 60 minutos (balance entre frescura y performance)
 * - Datos estables: 24 horas (máximo ahorro)
 */
public class CacheConfig {

    // ========== CONFIGURACIÓN DE TTL (Time To Live) ==========
    
    /**
     * TTL por defecto para entradas de caché: 1 hora en milisegundos.
     * 
     * Presets recomendados:
     * - 5 minutos: 5 * 60 * 1000 = 300,000ms (desarrollo, datos muy variables)
     * - 10 minutos: 10 * 60 * 1000 = 600,000ms 
     * - 30 minutos: 30 * 60 * 1000 = 1,800,000ms (web apps)
     * - 60 minutos: 60 * 60 * 1000 = 3,600,000ms (por defecto, balance)
     * - 24 horas: 24 * 60 * 60 * 1000 = 86,400,000ms (datos muy estables)
     */
    public static final long DEFAULT_TTL_MS = 60 * 60 * 1000; // 1 hora

    /**
     * TTL para aplicaciones móviles (ahorro de batería + datos frescos)
     */
    public static final long MOBILE_APP_TTL_MS = 30 * 60 * 1000; // 30 minutos

    /**
     * TTL para aplicaciones desktop (balance)
     */
    public static final long DESKTOP_APP_TTL_MS = 60 * 60 * 1000; // 1 hora

    /**
     * TTL para web apps (con muchos usuarios, reduce API calls)
     */
    public static final long WEB_APP_TTL_MS = 60 * 60 * 1000; // 1 hora

    /**
     * TTL para monitoreo en tiempo real (datos muy frescos)
     */
    public static final long REALTIME_TTL_MS = 5 * 60 * 1000; // 5 minutos

    /**
     * TTL para datos críticos (máxima frescura)
     */
    public static final long CRITICAL_DATA_TTL_MS = 1 * 60 * 1000; // 1 minuto

    // ========== CONFIGURACIÓN DE LIMPIEZA ==========

    /**
     * Si es true, limpia automáticamente entradas expiradas cada cierto tiempo.
     * Si es false, solo limpia cuando se realiza limpieza manual.
     */
    public static final boolean AUTO_CLEANUP_ENABLED = true;

    /**
     * Intervalo para limpieza automática en milisegundos (cuando AUTO_CLEANUP_ENABLED es true)
     */
    public static final long AUTO_CLEANUP_INTERVAL_MS = 10 * 60 * 1000; // 10 minutos

    // ========== CONFIGURACIÓN DE LIMITES ==========

    /**
     * Número máximo de entradas en caché antes de forzar limpieza.
     * -1 = sin límite (no se recomienda para aplicaciones de larga duración)
     */
    public static final int MAX_CACHE_ENTRIES = 10_000;

    /**
     * Si el caché excede este límite, se dispara una limpieza de emergencia
     */
    public static final int EMERGENCY_CLEANUP_THRESHOLD = MAX_CACHE_ENTRIES * 2;

    // ========== CONFIGURACIÓN DE LOGGING ==========

    /**
     * Nivel de detalle del logging:
     * - TRACE: Muy detallado (no usar en producción)
     * - DEBUG: Detallado (desarrollo)
     * - INFO: Información importante (por defecto)
     * - WARN: Advertencias (errores no críticos)
     * - ERROR: Solo errores
     */
    public static final String LOG_LEVEL = "INFO";

    /**
     * Si es true, muestra estadísticas del caché cada cierto tiempo
     */
    public static final boolean LOG_STATS_ENABLED = true;

    /**
     * Intervalo para mostrar estadísticas en milisegundos
     */
    public static final long LOG_STATS_INTERVAL_MS = 5 * 60 * 1000; // 5 minutos

    // ========== CONFIGURACIÓN DE ESTADÍSTICAS ==========

    /**
     * Si es true, registra estadísticas detalladas (hit ratio, miss ratio, etc.)
     * Tiene un pequeño overhead de memoria
     */
    public static final boolean STATS_ENABLED = true;

    /**
     * Si es true, registra qué ciudades tienen más hits/misses
     */
    public static final boolean DETAILED_STATS_ENABLED = false; // Overhead mayor

    // ========== CONFIGURACIÓN DE VALIDACIÓN ==========

    /**
     * Si es true, valida que los datos NO sean null antes de cachear
     */
    public static final boolean VALIDATE_NOT_NULL = true;

    /**
     * Si es true, valida que el nombre de ciudad NO sea null/empty
     */
    public static final boolean VALIDATE_CITY_NAME = true;

    // ========== PRESETS PARA DIFERENTES CASOS DE USO ==========

    /**
     * Crea una configuración pre-hecha para aplicaciones móviles
     */
    public static long getMobileAppConfig() {
        return MOBILE_APP_TTL_MS;
    }

    /**
     * Crea una configuración pre-hecha para aplicaciones desktop
     */
    public static long getDesktopAppConfig() {
        return DESKTOP_APP_TTL_MS;
    }

    /**
     * Crea una configuración pre-hecha para web apps
     */
    public static long getWebAppConfig() {
        return WEB_APP_TTL_MS;
    }

    /**
     * Crea una configuración pre-hecha para monitoreo en tiempo real
     */
    public static long getRealtimeConfig() {
        return REALTIME_TTL_MS;
    }

    /**
     * Crea una configuración pre-hecha para datos críticos
     */
    public static long getCriticalDataConfig() {
        return CRITICAL_DATA_TTL_MS;
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Convierte milisegundos a minutos (para logging legible)
     */
    public static long msToMinutes(long ms) {
        return ms / (60 * 1000);
    }

    /**
     * Convierte minutos a milisegundos
     */
    public static long minutesToMs(long minutes) {
        return minutes * 60 * 1000;
    }

    /**
     * Convierte horas a milisegundos
     */
    public static long hoursToMs(long hours) {
        return hours * 60 * 60 * 1000;
    }

    /**
     * Convierte milisegundos a representación legible
     * Ej: 3600000ms → "1 hora"
     */
    public static String formatTTL(long ms) {
        if (ms < 60 * 1000) {
            return (ms / 1000) + " segundos";
        } else if (ms < 60 * 60 * 1000) {
            return (ms / (60 * 1000)) + " minutos";
        } else if (ms < 24 * 60 * 60 * 1000) {
            return (ms / (60 * 60 * 1000)) + " horas";
        } else {
            return (ms / (24 * 60 * 60 * 1000)) + " días";
        }
    }

    // ========== USO EN CÓDIGO ==========

    /**
     * Ejemplo de uso:
     * 
     * // Opción 1: Caché con TTL por defecto (1 hora)
     * WeatherCache cache = new WeatherCache();
     * 
     * // Opción 2: Caché con TTL custom
     * WeatherCache cache = new WeatherCache(CacheConfig.MOBILE_APP_TTL_MS);
     * 
     * // Opción 3: Caché con TTL custom en minutos
     * WeatherCache cache = new WeatherCache(CacheConfig.minutesToMs(30));
     * 
     * // Ver TTL configurado en logs
     * System.out.println("TTL configurado: " + CacheConfig.formatTTL(CacheConfig.DEFAULT_TTL_MS));
     * // Output: "TTL configurado: 1 horas"
     */

    /**
     * Ejemplo en aplicación multiplataforma:
     * 
     * public class WeatherApp {
     *     public static void main(String[] args) {
     *         // Detectar plataforma
     *         String os = System.getProperty("os.name").toLowerCase();
     *         
     *         long ttl;
     *         if (os.contains("android")) {
     *             ttl = CacheConfig.MOBILE_APP_TTL_MS;
     *         } else if (os.contains("windows") || os.contains("linux")) {
     *             ttl = CacheConfig.DESKTOP_APP_TTL_MS;
     *         } else {
     *             ttl = CacheConfig.DEFAULT_TTL_MS;
     *         }
     *         
     *         WeatherCache cache = new WeatherCache(ttl);
     *         // ... resto del código
     *     }
     * }
     */

    /**
     * NO instanciar esta clase - es solamente configuración
     */
    private CacheConfig() {
        throw new AssertionError("No instanciar CacheConfig");
    }
}
