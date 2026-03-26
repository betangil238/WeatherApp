package com.weatherapp.service;

import com.weatherapp.cache.WeatherCache;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.exception.WeatherDataException;
import com.weatherapp.model.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Servicio de clima con caché integrado.
 * 
 * Envuelve WeatherService agregando:
 * - Almacenamiento en caché de resultados (1 hora por defecto)
 * - Ahorro de llamadas a API
 * - Mejor rendimiento y latencia
 * - Thread-safe para aplicaciones multiplataforma
 * 
 * Patrón: Decorator Pattern
 */
public class CachedWeatherService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedWeatherService.class);
    
    private final WeatherService delegatedService;
    private final WeatherCache cache;
    
    /**
     * Constructor con caché por defecto (1 hora).
     * 
     * @param weatherService Servicio de clima a decorar
     */
    public CachedWeatherService(WeatherService weatherService) {
        this(weatherService, new WeatherCache());
    }
    
    /**
     * Constructor con caché personalizado.
     * 
     * @param weatherService Servicio de clima a decorar
     * @param cache Instancia de caché personalizada
     */
    public CachedWeatherService(WeatherService weatherService, WeatherCache cache) {
        this.delegatedService = weatherService;
        this.cache = cache;
        LOGGER.info("CachedWeatherService inicializado con caché");
    }
    
    /**
     * Obtiene datos climáticos con caché.
     * Intenta obtener del caché primero. Si no está disponible o expiró,
     * consulta el servicio delegado y actualiza el caché.
     * 
     * @param cityName Nombre de la ciudad
     * @return WeatherData con los datos climáticos
     * @throws CityNotFoundException si la ciudad no existe
     * @throws WeatherDataException si los datos son inválidos
     */
    public WeatherData getWeatherByCityName(String cityName) {
        // Validar entrada
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ El nombre de la ciudad no puede estar vacío");
        }
        
        String normalizedCity = cityName.trim();
        
        // Intentar obtener del caché
        Optional<WeatherData> cachedData = cache.get(normalizedCity);
        if (cachedData.isPresent()) {
            LOGGER.info("📦 Retornando datos de CACHÉ para: '{}'", normalizedCity);
            return cachedData.get();
        }
        
        // No está en caché, obtener del servicio delegado
        LOGGER.info("🌐 Consultando API para: '{}'", normalizedCity);
        WeatherData weatherData = delegatedService.getWeatherByCityName(normalizedCity);
        
        // Almacenar en caché para futuras consultas
        cache.put(normalizedCity, weatherData);
        LOGGER.info("✅ Datos obtenidos y almacenados en caché para: '{}'", normalizedCity);
        
        return weatherData;
    }
    
    /**
     * Obtiene la unidad de temperatura configurada.
     * 
     * @return TemperatureUnit actual
     */
    public TemperatureUnit getTemperatureUnit() {
        return delegatedService.getTemperatureUnit();
    }
    
    /**
     * Obtiene estadísticas del caché.
     * 
     * @return Estadísticas del caché
     */
    public WeatherCache.CacheStats getCacheStats() {
        return cache.getStats();
    }
    
    /**
     * Limpia las entradas expiradas del caché.
     */
    public void cleanupCache() {
        cache.cleanup();
    }
    
    /**
     * Limpia completamente el caché.
     */
    public void clearCache() {
        cache.clear();
    }
    
    /**
     * Obtiene la instancia del caché para acceso avanzado.
     * 
     * @return Instancia de WeatherCache
     */
    public WeatherCache getCache() {
        return cache;
    }
    
    /**
     * Obtiene el servicio delegado para acceso directo si es necesario.
     * 
     * @return Instancia de WeatherService
     */
    public WeatherService getDelegatedService() {
        return delegatedService;
    }
}
