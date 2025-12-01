package com.dispensador.app.data

/**
 * Modelo de datos para notificaciones del dispensador
 */
data class DispenserNotification(
    val nivelBajo: Boolean = false,
    val contenidoVacio: Boolean = false,
    val errorSistema: Boolean = false,
    val desconexion: Boolean = false,
    val mensaje: String = "",
    val timestamp: Long = 0
) {
    companion object {
        /**
         * Crea una notificación vacía
         */
        fun crearVacia(): DispenserNotification {
            return DispenserNotification(
                nivelBajo = false,
                contenidoVacio = false,
                errorSistema = false,
                desconexion = false,
                mensaje = "",
                timestamp = 0
            )
        }
    }

    /**
     * Cuenta el número de notificaciones activas
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
     * Verifica si hay alguna notificación activa
     */
    fun tieneNotificaciones(): Boolean {
        return nivelBajo || contenidoVacio || errorSistema || desconexion
    }

    /**
     * Obtiene el mensaje de la notificación más importante
     */
    fun obtenerMensajePrincipal(): String {
        return when {
            contenidoVacio -> "Contenido vacío"
            errorSistema -> "Error en el sistema"
            desconexion -> "Dispositivo desconectado"
            nivelBajo -> "Nivel bajo de agua"
            mensaje.isNotEmpty() -> mensaje
            else -> "Sin notificaciones"
        }
    }

    /**
     * Obtiene una lista de notificaciones activas para mostrar en la UI
     */
    fun obtenerListaNotificaciones(): List<NotificationItem> {
        val lista = mutableListOf<NotificationItem>()
        
        if (contenidoVacio) {
            lista.add(
                NotificationItem(
                    id = "contenido_vacio",
                    titulo = "Contenido Vacío",
                    mensaje = "El dispensador está vacío. Por favor, recarga el contenido.",
                    tipo = NotificationType.ERROR,
                    timestamp = timestamp
                )
            )
        }
        
        if (errorSistema) {
            lista.add(
                NotificationItem(
                    id = "error_sistema",
                    titulo = "Error del Sistema",
                    mensaje = mensaje.ifEmpty { "Se ha detectado un error en el sistema." },
                    tipo = NotificationType.ERROR,
                    timestamp = timestamp
                )
            )
        }
        
        if (desconexion) {
            lista.add(
                NotificationItem(
                    id = "desconexion",
                    titulo = "Dispositivo Desconectado",
                    mensaje = "El dispensador ha perdido la conexión.",
                    tipo = NotificationType.WARNING,
                    timestamp = timestamp
                )
            )
        }
        
        if (nivelBajo) {
            lista.add(
                NotificationItem(
                    id = "nivel_bajo",
                    titulo = "Nivel Bajo de Agua",
                    mensaje = "El nivel de agua está por debajo del 20%.",
                    tipo = NotificationType.INFO,
                    timestamp = timestamp
                )
            )
        }
        
        // Si no hay notificaciones específicas pero hay un mensaje
        if (lista.isEmpty() && mensaje.isNotEmpty()) {
            lista.add(
                NotificationItem(
                    id = "mensaje_general",
                    titulo = "Notificación",
                    mensaje = mensaje,
                    tipo = NotificationType.INFO,
                    timestamp = timestamp
                )
            )
        }
        
        return lista
    }
}

/**
 * Modelo para items individuales de notificación en la UI
 */
data class NotificationItem(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val tipo: NotificationType,
    val timestamp: Long
)

/**
 * Tipos de notificación
 */
enum class NotificationType {
    INFO,
    WARNING,
    ERROR
}
