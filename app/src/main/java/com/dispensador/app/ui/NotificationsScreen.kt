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
import com.dispensador.app.viewmodel.DispenserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    dispenserViewModel: DispenserViewModel,
    onNavigateBack: () -> Unit
) {
    val notificaciones by dispenserViewModel.notificaciones.collectAsState()
    val estado by dispenserViewModel.estado.collectAsState()

    val listaNotificaciones = notificaciones?.obtenerListaNotificaciones() ?: emptyList()

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
                            text = "Notificaciones",
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
                    actions = {
                        if (listaNotificaciones.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    dispenserViewModel.limpiarNotificaciones()
                                }
                            ) {
                                Text(
                                    text = "Limpiar",
                                    color = Color.White
                                )
                            }
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
                // Resumen de estado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (listaNotificaciones.isEmpty())
                            Color(0xFFE8F5E9)
                        else
                            Color(0xFFFFEBEE)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = if (listaNotificaciones.isEmpty())
                                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    else
                                        Color(0xFFF44336).copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (listaNotificaciones.isEmpty())
                                    Icons.Default.CheckCircle
                                else
                                    Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (listaNotificaciones.isEmpty())
                                    Color(0xFF4CAF50)
                                else
                                    Color(0xFFF44336),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = if (listaNotificaciones.isEmpty())
                                    "Todo en orden"
                                else
                                    "${listaNotificaciones.size} alerta(s) activa(s)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = if (listaNotificaciones.isEmpty())
                                    "No hay notificaciones pendientes"
                                else
                                    "Requiere atención",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de notificaciones
                if (listaNotificaciones.isEmpty()) {
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
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sin notificaciones",
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "El sistema está funcionando correctamente",
                                fontSize = 14.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Notificación de contenedor vacío
                        if (notificaciones?.contenidoVacio == true) {
                            item {
                                NotificationCard(
                                    title = "Contenedor Vacío",
                                    message = "El contenedor de agua está vacío. Por favor, rellénelo.",
                                    icon = Icons.Default.WaterDrop,
                                    color = Color(0xFFF44336),
                                    priority = "Alta"
                                )
                            }
                        }

                        // Notificación de error del sistema
                        if (notificaciones?.errorSistema == true) {
                            item {
                                NotificationCard(
                                    title = "Error del Sistema",
                                    message = "Se ha detectado un error en el sistema. Revise el dispositivo.",
                                    icon = Icons.Default.Error,
                                    color = Color(0xFFF44336),
                                    priority = "Alta"
                                )
                            }
                        }

                        // Notificación de desconexión
                        if (notificaciones?.desconexion == true) {
                            item {
                                NotificationCard(
                                    title = "Dispositivo Desconectado",
                                    message = "El dispositivo se ha desconectado. Verifique la conexión.",
                                    icon = Icons.Default.WifiOff,
                                    color = Color(0xFFFF9800),
                                    priority = "Media"
                                )
                            }
                        }

                        // Notificación de nivel bajo
                        if (notificaciones?.nivelBajo == true) {
                            item {
                                NotificationCard(
                                    title = "Nivel Bajo de Agua",
                                    message = "El nivel de agua está bajo (${estado?.nivelAgua ?: 0}%). Considere rellenar pronto.",
                                    icon = Icons.Default.WaterDrop,
                                    color = Color(0xFFFFEB3B),
                                    priority = "Baja"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Información adicional
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Información",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Las notificaciones se actualizan en tiempo real",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    priority: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = color.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = priority,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
