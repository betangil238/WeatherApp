package com.weatherapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aplicación Spring Boot para Weather App.
 * Inicia el servidor web que expone los endpoints REST para el interfaz web.
 */
@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        LOGGER.info("==================================================");
        LOGGER.info("🌍 Iniciando Weather App - Web Server");
        LOGGER.info("==================================================");

        try {
            SpringApplication.run(Application.class, args);
            LOGGER.info("✅ Servidor iniciado exitosamente");
            LOGGER.info("📍 Accede a: http://localhost:8080");
        } catch (Exception e) {
            LOGGER.error("❌ Error al iniciar la aplicación", e);
            System.exit(1);
        }
    }
}
