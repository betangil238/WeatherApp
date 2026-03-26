# Java Weather App - Comprehensive Code Review

**Review Date:** March 22, 2026  
**Scope:** All business logic files in `src/main/java/com/weatherapp/`

---

## Executive Summary

The weather app demonstrates good structural organization with clear separation of concerns across layers (presentation, business logic, data access). However, the codebase has several **critical** issues related to resource management, error handling, and testability that should be addressed before production use. The code also lacks flexibility for configuration and shows some design anti-patterns.

**Overall Quality Assessment:** **MEDIUM** - Functional but needs improvements in robustness and maintainability.

---

## Critical Issues (Must Fix)

### 1. **[CRITICAL] Resource Leak in Main.java**

**File:** [Main.java](Main.java#L80-L88)  
**Priority:** CRITICAL  
**Severity:** Memory/Resource leak

**Problem:**
In the RuntimeException catch block, a new `Scanner` is created but never closed, causing resource leaks:

```java
System.out.print("¿Deseas intentar con otra ciudad? (S/N): ");
String input = new java.util.Scanner(System.in).nextLine().trim();  // ❌ Scanner not closed
if (input.isEmpty() || input.equalsIgnoreCase("S") || input.equalsIgnoreCase("Si")) {
    continueRunning = true;
}
```

**Impact:**
- Memory leak: Each retry creates and abandons a Scanner
- Resource waste: Open file descriptors accumulate
- Inconsistency: Main Scanner from ConsoleUI is used elsewhere, but this one is separate

**Suggested Fix:**
```java
// Use the existing Scanner from ConsoleUI instead
boolean continueRunning = ui.askContinueCatchBlock();  // New method or reuse existing logic
```

OR use try-with-resources:
```java
try (Scanner tempScanner = new Scanner(System.in)) {
    String input = tempScanner.nextLine().trim();
    continueRunning = input.isEmpty() || input.equalsIgnoreCase("S") || input.equalsIgnoreCase("Si");
}
```

---

### 2. **[CRITICAL] Scanner Never Closed in ConsoleUI**

**File:** [ConsoleUI.java](ConsoleUI.java#L18)  
**Priority:** CRITICAL  
**Severity:** Resource leak

**Problem:**
The Scanner is created in the constructor but never closed in any execution path. The `close()` method exists but is never called from Main:

```java
public ConsoleUI() {
    this.scanner = new Scanner(System.in);  // ❌ Created but never properly cleaned up
}

public void close() {
    if (scanner != null) {
        scanner.close();  // ❌ Never called from Main
    }
}
```

**Impact:**
- Resource leak: Scanner wrapping System.in is never closed
- Operating system impact: Unclosed streams can accumulate across app restarts
- JVM warnings: May trigger warnings about unclosed resources

**Suggested Fix:**
In [Main.java](Main.java#L110):
```java
try {
    // ... application logic ...
} catch (Exception e) {
    // ...
} finally {
    if (ui != null) {
        ui.close();  // ✅ Already here but needs verification
    }
}
```

Verify the finally block is always executed by adding logging:
```java
finally {
    if (ui != null) {
        System.out.println(TAG + " Closing UI resources...");
        ui.close();
    }
}
```

---

### 3. **[CRITICAL] Stack Overflow Risk in ConsoleUI.getUserCity()**

**File:** [ConsoleUI.java](ConsoleUI.java#L106-L115)  
**Priority:** CRITICAL  
**Severity:** Stack overflow on repeated invalid input

**Problem:**
Recursive calls without depth limit on invalid input:

```java
public String getUserCity() {
    System.out.print("🔍 Ingresa el nombre de la ciudad (ej: Madrid, Nueva York): ");
    String cityName = scanner.nextLine().trim();

    if (cityName.isEmpty()) {
        System.out.println("⚠️  Por favor ingresa un nombre de ciudad válido.");
        return getUserCity();  // ❌ Recursive call without limit
    }
    return cityName;
}
```

**Impact:**
- If user repeatedly enters empty strings, call stack will overflow
- Stack overflow exception crashes the app
- No user-friendly error recovery

**Suggested Fix:**
```java
public String getUserCity() {
    int maxRetries = 3;
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
```

---

## High-Priority Issues

### 4. **[HIGH] Inconsistent Exception Handling and Mixing Error Types**

**File:** [Main.java](Main.java#L66-L100)  
**Priority:** HIGH  
**Severity:** Logic bugs, difficult error recovery

**Problem:**
The main loop catches `IllegalArgumentException` and `RuntimeException` but mixes validation errors with network errors:

```java
catch (IllegalArgumentException e) {
    // Expected for validation errors, but could also be thrown during parsing
    ui.showError("[VALIDACION] " + e.getMessage());
} catch (RuntimeException e) {
    // Could be network, timeout, parsing error, or unexpected bug
    String errorMsg = e.getMessage() != null ? e.getMessage() : "Error desconocido";
    ui.showError("[RED] " + errorMsg);
    // Custom retry logic follows
}
```

**Impact:**
- Unable to distinguish between validation errors (city not found) and transient network errors
- Different error types should have different recovery strategies
- User might retry when they shouldn't (invalid city) or give up when they should retry (network timeout)
- Parsing errors are treated as network errors

**Suggested Fix:**
Create domain-specific exceptions:
```java
public class CityNotFoundException extends IllegalArgumentException { }
public class GeolocationException extends RuntimeException { }
public class WeatherDataException extends RuntimeException { }
public class NetworkException extends RuntimeException { }
```

Then in Main:
```java
try {
    // ...
} catch (CityNotFoundException e) {
    ui.showError("[CITY_NOT_FOUND] " + e.getMessage());
    // Don't ask to retry network - just continue loop
} catch (GeolocationException | WeatherDataException e) {
    ui.showError("[DATA_ERROR] " + e.getMessage());
    // Could be validation or parsing
} catch (NetworkException e) {
    ui.showError("[NETWORK_ERROR] " + e.getMessage());
    // Ask if they want to retry
}
```

---

### 5. **[HIGH] Duplicate Validation Logic and Temperature Conversion**

**File:** [OpenMeteoClient.java](OpenMeteoClient.java#L250-L300)  
**Priority:** HIGH  
**Severity:** Code duplication, maintenance burden

**Problem:**
Temperature validation is duplicated for regular and apparent temperature, with identical conversion logic:

```java
// ❌ FIRST VALIDATION (lines 265-280)
double tempCelsius = weatherData.getTemperature();
if (weatherData.getTemperatureUnit() == TemperatureUnit.FAHRENHEIT) {
    tempCelsius = (weatherData.getTemperature() - 32) * 5 / 9;
} else if (weatherData.getTemperatureUnit() == TemperatureUnit.KELVIN) {
    tempCelsius = weatherData.getTemperature() - 273.15;
}
if (tempCelsius < AppConfig.TEMPERATURE_MIN_CELSIUS || 
    tempCelsius > AppConfig.TEMPERATURE_MAX_CELSIUS) {
    throw new IllegalArgumentException(...);
}

// ❌ SECOND VALIDATION - IDENTICAL PATTERN (lines 282-297)
double apparentTempCelsius = weatherData.getApparentTemperature();
if (weatherData.getTemperatureUnit() == TemperatureUnit.FAHRENHEIT) {
    apparentTempCelsius = (weatherData.getApparentTemperature() - 32) * 5 / 9;
} else if (weatherData.getTemperatureUnit() == TemperatureUnit.KELVIN) {
    apparentTempCelsius = weatherData.getApparentTemperature() - 273.15;
}
// ... identical validation ...
```

**Impact:**
- Violation of DRY (Don't Repeat Yourself) principle
- Harder to maintain: changes needed in multiple places
- Increased bug risk: duplicate logic diverge during modifications
- Temperature conversion should be in a utility or TemperatureUnit enum

**Suggested Fix:**
Create a utility method:
```java
private double convertToCelsius(double temperature, TemperatureUnit unit) {
    if (unit == TemperatureUnit.FAHRENHEIT) {
        return (temperature - 32) * 5 / 9;
    } else if (unit == TemperatureUnit.KELVIN) {
        return temperature - 273.15;
    }
    return temperature; // Already Celsius
}

private void validateTemperatureWithName(WeatherData weatherData, 
                                         String fieldName, 
                                         double tempValue) {
    double tempCelsius = convertToCelsius(tempValue, weatherData.getTemperatureUnit());
    
    if (tempCelsius < AppConfig.TEMPERATURE_MIN_CELSIUS || 
        tempCelsius > AppConfig.TEMPERATURE_MAX_CELSIUS) {
        throw new IllegalArgumentException(
            String.format("❌ %s irreal: %.1f %s (%.1f°C). Debe estar entre %.1f°C y %.1f°C",
                        fieldName,
                        tempValue,
                        weatherData.getTemperatureSymbol(),
                        tempCelsius,
                        AppConfig.TEMPERATURE_MIN_CELSIUS,
                        AppConfig.TEMPERATURE_MAX_CELSIUS));
    }
}

// Then in validateWeatherData:
validateTemperatureWithName(weatherData, "Temperatura", weatherData.getTemperature());
validateTemperatureWithName(weatherData, "Temperatura aparente", weatherData.getApparentTemperature());
```

---

### 6. **[HIGH] Logging Strategy Inconsistency**

**File:** All files  
**Priority:** HIGH  
**Severity:** Operational visibility, difficulty debugging

**Issues:**
- **OpenMeteoClient.java**: Uses `System.out` and `System.err` only
- **WeatherService.java**: Uses SLF4J Logger (correct approach)
- **Main.java, ConsoleUI.java, ConfigManager.java**: Mix of `System.out` and hardcoded TAG prefixes
- No centralized logging configuration

**Impact:**
- Cannot control log levels at runtime (e.g., production is verbose)
- Cannot filter logs by component
- Cannot easily capture logs to files
- Inconsistent format makes parsing difficult
- SLF4J warnings/errors might not appear with System.out logs

**Suggested Fix:**
Standardize on SLF4J across all components:

```java
// In OpenMeteoClient.java
private static final Logger LOGGER = LoggerFactory.getLogger(OpenMeteoClient.class);

// Replace all System.out calls:
// Old: System.out.println(TAG + " Resolviendo ciudad: " + cityName);
// New:
LOGGER.info("Resolviendo ciudad: {}", cityName);

// Old: System.out.println(TAG + " Intento " + attempt + ...);
// New:
LOGGER.debug("Intento {}/{}: GET {}", attempt, AppConfig.MAX_RETRIES, url);
```

Update `logback.xml` or `log4j2.properties` to configure levels and formats.

---

## Medium-Priority Issues

### 7. **[MEDIUM] OpenMeteoClient Has Too Many Responsibilities (Low Cohesion)**

**File:** [OpenMeteoClient.java](OpenMeteoClient.java)  
**Priority:** MEDIUM  
**Severity:** Violates Single Responsibility Principle

**Problem:**
The class handles:
1. HTTP communication and retry logic
2. JSON parsing
3. Data validation
4. Weather code mapping

This violates the Single Responsibility Principle.

**Impact:**
- Hard to test: need to mock HTTP, parsing, and validation
- Hard to replace: can't swap out just the HTTP transport
- Bloated class: too many reasons to change
- `parseWeatherCode()` should not be in HTTP client

**Suggested Fix:**
Create separate classes:

```java
// 1. HttpWeatherClient - only HTTP communication
public class HttpWeatherClient {
    public String getGeocoding(String cityName) { /* ... */ }
    public String getWeatherData(GeoLocation location, TemperatureUnit unit) { /* ... */ }
}

// 2. WeatherDataParser - handles JSON parsing
public class WeatherDataParser {
    public GeoLocation parseGeocoding(String cityName, String jsonResponse) { /* ... */ }
    public WeatherData parseWeatherData(String cityName, String jsonResponse, TemperatureUnit unit) { /* ... */ }
    public String mapWeatherCode(int code) { /* ... */ }
}

// 3. WeatherDataValidator - handles validation
public class WeatherDataValidator {
    public void validateGeoLocation(GeoLocation location) { /* ... */ }
    public void validateWeatherData(WeatherData data) { /* ... */ }
}

// 4. OpenMeteoClient - orchestrates (uses the above)
public class OpenMeteoClient {
    private final HttpWeatherClient httpClient;
    private final WeatherDataParser parser;
    private final WeatherDataValidator validator;
    
    public GeoLocation resolveCity(String cityName) {
        String response = httpClient.getGeocoding(cityName);
        GeoLocation location = parser.parseGeocoding(cityName, response);
        validator.validateGeoLocation(location);
        return location;
    }
}
```

---

### 8. **[MEDIUM] Large Switch Statement in parseWeatherCode()**

**File:** [OpenMeteoClient.java](OpenMeteoClient.java#L325-L375)  
**Priority:** MEDIUM  
**Severity:** Maintenance burden, hard to extend

**Problem:**
60+ line switch statement with 25 cases for weather code mapping:

```java
public String parseWeatherCode(int weatherCode) {
    switch (weatherCode) {
        case 0: return "☀️ Despejado";
        case 1:
        case 2: return "🌤️ Mayormente despejado";
        // ... 20+ more cases ...
        default: return "🌍 Desconocido (código: " + weatherCode + ")";
    }
}
```

**Impact:**
- Hard to find specific codes
- Difficult to add/modify descriptions
- Not testable without mocking the entire client
- Tight coupling to specific weather codes

**Suggested Fix:**
Use an enum-based approach:

```java
public enum WeatherCode {
    CLEAR_SKY(0, "☀️ Despejado"),
    MOSTLY_CLEAR(new int[]{1, 2}, "🌤️ Mayormente despejado"),
    PARTLY_CLOUDY(3, "☁️ Parcialmente nublado"),
    FOG(new int[]{45, 48}, "🌫️ Niebla"),
    // ... etc
    UNKNOWN(Integer.MAX_VALUE, "🌍 Desconocido");
    
    private final int[] codes;
    private final String description;
    
    WeatherCode(int code, String description) {
        this.codes = new int[]{code};
        this.description = description;
    }
    
    WeatherCode(int[] codes, String description) {
        this.codes = codes;
        this.description = description;
    }
    
    public static WeatherCode fromCode(int weatherCode) {
        for (WeatherCode code : WeatherCode.values()) {
            for (int c : code.codes) {
                if (c == weatherCode) return code;
            }
        }
        return UNKNOWN;
    }
    
    public String getDescription() {
        return description;
    }
}

// Usage:
public String parseWeatherCode(int weatherCode) {
    return WeatherCode.fromCode(weatherCode).getDescription();
}
```

---

### 9. **[MEDIUM] Model Classes Lack Immutability and Validation**

**File:** [GeoLocation.java](GeoLocation.java), [WeatherData.java](WeatherData.java)  
**Priority:** MEDIUM  
**Severity:** Potential data corruption, threading issues

**Problems:**

a) **Mutable fields with public setters:**
```java
public class GeoLocation {
    private double latitude;
    private double longitude;
    private String cityName;
    
    public void setLatitude(double latitude) { /* ❌ No validation */ }
    public void setLongitude(double longitude) { /* ❌ No validation */ }
    public void setCityName(String cityName) { /* ❌ No validation */ }
}
```

b) **No validation in constructors:**
```java
public GeoLocation(double latitude, double longitude, String cityName) {
    // ❌ No checks for:
    // - Invalid coordinates (-100 latitude should not be allowed)
    // - Null cityName
    this.latitude = latitude;
    this.longitude = longitude;
    this.cityName = cityName;
}
```

c) **Setters allow circumventing validation:**
```java
WeatherData data = new WeatherData(...); // Created with valid data
data.setHumidity(-50);                    // ❌ Now invalid but not caught until next validation
```

**Impact:**
- Data can become invalid after creation
- No guarantees about object state
- Difficult to debug: error happens far from the mutation
- Not thread-safe: concurrent modifications possible
- Violates immutability pattern used in modern Java

**Suggested Fix:**
Make models immutable:

```java
public final class GeoLocation {
    private final double latitude;
    private final double longitude;
    private final String cityName;
    
    public GeoLocation(double latitude, double longitude, String cityName) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude: " + longitude);
        }
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }
        
        this.latitude = latitude;
        this.longitude = longitude;
        this.cityName = cityName.trim();
    }
    
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getCityName() { return cityName; }
    
    // No setters!
}

// For updates, use builder pattern or copy-with constructor:
GeoLocation updated = new GeoLocation(
    newLatitude, 
    newLongitude, 
    geoLocation.getCityName()
);
```

---

### 10. **[MEDIUM] ConfigManager Lacks Validation and Error Recovery**

**File:** [ConfigManager.java](ConfigManager.java#L60-L70)  
**Priority:** MEDIUM  
**Severity:** Silent failures, no user feedback

**Problems:**

a) **No validation of saved values:**
```java
public void saveTemperatureUnit(TemperatureUnit unit) {
    // ❌ No check that unit != null
    properties.setProperty(AppConfig.CONFIG_PROPERTY_TEMPERATURE_UNIT, unit.name());
    saveConfiguration();
}
```

b) **Swallows IOException silently:**
```java
} catch (IOException e) {
    System.out.println(TAG + " Error saving configuration: " + e.getMessage());
    // ❌ Continues without telling caller that save failed
    // Application thinks config is saved but it's not
}
```

c) **No fallback when directory creation fails:**
```java
if (!configDir.mkdirs()) {
    System.out.println(TAG + " Failed to create config directory: " + AppConfig.CONFIG_DIR);
    return;  // ❌ Silently returns without saving
}
```

**Impact:**
- Configuration might not persist
- User thinks settings are saved but they're not
- Next run uses default values unexpectedly
- Difficult to diagnose: no exceptions thrown
- Race condition in multi-threaded environment

**Suggested Fix:**
```java
public void saveTemperatureUnit(TemperatureUnit unit) 
        throws ConfigurationException {
    if (unit == null) {
        throw new IllegalArgumentException("Temperature unit cannot be null");
    }
    properties.setProperty(AppConfig.CONFIG_PROPERTY_TEMPERATURE_UNIT, unit.name());
    saveConfiguration();  // Will throw ConfigurationException if fails
}

private void saveConfiguration() throws ConfigurationException {
    try {
        File configDir = new File(AppConfig.CONFIG_DIR);
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                throw new ConfigurationException(
                    "Failed to create config directory: " + AppConfig.CONFIG_DIR);
            }
        }
        
        try (FileOutputStream fos = new FileOutputStream(AppConfig.CONFIG_FILE)) {
            properties.store(fos, "Weather App Configuration - Do not edit manually");
        }
        
        LOGGER.info("Configuration saved to: {}", AppConfig.CONFIG_FILE);
    } catch (IOException e) {
        throw new ConfigurationException(
            "Failed to save configuration file: " + e.getMessage(), e);
    }
}

// Caller must handle:
try {
    configManager.saveTemperatureUnit(unit);
} catch (ConfigurationException e) {
    LOGGER.error("Failed to persist temperature unit preference: {}", e.getMessage());
    ui.showError("⚠️ No pudimos guardar tu preferencia. Se usará Celsius en proxima sesion.");
}
```

---

### 11. **[MEDIUM] Missing No-Arg Constructor for GeoLocation and WeatherData (Gson Compatibility)**

**File:** [GeoLocation.java](GeoLocation.java#L16-L21), [WeatherData.java](WeatherData.java#L23-L29)  
**Priority:** MEDIUM  
**Severity:** Potential runtime errors with Gson

**Problem:**
No-arg constructors exist for Gson compatibility, but they're mentioned in comments only:

```java
/**
 * Constructor vacío para compatibilidad con Gson.
 */
public GeoLocation() {
}
```

This creates uninitialized objects, which could cause:
```java
GeoLocation loc = new GeoLocation();  // latitude = 0, longitude = 0, cityName = null
loc.getCityName();  // Returns null - potential NullPointerException downstream
```

**Impact:**
- Gson deserialization creates invalid objects
- Null pointer exceptions at runtime
- Objects can be in invalid states after Gson parsing
- No validation happens during deserialization

**Suggested Fix:**
Use Gson-aware validation:
```java
public class GeoLocation {
    private final double latitude;
    private final double longitude;
    private final String cityName;
    
    // Private constructor for Gson
    @SerializedName("latitude")
    private double _latitude;
    
    @SerializedName("longitude")
    private double _longitude;
    
    @SerializedName("name")
    private String _cityName;
    
    public GeoLocation(double latitude, double longitude, String cityName) {
        validate(latitude, longitude, cityName);
        this.latitude = latitude;
        this.longitude = longitude;
        this.cityName = cityName;
    }
    
    @PostConstruct  // Called by Gson after deserialization
    private void postDeserialize() {
        validate(_latitude, _longitude, _cityName);
        // Copy to final fields via reflection or builder
    }
}
```

OR use Gson TypeAdapter:
```java
public class GeoLocationTypeAdapter extends TypeAdapter<GeoLocation> {
    @Override
    public GeoLocation read(JsonReader in) throws IOException {
        // Manual validation during parsing
    }
}
```

---

## Low-Priority Issues

### 12. **[LOW] Loose Error Handling in TemperatureUnit Enum**

**File:** [TemperatureUnit.java](TemperatureUnit.java#L44-L75)  
**Priority:** LOW  
**Severity:** Silent defaults might hide bugs

**Problem:**
`fromChoice()` and `fromString()` silently return CELSIUS on invalid input instead of throwing exceptions:

```java
public static TemperatureUnit fromChoice(int choice) {
    switch (choice) {
        case 1: return CELSIUS;
        case 2: return FAHRENHEIT;
        case 3: return KELVIN;
        default: return CELSIUS;  // ❌ Silent fallback
    }
}

public static TemperatureUnit fromString(String unitName) {
    // ... similar pattern ...
    default: return CELSIUS;  // ❌ Silent fallback
}
```

**Impact:**
- Invalid configuration silently becomes Celsius
- Difficult to detect configuration problems
- Might be intentional (user-friendly defaults) but undocumented

**Suggested Fix:**
If defaults are intentional, document it. If not, throw exception:

```java
public static TemperatureUnit fromChoice(int choice) {
    switch (choice) {
        case 1: return CELSIUS;
        case 2: return FAHRENHEIT;
        case 3: return KELVIN;
        default: throw new IllegalArgumentException(
            "Invalid temperature unit choice: " + choice + ". Must be 1-3");
    }
}
```

OR document the behavior:
```java
/**
 * Converts a choice number to TemperatureUnit.
 * <p>
 * Invalid choices default to CELSIUS.  This is intentional to provide
 * a sensible default if user input is malformed. Consider this when
 * loading from untrusted sources.
 */
```

---

### 13. **[LOW] AppConfig Has All Constants as Public Static**

**File:** [AppConfig.java](AppConfig.java)  
**Priority:** LOW  
**Severity:** Poor encapsulation, accidental modification

**Problem:**
```java
public static final String GEOCODING_API_BASE_URL = "https://...";
public static final int MAX_RETRIES = 3;
public static final String CONFIG_FILE = CONFIG_DIR + "/config.properties";
// ❌ All public; could be accessed/modified anywhere
```

**Impact:**
- Easy to accidentally use wrong constant
- Maintenance: changing constant affects unknown number of places
- Testing: can't easily mock/override constants
- No encapsulation of configuration logic

**Suggested Fix:**
Either:

Option A - Make private with accessor:
```java
private static final int MAX_RETRIES = 3;

public static int getMaxRetries() {
    return MAX_RETRIES;
}
```

Option B - Read from external config file:
```java
private static final Properties CONFIG = loadConfig();

private static Properties loadConfig() {
    Properties props = new Properties();
    try (InputStream is = AppConfig.class.getResourceAsStream("/application.properties")) {
        props.load(is);
    } catch (IOException e) {
        // fallback to defaults
    }
    return props;
}

public static int getMaxRetries() {
    return Integer.parseInt(CONFIG.getProperty("max_retries", "3"));
}
```

Option C - Use Spring Boot @ConfigurationProperties:
```java
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private int maxRetries = 3;
    private String geocodingUrl = "https://geocoding-api.open-meteo.com/v1";
    
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int value) { this.maxRetries = value; }
}
```

---

### 14. **[LOW] No Caching Mechanism in WeatherService**

**File:** [WeatherService.java](WeatherService.java#L34-L50)  
**Priority:** LOW  
**Severity:** Performance, API quota

**Problem:**
Every identical city query makes a fresh API call:

```java
public WeatherData getWeatherByCityName(String cityName) {
    String normalizedCityName = validateAndNormalizeCityName(cityName);
    
    try {
        LOGGER.info("Buscando clima para: '{}'", normalizedCityName);
        
        GeoLocation location = openMeteoClient.resolveCity(normalizedCityName);
        WeatherData weatherData = openMeteoClient.getWeatherData(location, temperatureUnit);
        // ❌ No check if we just queried this city 30 seconds ago
        
        return weatherData;
    }
}
```

**Impact:**
- Hits API repeatedly for same city in short time
- Uses API quota/rate limits quickly
- Unnecessary network calls
- Slower response time

**Suggested Fix:**
Add simple caching:

```java
private final Map<String, CachedWeatherData> cache = new ConcurrentHashMap<>();
private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

private static class CachedWeatherData {
    final WeatherData data;
    final long timestamp;
    
    CachedWeatherData(WeatherData data) {
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    boolean isExpired() {
        return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
    }
}

public WeatherData getWeatherByCityName(String cityName) {
    String normalized = validateAndNormalizeCityName(cityName);
    
    // Check cache
    CachedWeatherData cached = cache.get(normalized);
    if (cached != null && !cached.isExpired()) {
        LOGGER.debug("Cache hit for city: {}", normalized);
        return cached.data;
    }
    
    // Fetch from API
    GeoLocation location = openMeteoClient.resolveCity(normalized);
    WeatherData weatherData = openMeteoClient.getWeatherData(location, temperatureUnit);
    cache.put(normalized, new CachedWeatherData(weatherData));
    
    return weatherData;
}
```

---

### 15. **[LOW] Main Method Is Complex and Hard to Test**

**File:** [Main.java](Main.java#L20-L120)  
**Priority:** LOW  
**Severity:** Testability, maintainability

**Problem:**
Main method contains business logic flow instead of delegating:

```java
public static void main(String[] args) {
    // ... initialization ...
    while (continueRunning) {
        try {
            String cityName = ui.getUserCity();
            WeatherData weatherData = weatherService.getWeatherByCityName(cityName);
            ui.displayWeather(weatherData);
            continueRunning = ui.askContinue();
        } catch (IllegalArgumentException e) {
            // Catch and handle
        } catch (RuntimeException e) {
            // Catch and handle
        }
    }
    // ... cleanup ...
}
```

All orchestration logic is in main, making it impossible to test without mocking everything.

**Suggested Fix:**
Extract to separate application class:

```java
public class WeatherApplication {
    private final ConsoleUI ui;
    private final WeatherService weatherService;
    
    public WeatherApplication(ConsoleUI ui, WeatherService weatherService) {
        this.ui = ui;
        this.weatherService = weatherService;
    }
    
    public void run() {
        boolean continueRunning = true;
        while (continueRunning) {
            try {
                String cityName = ui.getUserCity();
                WeatherData weatherData = weatherService.getWeatherByCityName(cityName);
                ui.displayWeather(weatherData);
                continueRunning = ui.askContinue();
            } catch (CityNotFoundException e) {
                ui.showError("City not found: " + e.getMessage());
            } catch (NetworkException e) {
                ui.showError("Network error: " + e.getMessage());
                continueRunning = isRetryRequested();
            }
        }
    }
    
    private boolean isRetryRequested() {
        // ... retry logic ...
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            ConsoleUI ui = new ConsoleUI();
            // ... initialize ..
            WeatherApplication app = new WeatherApplication(ui, weatherService);
            app.run();
        } finally {
            // cleanup
        }
    }
}
```

---

### 16. **[LOW] Hard-coded Strings in ConsoleUI**

**File:** [ConsoleUI.java](ConsoleUI.java#L40-L90)  
**Priority:** LOW  
**Severity:** Localization, maintenance

**Problem:**
All UI strings are hard-coded in Spanish with no i18n support:

```java
System.out.println("╔═══════════════════════════════════════════════════════════╗");
System.out.println("║        🌍 APLICACIÓN DE CLIMA EN TIEMPO REAL 🌦️          ║");
// ... repeated throughout file
System.out.print("🔍 Ingresa el nombre de la ciudad (ej: Madrid, Nueva York): ");
```

**Impact:**
- Cannot support multiple languages
- String changes require code recompilation
- Difficult to maintain: strings scattered throughout code

**Suggested Fix:**
Use resource bundles:

```java
public class ConsoleUI {
    private static final ResourceBundle messages = 
        ResourceBundle.getBundle("messages");
    
    public void showWelcomeMessage() {
        System.out.println(messages.getString("ui.welcome.banner"));
        System.out.println(messages.getString("ui.welcome.powered_by"));
    }
    
    public String getUserCity() {
        System.out.print(messages.getString("ui.prompt.city_name"));
        // ...
    }
}
```

Create `src/main/resources/messages.properties`:
```properties
ui.welcome.banner=╔═══════════════════════════════════════════════════════════╗
ui.welcome.powered_by=║              Powered by Open-Meteo API                   ║
ui.prompt.city_name=🔍 Ingresa el nombre de la ciudad (ej: Madrid, Nueva York):
```

And `messages_en.properties` for English.

---

## Architecture and Design Pattern Issues

### 17. **[MEDIUM] Lack of Dependency Injection**

**File:** All files  
**Priority:** MEDIUM  
**Severity:** Tight coupling, testability

**Problem:**
Classes instantiate their own dependencies directly:

```java
// In Main.java
OpenMeteoClient client = new OpenMeteoClient();
WeatherService weatherService = new WeatherService(client, selectedUnit);

// In OpenMeteoClient.java
this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(AppConfig.REQUEST_TIMEOUT_SECONDS))
        .build();
```

**Impact:**
- Hard to mock in tests
- Can't swap implementations (e.g., use cached API responder for testing)
- Direct dependency on concrete classes
- Hidden dependencies (hard-coded timeouts, hardcoded config)

**Suggested Fix:**
Use constructor injection and interface extraction:

```java
public interface WeatherClient {
    GeoLocation resolveCity(String cityName);
    WeatherData getWeatherData(GeoLocation location, TemperatureUnit unit);
}

public class OpenMeteoClient implements WeatherClient { }

public class MockWeatherClient implements WeatherClient { }

public class WeatherService {
    private final WeatherClient client;
    
    public WeatherService(WeatherClient client, TemperatureUnit unit) {
        this.client = client;
    }
}

// In Main or app configuration:
WeatherClient client = new OpenMeteoClient();  // or MockWeatherClient for testing
WeatherService service = new WeatherService(client, unit);
```

---

### 18. **[MEDIUM] Missing Domain-Specific Exception Hierarchy**

**File:** Multiple files  
**Priority:** MEDIUM  
**Severity:** Poor error handling semantics

**Creates:**
All exception types are either `IllegalArgumentException` or `RuntimeException`:

```java
throw new IllegalArgumentException("❌ La ubicación no puede ser nula");
throw new RuntimeException("Error de conexión después de " + AppConfig.MAX_RETRIES + " intentos");
throw new RuntimeException("Solicitud interrumpida");
throw new RuntimeException("Error al parsear respuesta de geocodificación: " + e.getMessage());
```

**Impact:**
- Can't distinguish between different error types
- Hard to handle specific errors specially
- All RuntimeExceptions look the same
- No semantic information

**Suggested Fix:**
Create exception hierarchy:

```java
public abstract class WeatherAppException extends Exception {
    public WeatherAppException(String message) {
        super(message);
    }
    
    public WeatherAppException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class CityNotFoundException extends WeatherAppException {
    public CityNotFoundException(String cityName) {
        super("City not found: " + cityName);
    }
}

public class GeolocationException extends WeatherAppException {
    public GeolocationException(String message) {
        super(message);
    }
}

public class WeatherDataException extends WeatherAppException {
    public WeatherDataException(String message) {
        super(message);
    }
}

public class NetworkException extends WeatherAppException {
    private final int attemptsMade;
    
    public NetworkException(String message, int attemptsMade, Throwable cause) {
        super(message, cause);
        this.attemptsMade = attemptsMade;
    }
}
```

---

## Summary Table

| File | Issue | Priority | Category |
|------|-------|----------|----------|
| Main.java | Resource leak: Scanner never closed | CRITICAL | Resource Management |
| Main.java | Stack overflow: Recursive getUserCity() | CRITICAL | Error Handling |
| Main.java | Scanner created in catch block not closed | CRITICAL | Resource Management |
| ConsoleUI.java | Scanner resource leak | CRITICAL | Resource Management |
| OpenMeteoClient.java | Duplicate temperature validation | HIGH | Code Quality |
| OpenMeteoClient.java | Low cohesion: too many responsibilities | MEDIUM | Architecture |
| OpenMeteoClient.java | Large switch statement for weather codes | MEDIUM | Code Quality |
| All files | Inconsistent logging (System.out vs SLF4J) | HIGH | Operational Visibility |
| Main.java | Mixed exception types in catch block | HIGH | Error Handling |
| GeoLocation.java | Mutable model with public setters | MEDIUM | Design Patterns |
| WeatherData.java | No validation in constructors | MEDIUM | Design Patterns |
| ConfigManager.java | Silent error handling, no validation | MEDIUM | Error Handling |
| TemperatureUnit.java | Silent defaults instead of exceptions | LOW | Error Handling |
| AppConfig.java | All public static constants | LOW | Encapsulation |
| WeatherService.java | No caching mechanism | LOW | Performance |
| Main.java | Complex main method, hard to test | LOW | Testability |
| ConsoleUI.java | Hard-coded strings, no i18n | LOW | Maintainability |
| All files | Lack of dependency injection | MEDIUM | Architecture |
| All files | Missing domain-specific exceptions | MEDIUM | Design Patterns |

---

## Recommended Priority Order for Fixes

**Phase 1 (Critical - Fix immediately):**
1. Resource leaks (Scanner in Main, ConsoleUI)
2. Stack overflow in getUserCity()
3. Clean up exception handling in Main

**Phase 2 (High - Fix before production):**
4. Implement consistent logging (SLF4J)
5. Refactor OpenMeteoClient for single responsibility
6. Create domain-specific exception hierarchy

**Phase 3 (Medium - Improve quality):**
7. Make models immutable with validation
8. Fix ConfigManager error handling
9. Add caching to WeatherService
10. Implement dependency injection

**Phase 4 (Low - Nice to have):**
11. Replace hard-coded strings with ResourceBundle
12. Extract main logic to testable App class
13. Refactor AppConfig to be externally configurable

---

## Testing Recommendations

**Current testability problems:**
- Static methods in AppConfig
- Hard dependency instantiation
- No interface abstractions
- Mixed testing concerns

**Add test coverage for:**
- Exception handling paths (different error types)
- Boundary validation (invalid coordinates, temperatures)
- Configuration persistence and loading
- Retry logic with exponential backoff
- Weather code mapping completeness
- Input validation in ConsoleUI

**Example test structure:**
```java
@Test
public void givenInvalidLatitude_whenValidating_thenThrowsException() {
    GeoLocation invalid = new GeoLocation(91.0, 0, "Test");  // Should fail validation
    assertThrows(IllegalArgumentException.class, invalid);
}

@Test
public void givenNetworkError_whenFetchingWeather_thenRetriesWithBackoff() {
    WeatherClient mockClient = new MockWeatherClient()
        .failTimes(2)  // Fail twice
        .succeedOnce();
    
    WeatherService service = new WeatherService(mockClient, TemperatureUnit.CELSIUS);
    WeatherData result = service.getWeatherByCityName("Madrid");
    
    assertNotNull(result);
    assertEquals("Madrid", result.getCityName());
}
```

---

## Conclusion

The weather app demonstrates good foundational structure with clear separation of concerns. However, it has **critical resource management issues** that must be fixed immediately before running in production. The main areas for improvement are:

1. **Resource Management** - Fix Scanner leaks
2. **Error Handling** - Distinguish exception types and handle stack overflow
3. **Code Organization** - Refactor for single responsibility
4. **Logging** - Standardize on SLF4J
5. **Design** - Add immutability, validation, and dependency injection

With these fixes, the codebase will be significantly more robust, testable, and maintainable.
