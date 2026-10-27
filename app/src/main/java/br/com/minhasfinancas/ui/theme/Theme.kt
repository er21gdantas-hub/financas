package br.com.minhasfinancas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Claro = lightColorScheme(
    primary = Color(0xFF0F6B5C), onPrimary = Color.White,
    error = Color(0xFFB4412F),
    background = Color(0xFFF2F5F3), surface = Color(0xFFF2F5F3),
)
private val Escuro = darkColorScheme(
    primary = Color(0xFF4FC3AD), onPrimary = Color(0xFF00382F),
    error = Color(0xFFEF8A78),
    background = Color(0xFF0E1512), surface = Color(0xFF0E1512),
)

/** Segue o modo claro/escuro do aparelho. */
@Composable
fun MinhasFinancasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Escuro else Claro, content = content)
}
