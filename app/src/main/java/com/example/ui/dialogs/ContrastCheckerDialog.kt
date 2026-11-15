package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.model.ColorShade
import com.example.ui.theme.*
import com.example.util.ColorUtils

@Composable
fun ContrastCheckerDialog(
  initialShade: ColorShade,
  scale: List<ColorShade>,
  onDismiss: () -> Unit,
  onApplyShade: (ColorShade) -> Unit
) {
  var fgHex by remember { mutableStateOf("#FFFFFF") }
  var bgHex by remember { mutableStateOf(initialShade.hex) }

  val fgColor = ColorUtils.parseHex(fgHex, Color.White)
  val bgColor = ColorUtils.parseHex(bgHex, ColorUtils.parseHex(initialShade.hex))

  val ratio = remember(fgColor, bgColor) {
    ColorUtils.calculateContrastRatio(fgColor, bgColor)
  }

  val isAaNormal = ColorUtils.isAaNormalPassed(ratio)
  val isAaLarge = ColorUtils.isAaLargePassed(ratio)
  val isAaa = ColorUtils.isAaaNormalPassed(ratio)

  // Find accessible alternative if failing
  val recommendedShade = remember(scale, fgColor) {
    ColorUtils.findAccessibleShade(scale, fgColor, minRatio = 4.5)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "WCAG 2.1 Contrast Checker",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Specimen Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, PaletteMutedBorder, RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Aa Specimen",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = fgColor
            )
            Text(
              text = "The quick brown fox",
              style = MaterialTheme.typography.bodySmall,
              color = fgColor
            )
          }
        }

        // Ratio & Score
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = ColorUtils.formatRatio(ratio),
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
              ),
              color = if (isAaNormal) PaletteBrandDark else PaletteError
            )
            Text(
              text = "Contrast Ratio",
              style = MaterialTheme.typography.labelSmall,
              color = PaletteTextSecondary
            )
          }

          Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            ContrastBadge("Normal Text (4.5:1)", isAaNormal)
            ContrastBadge("Large Text (3.0:1)", isAaLarge)
            ContrastBadge("AAA Strict (7.0:1)", isAaa)
          }
        }

        // Inputs for Foreground & Background
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = fgHex,
            onValueChange = { fgHex = it },
            label = { Text("Foreground") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = bgHex,
            onValueChange = { bgHex = it },
            label = { Text("Background") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        // Recommendation banner if ratio is low
        if (!isAaNormal && recommendedShade != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = PaletteBrandOliveLight,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "Recommendation from Shade Card:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PaletteBrandDark
              )
              Text(
                text = "Use shade ${recommendedShade.shade} (${recommendedShade.hex}) for certified AA pass.",
                style = MaterialTheme.typography.bodySmall,
                color = PaletteBrandDark
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = {
                  bgHex = recommendedShade.hex
                  onApplyShade(recommendedShade)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PaletteBrandDark),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Text("Apply Shade ${recommendedShade.shade}", style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = PaletteBrandDark)
      ) {
        Text("Done")
      }
    }
  )
}

@Composable
fun ContrastBadge(label: String, passed: Boolean) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = PaletteTextSecondary
    )
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(if (passed) PaletteSuccess.copy(alpha = 0.15f) else PaletteError.copy(alpha = 0.15f))
        .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
      Text(
        text = if (passed) "PASS" else "FAIL",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp
        ),
        color = if (passed) PaletteSuccess else PaletteError
      )
    }
  }
}
