package com.example.falconplayer.ui.player.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.ui.player.TrackItem
import com.example.falconplayer.ui.player.TrackTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackSelectionSheet(
    sheetState: SheetState,
    initialTab: TrackTab,
    audioTracks: List<TrackItem>,
    selectedAudioTrackId: String?,
    onAudioTrackSelect: (TrackItem) -> Unit,
    subtitleTracks: List<TrackItem>,
    selectedSubtitleTrackId: String?,
    onSubtitleTrackSelect: (TrackItem) -> Unit,
    onDisableSubtitles: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = FalconSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tracks & Subtitles",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isAudioActive = activeTab == TrackTab.AUDIO
                val audioBg by animateColorAsState(
                    targetValue = if (isAudioActive) FalconRed else Color.Transparent,
                    label = "audioTabBg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(audioBg)
                        .clickable { activeTab = TrackTab.AUDIO }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Audiotrack,
                            contentDescription = null,
                            tint = if (isAudioActive) Color.White else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Audio (${audioTracks.size})",
                            color = if (isAudioActive) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = if (isAudioActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                val isSubActive = activeTab == TrackTab.SUBTITLE
                val subBg by animateColorAsState(
                    targetValue = if (isSubActive) FalconRed else Color.Transparent,
                    label = "subTabBg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(subBg)
                        .clickable { activeTab = TrackTab.SUBTITLE }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Subtitles,
                            contentDescription = null,
                            tint = if (isSubActive) Color.White else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Subtitles (${subtitleTracks.size})",
                            color = if (isSubActive) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = if (isSubActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Track Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activeTab == TrackTab.AUDIO) {
                    if (audioTracks.isEmpty()) {
                        item {
                            EmptyTrackPlaceholder(
                                message = "No audio tracks detected in this video",
                                icon = Icons.Filled.Audiotrack
                            )
                        }
                    } else {
                        items(audioTracks, key = { it.id }) { track ->
                            val isSelected = track.id == selectedAudioTrackId
                            TrackItemRow(
                                title = track.name,
                                subtitle = track.technicalDetails,
                                isSelected = isSelected,
                                onClick = { onAudioTrackSelect(track) }
                            )
                        }
                    }
                } else {
                    // Subtitles Tab: Always has "Off" at the top
                    item {
                        val isOff = selectedSubtitleTrackId == null
                        TrackItemRow(
                            title = "Off",
                            subtitle = "No subtitles displayed",
                            isSelected = isOff,
                            onClick = onDisableSubtitles
                        )
                    }

                    if (subtitleTracks.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(subtitleTracks, key = { it.id }) { track ->
                            val isSelected = track.id == selectedSubtitleTrackId
                            TrackItemRow(
                                title = track.name,
                                subtitle = track.technicalDetails,
                                isSelected = isSelected,
                                onClick = { onSubtitleTrackSelect(track) }
                            )
                        }
                    } else {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            EmptyTrackPlaceholder(
                                message = "No embedded subtitle tracks available",
                                icon = Icons.Filled.SubtitlesOff
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackItemRow(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) FalconRed.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.04f),
        label = "trackItemBg"
    )
    val borderColor = if (isSelected) FalconRed.copy(alpha = 0.6f) else Color.Transparent

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio/Selection Icon
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.background(FalconRed)
                        } else {
                            Modifier.border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(FalconRed)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTrackPlaceholder(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
