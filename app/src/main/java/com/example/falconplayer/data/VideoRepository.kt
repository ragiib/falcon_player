package com.example.falconplayer.data

import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
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
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED
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
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)

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
                    val dateAddedSec = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else 0L

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
                            dateAddedSec = dateAddedSec,
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

    /**
     * Deletes a video from device storage using scoped storage APIs.
     * On Android Q+, returns [DeleteResult.NeedsPermission] with an IntentSender
     * that the Activity must launch to request OS-level delete permission.
     * On older Android, deletes directly.
     */
    suspend fun deleteVideo(videoUri: Uri): DeleteResult = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ — createDeleteRequest grants permission and deletes atomically
                val intentSender = MediaStore.createDeleteRequest(
                    context.contentResolver,
                    listOf(videoUri)
                ).intentSender
                DeleteResult.NeedsPermission(intentSender)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 — try direct delete; may throw RecoverableSecurityException
                try {
                    val deleted = context.contentResolver.delete(videoUri, null, null)
                    if (deleted > 0) DeleteResult.Success else DeleteResult.Failure("File not found or already deleted")
                } catch (e: android.app.RecoverableSecurityException) {
                    DeleteResult.NeedsPermission(e.userAction.actionIntent.intentSender)
                }
            } else {
                // Android 9 and below — direct delete
                val deleted = context.contentResolver.delete(videoUri, null, null)
                if (deleted > 0) DeleteResult.Success else DeleteResult.Failure("File not found or already deleted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting video $videoUri", e)
            DeleteResult.Failure(e.localizedMessage ?: "Unknown error")
        }
    }

    /**
     * Bulk delete multiple videos. On Android 11+ a single system dialog covers all.
     * On older Android, deletes each individually and collects failures.
     */
    suspend fun deleteVideos(uris: List<Uri>): BulkDeleteResult = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext BulkDeleteResult(emptyList(), emptyList())
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // One request, one dialog, all deleted atomically
                val intentSender = MediaStore.createDeleteRequest(
                    context.contentResolver,
                    uris
                ).intentSender
                BulkDeleteResult(emptyList(), emptyList(), needsPermissionSender = intentSender)
            } else {
                val deleted = mutableListOf<Uri>()
                val failed = mutableListOf<Pair<Uri, String>>()
                for (uri in uris) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                val count = context.contentResolver.delete(uri, null, null)
                                if (count > 0) deleted.add(uri) else failed.add(uri to "Not found")
                            } catch (e: android.app.RecoverableSecurityException) {
                                failed.add(uri to "Permission denied")
                            }
                        } else {
                            val count = context.contentResolver.delete(uri, null, null)
                            if (count > 0) deleted.add(uri) else failed.add(uri to "Not found")
                        }
                    } catch (e: Exception) {
                        failed.add(uri to (e.localizedMessage ?: "Unknown error"))
                    }
                }
                BulkDeleteResult(deleted, failed)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error bulk deleting videos", e)
            BulkDeleteResult(emptyList(), uris.map { it to (e.localizedMessage ?: "Error") })
        }
    }

    data class BulkDeleteResult(
        val deleted: List<Uri>,
        val failed: List<Pair<Uri, String>>,
        val needsPermissionSender: IntentSender? = null
    )

    sealed interface DeleteResult {
        object Success : DeleteResult
        data class NeedsPermission(val intentSender: IntentSender) : DeleteResult
        data class Failure(val reason: String) : DeleteResult
    }
}
