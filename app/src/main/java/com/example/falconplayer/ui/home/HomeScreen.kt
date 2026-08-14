package com.example.falconplayer.ui.home

import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.falconplayer.theme.FalconBackground
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.theme.FalconSurfaceVariant
import com.example.falconplayer.theme.FalconTextPrimary
import com.example.falconplayer.theme.FalconTextSecondary

data class MediaItemUi(
    val id: String,
    val title: String,
    val subtext: String,
    val isFolder: Boolean,
    val durationOrCount: String,
    val badge: String? = null,
    val videoUri: String? = null
)

// Sample media list mirroring the VLC layout with videos & folders
private val sampleMediaList = listOf(
    MediaItemUi("1", "143635 784138054", "0:22", isFolder = false, durationOrCount = "0:22", badge = "1080p", videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
    MediaItemUi("2", "1784664309531", "0:09", isFolder = false, durationOrCount = "0:09", badge = "✓", videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
    MediaItemUi("3", "202509", "4 videos", isFolder = true, durationOrCount = "4 videos", videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
    MediaItemUi("4", "202510", "3 videos", isFolder = true, durationOrCount = "3 videos", videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
    MediaItemUi("5", "20251117_032325", "0:15", isFolder = false, durationOrCount = "0:15", badge = "1080p", videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"),
    MediaItemUi("6", "202512", "9 videos", isFolder = true, durationOrCount = "9 videos", videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyplays.mp4"),
    MediaItemUi("7", "202601", "6 videos", isFolder = true, durationOrCount = "6 videos", videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdown.mp4"),
    MediaItemUi("8", "202602", "12 videos", isFolder = true, durationOrCount = "12 videos", videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4")
)

@Composable
fun HomeScreen(
    onPlayMedia: (uri: Uri?, title: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onPlayMedia(it, it.lastPathSegment ?: "Selected Video")
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
                        // Falcon Logo Icon in Red
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
                            .clickable { selectedTab = 0 }
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
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val defaultMedia = sampleMediaList.first()
                    onPlayMedia(Uri.parse(defaultMedia.videoUri), defaultMedia.title)
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
        // Main Content Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sampleMediaList, key = { it.id }) { item ->
                MediaGridCard(
                    item = item,
                    onClick = {
                        item.videoUri?.let { uriStr ->
                            onPlayMedia(Uri.parse(uriStr), item.title)
                        } ?: run {
                            videoPickerLauncher.launch(arrayOf("video/*"))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MediaGridCard(
    item: MediaItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // Thumbnail Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(FalconSurface)
        ) {
            if (item.isFolder) {
                // 2x2 Composite Thumbnail Grid for Folders
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF2A2A2A))
                                .border(0.5.dp, FalconBackground)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF333333))
                                .border(0.5.dp, FalconBackground)
                        )
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF222222))
                                .border(0.5.dp, FalconBackground)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF1F1F1F))
                                .border(0.5.dp, FalconBackground)
                        )
                    }
                }
            } else {
                // Video Preview Gradient Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2C2C2C),
                                    Color(0xFF1A1A1A)
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // Top Left Badge (e.g. 1080p or Checkmark)
            item.badge?.let { badgeText ->
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    if (badgeText == "✓") {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                    contentDescription = "Item Menu",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Bottom Left Badge (Duration or Video Count)
            if (!item.isFolder) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.durationOrCount,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title text
        Text(
            text = item.title,
            color = FalconTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtext (e.g. "0:22" or "4 videos")
        Text(
            text = item.subtext,
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
