package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorScale
import com.example.model.ColorShade
import com.example.model.DesignSystem
import com.example.model.TypographyScale
import com.example.ui.theme.*
import com.example.util.ColorUtils

private enum class PreviewScaleFilter(val label: String) {
  ALL("All Scales"),
  BRAND("Brand & Neutral"),
  SEMANTIC("Semantic")
}

/**
 * Live Design System Preview grid rendering the active DesignSystem's generated
 * 50-950 color scales and typography hierarchy in real time.
 */
@Composable
fun DesignSystemPreview(
  designSystem: DesignSystem,
  modifier: Modifier = Modifier,
  onSwatchClick: ((ColorShade) -> Unit)? = null,
  onEditColors: (() -> Unit)? = null,
  onEditTypography: (() -> Unit)? = null
) {
  var isDarkPreview by remember { mutableStateOf(false) }
  var selectedFilter by remember { mutableStateOf(PreviewScaleFilter.ALL) }
  var inspectedShade by remember(designSystem.id, designSystem.primaryHex) {
    mutableStateOf(
      designSystem.primaryScale.find { it.shade == 500 }
        ?: designSystem.primaryScale.firstOrNull()
    )
  }

  val neutral50 = ColorUtils.parseHex(
    designSystem.neutralScale.find { it.shade == 50 }?.hex ?: "#FAFAF9",
    PaletteBg
  )
  val neutral100 = ColorUtils.parseHex(
    designSystem.neutralScale.find { it.shade == 100 }?.hex ?: "#F4F4F1",
    PaletteSurfaceSecondary
  )
  val neutral200 = ColorUtils.parseHex(
    designSystem.neutralScale.find { it.shade == 200 }?.hex ?: "#E9E9E5",
    PaletteMutedBorder
  )
  val neutral700 = ColorUtils.parseHex(
    designSystem.neutralScale.find { it.shade == 700 }?.hex ?: "#4A4B47",
    PaletteTextSecondary
  )
  val neutral800 = ColorUtils.parseHex(
    designSystem.neutralScale.find { it.shade == 800 }?.hex ?: "#2E2E2A",
    PaletteMutedBorderDark
  )
  val neutral900 = ColorUtils.parseHex(
    designSystem.neutralScale.find { it.shade == 900 }?.hex ?: "#20211F",
    PaletteTextPrimary
  )
  val neutral950 = ColorUtils.parseHex(
    designSystem.neutralScale.find { it.shade == 950 }?.hex ?: "#141413",
    PaletteBgDark
  )

  val previewBg by animateColorAsState(
    targetValue = if (isDarkPreview) neutral950 else neutral50,
    animationSpec = spring(),
    label = "previewBg"
  )
  val previewCardBg by animateColorAsState(
    targetValue = if (isDarkPreview) neutral900 else Color.White,
    animationSpec = spring(),
    label = "previewCardBg"
  )
  val previewSubtleBg by animateColorAsState(
    targetValue = if (isDarkPreview) neutral800 else neutral100,
    animationSpec = spring(),
    label = "previewSubtleBg"
  )
  val previewBorder by animateColorAsState(
    targetValue = if (isDarkPreview) neutral800 else neutral200,
    animationSpec = spring(),
    label = "previewBorder"
  )
  val previewTextPrimary by animateColorAsState(
    targetValue = if (isDarkPreview) Color(0xFFF5F5F3) else neutral900,
    animationSpec = spring(),
    label = "previewTextPrimary"
  )
  val previewTextSecondary by animateColorAsState(
    targetValue = if (isDarkPreview) Color(0xFFB3B3AD) else neutral700,
    animationSpec = spring(),
    label = "previewTextSecondary"
  )

  val primaryAnchor = ColorUtils.parseHex(designSystem.primaryHex)
  val secondaryAnchor = ColorUtils.parseHex(
    designSystem.secondaryScale.find { it.shade == 500 }?.hex
      ?: designSystem.secondaryScale.firstOrNull()?.hex
      ?: designSystem.primaryHex
  )

  val allScales = remember(designSystem) {
    listOf(
      ColorScale("Primary", designSystem.primaryHex, designSystem.primaryScale),
      ColorScale(
        "Secondary",
        designSystem.secondaryScale.find { it.shade == 500 }?.hex ?: designSystem.primaryHex,
        designSystem.secondaryScale
      ),
      ColorScale(
        "Neutral",
        designSystem.neutralScale.find { it.shade == 500 }?.hex ?: "#737672",
        designSystem.neutralScale
      ),
      ColorScale(
        "Success",
        designSystem.successScale.find { it.shade == 500 }?.hex ?: "#19875A",
        designSystem.successScale
      ),
      ColorScale(
        "Warning",
        designSystem.warningScale.find { it.shade == 500 }?.hex ?: "#C78B26",
        designSystem.warningScale
      ),
      ColorScale(
        "Error",
        designSystem.errorScale.find { it.shade == 500 }?.hex ?: "#D94A4A",
        designSystem.errorScale
      )
    )
  }

  val visibleScales = remember(allScales, selectedFilter) {
    when (selectedFilter) {
      PreviewScaleFilter.ALL -> allScales
      PreviewScaleFilter.BRAND -> allScales.take(3)
      PreviewScaleFilter.SEMANTIC -> allScales.drop(3)
    }
  }

  Surface(
    shape = RoundedCornerShape(24.dp),
    color = previewBg,
    border = BorderStroke(1.dp, previewBorder),
    modifier = modifier
      .fillMaxWidth()
      .testTag("design_system_preview_root")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Live Preview Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(primaryAnchor),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = "Design System Preview",
              tint = accessibleOnColor(primaryAnchor),
              modifier = Modifier.size(18.dp)
            )
          }
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = designSystem.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = previewTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = previewSubtleBg
              ) {
                Text(
                  text = "v${designSystem.version}",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = previewTextSecondary,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "Live Token Canvas • ${designSystem.typography.fontFamily}",
              style = MaterialTheme.typography.bodySmall,
              color = previewTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Light / Dark Preview Toggle
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = previewCardBg,
          border = BorderStroke(1.dp, previewBorder),
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable { isDarkPreview = !isDarkPreview }
            .testTag("preview_theme_toggle")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = if (isDarkPreview) Icons.Default.DarkMode else Icons.Default.LightMode,
              contentDescription = if (isDarkPreview) "Switch preview to light mode" else "Switch preview to dark mode",
              tint = primaryAnchor,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = if (isDarkPreview) "Dark" else "Light",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = previewTextPrimary
            )
          }
        }
      }

      // Section 1: Color Scales Live Grid
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = previewCardBg,
        border = BorderStroke(1.dp, previewBorder),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("preview_color_grid_section")
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Color Scales Grid",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = previewTextPrimary
              )
              Text(
                text = "${visibleScales.size} families • 50–950 calibrated shades",
                style = MaterialTheme.typography.labelSmall,
                color = previewTextSecondary
              )
            }

            if (onEditColors != null) {
              TextButton(
                onClick = onEditColors,
                modifier = Modifier.testTag("preview_edit_colors_btn")
              ) {
                Icon(
                  imageVector = Icons.Outlined.Tune,
                  contentDescription = "Edit colors",
                  tint = primaryAnchor,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Edit",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = primaryAnchor
                )
              }
            }
          }

          // Filter Pills
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            PreviewScaleFilter.values().forEach { filter ->
              val selected = selectedFilter == filter
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selected) primaryAnchor else previewSubtleBg,
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { selectedFilter = filter }
                  .testTag("preview_filter_${filter.name.lowercase()}")
              ) {
                Text(
                  text = filter.label,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (selected) accessibleOnColor(primaryAnchor) else previewTextSecondary,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
              }
            }
          }

          // Responsive Grid of Color Scales
          BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 520.dp
            if (isWide) {
              // 2-column grid of scale cards on tablets/landscape
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visibleScales.chunked(2).forEach { pair ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    pair.forEach { scale ->
                      ColorScaleGridRow(
                        scale = scale,
                        selectedShade = inspectedShade,
                        surfaceBg = previewSubtleBg,
                        border = previewBorder,
                        textPrimary = previewTextPrimary,
                        textSecondary = previewTextSecondary,
                        onSelectShade = { shade ->
                          inspectedShade = shade
                          onSwatchClick?.invoke(shade)
                        },
                        modifier = Modifier.weight(1f)
                      )
                    }
                    if (pair.size == 1) {
                      Spacer(modifier = Modifier.weight(1f))
                    }
                  }
                }
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visibleScales.forEach { scale ->
                  ColorScaleGridRow(
                    scale = scale,
                    selectedShade = inspectedShade,
                    surfaceBg = previewSubtleBg,
                    border = previewBorder,
                    textPrimary = previewTextPrimary,
                    textSecondary = previewTextSecondary,
                    onSelectShade = { shade ->
                      inspectedShade = shade
                      onSwatchClick?.invoke(shade)
                    },
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }
            }
          }

          // Live Selected Swatch Inspector Bar
          AnimatedVisibility(visible = inspectedShade != null) {
            inspectedShade?.let { shade ->
              val shadeColor = ColorUtils.parseHex(shade.hex)
              val contrastWhite = ColorUtils.calculateContrastRatio(shadeColor, Color.White)
              val contrastDark = ColorUtils.calculateContrastRatio(shadeColor, neutral950)
              val bestContrast = maxOf(contrastWhite, contrastDark)
              val wcagGrade = when {
                ColorUtils.isAaaNormalPassed(bestContrast) -> "AAA"
                ColorUtils.isAaNormalPassed(bestContrast) -> "AA"
                ColorUtils.isAaLargePassed(bestContrast) -> "AA Large"
                else -> "Decorative"
              }

              Surface(
                shape = RoundedCornerShape(14.dp),
                color = previewSubtleBg,
                border = BorderStroke(1.dp, previewBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("preview_swatch_inspector")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(RoundedCornerShape(9.dp))
                      .background(shadeColor)
                      .border(1.dp, previewBorder, RoundedCornerShape(9.dp))
                  )
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${shade.name.ifBlank { "shade-${shade.shade}" }} • ${shade.hex}",
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                      ),
                      color = previewTextPrimary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = shade.role.ifBlank { "Shade ${shade.shade} token" },
                      style = MaterialTheme.typography.bodySmall,
                      color = previewTextSecondary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = previewCardBg,
                    border = BorderStroke(1.dp, previewBorder)
                  ) {
                    Text(
                      text = "$wcagGrade ${ColorUtils.formatRatio(bestContrast)}",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = previewTextPrimary,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Section 2: Typography Scale Live Grid
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = previewCardBg,
        border = BorderStroke(1.dp, previewBorder),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("preview_typography_grid_section")
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                tint = primaryAnchor,
                modifier = Modifier.size(18.dp)
              )
              Column {
                Text(
                  text = "Typography Scale Grid",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = previewTextPrimary
                )
                Text(
                  text = "Family: ${designSystem.typography.fontFamily} • 5 semantic steps",
                  style = MaterialTheme.typography.labelSmall,
                  color = previewTextSecondary
                )
              }
            }

            if (onEditTypography != null) {
              TextButton(
                onClick = onEditTypography,
                modifier = Modifier.testTag("preview_edit_typography_btn")
              ) {
                Icon(
                  imageVector = Icons.Outlined.Tune,
                  contentDescription = "Edit typography",
                  tint = primaryAnchor,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Edit",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = primaryAnchor
                )
              }
            }
          }

          TypographyScaleGrid(
            typography = designSystem.typography,
            accentColor = primaryAnchor,
            secondaryAccent = secondaryAnchor,
            surfaceBg = previewSubtleBg,
            border = previewBorder,
            textPrimary = previewTextPrimary,
            textSecondary = previewTextSecondary
          )
        }
      }

      // Section 3: Live Token Specimen Strip (Visualizes Colors + Typography Composed Together)
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = previewCardBg,
        border = BorderStroke(1.dp, previewBorder),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("preview_specimen_card")
      ) {
        val resolvedFont = resolveFontFamily(designSystem.typography.fontFamily)
        val radiusDp = designSystem.radius.md.coerceIn(4, 24).dp
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Composed UI Specimen",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = previewTextSecondary
            )
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = primaryAnchor.copy(alpha = 0.14f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = primaryAnchor,
                  modifier = Modifier.size(12.dp)
                )
                Text(
                  text = "Live Tokens",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = primaryAnchor
                )
              }
            }
          }

          Text(
            text = designSystem.name,
            fontFamily = resolvedFont,
            fontSize = designSystem.typography.h3Sp.coerceIn(18, 28).sp,
            fontWeight = FontWeight.Bold,
            color = previewTextPrimary
          )
          Text(
            text = designSystem.description.ifBlank { designSystem.typography.sampleText },
            fontFamily = resolvedFont,
            fontSize = designSystem.typography.bodySp.coerceIn(12, 18).sp,
            lineHeight = (designSystem.typography.bodySp.coerceIn(12, 18) * 1.45f).sp,
            color = previewTextSecondary
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { onEditColors?.invoke() },
              shape = RoundedCornerShape(radiusDp),
              colors = ButtonDefaults.buttonColors(
                containerColor = primaryAnchor,
                contentColor = accessibleOnColor(primaryAnchor)
              ),
              modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
            ) {
              Text(
                text = "Primary 500",
                fontFamily = resolvedFont,
                fontWeight = FontWeight.SemiBold
              )
            }

            OutlinedButton(
              onClick = { onEditTypography?.invoke() },
              shape = RoundedCornerShape(radiusDp),
              border = BorderStroke(1.5.dp, secondaryAnchor),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isDarkPreview) previewTextPrimary else secondaryAnchor
              ),
              modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
            ) {
              Text(
                text = "Secondary",
                fontFamily = resolvedFont,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ColorScaleGridRow(
  scale: ColorScale,
  selectedShade: ColorShade?,
  surfaceBg: Color,
  border: Color,
  textPrimary: Color,
  textSecondary: Color,
  onSelectShade: (ColorShade) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = surfaceBg,
    border = BorderStroke(1.dp, border),
    modifier = modifier.testTag("preview_scale_row_${scale.categoryName.lowercase()}")
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(ColorUtils.parseHex(scale.baseHex))
          )
          Text(
            text = scale.categoryName,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = textPrimary
          )
        }
        Text(
          text = scale.baseHex.uppercase(),
          style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
          color = textSecondary
        )
      }

      // 2-row compact grid of 50..950 swatches (6 + 5) so every swatch fits cleanly with zero horizontal clipping
      val rows = remember(scale.shades) {
        if (scale.shades.size > 6) listOf(scale.shades.take(6), scale.shades.drop(6))
        else listOf(scale.shades)
      }

      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { rowShades ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            rowShades.forEach { shade ->
              val shadeColor = ColorUtils.parseHex(shade.hex)
              val isSelected = selectedShade?.name == shade.name && selectedShade.hex == shade.hex
              val isBase500 = shade.shade == 500
              val onSwatch = accessibleOnColor(shadeColor)

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { onSelectShade(shade) }
                  .testTag("preview_swatch_${scale.categoryName.lowercase()}_${shade.shade}")
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shadeColor)
                    .border(
                      width = if (isSelected) 2.dp else 0.75.dp,
                      color = if (isSelected) textPrimary else border,
                      shape = RoundedCornerShape(8.dp)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isBase500 || isSelected) {
                    Box(
                      modifier = Modifier
                        .size(if (isSelected) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(onSwatch)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = "${shade.shade}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isBase500 || isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isBase500 || isSelected) textPrimary else textSecondary,
                  maxLines = 1
                )
              }
            }
            // Pad remaining slots on second row so swatch widths align evenly across the 6-column grid
            repeat((6 - rowShades.size).coerceAtLeast(0)) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }
}

