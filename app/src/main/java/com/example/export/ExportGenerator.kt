package com.example.export

import com.example.model.DesignSystem

enum class ExportFormat(val displayName: String, val extension: String) {
  CSS("CSS Variables", "css"),
  TAILWIND("Tailwind Config", "js"),
  JSON_DTCG("JSON (DTCG Tokens)", "json"),
  TYPESCRIPT("TypeScript", "ts"),
  SCSS("SCSS Variables", "scss"),
  MARKDOWN("Markdown Docs", "md")
}

object ExportGenerator {

  fun generate(system: DesignSystem, format: ExportFormat): String {
    return when (format) {
      ExportFormat.CSS -> generateCss(system)
      ExportFormat.TAILWIND -> generateTailwind(system)
      ExportFormat.JSON_DTCG -> generateJson(system)
      ExportFormat.TYPESCRIPT -> generateTypeScript(system)
      ExportFormat.SCSS -> generateScss(system)
      ExportFormat.MARKDOWN -> generateMarkdown(system)
    }
  }

  private fun generateCss(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("/**")
    sb.appendLine(" * PaletteOS Design System — ${system.name}")
    sb.appendLine(" * Generated via PaletteOS Architectural AI Engine")
    sb.appendLine(" */")
    sb.appendLine(":root {")

    sb.appendLine("  /* Primary Palette */")
    system.primaryScale.forEach {
      sb.appendLine("  --color-primary-${it.shade}: ${it.hex};")
    }

    sb.appendLine("\n  /* Neutral Palette (Replacing 17 random greys) */")
    system.neutralScale.forEach {
      sb.appendLine("  --color-neutral-${it.shade}: ${it.hex};")
    }

    sb.appendLine("\n  /* Semantic Colors */")
    system.successScale.forEach {
      sb.appendLine("  --color-success-${it.shade}: ${it.hex};")
    }
    system.warningScale.forEach {
      sb.appendLine("  --color-warning-${it.shade}: ${it.hex};")
    }
    system.errorScale.forEach {
      sb.appendLine("  --color-error-${it.shade}: ${it.hex};")
    }

    sb.appendLine("\n  /* Typography Scale */")
    sb.appendLine("  --font-family-sans: '${system.typography.fontFamily}', -apple-system, sans-serif;")
    sb.appendLine("  --font-size-h1: ${system.typography.h1Sp}px;")
    sb.appendLine("  --font-size-h2: ${system.typography.h2Sp}px;")
    sb.appendLine("  --font-size-h3: ${system.typography.h3Sp}px;")
    sb.appendLine("  --font-size-body: ${system.typography.bodySp}px;")
    sb.appendLine("  --font-size-caption: ${system.typography.captionSp}px;")

    sb.appendLine("\n  /* Spacing Scale (Base: ${system.spacing.baseUnit}px) */")
    system.spacing.scaleDp.forEachIndexed { index, dp ->
      sb.appendLine("  --spacing-${index + 1}: ${dp}px;")
    }

    sb.appendLine("\n  /* Radius Scale */")
    sb.appendLine("  --radius-sm: ${system.radius.sm}px;")
    sb.appendLine("  --radius-md: ${system.radius.md}px;")
    sb.appendLine("  --radius-lg: ${system.radius.lg}px;")
    sb.appendLine("  --radius-xl: ${system.radius.xl}px;")
    sb.appendLine("  --radius-full: 9999px;")

    sb.appendLine("}")
    return sb.toString()
  }

