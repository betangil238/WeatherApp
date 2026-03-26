package com.weatherapp.exception;

/**
 * Excepción lanzada cuando ocurren errores de red o conectividad.
 * Es recuperable mediante reintentos.
 */
public class NetworkException extends WeatherAppException {
    
    private final int attemptsMade;
    private final int maxAttempts;

    /**
     * Constructor con información de reintentos.
     *
     * @param message     Mensaje de error
     * @param attemptsMade Intentos realizados
     * @param maxAttempts  Máximo de intentos permitidos
     * @param cause       Excepción que lo causó
     */
    public NetworkException(String message, int attemptsMade, int maxAttempts, Throwable cause) {
        super(message, cause);
        this.attemptsMade = attemptsMade;
        this.maxAttempts = maxAttempts;
    }

    /**
     * Constructor simplificado.
     *
     * @param message Mensaje de error
     * @param cause   Excepción que lo causó
     */
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
        this.attemptsMade = 0;
        this.maxAttempts = 0;
    }

    public int getAttemptsMade() {
        return attemptsMade;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public boolean canRetry() {
        return attemptsMade < maxAttempts;
    }
}
