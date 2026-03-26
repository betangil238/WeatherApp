# ✅ Checklist de Verificación - Weather App

## 📋 Pre-Deployment (Antes de Ejecutar)

### Estructura de Archivos
- [ ] `src/main/resources/index.html` - Existe
- [ ] `src/main/resources/styles.css` - Existe
- [ ] `src/main/resources/script.js` - Existe
- [ ] `src/main/java/com/weatherapp/controller/WeatherApiController.java` - Existe
- [ ] `src/main/java/com/weatherapp/config/WebMvcConfig.java` - Existe
- [ ] `pom.xml` - Contiene dependencias correctas

### Dependencias (pom.xml)
- [ ] Spring Boot Web Starter incluido
- [ ] Gson 2.10.1 incluido
- [ ] JUnit 5.9.2+ para testing (si necesario)
- [ ] Java version: 11 o superior

### Configuración Backend
- [ ] `application.properties` existe en `src/main/resources/`
- [ ] `server.port=8080` configurado
- [ ] `WeatherService` anotada con `@Service`
- [ ] `WeatherForecastService` anotada con `@Service`

### Verificaciones de Código
- [ ] `WeatherApiController` tiene `@RestController`
- [ ] `WebMvcConfig` tiene `@Configuration`
- [ ] DTOs (WeatherDTO, ForecastDTO) tienen getters/setters
- [ ] Endpoints manejan excepciones correctamente

---

## 🚀 Ejecución (Build & Run)

### Compilación
```bash
# Ejecutar en terminal desde raíz del proyecto
mvn clean install
```

- [ ] Compilación exitosa (sin errores)
- [ ] Todos los archivos .class en target/classes/
- [ ] DTOs compilados correctamente
- [ ] Controlador compila sin errores
- [ ] No hay warnings importantes

### Ejecutar Spring Boot
```bash
mvn spring-boot:run
```

O despues de compilar:
```bash
java -jar target/weather-app-*.jar
```

- [ ] Servidor inicia sin errores
- [ ] Logs muestran "Started ... in X.XXX seconds"
- [ ] Puerto 8080 está free (o el configurado)
- [ ] No hay excepciones en logs

---

## 🌐 Verificación de Endpoints

Abre navegador en `http://localhost:8080` o usa terminal:

### Health Check
```bash
curl http://localhost:8080/api/weather/health
```
- [ ] Devuelve `{"status":"ok"}` o similar
- [ ] Status code 200
- [ ] Sin CORS errors en consola

### Una Ciudad
```bash
curl "http://localhost:8080/api/weather?city=Madrid&unit=celsius"
```
- [ ] Devuelve JSON con weather data
- [ ] Incluye: temperature, apparentTemperature, humidity, windSpeed, precipitation
- [ ] temperatureUnit es "Celsius"
- [ ] Status code 200

### Múltiples Ciudades
```bash
curl "http://localhost:8080/api/weather/multiple?cities=Madrid,Barcelona,Valencia&unit=celsius"
```
- [ ] Devuelve array JSON con múltiples ciudades
- [ ] Cada ciudad tiene estructura completa
- [ ] Status code 200

### Pronóstico (5 días)
```bash
curl "http://localhost:8080/api/weather/forecast?city=Madrid&unit=celsius"
```
- [ ] Devuelve JSON con estructura: { cityName, weatherUnit, list: [...] }
- [ ] Lista contiene mínimo 5 días
- [ ] Cada día tiene: date, maxTemp, minTemp, humidity, description
- [ ] Status code 200

---

## 🎨 Verificación del Interfaz Web

Abre navegador: `http://localhost:8080`

### Página Principal Carga
- [ ] HTML loads sin errores 404
- [ ] CSS aplica (puedes ver colores, tipografía, diseño)
- [ ] JavaScript ejecuta (consulta console F12)
- [ ] No hay red flags rojos en console

### Elementos Visuales
- [ ] Encabezado visible ("Weather App")
- [ ] Selectores de modo (Una Ciudad / Comparar)
- [ ] Selectores de temperatura (°C, °F, K)
- [ ] Input de búsqueda visible
- [ ] Botones funcionales (no grayed out)

### Modo "Una Ciudad"
- [ ] Escribir ciudad y enterclick: petición se envía
- [ ] Datos cargan ("Cargando..." aparece y desaparece)
- [ ] Card de clima se muestra con datos:
  - [ ] Nombre ciudad
  - [ ] Icono/emoji de clima
  - [ ] Temperatura actual
  - [ ] Sensación térmica
  - [ ] Humedad
  - [ ] Velocidad viento
  - [ ] Precipitación
- [ ] Al cambiar °C/°F/K, temperaturas se actualizan

### Modo "Múltiples Ciudades"
- [ ] Escribir ciudades separadas por comas (ej: "Madrid, Barcelona, Valencia")
- [ ] Presionar Enter o botón
- [ ] Ciudades aparecen como "tags" removibles
- [ ] Botón "Comparar Ciudades" activa
- [ ] Click en comparar:
  - [ ] Tabla comparativa se muestra
  - [ ] Columnas: Ciudad, Temp, Sensación, Humedad, Viento, Lluvia
  - [ ] Analítica debajo (Hottest, Coldest, etc.)
- [ ] Remover ciudad del tag:
  - [ ] Desaparece de lista
  - [ ] Se actualiza tabla comparativa

