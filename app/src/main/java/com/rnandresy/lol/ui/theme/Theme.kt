package com.rnandresy.lol.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AskipPurple = Color(0xFF7C4DFF)
val AskipPink   = Color(0xFFE91E63)
val AskipCyan   = Color(0xFF00E5FF)
val DarkBg      = Color(0xFF07070F)
val DarkSurf    = Color(0xFF111128)
val DarkSurfV   = Color(0xFF1C1C3A)
val DarkOutl    = Color(0xFF363660)

private val DarkScheme = darkColorScheme(
    primary             = AskipPurple,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF3700B3),
    onPrimaryContainer  = Color(0xFFBB86FC),
    secondary           = AskipCyan,
    onSecondary         = Color.Black,
    secondaryContainer  = Color(0xFF00363D),
    onSecondaryContainer = AskipCyan,
    tertiary            = AskipPink,
    onTertiary          = Color.White,
    background          = DarkBg,
    onBackground        = Color.White,
    surface             = DarkSurf,
    onSurface           = Color.White,
    surfaceVariant      = DarkSurfV,
    onSurfaceVariant    = Color(0xFFB0B0D8),
    outline             = DarkOutl,
    error               = Color(0xFFFF4444),
    onError             = Color.White
)

private val LightScheme = lightColorScheme(
    primary             = AskipPurple,
    onPrimary           = Color.White,
    secondary           = Color(0xFF018786),
    tertiary            = AskipPink,
    background          = Color(0xFFF0EDFF),
    onBackground        = Color(0xFF111122),
    surface             = Color.White,
    onSurface           = Color(0xFF111122),
    surfaceVariant      = Color(0xFFEBE8FF),
    onSurfaceVariant    = Color(0xFF44446A),
    outline             = Color(0xFFCCCCEE),
    error               = Color(0xFFCC0000)
)

@Composable
fun AskipTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        typography  = Typography(
            displayLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp),
            titleLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 20.sp),
            titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 16.sp),
            titleSmall    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 14.sp),
            bodyLarge     = TextStyle(fontSize = 16.sp),
            bodyMedium    = TextStyle(fontSize = 14.sp),
            bodySmall     = TextStyle(fontSize = 12.sp),
            labelLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
            labelMedium   = TextStyle(fontSize = 12.sp),
            labelSmall    = TextStyle(fontSize = 11.sp)
        ),
        content = content
    )
}