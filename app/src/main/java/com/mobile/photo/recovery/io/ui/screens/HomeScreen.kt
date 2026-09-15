package com.mobile.photo.recovery.io.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appadskit.AdPlacement
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ads.NativeAdLayout
import com.mobile.photo.recovery.io.ads.NativeAdSlot
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.theme.AccentAmber
import com.mobile.photo.recovery.io.ui.theme.AccentCyan
import com.mobile.photo.recovery.io.ui.theme.AccentEmerald
import com.mobile.photo.recovery.io.ui.theme.AccentPink
import com.mobile.photo.recovery.io.ui.theme.DarkGradientStops
import com.mobile.photo.recovery.io.ui.theme.GlassFill
import com.mobile.photo.recovery.io.ui.theme.GlassStroke
import com.mobile.photo.recovery.io.ui.vm.HomeViewModel
import com.mobile.photo.recovery.io.util.formatBytes

@Composable
fun HomeScreen(
    onStartScan: () -> Unit,
    onQuickClean: () -> Unit,
    onDuplicate: () -> Unit,
    onScreenshot: () -> Unit,
    onRecentlyDeleted: () -> Unit, // aliases to Photo Recovery, spec 4.4
    onVault: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    // The mockup's gradient stops aren't evenly spaced (0%, 25%, 55%, 80%, 100%) — using
    // Brush.linearGradient(list) spaces them evenly instead (0/25/50/75/100), which pulls the
    // brighter magenta tone too far up the screen. Weighted stops keep the darker violet
    // dominant for longer, matching the mockup's 165deg gradient more closely.
    val homeBackground = Brush.verticalGradient(
        0f to DarkGradientStops[0],
        0.25f to DarkGradientStops[1],
        0.55f to DarkGradientStops[2],
        0.8f to DarkGradientStops[3],
        1f to DarkGradientStops[4]
    )
    Box(modifier = Modifier.fillMaxSize().background(homeBackground)) {
        // Ambient glowing highlights — soft/diffuse per the mockup's blur-3xl/[90px]/[100px], not
        // hard-edged circles.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-96).dp)
                .size(340.dp)
                .blur(90.dp, BlurredEdgeTreatment.Unbounded)
                .background(Color(0xFFA855F7).copy(alpha = 0.13f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-80).dp, y = (-60).dp)
                .size(288.dp)
                .blur(90.dp, BlurredEdgeTreatment.Unbounded)
                .background(Color(0xFF4F46E5).copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 64.dp, y = 100.dp)
                .size(320.dp)
                .blur(100.dp, BlurredEdgeTreatment.Unbounded)
                .background(Color(0xFFD946EF).copy(alpha = 0.13f), CircleShape)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top navigation header on a frosted bar with a hairline bottom border. Its own
            // statusBarsPadding() (not the outer Column's) so this bar's own navy background
            // bleeds behind the status bar — putting the padding on the Column instead would
            // leave the general screen gradient showing there rather than the header's color.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF180B2A).copy(alpha = 0.6f))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF9333EA), AccentPink))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.home_ai_pro_badge).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = Color(0xFF67E8F9),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AccentCyan.copy(alpha = 0.2f))
                                    .border(1.dp, AccentCyan.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            stringResource(R.string.home_deep_storage_engine),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AccentAmber.copy(alpha = 0.25f), Color(0xFFEAB308).copy(alpha = 0.2f))
                                )
                            )
                            .border(1.dp, AccentAmber.copy(alpha = 0.4f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.home_pro_badge).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentAmber
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassFill)
                            .border(1.dp, GlassStroke, CircleShape)
                            .clickable(onClick = onSettings),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassStroke))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Hero core recovery card / radar pulse hub.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(GlassFill)
                        .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.home_secure_banner),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Radar rings around the central scanner trigger.
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(176.dp)) {
                        Box(
                            modifier = Modifier
                                .size(176.dp)
                                .border(1.dp, Color(0xFFA78BFA).copy(alpha = 0.2f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(144.dp)
                                .border(1.dp, Color(0xFFF472B6).copy(alpha = 0.3f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF9333EA).copy(alpha = 0.4f), AccentPink.copy(alpha = 0.3f))
                                    ),
                                    CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF8455EF), Color(0xFF9D4EDD), Color(0xFFE040FB)))
                                )
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(Color(0xFF3A0CA3), Color(0xFF7209B7))))
                                .clickable(onClick = onStartScan),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Radar, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                Text(
                                    "SCAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = Color(0xFFA5F3FC)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.home_ready_to_scan),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.home_ready_to_scan_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))
                    // Big primary CTA — a gradient rounded-2xl bar, not a plain pill.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFA855F7), Color(0xFFD946EF), Color(0xFFEC4899))
                                )
                            )
                            .clickable(onClick = onStartScan),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.home_start_scan),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "• ${formatBytes(state.freeSpaceBytes)} ${stringResource(R.string.home_scan_ready_suffix)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                NativeAdSlot(
                    placement = AdPlacement.NATIVE_HOME,
                    layout = NativeAdLayout.Medium
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    stringResource(R.string.home_section_smart_clean).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // 2x2 glass utility grid.
                val tiles = listOf(
                    HomeGridItem(Icons.Filled.ContentCopy, R.string.home_grid_duplicate, R.string.home_grid_duplicate_desc, Color(0xFFC4B5FD), onDuplicate),
                    HomeGridItem(Icons.Filled.PhotoCamera, R.string.home_grid_screenshot, R.string.home_grid_screenshot_desc, Color(0xFFF9A8D4), onScreenshot),
                    HomeGridItem(Icons.Filled.Restore, R.string.home_grid_recently_deleted, R.string.home_grid_recently_deleted_desc, AccentCyan, onRecentlyDeleted),
                    HomeGridItem(Icons.Filled.Lock, R.string.home_grid_vault, R.string.home_grid_vault_desc, AccentEmerald, onVault)
                )
                UniformHeightGrid(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    columns = 2,
                    spacing = 12.dp
                ) {
                    tiles.forEach { item -> HomeGridCard(item) }
                }

                // Quick Swipe Clean shortcut — an app feature with no tile of its own in the mockup.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassFill)
                        .border(1.dp, GlassStroke, RoundedCornerShape(16.dp))
                        .clickable(onClick = onQuickClean)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentEmerald.copy(alpha = 0.25f))
                            .border(1.dp, AccentEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            stringResource(R.string.home_quick_clean_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Text(
                            stringResource(R.string.home_quick_clean_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/**
 * A fixed-column grid where every cell is forced to the same height — the tallest cell's natural
 * (intrinsic) height — so cards with a 1-line vs 2-line description don't end up mismatched.
 */
@Composable
private fun UniformHeightGrid(
    modifier: Modifier = Modifier,
    columns: Int,
    spacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content, modifier = modifier) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val columnWidth = (constraints.maxWidth - spacingPx * (columns - 1)) / columns
        val maxHeight = measurables.maxOf { it.maxIntrinsicHeight(columnWidth) }
        val cellConstraints = androidx.compose.ui.unit.Constraints.fixed(columnWidth, maxHeight)
        val placeables = measurables.map { it.measure(cellConstraints) }
        val rows = (placeables.size + columns - 1) / columns
        val totalHeight = rows * maxHeight + (rows - 1) * spacingPx
        layout(constraints.maxWidth, totalHeight) {
            placeables.forEachIndexed { index, placeable ->
                val col = index % columns
                val row = index / columns
                placeable.placeRelative(col * (columnWidth + spacingPx), row * (maxHeight + spacingPx))
            }
        }
    }
}

private data class HomeGridItem(
    val icon: ImageVector,
    val labelRes: Int,
    val descRes: Int,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
private fun HomeGridCard(item: HomeGridItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassFill)
            .border(1.dp, GlassStroke, RoundedCornerShape(16.dp))
            .clickable(onClick = item.onClick)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.tint.copy(alpha = 0.25f))
                .border(1.dp, item.tint.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = null, tint = item.tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(item.labelRes),
            style = MaterialTheme.typography.titleSmall,
            color = Color.White
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(item.descRes),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}
