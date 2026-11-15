package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CreateWithAiDialog(
  onDismiss: () -> Unit,
  onGenerate: (String) -> Unit
) {
  var prompt by remember { mutableStateOf("") }

  val suggestions = listOf(
    "Architecture app with limestone neutrals and forest olive brand",
    "Tasko productivity system with single 500 blue and 10-shade grey",
    "Fintech platform with deep emerald green and slate borders",
    "Calm meditation wellness app with warm terracotta"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = PaletteBrandDark
        )
        Text(
          text = "Generate Design System",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Describe your application, aesthetic, and brand identity in natural language. The AI will generate color shade families, typography scales, tokens, and verified WCAG accessible mappings.",
          style = MaterialTheme.typography.bodySmall,
          color = PaletteTextSecondary
        )

        OutlinedTextField(
          value = prompt,
          onValueChange = { prompt = it },
          placeholder = { Text("e.g. Modern construction app with earth tones...") },
          modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .testTag("ai_prompt_input"),
          shape = RoundedCornerShape(14.dp)
        )

        Text(
          text = "Or pick an archetype:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = PaletteTextPrimary
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          suggestions.forEach { s ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PaletteSurfaceSecondary)
                .clickable { prompt = s }
                .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
              Text(
                text = s,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = PaletteTextPrimary
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (prompt.isNotBlank()) {
            onGenerate(prompt)
          }
        },
        enabled = prompt.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = PaletteBrandDark),
        modifier = Modifier.testTag("generate_system_confirm_button")
      ) {
        Text("Generate System")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
