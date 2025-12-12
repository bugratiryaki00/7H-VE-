package com.example.proto7hive.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandYellowDark, // #EFAF20
    background = BrandBackgroundDark, // #1D1C1C
    surface = BrandBackgroundDark,
    onPrimary = Color.Black,
    onBackground = BrandTextDark, // #FFFFFF
    onSurface = BrandTextDark,
    secondary = BrandSlateDark,
    tertiary = BrandTealDark
)

private val LightColorScheme = lightColorScheme(
    primary = BrandYellow,
    secondary = BrandSlate,
    tertiary = BrandTeal,
)

@Composable
fun Proto7HiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Marka öncelikli: dinamik rengi kapatıyoruz; istenirse açılabilir
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}