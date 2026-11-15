package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.manager.GeneratedPaletteResult
import com.example.manager.PaletteSource
import com.example.model.*
import com.example.util.ColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Service that uses the Gemini Retrofit API service to generate complete, professional
 * design system configurations from natural language prompts.
 */
class GeminiDesignSystemService(
  private val apiService: GeminiApiService = GeminiRetrofitClient.service
) {

  companion object {
    private const val TAG = "GeminiDesignSysService"
  }

  /**
   * Generates a raw DesignSystemConfig DTO from the Gemini Retrofit API.
   */
  suspend fun generateDesignSystemConfig(prompt: String): Result<DesignSystemConfig> =
    withContext(Dispatchers.IO) {
      val apiKey = getApiKey()
      if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext Result.failure(
          IllegalStateException("GEMINI_API_KEY is not configured in Secrets panel")
        )
      }

      try {
        val systemPrompt = buildSystemPrompt(prompt)
        val request = GeminiRequest(
          contents = listOf(
            GeminiContent(
              parts = listOf(GeminiPart(text = systemPrompt))
            )
          ),
          generationConfig = GeminiGenerationConfig(
            responseMimeType = "application/json",
            temperature = 0.4f
          )
        )

        val response = apiService.generateDesignConfig(apiKey = apiKey, request = request)
        if (!response.isSuccessful) {
          val errorMsg = "Gemini API HTTP ${response.code()}: ${response.errorBody()?.string()}"
          logE(TAG, errorMsg)
          return@withContext Result.failure(Exception(errorMsg))
        }

        val body = response.body()
        val rawText = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
        if (rawText.isNullOrBlank()) {
          return@withContext Result.failure(Exception("Empty candidate response from Gemini API"))
        }

        val cleanJson = if (rawText.startsWith("```")) {
          rawText.substringAfter("\n").substringBeforeLast("```").trim()
        } else {
          rawText
        }

        val adapter = GeminiRetrofitClient.moshi.adapter(DesignSystemConfig::class.java)
        val config = adapter.fromJson(cleanJson)
          ?: return@withContext Result.failure(Exception("Failed to deserialize DesignSystemConfig JSON"))

        Result.success(config)
      } catch (e: Exception) {
        logE(TAG, "Error invoking Gemini Retrofit service", e)
        Result.failure(e)
      }
    }

  /**
   * Generates a full DesignSystem with complete 50–950 shade scales using the Retrofit
   * service, falling back smoothly to local color-theory heuristics if network or keys fail.
   */
  suspend fun generateCompleteDesignSystem(prompt: String): GeneratedPaletteResult =
    withContext(Dispatchers.IO) {
      val remoteResult = generateDesignSystemConfig(prompt)
      if (remoteResult.isSuccess) {
        val config = remoteResult.getOrThrow()
        val designSystem = buildDesignSystemFromConfig(config)
        return@withContext GeneratedPaletteResult(
          designSystem = designSystem,
          source = PaletteSource.GEMINI_AI,
          explanation = config.description,
          archetype = config.archetype
        )
      }

      logW(TAG, "Remote Gemini generation fallback: ${remoteResult.exceptionOrNull()?.message}; using heuristic engine.")
      generateLocalHeuristicPalette(prompt)
    }

  private fun logW(tag: String, msg: String) {
    try {
      android.util.Log.w(tag, msg)
    } catch (_: Throwable) {
      println("[$tag] $msg")
    }
  }

  private fun logE(tag: String, msg: String, tr: Throwable? = null) {
    try {
      android.util.Log.e(tag, msg, tr)
    } catch (_: Throwable) {
      System.err.println("[$tag] $msg: ${tr?.message}")
    }
  }

  private fun buildSystemPrompt(userPrompt: String): String {
    return """
      You are a master design system architect and color theorist specializing in architectural minimalism.
      Given the user's natural language request, generate a cohesive, production-ready design system configuration.
      Return ONLY a valid JSON object strictly matching these field names:
      {
        "name": "Creative system title (e.g. Nordic Architecture, Verdant Slate, Neo Tokyo)",
        "description": "1-2 sentences explaining the color rationale, atmosphere, and visual hierarchy",
        "category": "Architecture / Productivity / Wellness / Fintech / Creative",
        "primaryHex": "#6B7F5E (a valid 6-digit hex code with # for the base 500 brand color)",
        "secondaryHex": "#9E8B6D (harmonious accent hex with #)",
        "neutralHex": "#737672 (subtly tinted neutral grey hex with # to replace scattered greys)",
        "successHex": "#19875A (accessible green hex with #)",
        "warningHex": "#C78B26 (warm amber hex with #)",
        "errorHex": "#D94A4A (accessible coral/red hex with #)",
        "fontFamily": "Inter or Geist or Roboto or Outfit or Playfair Display",
        "archetype": "Organic Earth / Minimalist Tech / High Contrast / Editorial",
        "baseSpacingDp": 4,
        "baseRadiusDp": 8
      }

      User Prompt: "$userPrompt"
    """.trimIndent()
  }

  private fun buildDesignSystemFromConfig(config: DesignSystemConfig): DesignSystem {
    val id = "sys-" + UUID.randomUUID().toString().take(8)
    val primary = validateHex(config.primaryHex, "#6B7F5E")
    val secondary = validateHex(config.secondaryHex, "#9E8B6D")
    val neutral = validateHex(config.neutralHex, "#737672")
    val success = validateHex(config.successHex, "#19875A")
    val warning = validateHex(config.warningHex, "#C78B26")
    val error = validateHex(config.errorHex, "#D94A4A")

    val primaryScale = ColorUtils.generateShades(primary, "primary")
    val secondaryScale = ColorUtils.generateShades(secondary, "secondary")
    val neutralScale = ColorUtils.generateShades(neutral, "neutral")
    val successScale = ColorUtils.generateShades(success, "success")
    val warningScale = ColorUtils.generateShades(warning, "warning")
    val errorScale = ColorUtils.generateShades(error, "error")

    val tokens = mutableListOf<DesignToken>()
    primaryScale.forEach {
      tokens.add(DesignToken("color.primary.${it.shade}", "color", it.hex, it.hex, it.role))
    }
    neutralScale.forEach {
      tokens.add(DesignToken("color.neutral.${it.shade}", "color", it.hex, it.hex, it.role))
    }
    tokens.add(DesignToken("typography.fontFamily", "typography", config.fontFamily, config.fontFamily, "Font family"))
    tokens.add(DesignToken("typography.h1", "typography", "40px", "40px", "Heading 1"))
    tokens.add(DesignToken("typography.body", "typography", "16px", "16px", "Body text"))
    tokens.add(DesignToken("spacing.base", "spacing", "${config.baseSpacingDp}px", "${config.baseSpacingDp}px", "Base grid unit"))
    tokens.add(DesignToken("radius.md", "radius", "${config.baseRadiusDp}px", "${config.baseRadiusDp}px", "Base corner radius"))

    return DesignSystem(
      id = id,
      name = config.name.ifBlank { "PaletteOS Studio" },
      description = config.description,
      category = config.category,
      primaryHex = primary,
      primaryScale = primaryScale,
      secondaryScale = secondaryScale,
      neutralScale = neutralScale,
      successScale = successScale,
      warningScale = warningScale,
      errorScale = errorScale,
      typography = TypographyScale(fontFamily = config.fontFamily.ifBlank { "Inter" }),
      tokens = tokens,
      updatedAt = "Just now",
      version = 1
    )
  }

  private fun validateHex(hex: String, fallback: String): String {
    val clean = hex.trim()
    return if (clean.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))) {
      if (clean.length == 4) {
        "#" + clean[1] + clean[1] + clean[2] + clean[2] + clean[3] + clean[3]
      } else {
        clean.uppercase()
      }
    } else {
      fallback
    }
  }

  private fun generateLocalHeuristicPalette(prompt: String): GeneratedPaletteResult {
    val lower = prompt.lowercase()
    val (primary, secondary, neutral, category, archetype, font) = when {
      lower.contains("earth") || lower.contains("nordic") || lower.contains("construct") || lower.contains("architect") -> {
        Tuple6("#6B7F5E", "#9E8B6D", "#737672", "Architecture", "Organic Earth", "Inter")
      }
      lower.contains("blue") || lower.contains("task") || lower.contains("productiv") -> {
        Tuple6("#3B86F5", "#8B5CF6", "#6B7280", "Productivity", "Modern SaaS", "Inter")
      }
      lower.contains("wellness") || lower.contains("aura") || lower.contains("warm") || lower.contains("peach") -> {
        Tuple6("#D97A53", "#E6B89C", "#78716C", "Wellness", "Calm Pigment", "Outfit")
      }
      lower.contains("dark") || lower.contains("cyber") || lower.contains("night") || lower.contains("neo") -> {
        Tuple6("#5369D9", "#EC4899", "#334155", "Cyber", "High Contrast Dark", "Geist")
      }
      lower.contains("fintech") || lower.contains("emerald") || lower.contains("finance") || lower.contains("money") -> {
        Tuple6("#10B981", "#059669", "#64748B", "Fintech", "Precision Finance", "Inter")
      }
      lower.contains("editorial") || lower.contains("fashion") || lower.contains("luxury") || lower.contains("serif") -> {
        Tuple6("#2B2D42", "#8D99AE", "#6B7280", "Editorial", "Haute Minimal", "Playfair Display")
      }
      lower.contains("coffee") || lower.contains("cafe") || lower.contains("bake") -> {
        Tuple6("#78350F", "#B45309", "#78716C", "Lifestyle", "Warm Roast", "Outfit")
      }
      lower.contains("health") || lower.contains("medic") || lower.contains("clinic") -> {
        Tuple6("#0284C7", "#0D9488", "#64748B", "Healthcare", "Clinical Trust", "Inter")
      }
      else -> {
        Tuple6("#4F46E5", "#A855F7", "#64748B", "SaaS", "Modern Digital", "Inter")
      }
    }

    val words = prompt.split(" ").filter { it.isNotBlank() }
    val title = words.take(2).joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } + " System"

    val config = DesignSystemConfig(
      name = title.ifBlank { "PaletteOS Studio" },
      description = "Engineered design system with 50–950 shade family tailored to \"$prompt\".",
      category = category,
      primaryHex = primary,
      secondaryHex = secondary,
      neutralHex = neutral,
      successHex = "#19875A",
      warningHex = "#C78B26",
      errorHex = "#D94A4A",
      fontFamily = font,
      archetype = archetype
    )

    val designSystem = buildDesignSystemFromConfig(config)
    return GeneratedPaletteResult(
      designSystem = designSystem,
      source = PaletteSource.LOCAL_HEURISTIC,
      explanation = config.description,
      archetype = archetype
    )
  }

  private fun getApiKey(): String {
    return try {
      BuildConfig.GEMINI_API_KEY
    } catch (_: Exception) {
      ""
    }
  }

  private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
  )
}
