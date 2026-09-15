package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.appadskit.AdPlacement
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.ads.NativeAdLayout
import com.mobile.photo.recovery.io.ads.NativeAdSlot
import com.mobile.photo.recovery.io.data.MediaItem
import com.mobile.photo.recovery.io.ui.components.DiagnosticStatTile
import com.mobile.photo.recovery.io.ui.components.EmptyMediaState
import com.mobile.photo.recovery.io.ui.components.FloatingGlassDeck
import com.mobile.photo.recovery.io.ui.components.GhostCircleButton
import com.mobile.photo.recovery.io.ui.components.LimitedAccessBanner
import com.mobile.photo.recovery.io.ui.components.PermissionRequiredState
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.components.ScreenSubHeader
import com.mobile.photo.recovery.io.ui.components.StatusPill
import com.mobile.photo.recovery.io.ui.components.rememberBackAction
import com.mobile.photo.recovery.io.ui.theme.DangerRed
import com.mobile.photo.recovery.io.ui.theme.ErrorContainer
import com.mobile.photo.recovery.io.ui.theme.InverseOnSurface
import com.mobile.photo.recovery.io.ui.theme.InverseSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurfaceVariant
import com.mobile.photo.recovery.io.ui.theme.OutlineVariant
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.SecondaryContainer
import com.mobile.photo.recovery.io.ui.theme.Surface
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainer
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerHigh
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLowest
import com.mobile.photo.recovery.io.ui.theme.TertiaryFixed
import com.mobile.photo.recovery.io.ui.theme.OnTertiaryFixed
import com.mobile.photo.recovery.io.ui.vm.PhotoRecoveryViewModel
import com.mobile.photo.recovery.io.util.MediaPermissionStatus
import com.mobile.photo.recovery.io.util.formatBytes
import com.mobile.photo.recovery.io.util.formatRelativeDate
import com.mobile.photo.recovery.io.util.openAppSettings
import com.mobile.photo.recovery.io.util.rememberMediaPermissionState

// Mirrors PhotoRecoveryViewModel's private PAGE_SIZE — used only to label the Load More button.
private const val PHOTO_RECOVERY_PAGE_SIZE = 10

@Composable
fun PhotoRecoveryScreen(viewModel: PhotoRecoveryViewModel = viewModel()) {
    val (permissionStatus, requestPermission) = rememberMediaPermissionState()
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.photo_recovery_success_snackbar)

    LaunchedEffect(permissionStatus) {
        if (permissionStatus != MediaPermissionStatus.DENIED) {
            viewModel.loadInitial()
        } else {
            requestPermission()
        }
    }

    LaunchedEffect(state.lastRecoveredCount) {
        state.lastRecoveredCount?.let { count ->
            snackbarHostState.showSnackbar(successMessage.replace("%1\$d", count.toString()))
            viewModel.consumeRecoveredEvent()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Surface,
        // Scaffold's default contentWindowInsets already reserves space for the status bar,
        // which would double-pad against the Column's own statusBarsPadding() below — disable
        // it here (navigationBarsPadding further down still handles the bottom inset).
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).background(Surface)) {
            when (permissionStatus) {
                MediaPermissionStatus.DENIED -> PermissionRequiredState(onOpenSettings = { openAppSettings(context) })
                MediaPermissionStatus.LOADING -> {}
                else -> PhotoRecoveryContent(state, viewModel, permissionStatus == MediaPermissionStatus.LIMITED)
            }
        }
    }
}