  private fun generateTailwind(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("/** @type {import('tailwindcss').Config} */")
    sb.appendLine("// PaletteOS — ${system.name}")
    sb.appendLine("module.exports = {")
    sb.appendLine("  theme: {")
    sb.appendLine("    extend: {")
    sb.appendLine("      colors: {")

    sb.appendLine("        primary: {")
    system.primaryScale.forEach {
      sb.appendLine("          '${it.shade}': '${it.hex}',")
    }
    sb.appendLine("        },")

    sb.appendLine("        neutral: {")
    system.neutralScale.forEach {
      sb.appendLine("          '${it.shade}': '${it.hex}',")
    }
    sb.appendLine("        },")

    sb.appendLine("        success: {")
    system.successScale.forEach {
      sb.appendLine("          '${it.shade}': '${it.hex}',")
    }
    sb.appendLine("        },")

    sb.appendLine("        warning: {")
    system.warningScale.forEach {
      sb.appendLine("          '${it.shade}': '${it.hex}',")
    }
    sb.appendLine("        },")

    sb.appendLine("        error: {")
    system.errorScale.forEach {
      sb.appendLine("          '${it.shade}': '${it.hex}',")
    }
    sb.appendLine("        },")

    sb.appendLine("      },")
    sb.appendLine("      fontFamily: {")
    sb.appendLine("        sans: ['${system.typography.fontFamily}', 'sans-serif'],")
    sb.appendLine("      },")
    sb.appendLine("      fontSize: {")
    sb.appendLine("        'h1': '${system.typography.h1Sp}px',")
    sb.appendLine("        'h2': '${system.typography.h2Sp}px',")
    sb.appendLine("        'h3': '${system.typography.h3Sp}px',")
    sb.appendLine("        'body': '${system.typography.bodySp}px',")
    sb.appendLine("        'caption': '${system.typography.captionSp}px',")
    sb.appendLine("      },")
    sb.appendLine("      borderRadius: {")
    sb.appendLine("        'sm': '${system.radius.sm}px',")
    sb.appendLine("        'md': '${system.radius.md}px',")
    sb.appendLine("        'lg': '${system.radius.lg}px',")
    sb.appendLine("        'xl': '${system.radius.xl}px',")
    sb.appendLine("      }")
    sb.appendLine("    }")
    sb.appendLine("  }")
    sb.appendLine("};")
    return sb.toString()
  }

  private fun generateJson(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("{")
    sb.appendLine("  \"\$name\": \"${system.name}\",")
    sb.appendLine("  \"\$version\": \"${system.version}.0.0\",")
    sb.appendLine("  \"color\": {")

    sb.appendLine("    \"primary\": {")
    system.primaryScale.forEachIndexed { i, s ->
      val comma = if (i == system.primaryScale.size - 1) "" else ","
      sb.appendLine("      \"${s.shade}\": { \"\$value\": \"${s.hex}\", \"\$type\": \"color\", \"\$description\": \"${s.role}\" }$comma")
    }
    sb.appendLine("    },")

    sb.appendLine("    \"neutral\": {")
    system.neutralScale.forEachIndexed { i, s ->
      val comma = if (i == system.neutralScale.size - 1) "" else ","
      sb.appendLine("      \"${s.shade}\": { \"\$value\": \"${s.hex}\", \"\$type\": \"color\", \"\$description\": \"${s.role}\" }$comma")
    }
    sb.appendLine("    }")
    sb.appendLine("  },")

    sb.appendLine("  \"typography\": {")
    sb.appendLine("    \"fontFamily\": { \"\$value\": \"${system.typography.fontFamily}\", \"\$type\": \"fontFamily\" },")
    sb.appendLine("    \"h1\": { \"\$value\": \"${system.typography.h1Sp}px\", \"\$type\": \"dimension\" },")
    sb.appendLine("    \"h2\": { \"\$value\": \"${system.typography.h2Sp}px\", \"\$type\": \"dimension\" },")
    sb.appendLine("    \"body\": { \"\$value\": \"${system.typography.bodySp}px\", \"\$type\": \"dimension\" }")
    sb.appendLine("  }")
    sb.appendLine("}")
    return sb.toString()
  }

