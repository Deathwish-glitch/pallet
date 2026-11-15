package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.example.R
import com.example.export.DesignSystemExporter
import com.example.export.ExportFormat
import com.example.export.ExportGenerator
import com.example.model.DesignSystem
import com.example.model.DesignToken
import com.example.ui.components.DesignSystemPreview
import com.example.ui.components.MetricBadge
import com.example.ui.theme.*
import com.example.util.ColorUtils
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
  designSystem: DesignSystem,
  onBack: () -> Unit,
  onOpenColorStudio: () -> Unit,
  onOpenTypography: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf("Overview") }
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = designSystem.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { /* More actions */ }) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
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
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Top Segmented Tabs: Overview | Preview | Tokens | Components | Export
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(PaletteSurfaceSecondary)
          .horizontalScroll(rememberScrollState())
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        listOf("Overview", "Preview", "Tokens", "Components", "Export").forEach { tab ->
          val isSelected = selectedTab == tab
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) PaletteBrandDark else Color.Transparent)
              .clickable { selectedTab = tab }
              .padding(horizontal = 14.dp, vertical = 10.dp)
              .testTag("project_tab_${tab.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = tab,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
              ),
              color = if (isSelected) Color.White else PaletteTextSecondary,
              maxLines = 1
            )
          }
        }
      }

      when (selectedTab) {
        "Overview" -> {
          OverviewTabContent(
            designSystem = designSystem,
            onOpenEditor = onOpenColorStudio,
            onOpenPreview = { selectedTab = "Preview" }
          )
        }
        "Preview" -> {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            DesignSystemPreview(
              designSystem = designSystem,
              onSwatchClick = { shade ->
                clipboardManager.setText(AnnotatedString(shade.hex))
                Toast.makeText(
                  context,
                  "Copied ${shade.name.ifBlank { "shade-${shade.shade}" }} (${shade.hex})",
                  Toast.LENGTH_SHORT
                ).show()
              },
              onEditColors = onOpenColorStudio,
              onEditTypography = onOpenTypography
            )
          }
        }
        "Tokens" -> {
          TokensTabContent(
            tokens = designSystem.tokens,
            onCopyToken = { token ->
              clipboardManager.setText(AnnotatedString(token.value))
              Toast.makeText(context, "Copied ${token.name}", Toast.LENGTH_SHORT).show()
            }
          )
        }
        "Components" -> {
          ComponentsTabContent(designSystem = designSystem)
        }
        "Export" -> {
          ExportTabContent(designSystem = designSystem)
        }
      }
    }
  }
}

