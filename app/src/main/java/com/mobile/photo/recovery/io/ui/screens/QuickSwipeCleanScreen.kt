package com.mobile.photo.recovery.io.ui.screens

import android.app.Activity
import android.content.IntentSender
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.data.MediaItem
import com.mobile.photo.recovery.io.data.MediaTypeFilter
import com.mobile.photo.recovery.io.ui.components.EmptyMediaState
import com.mobile.photo.recovery.io.ui.components.LimitedAccessBanner
import com.mobile.photo.recovery.io.ui.components.PermissionRequiredState
import com.mobile.photo.recovery.io.ui.components.StatChip
import com.mobile.photo.recovery.io.ui.theme.DangerRed
import com.mobile.photo.recovery.io.ui.theme.LavenderBackground
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.vm.PendingDeleteRequest
import com.mobile.photo.recovery.io.ui.vm.QuickCleanViewModel
import com.mobile.photo.recovery.io.ui.vm.SWIPE_COOLDOWN_MS
import com.mobile.photo.recovery.io.util.MediaPermissionStatus
import com.mobile.photo.recovery.io.util.formatBytes
import com.mobile.photo.recovery.io.util.openAppSettings
import com.mobile.photo.recovery.io.util.rememberMediaPermissionState
import kotlin.math.roundToInt

