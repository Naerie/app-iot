package com.dispensador.app.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de autenticación con Firebase Auth
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val TAG = "AuthRepository"

    /**
     * Observa el estado de autenticación del usuario
     */
    fun observarEstadoAuth(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }

        auth.addAuthStateListener(listener)
        
        // Enviar estado inicial
        trySend(auth.currentUser)

        awaitClose {
            auth.removeAuthStateListener(listener)
            Log.d(TAG, "Listener de auth eliminado")
        }
    }

    /**
     * Obtiene el usuario actual
     */
    fun obtenerUsuarioActual(): FirebaseUser? = auth.currentUser

    /**
     * Verifica si hay un usuario autenticado
     */
    fun estaAutenticado(): Boolean = auth.currentUser != null

    /**
     * Inicia sesión con email y contraseña
     */
    suspend fun iniciarSesion(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Usuario no encontrado")
            Log.d(TAG, "Inicio de sesión exitoso: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error en inicio de sesión", e)
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo usuario con email y contraseña
     */
    suspend fun registrarUsuario(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Error creando usuario")
            Log.d(TAG, "Registro exitoso: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro", e)
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesión del usuario actual
     */
    fun cerrarSesion() {
        auth.signOut()
        Log.d(TAG, "Sesión cerrada")
    }

    /**
     * Envía email de recuperación de contraseña
     */
    suspend fun recuperarPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Email de recuperación enviado a: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando email de recuperación", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el email del usuario
     */
    suspend fun actualizarEmail(nuevoEmail: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
            user.updateEmail(nuevoEmail).await()
            Log.d(TAG, "Email actualizado a: $nuevoEmail")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando email", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza la contraseña del usuario
     */
    suspend fun actualizarPassword(nuevaPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
            user.updatePassword(nuevaPassword).await()
            Log.d(TAG, "Contraseña actualizada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando contraseña", e)
            Result.failure(e)
        }
    }
}