@Composable
private fun PhotoRecoveryContent(
    state: com.mobile.photo.recovery.io.ui.vm.PhotoRecoveryUiState,
    viewModel: PhotoRecoveryViewModel,
    isLimitedAccess: Boolean = false
) {
    val photosCount = state.items.count { !it.isVideo }
    val videosCount = state.items.count { it.isVideo }
    val totalBytes = state.items.sumOf { it.size }
    val allSelected = state.items.isNotEmpty() && state.selectedIds.size == state.items.size
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    val onBack = rememberBackAction()

    fun toggleSelectAll() {
        state.items.forEach { item ->
            val isSelected = item.id in state.selectedIds
            if (allSelected == isSelected) viewModel.toggleSelected(item.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            if (isLimitedAccess) LimitedAccessBanner()

            ScreenSubHeader(
                title = stringResource(R.string.photo_recovery_title),
                caption = stringResource(R.string.photo_recovery_caption),
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 20.dp),
                trailing = {
                    GhostCircleButton(Icons.Filled.Tune, contentDescription = null, onClick = {})
                }
            )

            // Top status banner card.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, OutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        StatusPill(stringResource(R.string.photo_recovery_scan_complete))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.photo_recovery_found, state.items.size),
                            style = MaterialTheme.typography.headlineMedium,
                            color = OnSurface
                        )
                        Text(
                            stringResource(R.string.photo_recovery_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    StatusPill(
                        text = formatBytes(totalBytes),
                        leadingIcon = Icons.Filled.DataUsage,
                        container = SecondaryContainer.copy(alpha = 0.4f),
                        contentColor = Secondary
                    )
                }

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagnosticStatTile(
                        icon = Icons.Filled.PhotoLibrary,
                        caption = stringResource(R.string.photo_recovery_stat_photos),
                        value = "$photosCount",
                        unit = stringResource(R.string.photo_recovery_stat_total),
                        accent = DangerRed,
                        container = ErrorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    )
                    DiagnosticStatTile(
                        icon = Icons.Filled.Videocam,
                        caption = stringResource(R.string.photo_recovery_stat_videos),
                        value = "$videosCount",
                        unit = stringResource(R.string.photo_recovery_stat_total),
                        accent = Primary,
                        container = SurfaceContainer,
                        modifier = Modifier.weight(1f)
                    )
                    DiagnosticStatTile(
                        icon = Icons.Filled.Restore,
                        caption = stringResource(R.string.photo_recovery_stat_recover),
                        value = formatBytes(
                            state.items.filter { it.id in state.selectedIds }.sumOf { it.size }
                        ),
                        unit = stringResource(R.string.photo_recovery_stat_total),
                        accent = Secondary,
                        container = SecondaryContainer.copy(alpha = 0.4f),
                        valueColor = Secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Selection control & filter row.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer)
                            .clickable { toggleSelectAll() }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.SelectAll, contentDescription = null, tint = OnSurface, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(if (allSelected) R.string.action_deselect_all else R.string.action_select_all),
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurface
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.photo_recovery_selected_count, state.selectedIds.size, state.items.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    GhostCircleButton(Icons.Filled.FilterList, null, size = 32.dp, tint = OnSurfaceVariant, onClick = {})
                    GhostCircleButton(Icons.Filled.CalendarToday, null, size = 32.dp, tint = OnSurfaceVariant, onClick = {})
                }
            }

            if (state.items.isEmpty() && !state.isLoadingMore) {
                EmptyMediaState(stringResource(R.string.photo_recovery_empty))
            } else {
                PhotoRecoveryGrid(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f),
                    onView = { index -> viewerIndex = index }
                )
            }

            NativeAdSlot(
                placement = AdPlacement.NATIVE_SESSIONS,
                layout = NativeAdLayout.Compact,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Fixed floating bottom recovery deck.
        FloatingGlassDeck(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    stringResource(R.string.photo_recovery_ready).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
                Text(
                    "${state.selectedIds.size}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurface
                )
            }
            PrimaryPillButton(
                text = stringResource(R.string.photo_recovery_recover_button, state.selectedIds.size),
                enabled = state.selectedIds.isNotEmpty() && !state.isRecovering,
                containerColor = Secondary,
                height = 56.dp,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.recoverSelected() }
            )
        }
    }

    viewerIndex?.let { index ->
        PhotoViewerDialog(
            items = state.items,
            startIndex = index,
            selectedIds = state.selectedIds,
            onToggleSelect = { viewModel.toggleSelected(it) },
            onDismiss = { viewerIndex = null }
        )
    }
}

