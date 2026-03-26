# 🔧 Guía de Integración: Caché en tu Aplicación

## Paso 1: Integrar en Main.java

```java
package com.weatherapp;

import com.weatherapp.client.OpenMeteoClient;
import com.weatherapp.config.TemperatureUnit;
import com.weatherapp.service.WeatherService;
import com.weatherapp.service.CachedWeatherService;
import com.weatherapp.cache.CacheConfig;
import com.weatherapp.presentation.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        // Crear servicios
        OpenMeteoClient client = new OpenMeteoClient();
        WeatherService service = new WeatherService(client, TemperatureUnit.CELSIUS);
        
        // 🔥 AGREGAR CACHÉ (1 línea vs sin caché)
        WeatherService cachedService = new CachedWeatherService(service);
        
        // UI usa el servicio con caché automáticamente
        ConsoleUI ui = new ConsoleUI(cachedService);
        ui.run();
    }
}
```

**¡Eso es todo!** El caché funciona automáticamente. Sin cambios adicionales.

---

## Paso 2: Personalizar TTL (Opcional)

### Opción A: TTL fijo
```java
WeatherService cachedService = new CachedWeatherService(
    service, 
    new WeatherCache(30 * 60 * 1000) // 30 minutos
);
```

### Opción B: Usar presets en CacheConfig
```java
WeatherService cachedService = new CachedWeatherService(
    service,
    new WeatherCache(CacheConfig.MOBILE_APP_TTL_MS) // 30 minutos para móvil
);
```

### Opción C: Detectar plataforma automáticamente
```java
public class Main {
    public static void main(String[] args) {
        OpenMeteoClient client = new OpenMeteoClient();
        WeatherService service = new WeatherService(client, TemperatureUnit.CELSIUS);
        
        // Detectar SO y ajustar TTL
        long ttl = detectTTLByPlatform();
        WeatherService cachedService = new CachedWeatherService(
            service,
            new WeatherCache(ttl)
        );
        
        ConsoleUI ui = new ConsoleUI(cachedService);
        ui.run();
    }
    
    private static long detectTTLByPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("android")) {
            return CacheConfig.MOBILE_APP_TTL_MS;      // 30 min
        } else if (os.contains("mac")) {
            return CacheConfig.DESKTOP_APP_TTL_MS;     // 60 min
        } else if (os.contains("win")) {
            return CacheConfig.DESKTOP_APP_TTL_MS;     // 60 min
        } else if (os.contains("nux")) {
            return CacheConfig.WEB_APP_TTL_MS;         // 60 min
        }
        
        return CacheConfig.DEFAULT_TTL_MS;             // 60 min (fallback)
    }
}
```

---

## Paso 3: Monitorear Caché (Opcional)

### En ConsoleUI o tu componente principal
```java
public class ConsoleUI {
    // ... código existente ...
    
    private final CachedWeatherService service;
    
    public void displayCacheStats() {
        if (service instanceof CachedWeatherService) {
            CachedWeatherService cached = (CachedWeatherService) service;
            var stats = cached.getCacheStats();
            
            System.out.println("\n📊 Estadísticas del Caché:");
            System.out.println("├─ Ciudades en caché: " + stats.getEntriesCount());
            System.out.println("├─ TTL: " + stats.getTtlMinutes() + " minutos");
            System.out.println("└─ Uso estimado: ~" + (stats.getEntriesCount() * 800) + " bytes");
        }
    }
    
    public void cleanupCache() {
        if (service instanceof CachedWeatherService) {
            CachedWeatherService cached = (CachedWeatherService) service;
            cached.cleanupCache();
            System.out.println("✅ Caché limpiado");
        }
    }
}
```

---

## Paso 4: Pruebas (Opcional)

### Test simple
```java
import static org.junit.jupiter.api.Assertions.*;

public class CacheIntegrationTest {
    
    @Test
    public void testCachedServiceTransparency() throws Exception {
        WeatherService service = mock(WeatherService.class);
        WeatherData mockData = new WeatherData(/* ... */);
        when(service.getWeatherByCityName("Madrid")).thenReturn(mockData);
        
        CachedWeatherService cached = new CachedWeatherService(service);
        
        // Primera llamada: API
        WeatherData data1 = cached.getWeatherByCityName("Madrid");
        verify(service, times(1)).getWeatherByCityName("Madrid");
        
        // Segunda llamada: Caché
        WeatherData data2 = cached.getWeatherByCityName("Madrid");
        verify(service, times(1)).getWeatherByCityName("Madrid"); // Sin aumento
        
        assertEquals(data1, data2);
    }
}
```

---

## Casos de Integración Específicos

### 📱 Android/Kotlin
```kotlin
// main/java/com/example/weatherapp/MainActivity.kt
class MainActivity : AppCompatActivity() {
    private lateinit var service: WeatherService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val client = OpenMeteoClient()
        val baseService = WeatherService(client, TemperatureUnit.CELSIUS)
        
        // Personalizar TTL para batería (menos actualizaciones)
        service = CachedWeatherService(
            baseService,
            WeatherCache(CacheConfig.MOBILE_APP_TTL_MS)
        )
        
        loadWeatherData()
    }
    
    private fun loadWeatherData() {
        lifecycleScope.launch {
            try {
                val data = service.getWeatherByCityName("Madrid")
                updateUI(data)
            } catch (e: Exception) {
                showError(e.message)
            }
        }
    }
}
```

