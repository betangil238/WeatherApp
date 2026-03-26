# 🌐 Weather App - Interfaz Web con REST API

## 📋 Descripción

Esta carpeta contiene la **interfaz web completa** (HTML, CSS, JavaScript) para la aplicación Weather App. Es una aplicación moderna, responsive y estéticamente atractiva que se conecta con un **backend REST API** para obtener datos climáticos en tiempo real.

## 🎨 Características de la UI

### 1. **Diseño Moderno y Atractivo**
- Gradientes dinámicos con animaciones fluidas
- Tema oscuro profesional (dark mode)
- Iconos emoji intuitivos
- Efectos de hover y transiciones suaves
- Layout responsive para móviles, tablets y escritorio

### 2. **Modos de Operación**
- **Una Ciudad**: Búsqueda simple de clima actual
- **Múltiples Ciudades**: Modo comparativo con tabla de análisis
- **Pronóstico 5 Días**: Predicción del tiempo próximos 5 días

### 3. **Funcionalidades**
- ✅ Búsqueda por nombre de ciudad
- ✅ Comparación de clima entre ciudades
- ✅ Análisis comparativo (ciudad más calurosa, fría, etc.)
- ✅ Selector de unidades de temperatura (°C, °F, K)
- ✅ Pronóstico de 5 días con máximas/mínimas
- ✅ Manejo elegante de errores
- ✅ Mensajes de éxito/error con auto-ocultamiento
- ✅ Indicador de carga con spinner animado

## 📦 Estructura de Archivos

```
src/main/resources/
├── index.html          # Estructura HTML de la aplicación
├── styles.css         # Estilos CSS (1000+ líneas)
├── script.js          # Lógica JavaScript y peticiones API
└── README_WEB.md      # Este archivo
```

## 🔧 Configuración del API

### URL Base
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

Modifica esta URL según tu configuración de servidor.

### Endpoints Requeridos

#### 1. **Clima Actual - Una Ciudad**
```
GET /api/weather?city=Madrid&unit=celsius

Response: {
  "cityName": "Madrid, Spain",
  "temperature": 25.5,
  "apparentTemperature": 23.0,
  "humidity": 60,
  "windSpeed": 5.0,
  "precipitation": 0.0,
  "description": "☀️ Despejado",
  "temperatureUnit": "CELSIUS"
}
```

#### 2. **Múltiples Ciudades**
```
GET /api/weather/multiple?cities=Madrid,Barcelona,Valencia&unit=celsius

Response: [
  { ... weather data for Madrid ... },
  { ... weather data for Barcelona ... },
  { ... weather data for Valencia ... }
]
```

#### 3. **Pronóstico 5 Días**
```
GET /api/weather/forecast?city=Madrid&unit=celsius

Response: {
  "cityName": "Madrid, Spain",
  "days": [
    {
      "date": "2026-03-25",
      "maxTemp": 28.5,
      "minTemp": 15.2,
      "humidity": 65,
      "description": "☀️ Despejado"
    },
    ...
  ]
}
```

## 🚀 Cómo Usar

### 1. **Servir los Archivos Estáticos**

Opción A: Con Spring Boot
```java
// agregar a pom.xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

// Los archivos en src/main/resources/public/ serán servidos automáticamente
```

Opción B: Con servidor web simple
```bash
python -m http.server 8000
# Luego acceder a http://localhost:8000
```

### 2. **Crear Controladores REST en Java**

Ejemplo básico con Spring Boot:

```java
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class WeatherController {
    
    @Autowired
    private WeatherService weatherService;
    
    @GetMapping("/weather")
    public ResponseEntity<WeatherDTO> getWeather(
            @RequestParam String city,
            @RequestParam(defaultValue = "celsius") String unit) {
        
        TemperatureUnit temperatureUnit = TemperatureUnit.valueOf(unit.toUpperCase());
        WeatherData data = weatherService.getWeatherByCityName(city);
        return ResponseEntity.ok(mapToDTO(data, temperatureUnit));
    }
    
    @GetMapping("/weather/multiple")
    public ResponseEntity<List<WeatherDTO>> getMultipleWeather(
            @RequestParam String cities,
            @RequestParam(defaultValue = "celsius") String unit) {
        
        List<String> cityList = Arrays.asList(cities.split(","));
        TemperatureUnit temperatureUnit = TemperatureUnit.valueOf(unit.toUpperCase());
        List<WeatherData> results = weatherService.getWeatherByMultipleCities(cityList);
        return ResponseEntity.ok(results.stream()
            .map(d -> mapToDTO(d, temperatureUnit))
            .collect(Collectors.toList()));
    }
    
    @GetMapping("/weather/forecast")
    public ResponseEntity<ForecastDTO> getForecast(
            @RequestParam String city,
            @RequestParam(defaultValue = "celsius") String unit) {
        
        TemperatureUnit temperatureUnit = TemperatureUnit.valueOf(unit.toUpperCase());
        ForecastData data = forecastService.getForecast5Days(city);
        return ResponseEntity.ok(mapToForecastDTO(data, temperatureUnit));
    }
}
```

