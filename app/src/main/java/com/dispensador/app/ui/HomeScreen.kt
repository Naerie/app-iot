package com.dispensador.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.dispensador.app.composables.*
import com.dispensador.app.viewmodel.AuthViewModel
import com.dispensador.app.viewmodel.DispenserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    dispenserViewModel: DispenserViewModel,
    authViewModel: AuthViewModel,
    onNavigateToControl: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val estado by dispenserViewModel.estado.collectAsState()
    val control by dispenserViewModel.control.collectAsState()
    val notificaciones by dispenserViewModel.notificaciones.collectAsState()
    val isLoading by dispenserViewModel.isLoading.collectAsState()

    val notificationCount = notificaciones?.contarNotificaciones() ?: 0

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
                            text = "Dispensador",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF00D2CF)
                    ),
                    actions = {
                        // Notificaciones
                        BadgedBox(
                            badge = {
                                if (notificationCount > 0) {
                                    Badge {
                                        Text(text = notificationCount.toString())
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = onNavigateToNotifications) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = Color.White
                                )
                            }
                        }

                        // Cerrar sesión
                        IconButton(onClick = {
                            authViewModel.signOut()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF00D2CF)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Estado compacto
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
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
                                    text = estado?.obtenerEstado() ?: "Desconocido",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = if (control?.modoAutomatico == true) "Modo Automático" else "Modo Manual",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                            Icon(
                                imageVector = when {
                                    estado?.enUso == true -> Icons.Default.PlayArrow
                                    estado?.encendido == true -> Icons.Default.Power
                                    else -> Icons.Default.PowerOff
                                },
                                contentDescription = null,
                                tint = when {
                                    estado?.conexion == false -> Color(0xFFCCCCCC)
                                    estado?.enUso == true -> Color(0xFF00D2CF)
                                    estado?.encendido == true -> Color(0xFF4CAF50)
                                    else -> Color(0xFFFF9800)
                                },
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // Nivel de agua y conexión
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
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
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Agua",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                WaterLevelIndicator(
                                    nivel = estado?.nivelAgua ?: 0
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
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
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "WiFi",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                WiFiIndicator(
                                    conectado = estado?.conexion ?: false,
                                    intensidad = estado?.intensidadWifi ?: 0
                                )
                            }
                        }
                    }

                    // Accesos rápidos
                    Text(
                        text = "Accesos Rápidos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickAccessCard(
                            title = "Control",
                            icon = Icons.Default.Settings,
                            onClick = onNavigateToControl,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Horarios",
                            icon = Icons.Default.Schedule,
                            onClick = onNavigateToSchedule,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickAccessCard(
                            title = "Historial",
                            icon = Icons.Default.History,
                            onClick = onNavigateToHistory,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Alertas",
                            icon = Icons.Default.Notifications,
                            onClick = onNavigateToNotifications,
                            modifier = Modifier.weight(1f),
                            badge = if (notificationCount > 0) notificationCount.toString() else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF00D2CF),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
            }

            if (badge != null) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(text = badge)
                }
            }
        }
    }
}
