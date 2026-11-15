package com.example.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.ColorShade
import com.example.model.DesignSystem
import com.example.util.ColorUtils
import java.io.File
import java.io.FileWriter

/**
 * DesignSystemExporter converts generated color shade scales (50-950) and design
 * tokens into production-ready formats: DTCG standard JSON, Figma Tokens Studio JSON,
 * Figma Variables API JSON, CSS Custom Properties, and Tailwind configurations.
 */
object DesignSystemExporter {

  /**
   * Supported export target formats.
   */
  enum class Format(val displayName: String, val fileExtension: String, val mimeType: String) {
    JSON_DTCG("Design Tokens (DTCG JSON)", "json", "application/json"),
    FIGMA_TOKENS("Figma Tokens Studio JSON", "json", "application/json"),
    FIGMA_VARIABLES("Figma Variables API JSON", "json", "application/json"),
    CSS_VARIABLES("CSS Custom Properties", "css", "text/css"),
    TAILWIND_CONFIG("Tailwind CSS Theme", "js", "application/javascript"),
    COMPOSE_KOTLIN("Jetpack Compose Color.kt", "kt", "text/x-kotlin")
  }

  /**
   * Main export dispatcher for a complete DesignSystem.
   */
  fun export(system: DesignSystem, format: Format): String {
    return when (format) {
      Format.JSON_DTCG -> exportToJsonDtcg(system)
      Format.FIGMA_TOKENS -> exportToFigmaTokens(system)
      Format.FIGMA_VARIABLES -> exportToFigmaVariables(system)
      Format.CSS_VARIABLES -> exportToCssVariables(system)
      Format.TAILWIND_CONFIG -> exportToTailwind(system)
      Format.COMPOSE_KOTLIN -> exportToComposeKotlin(system)
    }
  }

  /**
   * Export raw color shade scale(s) directly to DTCG standard JSON format.
   */
  fun exportShadesToJson(
    scales: Map<String, List<ColorShade>>,
    systemName: String = "PaletteOS Palette"
  ): String {
    val sb = StringBuilder()
    sb.appendLine("{")
    sb.appendLine("  \"\$name\": \"${escapeJson(systemName)}\",")
    sb.appendLine("  \"\$version\": \"1.0.0\",")
    sb.appendLine("  \"\$description\": \"Exported from PaletteOS Architectural Color Engine\",")
    sb.appendLine("  \"color\": {")

    val categoryEntries = scales.entries.toList()
    categoryEntries.forEachIndexed { catIndex, (catName, shades) ->
      sb.appendLine("    \"${catName.lowercase()}\": {")
      shades.forEachIndexed { shadeIndex, shade ->
        val comma = if (shadeIndex == shades.size - 1) "" else ","
        val roleDesc = if (shade.role.isNotBlank()) shade.role else "Shade ${shade.shade}"
        sb.appendLine("      \"${shade.shade}\": {")
        sb.appendLine("        \"\$value\": \"${shade.hex}\",")
        sb.appendLine("        \"\$type\": \"color\",")
        sb.appendLine("        \"\$description\": \"${escapeJson(roleDesc)}\"")
        sb.appendLine("      }$comma")
      }
      val catComma = if (catIndex == categoryEntries.size - 1) "" else ","
      sb.appendLine("    }$catComma")
    }

    sb.appendLine("  }")
    sb.appendLine("}")
    return sb.toString()
  }

  /**
   * Converts a single ColorShade list (50-950) into DTCG JSON format.
   */
  fun exportSingleScaleToJson(categoryName: String, shades: List<ColorShade>): String {
    return exportShadesToJson(mapOf(categoryName to shades), "$categoryName Scale")
  }

  /**
   * Exports full DesignSystem to standard Design Tokens Community Group (DTCG) JSON.
   */
  fun exportToJsonDtcg(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("{")
    sb.appendLine("  \"\$name\": \"${escapeJson(system.name)}\",")
    sb.appendLine("  \"\$version\": \"${system.version}.0.0\",")
    sb.appendLine("  \"\$description\": \"${escapeJson(system.description)}\",")
    sb.appendLine("  \"color\": {")

    val scales = listOf(
      "primary" to system.primaryScale,
      "secondary" to system.secondaryScale,
      "neutral" to system.neutralScale,
      "success" to system.successScale,
      "warning" to system.warningScale,
      "error" to system.errorScale
    )

    scales.forEachIndexed { scaleIdx, (name, shades) ->
      sb.appendLine("    \"$name\": {")
      shades.forEachIndexed { shadeIdx, shade ->
        val comma = if (shadeIdx == shades.size - 1) "" else ","
        sb.appendLine("      \"${shade.shade}\": {")
        sb.appendLine("        \"\$value\": \"${shade.hex}\",")
        sb.appendLine("        \"\$type\": \"color\",")
        sb.appendLine("        \"\$description\": \"${escapeJson(shade.role)}\"")
        sb.appendLine("      }$comma")
      }
      val scaleComma = if (scaleIdx == scales.size - 1) "" else ","
      sb.appendLine("    }$scaleComma")
    }
    sb.appendLine("  },")

    sb.appendLine("  \"typography\": {")
    sb.appendLine("    \"fontFamily\": { \"\$value\": \"${escapeJson(system.typography.fontFamily)}\", \"\$type\": \"fontFamily\" },")
    sb.appendLine("    \"h1\": { \"\$value\": \"${system.typography.h1Sp}px\", \"\$type\": \"dimension\" },")
    sb.appendLine("    \"h2\": { \"\$value\": \"${system.typography.h2Sp}px\", \"\$type\": \"dimension\" },")
    sb.appendLine("    \"body\": { \"\$value\": \"${system.typography.bodySp}px\", \"\$type\": \"dimension\" },")
    sb.appendLine("    \"caption\": { \"\$value\": \"${system.typography.captionSp}px\", \"\$type\": \"dimension\" }")
    sb.appendLine("  }")
    sb.appendLine("}")
    return sb.toString()
  }

