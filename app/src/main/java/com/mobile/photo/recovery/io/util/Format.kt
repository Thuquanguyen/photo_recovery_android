package com.mobile.photo.recovery.io.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/** Byte formatting, exactly per spec 5.3. */
fun formatBytes(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        bytes < kb -> "0 KB"
        bytes >= gb -> String.format(Locale.US, "%.1f GB", bytes / gb)
        bytes >= mb -> String.format(Locale.US, "%.1f MB", bytes / mb)
        else -> "${(bytes / kb).roundToInt()} KB"
    }
}

/** Relative date formatting, exactly per spec 5.2. */
fun formatRelativeDate(timestampMs: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - timestampMs
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val days = TimeUnit.MILLISECONDS.toDays(diffMs)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        isSameDay(timestampMs, now) -> "$hours h ago"
        days < 2 -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.US).format(timestampMs)
    }
}

private fun isSameDay(a: Long, b: Long): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.US)
    return fmt.format(a) == fmt.format(b)
}
