package com.example.falconplayer.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.falconplayer.data.VideoItem
import com.example.falconplayer.theme.FalconBackground
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.theme.FalconSurfaceVariant
import com.example.falconplayer.theme.FalconTextPrimary
import com.example.falconplayer.theme.FalconTextSecondary
import com.example.falconplayer.ui.components.VideoThumbnailImage

sealed interface GridCardItem {
    data class Video(val item: VideoItem) : GridCardItem
    data class Folder(val item: FolderItem) : GridCardItem
}

@Composable
fun HomeScreen(
    onPlayMedia: (uri: Uri?, title: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
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

    LaunchedEffect(Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(context, requiredPermission)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(requiredPermission)
        }
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
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = FalconTextPrimary
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = FalconTextPrimary
                            )
                        }
                        IconButton(onClick = { videoPickerLauncher.launch(arrayOf("video/*")) }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = FalconTextPrimary
                            )
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
                            .clickable { selectedTab = 1 }
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

                // Folder Filter Header (If a folder is selected)
                if (uiState.selectedFolderBucketId != null) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val firstVideo = uiState.filteredVideos.firstOrNull() ?: uiState.videos.firstOrNull()
                    if (firstVideo != null) {
                        onPlayMedia(firstVideo.contentUri, firstVideo.title)
                    } else {
                        videoPickerLauncher.launch(arrayOf("video/*"))
                    }
                },
                containerColor = FalconRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Media",
                    modifier = Modifier.size(32.dp)
                )
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
                        onClick = { selectedNavIndex = index },
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
                            // If filtering by folder, display that folder's videos
                            uiState.filteredVideos.map { GridCardItem.Video(it) }
                        } else {
                            // Display top folders first, then individual videos
                            val folderCards = uiState.folders.map { GridCardItem.Folder(it) }
                            val videoCards = uiState.videos.map { GridCardItem.Video(it) }
                            folderCards + videoCards
                        }
                    }

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
                        items(gridItems, key = { item ->
                            when (item) {
                                is GridCardItem.Video -> "video_${item.item.id}"
                                is GridCardItem.Folder -> "folder_${item.item.bucketId}"
                            }
                        }) { item ->
                            when (item) {
                                is GridCardItem.Video -> {
                                    RealVideoCard(
                                        video = item.item,
                                        onClick = { onPlayMedia(item.item.contentUri, item.item.title) }
                                    )
                                }

                                is GridCardItem.Folder -> {
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

@Composable
fun RealVideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // Real Video Thumbnail Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(FalconSurface)
        ) {
            // Asynchronous Real Thumbnail Image
            VideoThumbnailImage(
                videoUri = video.contentUri,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize()
            )

            // Top Left Resolution Badge (e.g. 1080p, 4K, 720p)
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
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Item Menu",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Bottom Left Duration Badge
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

        // Real Video Title
        Text(
            text = video.title,
            color = FalconTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Duration Subtext
        Text(
            text = video.durationFormatted,
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
        // Composite Folder Thumbnail Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(FalconSurface)
        ) {
            // 2x2 Thumbnail Grid for Folder Preview
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

            // Top Right 3-dots Menu Button
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

        // Folder Title
        Text(
            text = folder.bucketName,
            color = FalconTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Real Video Count Subtext
        Text(
            text = "${folder.videoCount} video${if (folder.videoCount > 1) "s" else ""}",
            color = FalconTextSecondary,
            fontSize = 12.sp,
            maxLines = 1
        )
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
