package com.weatherapp.config;

import com.weatherapp.client.OpenMeteoClient;
import com.weatherapp.service.WeatherForecastService;
import com.weatherapp.service.WeatherService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Web MVC para servir archivos estáticos,
 * configurar CORS para la interfaz web, y registrar beans de servicios.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Registra el cliente HTTP para Open-Meteo API.
     * @return OpenMeteoClient configurado
     */
    @Bean
    public OpenMeteoClient openMeteoClient() {
        return new OpenMeteoClient();
    }

    /**
     * Registra el servicio de clima actual.
     * @param openMeteoClient cliente HTTP inyectado
     * @return WeatherService configurado con unidad CELSIUS
     */
    @Bean
    public WeatherService weatherService(OpenMeteoClient openMeteoClient) {
        return new WeatherService(openMeteoClient, TemperatureUnit.CELSIUS);
    }

    /**
     * Registra el servicio de pronóstico de clima.
     * @param weatherService servicio de clima inyectado
     * @return WeatherForecastService configurado con unidad CELSIUS
     */
    @Bean
    public WeatherForecastService weatherForecastService(WeatherService weatherService) {
        return new WeatherForecastService(weatherService, TemperatureUnit.CELSIUS);
    }

    /**
     * Configurar view controllers para servir index.html en la raíz
     */
    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    /**
     * Configurar CORS para permitir solicitudes desde el frontend
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")  // En producción, especifica dominio
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    /**
     * Servir archivos estáticos (HTML, CSS, JS)
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/", "classpath:/resources/");
    }
}
