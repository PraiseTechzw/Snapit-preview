package com.snaptool.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Always-dark scheme – SnapTool is a camera/recording app, dark looks best ──
private val SnapToolDarkColors = darkColorScheme(
    primary            = Indigo60,
    onPrimary          = Color.White,
    primaryContainer   = Indigo30,
    onPrimaryContainer = Indigo90,

    secondary            = Violet60,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF2D1E5C),
    onSecondaryContainer = Violet70,

    tertiary            = Cyan50,
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFF003344),
    onTertiaryContainer = Cyan70,

    error            = ErrorRed,
    onError          = Color.White,
    errorContainer   = Color(0xFF4A0A0A),
    onErrorContainer = Color(0xFFFFB4AB),

    background         = Surface0,
    onBackground       = Color(0xFFE8E8F8),

    surface            = Surface1,
    onSurface          = Color(0xFFE8E8F8),
    surfaceVariant     = Surface2,
    onSurfaceVariant   = Color(0xFFB0B0CC),

    outline            = Color(0xFF3A3A6A),
    outlineVariant     = Color(0xFF252550),

    surfaceTint        = Indigo60,
    inverseSurface     = Indigo90,
    inverseOnSurface   = Surface1,
    inversePrimary     = Indigo50,
    scrim              = Color(0xCC000000)
)

@Composable
fun SnapToolTheme(
    darkTheme: Boolean = true,                // force dark – looks great for a media app
    dynamicColor: Boolean = false,            // skip Material You – use our brand palette
    content: @Composable () -> Unit
) {
    val colorScheme = SnapToolDarkColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make status bar transparent so our gradient shows through
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Surface0.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
