package com.dispensador.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dispensador.app.data.NotificationItem
import com.dispensador.app.data.NotificationType
import com.dispensador.app.viewmodel.DispenserViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de notificaciones del dispensador
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    dispenserViewModel: DispenserViewModel,
    onNavigateBack: () -> Unit
) {
    val notificaciones by dispenserViewModel.notificaciones.collectAsState()
    
    // Obtener lista de notificaciones
    val listaNotificaciones = remember(notificaciones) {
        notificaciones?.obtenerListaNotificaciones() ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (listaNotificaciones.isNotEmpty()) {
                        IconButton(onClick = {
                            dispenserViewModel.limpiarNotificaciones()
                        }) {
                            Icon(Icons.Default.Clear, "Limpiar notificaciones")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (listaNotificaciones.isEmpty()) {
                // Estado vacío
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay notificaciones",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Todas las notificaciones aparecerán aquí",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                // Lista de notificaciones
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaNotificaciones) { notificacion ->
                        NotificationCard(notificacion)
                    }
                }
            }
        }
    }
}

/**
 * Card individual de notificación
 */
@Composable
fun NotificationCard(notificacion: NotificationItem) {
    val (backgroundColor, iconColor, icon) = when (notificacion.tipo) {
        NotificationType.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            Icons.Default.Warning
        )
        NotificationType.WARNING -> Triple(
            Color(0xFFFFF3CD),
            Color(0xFFFF9800),
            Icons.Default.Info
        )
        NotificationType.INFO -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            Icons.Default.Notifications
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            // Contenido
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notificacion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                
                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (notificacion.timestamp > 0) {
                    Text(
                        text = formatearFecha(notificacion.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Formatea la fecha de la notificación
 */
private fun formatearFecha(timestamp: Long): String {
    if (timestamp == 0L) return ""
    
    val ahora = System.currentTimeMillis()
    val diferencia = ahora - timestamp
    
    return when {
        diferencia < 60_000 -> "Hace un momento"
        diferencia < 3_600_000 -> "Hace ${diferencia / 60_000} minutos"
        diferencia < 86_400_000 -> "Hace ${diferencia / 3_600_000} horas"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
