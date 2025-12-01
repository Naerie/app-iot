package com.dispensador.app.firebase

import android.util.Log
import com.dispensador.app.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio unificado para todas las operaciones de Firebase
 * Centraliza la lectura y escritura de datos del dispensador
 */
class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance()
    private val TAG = "FirebaseRepository"

    // Rutas unificadas de Firebase
    private companion object {
        const val PATH_ESTADO = "dispensador/estado"
        const val PATH_CONTROL = "dispensador/control"
        const val PATH_HORARIOS = "dispensador/horarios"
        const val PATH_HISTORIAL = "dispensador/historial"
        const val PATH_NOTIFICACIONES = "dispensador/notificaciones"
    }

    // ==================== OBSERVADORES (FLOWS) ====================

    /**
     * Observa el estado del dispensador en tiempo real
     */
    fun observarEstado(): Flow<Result<DispenserState>> = callbackFlow {
        val reference = database.getReference(PATH_ESTADO)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val estado = snapshot.getValue(DispenserState::class.java)
                    if (estado != null) {
                        Log.d(TAG, "Estado actualizado: $estado")
                        trySend(Result.success(estado))
                    } else {
                        Log.w(TAG, "Estado es null, creando estado por defecto")
                        trySend(Result.success(DispenserState()))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando estado", e)
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de estado cancelada", error.toException())
                trySend(Result.failure(error.toException()))
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
            Log.d(TAG, "Listener de estado eliminado")
        }
    }

    /**
     * Observa el control del dispensador en tiempo real
     */
    fun observarControl(): Flow<Result<DispenserControl>> = callbackFlow {
        val reference = database.getReference(PATH_CONTROL)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val control = snapshot.getValue(DispenserControl::class.java)
                    if (control != null) {
                        Log.d(TAG, "Control actualizado: $control")
                        trySend(Result.success(control))
                    } else {
                        Log.w(TAG, "Control es null, creando control por defecto")
                        trySend(Result.success(DispenserControl()))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando control", e)
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de control cancelada", error.toException())
                trySend(Result.failure(error.toException()))
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
            Log.d(TAG, "Listener de control eliminado")
        }
    }

    /**
     * Observa las notificaciones en tiempo real
     */
    fun observarNotificaciones(): Flow<Result<DispenserNotification>> = callbackFlow {
        val reference = database.getReference(PATH_NOTIFICACIONES)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val notificacion = snapshot.getValue(DispenserNotification::class.java)
                    if (notificacion != null) {
                        Log.d(TAG, "Notificación actualizada: $notificacion")
                        trySend(Result.success(notificacion))
                    } else {
                        Log.w(TAG, "Notificación es null, creando notificación vacía")
                        trySend(Result.success(DispenserNotification.vacia()))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando notificación", e)
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de notificaciones cancelada", error.toException())
                trySend(Result.failure(error.toException()))
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
            Log.d(TAG, "Listener de notificaciones eliminado")
        }
    }

    /**
     * Observa los horarios programados en tiempo real
     */
    fun observarHorarios(): Flow<Result<List<DispenserSchedule>>> = callbackFlow {
        val reference = database.getReference(PATH_HORARIOS)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val horarios = mutableListOf<DispenserSchedule>()
                    for (childSnapshot in snapshot.children) {
                        val horario = childSnapshot.getValue(DispenserSchedule::class.java)
                        horario?.let { horarios.add(it) }
                    }
                    Log.d(TAG, "Horarios actualizados: ${horarios.size} horarios")
                    trySend(Result.success(horarios))
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando horarios", e)
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de horarios cancelada", error.toException())
                trySend(Result.failure(error.toException()))
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
            Log.d(TAG, "Listener de horarios eliminado")
        }
    }

    /**
     * Observa el historial de dispensaciones (limitado a los últimos N registros)
     */
    fun observarHistorial(limit: Int = 50): Flow<Result<List<DispenserHistory>>> = callbackFlow {
        val reference = database.getReference(PATH_HISTORIAL)
            .orderByChild("timestamp")
            .limitToLast(limit)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val historial = mutableListOf<DispenserHistory>()
                    for (childSnapshot in snapshot.children) {
                        val registro = childSnapshot.getValue(DispenserHistory::class.java)
                        registro?.let { historial.add(it) }
                    }
                    // Ordenar por timestamp descendente (más reciente primero)
                    historial.sortByDescending { it.timestamp }
                    Log.d(TAG, "Historial actualizado: ${historial.size} registros")
                    trySend(Result.success(historial))
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando historial", e)
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de historial cancelada", error.toException())
                trySend(Result.failure(error.toException()))
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
            Log.d(TAG, "Listener de historial eliminado")
        }
    }

    // ==================== OPERACIONES DE ESCRITURA ====================

    /**
     * Actualiza el control del dispensador
     */
    suspend fun actualizarControl(control: DispenserControl): Result<Unit> {
        return try {
            val reference = database.getReference(PATH_CONTROL)
            val controlActualizado = control.copy(
                ultimaInstruccion = System.currentTimeMillis()
            )
            reference.setValue(controlActualizado).await()
            Log.d(TAG, "Control actualizado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando control", e)
            Result.failure(e)
        }
    }

    /**
     * Dispensa agua manualmente con la cantidad especificada
     */
    suspend fun dispensarManual(cantidad: Int, usuario: String = "Usuario"): Result<Unit> {
        return try {
            // Actualizar control para dispensar
            val control = DispenserControl(
                motorEncendido = true,
                cantidadDispensado = cantidad,
                ultimaInstruccion = System.currentTimeMillis()
            )
            database.getReference(PATH_CONTROL).setValue(control).await()

            // Registrar en historial
            val historyId = System.currentTimeMillis().toString()
            val history = DispenserHistory(
                id = historyId,
                timestamp = System.currentTimeMillis(),
                cantidad = cantidad,
                tipo = DispenserHistory.TIPO_MANUAL,
                usuario = usuario,
                exitoso = true
            )
            database.getReference("$PATH_HISTORIAL/$historyId").setValue(history).await()

            Log.d(TAG, "Dispensado manual exitoso: $cantidad ml")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en dispensado manual", e)
            Result.failure(e)
        }
    }

    /**
     * Detiene el motor del dispensador
     */
    suspend fun detenerMotor(): Result<Unit> {
        return try {
            val reference = database.getReference(PATH_CONTROL)
            val control = DispenserControl(
                motorEncendido = false,
                ultimaInstruccion = System.currentTimeMillis()
            )
            reference.setValue(control).await()
            Log.d(TAG, "Motor detenido exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deteniendo motor", e)
            Result.failure(e)
        }
    }

    /**
     * Cambia el modo de operación (manual/automático)
     */
    suspend fun cambiarModo(automatico: Boolean): Result<Unit> {
        return try {
            val reference = database.getReference(PATH_CONTROL)
            val control = DispenserControl(
                modoAutomatico = automatico,
                ultimaInstruccion = System.currentTimeMillis()
            )
            reference.setValue(control).await()
            Log.d(TAG, "Modo cambiado a: ${if (automatico) "Automático" else "Manual"}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cambiando modo", e)
            Result.failure(e)
        }
    }

    /**
     * Activa o desactiva la programación
     */
    suspend fun toggleProgramacion(activa: Boolean): Result<Unit> {
        return try {
            val reference = database.getReference(PATH_CONTROL)
            val control = DispenserControl(
                programacionActiva = activa,
                ultimaInstruccion = System.currentTimeMillis()
            )
            reference.setValue(control).await()
            Log.d(TAG, "Programación ${if (activa) "activada" else "desactivada"}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cambiando programación", e)
            Result.failure(e)
        }
    }

    /**
     * Agrega un nuevo horario programado
     */
    suspend fun agregarHorario(schedule: DispenserSchedule): Result<String> {
        return try {
            val id = database.getReference(PATH_HORARIOS).push().key
                ?: throw Exception("No se pudo generar ID")
            
            val horarioConId = schedule.copy(
                id = id,
                fechaCreacion = System.currentTimeMillis()
            )
            
            database.getReference("$PATH_HORARIOS/$id").setValue(horarioConId).await()
            Log.d(TAG, "Horario agregado exitosamente: $id")
            Result.success(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error agregando horario", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza un horario existente
     */
    suspend fun actualizarHorario(schedule: DispenserSchedule): Result<Unit> {
        return try {
            if (schedule.id.isEmpty()) {
                throw Exception("ID de horario no válido")
            }
            database.getReference("$PATH_HORARIOS/${schedule.id}").setValue(schedule).await()
            Log.d(TAG, "Horario actualizado exitosamente: ${schedule.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando horario", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un horario programado
     */
    suspend fun eliminarHorario(id: String): Result<Unit> {
        return try {
            database.getReference("$PATH_HORARIOS/$id").removeValue().await()
            Log.d(TAG, "Horario eliminado exitosamente: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando horario", e)
            Result.failure(e)
        }
    }

    /**
     * Limpia todas las notificaciones
     */
    suspend fun limpiarNotificaciones(): Result<Unit> {
        return try {
            val reference = database.getReference(PATH_NOTIFICACIONES)
            reference.setValue(DispenserNotification.vacia()).await()
            Log.d(TAG, "Notificaciones limpiadas")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando notificaciones", e)
            Result.failure(e)
        }
    }

    /**
     * Limpia el historial completo
     */
    suspend fun limpiarHistorial(): Result<Unit> {
        return try {
            database.getReference(PATH_HISTORIAL).removeValue().await()
            Log.d(TAG, "Historial limpiado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando historial", e)
            Result.failure(e)
        }
    }
}