  /**
   * Exports to Tokens Studio for Figma (Figma Tokens) compatible format.
   */
  fun exportToFigmaTokens(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("{")
    sb.appendLine("  \"global\": {")
    sb.appendLine("    \"color\": {")

    val scales = listOf(
      "primary" to system.primaryScale,
      "secondary" to system.secondaryScale,
      "neutral" to system.neutralScale,
      "success" to system.successScale,
      "warning" to system.warningScale,
      "error" to system.errorScale
    )

    scales.forEachIndexed { scaleIdx, (name, shades) ->
      sb.appendLine("      \"$name\": {")
      shades.forEachIndexed { shadeIdx, shade ->
        val comma = if (shadeIdx == shades.size - 1) "" else ","
        sb.appendLine("        \"${shade.shade}\": {")
        sb.appendLine("          \"value\": \"${shade.hex}\",")
        sb.appendLine("          \"type\": \"color\",")
        sb.appendLine("          \"description\": \"${escapeJson(shade.role)}\"")
        sb.appendLine("        }$comma")
      }
      val scaleComma = if (scaleIdx == scales.size - 1) "" else ","
      sb.appendLine("      }$scaleComma")
    }

    sb.appendLine("    }")
    sb.appendLine("  },")
    sb.appendLine("  \"\$themes\": [],")
    sb.appendLine("  \"\$metadata\": {")
    sb.appendLine("    \"tokenSetOrder\": [")
    sb.appendLine("      \"global\"")
    sb.appendLine("    ]")
    sb.appendLine("  }")
    sb.appendLine("}")
    return sb.toString()
  }

  /**
   * Exports to Figma Variables REST / Plugin API Collection format.
   */
  fun exportToFigmaVariables(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("{")
    sb.appendLine("  \"variableCollections\": [")
    sb.appendLine("    {")
    sb.appendLine("      \"name\": \"PaletteOS / Primitives\",")
    sb.appendLine("      \"modes\": [")
    sb.appendLine("        { \"modeId\": \"mode:1\", \"name\": \"Default\" }")
    sb.appendLine("      ],")
    sb.appendLine("      \"variables\": [")

    val scales = listOf(
      "primary" to system.primaryScale,
      "secondary" to system.secondaryScale,
      "neutral" to system.neutralScale,
      "success" to system.successScale,
      "warning" to system.warningScale,
      "error" to system.errorScale
    )

    val allShades = mutableListOf<Pair<String, ColorShade>>()
    scales.forEach { (cat, shades) ->
      shades.forEach { shade ->
        allShades.add(cat to shade)
      }
    }

    allShades.forEachIndexed { index, (cat, shade) ->
      val color = ColorUtils.parseHex(shade.hex)
      val r = String.format("%.3f", color.red)
      val g = String.format("%.3f", color.green)
      val b = String.format("%.3f", color.blue)
      val comma = if (index == allShades.size - 1) "" else ","

      sb.appendLine("        {")
      sb.appendLine("          \"name\": \"$cat/${shade.shade}\",")
      sb.appendLine("          \"type\": \"COLOR\",")
      sb.appendLine("          \"description\": \"${escapeJson(shade.role)}\",")
      sb.appendLine("          \"valuesByMode\": {")
      sb.appendLine("            \"mode:1\": { \"r\": $r, \"g\": $g, \"b\": $b, \"a\": 1.0 }")
      sb.appendLine("          }")
      sb.appendLine("        }$comma")
    }

    sb.appendLine("      ]")
    sb.appendLine("    }")
    sb.appendLine("  ]")
    sb.appendLine("}")
    return sb.toString()
  }

