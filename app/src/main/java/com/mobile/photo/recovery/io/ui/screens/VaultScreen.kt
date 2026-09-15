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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.data.VaultItem
import com.mobile.photo.recovery.io.ui.components.EmptyMediaState
import com.mobile.photo.recovery.io.ui.components.ScreenSubHeader
import com.mobile.photo.recovery.io.ui.components.rememberBackAction
import com.mobile.photo.recovery.io.ui.theme.DangerRed
import com.mobile.photo.recovery.io.ui.theme.InverseSurface
import com.mobile.photo.recovery.io.ui.theme.OnSurface
import com.mobile.photo.recovery.io.ui.theme.OutlineVariant
import com.mobile.photo.recovery.io.ui.theme.Secondary
import com.mobile.photo.recovery.io.ui.theme.Surface
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainer
import com.mobile.photo.recovery.io.ui.theme.SurfaceContainerLowest
import com.mobile.photo.recovery.io.ui.vm.VaultEvent
import com.mobile.photo.recovery.io.ui.vm.VaultViewModel

@Composable
fun VaultScreen(viewModel: VaultViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<VaultItem?>(null) }
    val onBack = rememberBackAction()

    val restoredMsg = stringResource(R.string.vault_restore_success)
    val deletedMsg = stringResource(R.string.vault_delete_success)

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(state.lastEvent) {
        when (state.lastEvent) {
            VaultEvent.Restored -> snackbarHostState.showSnackbar(restoredMsg)
            VaultEvent.Deleted -> snackbarHostState.showSnackbar(deletedMsg)
            null -> {}
        }
        if (state.lastEvent != null) viewModel.consumeEvent()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Surface,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
                .padding(padding)
                .statusBarsPadding()
        ) {
            ScreenSubHeader(
                title = stringResource(R.string.vault_title),
                caption = stringResource(R.string.home_grid_vault_desc),
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            if (state.items.isEmpty()) {
                EmptyMediaState(stringResource(R.string.vault_empty))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.items, key = { it.path }) { item ->
                        VaultCell(item, onRestore = { viewModel.restore(item) }, onDeleteRequest = { pendingDelete = item })
                    }
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = SurfaceContainerLowest,
            shape = RoundedCornerShape(20.dp),
            title = { Text(stringResource(R.string.vault_delete_confirm_title)) },
            text = { Text(stringResource(R.string.vault_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePermanently(item)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete), color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun VaultCell(item: VaultItem, onRestore: () -> Unit, onDeleteRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainer)
        ) {
            if (item.isVideo) {
                // Videos show a static icon only, no real thumbnail (spec 4.9).
                Box(
                    modifier = Modifier.fillMaxSize().background(InverseSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color.White)
                }
            } else {
                AsyncImage(
                    model = java.io.File(item.path),
                    contentDescription = item.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Text(
            item.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurface,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VaultAction(Icons.Filled.Restore, stringResource(R.string.vault_restore), Secondary, Modifier.weight(1f), onRestore)
            VaultAction(Icons.Filled.Delete, stringResource(R.string.vault_delete), DangerRed, Modifier.weight(1f), onDeleteRequest)
        }
    }
}

@Composable
private fun VaultAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint, maxLines = 1)
    }
}