@Composable
fun OverviewTabContent(
  designSystem: DesignSystem,
  onOpenEditor: () -> Unit,
  onOpenPreview: () -> Unit = {}
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      // Architectural Hero Image
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .clip(RoundedCornerShape(24.dp))
      ) {
        if (designSystem.heroImageRes != null) {
          Image(
            painter = painterResource(id = designSystem.heroImageRes),
            contentDescription = designSystem.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(ColorUtils.parseHex(designSystem.primaryHex).copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = null,
              tint = ColorUtils.parseHex(designSystem.primaryHex),
              modifier = Modifier.size(56.dp)
            )
          }
        }
      }

      // Title & Description
      Column {
        Text(
          text = designSystem.name,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
          ),
          color = PaletteTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = designSystem.description,
          style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 22.sp,
            fontSize = 14.sp
          ),
          color = PaletteTextSecondary
        )
      }

      // Metric Badges (Tokens, Components, Themes) matching Screen 6
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricBadge(
          count = "${designSystem.tokens.size.coerceAtLeast(42)}",
          label = "Tokens",
          modifier = Modifier.weight(1f)
        )
        MetricBadge(
          count = "18",
          label = "Components",
          modifier = Modifier.weight(1f)
        )
        MetricBadge(
          count = "2",
          label = "Themes",
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Live Preview & Open Editor CTA Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, bottom = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = onOpenPreview,
        modifier = Modifier
          .weight(1f)
          .height(54.dp)
          .testTag("open_live_preview_button"),
        shape = RoundedCornerShape(27.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder)
      ) {
        Icon(
          imageVector = Icons.Default.GridView,
          contentDescription = null,
          tint = PaletteBrandDark,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Live Grid",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          color = PaletteBrandDark
        )
      }

      Button(
        onClick = onOpenEditor,
        modifier = Modifier
          .weight(1.2f)
          .height(54.dp)
          .testTag("open_editor_button"),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = PaletteBrandDark,
          contentColor = Color.White
        )
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = "Open Editor",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Forward",
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun TokensTabContent(
  tokens: List<DesignToken>,
  onCopyToken: (DesignToken) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val filteredTokens = tokens.filter {
    it.name.contains(searchQuery, ignoreCase = true) ||
      it.category.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Filter design tokens...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      modifier = Modifier.fillMaxWidth().testTag("token_search_input"),
      shape = RoundedCornerShape(16.dp),
      singleLine = true
    )

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 80.dp)
    ) {
      items(filteredTokens) { token ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = PaletteSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = token.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 13.sp
                ),
                color = PaletteTextPrimary
              )
              if (token.description.isNotBlank()) {
                Text(
                  text = token.description,
                  style = MaterialTheme.typography.bodySmall,
                  color = PaletteTextSecondary
                )
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (token.category == "color") {
                Box(
                  modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(ColorUtils.parseHex(token.value))
                    .border(1.dp, PaletteMutedBorder, CircleShape)
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = PaletteSurfaceSecondary
              ) {
                Text(
                  text = token.value,
                  style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  color = PaletteTextPrimary
                )
              }

              IconButton(onClick = { onCopyToken(token) }) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy Token",
                  tint = PaletteTextSecondary,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ComponentsTabContent(designSystem: DesignSystem) {
  val primaryColor = ColorUtils.parseHex(designSystem.primaryHex)
  val primary600 = ColorUtils.parseHex(designSystem.primaryScale.getOrNull(6)?.hex ?: designSystem.primaryHex)
  var task1Checked by remember { mutableStateOf(true) }
  var task2Checked by remember { mutableStateOf(false) }
  var task3Checked by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    // 1. Interactive Button Family with Hover/Pressed States from explainer video
    item {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = PaletteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Button Tokens & Interaction States",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = PaletteTextPrimary
          )
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {},
              colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
              shape = RoundedCornerShape(designSystem.radius.md.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Primary 500")
            }
            Button(
              onClick = {},
              colors = ButtonDefaults.buttonColors(containerColor = primary600),
              shape = RoundedCornerShape(designSystem.radius.md.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Hover 600")
            }
          }
        }
      }
    }

    // 2. The Tasko App Card from Explainer Video!
    item {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = PaletteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Today",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = PaletteTextPrimary
              )
              Text(
                text = "Friday, 31 October",
                style = MaterialTheme.typography.bodySmall,
                color = PaletteTextSecondary
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(PaletteBrandOliveLight)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Active Palette",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PaletteBrandDark
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Task items with checkboxes
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Checkbox(
              checked = task1Checked,
              onCheckedChange = { task1Checked = it },
              colors = CheckboxDefaults.colors(checkedColor = primaryColor)
            )
            Text(
              text = "Call dentist",
              style = MaterialTheme.typography.bodyMedium,
              color = PaletteTextPrimary,
              modifier = Modifier.weight(1f)
            )
            Text(
              text = "12:30",
              style = MaterialTheme.typography.labelSmall,
              color = PaletteTextSecondary
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Checkbox(
              checked = task2Checked,
              onCheckedChange = { task2Checked = it },
              colors = CheckboxDefaults.colors(checkedColor = primaryColor)
            )
            Text(
              text = "Buy diyas",
              style = MaterialTheme.typography.bodyMedium,
              color = PaletteTextPrimary,
              modifier = Modifier.weight(1f)
            )
            Text(
              text = "5:00 PM",
              style = MaterialTheme.typography.labelSmall,
              color = PaletteTextSecondary
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Checkbox(
              checked = task3Checked,
              onCheckedChange = { task3Checked = it },
              colors = CheckboxDefaults.colors(checkedColor = primaryColor)
            )
            Text(
              text = "Gym",
              style = MaterialTheme.typography.bodyMedium,
              color = PaletteTextPrimary,
              modifier = Modifier.weight(1f)
            )
            Text(
              text = "7:00 AM",
              style = MaterialTheme.typography.labelSmall,
              color = PaletteTextSecondary
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("+ Add Task")
          }
        }
      }
    }

    // 3. Semantic Alert Cards (Success, Warning, Error from video!)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AlertCard(
          title = "Task saved",
          subtitle = "Changes written to design system foundation",
          bgColor = Color(0xFFE8F5E9),
          textColor = PaletteSuccess,
          icon = Icons.Default.CheckCircle
        )
        AlertCard(
          title = "Storage almost full",
          subtitle = "Consider archiving older system drafts",
          bgColor = Color(0xFFFFF8E1),
          textColor = PaletteWarning,
          icon = Icons.Default.Warning
        )
        AlertCard(
          title = "Wrong password",
          subtitle = "Please verify your credentials",
          bgColor = Color(0xFFFFEBEE),
          textColor = PaletteError,
          icon = Icons.Default.Error
        )
      }
    }
  }
}

