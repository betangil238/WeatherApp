package com.weatherapp.config;

/**
 * Enumeración de unidades de temperatura soportadas.
 * Incluye Celsius, Fahrenheit y Kelvin.
 */
public enum TemperatureUnit {
    CELSIUS("Celsius", "°C", "celsius"),
    FAHRENHEIT("Fahrenheit", "°F", "fahrenheit"),
    KELVIN("Kelvin", "K", "kelvin");

    private final String name;
    private final String symbol;
    private final String apiParameter;

    /**
     * Constructor del enum.
     *
     * @param name          Nombre legible de la unidad
     * @param symbol        Símbolo de la unidad
     * @param apiParameter  Parámetro usado en la API de Open-Meteo
     */
    TemperatureUnit(String name, String symbol, String apiParameter) {
        this.name = name;
        this.symbol = symbol;
        this.apiParameter = apiParameter;
    }

    /**
     * Retorna el nombre de la unidad.
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna el símbolo de la unidad.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Retorna el parámetro usado en la API de Open-Meteo.
     */
    public String getApiParameter() {
        return apiParameter;
    }

    /**
     * Convierte un índice (1, 2, 3) a TemperatureUnit.
     * Utilizado para entrada del usuario en consola.
     *
     * @param choice Opción del usuario (1=Celsius, 2=Fahrenheit, 3=Kelvin)
     * @return TemperatureUnit correspondiente o CELSIUS por defecto
     */
    public static TemperatureUnit fromChoice(int choice) {
        switch (choice) {
            case 1:
                return CELSIUS;
            case 2:
                return FAHRENHEIT;
            case 3:
                return KELVIN;
            default:
                return CELSIUS;
        }
    }

    /**
     * Convierte un String a TemperatureUnit.
     * Utilizado para cargar desde archivo de configuración.
     *
     * @param unitName Nombre de la unidad (case-insensitive)
     * @return TemperatureUnit correspondiente o CELSIUS por defecto
     */
    public static TemperatureUnit fromString(String unitName) {
        if (unitName == null) {
            return CELSIUS;
        }
        String upper = unitName.toUpperCase();
        switch (upper) {
            case "CELSIUS":
            case "C":
                return CELSIUS;
            case "FAHRENHEIT":
            case "F":
                return FAHRENHEIT;
            case "KELVIN":
            case "K":
                return KELVIN;
            default:
                return CELSIUS;
        }
    }
}
