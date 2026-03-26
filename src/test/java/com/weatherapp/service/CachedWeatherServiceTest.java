package com.weatherapp.service;

import com.weatherapp.cache.WeatherCache;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests para el servicio CachedWeatherService.
 * Verifica que el caché funciona correctamente y reduce llamadas a la API.
 */
@DisplayName("CachedWeatherService - Tests de Caché")
class CachedWeatherServiceTest {
    
    private CachedWeatherService cachedService;
    private WeatherService mockWeatherService;
    private WeatherData testWeatherData;
    
    @BeforeEach
    void setUp() {
        // Crear mock del servicio original
        mockWeatherService = mock(WeatherService.class);
        
        // Crear datos de prueba
        testWeatherData = new WeatherData(
                "Madrid, España", 25.5, 23.0, 60, 5.0, 0.0, "☀️ Despejado", TemperatureUnit.CELSIUS
        );
        
        // Configurar mock para devolver datos de prueba
        when(mockWeatherService.getWeatherByCityName(anyString()))
                .thenReturn(testWeatherData);
        when(mockWeatherService.getTemperatureUnit())
                .thenReturn(TemperatureUnit.CELSIUS);
        
        // Crear servicio con caché (2 segundos de TTL para tests)
        WeatherCache testCache = new WeatherCache(2000);
        cachedService = new CachedWeatherService(mockWeatherService, testCache);
    }
    
    // ==================== TESTS DE FUNCIONALIDAD BÁSICA ====================
    
    @Test
    @DisplayName("Primera llamada consulta el servicio delegado")
    void testFirstCallDelegates() {
        // Act
        WeatherData result = cachedService.getWeatherByCityName("Madrid");
        
        // Assert
        assertNotNull(result);
        assertEquals("Madrid, España", result.getCityName());
        verify(mockWeatherService, times(1)).getWeatherByCityName("Madrid");
    }
    
    @Test
    @DisplayName("Segunda llamada usa caché sin consultar API")
    void testSecondCallUsesCacheWithoutDelegating() {
        // Act - Primera llamada
        cachedService.getWeatherByCityName("Madrid");
        verify(mockWeatherService, times(1)).getWeatherByCityName("Madrid");
        
        // Act - Segunda llamada (debe usar caché)
        cachedService.getWeatherByCityName("Madrid");
        
        // Assert - El mock solo fue llamado una vez
        verify(mockWeatherService, times(1)).getWeatherByCityName("Madrid");
    }
    
    @Test
    @DisplayName("Diferentes ciudades se cachean independientemente")
    void testMultipleCitiesCachedIndependently() {
        // Arrange
        WeatherData madridData = new WeatherData(
                "Madrid", 25.5, 23.0, 60, 5.0, 0.0, "Soleado", TemperatureUnit.CELSIUS);
        WeatherData barcelonaData = new WeatherData(
                "Barcelona", 22.0, 20.0, 70, 5.0, 0.0, "Nublado", TemperatureUnit.CELSIUS);
        
        when(mockWeatherService.getWeatherByCityName("Madrid")).thenReturn(madridData);
        when(mockWeatherService.getWeatherByCityName("Barcelona")).thenReturn(barcelonaData);
        
        // Act
        cachedService.getWeatherByCityName("Madrid");
        cachedService.getWeatherByCityName("Barcelona");
        cachedService.getWeatherByCityName("Madrid");
        cachedService.getWeatherByCityName("Barcelona");
        cachedService.getWeatherByCityName("Madrid");
        
        // Assert - Solo dos llamadas a la API (una por ciudad)
        verify(mockWeatherService, times(1)).getWeatherByCityName("Madrid");
        verify(mockWeatherService, times(1)).getWeatherByCityName("Barcelona");
    }
    
    // ==================== TESTS DE NORMALIZACIÓN ====================
    
    @Test
    @DisplayName("Normalización de nombres hace match en caché")
    void testCacheMatchesNormalizedNames() {
        // Act
        cachedService.getWeatherByCityName("madrid");
        cachedService.getWeatherByCityName("MADRID");
        cachedService.getWeatherByCityName("  Madrid  ");
        cachedService.getWeatherByCityName("MaDrId");
        
        // Assert - Solo una llamada a la API
        verify(mockWeatherService, times(1)).getWeatherByCityName(anyString());
    }
    
    // ==================== TESTS DE DELEGACIÓN ====================
    
    @Test
    @DisplayName("Obtener unidad de temperatura delegada")
    void testGetTemperatureUnitDelegates() {
        // Act
        TemperatureUnit result = cachedService.getTemperatureUnit();
        
        // Assert
        assertEquals(TemperatureUnit.CELSIUS, result);
        verify(mockWeatherService).getTemperatureUnit();
    }
    
    @Test
    @DisplayName("Acceso al servicio delegado")
    void testAccessDelegatedService() {
        // Act
        WeatherService delegated = cachedService.getDelegatedService();
        
        // Assert
        assertNotNull(delegated);
        assertEquals(mockWeatherService, delegated);
    }
    
