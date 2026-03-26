package com.weatherapp.service;

import com.weatherapp.client.OpenMeteoClient;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.exception.WeatherDataException;
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
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para validaciones de datos climáticos en WeatherService.
 * 
 * Verifica que:
 * 1. Validaciones de humedad (0-100%)
 * 2. Validaciones de temperatura realista
 * 3. Validaciones de coordenadas geográficas
 * 4. Manejo correcto de datos válidos e inválidos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherService - Validaciones de Datos")
class WeatherServiceValidationTest {

    @Mock
    private OpenMeteoClient mockOpenMeteoClient;

    private WeatherService weatherService;
    private GeoLocation testLocation;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(mockOpenMeteoClient, TemperatureUnit.CELSIUS);
        testLocation = new GeoLocation(40.4168, -3.7038, "Madrid, España");
    }

    // ==================== TESTS DE HUMEDAD ====================

    @Test
    @DisplayName("Humedad válida (0%) - No lanza excepción")
    void testHumidityValid_0Percent() {
        // Arrange: WeatherData con humedad 0% (mínimo válido)
        WeatherData validWeatherData = new WeatherData(
                "Test City", 25.0, 23.0, 0, 5.0, 0.0, "Despejado", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert: No lanza excepción
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Test City"));
    }

    @Test
    @DisplayName("Humedad válida (50%) - No lanza excepción")
    void testHumidityValid_50Percent() {
        // Arrange
        WeatherData validWeatherData = new WeatherData(
                "Test City", 25.0, 23.0, 50, 5.0, 0.0, "Nublado", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Test City"));
    }

    @Test
    @DisplayName("Humedad válida (100%) - No lanza excepción")
    void testHumidityValid_100Percent() {
        // Arrange
        WeatherData validWeatherData = new WeatherData(
                "Test City", 20.0, 19.0, 100, 5.0, 0.0, "Lluvia", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Test City"));
    }

    @Test
    @DisplayName("Humedad inválida (-1%) - Lanza WeatherDataException")
    void testHumidityInvalid_Negative() {
        // Arrange: El mock lanzará WeatherDataException cuando se llame con coordenadas de Test
        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenThrow(new WeatherDataException("Humedad", -1, "❌ Humedad inválida: -1%. Debe estar entre 0% y 100%"));

        // Act & Assert
        WeatherDataException exception = assertThrows(
                WeatherDataException.class,
                () -> weatherService.getWeatherByCityName("Test City"),
                "Debería lanzar excepción por humedad negativa"
        );
        assertTrue(exception.getMessage().contains("Humedad inválida"));
    }

    @Test
    @DisplayName("Humedad inválida (101%) - Lanza WeatherDataException")
    void testHumidityInvalid_GreaterThan100() {
        // Arrange
        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenThrow(new WeatherDataException("Humedad", 101, "❌ Humedad inválida: 101%. Debe estar entre 0% y 100%"));

        // Act & Assert
        WeatherDataException exception = assertThrows(
                WeatherDataException.class,
                () -> weatherService.getWeatherByCityName("Test City"),
                "Debería lanzar excepción por humedad > 100%"
        );
        assertTrue(exception.getMessage().contains("Humedad inválida"));
    }

    // ==================== TESTS DE TEMPERATURA ====================

    @Test
    @DisplayName("Temperatura realista (25°C) - No lanza excepción")
    void testTemperatureValid_Normal() {
        // Arrange: Temperatura típica realista
        WeatherData validWeatherData = new WeatherData(
                "Test City", 25.0, 23.5, 60, 5.0, 0.0, "Despejado", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Test City"));
    }

    @Test
    @DisplayName("Temperatura mínima válida (-60°C) - No lanza excepción")
    void testTemperatureValid_MinimumExtreme() {
        // Arrange
        WeatherData validWeatherData = new WeatherData(
                "Test City", -60.0, -58.0, 40, 5.0, 0.0, "Nieve", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Test City"));
    }

    @Test
    @DisplayName("Temperatura máxima válida (+60°C) - No lanza excepción")
    void testTemperatureValid_MaximumExtreme() {
        // Arrange
        WeatherData validWeatherData = new WeatherData(
                "Test City", 60.0, 58.0, 20, 5.0, 0.0, "Despejado", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Test City"));
    }

    @Test
    @DisplayName("Temperatura irreal (-61°C) - Lanza WeatherDataException")
    void testTemperatureInvalid_TooCold() {
        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenThrow(new WeatherDataException("Temperatura", -61.0, "❌ Temperatura irreal: -61.0°C (-61.0°C). Debe estar entre -60.0°C y 60.0°C"));

        // Act & Assert
        WeatherDataException exception = assertThrows(
                WeatherDataException.class,
                () -> weatherService.getWeatherByCityName("Test City"),
                "Debería lanzar excepción por temperatura irreal (muy fría)"
        );
        assertTrue(exception.getMessage().contains("Temperatura irreal"));
    }

    @Test
    @DisplayName("Temperatura irreal (+61°C) - Lanza WeatherDataException")
    void testTemperatureInvalid_TooHot() {
        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenThrow(new WeatherDataException("Temperatura", 61.0, "❌ Temperatura irreal: 61.0°C (61.0°C). Debe estar entre -60.0°C y 60.0°C"));

        // Act & Assert
        WeatherDataException exception = assertThrows(
                WeatherDataException.class,
                () -> weatherService.getWeatherByCityName("Test City"),
                "Debería lanzar excepción por temperatura irreal (muy caliente)"
        );
        assertTrue(exception.getMessage().contains("Temperatura irreal"));
    }

    // ==================== TESTS DE TEMPERATURA APARENTE ====================

    @Test
    @DisplayName("Temperatura aparente realista - No lanza excepción")
    void testApparentTemperatureValid() {
        // Arrange
        WeatherData validWeatherData = new WeatherData(
                "Test City", 25.0, 22.5, 60, 5.0, 0.0, "Despejado", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Test City"));
    }

    @Test
    @DisplayName("Temperatura aparente irreal (-61°C) - Lanza WeatherDataException")
    void testApparentTemperatureInvalid_TooCold() {
        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenThrow(new WeatherDataException("Temperatura aparente", -61.0, "❌ Temperatura aparente irreal: -61.0°C (-61.0°C). Debe estar entre -60.0°C y 60.0°C"));

        // Act & Assert
        WeatherDataException exception = assertThrows(
                WeatherDataException.class,
                () -> weatherService.getWeatherByCityName("Test City"),
                "Debería lanzar excepción por temperatura aparente irreal"
        );
        assertTrue(exception.getMessage().contains("Temperatura aparente irreal"));
    }

    // ==================== TESTS DE COORDENADAS ====================

    @Test
    @DisplayName("Latitud válida (40.4168°) - No lanza excepción")
    void testLatitudeValid_Normal() {
        // Arrange
        WeatherData validWeatherData = new WeatherData(
                "Madrid", 25.0, 23.0, 60, 5.0, 0.0, "Despejado", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Madrid"));
    }

    @Test
    @DisplayName("Latitud mínima válida (-90°) - No lanza excepción")
    void testLatitudeValid_Minimum() {
        // Arrange
        GeoLocation southPole = new GeoLocation(-90.0, 0.0, "Polo Sur");
        WeatherData validWeatherData = new WeatherData(
                "Polo Sur", -40.0, -38.0, 80, 5.0, 0.0, "Nieve", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(southPole);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Polo Sur"));
    }

    @Test
    @DisplayName("Latitud máxima válida (+90°) - No lanza excepción")
    void testLatitudeValid_Maximum() {
        // Arrange
        GeoLocation northPole = new GeoLocation(90.0, 0.0, "Polo Norte");
        WeatherData validWeatherData = new WeatherData(
                "Polo Norte", -35.0, -33.0, 75, 5.0, 0.0, "Nieve", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(northPole);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Polo Norte"));
    }

    @Test
    @DisplayName("Longitud válida (-3.7038°) - No lanza excepción")
    void testLongitudeValid_Normal() {
        // Arrange (usando testLocation que ya tiene longitud válida)
        WeatherData validWeatherData = new WeatherData(
                "Madrid", 25.0, 23.0, 60, 5.0, 0.0, "Despejado", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Madrid"));
    }

    @Test
    @DisplayName("Longitud mínima válida (-180°) - No lanza excepción")
    void testLongitudeValid_Minimum() {
        // Arrange
        GeoLocation datelineLong = new GeoLocation(0.0, -180.0, "Línea de fecha");
        WeatherData validWeatherData = new WeatherData(
                "Línea de fecha", 20.0, 19.0, 50, 5.0, 0.0, "Despejado", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(datelineLong);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Línea de fecha"));
    }

    @Test
    @DisplayName("Longitud máxima válida (+180°) - No lanza excepción")
    void testLongitudeValid_Maximum() {
        // Arrange
        GeoLocation datelinePositive = new GeoLocation(0.0, 180.0, "Línea de fecha +");
        WeatherData validWeatherData = new WeatherData(
                "Línea de fecha +", 20.0, 19.0, 50, 5.0, 0.0, "Despejado", TemperatureUnit.CELSIUS
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(datelinePositive);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Línea de fecha +"));
    }

    // ==================== TESTS DE MANEJO DE ERRORES ====================

    @Test
    @DisplayName("Ciudad no encontrada - Lanza CityNotFoundException")
    void testCityNotFound() {
        // Arrange
        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenThrow(new CityNotFoundException("XyzyZ999", "🚫 Ciudad no encontrada: 'XyzyZ999'"));

        // Act & Assert
        CityNotFoundException exception = assertThrows(
                CityNotFoundException.class,
                () -> weatherService.getWeatherByCityName("XyzyZ999"),
                "Debería propagar excepción de ciudad no encontrada"
        );
        assertTrue(exception.getMessage().contains("Ciudad no encontrada"));
    }

    @Test
    @DisplayName("Ciudad vacía - Lanza IllegalArgumentException")
    void testEmptyCity() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> weatherService.getWeatherByCityName(""),
                "Debería rechazar ciudad vacía"
        );
        assertTrue(exception.getMessage().contains("no puede estar vacío"));
    }

    @Test
    @DisplayName("Error de conexión - Lanza RuntimeException")
    void testNetworkError() {
        // Arrange
        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenThrow(new RuntimeException("Error de conexión después de 3 intentos"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> weatherService.getWeatherByCityName("Madrid"),
                "Debería propagar RuntimeException de error de conexión"
        );
        assertTrue(exception.getMessage().contains("obtener datos climáticos") || 
                   exception.getMessage().contains("Verifica tu conexión"));
    }

    // ==================== TESTS DE CONVERSIÓN DE UNIDADES ====================

    @Test
    @DisplayName("Temperatura en Fahrenheit realista - No lanza excepción")
    void testTemperatureValid_Fahrenheit() {
        // Arrange: 77°F ≈ 25°C (realista)
        WeatherData validWeatherData = new WeatherData(
                "Test City", 77.0, 73.5, 60, 5.0, 0.0, "Despejado", TemperatureUnit.FAHRENHEIT
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Test City"));
    }

    @Test
    @DisplayName("Temperatura en Kelvin realista - No lanza excepción")
    void testTemperatureValid_Kelvin() {
        // Arrange: 298.15K ≈ 25°C (realista)
        WeatherData validWeatherData = new WeatherData(
                "Test City", 298.15, 296.65, 60, 5.0, 0.0, "Despejado", TemperatureUnit.KELVIN
        );

        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenReturn(validWeatherData);

        // Act & Assert
        assertDoesNotThrow(() -> weatherService.getWeatherByCityName("Test City"));
    }

    @Test
    @DisplayName("Temperatura en Fahrenheit irreal - Lanza WeatherDataException")
    void testTemperatureInvalid_FahrenheitTooHot() {
        when(mockOpenMeteoClient.resolveCity(anyString()))
                .thenReturn(testLocation);
        when(mockOpenMeteoClient.getWeatherData(any(GeoLocation.class), any(TemperatureUnit.class)))
                .thenThrow(new WeatherDataException("Temperatura", 142.0, "❌ Temperatura irreal: 142.0°F (61.1°C). Debe estar entre -60.0°C y 60.0°C"));

        // Act & Assert
        WeatherDataException exception = assertThrows(
                WeatherDataException.class,
                () -> weatherService.getWeatherByCityName("Test City"),
                "Debería lanzar excepción por temperatura irreal en Fahrenheit"
        );
        assertTrue(exception.getMessage().contains("Temperatura irreal"));
    }
}



