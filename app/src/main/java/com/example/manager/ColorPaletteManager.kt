package com.example.manager

import com.example.model.*
import com.example.network.DesignSystemConfig
import com.example.network.GeminiDesignSystemService
import com.example.network.GeminiRetrofitClient
import com.example.util.ColorUtils
import okhttp3.OkHttpClient

enum class PaletteSource {
  GEMINI_AI,
  LOCAL_HEURISTIC
}

data class GeneratedPaletteResult(
  val designSystem: DesignSystem,
  val source: PaletteSource,
  val explanation: String,
  val archetype: String = "Modern"
)

/**
 * ColorPaletteManager generates professional design systems and palettes using
 * the Retrofit Gemini API service with structured JSON output, paired with a mathematical shade
 * generation engine spanning 50 to 950.
 */
class ColorPaletteManager(
  val geminiService: GeminiDesignSystemService = GeminiDesignSystemService()
) {

  constructor(client: OkHttpClient) : this(
    geminiService = GeminiDesignSystemService(GeminiRetrofitClient.createService(client = client))
  )

  /**
   * Generates a 50 to 950 shade family for a given base color hex.
   * Produces shades: 50, 100, 200, 300, 400, 500 (base), 600, 700, 800, 900, 950.
   */
  fun generateShades(baseHex: String, category: String = "primary"): List<ColorShade> {
    return ColorUtils.generateShades(baseHex, category)
  }

  /**
   * Explicit convenience function exposing the 50 to 950 shade generation.
   */
  fun generateShades50to950(baseHex: String, category: String = "primary"): List<ColorShade> {
    return generateShades(baseHex, category)
  }

  /**
   * Calculates a complete 50-950 shade scale for generated base colors.
   * Produces 11 calibrated shades: 50, 100, 200, 300, 400, 500 (base), 600, 700, 800, 900, 950.
   */
  fun calculateShadeScale(baseHex: String, category: String = "primary"): List<ColorShade> {
    return generateShades(baseHex, category)
  }

  /**
   * Calculates a complete 50-950 shade scale for generated base colors (explicit naming).
   */
  fun calculateShadeScale50to950(baseHex: String, category: String = "primary"): List<ColorShade> {
    return generateShades(baseHex, category)
  }

  /**
   * Generates a professional design palette using the Gemini Retrofit API based on natural language input,
   * calculating complete 50-950 shade scales for all generated base colors.
   */
  suspend fun generateDesignPalette(prompt: String): GeneratedPaletteResult {
    return generatePalette(prompt)
  }

  /**
   * Uses Gemini Retrofit API to generate a professional design palette from a natural language prompt.
   * If Gemini API key is absent or network fails, falls back gracefully to a color-theory heuristic engine.
   */
  suspend fun generatePalette(prompt: String): GeneratedPaletteResult {
    return geminiService.generateCompleteDesignSystem(prompt)
  }

  /**
   * Directly calls the Retrofit Gemini service to obtain structured DesignSystemConfig DTO.
   */
  suspend fun generateDesignSystemConfig(prompt: String): Result<DesignSystemConfig> {
    return geminiService.generateDesignSystemConfig(prompt)
  }
}
