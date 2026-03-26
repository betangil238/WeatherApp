package com.weatherapp.cache;

import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para la clase WeatherCache.
 * Verifica comportamiento de almacenamiento, validación de TTL y thread-safety.
 */
@DisplayName("WeatherCache - Tests de Caché")
class WeatherCacheTest {
    
    private WeatherCache cache;
    private WeatherData testWeatherData;
    
    @BeforeEach
    void setUp() {
        // Cache con TTL de 2 segundos para tests rápidos
        cache = new WeatherCache(2000);
        testWeatherData = new WeatherData(
                "Madrid, España",
                25.5,
                23.0,
                60,
                5.0,
                0.0,
                "☀️ Despejado",
                TemperatureUnit.CELSIUS
        );
    }
    
    // ==================== TESTS DE ALMACENAMIENTO ====================
    
    @Test
    @DisplayName("Almacenar y recuperar datos del caché")
    void testPutAndGet() {
        // Act
        cache.put("Madrid", testWeatherData);
        Optional<WeatherData> result = cache.get("Madrid");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Madrid, España", result.get().getCityName());
        assertEquals(25.5, result.get().getTemperature());
    }
    
    @Test
    @DisplayName("Recuperar datos no existentes devuelve Optional vacío")
    void testGetNonExistentCity() {
        // Act
        Optional<WeatherData> result = cache.get("CiudadInexistente");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Normalización de nombres de ciudad (minúsculas, espacios)")
    void testCityNameNormalization() {
        // Act
        cache.put("MADRID", testWeatherData);
        Optional<WeatherData> result1 = cache.get("madrid");
        Optional<WeatherData> result2 = cache.get("  MADRID  ");
        Optional<WeatherData> result3 = cache.get("MaDrId");
        
        // Assert - Todos deben encontrar el mismo dato
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
        assertEquals(testWeatherData.getCityName(), result1.get().getCityName());
    }
    
    // ==================== TESTS DE TTL Y EXPIRACIÓN ====================
    
    @Test
    @DisplayName("Datos expiran después del TTL")
    void testExpirationAfterTTL() throws InterruptedException {
        // Act
        cache.put("Madrid", testWeatherData);
        
        // Verificar que están disponibles
        assertTrue(cache.get("Madrid").isPresent());
        
        // Esperar a que expire el TTL (2 segundos + small buffer)
        Thread.sleep(2100);
        
        // Assert - Ahora deberían estar expirados
        assertFalse(cache.get("Madrid").isPresent());
    }
    
    @Test
    @DisplayName("Datos son válidos antes de expirar")
    void testValidBeforeExpiration() throws InterruptedException {
        // Act
        cache.put("Madrid", testWeatherData);
        
        // Esperar menos que el TTL
        Thread.sleep(500);
        Optional<WeatherData> result = cache.get("Madrid");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Madrid, España", result.get().getCityName());
    }
    
    @Test
    @DisplayName("TTL personalizado se respeta")
    void testCustomTTL() throws InterruptedException {
        // Arrange - Cache con TTL de 500ms
        WeatherCache shortCache = new WeatherCache(500);
        
        // Act
        shortCache.put("Madrid", testWeatherData);
        assertTrue(shortCache.get("Madrid").isPresent());
        
        // Esperar a expiración
        Thread.sleep(600);
        
        // Assert
        assertFalse(shortCache.get("Madrid").isPresent());
    }
    
    // ==================== TESTS DE VALIDACIÓN DE ENTRADA ====================
    
    @Test
    @DisplayName("Rechazar ciudad nula o vacía")
    void testRejectInvalidCityName() {
        // Act & Assert
        cache.put(null, testWeatherData);
        cache.put("", testWeatherData);
        cache.put("   ", testWeatherData);
        
        assertFalse(cache.get(null).isPresent());
        assertFalse(cache.get("").isPresent());
        assertFalse(cache.get("   ").isPresent());
    }
    
    @Test
    @DisplayName("Rechazar datos nulos")
    void testRejectNullWeatherData() {
        // Act
        cache.put("Madrid", null);
        
        // Assert
        assertFalse(cache.get("Madrid").isPresent());
    }
    
    // ==================== TESTS DE LIMPIEZA ====================
    
    @Test
    @DisplayName("Limpiar entradas expiradas")
    void testCleanup() throws InterruptedException {
        // Arrange
        cache.put("Madrid", testWeatherData);
        cache.put("Barcelona", testWeatherData);
        
        // Verificar que están presentes
        assertEquals(2, cache.getStats().getEntriesCount());
        
        // Esperar a que expiren
        Thread.sleep(2100);
        
        // Act
        cache.cleanup();
        
        // Assert
        assertEquals(0, cache.getStats().getEntriesCount());
    }
    
    @Test
    @DisplayName("Limpiar todo el caché")
    void testClearAll() {
        // Arrange
        cache.put("Madrid", testWeatherData);
        cache.put("Barcelona", testWeatherData);
        cache.put("Valencia", testWeatherData);
        
        assertEquals(3, cache.getStats().getEntriesCount());
        
        // Act
        cache.clear();
        
        // Assert
        assertEquals(0, cache.getStats().getEntriesCount());
        assertFalse(cache.get("Madrid").isPresent());
    }
    
    // ==================== TESTS DE MÚLTIPLES ENTRADAS ====================
    
    @Test
    @DisplayName("Almacenar múltiples ciudades de forma independiente")
    void testMultipleCities() {
        // Arrange
        WeatherData madridData = new WeatherData("Madrid", 25.5, 23.0, 60, 5.0, 0.0, "Soleado", TemperatureUnit.CELSIUS);
        WeatherData barcelonaData = new WeatherData("Barcelona", 22.0, 20.0, 70, 5.0, 0.0, "Nublado", TemperatureUnit.CELSIUS);
        WeatherData valenciaData = new WeatherData("Valencia", 23.5, 21.5, 65, 5.0, 0.0, "Parcialmente nublado", TemperatureUnit.CELSIUS);
        
        // Act
        cache.put("Madrid", madridData);
        cache.put("Barcelona", barcelonaData);
        cache.put("Valencia", valenciaData);
        
        // Assert
        assertEquals(3, cache.getStats().getEntriesCount());
        assertEquals(25.5, cache.get("Madrid").get().getTemperature());
        assertEquals(22.0, cache.get("Barcelona").get().getTemperature());
        assertEquals(23.5, cache.get("Valencia").get().getTemperature());
    }
    
    @Test
    @DisplayName("Actualizar datos existentes")
    void testUpdateExistingEntry() {
        // Arrange
        cache.put("Madrid", testWeatherData);
        
        // Crear nuevos datos
        WeatherData updatedData = new WeatherData("Madrid", 30.0, 28.0, 50, 5.0, 0.0, "Muy caluroso", TemperatureUnit.CELSIUS);
        
        // Act
        cache.put("Madrid", updatedData);
        Optional<WeatherData> result = cache.get("Madrid");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(30.0, result.get().getTemperature());
        assertEquals("Muy caluroso", result.get().getDescription());
    }
    
    // ==================== TESTS DE ESTADÍSTICAS ====================
    
    @Test
    @DisplayName("Obtener estadísticas del caché")
    void testGetStats() {
        // Act
        cache.put("Madrid", testWeatherData);
        cache.put("Barcelona", testWeatherData);
        
        WeatherCache.CacheStats stats = cache.getStats();
        
        // Assert
        assertEquals(2, stats.getEntriesCount());
        assertEquals(2000, stats.getTtlMillis());
        assertEquals(2, stats.getTtlSeconds());
        assertEquals(0, stats.getTtlMinutes()); // 2000ms = 0 minutos completos
    }
    
    @Test
    @DisplayName("Stats con caché vacío")
    void testStatsEmptyCache() {
        // Act
        WeatherCache.CacheStats stats = cache.getStats();
        
        // Assert
        assertEquals(0, stats.getEntriesCount());
        assertEquals(2000, stats.getTtlMillis());
    }
    
    // ==================== TESTS DE THREAD-SAFETY ====================
    
    @Test
    @DisplayName("Thread-safety: múltiples hilos escribiendo simultáneamente")
    void testThreadSafeConcurrentWrites() throws InterruptedException {
        // Arrange
        int numThreads = 10;
        Thread[] threads = new Thread[numThreads];
        
        // Act
        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                WeatherData data = new WeatherData(
                        "City" + index, 20.0 + index, 18.0 + index, 60, 5.0, 0.0,
                        "Test", TemperatureUnit.CELSIUS);
                cache.put("City" + index, data);
            });
            threads[i].start();
        }
        
        // Esperar a que terminen todos
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Assert
        assertEquals(numThreads, cache.getStats().getEntriesCount());
    }
    
    @Test
    @DisplayName("Thread-safety: múltiples hilos leyendo simultáneamente")
    void testThreadSafeConcurrentReads() throws InterruptedException {
        // Arrange
        cache.put("Madrid", testWeatherData);
        int numThreads = 10;
        int[] successCount = {0};
        
        // Act
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                Optional<WeatherData> data = cache.get("Madrid");
                if (data.isPresent()) {
                    synchronized (successCount) {
                        successCount[0]++;
                    }
                }
            });
            threads[i].start();
        }
        
        // Esperar a que terminen todos
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Assert
        assertEquals(numThreads, successCount[0]);
    }
}
