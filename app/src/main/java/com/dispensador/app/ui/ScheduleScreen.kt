package com.dispensador.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispensador.app.data.DispenserSchedule
import com.dispensador.app.viewmodel.DispenserViewModel
import com.dispensador.app.viewmodel.OperationState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    dispenserViewModel: DispenserViewModel,
    onNavigateBack: () -> Unit
) {
    val horarios by dispenserViewModel.horarios.collectAsState()
    val control by dispenserViewModel.control.collectAsState()
    val operationState by dispenserViewModel.operationState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<DispenserSchedule?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFDFEFE),
                        Color(0xFFF8FAFA)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Horarios",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF00D2CF)
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF00D2CF),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar horario"
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Control de programación global
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Programación Automática",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = if (control?.programacionActiva == true) 
                                    "Horarios activos" 
                                else 
                                    "Horarios desactivados",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                        Switch(
                            checked = control?.programacionActiva ?: false,
                            onCheckedChange = { activa ->
                                dispenserViewModel.toggleProgramacion(activa)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00D2CF)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de horarios
                if (horarios.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sin horarios",
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "Toca + para agregar",
                                fontSize = 14.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(horarios) { horario ->
                            ScheduleItem(
                                schedule = horario,
                                onEdit = {
                                    selectedSchedule = horario
                                    showEditDialog = true
                                },
                                onDelete = {
                                    dispenserViewModel.eliminarHorario(horario.id)
                                },
                                onToggle = { activo ->
                                    dispenserViewModel.actualizarHorario(
                                        horario.copy(activo = activo)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para agregar horario
    if (showAddDialog) {
        AddScheduleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { schedule ->
                dispenserViewModel.agregarHorario(schedule)
                showAddDialog = false
            }
        )
    }

    // Diálogo para editar horario
    if (showEditDialog && selectedSchedule != null) {
        EditScheduleDialog(
            schedule = selectedSchedule!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { schedule ->
                dispenserViewModel.actualizarHorario(schedule)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun ScheduleItem(
    schedule: DispenserSchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.hora,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (schedule.activo) Color(0xFF00D2CF) else Color(0xFFCCCCCC)
                )
                Text(
                    text = "${schedule.cantidad} ml",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = schedule.obtenerNombresDias(),
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = schedule.activo,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00D2CF)
                    )
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFF00D2CF)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (DispenserSchedule) -> Unit
) {
    var cantidad by remember { mutableStateOf("250") }
    var diasSeleccionados by remember { mutableStateOf(setOf<Int>()) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Estado del TimePicker
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Horario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de hora con TimePicker
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hora",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00D2CF)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Seleccionar hora",
                            tint = Color(0xFF00D2CF)
                        )
                    }
                }

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad (ml)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Días de la semana:", fontWeight = FontWeight.Bold)
                DispenserSchedule.DIAS_SEMANA.forEach { (dia, nombre) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                diasSeleccionados = if (dia in diasSeleccionados) {
                                    diasSeleccionados - dia
                                } else {
                                    diasSeleccionados + dia
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = dia in diasSeleccionados,
                            onCheckedChange = null
                        )
                        Text(nombre)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val schedule = DispenserSchedule(
                        hora = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                        cantidad = cantidad.toIntOrNull() ?: 250,
                        dias = diasSeleccionados.toList(),
                        activo = true
                    )
                    onConfirm(schedule)
                },
                enabled = diasSeleccionados.isNotEmpty()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // TimePicker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("OK")
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF5F5F5),
                        selectorColor = Color(0xFF00D2CF),
                        timeSelectorSelectedContainerColor = Color(0xFF00D2CF),
                        timeSelectorSelectedContentColor = Color.White
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleDialog(
    schedule: DispenserSchedule,
    onDismiss: () -> Unit,
    onConfirm: (DispenserSchedule) -> Unit
) {
    var cantidad by remember { mutableStateOf(schedule.cantidad.toString()) }
    var diasSeleccionados by remember { mutableStateOf(schedule.dias.toSet()) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Parsear hora inicial
    val horaParts = schedule.hora.split(":")
    val horaInicial = horaParts.getOrNull(0)?.toIntOrNull() ?: 8
    val minutoInicial = horaParts.getOrNull(1)?.toIntOrNull() ?: 0
    
    val timePickerState = rememberTimePickerState(
        initialHour = horaInicial,
        initialMinute = minutoInicial,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Horario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de hora con TimePicker
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hora",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00D2CF)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Seleccionar hora",
                            tint = Color(0xFF00D2CF)
                        )
                    }
                }

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad (ml)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Días de la semana:", fontWeight = FontWeight.Bold)
                DispenserSchedule.DIAS_SEMANA.forEach { (dia, nombre) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                diasSeleccionados = if (dia in diasSeleccionados) {
                                    diasSeleccionados - dia
                                } else {
                                    diasSeleccionados + dia
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = dia in diasSeleccionados,
                            onCheckedChange = null
                        )
                        Text(nombre)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedSchedule = schedule.copy(
                        hora = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                        cantidad = cantidad.toIntOrNull() ?: 250,
                        dias = diasSeleccionados.toList()
                    )
                    onConfirm(updatedSchedule)
                },
                enabled = diasSeleccionados.isNotEmpty()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // TimePicker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("OK")
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF5F5F5),
                        selectorColor = Color(0xFF00D2CF),
                        timeSelectorSelectedContainerColor = Color(0xFF00D2CF),
                        timeSelectorSelectedContentColor = Color.White
                    )
                )
            }
        )
    }
}
