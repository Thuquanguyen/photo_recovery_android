package com.mobile.photo.recovery.io.data

import android.net.Uri

/** A single image or video read from MediaStore. */
data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateAddedMs: Long,
    val isVideo: Boolean,
    val size: Long,
    val bucketName: String?
)