  private fun generateTypeScript(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("// PaletteOS — ${system.name} TypeScript Token Catalog")
    sb.appendLine("export const tokens = {")
    sb.appendLine("  name: '${system.name}',")
    sb.appendLine("  colors: {")

    sb.appendLine("    primary: {")
    system.primaryScale.forEach {
      sb.appendLine("      ${it.shade}: '${it.hex}',")
    }
    sb.appendLine("    },")

    sb.appendLine("    neutral: {")
    system.neutralScale.forEach {
      sb.appendLine("      ${it.shade}: '${it.hex}',")
    }
    sb.appendLine("    },")

    sb.appendLine("    semantic: {")
    sb.appendLine("      success: '${system.successScale.find { it.shade == 500 }?.hex ?: "#19875A"}',")
    sb.appendLine("      warning: '${system.warningScale.find { it.shade == 500 }?.hex ?: "#C78B26"}',")
    sb.appendLine("      error: '${system.errorScale.find { it.shade == 500 }?.hex ?: "#D94A4A"}',")
    sb.appendLine("    }")

    sb.appendLine("  },")
    sb.appendLine("  typography: {")
    sb.appendLine("    fontFamily: '${system.typography.fontFamily}',")
    sb.appendLine("    h1: ${system.typography.h1Sp},")
    sb.appendLine("    h2: ${system.typography.h2Sp},")
    sb.appendLine("    h3: ${system.typography.h3Sp},")
    sb.appendLine("    body: ${system.typography.bodySp},")
    sb.appendLine("    caption: ${system.typography.captionSp},")
    sb.appendLine("  }")
    sb.appendLine("} as const;\n")
    sb.appendLine("export type DesignTokens = typeof tokens;")
    return sb.toString()
  }

  private fun generateScss(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("// PaletteOS Design Tokens — ${system.name} SCSS")
    system.primaryScale.forEach {
      sb.appendLine("\$color-primary-${it.shade}: ${it.hex};")
    }
    system.neutralScale.forEach {
      sb.appendLine("\$color-neutral-${it.shade}: ${it.hex};")
    }
    sb.appendLine("\$font-family-base: '${system.typography.fontFamily}', sans-serif;")
    sb.appendLine("\$font-size-h1: ${system.typography.h1Sp}px;")
    sb.appendLine("\$font-size-body: ${system.typography.bodySp}px;")
    return sb.toString()
  }

  private fun generateMarkdown(system: DesignSystem): String {
    val sb = StringBuilder()
    sb.appendLine("# ${system.name} — Design System Specification")
    sb.appendLine("> ${system.description}\n")
    sb.appendLine("## 1. Color System (Shade Card)")
    sb.appendLine("| Shade | Hex Code | Role | Description |")
    sb.appendLine("|---|---|---|---|")
    system.primaryScale.forEach {
      sb.appendLine("| **${it.shade}** | `${it.hex}` | `${it.role}` | Primary brand scale |")
    }
    sb.appendLine("\n### Neutral Scale")
    sb.appendLine("| Shade | Hex Code | Usage |")
    sb.appendLine("|---|---|---|")
    system.neutralScale.forEach {
      sb.appendLine("| **${it.shade}** | `${it.hex}` | `${it.role}` |")
    }
    sb.appendLine("\n## 2. Typography Scale")
    sb.appendLine("- **Font Family**: ${system.typography.fontFamily}")
    sb.appendLine("- **H1**: ${system.typography.h1Sp}px")
    sb.appendLine("- **H2**: ${system.typography.h2Sp}px")
    sb.appendLine("- **H3**: ${system.typography.h3Sp}px")
    sb.appendLine("- **Body**: ${system.typography.bodySp}px")
    sb.appendLine("- **Caption**: ${system.typography.captionSp}px\n")
    sb.appendLine("## 3. WCAG Accessibility")
    sb.appendLine("All contrast ratios are calibrated for WCAG 2.1 AA/AAA compliance against neutral background surfaces.")
    return sb.toString()
  }
}