### 🌐 Spring Boot Web
```java
// com/example/weather/config/AppConfig.java
@Configuration
public class AppConfig {
    
    @Bean
    public WeatherService weatherService() {
        OpenMeteoClient client = new OpenMeteoClient();
        WeatherService service = new WeatherService(client, TemperatureUnit.CELSIUS);
        
        // Caché para web apps (muchos usuarios)
        return new CachedWeatherService(
            service,
            new WeatherCache(CacheConfig.WEB_APP_TTL_MS)
        );
    }
}

// com/example/weather/controller/WeatherController.java
@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    
    @Autowired
    private CachedWeatherService service;
    
    @GetMapping("/{city}")
    public ResponseEntity<WeatherData> getWeather(@PathVariable String city) {
        WeatherData data = service.getWeatherByCityName(city);
        return ResponseEntity.ok(data);
    }
    
    @GetMapping("/cache/stats")
    public ResponseEntity<CacheStats> getCacheStats() {
        return ResponseEntity.ok(service.getCacheStats());
    }
}
```

### 🖥️ Desktop GUI (Swing/JavaFX)
```java
// fx/WeatherApp.java (JavaFX)
public class WeatherApp extends Application {
    
    private CachedWeatherService service;
    
    @Override
    public void start(Stage stage) {
        initializeService();
        setupUI(stage);
    }
    
    private void initializeService() {
        OpenMeteoClient client = new OpenMeteoClient();
        WeatherService baseService = new WeatherService(
            client, 
            TemperatureUnit.CELSIUS
        );
        
        // Caché para desktop (balance)
        service = new CachedWeatherService(
            baseService,
            new WeatherCache(CacheConfig.DESKTOP_APP_TTL_MS)
        );
    }
    
    private void searchWeather(String city) {
        Task<WeatherData> task = new Task<WeatherData>() {
            @Override
            protected WeatherData call() throws Exception {
                return service.getWeatherByCityName(city); // Del caché si disponible
            }
        };
        
        // Actualizar UI cuando completa
        task.setOnSucceeded(e -> updateUI(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        
        new Thread(task).start();
    }
}
```

---

## ✅ Pre-requisitos

✓ Java 11+  
✓ Proyecto Maven debe tener `pom.xml` actualizado  
✓ WeatherService en classpath  
✓ OpenMeteoClient disponible  

---

## 🚀 Resumen

| Paso | Acción | Líneas Código |
|------|--------|---|
| 1 | Importar CachedWeatherService | 1 import |
| 2 | Envolver servicio | 1 línea |
| 3 | Usar en tu código | Sin cambios (API idéntica) |

**Total: 2 líneas de código para 99% menos API calls.**

---

## 📝 Checklist de Integración

- [ ] Importar `CachedWeatherService` en Main/Config
- [ ] Crear instancia envolviendo `WeatherService`
- [ ] (Opcional) Configurar TTL según plataforma
- [ ] (Opcional) Limpiar logs si es necesario
- [ ] Compilar: `mvn clean compile`
- [ ] Probar: `mvn test`
- [ ] Ejecutar: `mvn exec:java -Dexec.mainClass="com.weatherapp.Main"`

---

## 🔍 Verificación

**En logs, deberías ver mensajes como:**
```
[INFO] WeatherCache inicializado con TTL: 3600000ms (60 minutos)
[DEBUG] 💾 Datos almacenados en caché para 'madrid'
[DEBUG] ✅ Caché HIT para 'madrid' (edad: 125ms)
```

Si ves estos mensajes, **¡el caché está funcionando!** 🎉

---

## ❓ Problemas Comunes

### "Import cannot be resolved: CachedWeatherService"
→ Ejecuta `mvn clean install` para que Maven compile las nuevas clases

### "Caché no funciona (siempre hace API calls)"
→ Verifica que estés usando `CachedWeatherService` no `WeatherService`

### "Mensajes de caché no aparecen en logs"
→ Edita `pom.xml` para cambiar el nivel de logging a DEBUG

### "Quiero deshabilitar el caché temporalmente"
→ Reemplaza: `new CachedWeatherService(service)` por solo `service`

---

## 📖 Documentación Relacionada

- [README_CACHE.md](README_CACHE.md) - Guía rápida
- [CACHE_USAGE_GUIDE.md](CACHE_USAGE_GUIDE.md) - Casos de uso completos
- [CACHE_IMPLEMENTATION_SUMMARY.md](CACHE_IMPLEMENTATION_SUMMARY.md) - Detalles técnicos
- [CacheConfig.java](src/main/java/com/weatherapp/cache/CacheConfig.java) - Configuración centralizada

---

**¡Lista para integrar! 🚀**
