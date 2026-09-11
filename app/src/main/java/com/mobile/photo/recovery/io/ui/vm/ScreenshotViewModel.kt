package com.mobile.photo.recovery.io.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.photo.recovery.io.data.MediaItem
import com.mobile.photo.recovery.io.data.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val BATCH_SIZE = 60

enum class ScreenshotSection { TODAY, YESTERDAY, OLDER }

data class ScreenshotUiState(
    val items: List<MediaItem> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
    val pendingDeleteItems: List<MediaItem>? = null
)

/** Finds a "Screenshots" album by name match (best-effort, spec 4.8 — no smart detection). */
class ScreenshotViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = MediaRepository(app)

    private val _state = MutableStateFlow(ScreenshotUiState())
    val state: StateFlow<ScreenshotUiState> = _state

    private var offset = 0

    fun loadInitial() {
        if (_state.value.items.isNotEmpty()) return
        loadMore()
    }

    fun loadMore() {
        if (_state.value.isLoading || _state.value.endReached) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val page = repository.queryScreenshotsPage(offset, BATCH_SIZE)
            offset += page.size
            _state.value = _state.value.copy(
                items = _state.value.items + page,
                isLoading = false,
                endReached = page.size < BATCH_SIZE
            )
        }
    }

    fun toggleSelected(id: Long) {
        val current = _state.value.selectedIds
        _state.value = _state.value.copy(selectedIds = if (id in current) current - id else current + id)
    }

    fun requestDeleteSelected() {
        val selected = _state.value.items.filter { it.id in _state.value.selectedIds }
        if (selected.isEmpty()) return
        _state.value = _state.value.copy(pendingDeleteItems = selected)
    }

    fun createDeleteRequestFor(items: List<MediaItem>) = repository.createDeleteRequest(items.map { it.uri })

    suspend fun deleteDirectAll(items: List<MediaItem>): Boolean {
        var allOk = true
        for (item in items) {
            if (!repository.deleteDirect(item.uri)) allOk = false
        }
        return allOk
    }

    fun onDeleteResolved(deleted: List<MediaItem>) {
        val ids = deleted.map { it.id }.toSet()
        _state.value = _state.value.copy(
            items = _state.value.items.filterNot { it.id in ids },
            selectedIds = _state.value.selectedIds - ids,
            pendingDeleteItems = null
        )
    }

    fun cancelPendingDelete() {
        _state.value = _state.value.copy(pendingDeleteItems = null)
    }

    fun sectionOf(item: MediaItem): ScreenshotSection {
        val now = System.currentTimeMillis()
        val days = TimeUnit.MILLISECONDS.toDays(now - item.dateAddedMs)
        return when {
            days < 1 -> ScreenshotSection.TODAY
            days < 2 -> ScreenshotSection.YESTERDAY
            else -> ScreenshotSection.OLDER
        }
    }
}
