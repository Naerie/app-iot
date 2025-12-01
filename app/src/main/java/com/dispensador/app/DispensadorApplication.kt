package com.dispensador.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

/**
 * Application class para inicializar Firebase
 * Esta clase se ejecuta ANTES que cualquier Activity
 */
class DispensadorApplication : Application() {

    private val TAG = "DispensadorApp"

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

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar Firebase", e)
            Log.e(TAG, "Verifica que google-services.json esté en app/")
        }
    }
}
