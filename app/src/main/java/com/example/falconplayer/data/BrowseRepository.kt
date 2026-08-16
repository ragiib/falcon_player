package com.example.falconplayer.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BrowseRepository"

@Singleton
class BrowseRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val videoExtensions = setOf("mp4", "mkv", "webm", "avi", "mov", "flv", "3gp", "ts", "m4v")
    private val audioExtensions = setOf("mp3", "aac", "m4a", "wav", "flac", "ogg", "opus", "amr")

    suspend fun getFavorites(): List<BrowseFolderItem> = withContext(Dispatchers.IO) {
        val favorites = mutableListOf<BrowseFolderItem>()
        val primaryStorage = Environment.getExternalStorageDirectory()

        val downloadDir = File(primaryStorage, "Download")
        if (downloadDir.exists() && downloadDir.isDirectory) {
            val (folders, files) = getCounts(downloadDir)
            favorites.add(
                BrowseFolderItem(
                    name = "Download",
                    path = downloadDir.absolutePath,
                    folderCount = folders,
                    fileCount = files,
                    iconType = BrowseFolderType.DOWNLOAD
                )
            )
        }

        val moviesDir = File(primaryStorage, "Movies")
        if (moviesDir.exists() && moviesDir.isDirectory) {
            val (folders, files) = getCounts(moviesDir)
            favorites.add(
                BrowseFolderItem(
                    name = "Movies",
                    path = moviesDir.absolutePath,
                    folderCount = folders,
                    fileCount = files,
                    iconType = BrowseFolderType.MOVIES
                )
            )
        } else {
            val dcimDir = File(primaryStorage, "DCIM")
            if (dcimDir.exists() && dcimDir.isDirectory) {
                val (folders, files) = getCounts(dcimDir)
                favorites.add(
                    BrowseFolderItem(
                        name = "Movies",
                        path = dcimDir.absolutePath,
                        folderCount = folders,
                        fileCount = files,
                        iconType = BrowseFolderType.MOVIES
                    )
                )
            }
        }

        val musicDir = File(primaryStorage, "Music")
        if (musicDir.exists() && musicDir.isDirectory) {
            val (folders, files) = getCounts(musicDir)
            favorites.add(
                BrowseFolderItem(
                    name = "Music",
                    path = musicDir.absolutePath,
                    folderCount = folders,
                    fileCount = files,
                    iconType = BrowseFolderType.MUSIC
                )
            )
        }

        favorites
    }

    suspend fun getStorages(): List<StorageItem> = withContext(Dispatchers.IO) {
        val storages = mutableListOf<StorageItem>()
        val primaryStorage = Environment.getExternalStorageDirectory()

        if (primaryStorage.exists() && primaryStorage.isDirectory) {
            val (folders, files) = getCounts(primaryStorage)
            storages.add(
                StorageItem(
                    name = "Internal memory",
                    path = primaryStorage.absolutePath,
                    folderCount = folders,
                    fileCount = files
                )
            )
        }

        try {
            val storageParent = File("/storage")
            if (storageParent.exists() && storageParent.isDirectory) {
                storageParent.listFiles()?.forEach { file ->
                    if (file.isDirectory && file.name != "emulated" && file.name != "self" && file.canRead()) {
                        val (folders, files) = getCounts(file)
                        storages.add(
                            StorageItem(
                                name = if (file.name.equals("sdcard", ignoreCase = true)) "SD Card" else file.name,
                                path = file.absolutePath,
                                folderCount = folders,
                                fileCount = files
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking secondary storage", e)
        }

        storages
    }

    suspend fun getDirectoryContents(dirPath: String): List<BrowseNodeItem> = withContext(Dispatchers.IO) {
        val nodes = mutableListOf<BrowseNodeItem>()
        val targetDir = File(dirPath)

        if (!targetDir.exists() || !targetDir.isDirectory || !targetDir.canRead()) {
            return@withContext emptyList()
        }

        val files = targetDir.listFiles() ?: return@withContext emptyList()

        val folderNodes = mutableListOf<BrowseNodeItem.FolderNode>()
        val fileNodes = mutableListOf<BrowseNodeItem.FileNode>()

        for (file in files) {
            if (file.name.startsWith(".")) continue // Skip hidden files/dirs

            if (file.isDirectory) {
                val (subFolders, mediaFiles) = getCounts(file)
                folderNodes.add(
                    BrowseNodeItem.FolderNode(
                        BrowseFolderItem(
                            name = file.name,
                            path = file.absolutePath,
                            folderCount = subFolders,
                            fileCount = mediaFiles,
                            iconType = getFolderIconType(file.name)
                        )
                    )
                )
            } else if (file.isFile) {
                val ext = file.extension.lowercase()
                val isVid = ext in videoExtensions
                val isAud = ext in audioExtensions
                if (isVid || isAud) {
                    fileNodes.add(
                        BrowseNodeItem.FileNode(
                            BrowseFileItem(
                                name = file.name,
                                path = file.absolutePath,
                                uri = Uri.fromFile(file),
                                sizeBytes = file.length(),
                                isVideo = isVid,
                                isAudio = isAud
                            )
                        )
                    )
                }
            }
        }

        folderNodes.sortBy { (it.folder.name).lowercase() }
        fileNodes.sortBy { (it.file.name).lowercase() }

        nodes.addAll(folderNodes)
        nodes.addAll(fileNodes)
        nodes
    }

    private fun getCounts(dir: File): Pair<Int, Int> {
        var folderCount = 0
        var mediaFileCount = 0
        try {
            val children = dir.listFiles()
            if (children != null) {
                for (child in children) {
                    if (child.name.startsWith(".")) continue
                    if (child.isDirectory) {
                        folderCount++
                    } else if (child.isFile) {
                        val ext = child.extension.lowercase()
                        if (ext in videoExtensions || ext in audioExtensions) {
                            mediaFileCount++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error counting items in ${dir.absolutePath}", e)
        }
        return Pair(folderCount, mediaFileCount)
    }

    private fun getFolderIconType(name: String): BrowseFolderType {
        return when (name.lowercase()) {
            "download", "downloads" -> BrowseFolderType.DOWNLOAD
            "movies", "videos", "dcim", "camera" -> BrowseFolderType.MOVIES
            "music", "audio", "podcasts" -> BrowseFolderType.MUSIC
            else -> BrowseFolderType.GENERIC
        }
    }
}
