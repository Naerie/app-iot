package com.dispensador.app.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo unificado del historial de dispensaciones
 * Registra todas las operaciones de dispensado
 */
data class DispenserHistory(
    val id: String = "",
    val timestamp: Long = 0L,
    val cantidad: Int = 0,
    val tipo: String = "", // "manual", "programado"
    val usuario: String = "",
    val exitoso: Boolean = true
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this(
        id = "",
        timestamp = 0L,
        cantidad = 0,
        tipo = "",
        usuario = "",
        exitoso = true
    )

    /**
     * Obtiene la fecha formateada
     */
    fun obtenerFecha(): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(Date(timestamp))
    }

    /**
     * Obtiene la hora formateada
     */
    fun obtenerHora(): String {
        val formato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formato.format(Date(timestamp))
    }

    /**
     * Obtiene fecha y hora formateadas
     */
    fun obtenerFechaHora(): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formato.format(Date(timestamp))
    }

    /**
     * Obtiene el tipo de dispensación en formato legible
     */
    fun obtenerTipoLegible(): String {
        return when (tipo.lowercase()) {
            "manual" -> "Manual"
            "programado" -> "Programado"
            else -> "Desconocido"
        }
    }

    /**
     * Obtiene el estado de la operación
     */
    fun obtenerEstado(): String = if (exitoso) "Exitoso" else "Fallido"

    /**
     * Obtiene el color del indicador según el estado
     */
    fun obtenerColorEstado(): Long {
        return if (exitoso) 0xFF4CAF50 else 0xFFF44336
    }

    /**
     * Obtiene descripción completa de la dispensación
     */
    fun obtenerDescripcion(): String {
        return "${obtenerFechaHora()} - $cantidad ml - ${obtenerTipoLegible()}"
    }

    companion object {
        const val TIPO_MANUAL = "manual"
        const val TIPO_PROGRAMADO = "programado"
    }
}
