package com.example.falconplayer.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = FalconRed,
    onPrimary = Color.White,
    primaryContainer = FalconRedDark,
    onPrimaryContainer = Color.White,
    secondary = FalconRedLight,
    onSecondary = Color.White,
    background = FalconBackground,
    onBackground = FalconTextPrimary,
    surface = FalconSurface,
    onSurface = FalconTextPrimary,
    surfaceVariant = FalconSurfaceVariant,
    onSurfaceVariant = FalconTextSecondary,
  )

@Composable
fun FalconPlayerTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

