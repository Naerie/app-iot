package com.dispensador.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Application class para inicializar Firebase y notificaciones
 * Esta clase se ejecuta ANTES que cualquier Activity
 */
class DispensadorApplication : Application() {

    private val TAG = "DispensadorApp"

    companion object {
        const val CHANNEL_ID = "dispensador_notifications"
        const val CHANNEL_NAME = "Notificaciones del Dispensador"
        const val CHANNEL_DESCRIPTION = "Alertas sobre el estado del dispensador"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Inicializando aplicación...")

        // Inicializar Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "✅ Firebase inicializado correctamente")

            // Habilitar persistencia offline (opcional pero recomendado)
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true)
                Log.d(TAG, "✅ Persistencia offline habilitada")
            } catch (e: Exception) {
                Log.w(TAG, "Persistencia offline ya estaba habilitada o no se pudo habilitar")
            }

            // Configurar Firebase Cloud Messaging
            configurarFirebaseMessaging()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar Firebase", e)
            Log.e(TAG, "Verifica que google-services.json esté en app/")
        }

        // Crear canal de notificaciones
        crearCanalNotificaciones()
    }

    /**
     * Configura Firebase Cloud Messaging para notificaciones push
     */
    private fun configurarFirebaseMessaging() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Error al obtener FCM token", task.exception)
                    return@addOnCompleteListener
                }

                // Obtener token FCM
                val token = task.result
                Log.d(TAG, "✅ FCM Token: $token")
                
                // Suscribirse a tópicos para recibir notificaciones
                FirebaseMessaging.getInstance().subscribeToTopic("dispensador_alerts")
                    .addOnCompleteListener { subscribeTask ->
                        if (subscribeTask.isSuccessful) {
                            Log.d(TAG, "✅ Suscrito a notificaciones del dispensador")
                        } else {
                            Log.w(TAG, "Error al suscribirse a notificaciones", subscribeTask.exception)
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar FCM", e)
        }
    }

    /**
     * Crea el canal de notificaciones para Android 8.0+
     */
    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            
            Log.d(TAG, "✅ Canal de notificaciones creado")
        }
    }
}
