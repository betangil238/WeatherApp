package com.weatherapp.model;

import com.weatherapp.config.TemperatureUnit;
import java.time.LocalDate;
import java.util.List;

/**
 * Contiene el pronóstico del tiempo para los próximos 5 días de una ubicación.
 * Incluye datos actuales y pronóstico diario.
 */
public class ForecastData {
    private String cityName;
    private double latitude;
    private double longitude;
    private WeatherData currentWeather;  // Datos actuales
    private List<DailyForecast> dailyForecasts;  // Pronóstico de 5 días
    private TemperatureUnit temperatureUnit;

    /**
     * Constructor completo.
     * 
     * @param cityName Nombre de la ciudad
     * @param latitude Latitud geográfica
     * @param longitude Longitud geográfica
     * @param currentWeather Datos climáticos actuales
     * @param dailyForecasts Lista de pronósticos diarios (hasta 5 días)
     * @param temperatureUnit Unidad de temperatura
     */
    public ForecastData(String cityName, double latitude, double longitude,
                       WeatherData currentWeather, List<DailyForecast> dailyForecasts,
                       TemperatureUnit temperatureUnit) {
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentWeather = currentWeather;
        this.dailyForecasts = dailyForecasts;
        this.temperatureUnit = temperatureUnit;
    }

    /**
     * Constructor vacío para compatibilidad con serializadores.
     */
    public ForecastData() {
    }

    // ==================== GETTERS ====================

    public String getCityName() {
        return cityName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public WeatherData getCurrentWeather() {
        return currentWeather;
    }

    public List<DailyForecast> getDailyForecasts() {
        return dailyForecasts;
    }

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public int getForecastDays() {
        return dailyForecasts != null ? dailyForecasts.size() : 0;
    }

    // ==================== SETTERS ====================

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCurrentWeather(WeatherData currentWeather) {
        this.currentWeather = currentWeather;
    }

    public void setDailyForecasts(List<DailyForecast> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Obtiene el pronóstico para un día específico.
     * 
     * @param dayIndex Índice del día (0 = hoy, 1 = mañana, etc.)
     * @return DailyForecast del día, o null si no existe
     */
    public DailyForecast getForecastForDay(int dayIndex) {
        if (dailyForecasts != null && dayIndex >= 0 && dayIndex < dailyForecasts.size()) {
            return dailyForecasts.get(dayIndex);
        }
        return null;
    }

    /**
     * Obtiene el pronóstico para una fecha específica.
     * 
     * @param date La fecha a buscar
     * @return DailyForecast para esa fecha, o null si no existe
     */
    public DailyForecast getForecastForDate(LocalDate date) {
        if (dailyForecasts == null) return null;
        
        return dailyForecasts.stream()
                .filter(f -> f.getDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    /**
     * Calcula la temperatura promedio para los próximos X días.
     * 
     * @param days Número de días a promediar
     * @return Temperatura promedio, o Double.NaN si no hay datos
     */
    public double getAverageTemperature(int days) {
        if (dailyForecasts == null || dailyForecasts.isEmpty()) {
            return Double.NaN;
        }
        
        return dailyForecasts.stream()
                .limit(days)
                .mapToDouble(DailyForecast::getAvgTemperature)
                .average()
                .orElse(Double.NaN);
    }

    /**
     * Encuentra el día con mayor probabilidad de precipitación.
     * 
     * @return DailyForecast con máxima precipitación, o null si no hay datos
     */
    public DailyForecast getDayWithHighestPrecipitation() {
        if (dailyForecasts == null || dailyForecasts.isEmpty()) {
            return null;
        }
        
        return dailyForecasts.stream()
                .max((f1, f2) -> Integer.compare(
                        f1.getPrecipitationProbability(),
                        f2.getPrecipitationProbability()))
                .orElse(null);
    }

    /**
     * Marca si se espera lluvia en los próximos días.
     * 
     * @param days Número de días a verificar
     * @return true si hay al menos 40% de probabilidad en algún día
     */
    public boolean willRain(int days) {
        if (dailyForecasts == null) return false;
        
        return dailyForecasts.stream()
                .limit(days)
                .anyMatch(f -> f.getPrecipitationProbability() >= 40);
    }

    /**
     * Encuentra el día más frío del pronóstico.
     * 
     * @return DailyForecast con temperatura mínima más baja
     */
    public DailyForecast getColdestDay() {
        if (dailyForecasts == null || dailyForecasts.isEmpty()) {
            return null;
        }
        
        return dailyForecasts.stream()
                .min((f1, f2) -> Double.compare(
                        f1.getMinTemperature(),
                        f2.getMinTemperature()))
                .orElse(null);
    }

    /**
     * Encuentra el día más caluroso del pronóstico.
     * 
     * @return DailyForecast con temperatura máxima más alta
     */
    public DailyForecast getHottestDay() {
        if (dailyForecasts == null || dailyForecasts.isEmpty()) {
            return null;
        }
        
        return dailyForecasts.stream()
                .max((f1, f2) -> Double.compare(
                        f1.getMaxTemperature(),
                        f2.getMaxTemperature()))
                .orElse(null);
    }

    /**
     * Retorna una representación textual compacta del pronóstico.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📍 %s (%.2f, %.2f)%n", cityName, latitude, longitude));
        sb.append(String.format("🕐 Ahora: %.1f%s | %s%n%n",
                currentWeather.getTemperature(),
                currentWeather.getTemperatureSymbol(),
                currentWeather.getDescription()));
        
        sb.append("📅 Pronóstico 5 días:\n");
        if (dailyForecasts != null) {
            dailyForecasts.forEach(day -> sb.append("   ").append(day).append("\n"));
        }
        
        return sb.toString();
    }

    /**
     * Retorna un resumen detallado del pronóstico para impresión.
     */
    public String getDetailedForecast() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format(
                "╔════════════════════════════════════════════════╗\n" +
                "║         PRONÓSTICO 5 DÍAS - %s\n" +
                "║         📍 Lat: %.4f | Lon: %.4f\n" +
                "╚════════════════════════════════════════════════╝\n\n",
                cityName, latitude, longitude));
        
        sb.append("🕐 CONDICIONES ACTUALES:\n");
        if (currentWeather != null) {
            sb.append(currentWeather.getDescription()).append("\n");
            sb.append(String.format("   🌡️  Temperatura: %.1f%s (Sensación: %.1f%s)%n",
                    currentWeather.getTemperature(),
                    currentWeather.getTemperatureSymbol(),
                    currentWeather.getApparentTemperature(),
                    currentWeather.getTemperatureSymbol()));
            sb.append(String.format("   💧 Humedad: %d%%%n%n",
                    currentWeather.getHumidity()));
        }
        
        sb.append("📅 PRONÓSTICO DIARIO:\n");
        sb.append("─".repeat(46)).append("\n");
        
        if (dailyForecasts != null) {
            for (DailyForecast day : dailyForecasts) {
                sb.append(day.getDetailedInfo());
            }
        }
        
        return sb.toString();
    }
}
