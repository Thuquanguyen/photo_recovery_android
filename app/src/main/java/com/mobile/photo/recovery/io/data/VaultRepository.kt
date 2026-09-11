package com.mobile.photo.recovery.io.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** One item stored in the app-private vault. Note: no encryption, see spec 4.9. */
data class VaultItem(
    val assetId: Long,
    val path: String,
    val isVideo: Boolean,
    val addedAt: Long,
    val displayName: String
)

/**
 * Secure Vault storage: files are copied into <filesDir>/vault/ and tracked in
 * vault_index.json ({assetId, path, isVideo, addedAt}), exactly per spec 4.9.
 * There is intentionally NO encryption here — matches the original app's real behavior.
 */
class VaultRepository(private val context: Context) {

    private val vaultDir: File by lazy {
        File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }
    }
    private val indexFile: File by lazy { File(context.filesDir, "vault_index.json") }

    suspend fun loadIndex(): List<VaultItem> = withContext(Dispatchers.IO) {
        if (!indexFile.exists()) return@withContext emptyList()
        try {
            val text = indexFile.readText()
            val arr = JSONArray(text)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                VaultItem(
                    assetId = o.getLong("assetId"),
                    path = o.getString("path"),
                    isVideo = o.getBoolean("isVideo"),
                    addedAt = o.getLong("addedAt"),
                    displayName = o.optString("displayName", File(o.getString("path")).name)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveIndex(items: List<VaultItem>) {
        val arr = JSONArray()
        items.forEach { item ->
            val o = JSONObject()
            o.put("assetId", item.assetId)
            o.put("path", item.path)
            o.put("isVideo", item.isVideo)
            o.put("addedAt", item.addedAt)
            o.put("displayName", item.displayName)
            arr.put(o)
        }
        indexFile.writeText(arr.toString())
    }

    /** Copies [item]'s bytes into the vault and records it in the index. Returns the new VaultItem, or null on failure. */
    suspend fun addToVault(item: MediaItem): VaultItem? = withContext(Dispatchers.IO) {
        try {
            val ext = if (item.isVideo) "mp4" else "jpg"
            val destFile = File(vaultDir, "${item.id}_${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(item.uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null

            val vaultItem = VaultItem(
                assetId = item.id,
                path = destFile.absolutePath,
                isVideo = item.isVideo,
                addedAt = System.currentTimeMillis(),
                displayName = item.displayName
            )
            val current = loadIndex().toMutableList()
            current.add(vaultItem)
            saveIndex(current)
            vaultItem
        } catch (_: Exception) {
            null
        }
    }

    /** Deletes the vault copy without touching the index (used to roll back a failed original-delete). */
    suspend fun deleteVaultFileOnly(vaultItem: VaultItem) = withContext(Dispatchers.IO) {
        File(vaultItem.path).delete()
        val current = loadIndex().filterNot { it.path == vaultItem.path }
        saveIndex(current)
    }

    /** Permanently deletes an item from the vault (spec 4.9 delete-with-confirm). */
    suspend fun deletePermanently(vaultItem: VaultItem) = withContext(Dispatchers.IO) {
        File(vaultItem.path).delete()
        val current = loadIndex().filterNot { it.path == vaultItem.path }
        saveIndex(current)
    }

    /** Removes an item from the index after it has been restored to the gallery. */
    suspend fun removeFromIndex(vaultItem: VaultItem) = withContext(Dispatchers.IO) {
        val current = loadIndex().filterNot { it.path == vaultItem.path }
        saveIndex(current)
    }
}
