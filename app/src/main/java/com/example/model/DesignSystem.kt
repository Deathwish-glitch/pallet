package com.example.model

data class ColorShade(
  val shade: Int, // 50, 100, 200, 300, 400, 500, 600, 700, 800, 900
  val hex: String,
  val name: String = "",
  val isLocked: Boolean = false,
  val role: String = ""
)

data class ColorScale(
  val categoryName: String, // "Primary", "Secondary", "Neutral", "Success", "Warning", "Error"
  val baseHex: String,
  val shades: List<ColorShade>
)

data class TypographyScale(
  val fontFamily: String = "Inter",
  val h1Sp: Int = 40,
  val h2Sp: Int = 32,
  val h3Sp: Int = 24,
  val bodySp: Int = 16,
  val captionSp: Int = 12,
  val sampleText: String = "The quick brown fox jumps over the lazy dog."
)

data class SpacingScale(
  val baseUnit: Int = 4,
  val scaleDp: List<Int> = listOf(2, 4, 8, 12, 16, 24, 32, 48, 64)
)

data class RadiusScale(
  val none: Int = 0,
  val sm: Int = 4,
  val md: Int = 8,
  val lg: Int = 16,
  val xl: Int = 24,
  val full: Int = 999
)

data class DesignToken(
  val name: String,
  val category: String, // "color", "typography", "spacing", "radius", "shadow"
  val value: String,
  val resolvedValue: String,
  val description: String = ""
)

data class DesignSystem(
  val id: String,
  val name: String,
  val description: String,
  val category: String,
  val heroImageRes: Int? = null,
  val primaryHex: String = "#6B7F5E",
  val primaryScale: List<ColorShade>,
  val secondaryScale: List<ColorShade>,
  val neutralScale: List<ColorShade>,
  val successScale: List<ColorShade>,
  val warningScale: List<ColorShade>,
  val errorScale: List<ColorShade>,
  val typography: TypographyScale = TypographyScale(),
  val spacing: SpacingScale = SpacingScale(),
  val radius: RadiusScale = RadiusScale(),
  val tokens: List<DesignToken> = emptyList(),
  val updatedAt: String = "Just now",
  val version: Int = 1
)

enum class MessageRole {
  USER, ASSISTANT
}

data class DesignSystemProposal(
  val systemName: String,
  val summary: String,
  val primaryHex: String,
  val typographyFont: String,
  val scalesCount: Int = 8,
  val tokensCount: Int = 42,
  val componentsCount: Int = 18,
  val generatedSystem: DesignSystem
)

data class ChatMessage(
  val id: String,
  val role: MessageRole,
  val text: String,
  val timestamp: String = "Just now",
  val proposal: DesignSystemProposal? = null,
  val isApplied: Boolean = false
)
