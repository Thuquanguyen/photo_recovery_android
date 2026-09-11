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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
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
import com.mobile.photo.recovery.io.ui.components.TextOnlyButton
import com.mobile.photo.recovery.io.ui.theme.CardSurface
import com.mobile.photo.recovery.io.ui.theme.DangerRed
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.vm.ScreenshotSection
import com.mobile.photo.recovery.io.ui.vm.ScreenshotViewModel
import com.mobile.photo.recovery.io.util.MediaPermissionStatus
import com.mobile.photo.recovery.io.util.formatBytes
import com.mobile.photo.recovery.io.util.openAppSettings
import com.mobile.photo.recovery.io.util.rememberMediaPermissionState

@Composable
fun ScreenshotCleanerScreen(viewModel: ScreenshotViewModel = viewModel()) {
    val (permissionStatus, requestPermission) = rememberMediaPermissionState()
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(permissionStatus) {
        if (permissionStatus != MediaPermissionStatus.DENIED) viewModel.loadInitial() else requestPermission()
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val pending = state.pendingDeleteItems ?: return@rememberLauncherForActivityResult
        if (result.resultCode == Activity.RESULT_OK) viewModel.onDeleteResolved(pending) else viewModel.cancelPendingDelete()
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
        state.items.isEmpty() && !state.isLoading -> EmptyMediaState(stringResource(R.string.screenshot_empty))
        else -> ScreenshotContent(state, viewModel, permissionStatus == MediaPermissionStatus.LIMITED)
    }
}

@Composable
private fun ScreenshotContent(
    state: com.mobile.photo.recovery.io.ui.vm.ScreenshotUiState,
    viewModel: ScreenshotViewModel,
    isLimitedAccess: Boolean = false
) {
    val grouped = state.items.groupBy { viewModel.sectionOf(it) }
    val totalBytes = state.items.sumOf { it.size }
    val selectedBytes = state.items.filter { it.id in state.selectedIds }.sumOf { it.size }
    val allSelected = state.items.isNotEmpty() && state.selectedIds.size == state.items.size

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        if (isLimitedAccess) LimitedAccessBanner()
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(stringResource(R.string.screenshot_title), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        stringResource(R.string.screenshot_smart_audit),
                        style = MaterialTheme.typography.labelLarge,
                        color = Secondary
                    )
                }
                if (state.items.isNotEmpty()) {
                    TextOnlyButton(
                        text = stringResource(if (allSelected) R.string.action_deselect_all else R.string.action_select_all),
                        onClick = {
                            state.items.forEach { item ->
                                val isSelected = item.id in state.selectedIds
                                if (allSelected && isSelected) viewModel.toggleSelected(item.id)
                                if (!allSelected && !isSelected) viewModel.toggleSelected(item.id)
                            }
                        }
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text(formatBytes(totalBytes), style = MaterialTheme.typography.headlineSmall)
                        Text(
                            stringResource(R.string.screenshot_storage_hint, state.items.size),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Secondary.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = Secondary)
                    }
                }
                LinearProgressIndicator(
                    progress = { if (totalBytes > 0) (selectedBytes.toFloat() / totalBytes.toFloat()) else 0f },
                    color = Secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Text(
                    stringResource(R.string.screenshot_section_summary, state.selectedIds.size, formatBytes(selectedBytes)),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            listOf(
                ScreenshotSection.TODAY to R.string.screenshot_today,
                ScreenshotSection.YESTERDAY to R.string.screenshot_yesterday,
                ScreenshotSection.OLDER to R.string.screenshot_older
            ).forEach { (section, titleRes) ->
                val sectionItems = grouped[section].orEmpty()
                if (sectionItems.isNotEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(
                                    R.string.screenshot_section_summary,
                                    sectionItems.size,
                                    formatBytes(sectionItems.sumOf { it.size })
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    items(sectionItems, key = { it.id }) { item ->
                        ScreenshotCell(item, item.id in state.selectedIds) { viewModel.toggleSelected(item.id) }
                    }
                }
            }
            if (!state.endReached) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                    OutlinedPillButton(
                        text = stringResource(R.string.screenshot_load_more),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        onClick = { viewModel.loadMore() }
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            PrimaryPillButton(
                text = stringResource(R.string.screenshot_delete_button, state.selectedIds.size),
                enabled = state.selectedIds.isNotEmpty(),
                containerColor = DangerRed,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.requestDeleteSelected() }
            )
        }
    }
}

@Composable
private fun ScreenshotCell(item: MediaItem, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(22.dp)
                .background(if (isSelected) Secondary else Color.Black.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
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
