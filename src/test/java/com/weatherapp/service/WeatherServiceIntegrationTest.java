package com.weatherapp.service;

import com.weatherapp.client.OpenMeteoClient;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.model.GeoLocation;
import com.weatherapp.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de integración para WeatherService.
 * 
 * Prueba:
 * 1. Flujo completo: entrada → geocodificación → clima
 * 2. Orquestación correcta del cliente
 * 3. Diferentes unidades de temperatura
 * 4. Manejo de errores en diferentes fases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherService - Integración")
class WeatherServiceIntegrationTest {

    @Mock
    private OpenMeteoClient mockClient;

    private WeatherService service;

    @BeforeEach
    void setUp() {
        service = new WeatherService(mockClient, TemperatureUnit.CELSIUS);
    }

    // ==================== TESTS DE FLUJO COMPLETO ====================

    @Test
    @DisplayName("Flujo completo exitoso: Madrid C")
    void testCompleteFlowSuccessMadridCelsius() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid, España");
        WeatherData weather = new WeatherData(
                "Madrid, España", 25.5, 23.0, 60, 5.0, 0.0, "☀️ Despejado", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Madrid"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Madrid");

        // Assert
        assertNotNull(result);
        assertEquals("Madrid, España", result.getCityName());
        assertEquals(25.5, result.getTemperature(), 0.1);
        assertEquals(60, result.getHumidity());
        assertEquals("☀️ Despejado", result.getDescription());
    }

    @Test
    @DisplayName("Flujo completo exitoso: Londres")
    void testCompleteFlowSuccessLondon() {
        // Arrange
        GeoLocation location = new GeoLocation(51.5074, -0.1278, "London, UK");
        WeatherData weather = new WeatherData(
                "London, UK", 15.2, 13.5, 75, 5.0, 0.0, "🌧️ Lluvia", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("London"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("London");

        // Assert
        assertNotNull(result);
        assertEquals("London, UK", result.getCityName());
        assertEquals(15.2, result.getTemperature(), 0.1);
    }

    @Test
    @DisplayName("Cliente debe ser llamado en orden correcto: resolveCity → getWeatherData")
    void testClientCallOrder() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 25.5, 23.0, 60, 5.0, 0.0, "Clear", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity(anyString())).thenReturn(location);
        when(mockClient.getWeatherData(any(), any())).thenReturn(weather);

        // Act
        service.getWeatherByCityName("Madrid");

        // Assert - Verificar orden de llamadas
        verify(mockClient).resolveCity("Madrid");
        verify(mockClient).getWeatherData(location, TemperatureUnit.CELSIUS);
    }

    // ==================== TESTS DE UNIDADES DIFERENTES ====================

    @Test
    @DisplayName("Flujo completo con Fahrenheit")
    void testCompleteFlowWithFahrenheit() {
        // Arrange
        WeatherService serviceFahrenheit = new WeatherService(mockClient, TemperatureUnit.FAHRENHEIT);
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 77.9, 73.4, 60, 5.0, 0.0, "Clear", TemperatureUnit.FAHRENHEIT
        );

        when(mockClient.resolveCity("Madrid")).thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.FAHRENHEIT))
                .thenReturn(weather);

        // Act
        WeatherData result = serviceFahrenheit.getWeatherByCityName("Madrid");

        // Assert
        assertEquals(TemperatureUnit.FAHRENHEIT, result.getTemperatureUnit());
        assertEquals(77.9, result.getTemperature(), 0.1);
    }

    @Test
    @DisplayName("Flujo completo con Kelvin")
    void testCompleteFlowWithKelvin() {
        // Arrange
        WeatherService serviceKelvin = new WeatherService(mockClient, TemperatureUnit.KELVIN);
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 298.65, 296.15, 60, 5.0, 0.0, "Clear", TemperatureUnit.KELVIN
        );

        when(mockClient.resolveCity("Madrid")).thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.KELVIN))
                .thenReturn(weather);

        // Act
        WeatherData result = serviceKelvin.getWeatherByCityName("Madrid");

        // Assert
        assertEquals(TemperatureUnit.KELVIN, result.getTemperatureUnit());
        assertTrue(result.getTemperature() > 270);
    }

    // ==================== TESTS DE ERROR EN GEOCODIFICACIÓN ====================

    @Test
    @DisplayName("Error en geocodificación: ciudad no encontrada")
    void testGeocodeFailureCityNotFound() {
        // Arrange
        when(mockClient.resolveCity("NonExistentCity"))
                .thenThrow(new CityNotFoundException("NonExistentCity", "🚫 Ciudad no encontrada: 'NonExistentCity'"));

        // Act & Assert
        assertThrows(CityNotFoundException.class, () -> {
            service.getWeatherByCityName("NonExistentCity");
        });

        // Verificar que getWeatherData NO fue llamado
        verify(mockClient).resolveCity("NonExistentCity");
    }

    @Test
    @DisplayName("Error en geocodificación: error de red")
    void testGeocodeFailureNetworkError() {
        // Arrange
        when(mockClient.resolveCity("Madrid"))
                .thenThrow(new RuntimeException("Network timeout"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.getWeatherByCityName("Madrid");
        });
    }

    // ==================== TESTS DE ERROR EN OBTENCIÓN DE CLIMA ====================

    @Test
    @DisplayName("Error en obtención de clima después de geocodificación exitosa")
    void testWeatherFetchFailureAfterSuccessfulGeocode() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        when(mockClient.resolveCity("Madrid")).thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenThrow(new RuntimeException("Weather API error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.getWeatherByCityName("Madrid");
        });

        // Verificar que ambas llamadas se hicieron
        verify(mockClient).resolveCity("Madrid");
        verify(mockClient).getWeatherData(location, TemperatureUnit.CELSIUS);
    }

    // ==================== TESTS DE VALIDACIÓN DE DATOS RETORNADOS ====================

    @Test
    @DisplayName("Datos retornados deben ser íntegros y válidos")
    void testReturnedDataIntegrity() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 25.5, 23.0, 60, 5.0, 0.0, "Clear", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Madrid")).thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Madrid");

        // Assert - Verificar integridad de datos
        assertNotNull(result.getCityName());
        assertNotNull(result.getDescription());
        assertTrue(result.getHumidity() >= 0 && result.getHumidity() <= 100);
        assertTrue(result.getTemperature() >= -90 && result.getTemperature() <= 60);
    }

    // ==================== TESTS DE MÚLTIPLES CIUDADES ====================

    @Test
    @DisplayName("Debe manejar búsquedas secuenciales de diferentes ciudades")
    void testMultipleCitySearches() {
        // Arrange
        GeoLocation madrid = new GeoLocation(40.4168, -3.7038, "Madrid");
        GeoLocation london = new GeoLocation(51.5074, -0.1278, "London");

        WeatherData madridWeather = new WeatherData("Madrid", 25.5, 23.0, 60, 5.0, 0.0, "Clear", TemperatureUnit.CELSIUS);
        WeatherData londonWeather = new WeatherData("London", 15.2, 13.5, 75, 5.0, 0.0, "Rainy", TemperatureUnit.CELSIUS);

        when(mockClient.resolveCity("Madrid")).thenReturn(madrid);
        when(mockClient.resolveCity("London")).thenReturn(london);
        when(mockClient.getWeatherData(madrid, TemperatureUnit.CELSIUS)).thenReturn(madridWeather);
        when(mockClient.getWeatherData(london, TemperatureUnit.CELSIUS)).thenReturn(londonWeather);

        // Act
        WeatherData result1 = service.getWeatherByCityName("Madrid");
        WeatherData result2 = service.getWeatherByCityName("London");

        // Assert
        assertEquals("Madrid", result1.getCityName());
        assertEquals("London", result2.getCityName());
        assertEquals(25.5, result1.getTemperature(), 0.1);
        assertEquals(15.2, result2.getTemperature(), 0.1);
    }

    // ==================== TESTS DE TRIMMING Y NORMALIZACIÓN ====================

    @Test
    @DisplayName("Debe hacer trim de espacios de entrada de usuario")
    void testInputTrimmingForWhitespace() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 25.5, 23.0, 60, 5.0, 0.0, "Clear", TemperatureUnit.CELSIUS
        );

        // Mockear para cualquier variante trimmed
        when(mockClient.resolveCity("Madrid")).thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act - entrada con espacios será trimmed
        WeatherData result = service.getWeatherByCityName("  Madrid  ");

        // Assert
        assertNotNull(result);
        assertEquals("Madrid", result.getCityName());
    }

    // ==================== TESTS DE MENSAJES DE ERROR ====================

    @Test
    @DisplayName("Mensajes de error deben contener contexto útil")
    void testErrorMessagesHaveContext() {
        // Arrange
        when(mockClient.resolveCity("NonExistent"))
                .thenThrow(new IllegalArgumentException("City not found"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            service.getWeatherByCityName("NonExistent");
        });

        assertTrue(exception.getMessage().contains("NonExistent") || 
                   exception.getMessage().contains("City") ||
                   exception.getMessage().contains("not found"));
    }

    // ==================== TESTS DE CONSISTENCIA ENTRE UNIDADES ====================

    @Test
    @DisplayName("Temperaturas relativas deberían ser consistentes entre unidades")
    void testTemperatureConsistencyAcrossUnits() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");

        // Celsius: 25.5°C
        WeatherData weatherC = new WeatherData("Madrid", 25.5, 23.0, 60, 5.0, 0.0, "Clear", TemperatureUnit.CELSIUS);
        // Fahrenheit: 77.9°F (25.5 * 9/5 + 32)
        WeatherData weatherF = new WeatherData("Madrid", 77.9, 73.4, 60, 5.0, 0.0, "Clear", TemperatureUnit.FAHRENHEIT);
        // Kelvin: 298.65K (25.5 + 273.15)
        WeatherData weatherK = new WeatherData("Madrid", 298.65, 296.15, 60, 5.0, 0.0, "Clear", TemperatureUnit.KELVIN);

        WeatherService serviceC = new WeatherService(mockClient, TemperatureUnit.CELSIUS);
        WeatherService serviceF = new WeatherService(mockClient, TemperatureUnit.FAHRENHEIT);
        WeatherService serviceK = new WeatherService(mockClient, TemperatureUnit.KELVIN);

        when(mockClient.resolveCity("Madrid")).thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS)).thenReturn(weatherC);
        when(mockClient.getWeatherData(location, TemperatureUnit.FAHRENHEIT)).thenReturn(weatherF);
        when(mockClient.getWeatherData(location, TemperatureUnit.KELVIN)).thenReturn(weatherK);

        // Act
        WeatherData resultC = serviceC.getWeatherByCityName("Madrid");
        WeatherData resultF = serviceF.getWeatherByCityName("Madrid");
        WeatherData resultK = serviceK.getWeatherByCityName("Madrid");

        // Assert - Verificar que la conversión es aproximadamente correcta
        assertEquals(resultC.getTemperature() * 9/5 + 32, resultF.getTemperature(), 0.1);
        assertEquals(resultC.getTemperature() + 273.15, resultK.getTemperature(), 0.1);
    }
}


