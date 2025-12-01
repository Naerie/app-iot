package com.dispensador.app.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Indicador visual del nivel de agua
 */
@Composable
fun WaterLevelIndicator(
    nivel: Int,
    modifier: Modifier = Modifier
) {
    val nivelColor = when {
        nivel > 50 -> Color(0xFF4CAF50) // Verde
        nivel > 20 -> Color(0xFFFF9800) // Naranja
        else -> Color(0xFFF44336) // Rojo
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nivel de Agua",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width(60.dp)
                .height(200.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFCCCCCC),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(4.dp)
        ) {
            // Fondo vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(26.dp)
                    )
            )

            // Nivel de agua
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(nivel / 100f)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                nivelColor.copy(alpha = 0.7f),
                                nivelColor
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$nivel%",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = nivelColor
        )
    }
}
