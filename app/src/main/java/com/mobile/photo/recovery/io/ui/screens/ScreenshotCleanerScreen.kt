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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.data.MediaItem
import com.mobile.photo.recovery.io.ui.components.EmptyMediaState
import com.mobile.photo.recovery.io.ui.components.FilterPill
import com.mobile.photo.recovery.io.ui.components.GhostCircleButton
import com.mobile.photo.recovery.io.ui.components.LimitedAccessBanner
import com.mobile.photo.recovery.io.ui.components.PermissionRequiredState
import com.mobile.photo.recovery.io.ui.components.PrimaryPillButton
import com.mobile.photo.recovery.io.ui.components.rememberBackAction
import com.mobile.photo.recovery.io.ui.theme.DangerRed
import com.mobile.photo.recovery.io.ui.theme.OnSecondaryContainer
import com.mobile.photo.recovery.io.ui.theme.OnSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurfaceVariant
import com.mobile.photo.recovery.io.ui.theme.OutlineVariant
import com.mobile.photo.recovery.io.ui.theme.Primary
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.SecondaryContainer
import com.mobile.photo.recovery.io.ui.theme.Surface
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainer
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerHigh
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLow
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLowest
import com.mobile.photo.recovery.io.ui.vm.ScreenshotSection
import com.mobile.photo.recovery.io.ui.vm.ScreenshotViewModel
import com.mobile.photo.recovery.io.util.MediaPermissionStatus
import com.mobile.photo.recovery.io.util.formatBytes
import com.mobile.photo.recovery.io.util.formatRelativeDate
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

    if (permissionStatus == MediaPermissionStatus.DENIED) {
        PermissionRequiredState(onOpenSettings = { openAppSettings(context) })
    } else {
        ScreenshotContent(state, viewModel, permissionStatus == MediaPermissionStatus.LIMITED)
    }
}

/** Date buckets offered by the mockup's filter toolbar. */
private enum class ScreenshotDateFilter { ALL, TODAY, MONTH }

