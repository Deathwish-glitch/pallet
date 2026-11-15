package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DesignSystemRepository
import kotlinx.coroutines.launch
import com.example.model.ColorShade
import com.example.ui.dialogs.ContrastCheckerDialog
import com.example.ui.dialogs.CreateWithAiDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppScreen {
  ONBOARDING,
  HOME,
  PROJECT_DETAIL,
  COLOR_STUDIO,
  TYPOGRAPHY_STUDIO,
  ASSISTANT,
  PROFILE
}

class MainActivity : ComponentActivity() {

  private val repository = DesignSystemRepository()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        PaletteOSApp(repository = repository)
      }
    }
  }
}

@Composable
fun PaletteOSApp(repository: DesignSystemRepository) {
  var currentScreen by remember { mutableStateOf(AppScreen.ONBOARDING) }
  val designSystems by repository.designSystems.collectAsStateWithLifecycle()
  val currentSystemId by repository.currentSystemId.collectAsStateWithLifecycle()
  val chatMessages by repository.chatMessages.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val activeSystem = remember(designSystems, currentSystemId) {
    designSystems.find { it.id == currentSystemId } ?: designSystems.first()
  }

  // Dialog States
  var showCreateAiDialog by remember { mutableStateOf(false) }
  var contrastCheckerShade by remember { mutableStateOf<ColorShade?>(null) }

  // Back handling for sub-screens
  BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.ONBOARDING) {
    currentScreen = AppScreen.HOME
  }

  Scaffold(
    containerColor = PaletteBg,
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      if (currentScreen != AppScreen.ONBOARDING) {
        PaletteBottomNavigationBar(
          currentScreen = currentScreen,
          onNavigate = { screen -> currentScreen = screen },
          onCreateClick = { showCreateAiDialog = true }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        AppScreen.ONBOARDING -> {
          OnboardingScreen(
            onGetStarted = { currentScreen = AppScreen.HOME },
            onSkip = { currentScreen = AppScreen.HOME }
          )
        }

        AppScreen.HOME -> {
          HomeScreen(
            designSystems = designSystems,
            onSelectProject = { id ->
              repository.selectSystem(id)
              currentScreen = AppScreen.PROJECT_DETAIL
            },
            onCreateWithAi = { showCreateAiDialog = true },
            onOpenPalette = { currentScreen = AppScreen.COLOR_STUDIO },
            onOpenTypography = { currentScreen = AppScreen.TYPOGRAPHY_STUDIO },
            onOpenTokens = { currentScreen = AppScreen.PROJECT_DETAIL },
            onOpenComponents = { currentScreen = AppScreen.PROJECT_DETAIL }
          )
        }

        AppScreen.PROJECT_DETAIL -> {
          ProjectDetailScreen(
            designSystem = activeSystem,
            onBack = { currentScreen = AppScreen.HOME },
            onOpenColorStudio = { currentScreen = AppScreen.COLOR_STUDIO },
            onOpenTypography = { currentScreen = AppScreen.TYPOGRAPHY_STUDIO }
          )
        }

        AppScreen.COLOR_STUDIO -> {
          ColorStudioScreen(
            designSystem = activeSystem,
            onBack = { currentScreen = AppScreen.HOME },
            onUpdatePrimaryHex = { newHex ->
              repository.updatePrimaryHex(newHex)
              Toast.makeText(context, "Regenerated 10 shades for $newHex", Toast.LENGTH_SHORT).show()
            },
            onToggleLock = { category, shadeNum ->
              repository.toggleShadeLock(category, shadeNum)
            },
            onOpenContrastChecker = { shade ->
              contrastCheckerShade = shade
            },
            onOpenExport = {
              currentScreen = AppScreen.PROJECT_DETAIL
            }
          )
        }

        AppScreen.TYPOGRAPHY_STUDIO -> {
          TypographyStudioScreen(
            designSystem = activeSystem,
            onBack = { currentScreen = AppScreen.HOME },
            onUpdateScale = { h1, h2, h3, body, caption, font ->
              repository.updateTypographyScale(h1, h2, h3, body, caption, font)
            }
          )
        }

        AppScreen.ASSISTANT -> {
          AssistantScreen(
            chatMessages = chatMessages,
            onSendMessage = { text ->
              repository.sendUserMessage(text)
            },
            onApplyProposal = { proposal, msgId ->
              repository.applyProposal(proposal, msgId)
              Toast.makeText(context, "Applied ${proposal.systemName} to project", Toast.LENGTH_SHORT).show()
            },
            onBack = { currentScreen = AppScreen.HOME }
          )
        }

        AppScreen.PROFILE -> {
          ProfileSettingsScreen(
            activeSystem = activeSystem,
            onBack = { currentScreen = AppScreen.HOME },
            onResetToOnboarding = { currentScreen = AppScreen.ONBOARDING }
          )
        }
      }
    }
  }

  // Create with AI Dialog
  if (showCreateAiDialog) {
    CreateWithAiDialog(
      onDismiss = { showCreateAiDialog = false },
      onGenerate = { prompt ->
        showCreateAiDialog = false
        coroutineScope.launch {
          Toast.makeText(context, "Designing system with Gemini AI...", Toast.LENGTH_SHORT).show()
          val result = repository.generateWithGemini(prompt)
          currentScreen = AppScreen.PROJECT_DETAIL
          val source = if (result.source == com.example.manager.PaletteSource.GEMINI_AI) "Gemini AI" else "Design Engine"
          Toast.makeText(context, "Created ${result.designSystem.name} via $source!", Toast.LENGTH_LONG).show()
        }
      }
    )
  }

  // Contrast Checker Dialog
  contrastCheckerShade?.let { shade ->
    ContrastCheckerDialog(
      initialShade = shade,
      scale = activeSystem.primaryScale,
      onDismiss = { contrastCheckerShade = null },
      onApplyShade = { chosenShade ->
        repository.updatePrimaryHex(chosenShade.hex)
        contrastCheckerShade = null
        Toast.makeText(context, "Applied accessible shade ${chosenShade.shade}", Toast.LENGTH_SHORT).show()
      }
    )
  }
}

