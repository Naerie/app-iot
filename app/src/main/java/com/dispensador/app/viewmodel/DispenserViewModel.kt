package com.dispensador.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dispensador.app.data.*
import com.dispensador.app.firebase.FirebaseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel principal para gestionar el estado del dispensador
 * Versión con manejo robusto de errores que no crashea
 */
class DispenserViewModel : ViewModel() {

    private val TAG = "DispenserViewModel"

    // Repository se inicializa de forma segura
    private val repository = FirebaseRepository()

    // Estados observables
    private val _estado = MutableStateFlow<DispenserState?>(null)
    val estado: StateFlow<DispenserState?> = _estado.asStateFlow()

    private val _control = MutableStateFlow<DispenserControl?>(null)
    val control: StateFlow<DispenserControl?> = _control.asStateFlow()

    private val _notificaciones = MutableStateFlow<DispenserNotification?>(null)
    val notificaciones: StateFlow<DispenserNotification?> = _notificaciones.asStateFlow()

    private val _horarios = MutableStateFlow<List<DispenserSchedule>>(emptyList())
    val horarios: StateFlow<List<DispenserSchedule>> = _horarios.asStateFlow()

    private val _historial = MutableStateFlow<List<DispenserHistory>>(emptyList())
    val historial: StateFlow<List<DispenserHistory>> = _historial.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private val _firebaseInitialized = MutableStateFlow(false)
    val firebaseInitialized: StateFlow<Boolean> = _firebaseInitialized.asStateFlow()

    init {
        Log.d(TAG, "DispenserViewModel inicializado")
        verificarFirebase()
        observarDatos()
    }

    /**
     * Verifica si Firebase está inicializado
     */
    private fun verificarFirebase() {
        try {
            val initialized = repository.isInitialized()
            _firebaseInitialized.value = initialized

            if (!initialized) {
                Log.e(TAG, "Firebase no está inicializado correctamente")
                _errorMessage.value = "Firebase no está configurado. Verifica google-services.json"
            } else {
                Log.d(TAG, "Firebase inicializado correctamente")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar Firebase", e)
            _firebaseInitialized.value = false
            _errorMessage.value = "Error al conectar con Firebase: ${e.message}"
        }
    }

    /**
     * Observar todos los datos de Firebase en tiempo real
     */
    private fun observarDatos() {
        // Observar estado
        viewModelScope.launch {
            try {
                repository.observarEstado().collect { estado ->
                    _estado.value = estado
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observando estado", e)
                _errorMessage.value = "Error al observar estado: ${e.message}"
                _isLoading.value = false
            }
        }

        // Observar control
        viewModelScope.launch {
            try {
                repository.observarControl().collect { control ->
                    _control.value = control
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observando control", e)
                _errorMessage.value = "Error al observar control: ${e.message}"
            }
        }

        // Observar notificaciones
        viewModelScope.launch {
            try {
                repository.observarNotificaciones().collect { notificaciones ->
                    _notificaciones.value = notificaciones
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observando notificaciones", e)
                _errorMessage.value = "Error al observar notificaciones: ${e.message}"
            }
        }

        // Observar horarios
        viewModelScope.launch {
            try {
                repository.observarHorarios().collect { horarios ->
                    _horarios.value = horarios
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observando horarios", e)
                _errorMessage.value = "Error al observar horarios: ${e.message}"
            }
        }

        // Observar historial
        viewModelScope.launch {
            try {
                repository.observarHistorial().collect { historial ->
                    _historial.value = historial
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observando historial", e)
                _errorMessage.value = "Error al observar historial: ${e.message}"
            }
        }
    }

    /**
     * Dispensar agua manualmente
     */
    fun dispensarManual(cantidad: Int) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                val result = repository.dispensarManual(cantidad)
                result.onSuccess {
                    _operationState.value = OperationState.Success("Dispensando $cantidad ml")
                }.onFailure { error ->
                    _operationState.value = OperationState.Error(error.message ?: "Error al dispensar")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en dispensarManual", e)
                _operationState.value = OperationState.Error(e.message ?: "Error al dispensar")
            }
        }
    }

    /**
     * Detener el motor
     */
    fun detenerMotor() {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                val result = repository.detenerMotor()
                result.onSuccess {
                    _operationState.value = OperationState.Success("Motor detenido")
                }.onFailure { error ->
                    _operationState.value = OperationState.Error(error.message ?: "Error al detener motor")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en detenerMotor", e)
                _operationState.value = OperationState.Error(e.message ?: "Error al detener motor")
            }
        }
    }

    /**
     * Cambiar modo manual/automático
     */
    fun cambiarModo(automatico: Boolean) {
        viewModelScope.launch {
            try {
                val result = repository.cambiarModo(automatico)
                result.onFailure { error ->
                    _errorMessage.value = error.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en cambiarModo", e)
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Activar/desactivar programación
     */
    fun toggleProgramacion(activa: Boolean) {
        viewModelScope.launch {
            try {
                val result = repository.toggleProgramacion(activa)
                result.onFailure { error ->
                    _errorMessage.value = error.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en toggleProgramacion", e)
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Agregar horario
     */
    fun agregarHorario(horario: DispenserSchedule) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                val result = repository.agregarHorario(horario)
                result.onSuccess {
                    _operationState.value = OperationState.Success("Horario agregado")
                }.onFailure { error ->
                    _operationState.value = OperationState.Error(error.message ?: "Error al agregar horario")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en agregarHorario", e)
                _operationState.value = OperationState.Error(e.message ?: "Error al agregar horario")
            }
        }
    }

    /**
     * Actualizar horario
     */
    fun actualizarHorario(horario: DispenserSchedule) {
        viewModelScope.launch {
            try {
                val result = repository.actualizarHorario(horario)
                result.onFailure { error ->
                    _errorMessage.value = error.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en actualizarHorario", e)
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Eliminar horario
     */
    fun eliminarHorario(horarioId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                val result = repository.eliminarHorario(horarioId)
                result.onSuccess {
                    _operationState.value = OperationState.Success("Horario eliminado")
                }.onFailure { error ->
                    _operationState.value = OperationState.Error(error.message ?: "Error al eliminar horario")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en eliminarHorario", e)
                _operationState.value = OperationState.Error(e.message ?: "Error al eliminar horario")
            }
        }
    }

    /**
     * Limpiar notificaciones
     */
    fun limpiarNotificaciones() {
        viewModelScope.launch {
            try {
                val result = repository.limpiarNotificaciones()
                result.onFailure { error ->
                    _errorMessage.value = error.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en limpiarNotificaciones", e)
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Resetear estado de operación
     */
    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "DispenserViewModel destruido")
    }
}

/**
 * Estados de operación
 */
sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}
