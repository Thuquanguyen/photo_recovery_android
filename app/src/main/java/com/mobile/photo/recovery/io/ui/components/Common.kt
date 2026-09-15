package com.mobile.photo.recovery.io.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ui.theme.OnSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurfaceVariant
import com.mobile.photo.recovery.io.ui.theme.Outline
import com.mobile.photo.recovery.io.ui.theme.OutlineVariant
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainer
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerHigh
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLowest

/**
 * Circular "ghost" utility button on a `surface-container` disc — the back / tune / filter
 * control repeated at the top of every light-themed tool screen in the mockups.
 */
@Composable
fun GhostCircleButton(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    tint: Color = OnSurface,
    container: Color = SurfaceContainer,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/**
 * Back action for the tool screens' sub-header. These screens are registered in the nav graph
 * without an `onBack` parameter, so the header reuses the activity's back dispatcher rather than
 * changing the navigation wiring.
 */
@Composable
fun rememberBackAction(): () -> Unit {
    val dispatcher = androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    return { dispatcher?.onBackPressed() }
}

/**
 * Sub-header row shared by Photo Recovery, Quick Swipe Clean, Duplicate Cleaner and
 * Screenshot Cleaner: a round back button, a centred title with an uppercase status caption
 * under it, and an optional trailing action.
 */
@Composable
fun ScreenSubHeader(
    title: String,
    caption: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    captionDotColor: Color = Secondary,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GhostCircleButton(
            icon = Icons.AutoMirrored.Filled.ArrowBackIos,
            contentDescription = null,
            onClick = onBack
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = OnSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(captionDotColor, CircleShape))
                Spacer(Modifier.width(4.dp))
                Text(
                    caption.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
        if (trailing != null) trailing() else Spacer(Modifier.size(40.dp))
    }
}

/** Small pill with a leading status dot, e.g. "Deep Scan Complete" / "Swipe Queue Active". */
@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color = Secondary,
    container: Color = SurfaceContainer,
    contentColor: Color = OnSurfaceVariant,
    leadingIcon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(13.dp))
        } else {
            Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
        }
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}

/** Uppercase `label-caps` section header, e.g. "GENERAL", "POPULAR LANGUAGES". */
@Composable
fun SectionCapsHeader(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = Primary,
    trailing: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = Outline)
        }
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
    }
}

/**
 * The floating, translucent action deck docked above the bottom safe area
 * ("Floating Recovery Action Bar" in the design system).
 */
@Composable
fun FloatingGlassDeck(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(32.dp),
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceContainerLowest.copy(alpha = 0.94f))
            .border(1.dp, OutlineVariant.copy(alpha = 0.35f), shape)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/** Compact filter chip: solid white when inactive, primary-filled when active. */
@Composable
fun FilterPill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(CircleShape)
            .background(if (selected) Primary else SurfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else OnSurfaceVariant
        )
    }
}

/** Small pill-shaped stat chip, e.g. "128 Photos". */
@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = Primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
    }
}

/**
 * One tile of the three-across "diagnostic" stat row on the Photo Recovery banner:
 * a tinted rounded box with a caption and a large tracked number.
 */
@Composable
fun DiagnosticStatTile(
    icon: ImageVector,
    caption: String,
    value: String,
    unit: String,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier,
    valueColor: Color = OnSurface
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(caption, style = MaterialTheme.typography.labelSmall, color = accent, maxLines = 1)
        }
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor, maxLines = 1)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.8f))
    }
}

@Composable
fun FullScreenLoading() {
    Column(
        modifier = Modifier.fillMaxSize().background(com.mobile.photo.recovery.io.ui.theme.Surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Primary)
    }
}

@Composable
fun PermissionRequiredState(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.mobile.photo.recovery.io.ui.theme.Surface)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LockPerson, contentDescription = null, tint = Primary, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResourceCompat(R.string.permission_required_title),
            style = MaterialTheme.typography.headlineSmall,
            color = OnSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResourceCompat(R.string.permission_required_message),
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        PrimaryPillButton(text = stringResourceCompat(R.string.open_settings), onClick = onOpenSettings)
    }
}

/** Notice shown when the OS granted only limited/partial media access (spec 5.1). */
@Composable
fun LimitedAccessBanner() {
    Text(
        stringResourceCompat(R.string.permission_limited_message),
        style = MaterialTheme.typography.bodySmall,
        color = OnSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
fun EmptyMediaState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.mobile.photo.recovery.io.ui.theme.Surface)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)
