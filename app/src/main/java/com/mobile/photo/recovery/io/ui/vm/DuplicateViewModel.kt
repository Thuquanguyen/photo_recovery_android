package com.mobile.photo.recovery.io.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.photo.recovery.io.data.DuplicateRepository
import com.mobile.photo.recovery.io.data.MediaItem
import com.mobile.photo.recovery.io.data.MediaRepository
import com.mobile.photo.recovery.io.data.MediaTypeFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val SCAN_BATCH_SIZE = 150

data class DuplicateGroup(val hash: String, val original: MediaItem, val duplicates: List<MediaItem>)

data class DuplicateUiState(
    val groups: List<DuplicateGroup> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scannedCount: Int = 0,
    val endReached: Boolean = false,
    val pendingDeleteItems: List<MediaItem>? = null
)

/**
 * Real MD5-based duplicate detection (spec 4.7): full-file-byte MD5, grouped by hash.
 * The earliest-created item in each group is kept as "Original"; the rest are pre-selected.
 */
class DuplicateViewModel(app: Application) : AndroidViewModel(app) {
    private val mediaRepository = MediaRepository(app)
    private val duplicateRepository = DuplicateRepository(app)

    private val _state = MutableStateFlow(DuplicateUiState())
    val state: StateFlow<DuplicateUiState> = _state

    private var offset = 0
    private val hashToItems = mutableMapOf<String, MutableList<MediaItem>>()

    fun scanInitial() {
        if (_state.value.groups.isNotEmpty() || _state.value.isScanning) return
        scanMore()
    }

    fun scanMore() {
        if (_state.value.isScanning || _state.value.endReached) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, scanProgress = 0f)
            val batch = mediaRepository.queryMediaPage(offset, SCAN_BATCH_SIZE, MediaTypeFilter.IMAGES)
            offset += batch.size

            batch.forEachIndexed { index, item ->
                val hash = duplicateRepository.md5Of(item)
                if (hash != null) {
                    hashToItems.getOrPut(hash) { mutableListOf() }.add(item)
                }
                _state.value = _state.value.copy(
                    scanProgress = (index + 1f) / batch.size.coerceAtLeast(1),
                    scannedCount = _state.value.scannedCount + 1
                )
            }

            rebuildGroups()
            _state.value = _state.value.copy(isScanning = false, endReached = batch.size < SCAN_BATCH_SIZE)
        }
    }

    private fun rebuildGroups() {
        val groups = hashToItems.filter { it.value.size >= 2 }.map { (hash, items) ->
            val sorted = items.sortedBy { it.dateAddedMs }
            val original = sorted.first()
            val duplicates = sorted.drop(1)
            DuplicateGroup(hash, original, duplicates)
        }
        // Pre-select all duplicates (not the original) by default.
        val preSelected = groups.flatMap { it.duplicates.map { d -> d.id } }.toSet()
        _state.value = _state.value.copy(groups = groups, selectedIds = preSelected)
    }

    fun toggleSelected(id: Long) {
        val current = _state.value.selectedIds
        _state.value = _state.value.copy(selectedIds = if (id in current) current - id else current + id)
    }

    fun deleteSelected() {
        val selected = _state.value.selectedIds
        if (selected.isEmpty()) return
        val toDelete = _state.value.groups.flatMap { it.duplicates }.filter { it.id in selected }
        _state.value = _state.value.copy(pendingDeleteItems = toDelete)
    }

    fun createDeleteRequestFor(items: List<MediaItem>) = mediaRepository.createDeleteRequest(items.map { it.uri })

    suspend fun deleteDirectAll(items: List<MediaItem>): Boolean {
        var allOk = true
        for (item in items) {
            if (!mediaRepository.deleteDirect(item.uri)) allOk = false
        }
        return allOk
    }

    fun onDeleteResolved(deletedItems: List<MediaItem>) {
        val ids = deletedItems.map { it.id }.toSet()
        hashToItems.forEach { (_, items) -> items.removeAll { it.id in ids } }
        hashToItems.entries.removeAll { it.value.size < 2 }
        rebuildGroups()
        _state.value = _state.value.copy(pendingDeleteItems = null)
    }

    fun cancelPendingDelete() {
        _state.value = _state.value.copy(pendingDeleteItems = null)
    }

    fun selectedSizeBytes(): Long {
        val selected = _state.value.selectedIds
        return _state.value.groups.flatMap { it.duplicates }.filter { it.id in selected }.sumOf { it.size }
    }
}
