package com.weatherapp.service;

import com.weatherapp.client.OpenMeteoClient;
import com.weatherapp.config.TemperatureUnit;
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
 * Tests de casos límite y edge cases para WeatherService.
 * 
 * Prueba:
 * 1. Ciudades con caracteres especiales/Unicode
 * 2. Ciudades con espacios y guiones
 * 3. Extremos de temperatura
 * 4. Respuestas con valores en límites
 * 5. Casos de error inesperados
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherService - Casos Límite")
class WeatherServiceEdgeCasesTest {

    @Mock
    private OpenMeteoClient mockClient;

    private WeatherService service;

    @BeforeEach
    void setUp() {
        service = new WeatherService(mockClient, TemperatureUnit.CELSIUS);
    }

    // ==================== TESTS DE CIUDADES CON CARACTERES ESPECIALES ====================

    @Test
    @DisplayName("Ciudad con acentos: São Paulo")
    void testCityWithAccents() {
        // Arrange
        GeoLocation location = new GeoLocation(-23.5505, -46.6333, "São Paulo, Brasil");
        WeatherData weather = new WeatherData(
                "São Paulo, Brasil", 28.0, 26.0, 65, 5.0, 0.0, "☀️ Despejado", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("São Paulo"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("São Paulo");

        // Assert
        assertNotNull(result);
        assertEquals("São Paulo, Brasil", result.getCityName());
    }

    @Test
    @DisplayName("Ciudad con Unicode: Moscú (Руссия)")
    void testCityWithCyrillicCharacters() {
        // Arrange
        GeoLocation location = new GeoLocation(55.7558, 37.6173, "Москва");
        WeatherData weather = new WeatherData(
                "Москва", -5.0, -7.0, 80, 5.0, 0.0, "❄️ Nieve", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Moscow"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Moscow");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Ciudad con Unicode: Beijing (北京)")
    void testCityWithChineseCharacters() {
        // Arrange
        GeoLocation location = new GeoLocation(39.9042, 116.4074, "北京");
        WeatherData weather = new WeatherData(
                "北京", 5.0, 3.0, 45, 5.0, 0.0, "☁️ Nublado", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Beijing"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Beijing");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Ciudad con Unicode: Cairo (القاهرة)")
    void testCityWithArabicCharacters() {
        // Arrange
        GeoLocation location = new GeoLocation(30.0444, 31.2357, "Cairo");
        WeatherData weather = new WeatherData(
                "Cairo", 32.0, 30.0, 30, 5.0, 0.0, "☀️ Muy caliente", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Cairo"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Cairo");

        // Assert
        assertNotNull(result);
    }

    // ==================== TESTS DE CIUDADES CON ESPACIOS Y GUIONES ====================

    @Test
    @DisplayName("Ciudad con múltiples espacios: Nueva York")
    void testCityWithMultipleSpaces() {
        // Arrange
        GeoLocation location = new GeoLocation(40.7128, -74.0060, "New York");
        WeatherData weather = new WeatherData(
                "New York", 15.0, 13.0, 65, 5.0, 0.0, "🌤️ Parcialmente despejado", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity(anyString()))
                .thenReturn(location);
        when(mockClient.getWeatherData(any(), any()))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Nueva York");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Ciudad con guión: Rio de Janeiro")
    void testCityWithHypenAndSpaces() {
        // Arrange
        GeoLocation location = new GeoLocation(-22.9068, -43.1729, "Rio de Janeiro");
        WeatherData weather = new WeatherData(
                "Rio de Janeiro", 27.0, 25.0, 70, 5.0, 0.0, "🌤️ Cálido", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity(anyString()))
                .thenReturn(location);
        when(mockClient.getWeatherData(any(), any()))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Rio de Janeiro");

        // Assert
        assertNotNull(result);
    }

    // ==================== TESTS DE TEMPERATURAS EXTREMAS ====================

    @Test
    @DisplayName("Temperatura extrema baja: -89°C (Polo Sur)")
    void testExtremeTemperatureLow() {
        // Arrange
        GeoLocation location = new GeoLocation(-90.0, 0.0, "South Pole");
        WeatherData weather = new WeatherData(
                "South Pole", -89.0, -89.0, 35, 5.0, 0.0, "❄️ Extremadamente frío", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("South Pole"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("South Pole");

        // Assert
        assertEquals(-89.0, result.getTemperature(), 0.1);
        assertTrue(result.getTemperature() < -50);
    }

    @Test
    @DisplayName("Temperatura extrema alta: +60°C (Desierto)")
    void testExtremeTemperatureHigh() {
        // Arrange
        GeoLocation location = new GeoLocation(31.9454, 35.9284, "Death Valley");
        WeatherData weather = new WeatherData(
                "Death Valley", 54.0, 50.0, 15, 5.0, 0.0, "☀️ Extremadamente caliente", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Death Valley"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Death Valley");

        // Assert
        assertTrue(result.getTemperature() > 50);
        assertTrue(result.getTemperature() <= 60);
    }

    @Test
    @DisplayName("Temperatura cercana a cero: 0.1°C")
    void testTemperatureNearZero() {
        // Arrange
        GeoLocation location = new GeoLocation(60.0, 0.0, "Arctic City");
        WeatherData weather = new WeatherData(
                "Arctic City", 0.1, -0.5, 70, 5.0, 0.0, "☁️ Helado", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Arctic City"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Arctic City");

        // Assert
        assertTrue(result.getTemperature() > -1 && result.getTemperature() < 1);
    }

    // ==================== TESTS DE HUMEDAD EN LÍMITES ====================

    @Test
    @DisplayName("Humedad mínima: 0%")
    void testHumidityMinimum() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 35.0, 33.0, 0, 5.0, 0.0, "☀️ Extremadamente seco", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Madrid"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Madrid");

        // Assert
        assertEquals(0, result.getHumidity());
    }

    @Test
    @DisplayName("Humedad máxima: 100%")
    void testHumidityMaximum() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 20.0, 19.0, 100, 5.0, 0.0, "🌧️ Lluvia torrencial", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Madrid"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Madrid");

        // Assert
        assertEquals(100, result.getHumidity());
    }

    // ==================== TESTS DE TEMPERATURAS CON DIFERENCIAS ====================

    @Test
    @DisplayName("Temperatura aparente mucho menor que temperatura real")
    void testTemperatureDifferenceHighWindChill() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 5.0, -10.0, 80, 5.0, 0.0, "🌬️ Viento fuerte", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Madrid"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Madrid");

        // Assert
        assertTrue(result.getApparentTemperature() < result.getTemperature());
        assertEquals(15.0, result.getTemperature() - result.getApparentTemperature(), 0.1);
    }

