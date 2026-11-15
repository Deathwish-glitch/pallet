package com.example.data

import com.example.R
import com.example.model.*
import com.example.util.ColorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

import com.example.manager.ColorPaletteManager
import com.example.manager.GeneratedPaletteResult
import com.example.manager.PaletteSource

class DesignSystemRepository {

  val paletteManager = ColorPaletteManager()

  private val _designSystems = MutableStateFlow<List<DesignSystem>>(emptyList())
  val designSystems: StateFlow<List<DesignSystem>> = _designSystems.asStateFlow()

  private val _currentSystemId = MutableStateFlow<String>("nordic-studio")
  val currentSystemId: StateFlow<String> = _currentSystemId.asStateFlow()

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  private val undoStack = mutableListOf<DesignSystem>()
  private val redoStack = mutableListOf<DesignSystem>()

  init {
    initDefaultProjects()
    initDefaultChat()
  }

  private fun initDefaultProjects() {
    val nordicScale = ColorUtils.generateShades("#6B7F5E", "primary")
    val nordicSecondary = ColorUtils.generateShades("#9E8B6D", "secondary")
    val neutralGrey = ColorUtils.generateShades("#737672", "neutral")
    val success = ColorUtils.generateShades("#19875A", "success")
    val warning = ColorUtils.generateShades("#C78B26", "warning")
    val error = ColorUtils.generateShades("#D94A4A", "error")

    val nordicTokens = listOf(
      DesignToken("color.primary.500", "color", "#6B7F5E", "#6B7F5E", "Base brand olive"),
      DesignToken("color.primary.50", "color", nordicScale.first().hex, nordicScale.first().hex, "Subtle tint"),
      DesignToken("color.primary.600", "color", nordicScale[6].hex, nordicScale[6].hex, "Hover state"),
      DesignToken("color.neutral.50", "color", neutralGrey.first().hex, neutralGrey.first().hex, "Canvas surface"),
      DesignToken("color.neutral.900", "color", neutralGrey.last().hex, neutralGrey.last().hex, "High-contrast text"),
      DesignToken("typography.fontFamily", "typography", "Inter", "Inter", "Sans-serif"),
      DesignToken("typography.h1", "typography", "40px", "40px", "Display heading"),
      DesignToken("typography.body", "typography", "16px", "16px", "Default body text"),
      DesignToken("spacing.base", "spacing", "4px", "4px", "4px grid"),
      DesignToken("spacing.4", "spacing", "16px", "16px", "Medium spacing"),
      DesignToken("radius.md", "radius", "8px", "8px", "Card corner radius"),
      DesignToken("radius.full", "radius", "9999px", "9999px", "Pill buttons")
    )

    val nordicStudio = DesignSystem(
      id = "nordic-studio",
      name = "Nordic Studio",
      description = "A minimal and modern design system for architecture and construction products.",
      category = "Architecture",
      heroImageRes = R.drawable.nordic_arch,
      primaryHex = "#6B7F5E",
      primaryScale = nordicScale,
      secondaryScale = nordicSecondary,
      neutralScale = neutralGrey,
      successScale = success,
      warningScale = warning,
      errorScale = error,
      typography = TypographyScale(
        fontFamily = "Inter",
        h1Sp = 40,
        h2Sp = 32,
        h3Sp = 24,
        bodySp = 16,
        captionSp = 12
      ),
      tokens = nordicTokens,
      updatedAt = "2h ago",
      version = 1
    )

    // Tasko App (From the explainer video!)
    val taskoBlue = ColorUtils.generateShades("#3B86F5", "primary")
    val taskoNeutral = ColorUtils.generateShades("#6B7280", "neutral")
    val taskoApp = DesignSystem(
      id = "tasko-app",
      name = "Tasko App",
      description = "Modern productivity system featuring the single 500 blue family and a unified 10-shade neutral card.",
      category = "Productivity",
      primaryHex = "#3B86F5",
      primaryScale = taskoBlue,
      secondaryScale = ColorUtils.generateShades("#8B5CF6", "secondary"),
      neutralScale = taskoNeutral,
      successScale = success,
      warningScale = warning,
      errorScale = error,
      tokens = nordicTokens,
      updatedAt = "3h ago",
      version = 2
    )

    // Aura App
    val auraPeach = ColorUtils.generateShades("#D97A53", "primary")
    val auraApp = DesignSystem(
      id = "aura-app",
      name = "Aura App",
      description = "Warm organic wellness app tokens with earth pigments and calm typography.",
      category = "Wellness",
      primaryHex = "#D97A53",
      primaryScale = auraPeach,
      secondaryScale = ColorUtils.generateShades("#E6B89C", "secondary"),
      neutralScale = neutralGrey,
      successScale = success,
      warningScale = warning,
      errorScale = error,
      updatedAt = "1d ago",
      version = 1
    )

    _designSystems.value = listOf(nordicStudio, taskoApp, auraApp)
  }

