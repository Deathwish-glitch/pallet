package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorScale
import com.example.model.ColorShade
import com.example.model.DesignSystem
import com.example.ui.components.ShadeSwatch
import com.example.ui.theme.*
import com.example.util.ColorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorStudioScreen(
  designSystem: DesignSystem,
  onBack: () -> Unit,
  onUpdatePrimaryHex: (String) -> Unit,
  onToggleLock: (String, Int) -> Unit,
  onOpenContrastChecker: (ColorShade) -> Unit,
  onOpenExport: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf("Primary") }
  val activeScale = when (selectedCategory) {
    "Primary" -> ColorScale("Primary", designSystem.primaryHex, designSystem.primaryScale)
    "Secondary" -> ColorScale("Secondary", designSystem.secondaryScale[5].hex, designSystem.secondaryScale)
    "Neutral" -> ColorScale("Neutral", designSystem.neutralScale[5].hex, designSystem.neutralScale)
    else -> ColorScale("Success", designSystem.successScale[5].hex, designSystem.successScale)
  }

  var selectedShade by remember { mutableStateOf(activeScale.shades.find { it.shade == 500 } ?: activeScale.shades.first()) }
  var showHexEditDialog by remember { mutableStateOf(false) }
  var editedHexInput by remember { mutableStateOf(designSystem.primaryHex) }

  // Contrast calculation of 500 shade against white
  val contrastRatio = remember(designSystem.primaryHex) {
    ColorUtils.calculateContrastRatio(ColorUtils.parseHex(designSystem.primaryHex), Color.White)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Color Studio",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { /* More options */ }) {
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
      // Category Segmented Tabs
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(PaletteSurfaceSecondary)
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        listOf("Primary", "Secondary", "Neutral", "Semantic").forEach { category ->
          val isSelected = selectedCategory == category
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) PaletteBrandDark else Color.Transparent)
              .clickable {
                selectedCategory = category
                val newScale = when (category) {
                  "Primary" -> designSystem.primaryScale
                  "Secondary" -> designSystem.secondaryScale
                  "Neutral" -> designSystem.neutralScale
                  else -> designSystem.successScale
                }
                selectedShade = newScale.find { it.shade == 500 } ?: newScale.first()
              }
              .padding(vertical = 10.dp)
              .testTag("color_tab_${category.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = category,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
              ),
              color = if (isSelected) Color.White else PaletteTextSecondary
            )
          }
        }
      }

      // Base Color Large Card
      val baseColor = ColorUtils.parseHex(activeScale.baseHex)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(baseColor)
          .clickable {
            editedHexInput = activeScale.baseHex
            showHexEditDialog = true
          }
          .padding(20.dp)
          .testTag("base_color_card")
      ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
          Text(
            text = activeScale.categoryName,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.85f)
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = activeScale.baseHex.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 24.sp
            )
          )
        }

        Box(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Colorize,
            contentDescription = "Edit Color",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // 10-Shade Swatches Row (From 50 to 900)
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = PaletteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Shade Card Family (50–950)",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = PaletteTextPrimary
            )
            Text(
              text = "${selectedShade.shade} Selected",
              style = MaterialTheme.typography.labelSmall,
              color = PaletteTextSecondary
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            activeScale.shades.forEach { shade ->
              ShadeSwatch(
                shade = shade,
                isSelected = selectedShade.shade == shade.shade,
                onClick = { selectedShade = shade },
                onToggleLock = { onToggleLock(activeScale.categoryName, shade.shade) }
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Selected shade detail box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(PaletteSurfaceSecondary)
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Role: ${selectedShade.role.ifBlank { "Tint / Surface" }}",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                  color = PaletteTextPrimary
                )
                Text(
                  text = "HEX: ${selectedShade.hex.uppercase()}  •  Token: ${activeScale.categoryName.lowercase()}/${selectedShade.shade}",
                  style = MaterialTheme.typography.labelSmall,
                  color = PaletteTextSecondary
                )
              }

              Button(
                onClick = { onToggleLock(activeScale.categoryName, selectedShade.shade) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (selectedShade.isLocked) PaletteWarning else PaletteSurface,
                  contentColor = if (selectedShade.isLocked) Color.White else PaletteTextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Text(
                  text = if (selectedShade.isLocked) "Locked" else "Lock",
                  style = MaterialTheme.typography.labelSmall
                )
              }
            }
          }
        }
      }

      // Action Rows matching mockup Screen 4
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Contrast Check
        StudioToolRow(
          icon = Icons.Default.Timelapse,
          title = "Contrast Check",
          trailingBadge = "AA ${ColorUtils.formatRatio(contrastRatio)}",
          badgeColor = if (contrastRatio >= 4.5) PaletteBrandOliveLight else Color(0xFFFFECEC),
          badgeTextColor = if (contrastRatio >= 4.5) PaletteBrandDark else PaletteError,
          onClick = { onOpenContrastChecker(selectedShade) }
        )

        // Generate Variations
        StudioToolRow(
          icon = Icons.Default.AutoAwesome,
          title = "Generate Variations",
          onClick = {
            // Pick next earth / aesthetic tint
            val variations = listOf("#4F634A", "#5B7354", "#7E9070", "#3E523A", "#2E3B2C")
            val next = variations.random()
            onUpdatePrimaryHex(next)
          }
        )

        // Palette History
        StudioToolRow(
          icon = Icons.Default.History,
          title = "Palette History",
          trailingBadge = "v${designSystem.version}",
          badgeColor = PaletteSurfaceSecondary,
          badgeTextColor = PaletteTextSecondary,
          onClick = { /* Undo / History */ }
        )

        // Export Palette
        StudioToolRow(
          icon = Icons.Default.FileDownload,
          title = "Export Palette",
          onClick = onOpenExport
        )
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // Edit Hex Modal Dialog
  if (showHexEditDialog) {
    AlertDialog(
      onDismissRequest = { showHexEditDialog = false },
      title = { Text("Set Base Color Hex") },
      text = {
        Column {
          Text(
            text = "Enter a 6-character hex code. The 10-shade scale (50–900) will automatically regenerate around it.",
            style = MaterialTheme.typography.bodySmall,
            color = PaletteTextSecondary
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = editedHexInput,
            onValueChange = { editedHexInput = it },
            label = { Text("Hex Code") },
            placeholder = { Text("#6B7F5E") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("hex_input_field")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editedHexInput.isNotBlank()) {
              val formatted = if (editedHexInput.startsWith("#")) editedHexInput else "#$editedHexInput"
              onUpdatePrimaryHex(formatted)
            }
            showHexEditDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = PaletteBrandDark)
        ) {
          Text("Apply")
        }
      },
      dismissButton = {
        TextButton(onClick = { showHexEditDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun StudioToolRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  trailingBadge: String? = null,
  badgeColor: Color = PaletteSurfaceSecondary,
  badgeTextColor: Color = PaletteTextSecondary,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = PaletteSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("tool_row_${title.lowercase().replace(" ", "_")}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = PaletteTextPrimary,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = PaletteTextPrimary
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (trailingBadge != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(badgeColor)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = trailingBadge,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = badgeTextColor
            )
          }
        }
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
          contentDescription = "Navigate",
          tint = PaletteTextSecondary,
          modifier = Modifier.size(14.dp)
        )
      }
    }
  }
}
