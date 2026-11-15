package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DesignSystem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypographyStudioScreen(
  designSystem: DesignSystem,
  onBack: () -> Unit,
  onUpdateScale: (Int, Int, Int, Int, Int, String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf("Scale") }
  var selectedFont by remember { mutableStateOf(designSystem.typography.fontFamily) }
  var showFontMenu by remember { mutableStateOf(false) }

  var h1Val by remember { mutableStateOf(designSystem.typography.h1Sp.toFloat()) }
  var h2Val by remember { mutableStateOf(designSystem.typography.h2Sp.toFloat()) }
  var h3Val by remember { mutableStateOf(designSystem.typography.h3Sp.toFloat()) }
  var bodyVal by remember { mutableStateOf(designSystem.typography.bodySp.toFloat()) }
  var captionVal by remember { mutableStateOf(designSystem.typography.captionSp.toFloat()) }

  val fontFamilies = listOf("Inter", "Geist", "Roboto", "Outfit", "Playfair Display", "Monospace")

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Typography",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { /* Options */ }) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PaletteBg)
      )
    },
    containerColor = PaletteBg,
    modifier = modifier
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // Main Typeface Card matching Screen 5
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = PaletteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              Text(
                text = "Aa",
                style = MaterialTheme.typography.headlineLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 38.sp
                ),
                color = PaletteTextPrimary
              )
              Text(
                text = selectedFont,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = PaletteTextPrimary
              )
            }

            Box {
              IconButton(onClick = { showFontMenu = true }) {
                Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = "Select Font",
                  tint = PaletteTextSecondary
                )
              }
              DropdownMenu(
                expanded = showFontMenu,
                onDismissRequest = { showFontMenu = false }
              ) {
                fontFamilies.forEach { font ->
                  DropdownMenuItem(
                    text = { Text(font) },
                    onClick = {
                      selectedFont = font
                      showFontMenu = false
                      onUpdateScale(
                        h1Val.toInt(),
                        h2Val.toInt(),
                        h3Val.toInt(),
                        bodyVal.toInt(),
                        captionVal.toInt(),
                        font
                      )
                    }
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "The quick brown fox jumps over the lazy dog.",
            style = MaterialTheme.typography.bodyLarge.copy(
              fontSize = 18.sp,
              lineHeight = 26.sp
            ),
            color = PaletteTextPrimary
          )
        }
      }

      // Mode Tabs: Scale | Weights | Spacing
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(PaletteSurfaceSecondary)
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        listOf("Scale", "Weights", "Spacing").forEach { tab ->
          val isSelected = selectedTab == tab
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) PaletteBrandDark else Color.Transparent)
              .clickable { selectedTab = tab }
              .padding(vertical = 10.dp)
              .testTag("typography_tab_${tab.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = tab,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
              ),
              color = if (isSelected) Color.White else PaletteTextSecondary
            )
          }
        }
      }

      // Sliders matching Screen 5
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = PaletteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // H1 Slider
          TypographyScaleSlider(
            label = "H1",
            value = h1Val,
            valueRange = 28f..56f,
            unit = "px",
            onValueChange = {
              h1Val = it
              onUpdateScale(
                it.toInt(),
                h2Val.toInt(),
                h3Val.toInt(),
                bodyVal.toInt(),
                captionVal.toInt(),
                selectedFont
              )
            }
          )

          // H2 Slider
          TypographyScaleSlider(
            label = "H2",
            value = h2Val,
            valueRange = 22f..40f,
            unit = "px",
            onValueChange = {
              h2Val = it
              onUpdateScale(
                h1Val.toInt(),
                it.toInt(),
                h3Val.toInt(),
                bodyVal.toInt(),
                captionVal.toInt(),
                selectedFont
              )
            }
          )

          // H3 Slider
          TypographyScaleSlider(
            label = "H3",
            value = h3Val,
            valueRange = 18f..32f,
            unit = "px",
            onValueChange = {
              h3Val = it
              onUpdateScale(
                h1Val.toInt(),
                h2Val.toInt(),
                it.toInt(),
                bodyVal.toInt(),
                captionVal.toInt(),
                selectedFont
              )
            }
          )

          // Body Slider
          TypographyScaleSlider(
            label = "Body",
            value = bodyVal,
            valueRange = 12f..20f,
            unit = "px",
            onValueChange = {
              bodyVal = it
              onUpdateScale(
                h1Val.toInt(),
                h2Val.toInt(),
                h3Val.toInt(),
                it.toInt(),
                captionVal.toInt(),
                selectedFont
              )
            }
          )

          // Caption Slider
          TypographyScaleSlider(
            label = "Caption",
            value = captionVal,
            valueRange = 10f..16f,
            unit = "px",
            onValueChange = {
              captionVal = it
              onUpdateScale(
                h1Val.toInt(),
                h2Val.toInt(),
                h3Val.toInt(),
                bodyVal.toInt(),
                it.toInt(),
                selectedFont
              )
            }
          )
        }
      }

      // Live Hierarchy Preview Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = PaletteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Rendered Hierarchy Preview",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = PaletteTextSecondary
          )
          HorizontalDivider(color = PaletteMutedBorder)

          Text(
            text = "Heading 1 Preview",
            fontSize = h1Val.sp,
            fontWeight = FontWeight.Bold,
            color = PaletteTextPrimary
          )
          Text(
            text = "Heading 2 Preview",
            fontSize = h2Val.sp,
            fontWeight = FontWeight.SemiBold,
            color = PaletteTextPrimary
          )
          Text(
            text = "Heading 3 Preview",
            fontSize = h3Val.sp,
            fontWeight = FontWeight.Medium,
            color = PaletteTextPrimary
          )
          Text(
            text = "Body text: Designed with architectural minimalism and conversational intelligence.",
            fontSize = bodyVal.sp,
            color = PaletteTextPrimary
          )
          Text(
            text = "Caption: Token alias typography.scale.caption",
            fontSize = captionVal.sp,
            color = PaletteTextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

@Composable
fun TypographyScaleSlider(
  label: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  unit: String,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = PaletteTextPrimary,
      modifier = Modifier.width(60.dp)
    )

    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = valueRange,
      colors = SliderDefaults.colors(
        thumbColor = PaletteBrandOlive,
        activeTrackColor = PaletteBrandOlive,
        inactiveTrackColor = PaletteBrandOliveLight
      ),
      modifier = Modifier
        .weight(1f)
        .testTag("slider_${label.lowercase()}")
    )

    Spacer(modifier = Modifier.width(12.dp))

    Text(
      text = "${value.toInt()} $unit",
      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
      color = PaletteTextSecondary,
      modifier = Modifier.width(46.dp)
    )
  }
}
