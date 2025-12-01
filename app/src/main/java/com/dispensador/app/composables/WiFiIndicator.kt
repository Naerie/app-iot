package com.dispensador.app.composables

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
// Corrected imports to use the filled icons
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import androidx.compose.ui.unit.sp




@Composable
fun WiFiIndicator(
    conectado: Boolean,
    intensidad: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val color = when {
        !conectado -> Color(0xFFCCCCCC)
        intensidad >= -60 -> Color(0xFF4CAF50) // Verde
        intensidad >= -70 -> Color(0xFFFF9800) // Naranja
        else -> Color(0xFFF44336) // Rojo
    }

    val intensidadTexto = when {
        !conectado -> "Sin conexión"
        intensidad >= -50 -> "Excelente"
        intensidad >= -60 -> "Buena"
        intensidad >= -70 -> "Regular"
        else -> "Débil"
    }

    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = color.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) { // <-- FIX: Add curly braces to define content
            // Add the Icon inside the Box
            Icon(
                imageVector = if (conectado) Icons.Filled.SignalWifi4Bar else Icons.Filled.SignalWifiOff,
                contentDescription = "Estado del WiFi",
                tint = color,
                modifier = Modifier.size(18.dp) // Adjust size as needed
            )
        }

        if (showLabel) {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = if (conectado) "Conectado" else "Desconectado",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = intensidadTexto,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

/**
 * Indicador compacto de WiFi (solo ícono)
 */
@Composable
fun CompactWiFiIndicator(
    conectado: Boolean,
    intensidad: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        !conectado -> Color(0xFFCCCCCC)
        intensidad >= -60 -> Color(0xFF4CAF50)
        intensidad >= -70 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(36.dp)
            .background(
                color = color.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (conectado) Icons.Filled.SignalWifi4Bar else Icons.Filled.SignalWifiOff,
            contentDescription = "WiFi",
            tint = color,
            modifier = androidx.compose.ui.Modifier.size(20.dp)
        )

    }
}
