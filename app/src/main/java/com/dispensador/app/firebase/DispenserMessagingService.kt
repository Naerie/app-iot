package com.dispensador.app.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dispensador.app.DispensadorApplication
import com.dispensador.app.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio para manejar notificaciones push de Firebase Cloud Messaging
 */
class DispenserMessagingService : FirebaseMessagingService() {

    private val TAG = "FCMService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo FCM token: $token")
        
        // Aquí podrías enviar el token a tu servidor si lo necesitas
        // Por ahora solo lo registramos en el log
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        // Verificar si el mensaje contiene datos
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${message.data}")
            handleDataMessage(message.data)
        }

        // Verificar si el mensaje contiene notificación
        message.notification?.let {
            Log.d(TAG, "Título: ${it.title}, Cuerpo: ${it.body}")
            mostrarNotificacion(it.title ?: "Dispensador", it.body ?: "")
        }
    }

    /**
     * Maneja mensajes de datos personalizados
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val tipo = data["tipo"] ?: return
        val mensaje = data["mensaje"] ?: "Notificación del dispensador"
        
        when (tipo) {
            "nivel_bajo" -> {
                mostrarNotificacion(
                    "⚠️ Nivel de Agua Bajo",
                    "El nivel de agua está por debajo del 20%. Por favor, rellena el contenedor."
                )
            }
            "conexion_perdida" -> {
                mostrarNotificacion(
                    "❌ Conexión Perdida",
                    "El dispensador ha perdido la conexión. Verifica el WiFi."
                )
            }
            "dispensado_completado" -> {
                val cantidad = data["cantidad"] ?: "0"
                mostrarNotificacion(
                    "✅ Dispensado Completado",
                    "Se dispensaron $cantidad ml de agua correctamente."
                )
            }
            else -> {
                mostrarNotificacion("Dispensador", mensaje)
            }
        }
    }

    /**
     * Muestra una notificación local
     */
    private fun mostrarNotificacion(titulo: String, mensaje: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, DispensadorApplication.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        
        Log.d(TAG, "Notificación mostrada: $titulo")
    }
}
