package com.example.falconplayer.ui.audio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falconplayer.data.AlbumItem
import com.example.falconplayer.data.ArtistItem
import com.example.falconplayer.data.AudioItem
import com.example.falconplayer.data.GenreItem
import com.example.falconplayer.theme.FalconBackground
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.theme.FalconTextPrimary
import com.example.falconplayer.theme.FalconTextSecondary
import com.example.falconplayer.ui.home.FalconLogoIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val audioArtCache = LruCache<String, Bitmap>(30)

suspend fun loadAudioAlbumArt(context: Context, albumArtUri: Uri?): Bitmap? {
    if (albumArtUri == null) return null
    val cacheKey = albumArtUri.toString()
    audioArtCache.get(cacheKey)?.let { return it }

    return withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                bitmap = context.contentResolver.loadThumbnail(albumArtUri, Size(300, 300), null)
            } else {
                context.contentResolver.openInputStream(albumArtUri)?.use { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                }
            }
        } catch (e: Exception) {
            // Ignore failure fallback
        }
        if (bitmap != null) {
            audioArtCache.put(cacheKey, bitmap)
        }
        bitmap
    }
}

@Composable
fun AudioAlbumArtImage(
    albumArtUri: Uri?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackIcon: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FalconSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = FalconTextSecondary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
) {
    val context = LocalContext.current
    var bitmap by remember(albumArtUri) {
        mutableStateOf<Bitmap?>(albumArtUri?.let { audioArtCache.get(it.toString()) })
    }

    LaunchedEffect(albumArtUri) {
        if (bitmap == null && albumArtUri != null) {
            bitmap = loadAudioAlbumArt(context, albumArtUri)
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
        fallbackIcon()
    }
}

@Composable
fun AudioScreen(
    uiState: AudioUiState,
    onTabSelected: (Int) -> Unit,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPlayTrack: (AudioItem) -> Unit,
    onPlayTracks: (List<AudioItem>) -> Unit,
    onAddToPlaylist: (AudioItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("ARTISTS", "ALBUMS", "TRACKS", "GENRES", "PLAYLISTS")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconBackground)
    ) {
        // TOP APP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(FalconBackground)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.isSearching) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search audio...", color = FalconTextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FalconSurface,
                        unfocusedContainerColor = FalconSurface,
                        focusedTextColor = FalconTextPrimary,
                        unfocusedTextColor = FalconTextPrimary,
                        cursorColor = FalconRed,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Search",
                        tint = FalconTextPrimary
                    )
                }
            } else {
                AudioLogoIcon(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "VLC",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FalconTextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = FalconTextPrimary
                    )
                }
                IconButton(onClick = {
                    if (uiState.tracks.isNotEmpty()) {
                        onPlayTracks(uiState.tracks.shuffled())
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = FalconTextPrimary
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = FalconTextPrimary
                    )
                }
            }
        }

        // SUB-TABS ROW
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = FalconBackground,
            contentColor = FalconRed,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (uiState.selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = FalconRed,
                        height = 3.dp
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = uiState.selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            color = if (isSelected) FalconRed else FalconTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        // CONTENT AREA
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FalconRed)
                }
            } else if (uiState.tracks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = FalconRed,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Audio Files Found",
                        color = FalconTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Music and audio files on your device will appear here.",
                        color = FalconTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                when (uiState.selectedTab) {
                    0 -> ArtistsTabContent(
                        artists = uiState.filteredArtists,
                        onPlayArtist = { artist -> onPlayTracks(artist.tracks) },
                        onAddToPlaylist = { artist -> artist.tracks.firstOrNull()?.let(onAddToPlaylist) }
                    )
                    1 -> AlbumsTabContent(
                        albums = uiState.filteredAlbums,
                        onPlayAlbum = { album -> onPlayTracks(album.tracks) },
                        onAddToPlaylist = { album -> album.tracks.firstOrNull()?.let(onAddToPlaylist) }
                    )
                    2 -> TracksTabContent(
                        tracks = uiState.filteredTracks,
                        onPlayTrack = onPlayTrack,
                        onAddToPlaylist = onAddToPlaylist
                    )
                    3 -> GenresTabContent(
                        genres = uiState.filteredGenres,
                        onPlayGenre = { genre -> onPlayTracks(genre.tracks) }
                    )
                    4 -> TracksTabContent(
                        tracks = uiState.filteredTracks,
                        onPlayTrack = onPlayTrack,
                        onAddToPlaylist = onAddToPlaylist
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistsTabContent(
    artists: List<ArtistItem>,
    onPlayArtist: (ArtistItem) -> Unit,
    onAddToPlaylist: (ArtistItem) -> Unit
) {
    // Group artists by first letter
    val grouped = remember(artists) {
        artists.groupBy { artist ->
            val first = artist.name.trim().firstOrNull()?.uppercaseChar() ?: '#'
            if (first in 'A'..'Z') first.toString() else "#"
        }.toSortedMap()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        grouped.forEach { (header, artistList) ->
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = header,
                    color = FalconRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            items(artistList, key = { it.name }) { artist ->
                ArtistGridCard(
                    artist = artist,
                    onPlay = { onPlayArtist(artist) },
                    onAddToPlaylist = { onAddToPlaylist(artist) }
                )
            }
        }
    }
}

@Composable
private fun ArtistGridCard(
    artist: ArtistItem,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(FalconSurface)
        ) {
            AudioAlbumArtImage(
                albumArtUri = artist.coverUri,
                contentDescription = artist.name,
                modifier = Modifier.fillMaxSize(),
                fallbackIcon = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = FalconTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            )

            // Circular Play Overlay Button on Bottom Right
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
                    .size(38.dp)
                    .background(Color.White, CircleShape)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Artist",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    color = FalconTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${artist.albumCount} album${if (artist.albumCount != 1) "s" else ""}",
                    color = FalconTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Artist Options",
                        tint = FalconTextPrimary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(FalconSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play", color = FalconTextPrimary) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = FalconRed) },
                        onClick = {
                            showMenu = false
                            onPlay()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = FalconTextPrimary) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = FalconRed) },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumsTabContent(
    albums: List<AlbumItem>,
    onPlayAlbum: (AlbumItem) -> Unit,
    onAddToPlaylist: (AlbumItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumGridCard(
                album = album,
                onPlay = { onPlayAlbum(album) },
                onAddToPlaylist = { onAddToPlaylist(album) }
            )
        }
    }
}

@Composable
private fun AlbumGridCard(
    album: AlbumItem,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(FalconSurface)
        ) {
            AudioAlbumArtImage(
                albumArtUri = album.albumArtUri,
                contentDescription = album.title,
                modifier = Modifier.fillMaxSize(),
                fallbackIcon = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = FalconTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            )

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
                    .size(38.dp)
                    .background(Color.White, CircleShape)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Album",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.title,
                    color = FalconTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = album.artist,
                    color = FalconTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Album Options",
                        tint = FalconTextPrimary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(FalconSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play", color = FalconTextPrimary) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = FalconRed) },
                        onClick = {
                            showMenu = false
                            onPlay()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = FalconTextPrimary) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = FalconRed) },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TracksTabContent(
    tracks: List<AudioItem>,
    onPlayTrack: (AudioItem) -> Unit,
    onAddToPlaylist: (AudioItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tracks, key = { it.id }) { track ->
            AudioTrackCard(
                track = track,
                onPlay = { onPlayTrack(track) },
                onAddToPlaylist = { onAddToPlaylist(track) }
            )
        }
    }
}

@Composable
private fun AudioTrackCard(
    track: AudioItem,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(FalconSurface)
        ) {
            AudioAlbumArtImage(
                albumArtUri = track.albumArtUri,
                contentDescription = track.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                color = FalconTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                color = FalconTextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = track.durationFormatted,
            color = FalconTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Track Options",
                    tint = FalconTextPrimary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(FalconSurface)
            ) {
                DropdownMenuItem(
                    text = { Text("Play", color = FalconTextPrimary) },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = FalconRed) },
                    onClick = {
                        showMenu = false
                        onPlay()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = FalconTextPrimary) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = FalconRed) },
                    onClick = {
                        showMenu = false
                        onAddToPlaylist()
                    }
                )
            }
        }
    }
}

@Composable
private fun GenresTabContent(
    genres: List<GenreItem>,
    onPlayGenre: (GenreItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(genres, key = { it.name }) { genre ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayGenre(genre) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(FalconSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = FalconRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = genre.name,
                        color = FalconTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${genre.trackCount} track${if (genre.trackCount != 1) "s" else ""}",
                        color = FalconTextSecondary,
                        fontSize = 13.sp
                    )
                }

                IconButton(onClick = { onPlayGenre(genre) }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Genre",
                        tint = FalconTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AudioLogoIcon(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.15f, height * 0.15f)
            lineTo(width * 0.85f, height * 0.50f)
            lineTo(width * 0.15f, height * 0.85f)
            lineTo(width * 0.35f, height * 0.50f)
            close()
        }
        drawPath(path, color = FalconRed)
    }
}
