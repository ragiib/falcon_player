package com.example.falconplayer.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.falconplayer.data.FolderItem
import com.example.falconplayer.data.Playlist
import com.example.falconplayer.data.VideoItem
import com.example.falconplayer.theme.FalconBackground
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.theme.FalconSurfaceVariant
import com.example.falconplayer.theme.FalconTextPrimary
import com.example.falconplayer.theme.FalconTextSecondary
import com.example.falconplayer.ui.components.VideoThumbnailImage
import com.example.falconplayer.ui.audio.AudioScreen
import com.example.falconplayer.ui.audio.AudioViewModel
import com.example.falconplayer.ui.browse.BrowseScreen
import com.example.falconplayer.ui.playlist.PlaylistViewModel
import com.example.falconplayer.ui.playlist.components.AddToPlaylistDialog
import com.example.falconplayer.ui.playlist.components.CreatePlaylistDialog
import com.example.falconplayer.ui.playlist.components.DeletePlaylistDialog
import com.example.falconplayer.ui.playlist.components.RenamePlaylistDialog

sealed interface GridCardItem {
    data class Video(val item: VideoItem) : GridCardItem
    data class Folder(val item: FolderItem) : GridCardItem
}

@Composable
fun HomeScreen(
    onPlayMedia: (uri: Uri?, title: String?) -> Unit,
    onOpenPlaylist: (playlistId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    audioViewModel: AudioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val audioUiState by audioViewModel.uiState.collectAsStateWithLifecycle()
    val playlists by playlistViewModel.playlists.collectAsStateWithLifecycle()

    val showCreateDialog by playlistViewModel.showCreateDialog.collectAsStateWithLifecycle()
    val renameTarget by playlistViewModel.renameTarget.collectAsStateWithLifecycle()
    val deleteTarget by playlistViewModel.deleteTarget.collectAsStateWithLifecycle()
    val addToPlaylistVideos by playlistViewModel.addToPlaylistVideos.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = VIDEOS, 1 = PLAYLISTS
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onPlayMedia(it, it.lastPathSegment ?: "Selected Video")
        }
    }

    val focusRequester = remember { FocusRequester() }

    var showOptionsDropdown by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.showDisplaySettingsScreen || uiState.isSearching || uiState.isHistoryActive) {
        if (uiState.showDisplaySettingsScreen) {
            viewModel.closeDisplaySettings()
        } else if (uiState.isSearching) {
            viewModel.closeSearch()
        } else if (uiState.isHistoryActive) {
            viewModel.closeHistory()
        }
    }

    LaunchedEffect(uiState.isSearching) {
        if (uiState.isSearching) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(context, requiredPermission)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(requiredPermission)
        }
    }

    if (uiState.showDisplaySettingsScreen) {
        DisplaySettingsScreen(
            isListView = uiState.isListView,
            showOnlyFavorites = uiState.showOnlyFavorites,
            groupOption = uiState.groupOption,
            playbackAction = uiState.playbackAction,
            sortType = uiState.sortType,
            onBackClick = { viewModel.closeDisplaySettings() },
            onToggleListView = { viewModel.toggleListView() },
            onToggleShowOnlyFavorites = { viewModel.toggleShowOnlyFavorites() },
            onSelectGroupOption = { viewModel.setGroupOption(it) },
            onSelectPlaybackAction = { viewModel.setPlaybackAction(it) },
            onSelectSortType = { viewModel.setSortType(it) }
        )
    } else {
        Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FalconBackground,
        topBar = {
            if (selectedNavIndex == 0) {
                Column(
                    modifier = Modifier
                        .background(FalconBackground)
                        .statusBarsPadding()
                ) {
                if (uiState.isSearching) {
                    // Search Bar
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
                                    text = "Search videos...",
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
                } else if (uiState.isHistoryActive) {
                    // History Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.closeHistory() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Videos",
                                tint = FalconTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FalconTextPrimary
                        )
                    }
                } else {
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FalconLogoIcon(modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Falcon",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = FalconTextPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.openSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = FalconTextPrimary
                                )
                            }
                            IconButton(onClick = { viewModel.openHistory() }) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = FalconTextPrimary
                                )
                            }
                            Box {
                                IconButton(onClick = { showOptionsDropdown = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = FalconTextPrimary
                                    )
                                }
                                DropdownMenu(
                                    expanded = showOptionsDropdown,
                                    onDismissRequest = { showOptionsDropdown = false },
                                    modifier = Modifier.background(FalconSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Display settings", color = FalconTextPrimary) },
                                        onClick = {
                                            showOptionsDropdown = false
                                            viewModel.openDisplaySettings()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Incognito mode", color = FalconTextPrimary)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Checkbox(
                                                    checked = uiState.incognitoMode,
                                                    onCheckedChange = null,
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = FalconRed,
                                                        uncheckedColor = FalconTextSecondary
                                                    )
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.toggleIncognitoMode()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Refresh", color = FalconTextPrimary) },
                                        onClick = {
                                            showOptionsDropdown = false
                                            viewModel.loadMedia()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Tab Header: VIDEOS / PLAYLISTS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // VIDEOS Tab
                        Column(
                            modifier = Modifier
                                .clickable {
                                    selectedTab = 0
                                    selectedNavIndex = 0
                                    viewModel.clearFolderFilter()
                                }
                                .padding(end = 24.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "VIDEOS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) FalconRed else FalconTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (selectedTab == 0) {
                                Box(
                                    modifier = Modifier
                                        .width(64.dp)
                                        .height(3.dp)
                                        .background(FalconRed, shape = RoundedCornerShape(2.dp))
                                )
                            } else {
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }

                        // PLAYLISTS Tab
                        Column(
                            modifier = Modifier
                                .clickable {
                                    selectedTab = 1
                                    selectedNavIndex = 3
                                }
                                .padding(end = 24.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "PLAYLISTS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) FalconRed else FalconTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (selectedTab == 1) {
                                Box(
                                    modifier = Modifier
                                        .width(78.dp)
                                        .height(3.dp)
                                        .background(FalconRed, shape = RoundedCornerShape(2.dp))
                                )
                            } else {
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }

                    // Folder Filter Header (If a folder is selected in Videos tab)
                    if (selectedTab == 0 && uiState.selectedFolderBucketId != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FalconSurface)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.clearFolderFilter() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to all videos",
                                    tint = FalconRed
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.selectedFolderName ?: "Folder",
                                color = FalconTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${uiState.filteredVideos.size} videos)",
                                color = FalconTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    },
        floatingActionButton = {
            if (selectedNavIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        if (selectedTab == 1) {
                            playlistViewModel.openCreateDialog()
                        } else {
                            val firstVideo = uiState.filteredVideos.firstOrNull() ?: uiState.videos.firstOrNull()
                            if (firstVideo != null) {
                                onPlayMedia(firstVideo.contentUri, firstVideo.title)
                            } else {
                                videoPickerLauncher.launch(arrayOf("video/*"))
                            }
                        }
                    },
                    containerColor = FalconRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = if (selectedTab == 1) Icons.Default.Add else Icons.Default.PlayArrow,
                        contentDescription = if (selectedTab == 1) "Create Playlist" else "Play Media",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = FalconSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                val navItems = listOf(
                    Triple("Video", Icons.Default.Movie, 0),
                    Triple("Audio", Icons.Default.Audiotrack, 1),
                    Triple("Browse", Icons.Default.Folder, 2),
                    Triple("Playlists", Icons.AutoMirrored.Filled.PlaylistPlay, 3),
                    Triple("More", Icons.Default.MoreHoriz, 4)
                )

                navItems.forEach { (label, icon, index) ->
                    val isSelected = selectedNavIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedNavIndex = index
                            if (index == 0) {
                                selectedTab = 0
                            } else if (index == 3) {
                                selectedTab = 1
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FalconRed,
                            selectedTextColor = FalconRed,
                            unselectedIconColor = FalconTextSecondary,
                            unselectedTextColor = FalconTextSecondary,
                            indicatorColor = FalconSurfaceVariant
                        )
                    )
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
                selectedNavIndex == 1 -> {
                    AudioScreen(
                        uiState = audioUiState,
                        onTabSelected = audioViewModel::selectTab,
                        onSearchToggle = audioViewModel::toggleSearch,
                        onSearchQueryChange = audioViewModel::onSearchQueryChange,
                        onPlayTrack = { track -> onPlayMedia(track.contentUri, track.title) },
                        onPlayTracks = { tracks -> tracks.firstOrNull()?.let { onPlayMedia(it.contentUri, it.title) } },
                        onAddToPlaylist = { audioItem ->
                            playlistViewModel.openAddToPlaylistDialog(
                                listOf(
                                    VideoItem(
                                        id = audioItem.id,
                                        contentUri = audioItem.contentUri,
                                        title = audioItem.title,
                                        durationMs = audioItem.durationMs,
                                        width = 0,
                                        height = 0,
                                        sizeBytes = audioItem.sizeBytes,
                                        bucketId = "audio",
                                        bucketName = "Audio"
                                    )
                                )
                            )
                        }
                    )
                }

                selectedNavIndex == 2 -> {
                    BrowseScreen(
                        onPlayMedia = onPlayMedia,
                        onAddToPlaylist = { uri, title ->
                            playlistViewModel.openAddToPlaylistDialog(
                                listOf(
                                    VideoItem(
                                        id = uri.hashCode().toLong(),
                                        contentUri = uri,
                                        title = title,
                                        durationMs = 0L,
                                        width = 0,
                                        height = 0,
                                        sizeBytes = 0L,
                                        bucketId = "browse",
                                        bucketName = "Browse"
                                    )
                                )
                            )
                        }
                    )
                }

                // State 1: Permission Not Granted
                !uiState.hasPermission -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Permission Required",
                            tint = FalconRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Storage Permission Required",
                            color = FalconTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Falcon Player needs access to your device media storage to display and play your videos.",
                            color = FalconTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { permissionLauncher.launch(requiredPermission) },
                            colors = ButtonDefaults.buttonColors(containerColor = FalconRed)
                        ) {
                            Text(text = "Grant Permission", color = Color.White)
                        }
                    }
                }

                // State 2: Loading Media
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = FalconRed)
                    }
                }

                // State 3: Search Active - No Results
                uiState.isSearching && uiState.filteredVideos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No Results",
                            tint = FalconTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found",
                            color = FalconTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (uiState.searchQuery.isBlank()) {
                                "Type to search your video files."
                            } else {
                                "No videos found matching \"${uiState.searchQuery}\""
                            },
                            color = FalconTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // State 4: Search Active - Matching Videos List
                uiState.isSearching -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = 80.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredVideos, key = { "search_video_${it.id}" }) { video ->
                            RealVideoCard(
                                video = video,
                                onClick = { onPlayMedia(video.contentUri, video.title) },
                                onAddToPlaylist = { playlistViewModel.openAddToPlaylistDialog(listOf(video)) }
                            )
                        }
                    }
                }

                // State 5: History Active - Empty State
                uiState.isHistoryActive && uiState.historyVideos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No Watch History",
                            tint = FalconTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Watch History",
                            color = FalconTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Videos you play will appear here.",
                            color = FalconTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // State 6: History Active - History List
                uiState.isHistoryActive -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = 80.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.historyVideos, key = { "history_video_${it.id}" }) { video ->
                            RealVideoCard(
                                video = video,
                                onClick = {
                                    viewModel.recordVideoPlayed(video.contentUri.toString())
                                    onPlayMedia(video.contentUri, video.title)
                                },
                                onAddToPlaylist = { playlistViewModel.openAddToPlaylistDialog(listOf(video)) }
                            )
                        }
                    }
                }

                // VIEWPORT FOR PLAYLISTS TAB
                selectedTab == 1 -> {
                    if (playlists.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                                contentDescription = "No Playlists",
                                tint = FalconTextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Playlists Yet",
                                color = FalconTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create custom playlists to organize your favorite video clips.",
                                color = FalconTextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { playlistViewModel.openCreateDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = FalconRed)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Create Playlist", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                end = 12.dp,
                                top = 12.dp,
                                bottom = 80.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(playlists, key = { it.id }) { playlist ->
                                RealPlaylistCard(
                                    playlist = playlist,
                                    allVideos = uiState.videos,
                                    onClick = { onOpenPlaylist(playlist.id) },
                                    onRename = { playlistViewModel.openRenameDialog(playlist) },
                                    onDelete = { playlistViewModel.openDeleteDialog(playlist) }
                                )
                            }
                        }
                    }
                }

                // State 3: Empty Video List
                uiState.videos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "No Videos Found",
                            tint = FalconTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Videos Found",
                            color = FalconTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No video files were detected on this device. You can also pick a video manually.",
                            color = FalconTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = FalconRed)
                        ) {
                            Text(text = "Open File Picker", color = Color.White)
                        }
                    }
                }

                // State 4: Display Real Videos & Folders Grid
                else -> {
                    val gridItems: List<GridCardItem> = remember(uiState) {
                        if (uiState.selectedFolderBucketId != null) {
                            uiState.filteredVideos.map { GridCardItem.Video(it) }
                        } else {
                            val folderCards = uiState.sortedFolders.map { GridCardItem.Folder(it) }
                            val videoCards = uiState.filteredVideos.map { GridCardItem.Video(it) }
                            folderCards + videoCards
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (uiState.isListView) 1 else 2),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = 80.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(gridItems, key = { item ->
                            when (item) {
                                is GridCardItem.Video -> "video_${item.item.id}"
                                is GridCardItem.Folder -> "folder_${item.item.bucketId}"
                            }
                        }) { item ->
                            when (item) {
                                is GridCardItem.Video -> {
                                    val isFav = item.item.contentUri.toString() in uiState.favoriteUris
                                    if (uiState.isListView) {
                                        RealVideoListCard(
                                            video = item.item,
                                            isFavorite = isFav,
                                            onClick = { onPlayMedia(item.item.contentUri, item.item.title) },
                                            onAddToPlaylist = { playlistViewModel.openAddToPlaylistDialog(listOf(item.item)) },
                                            onToggleFavorite = { viewModel.toggleFavorite(item.item.contentUri.toString()) }
                                        )
                                    } else {
                                        RealVideoCard(
                                            video = item.item,
                                            isFavorite = isFav,
                                            onClick = { onPlayMedia(item.item.contentUri, item.item.title) },
                                            onAddToPlaylist = { playlistViewModel.openAddToPlaylistDialog(listOf(item.item)) },
                                            onToggleFavorite = { viewModel.toggleFavorite(item.item.contentUri.toString()) }
                                        )
                                    }
                                }

                                is GridCardItem.Folder -> {
                                    if (uiState.isListView) {
                                        RealFolderListCard(
                                            folder = item.item,
                                            onClick = { viewModel.selectFolder(item.item.bucketId, item.item.bucketName) }
                                        )
                                    } else {
                                        RealFolderCard(
                                            folder = item.item,
                                            onClick = { viewModel.selectFolder(item.item.bucketId, item.item.bucketName) }
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

    // DIALOGS
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = playlistViewModel::closeCreateDialog,
            onCreate = { name -> playlistViewModel.createPlaylist(name) }
        )
    }

    renameTarget?.let { target ->
        RenamePlaylistDialog(
            initialName = target.name,
            onDismiss = playlistViewModel::closeRenameDialog,
            onRename = { newName -> playlistViewModel.renamePlaylist(target.id, newName) }
        )
    }

    deleteTarget?.let { target ->
        DeletePlaylistDialog(
            playlistName = target.name,
            onDismiss = playlistViewModel::closeDeleteDialog,
            onConfirmDelete = { playlistViewModel.deletePlaylist(target.id) }
        )
    }

    if (addToPlaylistVideos.isNotEmpty()) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = playlistViewModel::closeAddToPlaylistDialog,
            onSelectPlaylist = { playlistId ->
                val target = playlists.find { it.id == playlistId }
                playlistViewModel.addVideosToPlaylist(playlistId, addToPlaylistVideos)
                Toast.makeText(context, "Added to ${target?.name ?: "Playlist"}", Toast.LENGTH_SHORT).show()
            },
            onCreateNewPlaylist = {
                val videosToPass = addToPlaylistVideos
                playlistViewModel.closeAddToPlaylistDialog()
                playlistViewModel.openCreateDialog()
            }
        )
    }
    }
}

@Composable
fun RealVideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(FalconSurface)
        ) {
            VideoThumbnailImage(
                videoUri = video.contentUri,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize()
            )

            video.resolutionBadge?.let { badgeText ->
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top Right 3-dots Menu Button
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Item Menu",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(FalconSurface)
                ) {
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
                            onAddToPlaylist()
                        }
                    )
                    onToggleFavorite?.let { toggleFav ->
                        DropdownMenuItem(
                            text = { Text(if (isFavorite) "Remove Favorite" else "Add to Favorites", color = FalconTextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = FalconRed
                                )
                            },
                            onClick = {
                                showMenu = false
                                toggleFav()
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.durationFormatted,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = video.title,
            color = FalconTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = video.durationFormatted,
            color = FalconTextSecondary,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
fun RealVideoListCard(
    video: VideoItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val subtitle = remember(video) {
        if (!video.resolutionBadge.isNullOrBlank()) {
            "${video.durationFormatted} • ${video.resolutionBadge}"
        } else {
            video.durationFormatted
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(FalconSurface)
        ) {
            VideoThumbnailImage(
                videoUri = video.contentUri,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = video.title,
                color = FalconTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = FalconTextSecondary,
                fontSize = 13.sp,
                maxLines = 1
            )
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
                        onAddToPlaylist()
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (isFavorite) "Remove Favorite" else "Add to Favorites", color = FalconTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = FalconRed
                        )
                    },
                    onClick = {
                        showMenu = false
                        onToggleFavorite()
                    }
                )
            }
        }
    }
}

