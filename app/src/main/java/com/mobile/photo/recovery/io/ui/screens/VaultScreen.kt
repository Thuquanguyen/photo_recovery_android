package com.mobile.photo.recovery.io.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobile.photo.recovery.io.R
import com.mobile.photo.recovery.io.data.VaultItem
import com.mobile.photo.recovery.io.ui.components.EmptyMediaState
import com.mobile.photo.recovery.io.ui.theme.NeutralDark
import com.mobile.photo.recovery.io.ui.vm.VaultEvent
import com.mobile.photo.recovery.io.ui.vm.VaultViewModel

@Composable
fun VaultScreen(viewModel: VaultViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<VaultItem?>(null) }

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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(com.mobile.photo.recovery.io.ui.theme.LavenderBackground)
                .padding(padding)
        ) {
            Text(
                stringResource(R.string.vault_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(20.dp)
            )
            if (state.items.isEmpty()) {
                EmptyMediaState(stringResource(R.string.vault_empty))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
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
            title = { Text(stringResource(R.string.vault_delete_confirm_title)) },
            text = { Text(stringResource(R.string.vault_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePermanently(item)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun VaultCell(item: VaultItem, onRestore: () -> Unit, onDeleteRequest: () -> Unit) {
    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = com.mobile.photo.recovery.io.ui.theme.CardSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (item.isVideo) {
                // Videos show a static icon only, no real thumbnail (spec 4.9).
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NeutralDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Videocam, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onRestore) { Text(stringResource(R.string.vault_restore)) }
            TextButton(onClick = onDeleteRequest) { Text(stringResource(R.string.vault_delete)) }
        }
    }
}