private data class TypographyGridItem(
  val token: String,
  val sizeSp: Int,
  val weight: FontWeight,
  val sample: String,
  val isSecondaryColor: Boolean = false
)

@Composable
private fun TypographyScaleGrid(
  typography: TypographyScale,
  accentColor: Color,
  secondaryAccent: Color,
  surfaceBg: Color,
  border: Color,
  textPrimary: Color,
  textSecondary: Color
) {
  val fontFamily = remember(typography.fontFamily) { resolveFontFamily(typography.fontFamily) }
  val items = remember(typography) {
    listOf(
      TypographyGridItem("H1", typography.h1Sp, FontWeight.Bold, "Architectural Minimalism"),
      TypographyGridItem("H2", typography.h2Sp, FontWeight.SemiBold, "Balanced Design Tokens"),
      TypographyGridItem("H3", typography.h3Sp, FontWeight.Medium, "Modular Scale System"),
      TypographyGridItem("Body", typography.bodySp, FontWeight.Normal, typography.sampleText),
      TypographyGridItem(
        "Caption",
        typography.captionSp,
        FontWeight.Medium,
        "WCAG 2.1 AA • ${typography.fontFamily} Token Metadata",
        isSecondaryColor = true
      )
    )
  }

  BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
    val isWide = maxWidth >= 520.dp
    if (isWide) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { pair ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            pair.forEach { item ->
              TypographyGridCell(
                item = item,
                fontFamily = fontFamily,
                accentColor = accentColor,
                secondaryAccent = secondaryAccent,
                surfaceBg = surfaceBg,
                border = border,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                modifier = Modifier.weight(1f)
              )
            }
            if (pair.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
          TypographyGridCell(
            item = item,
            fontFamily = fontFamily,
            accentColor = accentColor,
            secondaryAccent = secondaryAccent,
            surfaceBg = surfaceBg,
            border = border,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

@Composable
private fun TypographyGridCell(
  item: TypographyGridItem,
  fontFamily: FontFamily,
  accentColor: Color,
  secondaryAccent: Color,
  surfaceBg: Color,
  border: Color,
  textPrimary: Color,
  textSecondary: Color,
  modifier: Modifier = Modifier
) {
  val lineHeightSp = (item.sizeSp * 1.25f).sp
  val weightLabel = when (item.weight) {
    FontWeight.Bold -> "700"
    FontWeight.SemiBold -> "600"
    FontWeight.Medium -> "500"
    else -> "400"
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = surfaceBg,
    border = BorderStroke(1.dp, border),
    modifier = modifier.testTag("preview_typo_cell_${item.token.lowercase()}")
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = (if (item.isSecondaryColor) secondaryAccent else accentColor).copy(alpha = 0.16f)
          ) {
            Text(
              text = item.token,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (item.isSecondaryColor) secondaryAccent else accentColor,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
          }
          Text(
            text = "W$weightLabel",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = textSecondary
          )
        }

        Text(
          text = "${item.sizeSp}sp",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          ),
          color = textPrimary
        )
      }

      Text(
        text = item.sample,
        fontFamily = fontFamily,
        fontSize = item.sizeSp.coerceAtMost(38).sp,
        lineHeight = lineHeightSp,
        fontWeight = item.weight,
        color = if (item.isSecondaryColor) textSecondary else textPrimary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

private fun resolveFontFamily(name: String): FontFamily {
  return when {
    name.contains("playfair", ignoreCase = true) ||
      name.contains("serif", ignoreCase = true) ||
      name.contains("editorial", ignoreCase = true) -> FontFamily.Serif
    name.contains("mono", ignoreCase = true) ||
      name.contains("code", ignoreCase = true) -> FontFamily.Monospace
    else -> FontFamily.SansSerif
  }
}

private fun accessibleOnColor(bg: Color): Color {
  return if (ColorUtils.calculateLuminance(bg) > 0.45) Color(0xFF1A1B19) else Color.White
}
