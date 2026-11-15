package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = PaletteBrandDark,
  onPrimary = Color.White,
  primaryContainer = PaletteBrandOliveLight,
  onPrimaryContainer = PaletteBrandDark,
  secondary = PaletteBrandOlive,
  onSecondary = Color.White,
  background = PaletteBg,
  onBackground = PaletteTextPrimary,
  surface = PaletteSurface,
  onSurface = PaletteTextPrimary,
  surfaceVariant = PaletteSurfaceSecondary,
  onSurfaceVariant = PaletteTextSecondary,
  outline = PaletteMutedBorder,
  error = PaletteError,
  onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
  primary = PaletteBrandOlive,
  onPrimary = Color.White,
  primaryContainer = PaletteSurfaceSecondaryDark,
  onPrimaryContainer = PaletteTextPrimaryDark,
  secondary = PaletteAccent,
  onSecondary = Color.White,
  background = PaletteBgDark,
  onBackground = PaletteTextPrimaryDark,
  surface = PaletteSurfaceDark,
  onSurface = PaletteTextPrimaryDark,
  surfaceVariant = PaletteSurfaceSecondaryDark,
  onSurfaceVariant = PaletteTextSecondaryDark,
  outline = PaletteMutedBorderDark,
  error = PaletteError,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
