package com.dispensador.app.data

/**
 * Modelo unificado del estado del dispensador
 * Combina información de conexión, estado operativo y niveles
 */
data class DispenserState(
    val conexion: Boolean = true,
    val encendido: Boolean = true,
    val enUso: Boolean = false,
    val nivelAgua: Int = 20,
    val tazaPresente: Boolean = true,
    val intensidadWifi: Int = 0,
    val ultimaActualizacion: Long = 0L,
    val modo: String = "manual" // "manual" o "automatico"
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this(
        conexion = true,
        encendido = true,
        enUso = false,
        nivelAgua = 20,
        tazaPresente = true,
        intensidadWifi = 0,
        ultimaActualizacion = 0L,
        modo = "manual"
    )

    /**
     * Obtiene el estado legible del dispositivo
     */
    fun obtenerEstado(): String {
        return when {
            !conexion -> "Desconectado"
            enUso -> "En Uso"
            encendido -> "Encendido"
            else -> "Apagado"
        }
    }

    /**
     * Verifica si el nivel de agua está bajo (< 20%)
     */
    fun nivelAguaBajo(): Boolean = nivelAgua < 20

    /**
     * Verifica si el contenedor está vacío
     */
    fun contenedorVacio(): Boolean = nivelAgua <= 0

    /**
     * Obtiene la intensidad de señal WiFi en formato legible
     */
    fun obtenerIntensidadWifi(): String {
        return when {
            !conexion -> "Sin conexión"
            intensidadWifi >= -50 -> "Excelente"
            intensidadWifi >= -60 -> "Buena"
            intensidadWifi >= -70 -> "Regular"
            else -> "Débil"
        }
    }

    /**
     * Verifica si el dispositivo está listo para dispensar
     */
    fun puedeDispensar(): Boolean {
        return conexion && !enUso && !contenedorVacio() && tazaPresente
    }

    /**
     * Obtiene el color del indicador de WiFi
     */
    fun obtenerColorWifi(): Long {
        return when {
            !conexion -> 0xFFCCCCCC
            intensidadWifi >= -60 -> 0xFF4CAF50 // Verde
            intensidadWifi >= -70 -> 0xFFFF9800 // Naranja
            else -> 0xFFF44336 // Rojo
        }
    }
}
