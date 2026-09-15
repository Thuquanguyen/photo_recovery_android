package com.mobile.photo.recovery.io.ui.screens

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.mobile.photo.recovery.io.ui.components.GhostCircleButton
import com.mobile.photo.recovery.io.ui.components.LimitedAccessBanner
import com.mobile.photo.recovery.io.ui.components.PermissionRequiredState
import com.mobile.photo.recovery.io.ui.components.ScreenSubHeader
import com.mobile.photo.recovery.io.ui.components.StatusPill
import com.mobile.photo.recovery.io.ui.components.rememberBackAction
import com.mobile.photo.recovery.io.ui.theme.DangerRed
import com.mobile.photo.recovery.io.ui.theme.ErrorContainer
import com.mobile.photo.recovery.io.ui.theme.InverseSurface
import com.mobile.photo.recovery.io.ui.theme.OnErrorContainer
import com.mobile.photo.recovery.io.ui.theme.OnPrimaryFixed
import com.mobile.photo.recovery.io.ui.theme.OnSecondaryContainer
import com.mobile.photo.recovery.io.ui.theme.OnSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurfaceVariant
import com.mobile.photo.recovery.io.ui.theme.OnTertiaryFixed
import com.mobile.photo.recovery.io.ui.theme.OutlineVariant
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.PrimaryFixed
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.SecondaryContainer
import com.mobile.photo.recovery.io.ui.theme.Surface
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainer
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerHigh
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerHighest
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLow
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLowest
import com.mobile.photo.recovery.io.ui.theme.Tertiary
import com.mobile.photo.recovery.io.ui.theme.TertiaryFixed
import com.mobile.photo.recovery.io.ui.vm.PendingDeleteRequest
import com.mobile.photo.recovery.io.ui.vm.QuickCleanViewModel
import com.mobile.photo.recovery.io.ui.vm.SWIPE_COOLDOWN_MS
import com.mobile.photo.recovery.io.util.MediaPermissionStatus
import com.mobile.photo.recovery.io.util.formatBytes
import com.mobile.photo.recovery.io.util.openAppSettings
import com.mobile.photo.recovery.io.util.rememberMediaPermissionState

