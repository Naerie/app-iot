package com.dispensador.app.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Botón de control personalizado para cantidades rápidas
 */
@Composable
fun ControlButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF00D2CF),
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = Color(0xFFCCCCCC),
            disabledContentColor = Color(0xFF999999)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Botón de control secundario (para acciones menos importantes)
 */
@Composable
fun SecondaryControlButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    ControlButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        backgroundColor = Color(0xFFF5F5F5),
        contentColor = Color(0xFF333333)
    )
}

/**
 * Botón de control de peligro (para acciones destructivas)
 */
@Composable
fun DangerControlButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    ControlButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        backgroundColor = Color(0xFFF44336),
        contentColor = Color.White
    )
}
