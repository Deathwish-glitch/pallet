package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DesignSystem
import com.example.ui.theme.*
import com.example.util.ColorUtils

@Composable
fun HomeScreen(
  designSystems: List<DesignSystem>,
  onSelectProject: (String) -> Unit,
  onCreateWithAi: () -> Unit,
  onOpenPalette: () -> Unit,
  onOpenTypography: () -> Unit,
  onOpenTokens: () -> Unit,
  onOpenComponents: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(PaletteBg)
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Top Greeting Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Good morning, Arjun 👋",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp
            ),
            color = PaletteTextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Let's build a better design system.",
            style = MaterialTheme.typography.bodyMedium,
            color = PaletteTextSecondary
          )
        }

        // Avatar
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(PaletteBrandOliveLight)
            .border(1.dp, PaletteMutedBorder, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "AN",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = PaletteBrandDark
          )
        }
      }
    }

    // "Create with AI" Banner Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(24.dp))
          .background(
            Brush.linearGradient(
              colors = listOf(
                PaletteBrandDark,
                Color(0xFF3E503E),
                Color(0xFF566952)
              )
            )
          )
          .clickable { onCreateWithAi() }
          .testTag("create_with_ai_banner")
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFD4E0D2),
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = "Create with AI",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                ),
                color = Color.White
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Generate a complete design system from a simple prompt.",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp
              ),
              color = Color.White.copy(alpha = 0.85f)
            )
          }

          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Color.White),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Create",
              tint = PaletteBrandDark,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // Quick Feature Navigation Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionChip(
          iconText = "🎨",
          label = "Palette",
          onClick = onOpenPalette,
          modifier = Modifier.weight(1f)
        )
        QuickActionChip(
          iconText = "Aa",
          label = "Typography",
          onClick = onOpenTypography,
          modifier = Modifier.weight(1f)
        )
        QuickActionChip(
          iconText = "🪙",
          label = "Tokens",
          onClick = onOpenTokens,
          modifier = Modifier.weight(1f)
        )
        QuickActionChip(
          iconText = "🎛️",
          label = "Components",
          onClick = onOpenComponents,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Recent Projects Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Projects",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          ),
          color = PaletteTextPrimary
        )
        TextButton(onClick = { /* View all */ }) {
          Text(
            text = "See All",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = PaletteTextSecondary
          )
        }
      }
    }

    // Recent Projects List
    items(designSystems) { project ->
      ProjectCard(
        project = project,
        onClick = { onSelectProject(project.id) }
      )
    }
  }
}

@Composable
fun QuickActionChip(
  iconText: String,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = PaletteSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
    modifier = modifier
      .clickable { onClick() }
      .testTag("quick_action_${label.lowercase()}")
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp)
    ) {
      Text(
        text = iconText,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 11.sp
        ),
        color = PaletteTextPrimary
      )
    }
  }
}

@Composable
fun ProjectCard(
  project: DesignSystem,
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
      .testTag("project_card_${project.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Thumbnail
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(ColorUtils.parseHex(project.primaryHex).copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        if (project.heroImageRes != null) {
          Image(
            painter = painterResource(id = project.heroImageRes),
            contentDescription = project.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(ColorUtils.parseHex(project.primaryHex))
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = project.name,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          color = PaletteTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${project.category} System • ${project.updatedAt}",
          style = MaterialTheme.typography.bodySmall,
          color = PaletteTextSecondary
        )
      }

      IconButton(onClick = { /* Project options */ }) {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = "Options",
          tint = PaletteTextSecondary
        )
      }
    }
  }
}
