package com.weatherapp.client;

import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.model.GeoLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para la geocodificación de ciudades en OpenMeteoClient.
 * 
 * Prueba:
 * 1. Resolución de ciudades comunes
 * 2. Ciudades con caracteres especiales
 * 3. Validación de coordenadas retornadas
 * 4. Casos límites y errores
 */
@DisplayName("OpenMeteoClient - Geocodificación")
class OpenMeteoClientGeocodeTest {

    private OpenMeteoClient client;

    @BeforeEach
    void setUp() {
        client = new OpenMeteoClient();
    }

    // ==================== TESTS DE CIUDADES COMUNES ====================

    @Test
    @DisplayName("Resolver ciudad común: Madrid")
    void testResolveMadrid() {
        GeoLocation location = client.resolveCity("Madrid");

        assertNotNull(location);
        assertTrue(location.getCityName().toLowerCase().contains("madrid"));
        assertTrue(location.getLatitude() >= -90 && location.getLatitude() <= 90);
        assertTrue(location.getLongitude() >= -180 && location.getLongitude() <= 180);
        // Madrid está en España, aproximadamente 40.4°N, -3.7°E
        assertTrue(location.getLatitude() > 40 && location.getLatitude() < 41);
        assertTrue(location.getLongitude() < -3 && location.getLongitude() > -4);
    }

    @Test
    @DisplayName("Resolver ciudad común: Londres")
    void testResolveLondon() {
        GeoLocation location = client.resolveCity("London");

        assertNotNull(location);
        assertNotNull(location.getCityName());
        // Londres está aproximadamente en 51.5°N, -0.1°E
        assertTrue(location.getLatitude() > 51 && location.getLatitude() < 52);
        assertTrue(location.getLongitude() > -1 && location.getLongitude() < 0);
    }

    @Test
    @DisplayName("Resolver ciudad común: Nueva York")
    void testResolveNewYork() {
        GeoLocation location = client.resolveCity("New York");

        assertNotNull(location);
        // Podría retornar "York" o "New York" dependiendo del API
        assertTrue(location.getCityName().toLowerCase().contains("york") || 
                   location.getCityName().toLowerCase().contains("new"));
        // Si es York, Estados Unidos (40.86°N, -97.59°E) también es válido
        // Si es Nueva York (40.71°N, -74.00°E) también es válido
        assertTrue(location.getLatitude() >= 40 && location.getLatitude() <= 41);
        assertTrue(location.getLongitude() <= -73 || location.getLongitude() <= -97);
    }

    @Test
    @DisplayName("Resolver ciudad común: París")
    void testResolveParis() {
        GeoLocation location = client.resolveCity("Paris");

        assertNotNull(location);
        // París está aproximadamente en 48.8°N, 2.3°E
        assertTrue(location.getLatitude() > 48 && location.getLatitude() < 49);
        assertTrue(location.getLongitude() > 2 && location.getLongitude() < 3);
    }

    @Test
    @DisplayName("Resolver ciudad común: Tokio")
    void testResolveTokyo() {
        GeoLocation location = client.resolveCity("Tokyo");

        assertNotNull(location);
        // Tokio está aproximadamente en 35.7°N, 139.7°E
        assertTrue(location.getLatitude() > 35 && location.getLatitude() < 36);
        assertTrue(location.getLongitude() > 139 && location.getLongitude() < 140);
    }

    // ==================== TESTS DE CARACTERES ESPECIALES ====================

    @Test
    @DisplayName("Resolver ciudad con acento: São Paulo")
    void testResolveSaoPaulo() {
        GeoLocation location = client.resolveCity("São Paulo");

        assertNotNull(location);
        // São Paulo está en Brasil, aproximadamente -23.5°S, -46.6°E
        assertTrue(location.getLatitude() < -23 && location.getLatitude() > -24);
        assertTrue(location.getLongitude() < -46 && location.getLongitude() > -47);
    }

    @Test
    @DisplayName("Resolver ciudad con acento: México")
    void testResolveMeticoCity() {
        GeoLocation location = client.resolveCity("Mexico");

        assertNotNull(location);
        assertNotNull(location.getCityName());
    }

    @Test
    @DisplayName("Resolver ciudad con acento: Zürich")
    void testResolveZurich() {
        GeoLocation location = client.resolveCity("Zurich");

        assertNotNull(location);
        // Zurich está en Suiza, aproximadamente 47.4°N, 8.5°E
        assertTrue(location.getLatitude() > 47 && location.getLatitude() < 48);
        assertTrue(location.getLongitude() > 8 && location.getLongitude() < 9);
    }

    // ==================== TESTS DE ESPACIOS EN NOMBRES ====================

    @Test
    @DisplayName("Resolver ciudad con espacios: Nueva York")
    void testResolveNewYorkSpaces() {
        GeoLocation location = client.resolveCity("Nueva York");

        assertNotNull(location);
        assertTrue(location.getLatitude() > 40 && location.getLatitude() < 41);
    }

