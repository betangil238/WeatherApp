/* ========================================
   CONFIGURACIÓN DE LA APLICACIÓN
   ======================================== */

const API_BASE_URL = 'http://localhost:8080/api';
let currentTemperatureUnit = 'celsius';
let currentMode = 'single';
let selectedCities = [];

/* ========================================
   ELEMENTOS DEL DOM
   ======================================== */

const modeButtons = document.querySelectorAll('.mode-btn');
const unitButtons = document.querySelectorAll('.unit-btn');
const searchBtn = document.getElementById('search-btn');
const cityInput = document.getElementById('city-input');
const addCitiesBtn = document.getElementById('add-cities-btn');
const citiesInput = document.getElementById('cities-input');
const compareBtn = document.getElementById('compare-btn');
const modeContents = document.querySelectorAll('.mode-content');
const singleResult = document.getElementById('single-result');
const multipleResult = document.getElementById('multiple-result');
const citiesList = document.getElementById('cities-list');
const loadingElement = document.getElementById('loading');
const errorMessage = document.getElementById('error-message');
const successMessage = document.getElementById('success-message');
const forecastBtn = document.getElementById('forecast-btn');
const forecastCityInput = document.getElementById('forecast-city-input');
const forecastResult = document.getElementById('forecast-result');

/* ========================================
   EVENT LISTENERS
   ======================================== */

// Cambiar modo (una ciudad vs múltiples ciudades)
modeButtons.forEach((btn) => {
    btn.addEventListener('click', (e) => {
        modeButtons.forEach((b) => b.classList.remove('active'));
        btn.classList.add('active');
        currentMode = btn.getAttribute('data-mode');
        updateModeDisplay();
    });
});

// Cambiar unidad de temperatura
unitButtons.forEach((btn) => {
    btn.addEventListener('click', (e) => {
        unitButtons.forEach((b) => b.classList.remove('active'));
        btn.classList.add('active');
        currentTemperatureUnit = btn.getAttribute('data-unit');
    });
});

// Búsqueda de una ciudad
searchBtn.addEventListener('click', searchSingleCity);
cityInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') searchSingleCity();
});

// Agregar ciudades para comparación
addCitiesBtn.addEventListener('click', addMultipleCities);
citiesInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') addMultipleCities();
});

// Comparar múltiples ciudades
compareBtn.addEventListener('click', compareMultipleCities);

// Pronóstico
forecastBtn.addEventListener('click', getForecast);
forecastCityInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') getForecast();
});

/* ========================================
   FUNCIONES PRINCIPALES
   ======================================== */

/**
 * Actualizar visualización del modo actual
 */
function updateModeDisplay() {
    modeContents.forEach((content) => {
        content.classList.remove('active');
    });

    if (currentMode === 'single') {
        document.getElementById('single-mode').classList.add('active');
        cityInput.focus();
    } else {
        document.getElementById('multiple-mode').classList.add('active');
        citiesInput.focus();
    }

    // Limpiar resultados previos
    singleResult.innerHTML = '';
    multipleResult.innerHTML = '';
}

/**
 * Mostrar/ocultar indicador de carga
 */
function setLoading(isLoading) {
    if (isLoading) {
        loadingElement.style.display = 'flex';
    } else {
        loadingElement.style.display = 'none';
    }
}

/**
 * Mostrar mensaje de error
 */
function showError(message) {
    errorMessage.textContent = `❌ ${message}`;
    errorMessage.style.display = 'block';
    setTimeout(() => {
        errorMessage.style.display = 'none';
    }, 5000);
}

/**
 * Mostrar mensaje de éxito
 */
function showSuccess(message) {
    successMessage.textContent = `✅ ${message}`;
    successMessage.style.display = 'block';
    setTimeout(() => {
        successMessage.style.display = 'none';
    }, 5000);
}

/* ========================================
   BÚSQUEDA DE UNA CIUDAD
   ======================================== */

