package com.mobile.photo.recovery.io.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.photo.recovery.io.data.MediaRepository
import com.mobile.photo.recovery.io.data.VaultItem
import com.mobile.photo.recovery.io.data.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class VaultUiState(
    val items: List<VaultItem> = emptyList(),
    val isLoading: Boolean = false,
    val lastEvent: VaultEvent? = null
)

sealed class VaultEvent {
    object Restored : VaultEvent()
    object Deleted : VaultEvent()
}

/** Secure Vault: NOT encrypted, just app-private file storage + JSON index (spec 4.9). */
class VaultViewModel(app: Application) : AndroidViewModel(app) {
    private val vaultRepository = VaultRepository(app)
    private val mediaRepository = MediaRepository(app)

    private val _state = MutableStateFlow(VaultUiState())
    val state: StateFlow<VaultUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val items = vaultRepository.loadIndex()
            _state.value = _state.value.copy(items = items, isLoading = false)
        }
    }

    fun restore(item: VaultItem) {
        viewModelScope.launch {
            val ok = mediaRepository.restoreFileToGallery(File(item.path), item.displayName, item.isVideo)
            if (ok) {
                // Restoring moves the item out of the vault: drop the index entry and the
                // now-redundant private copy so it doesn't linger and waste storage.
                vaultRepository.removeFromIndex(item)
                File(item.path).delete()
                _state.value = _state.value.copy(
                    items = _state.value.items.filterNot { it.path == item.path },
                    lastEvent = VaultEvent.Restored
                )
            }
        }
    }

    fun deletePermanently(item: VaultItem) {
        viewModelScope.launch {
            vaultRepository.deletePermanently(item)
            _state.value = _state.value.copy(
                items = _state.value.items.filterNot { it.path == item.path },
                lastEvent = VaultEvent.Deleted
            )
        }
    }

    fun consumeEvent() {
        _state.value = _state.value.copy(lastEvent = null)
    }
}