@Composable
fun QuickSwipeCleanScreen(viewModel: QuickCleanViewModel = viewModel()) {
    val (permissionStatus, requestPermission) = rememberMediaPermissionState()
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    val onBack = rememberBackAction()

    LaunchedEffect(permissionStatus) {
        if (permissionStatus != MediaPermissionStatus.DENIED) viewModel.loadInitial() else requestPermission()
    }

    val deleteRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val pending = state.pendingDelete
        val confirmed = result.resultCode == Activity.RESULT_OK
        when (pending) {
            is PendingDeleteRequest.BatchDelete -> {
                if (confirmed) {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.onDeleteConfirmed(pending.items.sumOf { it.size })
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

    // Whenever a delete is pending, trigger ONE system confirmation covering every queued item
    // (Android 11+) or delete directly (older) — not one dialog per swipe.
    LaunchedEffect(state.pendingDelete) {
        val pending = state.pendingDelete ?: return@LaunchedEffect
        val uris = when (pending) {
            is PendingDeleteRequest.BatchDelete -> pending.items.map { it.uri }
            is PendingDeleteRequest.VaultRollback -> listOf(pending.item.uri)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = viewModel.createDeleteRequest(uris)
            if (pendingIntent != null) {
                deleteRequestLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            }
        } else {
            when (pending) {
                is PendingDeleteRequest.BatchDelete -> {
                    val ok = viewModel.deleteUrisDirect(uris)
                    if (ok) viewModel.onDeleteConfirmed(pending.items.sumOf { it.size }) else viewModel.onDeleteCancelled()
                }
                is PendingDeleteRequest.VaultRollback -> {
                    val ok = viewModel.deleteUriDirect(pending.item.uri)
                    if (ok) viewModel.onProtectDeleteConfirmed() else viewModel.onProtectDeleteCancelled()
                }
            }
        }
    }

    val totalCount = state.reviewedCount + (state.queue.size - state.currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        if (permissionStatus == MediaPermissionStatus.LIMITED) LimitedAccessBanner()

        ScreenSubHeader(
            title = stringResource(R.string.quick_clean_title),
            caption = stringResource(R.string.quick_clean_caption),
            onBack = onBack,
            trailing = {
                // The tune control cycles the media filter, exactly as the mockup's filter button does.
                GhostCircleButton(
                    icon = Icons.Filled.Tune,
                    contentDescription = null,
                    tint = Primary,
                    onClick = {
                        viewModel.setFilter(
                            when (state.filter) {
                                MediaTypeFilter.ALL -> MediaTypeFilter.IMAGES
                                MediaTypeFilter.IMAGES -> MediaTypeFilter.VIDEOS
                                MediaTypeFilter.VIDEOS -> MediaTypeFilter.ALL
                            }
                        )
                    }
                )
            }
        )

        // Dynamic progress & savings strip.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLowest)
                .border(1.dp, OutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(stringResource(R.string.quick_clean_queue_active))
                StatusPill(
                    text = stringResource(R.string.quick_clean_freed, formatBytes(state.bytesFreed)),
                    leadingIcon = Icons.Filled.DeleteSweep,
                    container = SecondaryContainer.copy(alpha = 0.4f),
                    contentColor = Secondary
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    stringResource(R.string.quick_clean_stat_reviewed, state.reviewedCount, totalCount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurface
                )
                Text(
                    stringResource(R.string.quick_clean_stat_protected, state.protectedCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary
                )
            }
            if (state.binQueue.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorContainer.copy(alpha = 0.5f))
                        .clickable { viewModel.confirmBinQueue() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.quick_clean_bin_queue, state.binQueue.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = DangerRed
                        )
                    }
                    Text(
                        stringResource(R.string.action_confirm),
                        style = MaterialTheme.typography.labelLarge,
                        color = DangerRed
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { if (totalCount > 0) state.reviewedCount / totalCount.toFloat() else 0f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = Primary,
                trackColor = SurfaceContainerHighest.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SwipeHintChip(
                    icon = Icons.Filled.Delete,
                    text = stringResource(R.string.quick_clean_swipe_left_short),
                    accent = DangerRed,
                    container = ErrorContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )
                SwipeHintChip(
                    icon = Icons.Filled.Lock,
                    text = stringResource(R.string.quick_clean_swipe_right_short),
                    accent = Secondary,
                    container = SecondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when {
            permissionStatus == MediaPermissionStatus.DENIED ->
                PermissionRequiredState(onOpenSettings = { openAppSettings(context) })
            state.currentIndex >= state.queue.size && state.endReached ->
                EmptyMediaState(stringResource(R.string.quick_clean_done))
            else -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background deck stacking, so the queue reads as a stack of cards.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.92f)
                            .offset(y = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerHigh.copy(alpha = 0.5f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .fillMaxHeight(0.96f)
                            .offset(y = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainer.copy(alpha = 0.8f))
                    )
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

                // Gesture cheatsheet pill.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(SurfaceContainerLow)
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GestureHint(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.quick_clean_gesture_bin), DangerRed)
                    CheatsheetDivider()
                    GestureHint(Icons.Filled.ArrowDownward, stringResource(R.string.quick_clean_gesture_prev), Tertiary)
                    CheatsheetDivider()
                    GestureHint(Icons.Filled.ArrowUpward, stringResource(R.string.quick_clean_gesture_next), Primary)
                    CheatsheetDivider()
                    GestureHint(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.quick_clean_gesture_vault), Secondary)
                }

                // 4 tactile buttons mirroring the gestures.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionCircle(
                        icon = Icons.Filled.DeleteForever,
                        description = stringResource(R.string.quick_clean_action_delete),
                        container = ErrorContainer,
                        tint = OnErrorContainer,
                        size = 52.dp
                    ) { viewModel.requestDelete() }
                    Spacer(Modifier.width(16.dp))
                    ActionCircle(
                        icon = Icons.Filled.ArrowDownward,
                        description = stringResource(R.string.quick_clean_action_previous),
                        container = TertiaryFixed,
                        tint = OnTertiaryFixed,
                        size = 44.dp
                    ) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        viewModel.previous()
                    }
                    Spacer(Modifier.width(16.dp))
                    ActionCircle(
                        icon = Icons.Filled.ArrowUpward,
                        description = stringResource(R.string.quick_clean_action_next),
                        container = PrimaryFixed,
                        tint = OnPrimaryFixed,
                        size = 44.dp
                    ) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        viewModel.skip()
                    }
                    Spacer(Modifier.width(16.dp))
                    ActionCircle(
                        icon = Icons.Filled.Lock,
                        description = stringResource(R.string.quick_clean_action_vault),
                        container = SecondaryContainer,
                        tint = OnSecondaryContainer,
                        size = 52.dp
                    ) { viewModel.protectToVault() }
                }
            }
        }

        // Safety & auto-purge reassurance banner.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow)
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    stringResource(R.string.quick_clean_banner_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurface
                )
                Text(
                    stringResource(R.string.quick_clean_banner_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SwipeHintChip(
    icon: ImageVector,
    text: String,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = accent, maxLines = 1)
    }
}

@Composable
private fun GestureHint(icon: ImageVector, label: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
private fun CheatsheetDivider() {
    Box(modifier = Modifier.height(10.dp).width(1.dp).background(OutlineVariant))
}

@Composable
private fun ActionCircle(
    icon: ImageVector,
    description: String,
    container: Color,
    tint: Color,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(size * 0.45f))
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .graphicsLayer {
                translationX = offsetX
                translationY = offsetY
                rotationZ = (offsetX / 40).coerceIn(-12f, 12f)
            }
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
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
            }
    ) {
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

        // Scrim for contrast.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            InverseSurface.copy(alpha = 0.5f),
                            InverseSurface.copy(alpha = 0.15f),
                            InverseSurface.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Top overlay: media type tag, size and a play chip for videos.
        Row(
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OverlayTag(if (item.isVideo) "VIDEO" else "PHOTO", icon = if (item.isVideo) Icons.Filled.PlayArrow else null)
                OverlayTag(formatBytes(item.size))
            }
        }

        // Bottom metadata tray.
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(12.dp)
        ) {
            Text(
                item.displayName,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.quick_clean_swipe_left_short).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = ErrorContainer
                )
                Text(
                    stringResource(R.string.quick_clean_swipe_right_short).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun OverlayTag(text: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(InverseSurface.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = SecondaryContainer, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
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
