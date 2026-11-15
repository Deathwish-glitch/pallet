package com.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.ColorShade
import com.example.model.DesignSystem
import com.example.ui.components.DesignSystemPreview as InternalDesignSystemPreview

/**
 * Top-level convenience wrapper for DesignSystemPreview in package com.example.
 */
@Composable
fun DesignSystemPreview(
  designSystem: DesignSystem,
  modifier: Modifier = Modifier,
  onSwatchClick: ((ColorShade) -> Unit)? = null,
  onEditColors: (() -> Unit)? = null,
  onEditTypography: (() -> Unit)? = null
) {
  InternalDesignSystemPreview(
    designSystem = designSystem,
    modifier = modifier,
    onSwatchClick = onSwatchClick,
    onEditColors = onEditColors,
    onEditTypography = onEditTypography
  )
}