  /**
   * Converts shade scales to standard CSS Custom Properties (:root).
   */
  fun exportToCssVariables(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("/**")
    sb.appendLine(" * PaletteOS Design System — ${system.name}")
    sb.appendLine(" * 50–950 Shade Scales and Semantic Bindings")
    sb.appendLine(" */")
    sb.appendLine(":root {")

    fun appendScale(name: String, shades: List<ColorShade>) {
      sb.appendLine("  /* $name Scale (50–950) */")
      shades.forEach {
        sb.appendLine("  --color-$name-${it.shade}: ${it.hex};")
      }
      sb.appendLine()
    }

    appendScale("primary", system.primaryScale)
    appendScale("secondary", system.secondaryScale)
    appendScale("neutral", system.neutralScale)
    appendScale("success", system.successScale)
    appendScale("warning", system.warningScale)
    appendScale("error", system.errorScale)

    sb.appendLine("  /* Typography */")
    sb.appendLine("  --font-family-sans: '${system.typography.fontFamily}', -apple-system, sans-serif;")
    sb.appendLine("  --font-size-h1: ${system.typography.h1Sp}px;")
    sb.appendLine("  --font-size-h2: ${system.typography.h2Sp}px;")
    sb.appendLine("  --font-size-h3: ${system.typography.h3Sp}px;")
    sb.appendLine("  --font-size-body: ${system.typography.bodySp}px;")
    sb.appendLine("  --font-size-caption: ${system.typography.captionSp}px;")
    sb.appendLine()

    sb.appendLine("  /* Semantic Color Aliases */")
    sb.appendLine("  --surface-canvas: var(--color-neutral-50);")
    sb.appendLine("  --surface-card: var(--color-neutral-100);")
    sb.appendLine("  --surface-border: var(--color-neutral-200);")
    sb.appendLine("  --text-primary: var(--color-neutral-900);")
    sb.appendLine("  --text-secondary: var(--color-neutral-600);")
    sb.appendLine("  --brand-primary: var(--color-primary-500);")
    sb.appendLine("  --brand-hover: var(--color-primary-600);")
    sb.appendLine("  --brand-active: var(--color-primary-700);")
    sb.appendLine("}")
    sb.appendLine()

    sb.appendLine("[data-theme=\"dark\"] {")
    sb.appendLine("  --surface-canvas: var(--color-neutral-950);")
    sb.appendLine("  --surface-card: var(--color-neutral-900);")
    sb.appendLine("  --surface-border: var(--color-neutral-800);")
    sb.appendLine("  --text-primary: var(--color-neutral-50);")
    sb.appendLine("  --text-secondary: var(--color-neutral-400);")
    sb.appendLine("  --brand-primary: var(--color-primary-400);")
    sb.appendLine("}")

    return sb.toString()
  }

  /**
   * Converts shade scales to a Tailwind CSS theme extension.
   */
  fun exportToTailwind(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("/** @type {import('tailwindcss').Config} */")
    sb.appendLine("// PaletteOS — ${system.name}")
    sb.appendLine("module.exports = {")
    sb.appendLine("  theme: {")
    sb.appendLine("    extend: {")
    sb.appendLine("      colors: {")

    fun appendTailwindColor(name: String, shades: List<ColorShade>) {
      sb.appendLine("        '$name': {")
      shades.forEach {
        sb.appendLine("          '${it.shade}': '${it.hex}',")
      }
      sb.appendLine("        },")
    }

    appendTailwindColor("primary", system.primaryScale)
    appendTailwindColor("secondary", system.secondaryScale)
    appendTailwindColor("neutral", system.neutralScale)
    appendTailwindColor("success", system.successScale)
    appendTailwindColor("warning", system.warningScale)
    appendTailwindColor("error", system.errorScale)

    sb.appendLine("      },")
    sb.appendLine("      fontFamily: {")
    sb.appendLine("        sans: ['${system.typography.fontFamily}', 'sans-serif'],")
    sb.appendLine("      }")
    sb.appendLine("    }")
    sb.appendLine("  }")
    sb.appendLine("};")
    return sb.toString()
  }

  /**
   * Converts shade scales into Jetpack Compose Color definitions.
   */
  fun exportToComposeKotlin(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("package com.example.ui.theme")
    sb.appendLine()
    sb.appendLine("import androidx.compose.ui.graphics.Color")
    sb.appendLine()
    sb.appendLine("// PaletteOS Generated Colors — ${system.name}")

    fun appendComposeGroup(group: String, shades: List<ColorShade>) {
      sb.appendLine("// $group 50–950")
      shades.forEach {
        val cleanHex = it.hex.removePrefix("#")
        val colorName = "${group.replaceFirstChar { c -> c.uppercase() }}${it.shade}"
        sb.appendLine("val $colorName = Color(0xFF$cleanHex)")
      }
      sb.appendLine()
    }

    appendComposeGroup("primary", system.primaryScale)
    appendComposeGroup("secondary", system.secondaryScale)
    appendComposeGroup("neutral", system.neutralScale)

    return sb.toString()
  }

  /**
   * Helper to write exported text content to a cache file for sharing or download.
   */
  fun writeToCacheFile(context: Context, fileName: String, content: String): File {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(exportDir, fileName)
    FileWriter(file).use { it.write(content) }
    return file
  }

  /**
   * Creates an Android share Intent to share or download the exported file.
   */
  fun createShareIntent(context: Context, file: File, mimeType: String): Intent {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    return Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }

  /**
   * Helper to copy exported tokens to clipboard.
   */
  fun copyToClipboard(context: Context, content: String, label: String = "Design System Tokens") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, content)
    clipboard.setPrimaryClip(clip)
  }

  private fun escapeJson(value: String): String {
    return value.replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t")
  }
}