@Composable
fun AlertCard(
  title: String,
  subtitle: String,
  bgColor: Color,
  textColor: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(bgColor)
      .padding(14.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = textColor)
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = textColor
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = textColor.copy(alpha = 0.85f)
        )
      }
    }
  }
}

@Composable
fun ExportTabContent(designSystem: DesignSystem) {
  var selectedFormat by remember { mutableStateOf(DesignSystemExporter.Format.JSON_DTCG) }
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val generatedCode = remember(designSystem, selectedFormat) {
    DesignSystemExporter.export(designSystem, selectedFormat)
  }

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Format Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      DesignSystemExporter.Format.values().forEach { format ->
        FilterChip(
          selected = selectedFormat == format,
          onClick = { selectedFormat = format },
          label = { Text(format.displayName) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PaletteBrandDark,
            selectedLabelColor = Color.White
          )
        )
      }
    }

    // Code Preview Box
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF1E1E1E),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
          Text(
            text = generatedCode,
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace,
              color = Color(0xFFD4D4D4),
              fontSize = 12.sp,
              lineHeight = 18.sp
            )
          )
        }
      }
    }

    // Actions Row: Copy and Share/Download
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = {
          clipboardManager.setText(AnnotatedString(generatedCode))
          Toast.makeText(context, "Copied ${selectedFormat.displayName} to clipboard", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
          .weight(1f)
          .height(52.dp)
          .testTag("copy_export_button"),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PaletteBrandDark)
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copy")
      }

      OutlinedButton(
        onClick = {
          try {
            val fileName = "${designSystem.name.lowercase().replace(" ", "_")}.${selectedFormat.fileExtension}"
            val file = DesignSystemExporter.writeToCacheFile(context, fileName, generatedCode)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
              type = selectedFormat.mimeType
              putExtra(Intent.EXTRA_TEXT, generatedCode)
              putExtra(Intent.EXTRA_TITLE, fileName)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Download or Export $fileName"))
          } catch (_: Exception) {
            clipboardManager.setText(AnnotatedString(generatedCode))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
          }
        },
        modifier = Modifier
          .weight(1f)
          .height(52.dp)
          .testTag("share_export_button"),
        shape = RoundedCornerShape(26.dp)
      ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Share / Export")
      }
    }
  }
}
