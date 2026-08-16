package com.example.falconplayer.data

import android.net.Uri

enum class BrowseFolderType {
    DOWNLOAD,
    MOVIES,
    MUSIC,
    GENERIC
}

data class BrowseFolderItem(
    val name: String,
    val path: String,
    val folderCount: Int,
    val fileCount: Int,
    val iconType: BrowseFolderType = BrowseFolderType.GENERIC
)

data class BrowseFileItem(
    val name: String,
    val path: String,
    val uri: Uri,
    val sizeBytes: Long,
    val isVideo: Boolean,
    val isAudio: Boolean,
    val durationMs: Long = 0L,
    val formattedSize: String = formatFileSize(sizeBytes)
)

data class StorageItem(
    val name: String,
    val path: String,
    val folderCount: Int,
    val fileCount: Int
)

sealed interface BrowseNodeItem {
    data class FolderNode(val folder: BrowseFolderItem) : BrowseNodeItem
    data class FileNode(val file: BrowseFileItem) : BrowseNodeItem
}

fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return String.format("%.1f %s", sizeBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