    // ==================== TESTS DE GESTIÓN DE CACHÉ ====================
    
    @Test
    @DisplayName("Obtener estadísticas del caché")
    void testGetCacheStats() {
        // Arrange
        cachedService.getWeatherByCityName("Madrid");
        cachedService.getWeatherByCityName("Barcelona");
        
        // Act
        WeatherCache.CacheStats stats = cachedService.getCacheStats();
        
        // Assert
        assertEquals(2, stats.getEntriesCount());
        assertEquals(2000, stats.getTtlMillis());
    }
    
    @Test
    @DisplayName("Limpiar caché expira entradas manualmente")
    void testCleanupCache() throws InterruptedException {
        // Arrange
        cachedService.getWeatherByCityName("Madrid");
        assertEquals(1, cachedService.getCacheStats().getEntriesCount());
        
        // Esperar a que expire
        Thread.sleep(2100);
        
        // Act
        cachedService.cleanupCache();
        
        // Assert
        assertEquals(0, cachedService.getCacheStats().getEntriesCount());
    }
    
    @Test
    @DisplayName("Limpiar todo el caché")
    void testClearCache() {
        // Arrange
        cachedService.getWeatherByCityName("Madrid");
        cachedService.getWeatherByCityName("Barcelona");
        assertEquals(2, cachedService.getCacheStats().getEntriesCount());
        
        // Act
        cachedService.clearCache();
        
        // Assert
        assertEquals(0, cachedService.getCacheStats().getEntriesCount());
        
        // Siguiente llamada debe consultar API nuevamente
        cachedService.getWeatherByCityName("Madrid");
        verify(mockWeatherService, times(2)).getWeatherByCityName("Madrid");
    }
    
    // ==================== TESTS DE ERRORES ====================
    
    @Test
    @DisplayName("Rechazar ciudad nula o vacía")
    void testRejectInvalidCityName() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cachedService.getWeatherByCityName(null));
        assertThrows(IllegalArgumentException.class, () -> cachedService.getWeatherByCityName(""));
        assertThrows(IllegalArgumentException.class, () -> cachedService.getWeatherByCityName("   "));
    }
    
    @Test
    @DisplayName("Propagar excepciones del servicio delegado")
    void testPropagateServiceExceptions() {
        // Arrange
        when(mockWeatherService.getWeatherByCityName("InvalidCity"))
                .thenThrow(new RuntimeException("API Error"));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> cachedService.getWeatherByCityName("InvalidCity"));
    }
    
    // ==================== TESTS DE PERFORMANCE ====================
    
    @Test
    @DisplayName("Caché reduce significativamente llamadas a API")
    void testCacheSignificantlyReducesAPICalls() {
        // Act
        for (int i = 0; i < 100; i++) {
            cachedService.getWeatherByCityName("Madrid");
        }
        
        // Assert - Solo 1 llamada a la API para 100 solicitudes
        verify(mockWeatherService, times(1)).getWeatherByCityName("Madrid");
    }
    
    @Test
    @DisplayName("Caché de múltiples ciudades con patrón de acceso realista")
    void testCacheWithRealisticAccessPattern() throws InterruptedException {
        // Arrange - Simular acceso a múltiples ciudades
        String[] cities = {"Madrid", "Barcelona", "Valencia", "Sevilla", "Bilbao"};
        
        for (String city : cities) {
            when(mockWeatherService.getWeatherByCityName(city))
                    .thenReturn(new WeatherData(
                            city, 20.0, 18.0, 60, 5.0, 0.0, "Test", TemperatureUnit.CELSIUS));
        }
        
        // Act - Acceso múltiple a las mismas ciudades
        for (int round = 0; round < 10; round++) {
            for (String city : cities) {
                cachedService.getWeatherByCityName(city);
            }
        }
        
        // Assert - 5 llamadas a API (una por ciudad), no 50
        for (String city : cities) {
            verify(mockWeatherService, times(1)).getWeatherByCityName(city);
        }
    }
    
    @Test
    @DisplayName("Caché asegura consistencia de datos durante TTL")
    void testCacheConsistency() {
        // Arrange
        WeatherData v1 = new WeatherData("Madrid", 25.0, 23.0, 60, 5.0, 0.0, "V1", TemperatureUnit.CELSIUS);
        WeatherData v2 = new WeatherData("Madrid", 30.0, 28.0, 50, 5.0, 0.0, "V2", TemperatureUnit.CELSIUS);
        
        when(mockWeatherService.getWeatherByCityName("Madrid"))
                .thenReturn(v1)  // Primera llamada
                .thenReturn(v2); // Segunda llamada (no debe ocurrir por caché)
        
        // Act
        WeatherData first = cachedService.getWeatherByCityName("Madrid");
        WeatherData second = cachedService.getWeatherByCityName("Madrid");
        
        // Assert - Ambas deben ser idénticas (v1)
        assertEquals(first.getTemperature(), second.getTemperature());
        assertEquals("V1", second.getDescription());
        verify(mockWeatherService, times(1)).getWeatherByCityName("Madrid");
    }
}


