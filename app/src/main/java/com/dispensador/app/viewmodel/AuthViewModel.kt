package com.dispensador.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dispensador.app.firebase.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de autenticación
 */
class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        observeAuthState()
    }

    /**
     * Observa el estado de autenticación
     */
    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.observarEstadoAuth().collect { user ->
                _currentUser.value = user
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    /**
     * Inicia sesión con email y contraseña
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.iniciarSesion(email, password)
            
            result.onSuccess { user ->
                _authState.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _authState.value = AuthState.Error(
                    error.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    /**
     * Registra un nuevo usuario
     */
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.registrarUsuario(email, password)
            
            result.onSuccess { user ->
                _authState.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _authState.value = AuthState.Error(
                    error.message ?: "Error al registrar usuario"
                )
            }
        }
    }

    /**
     * Cierra la sesión del usuario
     */
    fun signOut() {
        authRepository.cerrarSesion()
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Envía email de recuperación de contraseña
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.recuperarPassword(email)
            
            result.onSuccess {
                _authState.value = AuthState.PasswordResetSent
            }.onFailure { error ->
                _authState.value = AuthState.Error(
                    error.message ?: "Error al enviar email de recuperación"
                )
            }
        }
    }

    /**
     * Limpia el estado de error
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Obtiene el email del usuario actual
     */
    fun getCurrentUserEmail(): String? {
        return _currentUser.value?.email
    }

    /**
     * Verifica si el usuario está autenticado
     */
    fun isAuthenticated(): Boolean {
        return authRepository.estaAutenticado()
    }
}

/**
 * Estados de autenticación
 */
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetSent : AuthState()
}