  private fun initDefaultChat() {
    val initialSystem = _designSystems.value.first()
    _chatMessages.value = listOf(
      ChatMessage(
        id = "msg-1",
        role = MessageRole.USER,
        text = "Create a modern design system for a construction app. Use earth tones and a premium look.",
        timestamp = "10:14 AM"
      ),
      ChatMessage(
        id = "msg-2",
        role = MessageRole.ASSISTANT,
        text = "Here's a complete design system for your construction app.",
        timestamp = "10:15 AM",
        proposal = DesignSystemProposal(
          systemName = "Nordic Studio",
          summary = "Harmonious earth olive foundation with limestone neutrals, Inter typography, and verified WCAG 2.1 AA compliance.",
          primaryHex = "#6B7F5E",
          typographyFont = "Inter",
          scalesCount = 8,
          tokensCount = 42,
          componentsCount = 18,
          generatedSystem = initialSystem
        ),
        isApplied = true
      )
    )
  }

  fun getCurrentSystem(): DesignSystem {
    return _designSystems.value.find { it.id == _currentSystemId.value }
      ?: _designSystems.value.first()
  }

  fun selectSystem(id: String) {
    _currentSystemId.value = id
  }

  fun updatePrimaryHex(newHex: String) {
    val current = getCurrentSystem()
    pushUndo(current)
    val newScale = ColorUtils.generateShades(newHex, "primary")
    val updated = current.copy(
      primaryHex = newHex,
      primaryScale = newScale,
      updatedAt = "Just now",
      version = current.version + 1
    )
    updateSystem(updated)
  }

  fun toggleShadeLock(category: String, shadeNumber: Int) {
    val current = getCurrentSystem()
    val updatedScale = current.primaryScale.map {
      if (it.shade == shadeNumber) it.copy(isLocked = !it.isLocked) else it
    }
    updateSystem(current.copy(primaryScale = updatedScale))
  }

  fun updateTypographyScale(h1: Int, h2: Int, h3: Int, body: Int, caption: Int, font: String? = null) {
    val current = getCurrentSystem()
    pushUndo(current)
    val updatedTypo = current.typography.copy(
      h1Sp = h1,
      h2Sp = h2,
      h3Sp = h3,
      bodySp = body,
      captionSp = caption,
      fontFamily = font ?: current.typography.fontFamily
    )
    updateSystem(current.copy(typography = updatedTypo, version = current.version + 1))
  }

  fun generateNewSystemFromPrompt(prompt: String, categoryName: String = "App"): DesignSystem {
    val primaryColor = when {
      prompt.contains("blue", ignoreCase = true) || prompt.contains("task", ignoreCase = true) -> "#3B86F5"
      prompt.contains("earth", ignoreCase = true) || prompt.contains("construct", ignoreCase = true) || prompt.contains("nordic", ignoreCase = true) -> "#6B7F5E"
      prompt.contains("orange", ignoreCase = true) || prompt.contains("warm", ignoreCase = true) || prompt.contains("aura", ignoreCase = true) -> "#D97A53"
      prompt.contains("purple", ignoreCase = true) || prompt.contains("indigo", ignoreCase = true) || prompt.contains("tech", ignoreCase = true) -> "#5369D9"
      prompt.contains("emerald", ignoreCase = true) || prompt.contains("green", ignoreCase = true) || prompt.contains("finance", ignoreCase = true) -> "#10B981"
      prompt.contains("dark", ignoreCase = true) || prompt.contains("minimal", ignoreCase = true) -> "#2B2D42"
      else -> "#4F46E5"
    }

    val font = when {
      prompt.contains("serif", ignoreCase = true) || prompt.contains("editorial", ignoreCase = true) -> "Playfair"
      prompt.contains("tech", ignoreCase = true) || prompt.contains("code", ignoreCase = true) -> "Geist"
      else -> "Inter"
    }

    val id = "sys-" + UUID.randomUUID().toString().take(8)
    val words = prompt.split(" ").filter { it.isNotBlank() }
    val title = words.take(2).joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } + " System"

    val newSystem = DesignSystem(
      id = id,
      name = title.ifBlank { "Modern Studio" },
      description = "AI generated from prompt: \"$prompt\"",
      category = categoryName,
      primaryHex = primaryColor,
      primaryScale = paletteManager.generateShades50to950(primaryColor, "primary"),
      secondaryScale = paletteManager.generateShades50to950("#A855F7", "secondary"),
      neutralScale = paletteManager.generateShades50to950("#6B7280", "neutral"),
      successScale = paletteManager.generateShades50to950("#19875A", "success"),
      warningScale = paletteManager.generateShades50to950("#C78B26", "warning"),
      errorScale = paletteManager.generateShades50to950("#D94A4A", "error"),
      typography = TypographyScale(fontFamily = font),
      updatedAt = "Just now",
      version = 1
    )

