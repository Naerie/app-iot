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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio unificado para todas las operaciones de Firebase
 * Versión con manejo robusto de errores
 */
class FirebaseRepository {

    private val TAG = "FirebaseRepository"

    // Inicialización lazy para evitar crashes
    private val database: FirebaseDatabase? by lazy {
        try {
            FirebaseDatabase.getInstance().apply {
                Log.d(TAG, "Firebase Database inicializado correctamente")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar Firebase Database", e)
            null
        }
    }

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
    fun observarEstado(): Flow<DispenserState?> = callbackFlow {
        if (database == null) {
            Log.e(TAG, "Database no inicializado, retornando estado por defecto")
            trySend(DispenserState())
            close()
            return@callbackFlow
        }

        val reference = database!!.getReference(PATH_ESTADO)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val estado = snapshot.getValue(DispenserState::class.java) ?: DispenserState()
                    Log.d(TAG, "Estado actualizado: $estado")
                    trySend(estado)
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando estado", e)
                    trySend(DispenserState())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de estado cancelada", error.toException())
                trySend(DispenserState())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
            Log.d(TAG, "Listener de estado eliminado")
        }
    }.catch { e ->
        Log.e(TAG, "Error en observarEstado", e)
        emit(DispenserState())
    }

    /**
     * Observa el control del dispensador en tiempo real
     */
    fun observarControl(): Flow<DispenserControl?> = callbackFlow {
        if (database == null) {
            Log.e(TAG, "Database no inicializado, retornando control por defecto")
            trySend(DispenserControl())
            close()
            return@callbackFlow
        }

        val reference = database!!.getReference(PATH_CONTROL)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val control = snapshot.getValue(DispenserControl::class.java) ?: DispenserControl()
                    Log.d(TAG, "Control actualizado: $control")
                    trySend(control)
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando control", e)
                    trySend(DispenserControl())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de control cancelada", error.toException())
                trySend(DispenserControl())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
        }
    }.catch { e ->
        Log.e(TAG, "Error en observarControl", e)
        emit(DispenserControl())
    }

    /**
     * Observa las notificaciones en tiempo real
     */
    fun observarNotificaciones(): Flow<DispenserNotification?> = callbackFlow {
        if (database == null) {
            trySend(DispenserNotification.crearVacia())
            close()
            return@callbackFlow
        }

        val reference = database!!.getReference(PATH_NOTIFICACIONES)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val notif = snapshot.getValue(DispenserNotification::class.java)
                        ?: DispenserNotification.crearVacia()
                    trySend(notif)
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando notificaciones", e)
                    trySend(DispenserNotification.crearVacia())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de notificaciones cancelada", error.toException())
                trySend(DispenserNotification.crearVacia())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
        }
    }.catch { e ->
        Log.e(TAG, "Error en observarNotificaciones", e)
        emit(DispenserNotification.crearVacia())
    }

    /**
     * Observa los horarios programados
     */
    fun observarHorarios(): Flow<List<DispenserSchedule>> = callbackFlow {
        if (database == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val reference = database!!.getReference(PATH_HORARIOS)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val horarios = mutableListOf<DispenserSchedule>()
                    for (child in snapshot.children) {
                        child.getValue(DispenserSchedule::class.java)?.let {
                            horarios.add(it)
                        }
                    }
                    trySend(horarios)
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando horarios", e)
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de horarios cancelada", error.toException())
                trySend(emptyList())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
        }
    }.catch { e ->
        Log.e(TAG, "Error en observarHorarios", e)
        emit(emptyList())
    }

    /**
     * Observa el historial
     */
    fun observarHistorial(): Flow<List<DispenserHistory>> = callbackFlow {
        if (database == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val reference = database!!.getReference(PATH_HISTORIAL)
            .orderByChild("timestamp")
            .limitToLast(50)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val historial = mutableListOf<DispenserHistory>()
                    for (child in snapshot.children) {
                        child.getValue(DispenserHistory::class.java)?.let {
                            historial.add(it)
                        }
                    }
                    trySend(historial.reversed())
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando historial", e)
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Lectura de historial cancelada", error.toException())
                trySend(emptyList())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
        }
    }.catch { e ->
        Log.e(TAG, "Error en observarHistorial", e)
        emit(emptyList())
    }

    // ==================== OPERACIONES DE ESCRITURA ====================

    /**
     * Dispensa agua manualmente
     */
    suspend fun dispensarManual(cantidad: Int): Result<Unit> {
        return try {
            if (database == null) {
                return Result.failure(Exception("Firebase no inicializado"))
            }

            val control = DispenserControl(
                motorEncendido = true,
                cantidadDispensado = cantidad,
                ultimaInstruccion = System.currentTimeMillis()
            )

            database!!.getReference(PATH_CONTROL).setValue(control).await()

            Log.d(TAG, "Dispensado manual: $cantidad ml")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en dispensarManual", e)
            Result.failure(e)
        }
    }

    /**
     * Detiene el motor
     */
    suspend fun detenerMotor(): Result<Unit> {
        return try {
            if (database == null) {
                return Result.failure(Exception("Firebase no inicializado"))
            }

            database!!.getReference(PATH_CONTROL).child("motorEncendido").setValue(false).await()

            Log.d(TAG, "Motor detenido")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en detenerMotor", e)
            Result.failure(e)
        }
    }

    /**
     * Cambia el modo de operación
     */
    suspend fun cambiarModo(automatico: Boolean): Result<Unit> {
        return try {
            if (database == null) {
                return Result.failure(Exception("Firebase no inicializado"))
            }

            database!!.getReference(PATH_CONTROL).child("modoAutomatico").setValue(automatico).await()

            Log.d(TAG, "Modo cambiado a: ${if (automatico) "Automático" else "Manual"}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en cambiarModo", e)
            Result.failure(e)
        }
    }

    /**
     * Activa o desactiva la programación
     */
    suspend fun toggleProgramacion(activa: Boolean): Result<Unit> {
        return try {
            if (database == null) {
                return Result.failure(Exception("Firebase no inicializado"))
            }

            database!!.getReference(PATH_CONTROL).child("programacionActiva").setValue(activa).await()

            Log.d(TAG, "Programación ${if (activa) "activada" else "desactivada"}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en toggleProgramacion", e)
            Result.failure(e)
        }
    }

    /**
     * Agrega un horario
     */
    suspend fun agregarHorario(horario: DispenserSchedule): Result<Unit> {
        return try {
            if (database == null) {
                return Result.failure(Exception("Firebase no inicializado"))
            }

            database!!.getReference(PATH_HORARIOS).child(horario.id).setValue(horario).await()

            Log.d(TAG, "Horario agregado: ${horario.hora}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en agregarHorario", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza un horario
     */
    suspend fun actualizarHorario(horario: DispenserSchedule): Result<Unit> {
        return try {
            if (database == null) {
                return Result.failure(Exception("Firebase no inicializado"))
            }

            database!!.getReference(PATH_HORARIOS).child(horario.id).setValue(horario).await()

            Log.d(TAG, "Horario actualizado: ${horario.hora}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en actualizarHorario", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un horario
     */
    suspend fun eliminarHorario(id: String): Result<Unit> {
        return try {
            if (database == null) {
                return Result.failure(Exception("Firebase no inicializado"))
            }

            database!!.getReference(PATH_HORARIOS).child(id).removeValue().await()

            Log.d(TAG, "Horario eliminado: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en eliminarHorario", e)
            Result.failure(e)
        }
    }

    /**
     * Limpia las notificaciones
     */
    suspend fun limpiarNotificaciones(): Result<Unit> {
        return try {
            if (database == null) {
                return Result.failure(Exception("Firebase no inicializado"))
            }

            database!!.getReference(PATH_NOTIFICACIONES).setValue(DispenserNotification.crearVacia()).await()

            Log.d(TAG, "Notificaciones limpiadas")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en limpiarNotificaciones", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica si Firebase está inicializado
     */
    fun isInitialized(): Boolean {
        return database != null
    }
}
