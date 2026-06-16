package com.joyproxy.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object JoyProxyColors {
    val Indigo = Color(0xFF4F46E5)
    val Violet = Color(0xFF7C3AED)
    val PurpleDeep = Color(0xFF5B21B6)
    val BlueAccent = Color(0xFF818CF8)
    val Surface = Color(0xFFF8FAFC)
    val SurfaceCard = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFEEF2FF)
    val OnGradient = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF64748B)
    val Success = Color(0xFF10B981)
    val SuccessBg = Color(0xFFECFDF5)
    val Warning = Color(0xFFF59E0B)
    val WarningBg = Color(0xFFFFFBEB)
    val Error = Color(0xFFEF4444)
    val ErrorBg = Color(0xFFFEF2F2)
    val Border = Color(0xFFE2E8F0)

    val BrandGradient =
        Brush.linearGradient(
            colors = listOf(Indigo, Violet, Color(0xFF9333EA)),
        )
}

private val LightColorScheme =
    lightColorScheme(
        primary = JoyProxyColors.Indigo,
        onPrimary = Color.White,
        primaryContainer = JoyProxyColors.SurfaceMuted,
        onPrimaryContainer = JoyProxyColors.PurpleDeep,
        secondary = JoyProxyColors.Violet,
        onSecondary = Color.White,
        background = JoyProxyColors.Surface,
        onBackground = JoyProxyColors.TextPrimary,
        surface = JoyProxyColors.SurfaceCard,
        onSurface = JoyProxyColors.TextPrimary,
        surfaceVariant = JoyProxyColors.SurfaceMuted,
        onSurfaceVariant = JoyProxyColors.TextSecondary,
        outline = JoyProxyColors.Border,
        error = JoyProxyColors.Error,
    )

private val JoyProxyShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )

@Composable
fun JoyProxyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        shapes = JoyProxyShapes,
        content = content,
    )
}
