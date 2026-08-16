package com.example.falconplayer.ui.browse

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.falconplayer.data.BrowseFileItem
import com.example.falconplayer.data.BrowseFolderItem
import com.example.falconplayer.data.BrowseFolderType
import com.example.falconplayer.data.BrowseNodeItem
import com.example.falconplayer.data.StorageItem
import com.example.falconplayer.theme.FalconBackground
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.theme.FalconTextPrimary
import com.example.falconplayer.theme.FalconTextSecondary
import com.example.falconplayer.ui.home.FalconLogoIcon

@Composable
fun BrowseScreen(
    onPlayMedia: (uri: Uri?, title: String?) -> Unit,
    onAddToPlaylist: ((Uri, String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: BrowseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showOptionsMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    BackHandler(enabled = uiState.currentPath != null || uiState.isSearching) {
        viewModel.navigateUp()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconBackground)
    ) {
        // TOP APP BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FalconBackground)
                .statusBarsPadding()
        ) {
            if (uiState.isSearching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.closeSearch() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close Search",
                            tint = FalconTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = {
                            Text(
                                text = "Search files & folders...",
                                color = FalconTextSecondary,
                                fontSize = 16.sp
                            )
                        },
                        singleLine = true,
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = FalconTextSecondary
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = FalconSurface,
                            unfocusedContainerColor = FalconSurface,
                            focusedTextColor = FalconTextPrimary,
                            unfocusedTextColor = FalconTextPrimary,
                            cursorColor = FalconRed,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .focusRequester(focusRequester)
                    )
                }
            } else if (uiState.currentPath != null) {
                // Directory View Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FalconTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.currentFolderName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FalconTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.openSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = FalconTextPrimary
                        )
                    }
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = FalconTextPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier.background(FalconSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Refresh", color = FalconTextPrimary) },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.refresh()
                                }
                            )
                        }
                    }
                }
            } else {
                // Root Browse Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FalconLogoIcon(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Falcon",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FalconTextPrimary
                        )
                    }

                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = FalconTextPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier.background(FalconSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Refresh", color = FalconTextPrimary) },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.refresh()
                                }
                            )
                        }
                    }
                }
            }
        }

        // CONTENT AREA
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FalconRed)
                }
            } else if (uiState.currentPath == null) {
                // ROOT BROWSE SCREEN (Favorites, Storages, Local Network)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    // SECTION 1: FAVORITES
                    item {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Favorites",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FalconRed,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            if (uiState.favorites.isEmpty()) {
                                Text(
                                    text = "No favorite folders found.",
                                    color = FalconTextSecondary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            } else {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(uiState.favorites, key = { it.path }) { favorite ->
                                        FavoriteFolderCard(
                                            folder = favorite,
                                            onClick = { viewModel.openDirectory(favorite.path, favorite.name) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 2: STORAGES
                    item {
                        Column(modifier = Modifier.padding(top = 28.dp)) {
                            Text(
                                text = "Storages",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FalconRed,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(uiState.storages, key = { it.path }) { storage ->
                                    StorageCard(
                                        storage = storage,
                                        onClick = { viewModel.openDirectory(storage.path, storage.name) }
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 3: LOCAL NETWORK
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 36.dp, bottom = 24.dp)
                        ) {
                            Text(
                                text = "Local Network",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FalconRed,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                            Text(
                                text = "Looking for network shares.",
                                color = FalconTextSecondary,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                // DIRECTORY VIEW (Subfolders & Media files)
                if (uiState.filteredContents.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Empty Folder",
                            tint = FalconTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No files or folders found",
                            color = FalconTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) {
                                "No matches found for \"${uiState.searchQuery}\""
                            } else {
                                "This directory contains no media files or subfolders."
                            },
                            color = FalconTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(uiState.filteredContents, key = { node ->
                            when (node) {
                                is BrowseNodeItem.FolderNode -> "folder_${node.folder.path}"
                                is BrowseNodeItem.FileNode -> "file_${node.file.path}"
                            }
                        }) { node ->
                            when (node) {
                                is BrowseNodeItem.FolderNode -> {
                                    DirectoryFolderRow(
                                        folder = node.folder,
                                        onClick = { viewModel.openDirectory(node.folder.path, node.folder.name) }
                                    )
                                }
                                is BrowseNodeItem.FileNode -> {
                                    DirectoryFileRow(
                                        file = node.file,
                                        onClick = { onPlayMedia(node.file.uri, node.file.name) },
                                        onAddToPlaylist = { onAddToPlaylist?.invoke(node.file.uri, node.file.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteFolderCard(
    folder: BrowseFolderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(136.dp)
            .height(124.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(FalconSurface)
            .border(BorderStroke(1.dp, Color(0xFF2C2C2E)), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon in top center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FolderTypeIcon(
                    iconType = folder.iconType,
                    modifier = Modifier.size(52.dp)
                )
            }

            // Bottom Text & 3-dots Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        color = FalconTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${folder.folderCount} 📁 • ${folder.fileCount} 📄",
                        color = FalconTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = FalconTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(FalconSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open", color = FalconTextPrimary) },
                            onClick = {
                                showMenu = false
                                onClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StorageCard(
    storage: StorageItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(136.dp)
            .height(124.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(FalconSurface)
            .border(BorderStroke(1.dp, Color(0xFF2C2C2E)), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Folder icon center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = storage.name,
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier.size(52.dp)
                )
            }

            // Bottom Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = storage.name,
                        color = FalconTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${storage.folderCount} 📁 • ${storage.fileCount} 📄",
                        color = FalconTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = FalconTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(FalconSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Storage", color = FalconTextPrimary) },
                            onClick = {
                                showMenu = false
                                onClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FolderTypeIcon(
    iconType: BrowseFolderType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = Color(0xFF8E8E93),
            modifier = Modifier.fillMaxSize()
        )
        when (iconType) {
            BrowseFolderType.DOWNLOAD -> {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Download",
                    tint = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
            BrowseFolderType.MOVIES -> {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Movies",
                    tint = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
            BrowseFolderType.MUSIC -> {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
            BrowseFolderType.GENERIC -> {}
        }
    }
}

@Composable
fun DirectoryFolderRow(
    folder: BrowseFolderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = "Folder",
            tint = FalconRed,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                color = FalconTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${folder.folderCount} subfolders • ${folder.fileCount} media files",
                color = FalconTextSecondary,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = FalconTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DirectoryFileRow(
    file: BrowseFileItem,
    onClick: () -> Unit,
    onAddToPlaylist: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isVideo) Icons.Default.Movie else Icons.Default.Audiotrack,
            contentDescription = if (file.isVideo) "Video File" else "Audio File",
            tint = FalconRed,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                color = FalconTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = file.formattedSize,
                color = FalconTextSecondary,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = FalconTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(FalconSurface)
            ) {
                DropdownMenuItem(
                    text = { Text("Play", color = FalconTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = FalconRed
                        )
                    },
                    onClick = {
                        showMenu = false
                        onClick()
                    }
                )
                onAddToPlaylist?.let { addToPlaylist ->
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = FalconTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = null,
                                tint = FalconRed
                            )
                        },
                        onClick = {
                            showMenu = false
                            addToPlaylist()
                        }
                    )
                }
            }
        }
    }
}