@Composable
private fun PhotoRecoveryGrid(
    state: com.mobile.photo.recovery.io.ui.vm.PhotoRecoveryUiState,
    viewModel: PhotoRecoveryViewModel,
    modifier: Modifier,
    onView: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        // Bottom padding clears the floating recovery deck.
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 110.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        itemsIndexed(state.items, key = { _, it -> it.id }) { index, item ->
            PhotoGridCell(
                item = item,
                isSelected = item.id in state.selectedIds,
                isRestored = item.id in state.restoredIds,
                onToggleSelect = { viewModel.toggleSelected(item.id) },
                onView = { onView(index) }
            )
        }
        if (state.isLoadingMore) {
            item(span = { GridItemSpan(3) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        } else if (!state.endReached) {
            item(span = { GridItemSpan(3) }) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .shadow(4.dp, CircleShape, clip = false)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLowest)
                            .clickable { viewModel.loadMore() }
                            .padding(start = 6.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(TertiaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Movie,
                                contentDescription = null,
                                tint = OnTertiaryFixed,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.photo_recovery_load_more, PHOTO_RECOVERY_PAGE_SIZE),
                            style = MaterialTheme.typography.labelLarge,
                            color = OnSurface
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TertiaryFixed)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                stringResource(R.string.label_ad_badge),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = OnTertiaryFixed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoGridCell(
    item: MediaItem,
    isSelected: Boolean,
    isRestored: Boolean,
    onToggleSelect: () -> Unit,
    onView: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerHigh)
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clickable { onView() }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        // Source badge (media type stands in for the mockup's Trash/Cache origin tag).
        Text(
            if (item.isVideo) "MP4" else "IMG",
            color = InverseOnSurface,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(50))
                .background(if (item.isVideo) Primary.copy(alpha = 0.8f) else InverseSurface.copy(alpha = 0.8f))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        )

        // Selection tick — green when selected, per the design system's selection control.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) Secondary else SurfaceContainerLowest.copy(alpha = 0.8f))
                .clickable { onToggleSelect() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Box(modifier = Modifier.size(10.dp).background(OutlineVariant, CircleShape))
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.isVideo) {
                    Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(2.dp))
                }
                Text(formatBytes(item.size), color = Color.White, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
            Text(
                formatRelativeDate(item.dateAddedMs),
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }

        if (isRestored) {
            Text(
                stringResource(R.string.photo_recovery_badge_restored),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = (-22).dp)
                    .padding(end = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Secondary)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}

/**
 * Full-screen photo/video viewer: swipe between items starting at [startIndex], real MediaStore
 * content (photo via AsyncImage, video via a real ExoPlayer/PlayerView, not a static placeholder).
 */
@Composable
private fun PhotoViewerDialog(
    items: List<MediaItem>,
    startIndex: Int,
    selectedIds: Set<Long>,
    onToggleSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(initialPage = startIndex.coerceIn(0, items.lastIndex)) { items.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val item = items[page]
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (item.isVideo) {
                        ViewerVideoPlayer(item)
                    } else {
                        AsyncImage(
                            model = item.uri,
                            contentDescription = item.displayName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Top bar: close + select toggle
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = Color.White
                    )
                }
                val current = items[pagerState.currentPage.coerceIn(0, items.lastIndex)]
                IconButton(onClick = { onToggleSelect(current.id) }) {
                    Icon(
                        if (current.id in selectedIds) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (current.id in selectedIds) Secondary else Color.White
                    )
                }
            }

            // Bottom info bar: filename, size, relative date
            val current = items[pagerState.currentPage.coerceIn(0, items.lastIndex)]
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text(current.displayName, color = Color.White, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${formatBytes(current.size)} · ${formatRelativeDate(current.dateAddedMs)}",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ViewerVideoPlayer(item: MediaItem) {
    val context = LocalContext.current
    val exoPlayer = remember(item.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(Media3Item.fromUri(item.uri))
            prepare()
            playWhenReady = true
        }
    }
    androidx.compose.runtime.DisposableEffect(item.id) {
        onDispose { exoPlayer.release() }
    }
    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
