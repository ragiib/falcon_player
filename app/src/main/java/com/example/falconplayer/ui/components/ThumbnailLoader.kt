package com.example.falconplayer.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Global LruCache for video thumbnail bitmaps
private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
private val cacheSize = maxMemory / 8
private val thumbnailCache = object : LruCache<String, Bitmap>(cacheSize) {
    override fun sizeOf(key: String, bitmap: Bitmap): Int {
        return bitmap.byteCount / 1024
    }
}

suspend fun loadVideoThumbnail(context: Context, videoUri: Uri): Bitmap? {
    val cacheKey = videoUri.toString()
    thumbnailCache.get(cacheKey)?.let { return it }

    return withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                bitmap = context.contentResolver.loadThumbnail(
                    videoUri,
                    Size(320, 200),
                    null
                )
            } else {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, videoUri)
                    bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } finally {
                    retriever.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (bitmap != null) {
            thumbnailCache.put(cacheKey, bitmap)
        }
        bitmap
    }
}

@Composable
fun VideoThumbnailImage(
    videoUri: Uri,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))
                    )
                )
        )
    }
) {
    val context = LocalContext.current
    var bitmap by remember(videoUri) { mutableStateOf<Bitmap?>(thumbnailCache.get(videoUri.toString())) }

    LaunchedEffect(videoUri) {
        if (bitmap == null) {
            bitmap = loadVideoThumbnail(context, videoUri)
        }
    }

    val currentBitmap = bitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        placeholder()
    }
}
