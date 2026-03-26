package com.weatherapp.presentation;

import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.model.WeatherData;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Capa de presentación - Interfaz de consola para la aplicación.
 * Maneja:
 * 1. Menú de selección de unidad de temperatura
 * 2. Entrada de ciudad del usuario
 * 3. Visualización formateada de datos climáticos
 * 4. Mensajes de error amigables
 * 5. Menú de continuar/salir
 */
public class ConsoleUI {

    private final Scanner scanner;

    /**
     * Constructor inicializa el Scanner para entrada de usuario.
     */
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Muestra el mensaje de bienvenida con banner.
     */
    public void showWelcomeMessage() {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                           ║");
        System.out.println("║        🌍 APLICACIÓN DE CLIMA EN TIEMPO REAL 🌦️          ║");
        System.out.println("║                                                           ║");
        System.out.println("║              Powered by Open-Meteo API                   ║");
        System.out.println("║                                                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Muestra menú de selección de unidad de temperatura.
     * Retorna la unidad seleccionada por el usuario.
     *
     * @return TemperatureUnit seleccionada
     */
    public TemperatureUnit showTemperatureUnitMenu() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Selecciona la unidad de temperatura para mostrar:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  1️⃣  Celsius (°C)       - Recomendado para la mayoría");
        System.out.println("  2️⃣  Fahrenheit (°F)   - Usado en EE.UU. y otros países");
        System.out.println("  3️⃣  Kelvin (K)        - Escala absoluta (científica)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.print("Ingresa tu opción (1-3) [Defecto: 1]: ");

        String input = scanner.nextLine().trim();

        int choice;
        try {
            choice = input.isEmpty() ? 1 : Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("⚠️  Opción inválida. Usando Celsius por defecto.");
            choice = 1;
        }

        TemperatureUnit selected = TemperatureUnit.fromChoice(choice);
        System.out.println("✅ Unidad seleccionada: " + selected.getName() + " (" + selected.getSymbol() + ")");
        System.out.println();
        return selected;
    }

    /**
     * Solicita al usuario que ingrese un nombre de ciudad.
     * Valida que no esté vacío con reintentos limitados.
     * Evita recursión infinita mediante un límite de intentos.
     *
     * @return Nombre de ciudad ingresado (trimmed)
     * @throws IllegalArgumentException si se excede el número máximo de intentos
     */
    public String getUserCity() {
        final int maxRetries = 3;
        int attempts = 0;
        
        while (attempts < maxRetries) {
            System.out.print("🔍 Ingresa el nombre de la ciudad (ej: Madrid, Nueva York): ");
            String cityName = scanner.nextLine().trim();
            
            if (!cityName.isEmpty()) {
                return cityName;
            }
            
            attempts++;
            if (attempts < maxRetries) {
                System.out.println("⚠️  Por favor ingresa un nombre de ciudad válido. (" + 
                                 (maxRetries - attempts) + " intentos restantes)");
            }
        }
        
        throw new IllegalArgumentException("Máximo número de intentos excedido para ingreso de ciudad");
    }

    /**
     * Muestra los datos climáticos formateados con emojis y colores ANSI.
     *
     * @param weatherData Datos climáticos a mostrar
     */
    public void displayWeather(WeatherData weatherData) {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║           🌍 INFORMACIÓN DEL CLIMA                        ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");

        // Ciudad
        System.out.printf("║ 📍 Ciudad: %-47s ║%n", weatherData.getCityName());

        // Temperatura y Sensación Térmica
        String tempStr = String.format("%.1f%s", weatherData.getTemperature(),
                                       weatherData.getTemperatureSymbol());
        String appTempStr = String.format("%.1f%s", weatherData.getApparentTemperature(),
                                          weatherData.getTemperatureSymbol());

        System.out.printf("║ 🌡️  Temperatura:        %-35s ║%n", tempStr);
        System.out.printf("║ 🤔 Sensación térmica:   %-35s ║%n", appTempStr);

        // Humedad
        System.out.printf("║ 💧 Humedad:             %-35d%% ║%n", weatherData.getHumidity());

        // Velocidad del viento
        System.out.printf("║ 💨 Viento:              %-35.1f km/h ║%n", weatherData.getWindSpeed());

        // Precipitación
        System.out.printf("║ 🌧️  Precipitación:      %-35.1f mm ║%n", weatherData.getPrecipitation());

        // Descripción
        System.out.printf("║ ☁️  Descripción:        %-35s ║%n", weatherData.getDescription());

        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Muestra mensaje de error formateado.
     *
     * @param message Mensaje de error a mostrar
     */
    public void showError(String message) {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                     ❌ ERROR                             ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.printf("║ %s%-59s ║%n", message, " ".repeat(Math.max(0, 59 - message.length())));
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Pregunta al usuario si desea continuar con otra búsqueda.
     *
     * @return true si desea continuar, false si desea salir
     */
    public boolean askContinue() {
        System.out.print("¿Deseas consultar otra ciudad? (S/N): ");
        String input = scanner.nextLine().trim().toUpperCase();

        if (input.isEmpty() || input.equalsIgnoreCase("S") || input.equalsIgnoreCase("Si")) {
            System.out.println();
            return true;
        }

        return false;
    }

    /**
     * Pregunta al usuario si desea reintentar después de un error de red.
     *
     * @return true si desea reintentar con otra ciudad, false si desea salir
     */
    public boolean askForRetry() {
        System.out.print("¿Deseas intentar con otra ciudad? (S/N): ");
        String input = scanner.nextLine().trim();
        
        return input.isEmpty() || input.equalsIgnoreCase("S") || input.equalsIgnoreCase("Si");
    }

    /**
     * Muestra mensaje de despedida y cierra el Scanner.
     */
    public void showGoodbyeMessage() {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                           ║");
        System.out.println("║     👋 ¡Gracias por usar Weather App! ¡Hasta pronto! 👋  ║");
        System.out.println("║                                                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Cierra el Scanner de entrada.
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Solicita al usuario que ingrese múltiples nombres de ciudades.
     * Valida que al menos se ingrese una ciudad.
     * El usuario puede ingresar ciudades separadas por comas, una por línea,
     * o escribir "listo" para terminar.
     *
     * @return Lista de nombres de ciudades ingresadas
     * @throws IllegalArgumentException si no se ingresa ninguna ciudad válida
     */
    public List<String> getMultipleCities() {
        List<String> cities = new ArrayList<>();

        System.out.println();
        System.out.println("📍 INGRESA MÚLTIPLES CIUDADES PARA COMPARATIVO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Opciones:");
        System.out.println("  • Ingresa ciudades separadas por comas: Madrid, Barcelona, Valencia");
        System.out.println("  • O una ciudad por línea, escribe 'listo' cuando termines");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        System.out.print("🔍 Ingresa las ciudades: ");
        String input = scanner.nextLine().trim();

        // Verificar si el usuario ingresó ciudades separadas por comas
        if (input.contains(",")) {
            String[] citiesArray = input.split(",");
            for (String city : citiesArray) {
                String trimmedCity = city.trim();
                if (!trimmedCity.isEmpty()) {
                    cities.add(trimmedCity);
                }
            }
        } else if (!input.isEmpty() && !input.equalsIgnoreCase("listo")) {
            // Primera ciudad ingresada
            cities.add(input);

            // Pedir más ciudades hasta que el usuario escriba "listo"
            System.out.println("  (Escribe 'listo' para terminar, o ingresa más ciudades)");
            while (true) {
                System.out.print("  Ingresa otra ciudad (o 'listo'): ");
                String nextCity = scanner.nextLine().trim();

                if (nextCity.equalsIgnoreCase("listo")) {
                    break;
                }

                if (!nextCity.isEmpty()) {
                    cities.add(nextCity);
                }
            }
        }

        if (cities.isEmpty()) {
            throw new IllegalArgumentException("❌ Debes ingresar al menos una ciudad");
        }

        System.out.println("✅ Se buscarán datos para: " + String.join(", ", cities));
        System.out.println();

        return cities;
    }

    /**
     * Muestra los datos climáticos de múltiples ciudades en un formato comparativo de tabla.
     * Presenta temperatura, humedad, viento y precipitación para cada ciudad
     * en una tabla formateada.
     *
     * @param weatherDataList Lista de WeatherData para mostrar en comparativo
     */
    public void displayWeatherComparative(List<WeatherData> weatherDataList) {
        if (weatherDataList == null || weatherDataList.isEmpty()) {
            showError("No hay datos climáticos para mostrar");
            return;
        }

        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                           🌍 COMPARATIVO DE CLIMA - MÚLTIPLES CIUDADES                                                    ║");
        System.out.println("╠══════════════════════════════════════╦═════════════════════════╦═════════════════╦═════════════════╦═══════════════════╣");
        System.out.println("║      📍 CIUDAD                       ║  🌡️  TEMPERATURA       ║  💧 HUMEDAD     ║  💨 VIENTO      ║  🌧️  PRECIP.     ║");
        System.out.println("╠══════════════════════════════════════╬═════════════════════════╬═════════════════╬═════════════════╬═══════════════════╣");

        for (WeatherData weather : weatherDataList) {
            String cityName = String.format("%-36s", weather.getCityName());
            String temp = String.format("%-23s", 
                String.format("%.1f%s", weather.getTemperature(), weather.getTemperatureSymbol()));
            String humidity = String.format("%-15s", weather.getHumidity() + "%");
            String wind = String.format("%-15s", String.format("%.1f km/h", weather.getWindSpeed()));
            String precip = String.format("%-17s", String.format("%.1f mm", weather.getPrecipitation()));

            System.out.printf("║ %s║ %s║ %s║ %s║ %s║%n",
                    cityName, temp, humidity, wind, precip);
        }

        System.out.println("╚══════════════════════════════════════╩═════════════════════════╩═════════════════╩═════════════════╩═══════════════════╝");
        System.out.println();

        // Mostrar análisis adicional
        displayComparativeAnalysis(weatherDataList);
    }

    /**
     * Muestra un análisis adicional del comparativo de ciudades:
     * - Ciudad más caliente, más fría
     * - Ciudad con mayor y menor humedad
     * - Ciudad con mayor velocidad de viento y más precipitación
     *
     * @param weatherDataList Lista de WeatherData para analizar
     */
    private void displayComparativeAnalysis(List<WeatherData> weatherDataList) {
        if (weatherDataList.size() < 2) {
            return;
        }

        // Encontrar ciudad más caliente
        WeatherData hottestCity = weatherDataList.stream()
                .max((w1, w2) -> Double.compare(w1.getTemperature(), w2.getTemperature()))
                .orElse(null);

        // Encontrar ciudad más fría
        WeatherData coldestCity = weatherDataList.stream()
                .min((w1, w2) -> Double.compare(w1.getTemperature(), w2.getTemperature()))
                .orElse(null);

        // Encontrar ciudad con mayor humedad
        WeatherData humidestCity = weatherDataList.stream()
                .max((w1, w2) -> Integer.compare(w1.getHumidity(), w2.getHumidity()))
                .orElse(null);

        // Encontrar ciudad con menor humedad
        WeatherData driestCity = weatherDataList.stream()
                .min((w1, w2) -> Integer.compare(w1.getHumidity(), w2.getHumidity()))
                .orElse(null);

        // Encontrar ciudad con mayor velocidad de viento
        WeatherData windyCity = weatherDataList.stream()
                .max((w1, w2) -> Double.compare(w1.getWindSpeed(), w2.getWindSpeed()))
                .orElse(null);

        // Encontrar ciudad con mayor precipitación
        WeatherData rainestCity = weatherDataList.stream()
                .max((w1, w2) -> Double.compare(w1.getPrecipitation(), w2.getPrecipitation()))
                .orElse(null);

        System.out.println("📊 ANÁLISIS COMPARATIVO:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (hottestCity != null) {
            System.out.printf("  🔥 Más caliente: %s (%.1f%s)%n",
                    hottestCity.getCityName(),
                    hottestCity.getTemperature(),
                    hottestCity.getTemperatureSymbol());
        }

        if (coldestCity != null) {
            System.out.printf("  ❄️  Más fría: %s (%.1f%s)%n",
                    coldestCity.getCityName(),
                    coldestCity.getTemperature(),
                    coldestCity.getTemperatureSymbol());
        }

        if (humidestCity != null) {
            System.out.printf("  💦 Mayor humedad: %s (%d%%)%n",
                    humidestCity.getCityName(),
                    humidestCity.getHumidity());
        }

        if (driestCity != null) {
            System.out.printf("  🌵 Menor humedad: %s (%d%%)%n",
                    driestCity.getCityName(),
                    driestCity.getHumidity());
        }

        if (windyCity != null) {
            System.out.printf("  💨 Mayor viento: %s (%.1f km/h)%n",
                    windyCity.getCityName(),
                    windyCity.getWindSpeed());
        }

        if (rainestCity != null && rainestCity.getPrecipitation() > 0) {
            System.out.printf("  🌧️  Mayor precipitación: %s (%.1f mm)%n",
                    rainestCity.getCityName(),
                    rainestCity.getPrecipitation());
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
    }

    /**
     * Muestra menú para elegir entre ver clima de una ciudad o múltiples ciudades.
     *
     * @return 1 para una ciudad, 2 para múltiples ciudades
     */
    public int showMainMenu() {
        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("¿QUÉ DESEAS HACER?");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  1️⃣  Ver clima de una ciudad");
        System.out.println("  2️⃣  Comparar clima de múltiples ciudades");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.print("Selecciona una opción (1-2): ");

        String input = scanner.nextLine().trim();

        try {
            int choice = Integer.parseInt(input);
            if (choice == 1 || choice == 2) {
                return choice;
            }
        } catch (NumberFormatException e) {
            // Ignorar parsing error
        }

        System.out.println("⚠️  Opción inválida. Usando opción 1 (una ciudad).");
        return 1;
    }
}