@Composable
fun QuickSwipeCleanScreen(viewModel: QuickCleanViewModel = viewModel()) {
    val (permissionStatus, requestPermission) = rememberMediaPermissionState()
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(permissionStatus) {
        if (permissionStatus != MediaPermissionStatus.DENIED) viewModel.loadInitial() else requestPermission()
    }

    val deleteRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val pending = state.pendingDelete
        val confirmed = result.resultCode == Activity.RESULT_OK
        when (pending) {
            is PendingDeleteRequest.OriginalDelete -> {
                if (confirmed) {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.onDeleteConfirmed(pending.item.size)
                } else {
                    viewModel.onDeleteCancelled()
                }
            }
            is PendingDeleteRequest.VaultRollback -> {
                if (confirmed) {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.onProtectDeleteConfirmed()
                } else {
                    viewModel.onProtectDeleteCancelled()
                }
            }
            null -> {}
        }
    }

    // Whenever a delete is pending, trigger the system confirmation (Android 11+) or delete directly (older).
    LaunchedEffect(state.pendingDelete) {
        val pending = state.pendingDelete ?: return@LaunchedEffect
        val uri = when (pending) {
            is PendingDeleteRequest.OriginalDelete -> pending.item.uri
            is PendingDeleteRequest.VaultRollback -> pending.item.uri
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = viewModel.createDeleteRequest(uri)
            if (pendingIntent != null) {
                deleteRequestLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            }
        } else {
            val ok = viewModel.deleteUriDirect(uri)
            when (pending) {
                is PendingDeleteRequest.OriginalDelete ->
                    if (ok) viewModel.onDeleteConfirmed(pending.item.size) else viewModel.onDeleteCancelled()
                is PendingDeleteRequest.VaultRollback ->
                    if (ok) viewModel.onProtectDeleteConfirmed() else viewModel.onProtectDeleteCancelled()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        if (permissionStatus == MediaPermissionStatus.LIMITED) LimitedAccessBanner()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LavenderBackground)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val totalCount = state.reviewedCount + (state.queue.size - state.currentIndex)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.quick_clean_stat_reviewed, state.reviewedCount, totalCount),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        formatBytes(state.bytesFreed),
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (totalCount > 0) state.reviewedCount / totalCount.toFloat() else 0f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Primary,
                    trackColor = Primary.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip(stringResource(R.string.quick_clean_stat_protected), "${state.protectedCount}")
                    FilterChip(
                        selected = state.filter == MediaTypeFilter.ALL,
                        onClick = { viewModel.setFilter(MediaTypeFilter.ALL) },
                        label = { Text(stringResource(R.string.quick_clean_filter_all)) }
                    )
                    FilterChip(
                        selected = state.filter == MediaTypeFilter.IMAGES,
                        onClick = { viewModel.setFilter(MediaTypeFilter.IMAGES) },
                        label = { Text(stringResource(R.string.quick_clean_filter_photos)) }
                    )
                    FilterChip(
                        selected = state.filter == MediaTypeFilter.VIDEOS,
                        onClick = { viewModel.setFilter(MediaTypeFilter.VIDEOS) },
                        label = { Text(stringResource(R.string.quick_clean_filter_videos)) }
                    )
                }
            }
        }

        when {
            permissionStatus == MediaPermissionStatus.DENIED ->
                PermissionRequiredState(onOpenSettings = { openAppSettings(context) })
            state.currentIndex >= state.queue.size && state.endReached ->
                EmptyMediaState(stringResource(R.string.quick_clean_done))
            else -> {
                Box(modifier = Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    val item = viewModel.currentItem()
                    if (item != null) {
                        SwipeCard(
                            item = item,
                            onSwipeLeft = { viewModel.requestDelete() },
                            onSwipeRight = { viewModel.protectToVault() },
                            onSwipeUp = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                viewModel.skip()
                            },
                            onSwipeDown = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                viewModel.previous()
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { viewModel.requestDelete() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = DangerRed.copy(alpha = 0.15f),
                            contentColor = DangerRed
                        ),
                        modifier = Modifier.size(52.dp)
                    ) { Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.quick_clean_action_delete)) }

                    FilledIconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            viewModel.previous()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.size(44.dp)
                    ) { Icon(Icons.Filled.Undo, contentDescription = stringResource(R.string.quick_clean_action_previous)) }

                    FilledIconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            viewModel.skip()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Primary.copy(alpha = 0.15f),
                            contentColor = Primary
                        ),
                        modifier = Modifier.size(44.dp)
                    ) { Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.quick_clean_action_next)) }

                    FilledIconButton(
                        onClick = { viewModel.protectToVault() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Secondary.copy(alpha = 0.15f),
                            contentColor = Secondary
                        ),
                        modifier = Modifier.size(52.dp)
                    ) { Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.quick_clean_action_vault)) }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LavenderBackground)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(stringResource(R.string.quick_clean_banner_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.quick_clean_banner_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeCard(
    item: MediaItem,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit
) {
    var offsetX by remember(item.id) { mutableFloatStateOf(0f) }
    var offsetY by remember(item.id) { mutableFloatStateOf(0f) }
    var lastSwipeTime by remember { mutableStateOf(0L) }
    val threshold = 300f

    fun resolve(dx: Float, dy: Float) {
        val now = System.currentTimeMillis()
        if (now - lastSwipeTime < SWIPE_COOLDOWN_MS) return
        when {
            dx < -threshold -> { lastSwipeTime = now; onSwipeLeft() }
            dx > threshold -> { lastSwipeTime = now; onSwipeRight() }
            dy < -threshold -> { lastSwipeTime = now; onSwipeUp() }
            dy > threshold -> { lastSwipeTime = now; onSwipeDown() }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .graphicsLayer {
                translationX = offsetX
                translationY = offsetY
                rotationZ = (offsetX / 40).coerceIn(-12f, 12f)
            }
            .pointerInput(item.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    },
                    onDragEnd = {
                        resolve(offsetX, offsetY)
                        offsetX = 0f
                        offsetY = 0f
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.isVideo) {
                VideoPreview(item)
            } else {
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (item.isVideo) {
                Icon(
                    Icons.Filled.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .padding(6.dp)
                        .size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        item.displayName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        formatBytes(item.size),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoPreview(item: MediaItem) {
    val context = LocalContext.current
    val exoPlayer = remember(item.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(Media3Item.fromUri(item.uri))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(item.id) {
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
