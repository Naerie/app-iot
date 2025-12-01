package com.dispensador.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dispensador.app.data.*
import com.dispensador.app.firebase.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel principal unificado para el dispensador
 * Gestiona todo el estado y las operaciones del dispositivo
 */
class DispenserViewModel : ViewModel() {

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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    init {
        startMonitoring()
    }

    /**
     * Inicia el monitoreo de todos los datos en tiempo real
     */
    private fun startMonitoring() {
        // Monitorear estado del dispositivo
        viewModelScope.launch {
            repository.observarEstado().collect { result ->
                result.onSuccess { estado ->
                    _estado.value = estado
                    _isLoading.value = false
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message
                    _isLoading.value = false
                }
            }
        }

        // Monitorear control
        viewModelScope.launch {
            repository.observarControl().collect { result ->
                result.onSuccess { control ->
                    _control.value = control
                }.onFailure { error ->
                    _errorMessage.value = error.message
                }
            }
        }

        // Monitorear notificaciones
        viewModelScope.launch {
            repository.observarNotificaciones().collect { result ->
                result.onSuccess { notificacion ->
                    _notificaciones.value = notificacion
                }.onFailure { error ->
                    _errorMessage.value = error.message
                }
            }
        }

        // Monitorear horarios
        viewModelScope.launch {
            repository.observarHorarios().collect { result ->
                result.onSuccess { horarios ->
                    _horarios.value = horarios
                }.onFailure { error ->
                    _errorMessage.value = error.message
                }
            }
        }

        // Monitorear historial
        viewModelScope.launch {
            repository.observarHistorial(50).collect { result ->
                result.onSuccess { historial ->
                    _historial.value = historial
                }.onFailure { error ->
                    _errorMessage.value = error.message
                }
            }
        }
    }

    // ==================== OPERACIONES DE CONTROL ====================

    /**
     * Dispensa agua manualmente con la cantidad especificada
     */
    fun dispensarManual(cantidad: Int, usuario: String = "Usuario") {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            val result = repository.dispensarManual(cantidad, usuario)
            
            result.onSuccess {
                _operationState.value = OperationState.Success("Dispensado: $cantidad ml")
            }.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Error al dispensar"
                )
            }
        }
    }

    /**
     * Detiene el motor del dispensador
     */
    fun detenerMotor() {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            val result = repository.detenerMotor()
            
            result.onSuccess {
                _operationState.value = OperationState.Success("Motor detenido")
            }.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Error al detener motor"
                )
            }
        }
    }

    /**
     * Cambia el modo de operación (manual/automático)
     */
    fun cambiarModo(automatico: Boolean) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            val result = repository.cambiarModo(automatico)
            
            result.onSuccess {
                val modo = if (automatico) "Automático" else "Manual"
                _operationState.value = OperationState.Success("Modo: $modo")
            }.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Error al cambiar modo"
                )
            }
        }
    }

    /**
     * Activa o desactiva la programación
     */
    fun toggleProgramacion(activa: Boolean) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            val result = repository.toggleProgramacion(activa)
            
            result.onSuccess {
                val estado = if (activa) "activada" else "desactivada"
                _operationState.value = OperationState.Success("Programación $estado")
            }.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Error al cambiar programación"
                )
            }
        }
    }

    /**
     * Actualiza el control completo del dispensador
     */
    fun actualizarControl(control: DispenserControl) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            val result = repository.actualizarControl(control)
            
            result.onSuccess {
                _operationState.value = OperationState.Success("Control actualizado")
            }.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Error al actualizar control"
                )
            }
        }
    }

    // ==================== OPERACIONES DE HORARIOS ====================

    /**
     * Agrega un nuevo horario programado
     */
    fun agregarHorario(schedule: DispenserSchedule) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            if (!schedule.esValido()) {
                _operationState.value = OperationState.Error("Horario no válido")
                return@launch
            }
            
            val result = repository.agregarHorario(schedule)
            
            result.onSuccess {
                _operationState.value = OperationState.Success("Horario agregado")
            }.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Error al agregar horario"
                )
            }
        }
    }

    /**
     * Actualiza un horario existente
     */
    fun actualizarHorario(schedule: DispenserSchedule) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            if (!schedule.esValido()) {
                _operationState.value = OperationState.Error("Horario no válido")
                return@launch
            }
            
            val result = repository.actualizarHorario(schedule)
            
            result.onSuccess {
                _operationState.value = OperationState.Success("Horario actualizado")
            }.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Error al actualizar horario"
                )
            }
        }
    }

    /**
     * Elimina un horario programado
     */
    fun eliminarHorario(id: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            val result = repository.eliminarHorario(id)
            
            result.onSuccess {
                _operationState.value = OperationState.Success("Horario eliminado")
            }.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Error al eliminar horario"
                )
            }
        }
    }

    // ==================== OPERACIONES DE NOTIFICACIONES ====================

    /**
     * Limpia todas las notificaciones
     */
    fun limpiarNotificaciones() {
        viewModelScope.launch {
            val result = repository.limpiarNotificaciones()
            
            result.onSuccess {
                _operationState.value = OperationState.Success("Notificaciones limpiadas")
            }.onFailure { error ->
                _operationState.value = OperationState.Error(
                    error.message ?: "Error al limpiar notificaciones"
                )
            }
        }
    }

    // ==================== UTILIDADES ====================

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Resetea el estado de operación
     */
    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }

    /**
     * Obtiene el número de notificaciones activas
     */
    fun getNotificationCount(): Int {
        return _notificaciones.value?.contarNotificaciones() ?: 0
    }

    /**
     * Verifica si el dispositivo puede dispensar
     */
    fun puedeDispensar(): Boolean {
        return _estado.value?.puedeDispensar() ?: false
    }
}

/**
 * Estados de operación unificados
 */
sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}
