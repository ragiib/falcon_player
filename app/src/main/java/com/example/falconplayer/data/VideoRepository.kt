package com.example.falconplayer.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "VideoRepository"

@Singleton
class VideoRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun getVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoItem>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA
        )

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                Log.d(TAG, "MediaStore query returned ${cursor.count} rows")

                val idCol = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val titleCol = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                val bucketIdCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
                val bucketNameCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getLong(idCol) else continue
                    val dataPath = if (dataCol >= 0) cursor.getString(dataCol) else null
                    val displayName = if (nameCol >= 0) cursor.getString(nameCol) else null
                    val title = if (titleCol >= 0) cursor.getString(titleCol) else null

                    val name = displayName ?: title ?: dataPath?.let { File(it).name } ?: "Video_$id"
                    val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                    val width = if (widthCol >= 0) cursor.getInt(widthCol) else 0
                    val height = if (heightCol >= 0) cursor.getInt(heightCol) else 0
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L

                    val rawBucketId = if (bucketIdCol >= 0) cursor.getString(bucketIdCol) else null
                    val rawBucketName = if (bucketNameCol >= 0) cursor.getString(bucketNameCol) else null

                    val fallbackBucketName = dataPath?.let { File(it).parentFile?.name } ?: "Videos"
                    val bucketName = rawBucketName ?: fallbackBucketName
                    val bucketId = rawBucketId ?: bucketName.hashCode().toString()

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val resolutionBadge = calculateResolutionBadge(width, height)

                    videos.add(
                        VideoItem(
                            id = id,
                            contentUri = contentUri,
                            title = name,
                            durationMs = duration,
                            width = width,
                            height = height,
                            sizeBytes = size,
                            bucketId = bucketId,
                            bucketName = bucketName,
                            resolutionBadge = resolutionBadge
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
        }

        Log.d(TAG, "Scanned total ${videos.size} videos")
        videos
    }

    suspend fun getFolders(videos: List<VideoItem>): List<FolderItem> = withContext(Dispatchers.Default) {
        videos.groupBy { it.bucketId }
            .map { (bucketId, bucketVideos) ->
                val bucketName = bucketVideos.firstOrNull()?.bucketName ?: "Videos"
                FolderItem(
                    bucketId = bucketId,
                    bucketName = bucketName,
                    videoCount = bucketVideos.size,
                    previewVideos = bucketVideos.take(4)
                )
            }
            .sortedByDescending { it.videoCount }
    }
}
