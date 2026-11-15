package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.model.ChatMessage
import com.example.model.DesignSystemProposal
import com.example.model.MessageRole
import com.example.ui.theme.*
import com.example.util.ColorUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
  chatMessages: List<ChatMessage>,
  onSendMessage: (String) -> Unit,
  onApplyProposal: (DesignSystemProposal, String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val suggestedPrompts = listOf(
    "Make the primary blue more muted and professional",
    "Consolidate my 17 greys into a 10-shade neutral card",
    "Generate dark theme with high contrast",
    "Audit WCAG contrast on all buttons",
    "Create a clean fintech design system"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Design Assistant",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(PaletteSuccess)
              )
              Text(
                text = "Online",
                style = MaterialTheme.typography.labelSmall,
                color = PaletteTextSecondary
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { /* Menu */ }) {
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
    ) {
      // Chat message feed
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(chatMessages) { message ->
          if (message.role == MessageRole.USER) {
            UserMessageBubble(message = message)
          } else {
            AssistantMessageBubble(
              message = message,
              onApply = { proposal ->
                onApplyProposal(proposal, message.id)
              }
            )
          }
        }
      }

      // Suggested Prompts Horizontal Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        suggestedPrompts.forEach { prompt ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = PaletteSurfaceSecondary,
            border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
            modifier = Modifier.clickable {
              onSendMessage(prompt)
              coroutineScope.launch {
                listState.animateScrollToItem(chatMessages.size)
              }
            }
          ) {
            Text(
              text = prompt,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
              color = PaletteTextPrimary,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
          }
        }
      }

      // Bottom Input Bar matching Screen 3
      Surface(
        color = PaletteSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Add attachment button
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(PaletteSurfaceSecondary)
              .clickable { /* Attach */ },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Attach",
              tint = PaletteTextPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          // Text Field
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = {
              Text(
                "Ask anything about your design...",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = PaletteTextSecondary
              )
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = PaletteMutedBorder,
              focusedBorderColor = PaletteBrandOlive
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("assistant_input_field"),
            singleLine = true
          )

          // Send Button
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(if (inputText.isNotBlank()) PaletteBrandDark else PaletteSurfaceSecondary)
              .clickable(enabled = inputText.isNotBlank()) {
                val text = inputText
                inputText = ""
                onSendMessage(text)
                coroutineScope.launch {
                  listState.animateScrollToItem(chatMessages.size)
                }
              }
              .testTag("assistant_send_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send",
              tint = if (inputText.isNotBlank()) Color.White else PaletteTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun UserMessageBubble(message: ChatMessage) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End
  ) {
    Surface(
      shape = RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
      color = PaletteBrandOliveLight,
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Text(
        text = message.text,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
        color = PaletteBrandDark,
        modifier = Modifier.padding(14.dp)
      )
    }
  }
}

@Composable
fun AssistantMessageBubble(
  message: ChatMessage,
  onApply: (DesignSystemProposal) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(PaletteBrandOliveLight),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = "AI",
        tint = PaletteBrandDark,
        modifier = Modifier.size(16.dp)
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column(modifier = Modifier.widthIn(max = 320.dp)) {
      Surface(
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
        color = PaletteSurfaceSecondary,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
            color = PaletteTextPrimary
          )

          // Structured Proposal Card (Matching Screen 3)
          if (message.proposal != null) {
            val proposal = message.proposal
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
              shape = RoundedCornerShape(16.dp),
              color = PaletteSurface,
              border = androidx.compose.foundation.BorderStroke(1.dp, PaletteMutedBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                // Swatches row
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  listOf("#4A5B43", "#6B7F5E", "#9E8B6D", "#D4C7B5", "#F4F0EA").forEach { hex ->
                    Box(
                      modifier = Modifier
                        .size(width = 36.dp, height = 28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ColorUtils.parseHex(hex))
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Typography pill
                Row(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PaletteSurfaceSecondary)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = "Aa",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PaletteTextPrimary
                  )
                  Text(
                    text = proposal.typographyFont,
                    style = MaterialTheme.typography.labelSmall,
                    color = PaletteTextSecondary
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Checklist items with checkmarks
                ChecklistItem("Color palette (${proposal.scalesCount} scales)")
                ChecklistItem("Typography (${proposal.typographyFont})")
                ChecklistItem("Design tokens (${proposal.tokensCount})")
                ChecklistItem("UI components (${proposal.componentsCount})")

                Spacer(modifier = Modifier.height(14.dp))

                // Apply button
                Button(
                  onClick = { onApply(proposal) },
                  enabled = !message.isApplied,
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (message.isApplied) PaletteSuccess else PaletteBrandDark,
                    contentColor = Color.White
                  )
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(if (message.isApplied) "Applied to Project" else "Apply to Project")
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ChecklistItem(text: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(16.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(PaletteBrandOliveLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = PaletteBrandDark,
          modifier = Modifier.size(12.dp)
        )
      }
      Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        color = PaletteTextPrimary
      )
    }

    Icon(
      imageVector = Icons.Default.Check,
      contentDescription = "Ready",
      tint = PaletteSuccess,
      modifier = Modifier.size(14.dp)
    )
  }
}
