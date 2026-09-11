package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.data.MediaItem
import com.mobile.photo.recovery.io.ui.components.EmptyMediaState
import com.mobile.photo.recovery.io.ui.components.LimitedAccessBanner
import com.mobile.photo.recovery.io.ui.components.PermissionRequiredState
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.components.StatChip
import com.mobile.photo.recovery.io.ui.theme.LavenderBackground
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.vm.PhotoRecoveryViewModel
import com.mobile.photo.recovery.io.util.MediaPermissionStatus
import com.mobile.photo.recovery.io.util.formatBytes
import com.mobile.photo.recovery.io.util.formatRelativeDate
import com.mobile.photo.recovery.io.util.openAppSettings
import com.mobile.photo.recovery.io.util.rememberMediaPermissionState
import kotlinx.coroutines.launch

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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(modifier = Modifier.padding(padding)) {
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
    var viewerIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLimitedAccess) LimitedAccessBanner()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LavenderBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            stringResource(R.string.photo_recovery_banner_count, state.items.size),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            stringResource(R.string.photo_recovery_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.padding(top = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip(stringResource(R.string.photo_recovery_stat_photos), "$photosCount")
                    StatChip(stringResource(R.string.photo_recovery_stat_videos), "$videosCount")
                    StatChip(stringResource(R.string.photo_recovery_stat_restored), "${state.restoredIds.size}")
                }
            }
        }

        if (state.items.isEmpty() && !state.isLoadingMore) {
            EmptyMediaState(stringResource(R.string.photo_recovery_empty))
        } else {
            LazyVerticalGridWithLoadMore(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.weight(1f),
                onView = { index -> viewerIndex = index }
            )
        }

        Column(modifier = Modifier.padding(20.dp)) {
            PrimaryPillButton(
                text = stringResource(R.string.photo_recovery_recover_button, state.selectedIds.size),
                enabled = state.selectedIds.isNotEmpty() && !state.isRecovering,
                modifier = Modifier.fillMaxWidth(),
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
private fun LazyVerticalGridWithLoadMore(
    state: com.mobile.photo.recovery.io.ui.vm.PhotoRecoveryUiState,
    viewModel: PhotoRecoveryViewModel,
    modifier: Modifier,
    onView: (Int) -> Unit
) {
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(12.dp),
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
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        } else if (!state.endReached) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                com.mobile.photo.recovery.io.ui.components.OutlinedPillButton(
                    text = stringResource(R.string.photo_recovery_load_more),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    onClick = { viewModel.loadMore() }
                )
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
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable { onView() }
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                )
                .padding(4.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.isVideo) {
                    Icon(
                        Icons.Filled.Videocam,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                }
                Text(
                    formatBytes(item.size),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                formatRelativeDate(item.dateAddedMs),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall
            )
          }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .background(if (isSelected) Primary else Color.Black.copy(alpha = 0.35f), CircleShape)
                .clickable { onToggleSelect() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        if (isRestored) {
            Text(
                stringResource(R.string.photo_recovery_badge_restored),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Secondary, RoundedCornerShape(topStart = 8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = Color.White)
                }
                val current = items[pagerState.currentPage.coerceIn(0, items.lastIndex)]
                IconButton(onClick = { onToggleSelect(current.id) }) {
                    Icon(
                        if (current.id in selectedIds) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (current.id in selectedIds) Primary else Color.White
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
