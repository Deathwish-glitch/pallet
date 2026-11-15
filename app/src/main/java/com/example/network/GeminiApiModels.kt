package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request payload for the Gemini generateContent API.
 */
@JsonClass(generateAdapter = true)
data class GeminiRequest(
  @field:Json(name = "contents") val contents: List<GeminiContent>,
  @field:Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
  @field:Json(name = "parts") val parts: List<GeminiPart>,
  @field:Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
  @field:Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
  @field:Json(name = "response_mime_type") val responseMimeType: String? = "application/json",
  @field:Json(name = "temperature") val temperature: Float? = 0.4f
)

/**
 * Response payload from the Gemini generateContent API.
 */
@JsonClass(generateAdapter = true)
data class GeminiResponse(
  @field:Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
  @field:Json(name = "content") val content: GeminiContent? = null,
  @field:Json(name = "finishReason") val finishReason: String? = null
)

/**
 * Structured Design System Configuration model returned by Gemini AI.
 */
@JsonClass(generateAdapter = true)
data class DesignSystemConfig(
  @field:Json(name = "name") val name: String = "Modern Design System",
  @field:Json(name = "description") val description: String = "AI-generated design system configuration.",
  @field:Json(name = "category") val category: String = "General",
  @field:Json(name = "primaryHex") val primaryHex: String = "#6B7F5E",
  @field:Json(name = "secondaryHex") val secondaryHex: String = "#9E8B6D",
  @field:Json(name = "neutralHex") val neutralHex: String = "#737672",
  @field:Json(name = "successHex") val successHex: String = "#19875A",
  @field:Json(name = "warningHex") val warningHex: String = "#C78B26",
  @field:Json(name = "errorHex") val errorHex: String = "#D94A4A",
  @field:Json(name = "fontFamily") val fontFamily: String = "Inter",
  @field:Json(name = "archetype") val archetype: String = "Architectural Minimalist",
  @field:Json(name = "baseSpacingDp") val baseSpacingDp: Int = 4,
  @field:Json(name = "baseRadiusDp") val baseRadiusDp: Int = 8
)
