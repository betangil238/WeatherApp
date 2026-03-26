package com.weatherapp.exception;

/**
 * Excepción lanzada cuando los datos climáticos obtenidos no cumplen validaciones
 * (temperaturas irreales, humedad fuera de rango, coordenadas inválidas, etc.).
 */
public class WeatherDataException extends WeatherAppException {
    
    private final String fieldName;
    private final Object invalidValue;

    /**
     * Constructor con nombre del campo y valor inválido.
     *
     * @param fieldName    Nombre del campo que falló la validación
     * @param invalidValue Valor inválido
     * @param message      Mensaje de error
     */
    public WeatherDataException(String fieldName, Object invalidValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    /**
     * Constructor simplificado.
     *
     * @param message Mensaje de error
     */
    public WeatherDataException(String message) {
        super(message);
        this.fieldName = null;
        this.invalidValue = null;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}
