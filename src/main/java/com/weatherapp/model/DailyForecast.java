package com.weatherapp.model;

import com.weatherapp.config.TemperatureUnit;
import java.time.LocalDate;

/**
 * Representa los datos climáticos para un día específico en el pronóstico.
 * Incluye temperaturas máxima y mínima, descripción y condiciones.
 */
public class DailyForecast {
    private LocalDate date;
    private double maxTemperature;
    private double minTemperature;
    private double avgTemperature;
    private String description;
    private int precipitationProbability;  // Porcentaje (0-100)
    private double precipitationSum;        // Milímetros
    private int humidity;                   // Porcentaje (0-100)
    private TemperatureUnit temperatureUnit;

    /**
     * Constructor completo para crear un pronóstico diario.
     * 
     * @param date Fecha del pronóstico
     * @param maxTemperature Temperatura máxima del día
     * @param minTemperature Temperatura mínima del día
     * @param avgTemperature Temperatura promedio del día
     * @param description Descripción del clima (ej: "Parcialmente nublado")
     * @param precipitationProbability Probabilidad de precipitación (0-100)
     * @param precipitationSum Cantidad de precipitación (mm)
     * @param humidity Humedad relativa (0-100)
     * @param temperatureUnit Unidad de temperatura (C, F, K)
     */
    public DailyForecast(LocalDate date, double maxTemperature, double minTemperature,
                        double avgTemperature, String description, int precipitationProbability,
                        double precipitationSum, int humidity, TemperatureUnit temperatureUnit) {
        this.date = date;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.avgTemperature = avgTemperature;
        this.description = description;
        this.precipitationProbability = precipitationProbability;
        this.precipitationSum = precipitationSum;
        this.humidity = humidity;
        this.temperatureUnit = temperatureUnit;
    }

    /**
     * Constructor vacío para compatibilidad con Gson.
     */
    public DailyForecast() {
    }

    // ==================== GETTERS ====================

    public LocalDate getDate() {
        return date;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getAvgTemperature() {
        return avgTemperature;
    }

    public String getDescription() {
        return description;
    }

    public int getPrecipitationProbability() {
        return precipitationProbability;
    }

    public double getPrecipitationSum() {
        return precipitationSum;
    }

    public int getHumidity() {
        return humidity;
    }

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public String getTemperatureSymbol() {
        return temperatureUnit != null ? temperatureUnit.getSymbol() : "°C";
    }

    // ==================== SETTERS ====================

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public void setAvgTemperature(double avgTemperature) {
        this.avgTemperature = avgTemperature;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrecipitationProbability(int precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public void setPrecipitationSum(double precipitationSum) {
        this.precipitationSum = precipitationSum;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Retorna una representación textual compacta del día.
     * Formato: "Lun, 25 Mar | 15-20°C | Soleado"
     */
    @Override
    public String toString() {
        String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3);
        return String.format("%s, %d %s | %.0f-%.0f%s | %s",
                dayOfWeek,
                date.getDayOfMonth(),
                date.getMonth().toString().substring(0, 3),
                minTemperature,
                maxTemperature,
                getTemperatureSymbol(),
                description);
    }

    /**
     * Retorna una representación detallada del pronóstico para ese día.
     */
    public String getDetailedInfo() {
        return String.format(
                "📅 %s\n" +
                "🌡️  Máx: %.1f%s | Mín: %.1f%s | Promedio: %.1f%s\n" +
                "☁️  %s\n" +
                "💧 Humedad: %d%% | Precipitación: %d%% (%.1f mm)\n",
                date,
                maxTemperature, getTemperatureSymbol(),
                minTemperature, getTemperatureSymbol(),
                avgTemperature, getTemperatureSymbol(),
                description,
                humidity,
                precipitationProbability,
                precipitationSum
        );
    }
}