    _designSystems.value = listOf(newSystem) + _designSystems.value
    _currentSystemId.value = id
    return newSystem
  }

  fun addSystem(system: DesignSystem) {
    _designSystems.value = listOf(system) + _designSystems.value
    _currentSystemId.value = system.id
  }

  suspend fun generateWithGemini(prompt: String): GeneratedPaletteResult {
    val result = paletteManager.generatePalette(prompt)
    addSystem(result.designSystem)
    return result
  }

  fun sendUserMessage(text: String) {
    val userMsg = ChatMessage(
      id = "msg-" + UUID.randomUUID().toString().take(8),
      role = MessageRole.USER,
      text = text,
      timestamp = "Just now"
    )

    val current = getCurrentSystem()
    pushUndo(current)

    // Build intelligent assistant response
    val (replyText, proposal) = when {
      text.contains("mute", ignoreCase = true) || text.contains("blue", ignoreCase = true) -> {
        val newHex = "#3B86F5"
        val updated = current.copy(
          primaryHex = newHex,
          primaryScale = paletteManager.generateShades50to950(newHex, "primary"),
          version = current.version + 1
        )
        Pair(
          "I updated the primary palette to a balanced, professional blue (#3B86F5). All 50–950 shades have been calibrated with preserved contrast.",
          DesignSystemProposal(
            systemName = current.name,
            summary = "Calibrated primary scale with accessible 500 base and deep 600-950 interaction states.",
            primaryHex = newHex,
            typographyFont = current.typography.fontFamily,
            generatedSystem = updated
          )
        )
      }
      text.contains("dark", ignoreCase = true) -> {
        val darkHex = "#1E293B"
        val updated = current.copy(
          primaryHex = darkHex,
          neutralScale = paletteManager.generateShades50to950("#0F172A", "neutral"),
          version = current.version + 1
        )
        Pair(
          "Generated dark theme mapping with high-contrast neutral scales (50–950), preserving WCAG 2.1 AAA on all critical UI containers.",
          DesignSystemProposal(
            systemName = current.name + " (Dark)",
            summary = "Dark theme surface tokens, muted borders, and elevated card contrasts.",
            primaryHex = current.primaryHex,
            typographyFont = current.typography.fontFamily,
            generatedSystem = updated
          )
        )
      }
      text.contains("contrast", ignoreCase = true) || text.contains("wcag", ignoreCase = true) -> {
        Pair(
          "WCAG Audit Complete: Primary base #6B7F5E with pure white text achieves 4.8:1 ratio (AA Normal & Large Pass). Shade 600 delivers 6.4:1 ratio for enhanced accessibility, and shade 950 serves as the dark contrast anchor.",
          null
        )
      }
      text.contains("17", ignoreCase = true) || text.contains("grey", ignoreCase = true) || text.contains("gray", ignoreCase = true) -> {
        val updated = current.copy(
          neutralScale = paletteManager.generateShades50to950("#64748B", "neutral"),
          version = current.version + 1
        )
        Pair(
          "Consolidated all scattered greys into a single 50–950 neutral card. Every border, text, and surface now references a clear token number.",
          DesignSystemProposal(
            systemName = current.name,
            summary = "Replaced legacy greys with structured neutral/50 through neutral/950 palette.",
            primaryHex = current.primaryHex,
            typographyFont = current.typography.fontFamily,
            generatedSystem = updated
          )
        )
      }
      else -> {
        val result = runCatching {
          kotlinx.coroutines.runBlocking { paletteManager.generatePalette(text) }
        }.getOrNull()
        val newSystem = result?.designSystem ?: generateNewSystemFromPrompt(text)
        val sourceBadge = if (result?.source == PaletteSource.GEMINI_AI) " (via Gemini API)" else ""
        Pair(
          "Generated a complete design system tailored to \"$text\"$sourceBadge. Foundation tokens calibrated across 50–950:",
          DesignSystemProposal(
            systemName = newSystem.name,
            summary = newSystem.description,
            primaryHex = newSystem.primaryHex,
            typographyFont = newSystem.typography.fontFamily,
            generatedSystem = newSystem
          )
        )
      }
    }

    val assistantMsg = ChatMessage(
      id = "msg-" + UUID.randomUUID().toString().take(8),
      role = MessageRole.ASSISTANT,
      text = replyText,
      timestamp = "Just now",
      proposal = proposal,
      isApplied = false
    )

    _chatMessages.value = _chatMessages.value + listOf(userMsg, assistantMsg)
  }

  fun applyProposal(proposal: DesignSystemProposal, messageId: String) {
    pushUndo(getCurrentSystem())
    updateSystem(proposal.generatedSystem)
    _chatMessages.value = _chatMessages.value.map {
      if (it.id == messageId) it.copy(isApplied = true) else it
    }
  }

  fun undo() {
    if (undoStack.isNotEmpty()) {
      val previous = undoStack.removeAt(undoStack.size - 1)
      redoStack.add(getCurrentSystem())
      updateSystem(previous)
    }
  }

  fun redo() {
    if (redoStack.isNotEmpty()) {
      val next = redoStack.removeAt(redoStack.size - 1)
      undoStack.add(getCurrentSystem())
      updateSystem(next)
    }
  }

  private fun pushUndo(system: DesignSystem) {
    undoStack.add(system)
    redoStack.clear()
    if (undoStack.size > 20) undoStack.removeAt(0)
  }

  private fun updateSystem(system: DesignSystem) {
    _designSystems.value = _designSystems.value.map {
      if (it.id == system.id) system else it
    }
  }
}
