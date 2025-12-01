package com.dispensador.app.data

/**
 * Modelo unificado de control del dispensador
 * Gestiona comandos y configuración de dispensado
 */
data class DispenserControl(
    val motorEncendido: Boolean = false,
    val cantidadDispensado: Int = 250,
    val modoAutomatico: Boolean = false,
    val programacionActiva: Boolean = false,
    val ultimaInstruccion: Long = 0L
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this(
        motorEncendido = false,
        cantidadDispensado = 250,
        modoAutomatico = false,
        programacionActiva = false,
        ultimaInstruccion = 0L
    )

    /**
     * Valida que la cantidad de dispensado esté en el rango permitido
     */
    fun validarCantidad(): Boolean = cantidadDispensado in 50..500

    /**
     * Obtiene el modo de operación en formato legible
     */
    fun obtenerModo(): String = if (modoAutomatico) "Automático" else "Manual"

    /**
     * Verifica si la programación está activa y el modo es automático
     */
    fun programacionHabilitada(): Boolean = modoAutomatico && programacionActiva

    companion object {
        const val CANTIDAD_MIN = 50
        const val CANTIDAD_MAX = 500
        const val CANTIDAD_DEFAULT = 250
        
        // Cantidades rápidas predefinidas
        val CANTIDADES_RAPIDAS = listOf(100, 250, 500)
    }
}
