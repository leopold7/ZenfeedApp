package com.ddyy.zenfeed.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NightPrimary,
    secondary = NightSecondary,
    tertiary = Pink80,
    background = NightBackground,
    surface = NightSurface,
    onBackground = NightOnSurface,
    onSurface = NightOnSurface,
    onPrimary = NightBackground,
    onSecondary = NightBackground,
    onTertiary = NightBackground
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ZenfeedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    themeColorId: String = "default",
    content: @Composable () -> Unit
) {
    val resolvedThemeColorId = if (themeColorId == "default") "teal" else themeColorId
    val useDynamicColor = false
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }.let { scheme ->
        applyThemeAccentIfNeeded(
            baseScheme = scheme,
            darkTheme = darkTheme,
            themeColorId = resolvedThemeColorId
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun applyThemeAccentIfNeeded(
    baseScheme: androidx.compose.material3.ColorScheme,
    darkTheme: Boolean,
    themeColorId: String
): androidx.compose.material3.ColorScheme {
    val baseAccent = getThemeAccentColor(themeColorId) ?: return baseScheme
    val accent = if (darkTheme) optimizeAccentForDarkMode(baseAccent) else optimizeAccentForLightMode(baseAccent)

    val onAccent = if (accent.luminance() > 0.5f) Color.Black else Color.White

    val primaryContainer = if (darkTheme) {
        blend(accent, baseScheme.surface, 0.22f)
    } else {
        blend(accent, baseScheme.surface, 0.12f)
    }

    val onPrimaryContainer = if (primaryContainer.luminance() > 0.5f) Color.Black else Color.White

    return baseScheme.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = accent,
        onSecondary = onAccent,
        secondaryContainer = primaryContainer,
        onSecondaryContainer = onPrimaryContainer
    )
}

private fun getThemeAccentColor(themeColorId: String): Color? {
    return when (themeColorId) {
        "teal" -> Color(0xFF009688)
        "blue" -> Color(0xFF2196F3)
        "indigo" -> Color(0xFF3F51B5)
        "purple" -> Color(0xFF9C27B0)
        "pink" -> Color(0xFFE91E63)
        "red" -> Color(0xFFF44336)
        "orange" -> Color(0xFFFF9800)
        "amber" -> Color(0xFFFFC107)
        "green" -> Color(0xFF4CAF50)
        "cyan" -> Color(0xFF00BCD4)
        else -> null
    }
}

private fun optimizeAccentForDarkMode(base: Color): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(base.toArgb(), hsl)
    hsl[1] = (hsl[1] * 0.72f).coerceIn(0.18f, 0.65f)
    hsl[2] = hsl[2].coerceIn(0.40f, 0.55f)
    return Color(ColorUtils.HSLToColor(hsl))
}

private fun optimizeAccentForLightMode(base: Color): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(base.toArgb(), hsl)
    hsl[1] = (hsl[1] * 0.76f).coerceIn(0.14f, 0.62f)
    hsl[2] = hsl[2].coerceIn(0.22f, 0.42f)
    return Color(ColorUtils.HSLToColor(hsl))
}

private fun blend(foreground: Color, background: Color, foregroundRatio: Float): Color {
    val ratio = foregroundRatio.coerceIn(0f, 1f)
    return Color(
        ColorUtils.blendARGB(background.toArgb(), foreground.toArgb(), ratio)
    )
}

/**
 * 根据主题模式字符串确定是否使用暗色主题
 * @param themeMode 主题模式，可以是 "light", "dark", "system"
 * @param isSystemDark 系统是否处于暗色模式
 * @return 是否使用暗色主题
 */
fun shouldUseDarkTheme(themeMode: String, isSystemDark: Boolean): Boolean {
    return when (themeMode) {
        "light" -> false
        "dark" -> true
        "system" -> isSystemDark
        else -> isSystemDark
    }
}
