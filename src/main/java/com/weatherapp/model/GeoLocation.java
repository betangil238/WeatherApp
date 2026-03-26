package com.weatherapp.model;

/**
 * Representa una ubicación geográfica con coordenadas y nombre de ciudad.
 * Utilizada para almacenar resultados de consultas de geocodificación.
 */
public class GeoLocation {
    private double latitude;
    private double longitude;
    private String cityName;

    /**
     * Constructor con todos los campos.
     *
     * @param latitude  Latitud de la ubicación
     * @param longitude Longitud de la ubicación
     * @param cityName  Nombre de la ciudad
     */
    public GeoLocation(double latitude, double longitude, String cityName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.cityName = cityName;
    }

    /**
     * Constructor vacío para compatibilidad con Gson.
     */
    public GeoLocation() {
    }

    // Getters
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCityName() {
        return cityName;
    }

    // Setters
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public String toString() {
        return String.format("GeoLocation{ciudad='%s', latitud=%.4f, longitud=%.4f}", 
                             cityName, latitude, longitude);
    }
}
