package com.weatherapp.service;

import com.weatherapp.client.OpenMeteoClient;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.model.GeoLocation;
import com.weatherapp.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase WeatherService.
 * Prueba validación de entrada, manejo de errores y orquestación de lógica.
 * Nota: Los tests de integración real con Open-Meteo API se omiten
 * para mantener tests rápidos y no depender de conectividad externa.
 */
@DisplayName("WeatherService - Pruebas Unitarias")
public class WeatherServiceTest {

    private MockOpenMeteoClient mockClient;
    private WeatherService service;

    /**
     * Configura los mocks antes de cada test.
     */
    @BeforeEach
    void setUp() {
        mockClient = new MockOpenMeteoClient();
        service = new WeatherService(mockClient, TemperatureUnit.CELSIUS);
    }

    // ===== TESTS DE VALIDACIÓN DE ENTRADA =====

    @Test
    @DisplayName("Debe lanzar excepción para ciudad nula")
    void testNullCityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getWeatherByCityName(null);
        });
    }

    @Test
    @DisplayName("Debe lanzar excepción para ciudad vacía")
    void testEmptyCityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getWeatherByCityName("");
        });
    }

    @Test
    @DisplayName("Debe lanzar excepción para ciudad solo espacios")
    void testWhitespaceCityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getWeatherByCityName("   ");
        });
    }

    // ===== TESTS DE PROCESAMIENTO EXITOSO =====

    @Test
    @DisplayName("Debe retornar WeatherData válido para ciudad existente")
    void testSuccessfulWeatherDataFetch() {
        WeatherData result = service.getWeatherByCityName("Madrid");

        assertNotNull(result);
        assertEquals("Madrid, España", result.getCityName());
        assertEquals(25.5, result.getTemperature(), 0.1);
        assertEquals(23.0, result.getApparentTemperature(), 0.1);
        assertEquals(60, result.getHumidity());
        assertEquals("☀️ Despejado", result.getDescription());
    }

    @Test
    @DisplayName("Debe preservar unidad de temperatura correctamente")
    void testTemperatureUnitPreserved() {
        WeatherData result = service.getWeatherByCityName("Londres");

        assertNotNull(result);
        assertEquals(TemperatureUnit.CELSIUS, result.getTemperatureUnit());
        assertNotNull(result.getDescription());
        assertTrue(result.getDescription().length() > 0);
    }

    @Test
    @DisplayName("Debe usar la unidad configurada del servicio")
    void testServiceUsesConfiguredUnit() {
        WeatherService serviceF = new WeatherService(mockClient, TemperatureUnit.FAHRENHEIT);
        WeatherData result = serviceF.getWeatherByCityName("París");

        assertEquals(TemperatureUnit.FAHRENHEIT, result.getTemperatureUnit());
    }

    // ===== TESTS DE MANEJO DE ERRORES =====

    @Test
    @DisplayName("Debe lanzar excepción para ciudad no encontrada")
    void testCityNotFoundThrowsException() {
        mockClient.setCityNotFound(true);

        assertThrows(RuntimeException.class, () -> {
            service.getWeatherByCityName("CiudadInexistente123");
        });
    }

    @Test
    @DisplayName("Debe lanzar excepción en error de red")
    void testNetworkErrorThrowsException() {
        mockClient.setNetworkError(true);

        assertThrows(RuntimeException.class, () -> {
            service.getWeatherByCityName("Madrid");
        });
    }

    @Test
    @DisplayName("Debe capturar RuntimeException y re-lanzarla con contexto")
    void testExceptionHandlingWithContext() {
        mockClient.setNetworkError(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getWeatherByCityName("TestCity");
        });

        assertTrue(exception.getMessage().contains("obtener datos climáticos"));
    }

    // ===== TESTS DE GETTER =====

    @Test
    @DisplayName("Debe retornar la unidad de temperatura configurada")
    void testGetTemperatureUnit() {
        assertEquals(TemperatureUnit.CELSIUS, service.getTemperatureUnit());

        WeatherService serviceK = new WeatherService(mockClient, TemperatureUnit.KELVIN);
        assertEquals(TemperatureUnit.KELVIN, serviceK.getTemperatureUnit());
    }

    // ===== TESTS DE CASOS LÍMITES =====

    @Test
    @DisplayName("Debe manejar nombres de ciudad con espacios/caracteres especiales")
    void testCityNameWithSpecialCharacters() {
        WeatherData result = service.getWeatherByCityName("Nueva York");

        assertNotNull(result);
        assertTrue(result.getCityName().contains("Nueva") || result.getCityName().contains("York") ||
                   result.getCityName().contains("New"));
    }

    @Test
    @DisplayName("Debe preservar datos climáticos correctamente")
    void testWeatherDataIntegrity() {
        WeatherData result = service.getWeatherByCityName("Berlín");

        assertNotNull(result.getCityName());
        assertNotNull(result.getDescription());
        assertTrue(result.getHumidity() >= 0 && result.getHumidity() <= 100);
        assertNotNull(result.getTemperatureUnit());
    }

    // ===== MOCK DEL CLIENTE PARA TESTING =====

    /**
     * Cliente mock de Open-Meteo para testing sin dependencias externas.
     * Simula respuestas de API sin hacer llamadas reales.
     */
    static class MockOpenMeteoClient extends OpenMeteoClient {

        private boolean cityNotFound = false;
        private boolean networkError = false;

        public void setCityNotFound(boolean value) {
            this.cityNotFound = value;
        }

        public void setNetworkError(boolean value) {
            this.networkError = value;
        }

        @Override
        public GeoLocation resolveCity(String cityName) {
            if (networkError) {
                throw new RuntimeException("Simulated network error");
            }

            if (cityNotFound) {
                throw new CityNotFoundException("🚫 Ciudad no encontrada: '" + cityName + "'", cityName);
            }

            // Simular respuestas según el nombre
            switch (cityName) {
                case "Madrid":
                    return new GeoLocation(40.4168, -3.7038, "Madrid, España");
                case "Londres":
                    return new GeoLocation(51.5074, -0.1278, "Londres, Reino Unido");
                case "París":
                    return new GeoLocation(48.8566, 2.3522, "París, Francia");
                case "Nueva York":
                    return new GeoLocation(40.7128, -74.0060, "Nueva York, EE.UU.");
                case "Berlín":
                    return new GeoLocation(52.5200, 13.4050, "Berlín, Alemania");
                default:
                    return new GeoLocation(0, 0, cityName);
            }
        }

        @Override
        public WeatherData getWeatherData(GeoLocation location, TemperatureUnit temperatureUnit) {
            if (networkError) {
                throw new RuntimeException("Simulated network error");
            }

            // Simular diferentes datos según la ciudad
            String cityName = location.getCityName();
            if ("Madrid, España".equals(cityName)) {
                return new WeatherData(
                        "Madrid, España", 25.5, 23.0, 60, 5.0, 0.0,
                        "☀️ Despejado", temperatureUnit);
            } else if ("Londres, Reino Unido".equals(cityName)) {
                return new WeatherData(
                        "Londres, Reino Unido", 15.2, 13.5, 75, 5.0, 0.0,
                        "🌦️ Lluvia ligera", temperatureUnit);
            } else if ("París, Francia".equals(cityName)) {
                return new WeatherData(
                        "París, Francia", 18.3, 17.0, 70, 5.0, 0.0,
                        "☁️ Parcialmente nublado", temperatureUnit);
            } else if ("Nueva York, EE.UU.".equals(cityName)) {
                return new WeatherData(
                        "Nueva York, EE.UU.", 22.5, 20.8, 55, 5.0, 0.0,
                        "🌤️ Mayormente despejado", temperatureUnit);
            } else if ("Berlín, Alemania".equals(cityName)) {
                return new WeatherData(
                        "Berlín, Alemania", 16.0, 14.5, 68, 5.0, 0.0,
                        "☁️ Parcialmente nublado", temperatureUnit);
            } else {
                return new WeatherData(
                        location.getCityName(), 20.0, 18.0, 60, 5.0, 0.0,
                        "🌤️ Clima neutral", temperatureUnit);
            }
        }

        @Override
        public String parseWeatherCode(int weatherCode) {
            // Usar implementación base
            return super.parseWeatherCode(weatherCode);
        }
    }
}
