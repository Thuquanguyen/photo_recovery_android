package com.mobile.photo.recovery.io.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.photo.recovery.io.data.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val freeSpaceBytes: Long = 0L,
    val photoCount: Int = 0,
    val videoCount: Int = 0,
    val loaded: Boolean = false
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = MediaRepository(app)

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    fun load() {
        viewModelScope.launch {
            val free = repository.freeSpaceBytes()
            val (images, videos) = repository.countMedia()
            _state.value = HomeUiState(free, images, videos, loaded = true)
        }
    }
}
