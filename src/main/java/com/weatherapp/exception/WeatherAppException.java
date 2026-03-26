package com.weatherapp.exception;

/**
 * Excepción base para todos los errores de la aplicación Weather App.
 * Proporciona una jerarquía clara de excepciones específicas del dominio.
 * Hereda de RuntimeException para que no sea necesario declararlas en throws.
 */
public abstract class WeatherAppException extends RuntimeException {
    
    /**
     * Constructor con mensaje de error.
     *
     * @param message Descripción del error
     */
    public WeatherAppException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa raíz.
     *
     * @param message Descripción del error
     * @param cause   Excepción que lo causó
     */
    public WeatherAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
