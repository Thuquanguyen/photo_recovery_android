package com.mobile.photo.recovery.io.ui.screens

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.data.MediaItem
import com.mobile.photo.recovery.io.ui.components.EmptyMediaState
import com.mobile.photo.recovery.io.ui.components.FloatingGlassDeck
import com.mobile.photo.recovery.io.ui.components.LimitedAccessBanner
import com.mobile.photo.recovery.io.ui.components.PermissionRequiredState
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.components.rememberBackAction
import com.mobile.photo.recovery.io.ui.theme.DangerRed
import com.mobile.photo.recovery.io.ui.theme.InverseOnSurface
import com.mobile.photo.recovery.io.ui.theme.InverseSurface
import com.mobile.photo.recovery.io.ui.theme.OnPrimaryFixed
import com.mobile.photo.recovery.io.ui.theme.OnSecondaryContainer
import com.mobile.photo.recovery.io.ui.theme.OnSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurfaceVariant
import com.mobile.photo.recovery.io.ui.theme.OnTertiaryFixed
import com.mobile.photo.recovery.io.ui.theme.OutlineVariant
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.PrimaryContainer
import com.mobile.photo.recovery.io.ui.theme.PrimaryFixed
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.SecondaryContainer
import com.mobile.photo.recovery.io.ui.theme.Surface
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainer
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerHigh
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLowest
import com.mobile.photo.recovery.io.ui.theme.Tertiary
import com.mobile.photo.recovery.io.ui.theme.TertiaryFixed
import com.mobile.photo.recovery.io.ui.vm.DuplicateGroup
import com.mobile.photo.recovery.io.ui.vm.DuplicateViewModel
import com.mobile.photo.recovery.io.util.MediaPermissionStatus
import com.mobile.photo.recovery.io.util.formatBytes
import com.mobile.photo.recovery.io.util.openAppSettings
import com.mobile.photo.recovery.io.util.rememberMediaPermissionState

@Composable
fun DuplicateCleanerScreen(viewModel: DuplicateViewModel = viewModel()) {
    val (permissionStatus, requestPermission) = rememberMediaPermissionState()
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(permissionStatus) {
        if (permissionStatus != MediaPermissionStatus.DENIED) viewModel.scanInitial() else requestPermission()
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val pending = state.pendingDeleteItems ?: return@rememberLauncherForActivityResult
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onDeleteResolved(pending)
        } else {
            viewModel.cancelPendingDelete()
        }
    }

    LaunchedEffect(state.pendingDeleteItems) {
        val pending = state.pendingDeleteItems ?: return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = viewModel.createDeleteRequestFor(pending)
            if (pendingIntent != null) {
                deleteLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            } else {
                viewModel.cancelPendingDelete()
            }
        } else {
            if (viewModel.deleteDirectAll(pending)) viewModel.onDeleteResolved(pending) else viewModel.cancelPendingDelete()
        }
    }

    if (permissionStatus == MediaPermissionStatus.DENIED) {
        PermissionRequiredState(onOpenSettings = { openAppSettings(context) })
    } else {
        DuplicateContent(state, viewModel, permissionStatus == MediaPermissionStatus.LIMITED)
    }
}

@Composable
private fun DuplicateContent(
    state: com.mobile.photo.recovery.io.ui.vm.DuplicateUiState,
    viewModel: DuplicateViewModel,
    isLimitedAccess: Boolean = false
) {
    val selectedBytes = viewModel.selectedSizeBytes()
    val onBack = rememberBackAction()

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLimitedAccess) item { LimitedAccessBanner() }

            item { DuplicateHeaderCard(state, selectedBytes, onBack) }

            if (state.groups.isEmpty() && !state.isScanning) {
                item { EmptyMediaState(stringResource(R.string.duplicate_empty)) }
            } else {
                itemsIndexed(state.groups, key = { _, group -> group.hash }) { index, group ->
                    DuplicateGroupCard(index + 1, group, state.selectedIds, viewModel::toggleSelected)
                }
            }
        }

        // Sticky bottom controls deck.
        FloatingGlassDeck(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            shape = CircleShape
        ) {
            if (!state.endReached) {
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .clickable { viewModel.scanMore() }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Movie, contentDescription = null, tint = Tertiary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.duplicate_scan_more),
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurface,
                        maxLines = 1
                    )
                }
            }
            PrimaryPillButton(
                text = stringResource(R.string.duplicate_delete_button, state.selectedIds.size, formatBytes(selectedBytes)),
                enabled = state.selectedIds.isNotEmpty(),
                containerColor = DangerRed,
                leadingIcon = Icons.Filled.Delete,
                height = 48.dp,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.deleteSelected() }
            )
        }
    }
}

