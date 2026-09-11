package com.mobile.photo.recovery.io.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Real duplicate detection: MD5 of the full file bytes (spec 4.7 / 5.4).
 * This is byte-for-byte comparison, not a size/name heuristic.
 */
class DuplicateRepository(private val context: Context) {

    suspend fun md5Of(item: MediaItem): String? = withContext(Dispatchers.IO) {
        try {
            val digest = MessageDigest.getInstance("MD5")
            context.contentResolver.openInputStream(item.uri)?.use { input ->
                val buffer = ByteArray(8 * 1024)
                var read: Int
                while (input.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
            } ?: return@withContext null
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            null
        }
    }
}
