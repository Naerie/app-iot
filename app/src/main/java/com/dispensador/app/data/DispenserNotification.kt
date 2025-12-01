package com.dispensador.app.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo unificado de notificaciones del dispensador
 * Gestiona alertas y avisos del sistema
 */
data class DispenserNotification(
    val nivelBajo: Boolean = false,
    val contenidoVacio: Boolean = false,
    val errorSistema: Boolean = false,
    val desconexion: Boolean = false,
    val mensaje: String = "",
    val timestamp: Long = 0L
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this(
        nivelBajo = false,
        contenidoVacio = false,
        errorSistema = false,
        desconexion = false,
        mensaje = "",
        timestamp = 0L
    )

    /**
     * Verifica si hay alguna notificación activa
     */
    fun tieneNotificaciones(): Boolean {
        return nivelBajo || contenidoVacio || errorSistema || desconexion
    }

    /**
     * Obtiene el número total de notificaciones activas
     */
    fun contarNotificaciones(): Int {
        var count = 0
        if (nivelBajo) count++
        if (contenidoVacio) count++
        if (errorSistema) count++
        if (desconexion) count++
        return count
    }

    /**
     * Obtiene la prioridad de la notificación (mayor número = mayor prioridad)
     */
    fun obtenerPrioridad(): Int {
        return when {
            contenidoVacio -> 4
            errorSistema -> 3
            desconexion -> 2
            nivelBajo -> 1
            else -> 0
        }
    }

    /**
     * Obtiene el título de la notificación más importante
     */
    fun obtenerTitulo(): String {
        return when {
            contenidoVacio -> "Contenedor Vacío"
            errorSistema -> "Error del Sistema"
            desconexion -> "Dispositivo Desconectado"
            nivelBajo -> "Nivel Bajo de Agua"
            else -> "Sin Notificaciones"
        }
    }

    /**
     * Obtiene el mensaje de la notificación más importante
     */
    fun obtenerMensaje(): String {
        if (mensaje.isNotEmpty()) return mensaje
        
        return when {
            contenidoVacio -> "El contenedor de agua está vacío. Por favor, rellénelo."
            errorSistema -> "Se ha detectado un error en el sistema. Revise el dispositivo."
            desconexion -> "El dispositivo se ha desconectado. Verifique la conexión."
            nivelBajo -> "El nivel de agua está bajo (menos del 20%)."
            else -> "No hay notificaciones pendientes."
        }
    }

    /**
     * Obtiene el color del indicador según la prioridad
     */
    fun obtenerColor(): Long {
        return when {
            contenidoVacio || errorSistema -> 0xFFF44336 // Rojo
            desconexion -> 0xFFFF9800 // Naranja
            nivelBajo -> 0xFFFFEB3B // Amarillo
            else -> 0xFF4CAF50 // Verde
        }
    }

    /**
     * Obtiene la fecha y hora formateadas
     */
    fun obtenerFechaHora(): String {
        if (timestamp == 0L) return ""
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formato.format(Date(timestamp))
    }

    /**
     * Obtiene lista de todas las notificaciones activas
     */
    fun obtenerListaNotificaciones(): List<String> {
        val lista = mutableListOf<String>()
        if (contenidoVacio) lista.add("Contenedor vacío")
        if (errorSistema) lista.add("Error del sistema")
        if (desconexion) lista.add("Dispositivo desconectado")
        if (nivelBajo) lista.add("Nivel bajo de agua")
        return lista
    }

    companion object {
        /**
         * Crea una notificación vacía (sin alertas)
         */
        fun vacia(): DispenserNotification {
            return DispenserNotification(
                nivelBajo = false,
                contenidoVacio = false,
                errorSistema = false,
                desconexion = false,
                mensaje = "",
                timestamp = System.currentTimeMillis()
            )
        }
    }
}
