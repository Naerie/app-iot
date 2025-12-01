package com.dispensador.app.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Modelo unificado de horarios programados
 * Permite programar dispensaciones automáticas
 */
data class DispenserSchedule(
    val id: String = "",
    val hora: String = "08:00",
    val cantidad: Int = 250,
    val dias: List<Int> = listOf(), // 0=Domingo, 1=Lunes, ..., 6=Sábado
    val fechaEspecifica: Long = 0L, // 0 = no hay fecha específica, usa días de la semana
    val activo: Boolean = true,
    val fechaCreacion: Long = 0L
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this(
        id = "",
        hora = "08:00",
        cantidad = 250,
        dias = listOf(),
        fechaEspecifica = 0L,
        activo = true,
        fechaCreacion = 0L
    )

    /**
     * Valida que el horario tenga formato correcto HH:mm
     */
    fun validarHorario(): Boolean {
        val regex = Regex("^([0-1][0-9]|2[0-3]):[0-5][0-9]$")
        return regex.matches(hora)
    }

    /**
     * Valida que la cantidad esté en el rango permitido
     */
    fun validarCantidad(): Boolean = cantidad in 50..500

    /**
     * Verifica si el horario es válido para ejecutarse
     */
    fun esValido(): Boolean = validarHorario() && validarCantidad() && 
        (dias.isNotEmpty() || fechaEspecifica > 0)

    /**
     * Obtiene los nombres de los días seleccionados
     */
    fun obtenerNombresDias(): String {
        if (fechaEspecifica > 0) {
            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return formato.format(Date(fechaEspecifica))
        }
        
        val nombresDias = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
        return dias.sorted().joinToString(", ") { nombresDias.getOrNull(it) ?: "" }
    }

    /**
     * Verifica si el horario debe ejecutarse hoy
     */
    fun debeEjecutarseHoy(): Boolean {
        if (!activo) return false
        
        val hoy = Calendar.getInstance()
        val diaActual = hoy.get(Calendar.DAY_OF_WEEK) - 1 // Ajustar para que Domingo = 0
        
        return if (fechaEspecifica > 0) {
            val fechaHorario = Calendar.getInstance().apply { timeInMillis = fechaEspecifica }
            hoy.get(Calendar.YEAR) == fechaHorario.get(Calendar.YEAR) &&
            hoy.get(Calendar.DAY_OF_YEAR) == fechaHorario.get(Calendar.DAY_OF_YEAR)
        } else {
            dias.contains(diaActual)
        }
    }

    /**
     * Obtiene descripción completa del horario
     */
    fun obtenerDescripcion(): String {
        val estado = if (activo) "Activo" else "Inactivo"
        val dias = obtenerNombresDias()
        return "$hora - $cantidad ml - $dias ($estado)"
    }

    companion object {
        // Días de la semana para selección
        val DIAS_SEMANA = listOf(
            0 to "Domingo",
            1 to "Lunes",
            2 to "Martes",
            3 to "Miércoles",
            4 to "Jueves",
            5 to "Viernes",
            6 to "Sábado"
        )
    }
}
