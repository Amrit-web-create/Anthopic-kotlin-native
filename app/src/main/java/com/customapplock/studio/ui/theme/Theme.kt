package com.customapplock.studio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Black + blue design system. Legacy names (Sapphire, Cyan...) are kept so the lock designs share it. */
object Palette {
    val Bg = Color(0xFF05080F)
    val Surface = Color(0xFF0B1424)
    val SurfaceHigh = Color(0xFF101C33)
    val Border = Color(0xFF1B2A47)
    val Blue = Color(0xFF3B82F6)
    val BlueLight = Color(0xFF60A5FA)
    val BlueDeep = Color(0xFF1D4ED8)
    val Sky = Color(0xFF38BDF8)
    val Indigo = Color(0xFF6366F1)
    val Rose = Color(0xFFF43F5E)
    val Ok = Color(0xFF34D399)

    // legacy aliases
    val Sapphire = Bg
    val SapphireLight = SurfaceHigh
    val Cyan = BlueLight
    val Purple = Indigo
    val Emerald = Sky
    val Amber = Blue
}

private val Scheme = darkColorScheme(
    primary = Palette.Blue, onPrimary = Color.White,
    primaryContainer = Color(0xFF12306B), onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Palette.BlueLight, onSecondary = Palette.Bg,
    secondaryContainer = Color(0xFF14284F), onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = Palette.Sky, onTertiary = Palette.Bg,
    tertiaryContainer = Color(0xFF0C3A52), onTertiaryContainer = Color(0xFFE0F2FE),
    background = Palette.Bg, onBackground = Color(0xFFE6EDF7),
    surface = Palette.Bg, onSurface = Color(0xFFE6EDF7),
    surfaceVariant = Palette.Surface, onSurfaceVariant = Color(0xFF8EA0BE),
    outline = Palette.Border, outlineVariant = Palette.Border,
    error = Palette.Rose, onError = Color.White,
    errorContainer = Color(0xFF4C1020), onErrorContainer = Color(0xFFFFD9E0),
    surfaceContainerLowest = Color(0xFF03050A), surfaceContainerLow = Color(0xFF080E1B),
    surfaceContainer = Color(0xFF0B1424), surfaceContainerHigh = Color(0xFF101C33),
    surfaceContainerHighest = Color(0xFF16253F)
)

private val Serif = FontFamily.Serif

private val AppTypography = Typography().let {
    it.copy(
        displaySmall = it.displaySmall.copy(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp),
        headlineLarge = it.headlineLarge.copy(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp),
        headlineMedium = it.headlineMedium.copy(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
        headlineSmall = it.headlineSmall.copy(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 31.sp),
        titleLarge = it.titleLarge.copy(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
        titleMedium = it.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
        bodyLarge = it.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp),
        bodyMedium = it.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
        bodySmall = it.bodySmall.copy(fontSize = 13.sp),
        labelMedium = it.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
    )
}

/** The app is always dark (black + blue); [darkTheme] is only kept so old call sites compile. */
@Composable
fun AppLockTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
}
