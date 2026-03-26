package com.weatherapp.model;

import com.weatherapp.config.TemperatureUnit;

/**
 * Representa los datos climáticos de una ubicación.
 * Incluye temperatura, sensación térmica, humedad, velocidad del viento, precipitación y descripción del clima.
 */
public class WeatherData {
    private String cityName;
    private double temperature;
    private double apparentTemperature;
    private int humidity;
    private double windSpeed;              // Velocidad del viento en km/h
    private double precipitation;          // Precipitación en mm
    private String description;
    private TemperatureUnit temperatureUnit;

    /**
     * Constructor con todos los campos.
     *
     * @param cityName              Nombre de la ciudad
     * @param temperature           Temperatura actual
     * @param apparentTemperature   Sensación térmica
     * @param humidity              Porcentaje de humedad (0-100)
     * @param windSpeed             Velocidad del viento en km/h
     * @param precipitation         Precipitación en mm
     * @param description           Descripción del clima (ej: "Soleado", "Nublado")
     * @param temperatureUnit       Unidad de temperatura (C, F, K)
     */
    public WeatherData(String cityName, double temperature, double apparentTemperature,
                       int humidity, double windSpeed, double precipitation,
                       String description, TemperatureUnit temperatureUnit) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.apparentTemperature = apparentTemperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.precipitation = precipitation;
        this.description = description;
        this.temperatureUnit = temperatureUnit;
    }

    /**
     * Constructor vacío para compatibilidad con Gson.
     */
    public WeatherData() {
    }

    // Getters
    public String getCityName() {
        return cityName;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getApparentTemperature() {
        return apparentTemperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public String getDescription() {
        return description;
    }

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    /**
     * Retorna el símbolo de la unidad de temperatura.
     *
     * @return símbolo de la unidad (°C, °F, K)
     */
    public String getTemperatureSymbol() {
        return temperatureUnit != null ? temperatureUnit.getSymbol() : "°C";
    }

    // Setters
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setApparentTemperature(double apparentTemperature) {
        this.apparentTemperature = apparentTemperature;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    @Override
    public String toString() {
        return String.format("WeatherData{ciudad='%s', temp=%.1f%s, sensación=%.1f%s, humedad=%d%%, viento=%.1f km/h, precipitación=%.1f mm, clima='%s'}",
                             cityName, temperature, getTemperatureSymbol(), apparentTemperature,
                             getTemperatureSymbol(), humidity, windSpeed, precipitation, description);
    }
}