### 3. **DTOs Sugeridos para respuesta**

```java
@Data
public class WeatherDTO {
    private String cityName;
    private double temperature;
    private double apparentTemperature;
    private int humidity;
    private double windSpeed;
    private double precipitation;
    private String description;
    private String temperatureUnit;
}

@Data
public class ForecastDTO {
    private String cityName;
    private List<DayForecastDTO> days;
}

@Data
public class DayForecastDTO {
    private String date;
    private double maxTemp;
    private double minTemp;
    private int humidity;
    private String description;
}
```

## 🎯 Personalización

### Cambiar colores
Edita las variables CSS en `styles.css`:
```css
:root {
    --primary-color: #ff6b6b;
    --secondary-color: #4ecdc4;
    --accent-color: #45b7d1;
    /* ... más colores ... */
}
```

### Cambiar URL de API
Edita `script.js`:
```javascript
const API_BASE_URL = 'https://tu-dominio.com/api';
```

### Agregar nuevos campos
1. Actualiza la respuesta del API
2. Modifica las funciones `displayWeatherCard()` o `displayComparativeWeather()` en `script.js`
3. Actualiza el CSS según sea necesario

## 📱 Responsividad

El diseño es completamente responsive:
- **Desktop** (1200px+): Layout completo con 3+ columnas
- **Tablet** (768px-1199px): Layout adaptado, 2 columnas
- **Móvil** (< 768px): Layout single column, optimizado para touch

## 🔐 Consideraciones de Seguridad

- Agregar **CORS Headers** en el servidor backend
- Validar todas las entradas de usuario en el backend
- No exponer tokens o credenciales en el cliente
- Usar HTTPS en producción
- Implementar rate limiting en los endpoints

## 🐛 Solución de Problemas

### Error: "Failed to fetch"
- Verifica que el servidor esté ejecutándose en `http://localhost:8080`
- Revisa los CORS headers en el servidor
- Abre la consola del navegador (F12) para más detalles

### Error: "Ciudad no encontrada"
- Verifica que la ciudad existe en la API Open-Meteo
- Intenta con ciudades mayores/más conocidas
- Usa acentos correctamente (São Paulo, Zürich)

### Estilos no cargan
- Verifica que `styles.css` esté en la misma carpeta que `index.html`
- Revisa la consola para errores 404
- Limpia el caché del navegador (Ctrl+Shift+Delete)

## 📚 Recursos Adicionales

- **Open-Meteo API**: https://open-meteo.com/
- **MDN Web Docs**: https://developer.mozilla.org/
- **CSS Gradients**: https://cssgradients.io/
- **Animaciones CSS**: https://animate.style/

## ✅ Checklist de Implementación

- [ ] Crear carpeta `src/main/resources/`
- [ ] Copiar `index.html`, `styles.css`, `script.js`
- [ ] Crear controlador REST en Spring Boot
- [ ] Configurar CORS
- [ ] Implementar DTOs
- [ ] Probar endpoints con Postman/Insomnia
- [ ] Actualizar `API_BASE_URL` en script.js
- [ ] Probar en navegador
- [ ] Verificar responsividad en móviles
- [ ] Deployar a producción

## 📝 Notas

- La interfaz es completamente funcional sin dependencias externas (vanilla JavaScript)
- Soporta múltiples idiomas (principalmente español e inglés en descripciones)
- Optimizado para performance con carga asíncrona
- Estadísticas de código: **600+ líneas de JavaScript**, **1000+ líneas de CSS**

---

**Weather App © 2026** | [Ver código principal](/README.md)
