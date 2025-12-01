package com.dispensador.app.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository para autenticación con Firebase
 * Versión robusta que no crashea si Firebase no está configurado
 */
class AuthRepository {

    private val TAG = "AuthRepository"

    // Lazy initialization para evitar crashes
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener FirebaseAuth", e)
            null
        }
    }

    /**
     * Verifica si Firebase Auth está disponible
     */
    fun isInitialized(): Boolean {
        return auth != null
    }

    /**
     * Obtiene el usuario actual
     */
    fun getCurrentUser(): FirebaseUser? {
        return try {
            auth?.currentUser
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario actual", e)
            null
        }
    }

    /**
     * Observa cambios en el estado de autenticación
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }

        firebaseAuth.addAuthStateListener(listener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    /**
     * Inicia sesión con email y contraseña
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            return Result.failure(Exception("Firebase Auth no está inicializado"))
        }

        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Log.d(TAG, "Login exitoso: ${user.email}")
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login", e)
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo usuario
     */
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            return Result.failure(Exception("Firebase Auth no está inicializado"))
        }

        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Log.d(TAG, "Registro exitoso: ${user.email}")
                Result.success(user)
            } else {
                Result.failure(Exception("Error al crear usuario"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro", e)
            Result.failure(e)
        }
    }

    /**
     * Cierra sesión
     */
    fun signOut() {
        try {
            auth?.signOut()
            Log.d(TAG, "Sesión cerrada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
        }
    }

    /**
     * Envía email de recuperación de contraseña
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            return Result.failure(Exception("Firebase Auth no está inicializado"))
        }

        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Email de recuperación enviado a: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar email de recuperación", e)
            Result.failure(e)
        }
    }
}