@Composable
fun RealPlaylistCard(
    playlist: Playlist,
    allVideos: List<VideoItem>,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val videoMap = remember(allVideos) { allVideos.associateBy { it.contentUri.toString() } }
    val previewVideos = remember(playlist, videoMap) {
        playlist.videoUris.take(4).mapNotNull { videoMap[it] }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(FalconSurface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF2A2A2A))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        previewVideos.getOrNull(0)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF333333))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        previewVideos.getOrNull(1)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF222222))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        previewVideos.getOrNull(2)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF1F1F1F))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        previewVideos.getOrNull(3)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }

            // Top Right 3-dots Menu Button
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Playlist Menu",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(FalconSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename", color = FalconTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = FalconRed
                            )
                        },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = FalconRed) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = FalconRed
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = playlist.name,
            color = FalconTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${playlist.videoUris.size} video${if (playlist.videoUris.size != 1) "s" else ""}",
            color = FalconTextSecondary,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
fun RealFolderCard(
    folder: FolderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(FalconSurface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF2A2A2A))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        folder.previewVideos.getOrNull(0)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF333333))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        folder.previewVideos.getOrNull(1)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF222222))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        folder.previewVideos.getOrNull(2)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF1F1F1F))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        folder.previewVideos.getOrNull(3)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Folder Menu",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = folder.bucketName,
            color = FalconTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${folder.videoCount} video${if (folder.videoCount > 1) "s" else ""}",
            color = FalconTextSecondary,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
