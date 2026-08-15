package com.example.falconplayer.data

import android.net.Uri

data class VideoItem(
    val id: Long,
    val contentUri: Uri,
    val title: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val bucketId: String,
    val bucketName: String,
    val dateAddedSec: Long = 0L,
    val resolutionBadge: String? = null,
    val durationFormatted: String = formatDuration(durationMs)
)

data class FolderItem(
    val bucketId: String,
    val bucketName: String,
    val videoCount: Int,
    val previewVideos: List<VideoItem>
)

fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val totalSeconds = durationMs / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

fun calculateResolutionBadge(width: Int, height: Int): String? {
    val maxDim = maxOf(width, height)
    val minDim = minOf(width, height)

    return when {
        maxDim >= 3840 || minDim >= 2160 -> "4K"
        maxDim >= 1920 || minDim >= 1080 -> "1080p"
        maxDim >= 1280 || minDim >= 720 -> "720p"
        maxDim >= 854 || minDim >= 480 -> "480p"
        else -> null
    }
}