@Composable
fun PaletteBottomNavigationBar(
  currentScreen: AppScreen,
  onNavigate: (AppScreen) -> Unit,
  onCreateClick: () -> Unit
) {
  Surface(
    color = PaletteSurface,
    border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.navigationBars)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      BottomNavIconItem(
        icon = Icons.Default.Home,
        label = "Home",
        isSelected = currentScreen == AppScreen.HOME,
        onClick = { onNavigate(AppScreen.HOME) }
      )

      BottomNavIconItem(
        icon = Icons.Default.Folder,
        label = "Projects",
        isSelected = currentScreen == AppScreen.PROJECT_DETAIL,
        onClick = { onNavigate(AppScreen.PROJECT_DETAIL) }
      )

      // Center (+) Create Action FAB
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(PaletteBrandDark)
          .clickable { onCreateClick() }
          .testTag("center_create_fab"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Create",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }

      BottomNavIconItem(
        icon = Icons.Default.ChatBubbleOutline,
        label = "Assistant",
        isSelected = currentScreen == AppScreen.ASSISTANT,
        onClick = { onNavigate(AppScreen.ASSISTANT) }
      )

      BottomNavIconItem(
        icon = Icons.Default.PersonOutline,
        label = "Profile",
        isSelected = currentScreen == AppScreen.PROFILE,
        onClick = { onNavigate(AppScreen.PROFILE) }
      )
    }
  }
}

@Composable
fun BottomNavIconItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .minimumInteractiveComponentSize()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .testTag("nav_item_${label.lowercase()}")
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (isSelected) PaletteBrandDark else PaletteTextSecondary,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
      ),
      color = if (isSelected) PaletteBrandDark else PaletteTextSecondary
    )
  }
}

@Composable
fun ProfileSettingsScreen(
  activeSystem: com.example.model.DesignSystem,
  onBack: () -> Unit,
  onResetToOnboarding: () -> Unit
) {
  val context = LocalContext.current
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(PaletteBg)
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Account & Settings",
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
      color = PaletteTextPrimary
    )

    Surface(
      shape = RoundedCornerShape(20.dp),
      color = PaletteSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(PaletteBrandOliveLight),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "AN",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PaletteBrandDark
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(
            text = "Arjun Nehe",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PaletteTextPrimary
          )
          Text(
            text = "arjunnehe7@gmail.com",
            style = MaterialTheme.typography.bodySmall,
            color = PaletteTextSecondary
          )
          Text(
            text = "Workspace: Nordic Studio Enterprise",
            style = MaterialTheme.typography.labelSmall,
            color = PaletteBrandOlive
          )
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(20.dp),
      color = PaletteSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Preferences",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = PaletteTextPrimary
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onResetToOnboarding() }
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("View Onboarding Tour", style = MaterialTheme.typography.bodyMedium)
          Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PaletteTextSecondary)
        }

        HorizontalDivider(color = PaletteMutedBorder)

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              Toast.makeText(context, "Figma Variable sync engine ready for plugin release.", Toast.LENGTH_LONG).show()
            }
            .padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Figma Sync Plugin", style = MaterialTheme.typography.bodyMedium)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(PaletteBrandOliveLight)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text("v1.0 Ready", style = MaterialTheme.typography.labelSmall, color = PaletteBrandDark)
          }
        }
      }
    }
  }
}