fun RealFolderListCard(
    folder: FolderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(FalconSurface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF2A2A2A))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        folder.previewVideos.getOrNull(0)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF333333))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        folder.previewVideos.getOrNull(1)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF222222))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        folder.previewVideos.getOrNull(2)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF1F1F1F))
                            .border(0.5.dp, FalconBackground)
                    ) {
                        folder.previewVideos.getOrNull(3)?.let {
                            VideoThumbnailImage(videoUri = it.contentUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = folder.bucketName,
                color = FalconTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${folder.videoCount} video${if (folder.videoCount != 1) "s" else ""}",
                color = FalconTextSecondary,
                fontSize = 13.sp,
                maxLines = 1
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Folder Menu",
                    tint = FalconTextPrimary
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(FalconSurface)
            ) {
                DropdownMenuItem(
                    text = { Text("Open Folder", color = FalconTextPrimary) },
                    onClick = {
                        showMenu = false
                        onClick()
                    }
                )
            }
        }
    }
}

@Composable
fun FalconLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(width * 0.15f, height * 0.15f)
            lineTo(width * 0.85f, height * 0.50f)
            lineTo(width * 0.15f, height * 0.85f)
            lineTo(width * 0.35f, height * 0.50f)
            close()
        }
        drawPath(path, color = FalconRed)
    }
}
