package com.example.falconplayer.ui.player.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.theme.FalconSurface
import com.example.falconplayer.ui.player.PlayerUiState

@Composable
fun VideoInfoDialog(
    uiState: PlayerUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationSec = uiState.mediaInfo.durationMs / 1000
    val durationFormatted = if (durationSec > 3600) {
        String.format("%d:%02d:%02d", durationSec / 3600, (durationSec % 3600) / 60, durationSec % 60)
    } else {
        String.format("%02d:%02d", (durationSec % 3600) / 60, durationSec % 60)
    }

    val resolutionStr = if (uiState.videoWidth > 0 && uiState.videoHeight > 0) {
        "${uiState.videoWidth} × ${uiState.videoHeight}"
    } else "Auto Detect"

    val activeAudio = uiState.audioTracks.find { it.id == uiState.selectedAudioTrackId }?.name ?: "Default"
    val activeSubtitle = uiState.subtitleTracks.find { it.id == uiState.selectedSubtitleTrackId }?.name ?: "Off"

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = FalconSurface,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = FalconRed,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Media Information",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(bottom = 12.dp))

                InfoRow("File Title", uiState.mediaInfo.title)
                InfoRow("Resolution", resolutionStr)
                InfoRow("Duration", durationFormatted)
                InfoRow("Audio Track", activeAudio)
                InfoRow("Subtitles", activeSubtitle)
                InfoRow("Scaling Mode", uiState.resizeMode.displayName)
                InfoRow("Playback Speed", "${uiState.playbackSpeed}x")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = FalconRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
