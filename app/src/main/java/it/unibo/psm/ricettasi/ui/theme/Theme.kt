package it.unibo.psm.ricettasi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColors(
    val statusOk: Color,
    val statusWarning: Color,
    val statusCritical: Color
)

private val LightCustomColors = CustomColors(
    statusOk = StatusOk,
    statusWarning = StatusWarning,
    statusCritical = StatusCritical
)

private val DarkCustomColors = CustomColors(
    statusOk = StatusOkDark,
    statusWarning = StatusWarningDark,
    statusCritical = StatusCriticalDark
)

private val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }

val MaterialTheme.customColors: CustomColors
    @Composable get() = LocalCustomColors.current

private val BaseLightColorScheme = lightColorScheme(
    primary = Accent,
    background = Background,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary,
    error = StatusCritical
)

private val BaseDarkColorScheme = darkColorScheme(
    primary = AccentDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextSecondaryDark,
    error = StatusCriticalDark
)

@Composable
fun RicettaSiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors
    val colorScheme = if (darkTheme) BaseDarkColorScheme else BaseLightColorScheme

    CompositionLocalProvider(
        LocalCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}