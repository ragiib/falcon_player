package com.example.falconplayer.ui.playlist

import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.falconplayer.data.VideoItem
import com.example.falconplayer.data.formatDuration
import com.example.falconplayer.theme.FalconBackground
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.theme.FalconSurfaceVariant
import com.example.falconplayer.theme.FalconTextPrimary
import com.example.falconplayer.theme.FalconTextSecondary
import com.example.falconplayer.ui.components.VideoThumbnailImage
import com.example.falconplayer.ui.playlist.components.DeletePlaylistDialog
import com.example.falconplayer.ui.playlist.components.RenamePlaylistDialog

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onBackClick: () -> Unit,
    onPlayVideoInPlaylist: (uri: Uri?, title: String?, playlistId: String, index: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val detailStateFlow = remember(playlistId) { viewModel.getPlaylistDetailState(playlistId) }
    val detailState by detailStateFlow.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSelectVideosDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(playlistId) {
        viewModel.loadAvailableVideos()
    }

    val playlist = detailState.playlist

    if (playlist == null && !detailState.isLoading) {
        // Playlist deleted or not found
        onBackClick()
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FalconBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .background(FalconBackground)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = FalconTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = playlist?.name ?: "Playlist",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FalconTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val totalVideoCount = detailState.videos.size
                            Text(
                                text = "$totalVideoCount video${if (totalVideoCount != 1) "s" else ""} • ${formatDuration(detailState.totalDurationMs)}",
                                fontSize = 12.sp,
                                color = FalconTextSecondary
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = FalconTextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(FalconSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Videos", color = FalconTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = FalconRed
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.loadAvailableVideos()
                                    showSelectVideosDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rename Playlist", color = FalconTextPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = FalconRed
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showRenameDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Playlist", color = FalconRed) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = FalconRed
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                detailState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FalconRed)
                    }
                }

                detailState.videos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                            contentDescription = "Empty Playlist",
                            tint = FalconTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Playlist is Empty",
                            color = FalconTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No videos have been added to this playlist yet. Tap below to select and add videos.",
                            color = FalconTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                viewModel.loadAvailableVideos()
                                showSelectVideosDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FalconRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "+ Add Videos", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Play All Action Button & Add Videos Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val firstVideo = detailState.videos.firstOrNull()
                                        if (firstVideo != null && playlist != null) {
                                            onPlayVideoInPlaylist(firstVideo.contentUri, firstVideo.title, playlist.id, 0)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FalconRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play All",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Play All", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.loadAvailableVideos()
                                        showSelectVideosDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FalconSurfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Videos",
                                        tint = FalconRed
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Add Videos", color = FalconTextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        itemsIndexed(detailState.videos, key = { _, video -> video.id }) { index, video ->
                            PlaylistVideoRowItem(
                                video = video,
                                index = index,
                                totalCount = detailState.videos.size,
                                onClick = {
                                    playlist?.let { pl ->
                                        onPlayVideoInPlaylist(video.contentUri, video.title, pl.id, index)
                                    }
                                },
                                onMoveUp = {
                                    playlist?.let { pl ->
                                        viewModel.moveVideoInPlaylist(pl.id, index, index - 1)
                                    }
                                },
                                onMoveDown = {
                                    playlist?.let { pl ->
                                        viewModel.moveVideoInPlaylist(pl.id, index, index + 1)
                                    }
                                },
                                onRemoveFromPlaylist = {
                                    playlist?.let { pl ->
                                        viewModel.removeVideoFromPlaylist(pl.id, video.contentUri.toString())
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog && playlist != null) {
        RenamePlaylistDialog(
            initialName = playlist.name,
            onDismiss = { showRenameDialog = false },
            onRename = { newName ->
                val err = viewModel.renamePlaylist(playlist.id, newName)
                if (err == null) {
                    showRenameDialog = false
                }
                err
            }
        )
    }

    if (showDeleteDialog && playlist != null) {
        DeletePlaylistDialog(
            playlistName = playlist.name,
            onDismiss = { showDeleteDialog = false },
            onConfirmDelete = {
                viewModel.deletePlaylist(playlist.id)
                showDeleteDialog = false
                onBackClick()
            }
        )
    }

    if (showSelectVideosDialog && playlist != null) {
        val availableVideos by viewModel.availableVideos.collectAsStateWithLifecycle()
        val alreadyAddedUris = remember(playlist) { playlist.videoUris.toSet() }

        com.example.falconplayer.ui.playlist.components.SelectVideosForPlaylistDialog(
            allAvailableVideos = availableVideos,
            alreadyAddedUris = alreadyAddedUris,
            onDismiss = { showSelectVideosDialog = false },
            onAddSelectedVideos = { selectedList ->
                viewModel.addVideosToPlaylist(playlist.id, selectedList)
                showSelectVideosDialog = false
            }
        )
    }
}

@Composable
fun PlaylistVideoRowItem(
    video: VideoItem,
    index: Int,
    totalCount: Int,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemoveFromPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FalconSurface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .width(100.dp)
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black)
        ) {
            VideoThumbnailImage(
                videoUri = video.contentUri,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = video.durationFormatted,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Duration
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = video.title,
                color = FalconTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = video.durationFormatted,
                color = FalconTextSecondary,
                fontSize = 12.sp
            )
        }

        // 3-dot Menu
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = FalconTextSecondary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(FalconSurfaceVariant)
            ) {
                if (index > 0) {
                    DropdownMenuItem(
                        text = { Text("Move Up", color = FalconTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = FalconTextPrimary
                            )
                        },
                        onClick = {
                            showMenu = false
                            onMoveUp()
                        }
                    )
                }
                if (index < totalCount - 1) {
                    DropdownMenuItem(
                        text = { Text("Move Down", color = FalconTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = FalconTextPrimary
                            )
                        },
                        onClick = {
                            showMenu = false
                            onMoveDown()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Remove from Playlist", color = FalconRed) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = null,
                            tint = FalconRed
                        )
                    },
                    onClick = {
                        showMenu = false
                        onRemoveFromPlaylist()
                    }
                )
            }
        }
    }
}
