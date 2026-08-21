package com.example.falconplayer.ui.home

import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.falconplayer.data.VideoItem
import com.example.falconplayer.theme.FalconBackground
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.theme.FalconSurfaceVariant
import com.example.falconplayer.theme.FalconTextPrimary
import com.example.falconplayer.theme.FalconTextSecondary
import com.example.falconplayer.ui.components.VideoThumbnailImage

@Composable
fun MoreScreen(
    historyVideos: List<VideoItem>,
    incognitoMode: Boolean,
    onOpenDisplaySettings: () -> Unit,
    onToggleIncognitoMode: () -> Unit,
    onRefresh: () -> Unit,
    onOpenHistory: () -> Unit,
    onPlayMedia: (uri: Uri?, title: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showNewStreamDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FalconBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Top App Bar
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
                IconButton(onClick = { showDropdown = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = FalconTextPrimary
                    )
                }
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier.background(FalconSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Display settings", color = FalconTextPrimary) },
                        onClick = {
                            showDropdown = false
                            onOpenDisplaySettings()
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
                                    checked = incognitoMode,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = FalconRed,
                                        uncheckedColor = FalconTextSecondary
                                    )
                                )
                            }
                        },
                        onClick = {
                            onToggleIncognitoMode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Refresh", color = FalconTextPrimary) },
                        onClick = {
                            showDropdown = false
                            onRefresh()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("About", color = FalconTextPrimary) },
                        onClick = {
                            showDropdown = false
                            showAboutDialog = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. SETTINGS & ABOUT Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SETTINGS Button
            OutlinedButton(
                onClick = onOpenDisplaySettings,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, FalconRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FalconRed)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = FalconRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SETTINGS",
                        color = FalconRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // ABOUT Button
            OutlinedButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, FalconRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FalconRed)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = FalconRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ABOUT",
                        color = FalconRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Streams Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNewStreamDialog = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Streams",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FalconRed
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Streams",
                    tint = FalconRed
                )
            }

            Box(
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp, bottom = 16.dp)
                    .width(160.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FalconSurface)
                    .border(1.dp, FalconSurfaceVariant, RoundedCornerShape(8.dp))
                    .clickable { showNewStreamDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New stream",
                        tint = FalconRed,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "New stream",
                        color = FalconTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. History Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenHistory() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FalconRed
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "History",
                    tint = FalconRed
                )
            }

            if (historyVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(FalconSurface, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent watch history",
                        color = FalconTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(historyVideos, key = { "more_history_${it.id}" }) { video ->
                        Column(
                            modifier = Modifier
                                .width(150.dp)
                                .clickable { onPlayMedia(video.contentUri, video.title) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FalconSurface)
                            ) {
                                VideoThumbnailImage(
                                    videoUri = video.contentUri,
                                    contentDescription = video.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .align(Alignment.BottomStart)
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
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
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(88.dp)) // Clearance for bottom bar
    }

    // DIALOGS
    if (showNewStreamDialog) {
        var streamUrl by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewStreamDialog = false },
            containerColor = FalconSurface,
            title = {
                Text("Open Network Stream", color = FalconTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Enter network stream URL (e.g. http, rtsp, m3u8):",
                        color = FalconTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = streamUrl,
                        onValueChange = { streamUrl = it },
                        placeholder = { Text("http://example.com/stream.mp4", color = FalconTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FalconTextPrimary,
                            unfocusedTextColor = FalconTextPrimary,
                            focusedBorderColor = FalconRed,
                            unfocusedBorderColor = FalconSurfaceVariant,
                            cursorColor = FalconRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (streamUrl.isNotBlank()) {
                            showNewStreamDialog = false
                            onPlayMedia(Uri.parse(streamUrl.trim()), "Network Stream")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FalconRed)
                ) {
                    Text("Play", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewStreamDialog = false }) {
                    Text("Cancel", color = FalconTextSecondary)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = FalconSurface,
            icon = {
                FalconLogoIcon(modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Falcon Player", color = FalconTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Version 1.0.0", color = FalconRed, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "A fast, modern Android media player built with Jetpack Compose, Media3, and ExoPlayer.",
                        color = FalconTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FalconRed)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}
