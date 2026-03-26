package com.weatherapp.cache;

import com.weatherapp.model.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Caché de datos climáticos con TTL (Time To Live) de 1 hora.
 * Thread-safe y optimizado para aplicaciones multiplataforma.
 * 
 * Características:
 * - Almacenamiento en memoria con sincronización segura
 * - TTL configurable (por defecto 1 hora = 3600000ms)
 * - Conversión automática de timestamps
 * - Limpieza lazy de entradas expiradas
 * - Bajo uso de memoria
 */
public class WeatherCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherCache.class);
    
    /** TTL por defecto: 1 hora en milisegundos (use CacheConfig para cambiar) */
    private static final long DEFAULT_TTL_MS = CacheConfig.DEFAULT_TTL_MS;
    
    /** Mapa concurrente de ciudad → datos en caché */
    private final ConcurrentHashMap<String, CacheEntry> cache;
    
    /** Lock para operaciones de limpieza */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    /** TTL configurable en milisegundos */
    private final long ttlMillis;
    
    /**
     * Constructor con TTL por defecto (1 hora).
     */
    public WeatherCache() {
        this(DEFAULT_TTL_MS);
    }
    
    /**
     * Constructor con TTL personalizado.
     * 
     * @param ttlMillis Tiempo de vida en milisegundos
     */
    public WeatherCache(long ttlMillis) {
        this.cache = new ConcurrentHashMap<>();
        this.ttlMillis = ttlMillis;
        LOGGER.info("WeatherCache inicializado con TTL: {}ms ({} minutos)", 
                   ttlMillis, ttlMillis / 60000);
    }
    
    /**
     * Obtiene datos climáticos del caché si existen y son válidos.
     * 
     * @param cityName Nombre de la ciudad (clave)
     * @return Optional con los datos si están en caché y son válidos
     */
    public Optional<WeatherData> get(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String normalizedKey = normalizeCityName(cityName);
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(normalizedKey);
            
            if (entry != null && entry.isValid()) {
                LOGGER.debug("✅ Caché HIT para '{}' (edad: {}ms)", 
                           normalizedKey, entry.getAgeMs());
                return Optional.of(entry.getData());
            } else if (entry != null) {
                LOGGER.debug("⏰ Caché EXPIRADO para '{}' (edad: {}ms, TTL: {}ms)", 
                           normalizedKey, entry.getAgeMs(), ttlMillis);
            } else {
                LOGGER.debug("❌ Caché MISS para '{}'", normalizedKey);
            }
            
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Almacena datos climáticos en el caché.
     * 
     * @param cityName Nombre de la ciudad (clave)
     * @param weatherData Datos climáticos a almacenar
     */
    public void put(String cityName, WeatherData weatherData) {
        if (cityName == null || cityName.trim().isEmpty() || weatherData == null) {
            LOGGER.warn("Intento de poner en caché con datos inválidos: city={}, data={}", 
                       cityName, weatherData);
            return;
        }
        
        String normalizedKey = normalizeCityName(cityName);
        lock.writeLock().lock();
        try {
            CacheEntry entry = new CacheEntry(weatherData);
            cache.put(normalizedKey, entry);
            LOGGER.debug("💾 Datos almacenados en caché para '{}'", normalizedKey);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Limpia las entradas expiradas del caché.
     * Operación de limpieza lazy para mantener eficiencia.
     */
    public void cleanup() {
        lock.writeLock().lock();
        try {
            int initialSize = cache.size();
            
            cache.entrySet().removeIf(entry -> !entry.getValue().isValid());
            
            int finalSize = cache.size();
            int removed = initialSize - finalSize;
            
            if (removed > 0) {
                LOGGER.info("🧹 Caché limpiado: {} entradas eliminadas ({} → {})", 
                           removed, initialSize, finalSize);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Limpia todo el caché.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            int size = cache.size();
            cache.clear();
            LOGGER.info("🧹 Caché completamente limpiado ({} entradas eliminadas)", size);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Obtiene estadísticas del caché.
     * 
     * @return Información de uso del caché
     */
    public CacheStats getStats() {
        lock.readLock().lock();
        try {
            return new CacheStats(cache.size(), ttlMillis);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Normaliza el nombre de la ciudad para usar como clave.
     * Convierte a minúsculas y elimina espacios extra.
     */
    private String normalizeCityName(String cityName) {
        return cityName.trim().toLowerCase();
    }
    
    /**
     * Entrada del caché que contiene datos y timestamp.
     */
    private class CacheEntry {
        private final WeatherData data;
        private final long createdAtMs;
        
        CacheEntry(WeatherData data) {
            this.data = data;
            this.createdAtMs = System.currentTimeMillis();
        }
        
        /**
         * Verifica si la entrada aún es válida (dentro del TTL).
         */
        boolean isValid() {
            long ageMs = System.currentTimeMillis() - createdAtMs;
            return ageMs < ttlMillis;
        }
        
        /**
         * Obtiene la edad de la entrada en milisegundos.
         */
        long getAgeMs() {
            return System.currentTimeMillis() - createdAtMs;
        }
        
        WeatherData getData() {
            return data;
        }
    }
    
    /**
     * Estadísticas del caché.
     */
    public static class CacheStats {
        private final int entriesCount;
        private final long ttlMillis;
        
        public CacheStats(int entriesCount, long ttlMillis) {
            this.entriesCount = entriesCount;
            this.ttlMillis = ttlMillis;
        }
        
        public int getEntriesCount() {
            return entriesCount;
        }
        
        public long getTtlMillis() {
            return ttlMillis;
        }
        
        public long getTtlSeconds() {
            return ttlMillis / 1000;
        }
        
        public long getTtlMinutes() {
            return ttlMillis / 60000;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{entradas=%d, TTL=%dmin}", 
                               entriesCount, getTtlMinutes());
        }
    }
}
