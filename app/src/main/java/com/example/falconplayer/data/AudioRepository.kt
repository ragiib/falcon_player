package com.example.falconplayer.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AudioRepository"

@Singleton
class AudioRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun getAudioTracks(): List<AudioItem> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<AudioItem>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                Log.d(TAG, "MediaStore audio query returned ${cursor.count} rows")

                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val nameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)

                val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getLong(idCol) else continue
                    val titleStr = if (titleCol >= 0) cursor.getString(titleCol) else null
                    val nameStr = if (nameCol >= 0) cursor.getString(nameCol) else null
                    val title = titleStr ?: nameStr ?: "Audio_$id"

                    val artist = if (artistCol >= 0) cursor.getString(artistCol) ?: "Unknown Artist" else "Unknown Artist"
                    val album = if (albumCol >= 0) cursor.getString(albumCol) ?: "Unknown Album" else "Unknown Album"
                    val albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else 0L
                    val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val dateAdded = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else 0L
                    val trackNum = if (trackCol >= 0) cursor.getInt(trackCol) else 0

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumArtUri = if (albumId > 0) {
                        ContentUris.withAppendedId(albumArtBaseUri, albumId)
                    } else null

                    tracks.add(
                        AudioItem(
                            id = id,
                            contentUri = contentUri,
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Unknown Album" else album,
                            durationMs = duration,
                            albumId = albumId,
                            trackNumber = trackNum,
                            sizeBytes = size,
                            dateAddedSec = dateAdded,
                            albumArtUri = albumArtUri
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying audio MediaStore", e)
        }

        Log.d(TAG, "Scanned total ${tracks.size} audio tracks")
        tracks
    }

    suspend fun getArtists(tracks: List<AudioItem>): List<ArtistItem> = withContext(Dispatchers.Default) {
        tracks.groupBy { it.artist }
            .map { (artistName, artistTracks) ->
                val albums = artistTracks.map { it.album }.distinct()
                val coverUri = artistTracks.firstOrNull { it.albumArtUri != null }?.albumArtUri
                ArtistItem(
                    name = artistName,
                    albumCount = albums.size,
                    trackCount = artistTracks.size,
                    tracks = artistTracks,
                    coverUri = coverUri
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun getAlbums(tracks: List<AudioItem>): List<AlbumItem> = withContext(Dispatchers.Default) {
        tracks.groupBy { it.albumId }
            .map { (albumId, albumTracks) ->
                val albumTitle = albumTracks.firstOrNull()?.album ?: "Unknown Album"
                val artistName = albumTracks.firstOrNull()?.artist ?: "Unknown Artist"
                val albumArtUri = albumTracks.firstOrNull()?.albumArtUri
                AlbumItem(
                    id = albumId,
                    title = albumTitle,
                    artist = artistName,
                    trackCount = albumTracks.size,
                    tracks = albumTracks,
                    albumArtUri = albumArtUri
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    suspend fun getGenres(tracks: List<AudioItem>): List<GenreItem> = withContext(Dispatchers.Default) {
        tracks.groupBy { it.genre ?: "Unknown Genre" }
            .map { (genreName, genreTracks) ->
                GenreItem(
                    name = genreName,
                    trackCount = genreTracks.size,
                    tracks = genreTracks
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}
