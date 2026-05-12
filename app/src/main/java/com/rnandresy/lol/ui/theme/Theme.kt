package com.rnandresy.lol.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Couleur admin — dorée dans TOUS les thèmes ────────────────────────────────
val AdminGold        = Color(0xFFFFD700)
val AdminGoldLight   = Color(0xFFFFE566)
val AdminGoldDim     = Color(0xFFB8860B)

// ─────────────────────────────────────────────────────────────────────────────
// THÈME 1 — NOIR & BLANC (défaut)
// ─────────────────────────────────────────────────────────────────────────────

private val BW_Bg        = Color(0xFF080808)
private val BW_Surf      = Color(0xFF111111)
private val BW_SurfV     = Color(0xFF1C1C1C)
private val BW_SurfTint  = Color(0xFF242424)
private val BW_Primary   = Color(0xFFFFFFFF)
private val BW_OnPrimary = Color(0xFF000000)
private val BW_Gray      = Color(0xFF888888)
private val BW_LightGray = Color(0xFFAAAAAA)
private val BW_Outline   = Color(0xFF2E2E2E)
private val BW_Error     = Color(0xFFFF4444)

private val BlackWhiteScheme = darkColorScheme(
    primary             = BW_Primary,
    onPrimary           = BW_OnPrimary,
    primaryContainer    = BW_SurfV,
    onPrimaryContainer  = BW_Primary,
    secondary           = BW_Gray,
    onSecondary         = BW_Primary,
    secondaryContainer  = BW_SurfTint,
    onSecondaryContainer = BW_LightGray,
    tertiary            = BW_LightGray,
    onTertiary          = BW_OnPrimary,
    tertiaryContainer   = BW_SurfV,
    background          = BW_Bg,
    onBackground        = BW_Primary,
    surface             = BW_Surf,
    onSurface           = BW_Primary,
    surfaceVariant      = BW_SurfV,
    onSurfaceVariant    = BW_LightGray,
    outline             = BW_Outline,
    error               = BW_Error,
    onError             = Color.White,
    errorContainer      = Color(0xFF330000),
    onErrorContainer    = BW_Error
)

// ─────────────────────────────────────────────────────────────────────────────
// THÈME 2 — NÉON MODERNE
// ─────────────────────────────────────────────────────────────────────────────

private val Neo_Bg        = Color(0xFF040408)
private val Neo_Surf      = Color(0xFF08080F)
private val Neo_SurfV     = Color(0xFF0E0E1A)
private val Neo_SurfTint  = Color(0xFF14142A)
private val Neo_Purple    = Color(0xFFAA55FF)
private val Neo_Cyan      = Color(0xFF00EEFF)
private val Neo_Pink      = Color(0xFFFF2D78)
private val Neo_Outline   = Color(0xFF2A1A4A)
private val Neo_OnBg      = Color(0xFFE8E8FF)

private val NeonScheme = darkColorScheme(
    primary             = Neo_Purple,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF220055),
    onPrimaryContainer  = Neo_Purple,
    secondary           = Neo_Cyan,
    onSecondary         = Color(0xFF001A1F),
    secondaryContainer  = Color(0xFF002530),
    onSecondaryContainer = Neo_Cyan,
    tertiary            = Neo_Pink,
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFF30001A),
    background          = Neo_Bg,
    onBackground        = Neo_OnBg,
    surface             = Neo_Surf,
    onSurface           = Neo_OnBg,
    surfaceVariant      = Neo_SurfV,
    onSurfaceVariant    = Color(0xFFAA99CC),
    outline             = Neo_Outline,
    error               = Color(0xFFFF4466),
    onError             = Color.White,
    errorContainer      = Color(0xFF2A0011),
    onErrorContainer    = Color(0xFFFF4466)
)

// ─────────────────────────────────────────────────────────────────────────────
// THÈME 3 — NOSTALGIQUE
// ─────────────────────────────────────────────────────────────────────────────

private val Nos_Bg        = Color(0xFF0C0804)
private val Nos_Surf      = Color(0xFF18110A)
private val Nos_SurfV     = Color(0xFF241A0F)
private val Nos_SurfTint  = Color(0xFF2E2014)
private val Nos_Gold      = Color(0xFFC8A96E)
private val Nos_Amber     = Color(0xFF8B6A3E)
private val Nos_Parchment = Color(0xFFE8D5B0)
private val Nos_Outline   = Color(0xFF3D2B14)

private val NostalgicScheme = darkColorScheme(
    primary             = Nos_Gold,
    onPrimary           = Color(0xFF1A0F00),
    primaryContainer    = Color(0xFF3D2800),
    onPrimaryContainer  = Nos_Gold,
    secondary           = Nos_Amber,
    onSecondary         = Color(0xFF1A0F00),
    secondaryContainer  = Color(0xFF2A1A08),
    onSecondaryContainer = Nos_Gold,
    tertiary            = Color(0xFFB09060),
    onTertiary          = Color(0xFF1A0F00),
    background          = Nos_Bg,
    onBackground        = Nos_Parchment,
    surface             = Nos_Surf,
    onSurface           = Nos_Parchment,
    surfaceVariant      = Nos_SurfV,
    onSurfaceVariant    = Color(0xFFAA9070),
    outline             = Nos_Outline,
    error               = Color(0xFFCC4444),
    onError             = Color.White,
    errorContainer      = Color(0xFF2A0A00),
    onErrorContainer    = Color(0xFFCC4444)
)

// ─────────────────────────────────────────────────────────────────────────────
// ENUM + COMPOSABLE PRINCIPAL
// ─────────────────────────────────────────────────────────────────────────────

enum class AppTheme(val displayName: String, val emoji: String) {
    BLACK_WHITE("Noir & Blanc", "◼️"),
    NEON("Néon", "💜"),
    NOSTALGIC("Nostalgique", "🕯️")
}

@Composable
fun AskipTheme(
    appTheme: AppTheme = AppTheme.BLACK_WHITE,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.BLACK_WHITE -> BlackWhemeScheme
        AppTheme.NEON        -> NeonScheme
        AppTheme.NOSTALGIC   -> NostalgicScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AskipTypography,
        content     = content
    )
}

private val BlackWhemeScheme = BlackWhiteScheme

val AskipTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.5).sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 20.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 16.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 14.sp),
    bodyLarge     = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium   = TextStyle(fontSize = 12.sp),
    labelSmall    = TextStyle(fontSize = 11.sp)
)