### Modo "Pronóstico"
- [ ] Escribir ciudad en input general
- [ ] Puede estar en modo "Una Ciudad" o "Comparar"
- [ ] Ver sección de "Próximos 5 Días"
- [ ] 5 cards de día se muestran con:
  - [ ] Fecha
  - [ ] Icono/emoji
  - [ ] Temp máx/mín
  - [ ] Humedad
  - [ ] Descripción clima

### Interactividad
- [ ] Cambiar entre modos (Una Ciudad ↔ Comparar)
- [ ] Cambiar temperat unit (°C → °F → K)
- [ ] Buscar nuevas ciudades (datos actualizan)
- [ ] Remover ciudades de lista múltiple
- [ ] "Comparar" genera tabla nueva
- [ ] Scroll funciona sin lag
- [ ] Animaciones son smooth

### Feedback Visual
- [ ] Loading spinner activa durante carga
- [ ] Mensajes de error si:
  - [ ] Ciudad no encontrada
  - [ ] API no responde
  - [ ] Network error
- [ ] Mensajes de éxito si datos cargan correctamente
- [ ] Mensajes desaparecen después de 5s (auto-hide)

### Responsivo (Mobile)
- [ ] Abrir DevTools (F12)
- [ ] Click en icono "mobile" / responsive
- [ ] Seleccionar "iPhone 12" o "iPad"
- [ ] Layout se adapta:
  - [ ] Elementos no se solapan
  - [ ] Texto legible (no tiny)
  - [ ] Botones clickeables
  - [ ] Tabla scrollable horizontalmente si es necesario
  - [ ] Input funciona en mobile keyboard
  - [ ] Nada se corta fuera de pantalla

---

## 🔍 Verificación de Consola (F12 → Console)

- [ ] No hay errores rojos (red X)
- [ ] Warnings es OK (amarillo)
- [ ] Logs de debug muestran requests siendo hechas
- [ ] No hay "CORS error" cuando hace fetch
- [ ] API_BASE_URL correcto en logs: `http://localhost:8080/api`
- [ ] Responses JSON valido en Network tab

---

## 📊 Validación de Datos

### Temperaturas
```javascript
// En console, ejecutar:
fetch('http://localhost:8080/api/weather?city=Madrid&unit=celsius')
  .then(r => r.json())
  .then(d => {
    console.log('Temp:', d.temperature); // -90 a +70 °C
    console.log('Wind:', d.windSpeed);    // >= 0
    console.log('Rain:', d.precipitation); // >= 0
  });
```

- [ ] Temperatura entre -90°C y +70°C (válido para clima polar)
- [ ] Viento >= 0 km/h
- [ ] Precipitación >= 0 mm

### Conversiones
- [ ] °C: 20°C
- [ ] °F: 68°F (debería ser (20*9/5)+32 = 68)
- [ ] K: 293.15K (debería ser 20+273.15 = 293.15)

---

## 🐛 Troubleshooting Quick Checks

### Si no carga página
- [ ] `http://localhost:8080/index.html` funciona?
- [ ] Revisar Network tab, index.html devuelve 200?
- [ ] Revisar server logs para 404 errors
- [ ] ¿index.html está en `src/main/resources/public/`?

### Si fetch falla ("Failed to fetch")
- [ ] API endpoint realmente existe?
  - [ ] ¿Controlador tiene `@RestController`?
  - [ ] ¿Metodo tiene `@GetMapping`?
  - [ ] ¿Path es exacto? (no `/weather/`, es `/api/weather`)
- [ ] CORS error?
  - [ ] WebMvcConfig.java tiene CORS?
  - [ ] Probar primero `/api/weather/health`
- [ ] Status 500?
  - [ ] Revisar server logs
  - [ ] ¿City existe en DB? (OpenMeteo)
  - [ ] ¿API service inyectado correctamente?

### Si datos no se muestran
- [ ] Response es JSON válido?
  - [ ] Network tab → Response puede estar 404/500
  - [ ] Mirar console errors rojo
- [ ] DTO properties match?
  - [ ] HTML intenta acceder a `data.temperature`?
  - [ ] DTO tiene getter para `temperature`?
- [ ] Parsing error?
  - [ ] JSON.parse falla?
  - [ ] Mirar console para SyntaxError

### Si animaciones no se ven
- [ ] CSS loaded? (Network tab)
- [ ] Styles.css devuelve 200?
- [ ] Revisar en Elements → Styles panel
- [ ] Probar en incognito (cache issue)?

---

## ✅ Post-Deployment

Después de verificar todo:

- [ ] Documentacion actualizada
- [ ] README.md menciona web interface
- [ ] Credenciales/secrets NO en código
- [ ] CORS en producción: actualizar `allowedOrigins` (no `*`)
- [ ] API_BASE_URL en producción: actualizar a dominio real
- [ ] Hacer cambios finales de estilos (colores, tipografía)
- [ ] Optimidad de imagenes (si las hay)
- [ ] Testing cross-browser (Chrome, Firefox, Safari, Edge)
- [ ] Performance: F12 → Lighthouse score > 90

---

## 📝 Notas

- **Si algo falla**, verificar en este orden: estructura archivos → compilación → servidor → endpoints → interfaz
- **Para debugging**: F12 en navegador, buscar errores rojo en Console y Network
- **Para cambios rápidos**: React CSS en styles.css y refresh (Ctrl+Shift+R para hard reload)
- **Para cambios Java**: Recompilat con `mvn clean install` y reiniciar servidor

---

**Última revisión**: Marzo 24, 2026  
**Versión**: 1.0.0  
**Status**: ✅ Ready for Testing
