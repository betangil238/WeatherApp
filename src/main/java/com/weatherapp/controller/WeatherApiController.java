package com.weatherapp.controller;

import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.model.WeatherData;
import com.weatherapp.service.WeatherService;
import com.weatherapp.service.WeatherForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para exponer los servicios de clima.
 * Integra WeatherService y WeatherForecastService para la interfaz web.
 * 
 * Endpoints:
 * - GET /api/weather - Clima actual de una ciudad
 * - GET /api/weather/multiple - Clima de múltiples ciudades
 * - GET /api/weather/forecast - Pronóstico de 5 días
 */
@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WeatherApiController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherForecastService forecastService;

    /**
     * Obtener clima actual para una ciudad.
     * 
     * @param city Nombre de la ciudad
     * @param unit Unidad de temperatura (celsius, fahrenheit, kelvin)
     * @return WeatherDTO con datos del clima
     */
    @GetMapping("")
    public ResponseEntity<?> getWeather(
            @RequestParam String city,
            @RequestParam(defaultValue = "celsius", required = false) String unit) {

        try {
            // Validar temperatura
            TemperatureUnit temperatureUnit = parseTemperatureUnit(unit);

            // Obtener datos
            WeatherData weatherData = weatherService.getWeatherByCityName(city);

            // Mapear a DTO
            WeatherDTO response = mapWeatherDataToDTO(weatherData, temperatureUnit);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(createErrorResponse("Ciudad no encontrada: " + city));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(createErrorResponse("Error: " + ex.getMessage()));
        }
    }

    /**
     * Obtener clima para múltiples ciudades.
     * 
     * @param cities Lista de ciudades separadas por comas
     * @param unit Unidad de temperatura
     * @return Lista de WeatherDTO
     */
    @GetMapping("/multiple")
    public ResponseEntity<?> getMultipleWeather(
            @RequestParam String cities,
            @RequestParam(defaultValue = "celsius", required = false) String unit) {

        try {
            TemperatureUnit temperatureUnit = parseTemperatureUnit(unit);

            // Parsearlista de ciudades
            List<String> cityList = Arrays.stream(cities.split(","))
                    .map(String::trim)
                    .filter(c -> !c.isEmpty())
                    .collect(Collectors.toList());

            if (cityList.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Por favor proporciona al menos una ciudad"));
            }

            // Obtener datos para todas las ciudades
            List<WeatherData> results = weatherService.getWeatherByMultipleCities(cityList);

            // Mapear a DTOs
            List<WeatherDTO> response = results.stream()
                    .map(data -> mapWeatherDataToDTO(data, temperatureUnit))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(createErrorResponse("Error: " + ex.getMessage()));
        }
    }

    /**
     * Obtener pronóstico de 5 días.
     * 
     * @param city Nombre de la ciudad
     * @param unit Unidad de temperatura
     * @return ForecastDTO con pronóstico
     */
    @GetMapping("/forecast")
    public ResponseEntity<?> getForecast(
            @RequestParam String city,
            @RequestParam(defaultValue = "celsius", required = false) String unit) {

        try {
            TemperatureUnit temperatureUnit = parseTemperatureUnit(unit);

            // Obtener pronóstico (nota: actualmente devuelve datos de demostración)
            // En producción, esto consumiría la API de Open-Meteo con parámetro 'daily'
            var forecastData = forecastService.getForecast5Days(city);

            // Crear respuesta
            ForecastDTO response = new ForecastDTO();
            response.setCityName(forecastData.getCityName());
            response.setWeatherUnit(temperatureUnit.name());

            // Mapear días del pronóstico
            response.setDays(forecastData.getDailyForecasts().stream()
                    .map(daily -> {
                        DayForecastDTO day = new DayForecastDTO();
                        day.setDate(daily.getDate().toString());
                        day.setMaxTemp(convertTemperature(daily.getMaxTemperature(), temperatureUnit));
                        day.setMinTemp(convertTemperature(daily.getMinTemperature(), temperatureUnit));
                        day.setHumidity(daily.getHumidity());
                        day.setDescription(daily.getDescription());
                        return day;
                    })
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Ciudad no encontrada: " + city));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(createErrorResponse("Error: " + ex.getMessage()));
        }
    }

    /**
     * Health check del API
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "✅ API funcionando correctamente");
        response.put("service", "Weather Service");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /* ==================== FUNCIONES AUXILIARES ==================== */

    /**
     * Mapear WeatherData a WeatherDTO
     */
    private WeatherDTO mapWeatherDataToDTO(WeatherData data, TemperatureUnit unit) {
        WeatherDTO dto = new WeatherDTO();
        dto.setCityName(data.getCityName());
        dto.setTemperature(convertTemperature(data.getTemperature(), unit));
        dto.setApparentTemperature(convertTemperature(data.getApparentTemperature(), unit));
        dto.setHumidity(data.getHumidity());
        dto.setWindSpeed(data.getWindSpeed());
        dto.setPrecipitation(data.getPrecipitation());
        dto.setDescription(data.getDescription());
        dto.setTemperatureUnit(unit.name());
        return dto;
    }

    /**
     * Convertir temperatura entre unidades
     */
    private double convertTemperature(double celsius, TemperatureUnit targetUnit) {
        switch (targetUnit) {
            case FAHRENHEIT:
                return (celsius * 9.0 / 5.0) + 32.0;
            case KELVIN:
                return celsius + 273.15;
            default:
                return celsius;
        }
    }

    /**
     * Parsear unidad de temperatura
     */
    private TemperatureUnit parseTemperatureUnit(String unit) {
        try {
            return TemperatureUnit.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TemperatureUnit.CELSIUS;
        }
    }

    /**
     * Crear respuesta de error
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    /* ==================== CLASES DTO ==================== */

    /**
     * DTO para datos climáticos actuales
     */
    public static class WeatherDTO {
        private String cityName;
        private double temperature;
        private double apparentTemperature;
        private int humidity;
        private double windSpeed;
        private double precipitation;
        private String description;
        private String temperatureUnit;

        // Getters y Setters
        public String getCityName() { return cityName; }
        public void setCityName(String cityName) { this.cityName = cityName; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }

        public double getApparentTemperature() { return apparentTemperature; }
        public void setApparentTemperature(double apparentTemperature) { this.apparentTemperature = apparentTemperature; }

        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }

        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

        public double getPrecipitation() { return precipitation; }
        public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTemperatureUnit() { return temperatureUnit; }
        public void setTemperatureUnit(String temperatureUnit) { this.temperatureUnit = temperatureUnit; }
    }

    /**
     * DTO para pronóstico de 5 días
     */
    public static class ForecastDTO {
        private String cityName;
        private String weatherUnit;
        private List<DayForecastDTO> days;

        // Getters y Setters
        public String getCityName() { return cityName; }
        public void setCityName(String cityName) { this.cityName = cityName; }

        public String getWeatherUnit() { return weatherUnit; }
        public void setWeatherUnit(String weatherUnit) { this.weatherUnit = weatherUnit; }

        public List<DayForecastDTO> getDays() { return days; }
        public void setDays(List<DayForecastDTO> days) { this.days = days; }
    }

    /**
     * DTO para pronóstico diario
     */
    public static class DayForecastDTO {
        private String date;
        private double maxTemp;
        private double minTemp;
        private int humidity;
        private String description;

        // Getters y Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public double getMaxTemp() { return maxTemp; }
        public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }

        public double getMinTemp() { return minTemp; }
        public void setMinTemp(double minTemp) { this.minTemp = minTemp; }

        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
