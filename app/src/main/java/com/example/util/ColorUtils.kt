package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.model.ColorShade
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

object ColorUtils {

  fun parseHex(hex: String, fallback: Color = Color(0xFF6B7F5E)): Color {
    return try {
      val cleanHex = hex.trim().removePrefix("#")
      when (cleanHex.length) {
        6 -> {
          val r = cleanHex.substring(0, 2).toInt(16)
          val g = cleanHex.substring(2, 4).toInt(16)
          val b = cleanHex.substring(4, 6).toInt(16)
          Color(r, g, b)
        }
        8 -> {
          val a = cleanHex.substring(0, 2).toInt(16)
          val r = cleanHex.substring(2, 4).toInt(16)
          val g = cleanHex.substring(4, 6).toInt(16)
          val b = cleanHex.substring(6, 8).toInt(16)
          Color(r, g, b, a)
        }
        3 -> {
          val r = cleanHex.substring(0, 1).repeat(2).toInt(16)
          val g = cleanHex.substring(1, 2).repeat(2).toInt(16)
          val b = cleanHex.substring(2, 3).repeat(2).toInt(16)
          Color(r, g, b)
        }
        else -> fallback
      }
    } catch (_: Exception) {
      fallback
    }
  }

  fun toHex(color: Color, includeHash: Boolean = true): String {
    val r = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val g = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val b = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    val hex = String.format("%02X%02X%02X", r, g, b)
    return if (includeHash) "#$hex" else hex
  }

  // Convert RGB (0..1) to HSL (H: 0..360, S: 0..1, L: 0..1)
  fun rgbToHsl(r: Float, g: Float, b: Float): FloatArray {
    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    val delta = max - min
    var h = 0f
    val l = (max + min) / 2f
    var s = 0f

    if (delta != 0f) {
      s = if (l < 0.5f) delta / (max + min) else delta / (2f - max - min)
      h = when (max) {
        r -> ((g - b) / delta + (if (g < b) 6f else 0f))
        g -> ((b - r) / delta + 2f)
        else -> ((r - g) / delta + 4f)
      } * 60f
    }
    return floatArrayOf(h, s, l)
  }

  // Convert HSL back to Color
  fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f

    val (r1, g1, b1) = when {
      h < 60f -> Triple(c, x, 0f)
      h < 120f -> Triple(x, c, 0f)
      h < 180f -> Triple(0f, c, x)
      h < 240f -> Triple(0f, x, c)
      h < 300f -> Triple(x, 0f, c)
      else -> Triple(c, 0f, x)
    }

    val r = (r1 + m).coerceIn(0f, 1f)
    val g = (g1 + m).coerceIn(0f, 1f)
    val b = (b1 + m).coerceIn(0f, 1f)
    return Color(r, g, b)
  }

  // Generate 10 standard Tailwind/M3-compatible shades (50, 100, 200, 300, 400, 500, 600, 700, 800, 900)
  fun generateShades(baseHex: String, category: String = "primary"): List<ColorShade> {
    val baseColor = parseHex(baseHex)
    val hsl = rgbToHsl(baseColor.red, baseColor.green, baseColor.blue)
    val h = hsl[0]
    val baseS = hsl[1]

    val shadeLightness = mapOf(
      50 to 0.95f,
      100 to 0.90f,
      200 to 0.80f,
      300 to 0.70f,
      400 to 0.60f,
      500 to hsl[2], // base color lightness
      600 to 0.40f,
      700 to 0.30f,
      800 to 0.20f,
      900 to 0.12f,
      950 to 0.06f
    )

    val shadeRoles = mapOf(
      50 to "Subtle Tint / Card Bg",
      100 to "Light Surface / Chip",
      200 to "Border / Divider",
      300 to "Secondary Border",
      400 to "Muted Element",
      500 to "Base Brand / Default",
      600 to "Hover State",
      700 to "Active / Pressed",
      800 to "Deep Container",
      900 to "High Contrast Text",
      950 to "Ultra-Dark Surface / Contrast Anchor"
    )

    return shadeLightness.map { (shade, targetL) ->
      if (shade == 500) {
        ColorShade(
          shade = 500,
          hex = toHex(baseColor),
          name = "$category-500",
          role = shadeRoles[500] ?: "Base"
        )
      } else {
        // Boost saturation slightly in mid-range, reduce in very light/dark
        val adjustedS = when {
          targetL > 0.85f -> (baseS * 0.75f).coerceIn(0f, 1f)
          targetL < 0.25f -> (baseS * 0.85f).coerceIn(0f, 1f)
          else -> baseS
        }
        val shadeColor = hslToColor(h, adjustedS, targetL)
        ColorShade(
          shade = shade,
          hex = toHex(shadeColor),
          name = "$category-$shade",
          role = shadeRoles[shade] ?: ""
        )
      }
    }
  }

  // WCAG 2.1 Relative Luminance
  fun calculateLuminance(color: Color): Double {
    fun srgbToLin(v: Float): Double {
      return if (v <= 0.04045f) {
        (v / 12.92).toDouble()
      } else {
        ((v + 0.055) / 1.055).toDouble().pow(2.4)
      }
    }
    val rLin = srgbToLin(color.red)
    val gLin = srgbToLin(color.green)
    val bLin = srgbToLin(color.blue)
    return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin
  }

  // WCAG 2.1 Contrast Ratio: (L1 + 0.05) / (L2 + 0.05)
  fun calculateContrastRatio(fg: Color, bg: Color): Double {
    val l1 = calculateLuminance(fg)
    val l2 = calculateLuminance(bg)
    val lighter = max(l1, l2)
    val darker = min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)
  }

  fun formatRatio(ratio: Double): String {
    return String.format("%.1f:1", ratio)
  }

  fun isAaNormalPassed(ratio: Double): Boolean = ratio >= 4.5
  fun isAaLargePassed(ratio: Double): Boolean = ratio >= 3.0
  fun isAaaNormalPassed(ratio: Double): Boolean = ratio >= 7.0

  // Finds the best accessible shade from a scale for a given background
  fun findAccessibleShade(scale: List<ColorShade>, bg: Color, minRatio: Double = 4.5): ColorShade? {
    return scale.filter { shade ->
      val color = parseHex(shade.hex)
      calculateContrastRatio(color, bg) >= minRatio
    }.maxByOrNull { shade ->
      calculateContrastRatio(parseHex(shade.hex), bg)
    }
  }
}
