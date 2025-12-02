package com.dispensador.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispensador.app.composables.ControlButton
import com.dispensador.app.composables.DangerControlButton
import com.dispensador.app.viewmodel.DispenserViewModel
import com.dispensador.app.viewmodel.OperationState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    dispenserViewModel: DispenserViewModel,
    onNavigateBack: () -> Unit
) {
    val estado by dispenserViewModel.estado.collectAsState()
    val operationState by dispenserViewModel.operationState.collectAsState()

    var sliderValue by remember { mutableStateOf(250f) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Resetear mensajes al entrar a la pantalla
    LaunchedEffect(Unit) {
        dispenserViewModel.resetOperationState()
    }

    // Observar cambios en el estado de operación
    LaunchedEffect(operationState) {
        when (operationState) {
            is OperationState.Success -> {
                successMessage = (operationState as OperationState.Success).message
                showSuccessMessage = true
                dispenserViewModel.resetOperationState()
            }
            is OperationState.Error -> {
                successMessage = (operationState as OperationState.Error).message
                showSuccessMessage = true
                dispenserViewModel.resetOperationState()
            }
            else -> {}
        }
    }

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
                            text = "Control",
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
            snackbarHost = {
                if (showSuccessMessage) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            TextButton(onClick = { showSuccessMessage = false }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(successMessage)
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Advertencia si no puede dispensar
                if (estado?.puedeDispensar() == false) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = "No disponible",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6F00)
                                )
                                Text(
                                    text = when {
                                        estado?.conexion == false -> "Dispositivo desconectado"
                                        estado?.contenedorVacio() == true -> "Contenedor vacío"
                                        estado?.tazaPresente == false -> "Coloque un recipiente"
                                        else -> "Verifique el dispositivo"
                                    },
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }

                // Cantidades rápidas
                Text(
                    text = "Cantidades Rápidas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlButton(
                        text = "100 ml",
                        onClick = {
                            dispenserViewModel.dispensarManual(100)
                        },
                        enabled = estado?.puedeDispensar() == true &&
                                operationState !is OperationState.Loading,
                        modifier = Modifier.weight(1f)
                    )
                    ControlButton(
                        text = "250 ml",
                        onClick = {
                            dispenserViewModel.dispensarManual(250)
                        },
                        enabled = estado?.puedeDispensar() == true &&
                                operationState !is OperationState.Loading,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlButton(
                        text = "500 ml",
                        onClick = {
                            dispenserViewModel.dispensarManual(500)
                        },
                        enabled = estado?.puedeDispensar() == true &&
                                operationState !is OperationState.Loading,
                        modifier = Modifier.weight(1f)
                    )
                    ControlButton(
                        text = "Llenar",
                        onClick = {
                            dispenserViewModel.dispensarManual(500)
                        },
                        enabled = estado?.puedeDispensar() == true &&
                                operationState !is OperationState.Loading,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Cantidad personalizada con slider
                Text(
                    text = "Cantidad Personalizada",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "${sliderValue.roundToInt()} ml",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00D2CF),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            valueRange = 50f..500f,
                            steps = 44,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00D2CF),
                                activeTrackColor = Color(0xFF00D2CF),
                                inactiveTrackColor = Color(0xFFCCCCCC)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "50 ml",
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                            Text(
                                text = "500 ml",
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ControlButton(
                            text = "Dispensar ${sliderValue.roundToInt()} ml",
                            onClick = {
                                dispenserViewModel.dispensarManual(sliderValue.roundToInt())
                            },
                            enabled = estado?.puedeDispensar() == true &&
                                    operationState !is OperationState.Loading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Botón de emergencia
                Text(
                    text = "Emergencia",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                DangerControlButton(
                    text = "Detener Motor",
                    onClick = {
                        dispenserViewModel.detenerMotor()
                    },
                    enabled = operationState !is OperationState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
