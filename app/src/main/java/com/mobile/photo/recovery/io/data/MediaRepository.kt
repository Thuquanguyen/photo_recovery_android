package com.mobile.photo.recovery.io.data

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.StatFs
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class MediaTypeFilter { ALL, IMAGES, VIDEOS }

/**
 * All read/write access to the device media library. Everything here is real MediaStore
 * usage — there is no "deleted photo recovery" API on Android, so "recovery" in this app
 * means: list existing media and copy it elsewhere (spec 4.5).
 */
class MediaRepository(private val context: Context) {

    private val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATE_ADDED,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
    )

    private val baseSelection =
        "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
            " OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})"

    private fun collection(): Uri =
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

    private fun rowToItem(cursor: android.database.Cursor): MediaItem {
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
        val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
        val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)

        val id = cursor.getLong(idCol)
        val isVideo = cursor.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        val contentUri = if (isVideo) {
            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
        } else {
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        }
        return MediaItem(
            id = id,
            uri = contentUri,
            displayName = cursor.getString(nameCol) ?: "",
            dateAddedMs = cursor.getLong(dateCol) * 1000L,
            isVideo = isVideo,
            size = cursor.getLong(sizeCol),
            bucketName = cursor.getString(bucketCol)
        )
    }

    /** Real device free space (spec 4.4 / 5). */
    suspend fun freeSpaceBytes(): Long = withContext(Dispatchers.IO) {
        val stat = StatFs(context.getExternalFilesDir(null)?.path ?: "/")
        stat.availableBytes
    }

    /** Real photo/video counts on device (spec 4.4). */
    suspend fun countMedia(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        var images = 0
        var videos = 0
        context.contentResolver.query(
            collection(),
            arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE),
            baseSelection, null, null
        )?.use { cursor ->
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            while (cursor.moveToNext()) {
                if (cursor.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) videos++ else images++
            }
        }
        images to videos
    }

    /**
     * Runs a paginated query. Appending "LIMIT x OFFSET y" to the sortOrder string (the old
     * approach) throws "IllegalArgumentException: Invalid token LIMIT" on modern MediaProvider
     * implementations that validate sortOrder strictly — use the proper Bundle query-args form
     * (QUERY_ARG_SQL_LIMIT/QUERY_ARG_OFFSET, API 30+) instead, with the old string-based
     * approach only as a fallback for API < 30.
     */
    private fun pagedQuery(
        selection: String,
        selectionArgs: Array<String>?,
        offset: Int,
        limit: Int
    ): android.database.Cursor? {
        val sortColumn = MediaStore.Files.FileColumns.DATE_ADDED
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val args = android.os.Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs ?: emptyArray())
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "$sortColumn DESC")
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            }
            context.contentResolver.query(collection(), projection, args, null)
        } else {
            val sort = "$sortColumn DESC LIMIT $limit OFFSET $offset"
            context.contentResolver.query(collection(), projection, selection, selectionArgs, sort)
        }
    }

    /** Paginated, newest-first list of all images/videos (used by Photo Recovery & Quick Swipe Clean). */
    suspend fun queryMediaPage(
        offset: Int,
        limit: Int,
        filter: MediaTypeFilter = MediaTypeFilter.ALL
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val selection = when (filter) {
            MediaTypeFilter.ALL -> baseSelection
            MediaTypeFilter.IMAGES -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"
            MediaTypeFilter.VIDEOS -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"
        }
        val items = mutableListOf<MediaItem>()
        pagedQuery(selection, null, offset, limit)?.use { cursor ->
            while (cursor.moveToNext()) items.add(rowToItem(cursor))
        }
        items
    }

    /** All items whose bucket (album) name matches one of the known screenshot-folder names (spec 4.8). */
    private val screenshotBucketNames = setOf(
        "screenshots", "screenshot", "ảnh chụp màn hình", "chụp màn hình"
    )

    suspend fun queryScreenshotsPage(offset: Int, limit: Int): List<MediaItem> = withContext(Dispatchers.IO) {
        val placeholders = screenshotBucketNames.joinToString(",") { "?" }
        val selection = "$baseSelection AND lower(${MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME}) IN ($placeholders)"
        val args = screenshotBucketNames.toTypedArray()
        val items = mutableListOf<MediaItem>()
        pagedQuery(selection, args, offset, limit)?.use { cursor ->
            while (cursor.moveToNext()) items.add(rowToItem(cursor))
        }
        items
    }

    /**
     * Copies the given items into Pictures/Restored via MediaStore (scoped storage), spec 4.5.
     * Returns the number of files successfully copied.
     */
    suspend fun copyToRestored(items: List<MediaItem>): Int = withContext(Dispatchers.IO) {
        var copied = 0
        for (item in items) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, item.displayName)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Restored")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val targetCollection = if (item.isVideo) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val destUri = context.contentResolver.insert(targetCollection, values) ?: continue
                context.contentResolver.openInputStream(item.uri)?.use { input ->
                    context.contentResolver.openOutputStream(destUri)?.use { output ->
                        input.copyTo(output)
                    }
                }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(destUri, values, null, null)
                copied++
            } catch (_: Exception) {
                // Skip files that fail to copy; caller reports the final count only.
            }
        }
        copied
    }

    /**
     * Requests the system delete confirmation for the given URIs (Android 11+, spec 4.6/4.8).
     * On older versions, deletes directly through the ContentResolver (best effort, may throw
     * on Android 10 scoped-storage restrictions for files this app doesn't own).
     */
    fun createDeleteRequest(uris: List<Uri>): PendingIntent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.createDeleteRequest(context.contentResolver, uris)
        } else {
            null
        }
    }

    suspend fun deleteDirect(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (_: Exception) {
            false
        }
    }

    /** Saves a file from the app's private storage back into the public gallery (Vault restore, spec 4.9). */
    suspend fun restoreFileToGallery(sourcePath: java.io.File, displayName: String, isVideo: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, if (isVideo) "Movies/Restored" else "Pictures/Restored")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val targetCollection = if (isVideo) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val destUri = context.contentResolver.insert(targetCollection, values) ?: return@withContext false
                sourcePath.inputStream().use { input ->
                    context.contentResolver.openOutputStream(destUri)?.use { output -> input.copyTo(output) }
                }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(destUri, values, null, null)
                true
            } catch (_: Exception) {
                false
            }
        }
}
