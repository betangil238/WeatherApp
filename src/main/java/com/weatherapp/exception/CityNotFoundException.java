package com.weatherapp.exception;

/**
 * Excepción lanzada cuando una ciudad no puede ser resuelta.
 * No es recuperable mediante reintentos de red.
 */
public class CityNotFoundException extends WeatherAppException {
    
    private final String cityName;

    /**
     * Constructor con nombre de ciudad.
     *
     * @param cityName Nombre de la ciudad que no fue encontrada
     */
    public CityNotFoundException(String cityName) {
        super("🚫 Ciudad no encontrada: '" + cityName + "'");
        this.cityName = cityName;
    }

    /**
     * Constructor con nombre de ciudad y mensaje personalizado.
     *
     * @param cityName Nombre de la ciudad que no fue encontrada
     * @param message  Mensaje de error personalizado
     */
    public CityNotFoundException(String cityName, String message) {
        super(message);
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }
}