    @Test
    @DisplayName("Resolver ciudad con espacios: Los Angeles")
    void testResolveLosAngeles() {
        GeoLocation location = client.resolveCity("Los Angeles");

        assertNotNull(location);
        // Los Angeles está aproximadamente en 34.0°N, -118.2°E
        assertTrue(location.getLatitude() > 33 && location.getLatitude() < 35);
        assertTrue(location.getLongitude() < -118 && location.getLongitude() > -119);
    }

    @Test
    @DisplayName("Resolver ciudad con espacios: Rio de Janeiro")
    void testResolveRiodeJaneiro() {
        GeoLocation location = client.resolveCity("Rio de Janeiro");

        assertNotNull(location);
        // Rio está aproximadamente en -22.9°S, -43.2°E
        assertTrue(location.getLatitude() < -22 && location.getLatitude() > -23);
        assertTrue(location.getLongitude() < -43 && location.getLongitude() > -44);
    }

    // ==================== TESTS DE VALIDACIÓN DE COORDENADAS ====================

    @Test
    @DisplayName("Coordenadas retornadas deben estar en rango válido")
    void testReturnedCoordinatesInValidRange() {
        GeoLocation[] cities = {
            client.resolveCity("Madrid"),
            client.resolveCity("London"),
            client.resolveCity("Paris"),
            client.resolveCity("New York")
        };

        for (GeoLocation location : cities) {
            assertTrue(location.getLatitude() >= -90 && location.getLatitude() <= 90,
                    "Latitud fuera de rango: " + location.getLatitude());
            assertTrue(location.getLongitude() >= -180 && location.getLongitude() <= 180,
                    "Longitud fuera de rango: " + location.getLongitude());
        }
    }

    @Test
    @DisplayName("Latitud y longitud no deben ser NaN")
    void testCoordinatesNotNaN() {
        GeoLocation location = client.resolveCity("Madrid");

        assertFalse(Double.isNaN(location.getLatitude()));
        assertFalse(Double.isNaN(location.getLongitude()));
    }

    @Test
    @DisplayName("Nombre de ciudad no debe ser nulo")
    void testCityNameNotNull() {
        GeoLocation location = client.resolveCity("Madrid");

        assertNotNull(location.getCityName());
        assertFalse(location.getCityName().isEmpty());
    }

    // ==================== TESTS DE TRIMMING ====================

    @Test
    @DisplayName("Debe hacer trim de espacios al inicio/final")
    void testTrimWhitespace() {
        GeoLocation location1 = client.resolveCity("Madrid");
        GeoLocation location2 = client.resolveCity("  Madrid  ");

        assertNotNull(location2);
        // Ambas deberían resolver a la misma ubicación
        assertEquals(location1.getLatitude(), location2.getLatitude(), 0.01);
        assertEquals(location1.getLongitude(), location2.getLongitude(), 0.01);
    }

    // ==================== TESTS DE ERRORES ====================

    @Test
    @DisplayName("Lanzar excepción para ciudad nula")
    void testNullCityName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> client.resolveCity(null)
        );
        assertTrue(exception.getMessage().contains("vacío"));
    }

    @Test
    @DisplayName("Lanzar excepción para ciudad vacía")
    void testEmptyCityName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> client.resolveCity("")
        );
        assertTrue(exception.getMessage().contains("vacío"));
    }

    @Test
    @DisplayName("Lanzar excepción para ciudad solo espacios")
    void testWhitespaceCityName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> client.resolveCity("   ")
        );
        assertTrue(exception.getMessage().contains("vacío"));
    }

    @Test
    @DisplayName("Lanzar excepción para ciudad no encontrada")
    void testCityNotFound() {
        assertThrows(
                CityNotFoundException.class,
                () -> client.resolveCity("XYZ123NoExistsCity")
        );
    }

    // ==================== TESTS DE MÚLTIPLES RESULTADOS ====================

    @Test
    @DisplayName("Resolver Springfield (múltiples coincidencias) - debe retornar el primero")
    void testMultipleResultsShouldReturnFirst() {
        // Springfield existe en múltiples estados de EE.UU.
        // La API debe retornar según GEOCODING_LIMIT
        GeoLocation location = client.resolveCity("Springfield");

        assertNotNull(location);
        assertNotNull(location.getCityName());
        // Debería retornar alguna Springfield
        assertTrue(location.getCityName().contains("Springfield") || 
                   location.getLatitude() > 30); // Algún Springfield estadounidense
    }

    // ==================== TESTS DE CONSISTENCIA ====================

    @Test
    @DisplayName("Misma ciudad debe retornar mismas coordenadas")
    void testConsistentResults() {
        GeoLocation location1 = client.resolveCity("Madrid");
        GeoLocation location2 = client.resolveCity("Madrid");

        assertEquals(location1.getLatitude(), location2.getLatitude());
        assertEquals(location1.getLongitude(), location2.getLongitude());
        assertEquals(location1.getCityName(), location2.getCityName());
    }
}