@Composable
private fun ScreenshotContent(
    state: com.mobile.photo.recovery.io.ui.vm.ScreenshotUiState,
    viewModel: ScreenshotViewModel,
    isLimitedAccess: Boolean = false
) {
    var dateFilter by remember { mutableStateOf(ScreenshotDateFilter.ALL) }
    val onBack = rememberBackAction()

    val visibleItems = when (dateFilter) {
        ScreenshotDateFilter.ALL -> state.items
        ScreenshotDateFilter.TODAY -> state.items.filter { viewModel.sectionOf(it) == ScreenshotSection.TODAY }
        // "This Month" means the current calendar month, not just "today or yesterday" (that
        // was the bug — it reused the TODAY/YESTERDAY threshold and hid anything older).
        ScreenshotDateFilter.MONTH -> {
            val now = java.util.Calendar.getInstance()
            val currentYear = now.get(java.util.Calendar.YEAR)
            val currentMonth = now.get(java.util.Calendar.MONTH)
            state.items.filter { item ->
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = item.dateAddedMs }
                cal.get(java.util.Calendar.YEAR) == currentYear && cal.get(java.util.Calendar.MONTH) == currentMonth
            }
        }
    }
    val grouped = visibleItems.groupBy { viewModel.sectionOf(it) }
    val totalBytes = state.items.sumOf { it.size }
    val selectedItems = state.items.filter { it.id in state.selectedIds }
    val selectedBytes = selectedItems.sumOf { it.size }
    val allSelected = state.items.isNotEmpty() && state.selectedIds.size == state.items.size

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            if (isLimitedAccess) LimitedAccessBanner()

            // Sub-header: back + title/status on the start side, select-all action on the end.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    GhostCircleButton(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, onClick = onBack)
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            stringResource(R.string.screenshot_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(Secondary, CircleShape))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.screenshot_smart_audit).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Secondary
                            )
                        }
                    }
                }
                if (state.items.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer)
                            .clickable {
                                state.items.forEach { item ->
                                    val isSelected = item.id in state.selectedIds
                                    if (allSelected == isSelected) viewModel.toggleSelected(item.id)
                                }
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.DoneAll, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(if (allSelected) R.string.action_deselect_all else R.string.action_select_all),
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary
                        )
                    }
                }
            }

            // Hero storage stats banner.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
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
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SecondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.AutoFixHigh,
                            contentDescription = null,
                            tint = OnSecondaryContainer,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.screenshot_likely_analysis),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSecondaryContainer
                        )
                    }
                    Text(
                        stringResource(R.string.screenshot_ready_to_purge),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(formatBytes(totalBytes), style = MaterialTheme.typography.headlineLarge, color = OnSurface)
                        Text(
                            stringResource(R.string.screenshot_storage_hint, state.items.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Secondary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = Secondary, modifier = Modifier.size(26.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (totalBytes > 0) selectedBytes.toFloat() / totalBytes.toFloat() else 0f },
                    color = Secondary,
                    trackColor = SurfaceContainer,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.screenshot_selected_stat, selectedItems.size, formatBytes(selectedBytes)),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        stringResource(
                            R.string.screenshot_unselected_stat,
                            state.items.size - selectedItems.size,
                            formatBytes(totalBytes - selectedBytes)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Filter & controls toolbar.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill(
                        stringResource(R.string.screenshot_filter_all, state.items.size),
                        dateFilter == ScreenshotDateFilter.ALL
                    ) { dateFilter = ScreenshotDateFilter.ALL }
                    FilterPill(
                        stringResource(R.string.screenshot_filter_today),
                        dateFilter == ScreenshotDateFilter.TODAY
                    ) { dateFilter = ScreenshotDateFilter.TODAY }
                    FilterPill(
                        stringResource(R.string.screenshot_filter_month),
                        dateFilter == ScreenshotDateFilter.MONTH
                    ) { dateFilter = ScreenshotDateFilter.MONTH }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    GhostCircleButton(Icons.Filled.SwapVert, null, size = 32.dp, tint = OnSurfaceVariant, onClick = {})
                    GhostCircleButton(Icons.Filled.GridView, null, size = 32.dp, tint = OnSurfaceVariant, onClick = {})
                }
            }

            if (state.items.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmptyMediaState(stringResource(R.string.screenshot_empty))
                }
            } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                listOf(
                    ScreenshotSection.TODAY to R.string.screenshot_today,
                    ScreenshotSection.YESTERDAY to R.string.screenshot_yesterday,
                    ScreenshotSection.OLDER to R.string.screenshot_older
                ).forEach { (section, titleRes) ->
                    val sectionItems = grouped[section].orEmpty()
                    if (sectionItems.isNotEmpty()) {
                        item(span = { GridItemSpan(3) }) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    stringResource(titleRes),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = OnSurface
                                )
                                Text(
                                    stringResource(
                                        R.string.screenshot_section_summary,
                                        sectionItems.size,
                                        formatBytes(sectionItems.sumOf { it.size })
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        items(sectionItems, key = { it.id }) { item ->
                            ScreenshotCell(item, item.id in state.selectedIds) { viewModel.toggleSelected(item.id) }
                        }
                    }
                }
                if (!state.endReached) {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .height(44.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh)
                                    .clickable { viewModel.loadMore() }
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.screenshot_load_more),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }
            }
            }
        }

        // Sticky bottom deletion trigger bar.
        PrimaryPillButton(
            text = stringResource(R.string.screenshot_delete_button, state.selectedIds.size),
            enabled = state.selectedIds.isNotEmpty(),
            containerColor = DangerRed,
            leadingIcon = Icons.Filled.DeleteSweep,
            height = 56.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            onClick = { viewModel.requestDeleteSelected() }
        )
    }
}

@Composable
private fun ScreenshotCell(item: MediaItem, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, OutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(48.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) Secondary else Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatBytes(item.size),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                formatRelativeDate(item.dateAddedMs),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