async function searchSingleCity() {
    const city = cityInput.value.trim();

    if (!city) {
        showError('Por favor ingresa el nombre de una ciudad');
        return;
    }

    setLoading(true);
    singleResult.innerHTML = '';

    try {
        const response = await fetch(
            `${API_BASE_URL}/weather?city=${encodeURIComponent(city)}&unit=${currentTemperatureUnit}`
        );

        if (!response.ok) {
            if (response.status === 404) {
                throw new Error(`Ciudad no encontrada: ${city}`);
            }
            throw new Error('Error al obtener los datos');
        }

        const data = await response.json();
        displayWeatherCard(data, singleResult);
        cityInput.value = '';
        showSuccess(`Clima obtenido para ${data.cityName}`);
    } catch (error) {
        showError(error.message);
        console.error('Error:', error);
    } finally {
        setLoading(false);
    }
}

/**
 * Mostrar tarjeta de clima individual
 */
function displayWeatherCard(weather, container) {
    const temperatureSymbol = getTempSymbol(currentTemperatureUnit);
    const windSpeed = weather.windSpeed || 0;
    const precipitation = weather.precipitation || 0;

    const card = document.createElement('div');
    card.className = 'weather-card';
    card.innerHTML = `
        <div class="weather-header">
            <div class="weather-title">
                <div class="weather-icon">${getWeatherIcon(weather.description)}</div>
                <h2>${weather.cityName}</h2>
            </div>
            <div class="weather-temperature">
                ${weather.temperature.toFixed(1)}${temperatureSymbol}
            </div>
        </div>

        <p style="font-size: 1.1rem; color: var(--text-secondary); margin-bottom: 20px; text-align: center;">
            ${weather.description}
        </p>

        <div class="weather-details">
            <div class="detail-item">
                <div class="detail-icon">🌡️</div>
                <div class="detail-label">Temperatura Aparente</div>
                <div class="detail-value">${weather.apparentTemperature.toFixed(1)}${temperatureSymbol}</div>
            </div>

            <div class="detail-item">
                <div class="detail-icon">💧</div>
                <div class="detail-label">Humedad</div>
                <div class="detail-value">${weather.humidity}%</div>
            </div>

            <div class="detail-item">
                <div class="detail-icon">💨</div>
                <div class="detail-label">Velocidad del Viento</div>
                <div class="detail-value">${windSpeed.toFixed(1)} km/h</div>
            </div>

            <div class="detail-item">
                <div class="detail-icon">🌧️</div>
                <div class="detail-label">Precipitación</div>
                <div class="detail-value">${precipitation.toFixed(1)} mm</div>
            </div>
        </div>
    `;

    container.appendChild(card);
}

/* ========================================
   BÚSQUEDA DE MÚLTIPLES CIUDADES
   ======================================== */

function addMultipleCities() {
    const input = citiesInput.value.trim();

    if (!input) {
        showError('Por favor ingresa al menos una ciudad');
        return;
    }

    const cities = input.split(',').map((c) => c.trim()).filter((c) => c.length > 0);

    cities.forEach((city) => {
        if (!selectedCities.includes(city)) {
            selectedCities.push(city);
        }
    });

    citiesInput.value = '';
    renderCitiesList();
    showSuccess(`Se agregaron ${cities.length} ciudad(es)`);
}

/**
 * Renderizar lista de ciudades seleccionadas
 */
function renderCitiesList() {
    citiesList.innerHTML = '';

    if (selectedCities.length === 0) {
        compareBtn.style.display = 'none';
        return;
    }

    selectedCities.forEach((city) => {
        const tag = document.createElement('div');
        tag.className = 'city-tag';
        tag.innerHTML = `
            ${city}
            <span class="remove-city" data-city="${city}">✕</span>
        `;

        tag.querySelector('.remove-city').addEventListener('click', () => {
            selectedCities = selectedCities.filter((c) => c !== city);
            renderCitiesList();
        });

        citiesList.appendChild(tag);
    });

    compareBtn.style.display = selectedCities.length > 0 ? 'block' : 'none';
}

/**
 * Comparar múltiples ciudades
 */
async function compareMultipleCities() {
    if (selectedCities.length === 0) {
        showError('Por favor agrega al menos una ciudad');
        return;
    }

    setLoading(true);
    multipleResult.innerHTML = '';

    try {
        const citiesParam = selectedCities.join(',');
        const response = await fetch(
            `${API_BASE_URL}/weather/multiple?cities=${encodeURIComponent(citiesParam)}&unit=${currentTemperatureUnit}`
        );

        if (!response.ok) {
            throw new Error('Error al obtener los datos de múltiples ciudades');
        }

        const data = await response.json();
        displayComparativeWeather(data, multipleResult);
        showSuccess(`Comparativa obtenida para ${data.length} ciudades`);
    } catch (error) {
        showError(error.message);
        console.error('Error:', error);
    } finally {
        setLoading(false);
    }
}

