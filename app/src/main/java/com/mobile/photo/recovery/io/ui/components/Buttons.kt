package com.mobile.photo.recovery.io.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mobile.photo.recovery.io.ui.theme.NeutralDark
import com.mobile.photo.recovery.io.ui.theme.Primary

/** Rounded pill-shaped primary button, per the design system. */
@Composable
fun PrimaryPillButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    leadingIcon: ImageVector? = null,
    containerColor: Color = Primary,
    contentColor: Color = Color.White,
    height: androidx.compose.ui.unit.Dp = 52.dp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier.height(height)
    ) {
        if (trailingIcon != null || leadingIcon != null) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                if (leadingIcon != null) Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(text, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                if (trailingIcon != null) Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        } else {
            Text(text, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
    }
}

/** Inverted pill button (light background, dark text) for use on colored surfaces. */
@Composable
fun InvertedPillButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Primary),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier.height(52.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Outlined pill button. */
@Composable
fun OutlinedPillButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(1.5.dp, Primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier.height(52.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun TextOnlyButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = NeutralDark, style = MaterialTheme.typography.labelLarge)
    }
}