@Composable
private fun DuplicateHeaderCard(
    state: com.mobile.photo.recovery.io.ui.vm.DuplicateUiState,
    selectedBytes: Long,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Primary.copy(alpha = 0.15f),
                        PrimaryContainer.copy(alpha = 0.2f),
                        TertiaryFixed.copy(alpha = 0.3f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLowest.copy(alpha = 0.9f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = stringResource(R.string.action_back),
                    tint = OnSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceContainerLowest.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(Secondary, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.duplicate_groups_found, state.groups.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurface
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.duplicate_title), style = MaterialTheme.typography.headlineMedium, color = OnSurface)
        Text(
            stringResource(R.string.duplicate_scanned_summary, state.scannedCount, formatBytes(selectedBytes)),
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )

        // MD5 hash verification pill.
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLowest.copy(alpha = 0.95f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.duplicate_badge_verified),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                }
                Text(
                    stringResource(if (state.isScanning) R.string.duplicate_scanning else R.string.duplicate_analysis_done),
                    style = MaterialTheme.typography.labelMedium,
                    color = Secondary
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { if (state.isScanning) state.scanProgress else 1f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = Primary,
                trackColor = SurfaceContainer
            )
        }
    }
}

@Composable
private fun DuplicateGroupCard(
    index: Int,
    group: DuplicateGroup,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit
) {
    val allItems = listOf(group.original) + group.duplicates
    val totalBytes = allItems.sumOf { it.size }
    // Badge palette cycles violet / amber / neutral, matching the mockup's three group headers.
    val (badgeBg, badgeFg) = when (index % 3) {
        1 -> PrimaryFixed to OnPrimaryFixed
        2 -> TertiaryFixed to OnTertiaryFixed
        else -> SurfaceContainerHigh to OnSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(index.toString(), color = badgeFg, style = MaterialTheme.typography.labelMedium)
                }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        group.original.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurface,
                        maxLines = 1
                    )
                    Text(
                        stringResource(R.string.duplicate_group_header, allItems.size, formatBytes(totalBytes)),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
            Text(
                stringResource(R.string.duplicate_kept_safe, 1).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OnSecondaryContainer,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SecondaryContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DuplicateThumb(group.original, isOriginal = true, isSelected = false, onClick = {}, modifier = Modifier.weight(1f))
            group.duplicates.forEach { dup ->
                DuplicateThumb(
                    dup,
                    isOriginal = false,
                    isSelected = dup.id in selectedIds,
                    onClick = { onToggle(dup.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DuplicateThumb(
    item: MediaItem,
    isOriginal: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer)
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(if (!isOriginal) Modifier.clickable(onClick = onClick) else Modifier)
        )

        Row(
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOriginal) {
                Text(
                    stringResource(R.string.duplicate_original_label),
                    color = InverseOnSurface,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(InverseSurface.copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLowest.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = OutlineVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(Modifier.size(1.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) DangerRed else SurfaceContainerLowest.copy(alpha = 0.8f))
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else OutlineVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom metadata glass box.
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(InverseSurface.copy(alpha = 0.8f))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                item.displayName,
                color = InverseOnSurface,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            Text(
                formatBytes(item.size),
                color = InverseOnSurface.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}