/**
 * Mostrar tabla comparativa de múltiples ciudades
 */
function displayComparativeWeather(weatherList, container) {
    const temperatureSymbol = getTempSymbol(currentTemperatureUnit);

    const tableHTML = `
        <div class="comparison-table-container">
            <table class="comparison-table">
                <thead>
                    <tr>
                        <th>📍 Ciudad</th>
                        <th>🌡️ Temperatura</th>
                        <th>🌡️ Sensación</th>
                        <th>💧 Humedad</th>
                        <th>💨 Viento</th>
                        <th>🌧️ Precipitación</th>
                        <th>☁️ Clima</th>
                    </tr>
                </thead>
                <tbody>
                    ${weatherList
                        .map(
                            (w) => `
                        <tr>
                            <td class="city-name-cell">${w.cityName}</td>
                            <td class="weather-value">${w.temperature.toFixed(1)}${temperatureSymbol}</td>
                            <td>${w.apparentTemperature.toFixed(1)}${temperatureSymbol}</td>
                            <td>${w.humidity}%</td>
                            <td>${(w.windSpeed || 0).toFixed(1)} km/h</td>
                            <td>${(w.precipitation || 0).toFixed(1)} mm</td>
                            <td>${w.description}</td>
                        </tr>
                    `
                        )
                        .join('')}
                </tbody>
            </table>
        </div>

        <div class="comparative-analysis">
            <div class="analysis-title">📊 Análisis Comparativo</div>
            ${generateComparativeAnalysis(weatherList, temperatureSymbol)}
        </div>
    `;

    container.innerHTML = tableHTML;
}

/**
 * Generar análisis comparativo
 */
function generateComparativeAnalysis(weatherList, tempSymbol) {
    if (weatherList.length < 2) {
        return '<p style="color: var(--text-secondary);">Necesitas al menos 2 ciudades para análisis</p>';
    }

    // Encontrar ciudades con temperaturas extremas
    const hottest = weatherList.reduce((prev, current) =>
        prev.temperature > current.temperature ? prev : current
    );
    const coldest = weatherList.reduce((prev, current) =>
        prev.temperature < current.temperature ? prev : current
    );
    const most_humid = weatherList.reduce((prev, current) =>
        prev.humidity > current.humidity ? prev : current
    );
    const windiest = weatherList.reduce((prev, current) =>
        (prev.windSpeed || 0) > (current.windSpeed || 0) ? prev : current
    );
    const rainiest = weatherList.reduce((prev, current) =>
        (prev.precipitation || 0) > (current.precipitation || 0) ? prev : current
    );

    const avgTemp = (weatherList.reduce((sum, w) => sum + w.temperature, 0) / weatherList.length).toFixed(1);

    return `
        <div class="analysis-item">
            <span class="analysis-label">🔥 Más caluroso</span>
            <span class="analysis-value">${hottest.cityName}: ${hottest.temperature.toFixed(1)}${tempSymbol}</span>
        </div>
        <div class="analysis-item">
            <span class="analysis-label">❄️ Más frío</span>
            <span class="analysis-value">${coldest.cityName}: ${coldest.temperature.toFixed(1)}${tempSymbol}</span>
        </div>
        <div class="analysis-item">
            <span class="analysis-label">💧 Más húmedo</span>
            <span class="analysis-value">${most_humid.cityName}: ${most_humid.humidity}%</span>
        </div>
        <div class="analysis-item">
            <span class="analysis-label">💨 Más ventoso</span>
            <span class="analysis-value">${windiest.cityName}: ${(windiest.windSpeed || 0).toFixed(1)} km/h</span>
        </div>
        <div class="analysis-item">
            <span class="analysis-label">🌧️ Más lluvia</span>
            <span class="analysis-value">${rainiest.cityName}: ${(rainiest.precipitation || 0).toFixed(1)} mm</span>
        </div>
        <div class="analysis-item">
            <span class="analysis-label">📊 Temperatura Media</span>
            <span class="analysis-value">${avgTemp}${tempSymbol}</span>
        </div>
    `;
}

