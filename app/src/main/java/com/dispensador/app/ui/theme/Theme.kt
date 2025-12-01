package com.dispensador.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores unificada
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00D2CF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF006064),
    
    secondary = Color(0xFF00A8A6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF80DEEA),
    onSecondaryContainer = Color(0xFF004D40),
    
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color.White,
    
    error = Color(0xFFF44336),
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C),
    
    background = Color(0xFFFDFEFE),
    onBackground = Color(0xFF333333),
    
    surface = Color.White,
    onSurface = Color(0xFF333333),
    surfaceVariant = Color(0xFFF8FAFA),
    onSurfaceVariant = Color(0xFF666666),
    
    outline = Color(0xFFCCCCCC)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D2CF),
    onPrimary = Color(0xFF003735),
    primaryContainer = Color(0xFF004D4A),
    onPrimaryContainer = Color(0xFFB2EBF2),
    
    secondary = Color(0xFF00A8A6),
    onSecondary = Color(0xFF003735),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFF80DEEA),
    
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color(0xFF1B5E20),
    
    error = Color(0xFFF44336),
    onError = Color(0xFF5D0000),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFCDD2),
    
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE3E2E6),
    
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CE),
    
    outline = Color(0xFF8C9199)
)

@Composable
fun DispensadorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
