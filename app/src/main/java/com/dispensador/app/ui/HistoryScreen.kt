package com.dispensador.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.dispensador.app.data.DispenserHistory
import com.dispensador.app.viewmodel.DispenserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    dispenserViewModel: DispenserViewModel,
    onNavigateBack: () -> Unit
) {
    val historial by dispenserViewModel.historial.collectAsState()
    var filterType by remember { mutableStateOf("Todos") }

    val historialFiltrado = when (filterType) {
        "Manual" -> historial.filter { it.tipo == DispenserHistory.TIPO_MANUAL }
        "Programado" -> historial.filter { it.tipo == DispenserHistory.TIPO_PROGRAMADO }
        else -> historial
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
                            text = "Historial",
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Filtros
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == "Todos",
                        onClick = { filterType = "Todos" },
                        label = { Text("Todos") },
                        leadingIcon = {
                            if (filterType == "Todos") {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                    FilterChip(
                        selected = filterType == "Manual",
                        onClick = { filterType = "Manual" },
                        label = { Text("Manual") },
                        leadingIcon = {
                            if (filterType == "Manual") {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                    FilterChip(
                        selected = filterType == "Programado",
                        onClick = { filterType = "Programado" },
                        label = { Text("Programado") },
                        leadingIcon = {
                            if (filterType == "Programado") {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Estadísticas
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
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Total",
                            value = historial.size.toString(),
                            icon = Icons.Default.List
                        )
                        StatItem(
                            label = "Manual",
                            value = historial.count { it.tipo == DispenserHistory.TIPO_MANUAL }.toString(),
                            icon = Icons.Default.TouchApp
                        )
                        StatItem(
                            label = "Programado",
                            value = historial.count { it.tipo == DispenserHistory.TIPO_PROGRAMADO }.toString(),
                            icon = Icons.Default.Schedule
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de historial
                if (historialFiltrado.isEmpty()) {
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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay registros",
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historialFiltrado) { registro ->
                            HistoryItem(history = registro)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF00D2CF),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun HistoryItem(history: DispenserHistory) {
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
            // Icono de tipo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF00D2CF).copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (history.tipo == DispenserHistory.TIPO_MANUAL)
                        Icons.Default.TouchApp
                    else
                        Icons.Default.Schedule,
                    contentDescription = history.tipo,
                    tint = Color(0xFF00D2CF),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${history.cantidad} ml",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = history.obtenerTipoLegible(),
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = history.obtenerFechaHora(),
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }

            // Estado
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = Color(history.obtenerColorEstado()),
                        shape = CircleShape
                    )
            )
        }
    }
}
