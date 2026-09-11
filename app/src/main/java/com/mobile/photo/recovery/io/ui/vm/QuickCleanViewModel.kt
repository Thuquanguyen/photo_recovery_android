package com.mobile.photo.recovery.io.ui.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.photo.recovery.io.data.MediaItem
import com.mobile.photo.recovery.io.data.MediaRepository
import com.mobile.photo.recovery.io.data.MediaTypeFilter
import com.mobile.photo.recovery.io.data.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val BATCH_SIZE = 20
private const val LOAD_MORE_THRESHOLD = 5

/** Real cooldown to avoid overwhelming the system thumbnail decoder on very fast swipes (spec 4.6). */
const val SWIPE_COOLDOWN_MS = 220L

sealed class PendingDeleteRequest {
    data class OriginalDelete(val item: MediaItem) : PendingDeleteRequest()
    data class VaultRollback(val item: MediaItem, val vaultPath: String) : PendingDeleteRequest()
}

data class QuickCleanUiState(
    val queue: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val filter: MediaTypeFilter = MediaTypeFilter.ALL,
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
    val reviewedCount: Int = 0,
    val protectedCount: Int = 0,
    val bytesFreed: Long = 0L,
    val pendingDelete: PendingDeleteRequest? = null
)

class QuickCleanViewModel(app: Application) : AndroidViewModel(app) {
    private val mediaRepository = MediaRepository(app)
    private val vaultRepository = VaultRepository(app)

    private val _state = MutableStateFlow(QuickCleanUiState())
    val state: StateFlow<QuickCleanUiState> = _state

    private var offset = 0
    private val history = mutableListOf<MediaItem>()

    fun loadInitial() {
        if (_state.value.queue.isNotEmpty()) return
        loadMore()
    }

    fun setFilter(filter: MediaTypeFilter) {
        offset = 0
        _state.value = _state.value.copy(filter = filter, queue = emptyList(), currentIndex = 0, endReached = false)
        loadMore()
    }

    fun loadMore() {
        if (_state.value.isLoading || _state.value.endReached) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val page = mediaRepository.queryMediaPage(offset, BATCH_SIZE, _state.value.filter)
            offset += page.size
            _state.value = _state.value.copy(
                queue = _state.value.queue + page,
                isLoading = false,
                endReached = page.size < BATCH_SIZE
            )
        }
    }

    private fun maybeLoadMore() {
        val remaining = _state.value.queue.size - _state.value.currentIndex
        if (remaining <= LOAD_MORE_THRESHOLD) loadMore()
    }

    fun currentItem(): MediaItem? = _state.value.queue.getOrNull(_state.value.currentIndex)

    /** Up = skip to next item without deleting or protecting. */
    fun skip() {
        currentItem()?.let { history.add(it) }
        advance()
    }

    /** Down = go back to the previous item. */
    fun previous() {
        if (history.isNotEmpty()) {
            history.removeAt(history.lastIndex)
            _state.value = _state.value.copy(currentIndex = (_state.value.currentIndex - 1).coerceAtLeast(0))
        }
    }

    private fun advance() {
        _state.value = _state.value.copy(
            currentIndex = _state.value.currentIndex + 1,
            reviewedCount = _state.value.reviewedCount + 1
        )
        maybeLoadMore()
    }

    /** Left = request the system delete confirmation for the original file. */
    fun requestDelete() {
        val item = currentItem() ?: return
        _state.value = _state.value.copy(pendingDelete = PendingDeleteRequest.OriginalDelete(item))
    }

    fun onDeleteConfirmed(freedBytes: Long) {
        _state.value = _state.value.copy(
            bytesFreed = _state.value.bytesFreed + freedBytes,
            pendingDelete = null
        )
        advance()
    }

    fun onDeleteCancelled() {
        _state.value = _state.value.copy(pendingDelete = null)
    }

    /** Right = copy into the vault, then request deletion of the original; roll back the vault
     * copy if the user cancels the system delete confirmation (spec 4.6). */
    fun protectToVault() {
        val item = currentItem() ?: return
        viewModelScope.launch {
            val vaultItem = vaultRepository.addToVault(item) ?: return@launch
            _state.value = _state.value.copy(
                pendingDelete = PendingDeleteRequest.VaultRollback(item, vaultItem.path)
            )
        }
    }

    fun onProtectDeleteConfirmed() {
        val pending = _state.value.pendingDelete as? PendingDeleteRequest.VaultRollback ?: return
        _state.value = _state.value.copy(
            protectedCount = _state.value.protectedCount + 1,
            pendingDelete = null
        )
        advance()
    }

    fun onProtectDeleteCancelled() {
        val pending = _state.value.pendingDelete as? PendingDeleteRequest.VaultRollback ?: return
        viewModelScope.launch {
            vaultRepository.deleteVaultFileOnly(
                com.mobile.photo.recovery.io.data.VaultItem(
                    assetId = pending.item.id,
                    path = pending.vaultPath,
                    isVideo = pending.item.isVideo,
                    addedAt = 0L,
                    displayName = pending.item.displayName
                )
            )
            _state.value = _state.value.copy(pendingDelete = null)
        }
    }

    suspend fun deleteUriDirect(uri: Uri): Boolean = mediaRepository.deleteDirect(uri)

    fun createDeleteRequest(uri: Uri) = mediaRepository.createDeleteRequest(listOf(uri))
}
