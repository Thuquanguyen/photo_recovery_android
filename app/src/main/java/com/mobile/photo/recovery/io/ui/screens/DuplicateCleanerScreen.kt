package com.mobile.photo.recovery.io.ui.screens

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.mobile.photo.recovery.io.ui.components.LimitedAccessBanner
import com.mobile.photo.recovery.io.ui.components.OutlinedPillButton
import com.mobile.photo.recovery.io.ui.components.PermissionRequiredState
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.theme.CardSurface
import com.mobile.photo.recovery.io.ui.theme.DangerRed
import com.mobile.photo.recovery.io.ui.theme.LavenderBackground
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
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

    when {
        permissionStatus == MediaPermissionStatus.DENIED -> PermissionRequiredState(onOpenSettings = { openAppSettings(context) })
        state.groups.isEmpty() && !state.isScanning -> EmptyMediaState(stringResource(R.string.duplicate_empty))
        else -> DuplicateContent(state, viewModel, permissionStatus == MediaPermissionStatus.LIMITED)
    }
}

@Composable
private fun DuplicateContent(
    state: com.mobile.photo.recovery.io.ui.vm.DuplicateUiState,
    viewModel: DuplicateViewModel,
    isLimitedAccess: Boolean = false
) {
    val totalDuplicates = state.groups.sumOf { it.duplicates.size }
    val selectedBytes = viewModel.selectedSizeBytes()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        if (isLimitedAccess) LimitedAccessBanner()
        Column(modifier = Modifier.padding(20.dp)) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LavenderBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.duplicate_title), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        stringResource(R.string.duplicate_groups_found, state.groups.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary
                    )
                    Text(
                        stringResource(R.string.duplicate_scanned_summary, state.scannedCount, formatBytes(selectedBytes)),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                        Text(
                            stringResource(R.string.duplicate_badge_verified),
                            style = MaterialTheme.typography.labelLarge,
                            color = Secondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    if (state.isScanning) {
                        Text(stringResource(R.string.duplicate_scanning), modifier = Modifier.padding(top = 8.dp))
                        LinearProgressIndicator(
                            progress = { state.scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    } else if (state.groups.isNotEmpty()) {
                        // Cosmetic static "100% Analysis Done" bar — not tied to any real percentage (spec 4.7).
                        Text(stringResource(R.string.duplicate_analysis_done), modifier = Modifier.padding(top = 8.dp))
                        LinearProgressIndicator(
                            progress = { 1f },
                            color = Secondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            itemsIndexed(state.groups, key = { _, group -> group.hash }) { index, group ->
                DuplicateGroupRow(index + 1, group, state.selectedIds, viewModel::toggleSelected)
                androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            }
            if (!state.endReached) {
                item {
                    OutlinedPillButton(
                        text = stringResource(R.string.duplicate_scan_more),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.scanMore() }
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            PrimaryPillButton(
                text = stringResource(R.string.duplicate_delete_button, state.selectedIds.size, formatBytes(selectedBytes)),
                enabled = state.selectedIds.isNotEmpty(),
                containerColor = DangerRed,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.deleteSelected() }
            )
        }
    }
}

@Composable
private fun DuplicateGroupRow(
    index: Int,
    group: DuplicateGroup,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit
) {
    val allItems = listOf(group.original) + group.duplicates
    val totalBytes = allItems.sumOf { it.size }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(index.toString(), color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(group.original.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.duplicate_group_header, allItems.size, formatBytes(totalBytes)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
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
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(if (!isOriginal) Modifier.clickable(onClick = onClick) else Modifier)
        )
        if (isOriginal) {
            Text(
                stringResource(R.string.duplicate_original_label),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(Secondary, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .background(if (isSelected) DangerRed else Color.Black.copy(alpha = 0.35f), CircleShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            formatBytes(item.size),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}
