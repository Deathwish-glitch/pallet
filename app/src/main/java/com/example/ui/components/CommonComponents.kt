package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun ShadeSwatch(
  shade: ColorShade,
  isSelected: Boolean,
  onClick: () -> Unit,
  onToggleLock: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val color = ColorUtils.parseHex(shade.hex)
  val isLight = ColorUtils.calculateLuminance(color) > 0.45
  val contentColor = if (isLight) Color(0xFF20211F) else Color.White

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .width(52.dp)
      .clickable { onClick() }
      .testTag("shade_swatch_${shade.shade}")
  ) {
    Box(
      modifier = Modifier
        .size(46.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(color)
        .then(
          if (isSelected) {
            Modifier.border(2.5.dp, PaletteBrandDark, RoundedCornerShape(10.dp))
          } else {
            Modifier.border(1.dp, PaletteMutedBorder, RoundedCornerShape(10.dp))
          }
        ),
      contentAlignment = Alignment.Center
    ) {
      if (shade.isLocked) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Locked",
          tint = contentColor.copy(alpha = 0.8f),
          modifier = Modifier.size(16.dp)
        )
      } else if (isSelected) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(contentColor)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "${shade.shade}",
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
      ),
      color = if (isSelected) PaletteTextPrimary else PaletteTextSecondary
    )
  }
}

@Composable
fun MetricBadge(
  count: String,
  label: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = PaletteSurfaceSecondary,
    border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
    modifier = modifier
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Text(
        text = count,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = PaletteTextPrimary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        color = PaletteTextSecondary
      )
    }
  }
}