/* ========================================
   PRONÓSTICO DE 5 DÍAS
   ======================================== */

async function getForecast() {
    const city = forecastCityInput.value.trim();

    if (!city) {
        showError('Por favor ingresa el nombre de una ciudad para el pronóstico');
        return;
    }

    setLoading(true);
    forecastResult.innerHTML = '';

    try {
        const response = await fetch(
            `${API_BASE_URL}/weather/forecast?city=${encodeURIComponent(city)}&unit=${currentTemperatureUnit}`
        );

        if (!response.ok) {
            if (response.status === 404) {
                throw new Error(`Ciudad no encontrada: ${city}`);
            }
            throw new Error('Error al obtener pronóstico');
        }

        const data = await response.json();
        displayForecast(data, forecastResult);
        forecastCityInput.value = '';
        showSuccess(`Pronóstico obtenido para ${city}`);
    } catch (error) {
        showError(error.message);
        console.error('Error:', error);
    } finally {
        setLoading(false);
    }
}

/**
 * Mostrar pronóstico de 5 días
 */
function displayForecast(forecastData, container) {
    const temperatureSymbol = getTempSymbol(currentTemperatureUnit);

    const daysHTML = forecastData.days
        .map(
            (day) => `
        <div class="forecast-day">
            <div class="forecast-date">${formatDate(day.date)}</div>
            <div class="forecast-icon">${getWeatherIcon(day.description)}</div>
            <div class="forecast-temp">
                <div class="forecast-temp-label">Max</div>
                <div class="forecast-temp-value">${day.maxTemp.toFixed(1)}${temperatureSymbol}</div>
            </div>
            <div class="forecast-temp" style="margin-top: 10px;">
                <div class="forecast-temp-label">Min</div>
                <div class="forecast-temp-value">${day.minTemp.toFixed(1)}${temperatureSymbol}</div>
            </div>
            <div style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 10px;">
                💧 ${day.humidity}%
            </div>
        </div>
    `
        )
        .join('');

    container.innerHTML = daysHTML;
}

/* ========================================
   FUNCIONES AUXILIARES
   ======================================== */

/**
 * Obtener símbolo de temperatura
 */
function getTempSymbol(unit) {
    switch (unit) {
        case 'fahrenheit':
            return '°F';
        case 'kelvin':
            return 'K';
        default:
            return '°C';
    }
}

/**
 * Obtener emoji según descripción del clima
 */
function getWeatherIcon(description) {
    const desc = description.toLowerCase();

    if (desc.includes('despejado') || desc.includes('clear') || desc.includes('sunny')) return '☀️';
    if (desc.includes('nublado') || desc.includes('cloudy')) return '☁️';
    if (desc.includes('parcialmente')) return '🌤️';
    if (desc.includes('lluvia') || desc.includes('rain')) return '🌧️';
    if (desc.includes('tormenta') || desc.includes('storm')) return '⛈️';
    if (desc.includes('nieve') || desc.includes('snow')) return '❄️';
    if (desc.includes('niebla') || desc.includes('fog')) return '🌫️';
    if (desc.includes('viento') || desc.includes('windy')) return '💨';
    if (desc.includes('granizo') || desc.includes('hail')) return '🌨️';
    if (desc.includes('relámpago') || desc.includes('thunder')) return '⚡';

    return '🌡️';
}

/**
 * Formatear fecha para pronóstico
 */
function formatDate(dateString) {
    const options = { month: 'short', day: 'numeric' };
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', options);
}

/**
 * Convertir temperatura entre unidades
 */
function convertTemperature(celsius, targetUnit) {
    switch (targetUnit) {
        case 'fahrenheit':
            return (celsius * 9) / 5 + 32;
        case 'kelvin':
            return celsius + 273.15;
        default:
            return celsius;
    }
}

/* ========================================
   INICIALIZACIÓN
   ======================================== */

document.addEventListener('DOMContentLoaded', () => {
    updateModeDisplay();
    console.log('✅ Weather App iniciada correctamente');
    console.log(`API Base URL: ${API_BASE_URL}`);
});
