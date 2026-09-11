package com.mobile.photo.recovery.io.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.photo.recovery.io.data.MediaItem
import com.mobile.photo.recovery.io.data.MediaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10
// Purely cosmetic delay (spec 4.5): the real query is near-instant, this just makes each
// batch load feel like an active scan, matching the original app's UX choice.
private const val FAKE_BATCH_DELAY_MS = 700L

data class PhotoRecoveryUiState(
    val items: List<MediaItem> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val restoredIds: Set<Long> = emptySet(),
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val isRecovering: Boolean = false,
    val lastRecoveredCount: Int? = null
)

class PhotoRecoveryViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = MediaRepository(app)

    private val _state = MutableStateFlow(PhotoRecoveryUiState())
    val state: StateFlow<PhotoRecoveryUiState> = _state

    private var offset = 0
    private var loadJob: kotlinx.coroutines.Job? = null

    fun loadInitial() {
        if (_state.value.items.isNotEmpty()) return
        loadMore()
    }

    fun loadMore() {
        if (_state.value.isLoadingMore || _state.value.endReached) return
        loadJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMore = true)
            delay(FAKE_BATCH_DELAY_MS)
            val page = repository.queryMediaPage(offset, PAGE_SIZE)
            offset += page.size
            // New items are selected by default (spec 4.5).
            val newSelected = _state.value.selectedIds + page.map { it.id }
            _state.value = _state.value.copy(
                items = _state.value.items + page,
                selectedIds = newSelected,
                isLoadingMore = false,
                endReached = page.size < PAGE_SIZE
            )
        }
    }

    fun toggleSelected(id: Long) {
        val current = _state.value.selectedIds
        _state.value = _state.value.copy(
            selectedIds = if (id in current) current - id else current + id
        )
    }

    fun recoverSelected() {
        val selectedItems = _state.value.items.filter { it.id in _state.value.selectedIds }
        if (selectedItems.isEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isRecovering = true)
            val copied = repository.copyToRestored(selectedItems)
            _state.value = _state.value.copy(
                isRecovering = false,
                restoredIds = _state.value.restoredIds + selectedItems.map { it.id },
                lastRecoveredCount = copied
            )
        }
    }

    fun consumeRecoveredEvent() {
        _state.value = _state.value.copy(lastRecoveredCount = null)
    }
}