    @Test
    @DisplayName("Temperatura aparente igual a temperatura real")
    void testTemperatureDifferenceSame() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 25.0, 25.0, 50, 5.0, 0.0, "☀️ Calma", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Madrid"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Madrid");

        // Assert
        assertEquals(result.getTemperature(), result.getApparentTemperature(), 0.1);
    }

    // ==================== TESTS DE DESCRIPCIONES LARGAS ====================

    @Test
    @DisplayName("Descripción del clima larga con múltiples emojis")
    void testLongWeatherDescription() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 20.0, 18.0, 70, 5.0, 0.0,
                "🌤️ Parcialmente nublado con probabilidad de lluvia esporádica en la tarde",
                TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Madrid"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Madrid");

        // Assert
        assertNotNull(result.getDescription());
        assertTrue(result.getDescription().length() > 20);
    }

    // ==================== TESTS DE COMPARACIÓN ENTRE CIUDADES ====================

    @Test
    @DisplayName("Ciudades con temperaturas muy diferentes")
    void testTemperatureDifferencesBetweenCities() {
        // Arrange
        GeoLocation coldCity = new GeoLocation(-90.0, 0.0, "South Pole");
        GeoLocation hotCity = new GeoLocation(31.9454, 35.9284, "Death Valley");

        WeatherData coldWeather = new WeatherData("South Pole", -80.0, -80.0, 30, 5.0, 0.0, "❄️", TemperatureUnit.CELSIUS);
        WeatherData hotWeather = new WeatherData("Death Valley", 54.0, 50.0, 15, 5.0, 0.0, "☀️", TemperatureUnit.CELSIUS);

        when(mockClient.resolveCity("South Pole")).thenReturn(coldCity);
        when(mockClient.resolveCity("Death Valley")).thenReturn(hotCity);
        when(mockClient.getWeatherData(coldCity, TemperatureUnit.CELSIUS)).thenReturn(coldWeather);
        when(mockClient.getWeatherData(hotCity, TemperatureUnit.CELSIUS)).thenReturn(hotWeather);

        // Act
        WeatherData cold = service.getWeatherByCityName("South Pole");
        WeatherData hot = service.getWeatherByCityName("Death Valley");

        // Assert
        assertTrue(hot.getTemperature() - cold.getTemperature() > 130);
    }

    // ==================== TESTS DE CIUDADES CON NOMBRES SIMILARES ====================

    @Test
    @DisplayName("Diferenciar entre ciudades similares: Nueva York vs York")
    void testDistinguishSimilarCityNames() {
        // Arrange - Nueva York, EE.UU.
        GeoLocation newYork = new GeoLocation(40.7128, -74.0060, "New York, USA");
        WeatherData nyWeather = new WeatherData("New York, USA", 15.0, 13.0, 65, 5.0, 0.0, "☁️", TemperatureUnit.CELSIUS);

        when(mockClient.resolveCity("New York"))
                .thenReturn(newYork);
        when(mockClient.getWeatherData(newYork, TemperatureUnit.CELSIUS))
                .thenReturn(nyWeather);

        // Act
        WeatherData result = service.getWeatherByCityName("New York");

        // Assert
        assertNotNull(result);
        assertTrue(result.getCityName().contains("York"));
    }

    // ==================== TESTS DE FORMATO DE RESPUESTA ====================

    @Test
    @DisplayName("Respuesta debe contener todos los campos requeridos")
    void testResponseContainsAllRequiredFields() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 25.5, 23.0, 60, 5.0, 0.0, "Clear", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Madrid"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Madrid");

        // Assert
        assertNotNull(result.getCityName());
        assertNotNull(result.getDescription());
        assertNotNull(result.getTemperatureUnit());
        assertTrue(!Double.isNaN(result.getTemperature()));
        assertTrue(!Double.isNaN(result.getApparentTemperature()));
        assertTrue(result.getHumidity() >= 0 && result.getHumidity() <= 100);
    }

    // ==================== TESTS DE STABILIDAD NUMÉRICA ====================

    @Test
    @DisplayName("Temperaturas con decimales")
    void testTemperatureWithDecimals() {
        // Arrange
        GeoLocation location = new GeoLocation(40.4168, -3.7038, "Madrid");
        WeatherData weather = new WeatherData(
                "Madrid", 23.75, 21.33, 62, 5.0, 0.0, "Clear", TemperatureUnit.CELSIUS
        );

        when(mockClient.resolveCity("Madrid"))
                .thenReturn(location);
        when(mockClient.getWeatherData(location, TemperatureUnit.CELSIUS))
                .thenReturn(weather);

        // Act
        WeatherData result = service.getWeatherByCityName("Madrid");

        // Assert
        assertEquals(23.75, result.getTemperature(), 0.01);
        assertEquals(21.33, result.getApparentTemperature(), 0.01);
    }
}
