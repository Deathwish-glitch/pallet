package com.example

import androidx.compose.ui.graphics.Color
import com.example.data.DesignSystemRepository
import com.example.export.ExportFormat
import com.example.export.ExportGenerator
import com.example.manager.ColorPaletteManager
import com.example.util.ColorUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testColorPaletteManagerShades50to950() {
    val manager = ColorPaletteManager()
    val shades = manager.generateShades50to950("#6B7F5E", "primary")

    assertEquals(11, shades.size)

    val expectedShades = listOf(50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950)
    assertEquals(expectedShades, shades.map { it.shade })

    // Base color 500
    val shade500 = shades.find { it.shade == 500 }
    assertNotNull(shade500)
    assertEquals("#6B7F5E", shade500?.hex)

    // Deepest anchor shade 950
    val shade950 = shades.find { it.shade == 950 }
    assertNotNull(shade950)
    assertEquals("primary-950", shade950?.name)
    assertEquals("Ultra-Dark Surface / Contrast Anchor", shade950?.role)

    // 50 is lighter than 950
    val color50 = ColorUtils.parseHex(shades.first().hex)
    val color950 = ColorUtils.parseHex(shade950!!.hex)
    assertTrue(ColorUtils.calculateLuminance(color50) > ColorUtils.calculateLuminance(color950))

    // Verify calculateShadeScale method
    val calculated = manager.calculateShadeScale("#3B86F5", "primary")
    assertEquals(11, calculated.size)
    assertEquals(50, calculated.first().shade)
    assertEquals(500, calculated[5].shade)
    assertEquals("#3B86F5", calculated[5].hex)
    assertEquals(950, calculated.last().shade)
  }

  @Test
  fun testColorPaletteManagerNaturalLanguageGeneration() = runBlocking {
    val manager = ColorPaletteManager()
    val result = manager.generatePalette("warm earthy aesthetic for a sustainable architecture studio")

    assertNotNull(result)
    assertNotNull(result.designSystem)
    assertTrue(result.designSystem.name.isNotBlank())
    assertEquals(11, result.designSystem.primaryScale.size)
    assertEquals(11, result.designSystem.neutralScale.size)
    assertTrue(result.designSystem.primaryHex.startsWith("#"))
  }

  @Test
  fun testWcagContrastCalculation() {
    val white = Color.White
    val black = Color.Black

    val maxContrast = ColorUtils.calculateContrastRatio(white, black)
    assertTrue("White on black should be ~21:1", maxContrast > 20.0)

    val minContrast = ColorUtils.calculateContrastRatio(white, white)
    assertEquals(1.0, minContrast, 0.01)

    // Primary #6B7F5E against white
    val primaryColor = ColorUtils.parseHex("#6B7F5E")
    val ratio = ColorUtils.calculateContrastRatio(primaryColor, white)
    assertTrue("Should pass AA large", ColorUtils.isAaLargePassed(ratio))
  }

  @Test
  fun testExportCodeGeneration() {
    val repository = DesignSystemRepository()
    val system = repository.getCurrentSystem()

    val css = ExportGenerator.generate(system, ExportFormat.CSS)
    assertTrue(css.contains("--color-primary-500:"))
    assertTrue(css.contains("--font-family-sans:"))

    val tailwind = ExportGenerator.generate(system, ExportFormat.TAILWIND)
    assertTrue(tailwind.contains("module.exports"))
    assertTrue(tailwind.contains("'500':"))

    val json = ExportGenerator.generate(system, ExportFormat.JSON_DTCG)
    assertTrue(json.contains("\$value"))
    assertTrue(json.contains("\$type"))

    val markdown = ExportGenerator.generate(system, ExportFormat.MARKDOWN)
    assertTrue(markdown.contains("# Nordic Studio"))
    assertTrue(markdown.contains("Shade Card"))
  }

  @Test
  fun testAiPromptGenerationWith50to950Scales() {
    val repository = DesignSystemRepository()
    val system = repository.generateNewSystemFromPrompt("Fintech emerald and slate", "Fintech")

    assertEquals("Fintech", system.category)
    assertEquals(11, system.primaryScale.size)
    assertEquals(11, system.neutralScale.size)
    assertEquals(950, system.primaryScale.last().shade)
    assertNotNull(system.primaryHex)
  }

  @Test
  fun testDesignSystemExporterUtility() {
    val repository = DesignSystemRepository()
    val system = repository.getCurrentSystem()

    // 1. Raw shade scale to DTCG JSON
    val shadesJson = com.example.export.DesignSystemExporter.exportSingleScaleToJson("primary", system.primaryScale)
    assertTrue(shadesJson.contains("\"\$type\": \"color\""))
    assertTrue(shadesJson.contains("\"50\""))
    assertTrue(shadesJson.contains("\"950\""))

    // 2. Figma Tokens Studio format
    val figmaTokensJson = com.example.export.DesignSystemExporter.export(system, com.example.export.DesignSystemExporter.Format.FIGMA_TOKENS)
    assertTrue(figmaTokensJson.contains("\"global\":"))
    assertTrue(figmaTokensJson.contains("\"tokenSetOrder\":"))
    assertTrue(figmaTokensJson.contains("\"type\": \"color\""))

    // 3. Figma Variables API collection format
    val figmaVarsJson = com.example.export.DesignSystemExporter.export(system, com.example.export.DesignSystemExporter.Format.FIGMA_VARIABLES)
    assertTrue(figmaVarsJson.contains("\"variableCollections\":"))
    assertTrue(figmaVarsJson.contains("\"primary/500\""))
    assertTrue(figmaVarsJson.contains("\"valuesByMode\":"))

    // 4. CSS Custom Properties with 50-950 scale
    val css = com.example.export.DesignSystemExporter.export(system, com.example.export.DesignSystemExporter.Format.CSS_VARIABLES)
    assertTrue(css.contains("--color-primary-50:"))
    assertTrue(css.contains("--color-primary-500:"))
    assertTrue(css.contains("--color-primary-950:"))
    assertTrue(css.contains("[data-theme=\"dark\"]"))
  }

  @Test
  fun testGeminiRetrofitClientInitialization() {
    val service = com.example.network.GeminiRetrofitClient.service
    assertNotNull(service)

    val customService = com.example.network.GeminiRetrofitClient.createService()
    assertNotNull(customService)
  }

  @Test
  fun testDesignSystemConfigMoshiSerialization() {
    val moshi = com.example.network.GeminiRetrofitClient.moshi
    val adapter = moshi.adapter(com.example.network.DesignSystemConfig::class.java)

    val sampleJson = """
      {
        "name": "Nordic Architectural",
        "description": "Minimal raw concrete and natural timber tones",
        "category": "Architecture",
        "primaryHex": "#6B7F5E",
        "secondaryHex": "#9E8B6D",
        "neutralHex": "#737672",
        "successHex": "#19875A",
        "warningHex": "#C78B26",
        "errorHex": "#D94A4A",
        "fontFamily": "Inter",
        "archetype": "Organic Earth",
        "baseSpacingDp": 4,
        "baseRadiusDp": 8
      }
    """.trimIndent()

    val parsed = adapter.fromJson(sampleJson)
    assertNotNull(parsed)
    assertEquals("Nordic Architectural", parsed?.name)
    assertEquals("#6B7F5E", parsed?.primaryHex)
    assertEquals("Organic Earth", parsed?.archetype)
    assertEquals(4, parsed?.baseSpacingDp)
    assertEquals(8, parsed?.baseRadiusDp)

    // Serialization round-trip
    val jsonOut = adapter.toJson(parsed!!)
    assertTrue(jsonOut.contains("\"name\":\"Nordic Architectural\""))
    assertTrue(jsonOut.contains("\"primaryHex\":\"#6B7F5E\""))
  }

  @Test
  fun testGeminiRequestMoshiSerialization() {
    val moshi = com.example.network.GeminiRetrofitClient.moshi
    val adapter = moshi.adapter(com.example.network.GeminiRequest::class.java)

    val request = com.example.network.GeminiRequest(
      contents = listOf(
        com.example.network.GeminiContent(
          parts = listOf(com.example.network.GeminiPart(text = "Generate design system"))
        )
      ),
      generationConfig = com.example.network.GeminiGenerationConfig(
        responseMimeType = "application/json",
        temperature = 0.4f
      )
    )

    val json = adapter.toJson(request)
    assertTrue(json.contains("Generate design system"))
    assertTrue(json.contains("application/json"))
    assertTrue(json.contains("temperature"))
  }

  @Test
  fun testGeminiDesignSystemServiceHeuristicFallback() = runBlocking {
    val service = com.example.network.GeminiDesignSystemService()
    val result = service.generateCompleteDesignSystem("minimalist architectural studio with warm slate")

    assertNotNull(result)
    assertNotNull(result.designSystem)
    assertEquals(11, result.designSystem.primaryScale.size)
    assertEquals(11, result.designSystem.neutralScale.size)
    assertEquals(50, result.designSystem.primaryScale.first().shade)
    assertEquals(500, result.designSystem.primaryScale[5].shade)
    assertEquals(950, result.designSystem.primaryScale.last().shade)
  }

  @Test
  fun testColorPaletteManagerDelegationToRetrofitService() = runBlocking {
    val manager = ColorPaletteManager()
    val result = manager.generatePalette("modern fintech app with deep emerald")

    assertNotNull(result)
    assertEquals(11, result.designSystem.primaryScale.size)
    assertTrue(result.designSystem.primaryHex.startsWith("#"))
    assertEquals(11, result.designSystem.neutralScale.size)
  }
}

